package com.company.asset.security.jwt;

import com.company.asset.common.api.ApiResponse;
import com.company.asset.common.error.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtExceptionFilter extends OncePerRequestFilter {

    private final ObjectMapper om = new ObjectMapper();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        chain.doFilter(request, response);

        Object err = request.getAttribute("jwt_error");
        if (err != null && !response.isCommitted()) {
            response.setStatus(401);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(om.writeValueAsString(
                    ApiResponse.fail(ErrorCode.UNAUTHORIZED.getCode(), "JWT 인증 실패")
            ));
        }
    }
}
