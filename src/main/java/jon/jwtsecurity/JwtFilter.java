package jon.jwtsecurity;

import jakarta.servlet.http.Cookie;
import jon.jwtsecurity.service.JwtUserDetailsService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@AllArgsConstructor
@Component
public class JwtFilter extends OncePerRequestFilter {
    private JwtUserDetailsService userDetailsService;
    private JwtTokenManager jwtTokenManager;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = null;

        // 1. Try to get the token from the Authorization header first
        String tokenHeader = request.getHeader("Authorization");
        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
            token = tokenHeader.substring(7);
        } else {
            // 2. If not found in header, extract it from the cookie
            token = extractTokenFromCookie(request);
        }

        System.out.println("JwtFilter doFilterInternal call: token=" + token);

        if (request.getMethod().equals("OPTIONS")) {
            filterChain.doFilter(request, response); // Skip JWT logic for OPTIONS
            return;
        }

        String username = null;
        if (token != null) {
            try {
                username = jwtTokenManager.getUsernameFromToken(token);
            } catch (Exception e) {
                System.out.println("Unable to get JWT Token");
            }
        } else {
            System.out.println("Token not found in header or cookie");
        }

        validateToken(request, username, token);
        filterChain.doFilter(request, response);
    }

    /**
     * Helper method to extract JWT from cookies.
     */
    private String extractTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("token".equals(cookie.getName())) {  // Ensure the cookie name matches what you set on login
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void validateToken(HttpServletRequest request, String username, String token) {
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            if (jwtTokenManager.validateJwtToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            }
        }
    }
}


//
//@AllArgsConstructor
//@Component
//public class JwtFilter extends OncePerRequestFilter {
//    private JwtUserDetailsService userDetailsService;
//    private JwtTokenManager jwtTokenManager;
//    @Override
//    protected void doFilterInternal(HttpServletRequest request,
//                                    HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        String tokenHeader = request.getHeader("Authorization");
//        System.out.println("JwtFilter doFilterInternal call 3 request header" + tokenHeader ); // + JwtController.printHeader(request)
//        if (request.getMethod().equals("OPTIONS")) {
//            filterChain.doFilter(request, response); // Skip JWT logic for OPTIONS
//            return;
//        }
//        String username = null;
//        String token = null;
//        if (tokenHeader != null && tokenHeader.startsWith("Bearer ")) {
//            token = tokenHeader.substring(7);
//            try {
//                username = jwtTokenManager.getUsernameFromToken(token);
//            } catch (Exception e) {
//                System.out.println("Unable to get JWT Token");
//            }
//        } else {
//            System.out.println("String does not start with Bearer or tokenheader == NULL");
//        }
//        validateToken(request, username, token);
//        filterChain.doFilter(request, response); //possible: response.setHeader( "key",value); its up to you.
//    }
//
//    private void validateToken(HttpServletRequest request, String username, String token) {
//        if (null != username && SecurityContextHolder.getContext().getAuthentication() == null) {
//            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
//            if (jwtTokenManager.validateJwtToken(token, userDetails)) {
//                UsernamePasswordAuthenticationToken
//                        authenticationToken = new UsernamePasswordAuthenticationToken(
//                        userDetails, null,
//                        userDetails.getAuthorities());
//                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
//            }
//        }
//    }
//}