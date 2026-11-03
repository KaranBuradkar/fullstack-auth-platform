package com.authplatform.backend.controller;

import com.authplatform.backend.common.controller.BaseController;
import com.authplatform.backend.common.response.ApiResponse;
import com.authplatform.backend.common.response.ApiSuccessCode;
import com.authplatform.backend.dto.request.LoginRequest;
import com.authplatform.backend.dto.request.RefreshTokenRequest;
import com.authplatform.backend.dto.request.RegisterRequest;
import com.authplatform.backend.dto.response.AuthResponse;
import com.authplatform.backend.dto.response.UserTokenResponse;
import com.authplatform.backend.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Authentication",
        description = """
                Authentication and account access management APIs.
                Supports user registration, login, JWT refresh token rotation,
                and logout.
                """
)
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController extends BaseController {

    private final AuthService authService;

    public AuthController(
            AuthService authService,
            HttpServletRequest request
    ) {
        super(request);
        this.authService = authService;
    }

    // ================= REGISTER =================

    @Operation(
            summary = "Register a new user",
            description = """
                    Creates a new user account using the provided registration details.

                    - Validates the incoming request.
                    - Checks whether the email is already registered.
                    - Encrypts the password using the configured password encoder.
                    - Generates access and refresh tokens.
                    - Persists the user's refresh token.

                    Returns user details and authentication tokens.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or validation failure",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Email address is already registered",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @RequestBody @Valid RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);

        return buildResponse(
                ApiSuccessCode.USER_REGISTERED,
                response,
                HttpStatus.CREATED
        );
    }

    // ================= LOGIN =================

    @Operation(
            summary = "Authenticate user",
            description = """
                    Authenticates an existing user using email and password.

                    - Validates user credentials through Spring Security AuthenticationManager.
                    - Generates a new access token and refresh token.
                    - Rotates and persists the user's refresh token.
                    - Returns user details and authentication tokens.

                    Invalid credentials result in an authentication failure.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Login successful",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or validation failure",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Invalid email or password",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @RequestBody @Valid LoginRequest request
    ) {
        AuthResponse response = authService.login(request);

        return buildResponse(
                ApiSuccessCode.LOGIN_SUCCESS,
                response,
                HttpStatus.OK
        );
    }

    // ================= REFRESH TOKEN =================

    @Operation(
            summary = "Refresh authentication tokens",
            description = """
                    Generates a new access token and refresh token using
                    a valid refresh token.

                    - Verifies that the refresh token exists.
                    - Rejects revoked refresh tokens.
                    - Rejects expired refresh tokens.
                    - Rotates the refresh token and updates its expiry date.

                    This endpoint does not require an access token.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Authentication tokens refreshed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid refresh token request",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Refresh token is invalid, expired, or revoked",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<UserTokenResponse>> refreshToken(
            @RequestBody @Valid RefreshTokenRequest request
    ) {
        UserTokenResponse response =
                authService.refreshToken(request.refreshToken());

        return buildResponse(
                ApiSuccessCode.TOKEN_REFRESHED,
                response,
                HttpStatus.OK
        );
    }

    // ================= LOGOUT =================

    @Operation(
            summary = "Logout authenticated user",
            description = """
                    Logs out the currently authenticated user.

                    - Extracts the authenticated user's email from the security context.
                    - Revokes the user's refresh token.
                    - Sets the refresh token expiry to the current time.

                    Requires a valid access token in the Authorization header.
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User logged out successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Missing, invalid, or expired access token",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @Parameter(hidden = true)
            Authentication authentication
    ) {
        authService.logout(authentication.getName());

        return buildResponse(
                ApiSuccessCode.LOGOUT_SUCCESS,
                null,
                HttpStatus.OK
        );
    }
}