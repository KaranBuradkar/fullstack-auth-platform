package com.authplatform.backend.service;

import com.authplatform.backend.dto.request.ChangePasswordRequest;
import com.authplatform.backend.dto.request.ForgotPasswordRequest;
import com.authplatform.backend.dto.request.ResetPasswordRequest;
import jakarta.validation.Valid;

public interface PasswordService {

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(ChangePasswordRequest request);
}
