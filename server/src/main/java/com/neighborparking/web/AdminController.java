package com.neighborparking.web;

import com.neighborparking.domain.AuditLog;
import com.neighborparking.domain.Booking;
import com.neighborparking.domain.Community;
import com.neighborparking.domain.Complaint;
import com.neighborparking.domain.ParkingSpace;
import com.neighborparking.service.AdminService;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.DecimalMax;
import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasAnyRole('PROPERTY_ADMIN','PLATFORM_ADMIN')")
public class AdminController {

    private final AdminService service;

    public AdminController(AdminService service) {
        this.service = service;
    }

    @GetMapping("/communities")
    public List<Community> listCommunities() {
        return service.listCommunities();
    }

    @PostMapping("/communities")
    @ResponseStatus(HttpStatus.CREATED)
    public Community createCommunity(@Valid @RequestBody CommunityRequest request) {
        return service.createCommunity(request);
    }

    @PutMapping("/communities/{id}")
    public Community updateCommunity(@PathVariable Long id, @Valid @RequestBody CommunityRequest request) {
        return service.updateCommunity(id, request);
    }

    @GetMapping("/spaces/pending")
    public List<ParkingSpace> listPendingSpaces() {
        return service.listPendingSpaces();
    }

    @PostMapping("/spaces/{id}/approve")
    public ParkingSpace approve(@PathVariable Long id, @Valid @RequestBody ReviewSpaceRequest request) {
        return service.reviewSpace(id, true, request.getNote());
    }

    @PostMapping("/spaces/{id}/reject")
    public ParkingSpace reject(@PathVariable Long id, @Valid @RequestBody ReviewSpaceRequest request) {
        return service.reviewSpace(id, false, request.getNote());
    }

    @GetMapping("/bookings")
    public List<Booking> listBookings() {
        return service.listBookings();
    }

    @GetMapping("/complaints")
    public List<Complaint> listComplaints() {
        return service.listComplaints();
    }

    @PostMapping("/complaints/{id}/resolve")
    public Complaint resolveComplaint(@PathVariable Long id,
                                      @Valid @RequestBody ComplaintResolveRequest request) {
        return service.resolveComplaint(id, request);
    }

    @GetMapping("/audits")
    public List<AuditLog> listAudits() {
        return service.listAudits();
    }

    @Data
    @NoArgsConstructor
    public static class CommunityRequest {
        @NotBlank(message = "小区名称不能为空")
        @Size(max = 128, message = "小区名称最多 128 个字符")
        private String name;
        @NotBlank(message = "小区地址不能为空")
        @Size(max = 255, message = "小区地址最多 255 个字符")
        private String address;
        @NotNull(message = "纬度不能为空")
        @DecimalMin(value = "-90.0", message = "纬度不能小于 -90")
        @DecimalMax(value = "90.0", message = "纬度不能大于 90")
        private Double latitude;
        @NotNull(message = "经度不能为空")
        @DecimalMin(value = "-180.0", message = "经度不能小于 -180")
        @DecimalMax(value = "180.0", message = "经度不能大于 180")
        private Double longitude;
        private boolean active = true;
    }

    @Data
    @NoArgsConstructor
    public static class ReviewSpaceRequest {
        @NotBlank(message = "审核说明不能为空")
        @Size(max = 500, message = "审核说明最多 500 个字符")
        private String note;
    }

    @Data
    @NoArgsConstructor
    public static class ComplaintResolveRequest {
        private boolean resolved;
        @NotBlank(message = "处理说明不能为空")
        @Size(max = 1000, message = "处理说明最多 1000 个字符")
        private String note;
    }
}
