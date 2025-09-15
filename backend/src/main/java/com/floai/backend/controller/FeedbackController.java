package com.floai.backend.controller;

import com.floai.backend.model.FeedbackEvent;
import com.floai.backend.repository.FeedbackRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    private final FeedbackRepository feedbackRepository;

    public FeedbackController(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    // Minimal payload for recording user/product interaction.
    public record FeedbackRequest(Long productId, Long orderId, String action) {}

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody FeedbackRequest req) {
        // build using your ctor (lowercases action inside) and set timestamp
        FeedbackEvent e = new FeedbackEvent(req.productId(), req.orderId(), req.action());
        e.setCreatedAt(OffsetDateTime.now());

        FeedbackEvent saved = feedbackRepository.save(e);

        return ResponseEntity
                .created(URI.create("/feedback/" + saved.getId()))
                .build();
    }


    @GetMapping("/{id}")
    public ResponseEntity<FeedbackEvent> getById(@PathVariable Long id) {
        return feedbackRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<FeedbackEvent> list() {
        return feedbackRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
    }
}
