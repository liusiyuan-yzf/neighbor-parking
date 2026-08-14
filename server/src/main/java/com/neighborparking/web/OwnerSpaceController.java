package com.neighborparking.web;

import com.neighborparking.domain.AvailabilitySlot;
import com.neighborparking.domain.Booking;
import com.neighborparking.domain.ParkingSpace;
import com.neighborparking.service.OwnerSpaceService;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/owner")
@PreAuthorize("hasRole('OWNER')")
public class OwnerSpaceController {

    private final OwnerSpaceService service;

    public OwnerSpaceController(OwnerSpaceService service) {
        this.service = service;
    }

    @GetMapping("/spaces")
    public List<ParkingSpace> listSpaces() {
        return service.listSpaces();
    }

    @PostMapping("/spaces")
    @ResponseStatus(HttpStatus.CREATED)
    public ParkingSpace create(@Valid @RequestBody SpaceRequest request) {
        return service.create(request);
    }

    @PutMapping("/spaces/{id}")
    public ParkingSpace update(@PathVariable Long id, @Valid @RequestBody SpaceRequest request) {
        return service.update(id, request);
    }

    @GetMapping("/spaces/{spaceId}/slots")
    public List<AvailabilitySlot> listSlots(@PathVariable Long spaceId) {
        return service.listSlots(spaceId);
    }

    @PostMapping("/spaces/{spaceId}/slots")
    @ResponseStatus(HttpStatus.CREATED)
    public AvailabilitySlot publishSlot(@PathVariable Long spaceId, @Valid @RequestBody SlotRequest request) {
        return service.publishSlot(spaceId, request);
    }

    @DeleteMapping("/spaces/{spaceId}/slots/{slotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelSlot(@PathVariable Long spaceId, @PathVariable Long slotId) {
        service.cancelSlot(spaceId, slotId);
    }

    @GetMapping("/bookings")
    public List<Booking> listOwnerBookings() {
        return service.listOwnerBookings();
    }

    @Data
    @NoArgsConstructor
    public static class SpaceRequest {
        @NotNull(message = "小区不能为空")
        private Long communityId;
        @NotBlank(message = "车位编号不能为空")
        @Size(max = 64, message = "车位编号最多 64 个字符")
        private String spaceCode;
        @NotBlank(message = "车位标题不能为空")
        @Size(max = 128, message = "车位标题最多 128 个字符")
        private String title;
        @NotBlank(message = "入场说明不能为空")
        @Size(max = 1000, message = "入场说明最多 1000 个字符")
        private String accessInstructions;
        @NotBlank(message = "车辆限制不能为空")
        @Size(max = 64, message = "车辆限制最多 64 个字符")
        private String vehicleLimit;
    }

    @Data
    @NoArgsConstructor
    public static class SlotRequest {
        @NotNull(message = "开始时间不能为空")
        private Instant startAt;
        @NotNull(message = "结束时间不能为空")
        private Instant endAt;
    }
}
