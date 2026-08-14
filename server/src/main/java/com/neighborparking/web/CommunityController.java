package com.neighborparking.web;

import com.neighborparking.domain.Community;
import com.neighborparking.repository.CommunityRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/communities")
public class CommunityController {

    private final CommunityRepository repository;

    public CommunityController(CommunityRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Community> list() {
        return repository.findAllByActiveTrueOrderByNameAsc();
    }
}
