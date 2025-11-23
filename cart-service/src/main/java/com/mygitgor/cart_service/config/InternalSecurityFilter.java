package com.mygitgor.cart_service.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class InternalSecurityFilter implements Filter {

    @Value("${internal.auth.token}")
    private String expectedToken;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;

        String incomingToken = req.getHeader("X-Internal-Service-Auth");

        if (expectedToken.equals(incomingToken)) {
            chain.doFilter(request, response);
        } else {
            ((HttpServletResponse) response).sendError(403, "Access Denied");
        }
    }
}
