package com.neighborparking.web;

import com.neighborparking.domain.Complaint;
import com.neighborparking.domain.Review;
import com.neighborparking.domain.enums.BookingStatus;
import com.neighborparking.service.BookingService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
public class BookingController {

    private final BookingService service;

    public BookingController(BookingService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingView create(@Valid @RequestBody CreateBookingRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<BookingView> list() {
        return service.listMine();
    }

    @GetMapping("/{id}")
    public BookingView detail(@PathVariable Long id) {
        return service.detail(id);
    }

    @PostMapping("/{id}/cancel")
    public BookingView cancel(@PathVariable Long id, @Valid @RequestBody CancelRequest request) {
        return service.cancel(id, request);
    }

    @PostMapping("/{id}/check-in")
    public BookingView checkIn(@PathVariable Long id) {
        return service.checkIn(id);
    }

    @PostMapping("/{id}/complete")
    public BookingView complete(@PathVariable Long id) {
        return service.complete(id);
    }

    @PostMapping("/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public Review review(@PathVariable Long id, @Valid @RequestBody ReviewRequest request) {
        return service.review(id, request);
    }

    @PostMapping("/{id}/complaints")
    @ResponseStatus(HttpStatus.CREATED)
    public Complaint complain(@PathVariable Long id, @Valid @RequestBody ComplaintRequest request) {
        return service.complain(id, request);
    }

    @Data
    @NoArgsConstructor
    public static class CreateBookingRequest {
        @NotNull(message = "共享时段不能为空")
        private Long slotId;
        @NotNull(message = "车辆不能为空")
        private Long vehicleId;
        @NotNull(message = "开始时间不能为空")
        private Instant startAt;
        @NotNull(message = "结束时间不能为空")
        private Instant endAt;
    }

    @Data
    @NoArgsConstructor
    public static class CancelRequest {
        @NotBlank(message = "取消原因不能为空")
        @Size(max = 500, message = "取消原因最多 500 个字符")
        private String reason;
    }

    @Data
    @NoArgsConstructor
    public static class ReviewRequest {
        @NotNull(message = "评分不能为空")
        @Min(value = 1, message = "评分最低为 1")
        @Max(value = 5, message = "评分最高为 5")
        private Integer rating;
        @Size(max = 500, message = "评价最多 500 个字符")
        private String content;
    }

    @Data
    @NoArgsConstructor
    public static class ComplaintRequest {
        @NotBlank(message = "投诉内容不能为空")
        @Size(max = 1000, message = "投诉内容最多 1000 个字符")
        private String content;
    }

    @Data
    @AllArgsConstructor
    public static class BookingView {
        private Long id;
        private String bookingNo;
        private BookingStatus status;
        private Long spaceId;
        private String spaceTitle;
        private String spaceCode;
        private String accessInstructions;
        private Long vehicleId;
        private Instant startAt;
        private Instant endAt;
        private String cancelReason;
        private Instant createdAt;
    }
}
