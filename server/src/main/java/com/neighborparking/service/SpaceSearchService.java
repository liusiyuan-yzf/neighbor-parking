package com.neighborparking.service;

import com.neighborparking.domain.AvailabilitySlot;
import com.neighborparking.domain.Community;
import com.neighborparking.domain.ParkingSpace;
import com.neighborparking.domain.enums.BookingStatus;
import com.neighborparking.domain.enums.SpaceStatus;
import com.neighborparking.domain.enums.SlotStatus;
import com.neighborparking.repository.AvailabilitySlotRepository;
import com.neighborparking.repository.BookingRepository;
import com.neighborparking.repository.CommunityRepository;
import com.neighborparking.repository.ParkingSpaceRepository;
import com.neighborparking.web.SpaceController;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SpaceSearchService {

    private static final List<BookingStatus> OCCUPYING_STATUSES = Arrays.asList(
            BookingStatus.CONFIRMED, BookingStatus.IN_USE, BookingStatus.DISPUTED);

    private final ParkingSpaceRepository spaceRepository;
    private final AvailabilitySlotRepository slotRepository;
    private final BookingRepository bookingRepository;
    private final CommunityRepository communityRepository;

    public SpaceSearchService(ParkingSpaceRepository spaceRepository, AvailabilitySlotRepository slotRepository,
                              BookingRepository bookingRepository, CommunityRepository communityRepository) {
        this.spaceRepository = spaceRepository;
        this.slotRepository = slotRepository;
        this.bookingRepository = bookingRepository;
        this.communityRepository = communityRepository;
    }

    public List<SpaceController.SearchResult> search(Long communityId, Instant startAt, Instant endAt) {
        DomainSupport.requireFutureTimeRange(startAt, endAt);
        List<ParkingSpace> spaces = spaceRepository.findAllByCommunityIdAndStatus(communityId, SpaceStatus.APPROVED);
        if (spaces.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> spaceIds = spaces.stream().map(ParkingSpace::getId).collect(Collectors.toList());
        Map<Long, ParkingSpace> spaceById = spaces.stream()
                .collect(Collectors.toMap(ParkingSpace::getId, Function.identity()));
        Community community = communityRepository.findById(communityId)
                .orElseThrow(() -> DomainSupport.notFound("小区不存在"));
        List<AvailabilitySlot> slots = slotRepository
                .findAllBySpaceIdInAndStatusAndStartAtLessThanEqualAndEndAtGreaterThanEqual(
                        spaceIds, SlotStatus.PUBLISHED, startAt, endAt);
        return slots.stream()
                .filter(slot -> bookingRepository.countOverlaps(slot.getSpaceId(), OCCUPYING_STATUSES,
                        startAt, endAt) == 0)
                .map(slot -> toResult(spaceById.get(slot.getSpaceId()), slot, community))
                .collect(Collectors.toList());
    }

    public SpaceController.SearchResult detail(Long spaceId, Long slotId) {
        ParkingSpace space = spaceRepository.findById(spaceId)
                .filter(item -> item.getStatus() == SpaceStatus.APPROVED)
                .orElseThrow(() -> DomainSupport.notFound("车位不存在或未开放"));
        AvailabilitySlot slot = slotRepository.findByIdAndSpaceId(slotId, spaceId)
                .filter(item -> item.getStatus() == SlotStatus.PUBLISHED)
                .orElseThrow(() -> DomainSupport.notFound("共享时段不存在"));
        Community community = communityRepository.findById(space.getCommunityId())
                .orElseThrow(() -> DomainSupport.notFound("小区不存在"));
        return toResult(space, slot, community);
    }

    private SpaceController.SearchResult toResult(ParkingSpace space, AvailabilitySlot slot, Community community) {
        return new SpaceController.SearchResult(space.getId(), slot.getId(), space.getTitle(), community.getId(),
                community.getName(), community.getAddress(), community.getLatitude(), community.getLongitude(),
                maskCode(space.getSpaceCode()), space.getVehicleLimit(), slot.getStartAt(), slot.getEndAt(), true);
    }

    private String maskCode(String code) {
        if (code == null || code.length() < 2) {
            return "**";
        }
        return code.substring(0, 1) + "***" + code.substring(code.length() - 1);
    }
}
