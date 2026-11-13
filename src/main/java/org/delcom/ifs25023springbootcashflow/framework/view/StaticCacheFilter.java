package org.delcom.ifs25023springbootcashflow.framework.view;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;

/** Memberi masa simpan cache yang panjang pada berkas statis agar kunjungan berikutnya lebih cepat. */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class StaticCacheFilter implements Filter {

    private static final String ONE_YEAR = "public, max-age=31536000, immutable";
    private static final String ONE_WEEK = "public, max-age=604800";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (request instanceof HttpServletRequest http && response instanceof HttpServletResponse out) {
            String path = http.getRequestURI();
            if (path.startsWith("/VAADIN/build/")) {
                out.setHeader("Cache-Control", ONE_YEAR);
            } else if (path.startsWith("/icons/") || path.equals("/styles.css")) {
                out.setHeader("Cache-Control", ONE_WEEK);
            }
        }
        chain.doFilter(request, response);
    }
}