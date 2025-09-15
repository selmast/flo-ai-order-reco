package com.floai.backend.controller;

import com.floai.backend.dto.FeedbackCreateRequest;
import com.floai.backend.dto.FeedbackDto;
import com.floai.backend.mapper.FeedbackMapper;
import com.floai.backend.model.FeedbackEvent;
import com.floai.backend.repository.FeedbackRepository;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping(value = "/feedback", produces = MediaType.APPLICATION_JSON_VALUE)
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;
    private final FeedbackMapper feedbackMapper;

    public FeedbackController(FeedbackRepository feedbackRepository, FeedbackMapper feedbackMapper) {
        this.feedbackRepository = feedbackRepository;
        this.feedbackMapper = feedbackMapper;
    }

    // POST /feedback  -> create a feedback event
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> create(@Valid @RequestBody FeedbackCreateRequest req,
                                       UriComponentsBuilder uriBuilder) {
        FeedbackEvent saved = feedbackRepository.save(feedbackMapper.fromCreateRequest(req));
        return ResponseEntity
                .created(uriBuilder.path("/feedback/{id}").buildAndExpand(saved.getId()).toUri())
                .build();
    }

    // GET /feedback/{id} -> return DTO
    @GetMapping("/{id}")
    public ResponseEntity<FeedbackDto> getOne(@PathVariable Long id) {
        return feedbackRepository.findById(id)
                .map(feedbackMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    // (Optional) GET /feedback?productId=1 -> list DTOs
    @GetMapping
    public ResponseEntity<List<FeedbackDto>> listByProduct(@RequestParam(required = false) Long productId) {
        List<FeedbackEvent> events = (productId == null)
                ? feedbackRepository.findAll()
                : feedbackRepository.findByProductId(productId);
        return ResponseEntity.ok(events.stream().map(feedbackMapper::toDto).toList());
    }
}
