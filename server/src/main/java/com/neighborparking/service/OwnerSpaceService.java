package com.neighborparking.service;

import com.neighborparking.common.error.BusinessException;
import com.neighborparking.common.error.ErrorCode;
import com.neighborparking.domain.AvailabilitySlot;
import com.neighborparking.domain.Booking;
import com.neighborparking.domain.Community;
import com.neighborparking.domain.ParkingSpace;
import com.neighborparking.domain.enums.BookingStatus;
import com.neighborparking.domain.enums.SpaceStatus;
import com.neighborparking.domain.enums.SlotStatus;
import com.neighborparking.repository.AvailabilitySlotRepository;
import com.neighborparking.repository.BookingRepository;
import com.neighborparking.repository.CommunityRepository;
import com.neighborparking.repository.ParkingSpaceRepository;
import com.neighborparking.security.SecuritySupport;
import com.neighborparking.web.OwnerSpaceController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
public class OwnerSpaceService {

    private static final List<BookingStatus> OCCUPYING_STATUSES = Arrays.asList(
            BookingStatus.CONFIRMED, BookingStatus.IN_USE, BookingStatus.DISPUTED);

    private final ParkingSpaceRepository spaceRepository;
    private final AvailabilitySlotRepository slotRepository;
    private final CommunityRepository communityRepository;
    private final BookingRepository bookingRepository;

    public OwnerSpaceService(ParkingSpaceRepository spaceRepository, AvailabilitySlotRepository slotRepository,
                             CommunityRepository communityRepository, BookingRepository bookingRepository) {
        this.spaceRepository = spaceRepository;
        this.slotRepository = slotRepository;
        this.communityRepository = communityRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<ParkingSpace> listSpaces() {
        return spaceRepository.findAllByOwnerIdOrderByCreatedAtDesc(SecuritySupport.currentUser().getUserId());
    }

    @Transactional
    public ParkingSpace create(OwnerSpaceController.SpaceRequest request) {
        ParkingSpace space = new ParkingSpace();
        space.setOwnerId(SecuritySupport.currentUser().getUserId());
        apply(space, request);
        return spaceRepository.save(space);
    }

    @Transactional
    public ParkingSpace update(Long id, OwnerSpaceController.SpaceRequest request) {
        ParkingSpace space = ownedSpace(id);
        apply(space, request);
        // 关键信息变更后重新进入物业审核，避免审核通过后替换成其他车位。
        space.setStatus(SpaceStatus.PENDING_REVIEW);
        space.setReviewNote(null);
        return spaceRepository.save(space);
    }

    @Transactional
    public AvailabilitySlot publishSlot(Long spaceId, OwnerSpaceController.SlotRequest request) {
        DomainSupport.requireFutureTimeRange(request.getStartAt(), request.getEndAt());
        ParkingSpace space = ownedSpace(spaceId);
        if (space.getStatus() != SpaceStatus.APPROVED) {
            throw new BusinessException(ErrorCode.SPACE_NOT_APPROVED, HttpStatus.CONFLICT, "车位审核通过后才能发布时段");
        }
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setSpaceId(spaceId);
        slot.setStartAt(request.getStartAt());
        slot.setEndAt(request.getEndAt());
        return slotRepository.save(slot);
    }

    public List<AvailabilitySlot> listSlots(Long spaceId) {
        ownedSpace(spaceId);
        return slotRepository.findAllBySpaceIdOrderByStartAtDesc(spaceId);
    }

    @Transactional
    public void cancelSlot(Long spaceId, Long slotId) {
        ownedSpace(spaceId);
        AvailabilitySlot slot = slotRepository.findByIdAndSpaceId(slotId, spaceId)
                .orElseThrow(() -> DomainSupport.notFound("共享时段不存在"));
        if (bookingRepository.existsBySlotIdAndStatusIn(slotId, OCCUPYING_STATUSES)) {
            throw new BusinessException(ErrorCode.SLOT_NOT_AVAILABLE, HttpStatus.CONFLICT,
                    "该时段已有生效预约，不能撤销");
        }
        slot.setStatus(SlotStatus.CANCELLED);
        slotRepository.save(slot);
    }

    public List<Booking> listOwnerBookings() {
        return bookingRepository.findAllByOwnerIdOrderByCreatedAtDesc(SecuritySupport.currentUser().getUserId());
    }

    private ParkingSpace ownedSpace(Long id) {
        return spaceRepository.findByIdAndOwnerId(id, SecuritySupport.currentUser().getUserId())
                .orElseThrow(() -> DomainSupport.notFound("车位不存在"));
    }

    private void apply(ParkingSpace space, OwnerSpaceController.SpaceRequest request) {
        Community community = communityRepository.findById(request.getCommunityId())
                .orElseThrow(() -> DomainSupport.notFound("小区不存在"));
        if (!Boolean.TRUE.equals(community.getActive())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, HttpStatus.BAD_REQUEST, "小区当前未开放");
        }
        space.setCommunityId(community.getId());
        space.setSpaceCode(request.getSpaceCode().trim());
        space.setTitle(request.getTitle().trim());
        space.setAccessInstructions(request.getAccessInstructions().trim());
        space.setVehicleLimit(request.getVehicleLimit().trim());
        if (space.getId() == null) {
            space.setStatus(SpaceStatus.PENDING_REVIEW);
        }
    }
}
