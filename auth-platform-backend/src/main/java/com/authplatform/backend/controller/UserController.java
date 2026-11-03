package com.authplatform.backend.controller;

import com.authplatform.backend.common.controller.BaseController;
import com.authplatform.backend.common.response.ApiResponse;
import com.authplatform.backend.common.response.ApiSuccessCode;
import com.authplatform.backend.dto.response.UserProfileResponse;
import com.authplatform.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "User Management",
        description = """
                APIs for retrieving and managing authenticated user information.
                """
)
@RestController
@RequestMapping("/api/v1/users")
public class UserController extends BaseController {

    private final UserService userService;

    public UserController(
            HttpServletRequest request,
            UserService userService
    ) {
        super(request);
        this.userService = userService;
    }

    // ================= FETCH CURRENT USER PROFILE =================

    @Operation(
            summary = "Get authenticated user's profile",
            description = """
                    Retrieves the profile information of the currently
                    authenticated user.

                    - Requires a valid JWT access token.

                    The user does not need to provide their email or user ID.
                    The profile is determined from the authenticated session.
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "User profile retrieved successfully",
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
                    responseCode = "404",
                    description = "Authenticated user not found",
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
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> fetchProfile() {

        UserProfileResponse userProfile = userService.getUserProfile();

        return buildResponse(
                ApiSuccessCode.USER_RETRIEVED,
                userProfile,
                HttpStatus.OK
        );
    }
}
