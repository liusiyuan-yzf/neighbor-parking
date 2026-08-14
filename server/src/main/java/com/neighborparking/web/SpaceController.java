package com.neighborparking.web;

import com.neighborparking.service.SpaceSearchService;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/spaces")
public class SpaceController {

    private final SpaceSearchService service;

    public SpaceController(SpaceSearchService service) {
        this.service = service;
    }

    @GetMapping("/search")
    public List<SearchResult> search(@RequestParam Long communityId, @RequestParam Instant startAt,
                                     @RequestParam Instant endAt) {
        return service.search(communityId, startAt, endAt);
    }

    @GetMapping("/{id}")
    public SearchResult detail(@PathVariable Long id, @RequestParam Long slotId) {
        return service.detail(id, slotId);
    }

    @Data
    @AllArgsConstructor
    public static class SearchResult {
        private Long spaceId;
        private Long slotId;
        private String title;
        private Long communityId;
        private String communityName;
        private String communityAddress;
        private Double latitude;
        private Double longitude;
        private String maskedSpaceCode;
        private String vehicleLimit;
        private Instant startAt;
        private Instant endAt;
        private boolean freeOfCharge;
    }
}
