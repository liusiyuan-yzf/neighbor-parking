package com.neighborparking.service;

import com.neighborparking.common.error.BusinessException;
import com.neighborparking.common.error.ErrorCode;
import com.neighborparking.domain.AvailabilitySlot;
import com.neighborparking.domain.Booking;
import com.neighborparking.domain.CheckRecord;
import com.neighborparking.domain.Complaint;
import com.neighborparking.domain.ParkingSpace;
import com.neighborparking.domain.Review;
import com.neighborparking.domain.Vehicle;
import com.neighborparking.domain.enums.BookingStatus;
import com.neighborparking.domain.enums.CheckType;
import com.neighborparking.domain.enums.SpaceStatus;
import com.neighborparking.domain.enums.SlotStatus;
import com.neighborparking.domain.enums.VerificationStatus;
import com.neighborparking.repository.AvailabilitySlotRepository;
import com.neighborparking.repository.BookingRepository;
import com.neighborparking.repository.CheckRecordRepository;
import com.neighborparking.repository.ComplaintRepository;
import com.neighborparking.repository.ParkingSpaceRepository;
import com.neighborparking.repository.ReviewRepository;
import com.neighborparking.repository.VehicleRepository;
import com.neighborparking.security.CurrentUser;
import com.neighborparking.security.SecuritySupport;
import com.neighborparking.web.BookingController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final List<BookingStatus> OCCUPYING_STATUSES = Arrays.asList(
            BookingStatus.CONFIRMED, BookingStatus.IN_USE, BookingStatus.DISPUTED);

    private final BookingRepository bookingRepository;
    private final AvailabilitySlotRepository slotRepository;
    private final ParkingSpaceRepository spaceRepository;
    private final VehicleRepository vehicleRepository;
    private final CheckRecordRepository checkRecordRepository;
    private final ReviewRepository reviewRepository;
    private final ComplaintRepository complaintRepository;

    public BookingService(BookingRepository bookingRepository, AvailabilitySlotRepository slotRepository,
                          ParkingSpaceRepository spaceRepository, VehicleRepository vehicleRepository,
                          CheckRecordRepository checkRecordRepository, ReviewRepository reviewRepository,
                          ComplaintRepository complaintRepository) {
        this.bookingRepository = bookingRepository;
        this.slotRepository = slotRepository;
        this.spaceRepository = spaceRepository;
        this.vehicleRepository = vehicleRepository;
        this.checkRecordRepository = checkRecordRepository;
        this.reviewRepository = reviewRepository;
        this.complaintRepository = complaintRepository;
    }

    @Transactional
    public BookingController.BookingView create(BookingController.CreateBookingRequest request) {
        DomainSupport.requireValidTimeRange(request.getStartAt(), request.getEndAt());
        CurrentUser currentUser = SecuritySupport.currentUser();
        // 对共享时段加悲观锁，使“检查冲突”和“创建订单”处于同一串行化临界区。
        AvailabilitySlot slot = slotRepository.findLockedById(request.getSlotId())
                .orElseThrow(() -> DomainSupport.notFound("共享时段不存在"));
        if (slot.getStatus() != SlotStatus.PUBLISHED
                || request.getStartAt().isBefore(slot.getStartAt()) || request.getEndAt().isAfter(slot.getEndAt())) {
            throw new BusinessException(ErrorCode.SLOT_NOT_AVAILABLE, HttpStatus.CONFLICT, "共享时段不可用");
        }
        ParkingSpace space = spaceRepository.findById(slot.getSpaceId())
                .orElseThrow(() -> DomainSupport.notFound("车位不存在"));
        if (space.getStatus() != SpaceStatus.APPROVED) {
            throw new BusinessException(ErrorCode.SPACE_NOT_APPROVED, HttpStatus.CONFLICT, "车位尚未通过审核");
        }
        if (space.getOwnerId().equals(currentUser.getUserId())) {
            throw new BusinessException(ErrorCode.OWNER_CANNOT_BOOK_OWN_SPACE, HttpStatus.CONFLICT,
                    "不能预约自己的车位");
        }
        Vehicle vehicle = vehicleRepository.findByIdAndUserId(request.getVehicleId(), currentUser.getUserId())
                .filter(item -> Boolean.TRUE.equals(item.getActive())
                        && item.getVerificationStatus() == VerificationStatus.APPROVED)
                .orElseThrow(() -> new BusinessException(ErrorCode.VEHICLE_NOT_AVAILABLE, HttpStatus.CONFLICT,
                        "车辆不存在或已停用"));
        long overlaps = bookingRepository.countOverlaps(space.getId(), OCCUPYING_STATUSES,
                request.getStartAt(), request.getEndAt());
        if (overlaps > 0) {
            throw new BusinessException(ErrorCode.BOOKING_TIME_CONFLICT, HttpStatus.CONFLICT,
                    "该时段刚刚被预约，请选择其他时间");
        }
        Booking booking = new Booking();
        booking.setBookingNo(createBookingNo());
        booking.setSlotId(slot.getId());
        booking.setSpaceId(space.getId());
        booking.setOwnerId(space.getOwnerId());
        booking.setRenterId(currentUser.getUserId());
        booking.setVehicleId(vehicle.getId());
        booking.setStartAt(request.getStartAt());
        booking.setEndAt(request.getEndAt());
        return toView(bookingRepository.save(booking), space);
    }

    public List<BookingController.BookingView> listMine() {
        Long userId = SecuritySupport.currentUser().getUserId();
        return bookingRepository.findAllByRenterIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toViewForParticipant).collect(Collectors.toList());
    }

    public BookingController.BookingView detail(Long id) {
        Booking booking = participantBooking(id);
        return toViewForParticipant(booking);
    }

    @Transactional
    public BookingController.BookingView cancel(Long id, BookingController.CancelRequest request) {
        Booking booking = participantBooking(id);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw invalidTransition("只有待使用订单可以取消");
        }
        if (!Instant.now().isBefore(booking.getStartAt())) {
            throw invalidTransition("预约已经开始，不能取消");
        }
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelReason(request.getReason());
        return toViewForParticipant(bookingRepository.save(booking));
    }

    @Transactional
    public BookingController.BookingView checkIn(Long id) {
        Booking booking = renterBooking(id);
        if (booking.getStatus() != BookingStatus.CONFIRMED) {
            throw invalidTransition("当前订单不能签到");
        }
        Instant now = Instant.now();
        if (now.isBefore(booking.getStartAt().minusSeconds(30 * 60L)) || now.isAfter(booking.getEndAt())) {
            throw invalidTransition("仅可在开始前 30 分钟至结束时间内签到");
        }
        booking.setStatus(BookingStatus.IN_USE);
        saveCheckRecord(booking.getId(), CheckType.CHECK_IN);
        return toViewForParticipant(bookingRepository.save(booking));
    }

    @Transactional
    public BookingController.BookingView complete(Long id) {
        Booking booking = participantBooking(id);
        if (booking.getStatus() != BookingStatus.IN_USE) {
            throw invalidTransition("只有使用中的订单可以完成");
        }
        booking.setStatus(BookingStatus.COMPLETED);
        saveCheckRecord(booking.getId(), CheckType.CHECK_OUT);
        return toViewForParticipant(bookingRepository.save(booking));
    }

    @Transactional
    public Review review(Long id, BookingController.ReviewRequest request) {
        Booking booking = renterBooking(id);
        if (booking.getStatus() != BookingStatus.COMPLETED) {
            throw invalidTransition("订单完成后才能评价");
        }
        if (reviewRepository.existsByBookingId(id)) {
            throw new BusinessException(ErrorCode.DUPLICATE_REVIEW, HttpStatus.CONFLICT, "该订单已经评价");
        }
        Review review = new Review();
        review.setBookingId(id);
        review.setReviewerId(SecuritySupport.currentUser().getUserId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        return reviewRepository.save(review);
    }

    @Transactional
    public Complaint complain(Long id, BookingController.ComplaintRequest request) {
        Booking booking = participantBooking(id);
        if (!Arrays.asList(BookingStatus.IN_USE, BookingStatus.COMPLETED, BookingStatus.CANCELLED,
                BookingStatus.DISPUTED).contains(booking.getStatus())) {
            throw invalidTransition("订单开始使用、完成或取消后才能发起投诉");
        }
        Complaint complaint = new Complaint();
        complaint.setBookingId(booking.getId());
        complaint.setComplainantId(SecuritySupport.currentUser().getUserId());
        complaint.setContent(request.getContent());
        booking.setStatus(BookingStatus.DISPUTED);
        bookingRepository.save(booking);
        return complaintRepository.save(complaint);
    }

    private Booking participantBooking(Long id) {
        CurrentUser user = SecuritySupport.currentUser();
        Booking booking = bookingRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("订单不存在"));
        if (!booking.getRenterId().equals(user.getUserId()) && !booking.getOwnerId().equals(user.getUserId())) {
            throw DomainSupport.forbidden("只能查看或操作本人参与的订单");
        }
        return booking;
    }

    private Booking renterBooking(Long id) {
        Booking booking = participantBooking(id);
        if (!booking.getRenterId().equals(SecuritySupport.currentUser().getUserId())) {
            throw DomainSupport.forbidden("该操作仅限租用人");
        }
        return booking;
    }

    private void saveCheckRecord(Long bookingId, CheckType checkType) {
        CheckRecord record = new CheckRecord();
        record.setBookingId(bookingId);
        record.setCheckType(checkType);
        record.setOperatorId(SecuritySupport.currentUser().getUserId());
        record.setCheckedAt(Instant.now());
        checkRecordRepository.save(record);
    }

    private BookingController.BookingView toViewForParticipant(Booking booking) {
        ParkingSpace space = spaceRepository.findById(booking.getSpaceId())
                .orElseThrow(() -> DomainSupport.notFound("车位不存在"));
        return toView(booking, space);
    }

    private BookingController.BookingView toView(Booking booking, ParkingSpace space) {
        return new BookingController.BookingView(booking.getId(), booking.getBookingNo(), booking.getStatus(),
                booking.getSpaceId(), space.getTitle(), space.getSpaceCode(), space.getAccessInstructions(),
                booking.getVehicleId(), booking.getStartAt(), booking.getEndAt(), booking.getCancelReason(),
                booking.getCreatedAt());
    }

    private BusinessException invalidTransition(String message) {
        return new BusinessException(ErrorCode.INVALID_STATE_TRANSITION, HttpStatus.CONFLICT, message);
    }

    private String createBookingNo() {
        String date = DateTimeFormatter.ofPattern("yyyyMMdd").withZone(ZoneOffset.UTC).format(Instant.now());
        return "NP" + date + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
