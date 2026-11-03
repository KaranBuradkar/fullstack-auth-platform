
package com.authplatform.backend.controller;

import com.authplatform.backend.common.controller.BaseController;
import com.authplatform.backend.common.response.ApiResponse;
import com.authplatform.backend.common.response.ApiSuccessCode;
import com.authplatform.backend.dto.request.ChangePasswordRequest;
import com.authplatform.backend.dto.request.ForgotPasswordRequest;
import com.authplatform.backend.dto.request.ResetPasswordRequest;
import com.authplatform.backend.service.PasswordService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Password Management",
        description = """
                APIs for password recovery, password reset using email OTP,
                and changing the password of an authenticated user.
                """
)
@RestController
@RequestMapping("/api/v1/auth/password")
public class PasswordController extends BaseController {

    private final PasswordService passwordService;

    public PasswordController(
            PasswordService passwordService,
            HttpServletRequest request
    ) {
        super(request);
        this.passwordService = passwordService;
    }

    // ================= FORGOT PASSWORD =================

    @Operation(
            summary = "Request password reset OTP",
            description = """
                    Initiates the password recovery process by generating
                    and sending a password-reset OTP to the registered email.

                    - Checks whether the user exists.
                    - Deletes previously generated password-reset OTPs.
                    - Generates a new OTP.
                    - Stores the OTP with its configured expiration time.
                    - Sends the OTP to the registered email address.

                    The OTP is required to reset the user's password.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password reset OTP sent successfully",
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
                    responseCode = "404",
                    description = "User not found with the provided email",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "500",
                    description = "Unexpected server error or email delivery failure",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            )
    })
    @PostMapping("/forgot")
    public ResponseEntity<ApiResponse<Void>> forgot(
            @RequestBody @Valid ForgotPasswordRequest request
    ) {
        passwordService.forgotPassword(request);

        return buildResponse(
                ApiSuccessCode.PASSWORD_RESET_OTP_SENT,
                null,
                HttpStatus.OK
        );
    }

    // ================= RESET PASSWORD =================

    @Operation(
            summary = "Reset password using OTP",
            description = """
                    Resets the user's password using a valid password-reset OTP.

                    - Retrieves the password-reset OTP using the provided email.
                    - Checks whether the OTP has expired.
                    - Encodes the new password using the configured password encoder.
                    - Updates the user's password.
                    - Deletes the successfully used password-reset OTP.

                    This endpoint is intended for users who have forgotten
                    their existing password and do not require an access token.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password reset successfully",
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
                    responseCode = "422",
                    description = "Password reset OTP is invalid or expired",
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
    @PostMapping("/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request
    ) {
        passwordService.resetPassword(request);

        return buildResponse(
                ApiSuccessCode.PASSWORD_RESET_SUCCESS,
                null,
                HttpStatus.OK
        );
    }

    // ================= CHANGE PASSWORD =================

    @Operation(
            summary = "Change authenticated user's password",
            description = """
                    Changes the password of the currently authenticated user.

                    - Retrieves the authenticated user's email from Spring Security.
                    - Verifies the current password.
                    - Ensures the new password differs from the current password.
                    - Encodes and updates the new password.
                    - Revokes all existing refresh tokens belonging to the user.

                    Requires a valid access token in the Authorization header.
                    The user must authenticate again to obtain new tokens.
                    """
    )
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Password changed successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = """
                            Invalid request, incorrect current password,
                            or new password is identical to the current password.
                            """,
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "Missing or invalid authentication credentials",
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
    @PostMapping("/change")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request
    ) {
        passwordService.changePassword(request);

        return buildResponse(
                ApiSuccessCode.PASSWORD_CHANGED,
                null,
                HttpStatus.OK
        );
    }
}
