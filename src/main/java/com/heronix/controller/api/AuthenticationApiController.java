package com.heronix.controller.api;

import com.heronix.api.dto.LoginRequestDTO;
import com.heronix.api.dto.LoginResponseDTO;
import com.heronix.api.dto.TokenRefreshRequestDTO;
import com.heronix.service.ApiAuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication API Controller
 *
 * REST endpoints for user authentication, token management, and logout.
 * Provides JWT-based authentication for API access.
 *
 * @author Heronix Development Team
 * @version 1.0
 * @since Phase 43 - API Key Management & Authentication Endpoints
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Authentication", description = "User authentication and token management")
public class AuthenticationApiController {

    private final ApiAuthenticationService authenticationService;

    @Operation(summary = "User login", description = "Authenticate user and generate JWT tokens")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials"),
        @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(
            @Valid @RequestBody LoginRequestDTO request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        ApiAuthenticationService.AuthenticationResult result =
            authenticationService.login(request.getUsername(), request.getPassword(),
                ipAddress, userAgent);

        if (result.success()) {
            LoginResponseDTO response = LoginResponseDTO.builder()
                .accessToken(result.accessToken())
                .refreshToken(result.refreshToken())
                .userId(result.userId())
                .roles(result.roles())
                .build();

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("data", response);

            return ResponseEntity.ok(responseMap);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", result.errorMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @Operation(summary = "Refresh access token", description = "Get new access token using refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
        @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(
            @Valid @RequestBody TokenRefreshRequestDTO request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);

        ApiAuthenticationService.AuthenticationResult result =
            authenticationService.refreshAccessToken(request.getRefreshToken(), ipAddress);

        if (result.success()) {
            LoginResponseDTO response = LoginResponseDTO.builder()
                .accessToken(result.accessToken())
                .refreshToken(result.refreshToken())
                .userId(result.userId())
                .roles(result.roles())
                .build();

            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("data", response);

            return ResponseEntity.ok(responseMap);
        } else {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", result.errorMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }
    }

    @Operation(summary = "Logout", description = "Revoke access and refresh tokens")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletRequest httpRequest,
            Authentication authentication) {

        String accessToken = extractToken(authHeader);
        String refreshToken = httpRequest.getParameter("refreshToken");
        String userId = authentication != null ? authentication.getName() : "unknown";
        String ipAddress = getClientIp(httpRequest);

        authenticationService.logout(accessToken, refreshToken, userId, ipAddress);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Logged out successfully");

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get current user info", description = "Get authenticated user information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User info retrieved"),
        @ApiResponse(responseCode = "401", description = "Not authenticated")
    })
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Not authenticated");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("userId", authentication.getName());
        userInfo.put("authorities", authentication.getAuthorities());
        userInfo.put("authenticated", authentication.isAuthenticated());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("data", userInfo);

        return ResponseEntity.ok(response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String extractToken(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}
