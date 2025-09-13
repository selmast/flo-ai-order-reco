package com.floai.backend.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class FeedbackServiceTest {

    @Test
    void recordsAndReturnsCounts() {
        FeedbackService s = new FeedbackService();
        s.record(3L, "added_to_cart");
        s.record(3L, "purchased");
        s.record(3L, "ignored");

        var agg = s.get(3L);
        assertEquals(1, agg.addedToCart);
        assertEquals(1, agg.purchased);
        assertEquals(1, agg.ignored);
    }
}
