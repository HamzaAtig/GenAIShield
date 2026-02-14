package org.hat.genaishield.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitingFilterTest {

    @Test
    void blocks_after_capacity_exceeded() throws ServletException, IOException {
        RateLimitProperties props = new RateLimitProperties();
        props.setCapacityPerMinute(1);
        RateLimitingFilter filter = new RateLimitingFilter(props);

        MockHttpServletRequest req = new MockHttpServletRequest("POST", "/api/v1/chat");
        req.addHeader("X-Tenant-Id", "t1");
        req.addHeader("X-User-Id", "u1");
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        MockHttpServletResponse res2 = new MockHttpServletResponse();

        FilterChain chain = (request, response) -> ((HttpServletResponse) response).setStatus(200);

        filter.doFilter(req, res1, chain);
        assertEquals(200, res1.getStatus());

        filter.doFilter(req, res2, chain);
        assertEquals(429, res2.getStatus());
    }
}
