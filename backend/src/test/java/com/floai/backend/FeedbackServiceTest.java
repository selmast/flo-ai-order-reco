package com.floai.backend;

import com.floai.backend.model.FeedbackEvent;
import com.floai.backend.repository.FeedbackRepository;
import com.floai.backend.service.FeedbackService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for FeedbackService using a mocked FeedbackRepository.
 */
@ExtendWith(MockitoExtension.class)
public class FeedbackServiceTest {

    @Mock
    private FeedbackRepository repo;

    @Test
    void record_normalizes_and_persists_event() {
        FeedbackService service = new FeedbackService(repo);

        service.record(42L, "Added To Cart");

        ArgumentCaptor<FeedbackEvent> captor = ArgumentCaptor.forClass(FeedbackEvent.class);
        verify(repo, times(1)).save(captor.capture());

        FeedbackEvent ev = captor.getValue();
        assertThat(ev.getProductId()).isEqualTo(42L);
        assertThat(ev.getAction()).isEqualTo("added_to_cart"); // normalized
    }

    @Test
    void getScore_aggregates_from_counts() {
        when(repo.countByProductAndAction(10L, "viewed")).thenReturn(3L);
        when(repo.countByProductAndAction(10L, "ignored")).thenReturn(1L);
        when(repo.countByProductAndAction(10L, "added_to_cart")).thenReturn(2L);
        when(repo.countByProductAndAction(10L, "purchased")).thenReturn(1L);

        FeedbackService service = new FeedbackService(repo);

        double score = service.getScore(10L);

        // score = 3*0.1 + 1*(-0.2) + 2*0.5 + 1*1.0 = 2.1
        assertThat(score).isEqualTo(2.1);
    }

    @Test
    void getAllScores_uses_group_aggregate() {
        // Build a tiny implementation of the projection interface
        FeedbackRepository.FeedbackAggregate agg = new FeedbackRepository.FeedbackAggregate() {
            public Long getProductId()    { return 7L; }
            public Long getViewed()       { return 5L; }
            public Long getIgnored()      { return 2L; }
            public Long getAddedToCart()  { return 1L; }
            public Long getPurchased()    { return 3L; }
        };
        when(repo.aggregateCounts()).thenReturn(List.of(agg));

        FeedbackService service = new FeedbackService(repo);
        var map = service.getAllScores();

        // expected = 5*0.1 + 2*(-0.2) + 1*0.5 + 3*1.0 = 0.5 - 0.4 + 0.5 + 3.0 = 3.6
        assertThat(map).containsEntry(7L, 3.6);
    }
}
