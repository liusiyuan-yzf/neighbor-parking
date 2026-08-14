package com.neighborparking.service;

import com.neighborparking.common.error.BusinessException;
import com.neighborparking.common.error.ErrorCode;
import com.neighborparking.domain.AuditLog;
import com.neighborparking.domain.Booking;
import com.neighborparking.domain.Community;
import com.neighborparking.domain.Complaint;
import com.neighborparking.domain.ParkingSpace;
import com.neighborparking.domain.enums.ComplaintStatus;
import com.neighborparking.domain.enums.SpaceStatus;
import com.neighborparking.repository.AuditLogRepository;
import com.neighborparking.repository.BookingRepository;
import com.neighborparking.repository.CommunityRepository;
import com.neighborparking.repository.ComplaintRepository;
import com.neighborparking.repository.ParkingSpaceRepository;
import com.neighborparking.security.SecuritySupport;
import com.neighborparking.web.AdminController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final CommunityRepository communityRepository;
    private final ParkingSpaceRepository spaceRepository;
    private final BookingRepository bookingRepository;
    private final ComplaintRepository complaintRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuditService auditService;

    public AdminService(CommunityRepository communityRepository, ParkingSpaceRepository spaceRepository,
                        BookingRepository bookingRepository, ComplaintRepository complaintRepository,
                        AuditLogRepository auditLogRepository, AuditService auditService) {
        this.communityRepository = communityRepository;
        this.spaceRepository = spaceRepository;
        this.bookingRepository = bookingRepository;
        this.complaintRepository = complaintRepository;
        this.auditLogRepository = auditLogRepository;
        this.auditService = auditService;
    }

    public List<Community> listCommunities() {
        return communityRepository.findAll();
    }

    @Transactional
    public Community createCommunity(AdminController.CommunityRequest request) {
        Community community = new Community();
        apply(community, request);
        Community saved = communityRepository.save(community);
        auditService.record(operatorId(), "CREATE_COMMUNITY", "COMMUNITY", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional
    public Community updateCommunity(Long id, AdminController.CommunityRequest request) {
        Community community = communityRepository.findById(id)
                .orElseThrow(() -> DomainSupport.notFound("小区不存在"));
        apply(community, request);
        Community saved = communityRepository.save(community);
        auditService.record(operatorId(), "UPDATE_COMMUNITY", "COMMUNITY", saved.getId(), saved.getName());
        return saved;
    }

    public List<ParkingSpace> listPendingSpaces() {
        return spaceRepository.findAllByStatusOrderByCreatedAtAsc(SpaceStatus.PENDING_REVIEW);
    }

    @Transactional
    public ParkingSpace reviewSpace(Long id, boolean approved, String note) {
        ParkingSpace space = spaceRepository.findById(id).orElseThrow(() -> DomainSupport.notFound("车位不存在"));
        if (space.getStatus() != SpaceStatus.PENDING_REVIEW) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION, HttpStatus.CONFLICT,
                    "只有待审核车位可以执行审核");
        }
        space.setStatus(approved ? SpaceStatus.APPROVED : SpaceStatus.REJECTED);
        space.setReviewNote(note);
        ParkingSpace saved = spaceRepository.save(space);
        auditService.record(operatorId(), approved ? "APPROVE_SPACE" : "REJECT_SPACE", "PARKING_SPACE",
                saved.getId(), note);
        return saved;
    }

    public List<Booking> listBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Complaint> listComplaints() {
        return complaintRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional
    public Complaint resolveComplaint(Long id, AdminController.ComplaintResolveRequest request) {
        Complaint complaint = complaintRepository.findById(id)
                .orElseThrow(() -> DomainSupport.notFound("投诉不存在"));
        if (complaint.getStatus() == ComplaintStatus.RESOLVED || complaint.getStatus() == ComplaintStatus.REJECTED) {
            throw new BusinessException(ErrorCode.INVALID_STATE_TRANSITION, HttpStatus.CONFLICT, "投诉已经处理完成");
        }
        complaint.setStatus(request.isResolved() ? ComplaintStatus.RESOLVED : ComplaintStatus.REJECTED);
        complaint.setResolutionNote(request.getNote());
        Complaint saved = complaintRepository.save(complaint);
        auditService.record(operatorId(), request.isResolved() ? "RESOLVE_COMPLAINT" : "REJECT_COMPLAINT",
                "COMPLAINT", saved.getId(), request.getNote());
        return saved;
    }

    public List<AuditLog> listAudits() {
        return auditLogRepository.findTop100ByOrderByCreatedAtDesc();
    }

    private void apply(Community community, AdminController.CommunityRequest request) {
        community.setName(request.getName().trim());
        community.setAddress(request.getAddress().trim());
        community.setLatitude(request.getLatitude());
        community.setLongitude(request.getLongitude());
        community.setActive(request.isActive());
    }

    private Long operatorId() {
        return SecuritySupport.currentUser().getUserId();
    }
}
