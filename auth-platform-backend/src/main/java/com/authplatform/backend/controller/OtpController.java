package com.authplatform.backend.controller;

import com.authplatform.backend.common.controller.BaseController;
import com.authplatform.backend.common.response.ApiResponse;
import com.authplatform.backend.common.response.ApiSuccessCode;
import com.authplatform.backend.dto.request.EmailVerificationOtpRequest;
import com.authplatform.backend.dto.request.VerifyEmailRequest;
import com.authplatform.backend.service.OtpService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Email Verification",
        description = """
                APIs for email verification using one-time passwords (OTP).
                Supports generating and sending verification OTPs and
                verifying user email addresses.
                """
)
@RestController
@RequestMapping("/api/v1/auth/otp")
public class OtpController extends BaseController {

    private final OtpService otpService;

    public OtpController(
            OtpService otpService,
            HttpServletRequest request
    ) {
        super(request);
        this.otpService = otpService;
    }

    // ================= SEND EMAIL VERIFICATION OTP =================

    @Operation(
            summary = "Send email verification OTP",
            description = """
                    Generates and sends an email verification OTP to the
                    registered user's email address.

                    - Checks whether the user exists.
                    - Deletes any previously stored verification OTP.
                    - Generates a new OTP.
                    - Stores the OTP with its configured expiration time.
                    - Sends the OTP to the user's registered email address.

                    The OTP expiration duration is configurable through
                    application properties.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Email verification OTP sent successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "Invalid email request or validation failure",
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
    @PostMapping("/email/send")
    public ResponseEntity<ApiResponse<Void>> sendEmail(
            @RequestBody @Valid EmailVerificationOtpRequest request
    ) {
        otpService.sendEmailVerificationOtp(request.email());

        return buildResponse(
                ApiSuccessCode.EMAIL_VERIFICATION_OTP_SENT,
                null,
                HttpStatus.OK
        );
    }

    // ================= VERIFY EMAIL OTP =================

    @Operation(
            summary = "Verify user email using OTP",
            description = """
                    Verifies the user's email address using the OTP sent
                    to their registered email.

                    - Checks whether the user exists.
                    - Retrieves the stored verification OTP.
                    - Checks whether the OTP has expired.
                    - Validates the submitted OTP.
                    - Marks the user's email as verified.
                    - Deletes the successfully used OTP.

                    An OTP can only be successfully used while it remains valid.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Email verified successfully",
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
                    description = "User not found",
                    content = @Content(
                            schema = @Schema(implementation = ApiResponse.class)
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "422",
                    description = """
                            OTP verification failed because the OTP is
                            missing, expired, or incorrect.
                            """,
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
    @PostMapping("/email/verify")
    public ResponseEntity<ApiResponse<Void>> verifyEmailOtp(
            @RequestBody @Valid VerifyEmailRequest request
    ) {
        otpService.verifyEmailVerificationOtp(request);

        return buildResponse(
                ApiSuccessCode.EMAIL_VERIFIED,
                null,
                HttpStatus.OK
        );
    }
}
