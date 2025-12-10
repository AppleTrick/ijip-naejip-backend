package com.ssafy.home.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtExceptionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Expired JWT token", "만료된 토큰입니다.");
        } catch (MalformedJwtException | io.jsonwebtoken.security.SignatureException e) {
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token", "유효하지 않은 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Unsupported JWT token", "지원되지 않는 토큰입니다.");
        } catch (IllegalArgumentException e) {
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT claims string is empty", "토큰이 비어있거나 잘못되었습니다.");
        } catch (JwtException e) {
            setErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "JWT Error", "토큰 처리 중 오류가 발생했습니다.");
        }
    }

    private void setErrorResponse(HttpServletResponse response, int status, String error, String message) throws IOException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> body = new HashMap<>();
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);

        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(response.getOutputStream(), body);
    }
}
