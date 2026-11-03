package com.authplatform.backend.service;

import com.authplatform.backend.dto.request.ChangePasswordRequest;
import com.authplatform.backend.dto.request.ForgotPasswordRequest;
import com.authplatform.backend.dto.request.ResetPasswordRequest;

public interface PasswordService {

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);

    void changePassword(ChangePasswordRequest request);
}
