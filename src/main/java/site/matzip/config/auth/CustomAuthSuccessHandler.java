package site.matzip.config.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import site.matzip.config.jwt.JwtUtil;

import java.io.IOException;

@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {
    private final JwtUtil jwtUtil;

    public CustomAuthSuccessHandler(JwtUtil jwtUtil) { this.jwtUtil = jwtUtil; }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth) throws IOException {
        String username = auth.getName();
        String token = jwtUtil.generateToken(username);

        String accept = req.getHeader("Accept");
        String xhr = req.getHeader("X-Requested-With");

        boolean wantsJson = (accept != null && accept.contains("application/json"))
                || "XMLHttpRequest".equalsIgnoreCase(xhr);

        if (wantsJson) {
            // AJAX/Fetch 요청: JSON으로 토큰 반환
            res.setContentType("application/json");
            res.setCharacterEncoding("UTF-8");
            res.getWriter().write("{\"accessToken\":\"" + token + "\"}");
        } else {
            // 브라우저 폼 로그인: 페이지로 리다이렉트
            res.sendRedirect("/main");
        }
    }
}
