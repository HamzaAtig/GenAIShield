package org.hat.genaishield.api.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationIdFilterTest {

    @Test
    void uses_existing_header_and_exposes_in_response() throws ServletException, IOException {
        CorrelationIdFilter filter = new CorrelationIdFilter();
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/chat");
        req.addHeader(CorrelationIdFilter.HEADER, "corr-123");
        MockHttpServletResponse res = new MockHttpServletResponse();
        AtomicReference<String> seen = new AtomicReference<>();

        FilterChain chain = (request, response) -> seen.set(MDC.get(CorrelationIdFilter.MDC_KEY));
        filter.doFilter(req, res, chain);

        assertEquals("corr-123", seen.get());
        assertEquals("corr-123", res.getHeader(CorrelationIdFilter.HEADER));
        assertNull(MDC.get(CorrelationIdFilter.MDC_KEY));
    }

    @Test
    void generates_when_missing() throws ServletException, IOException {
        CorrelationIdFilter filter = new CorrelationIdFilter();
        MockHttpServletRequest req = new MockHttpServletRequest("GET", "/api/v1/chat");
        MockHttpServletResponse res = new MockHttpServletResponse();
        AtomicReference<String> seen = new AtomicReference<>();

        FilterChain chain = (request, response) -> seen.set(MDC.get(CorrelationIdFilter.MDC_KEY));
        filter.doFilter(req, res, chain);

        assertNotNull(seen.get());
        assertFalse(seen.get().isBlank());
        assertEquals(seen.get(), res.getHeader(CorrelationIdFilter.HEADER));
        assertNull(MDC.get(CorrelationIdFilter.MDC_KEY));
    }
}
