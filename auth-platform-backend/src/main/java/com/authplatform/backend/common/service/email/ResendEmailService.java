package com.authplatform.backend.common.service.email;

import com.authplatform.backend.common.constants.EmailHtmlConstants;
import com.authplatform.backend.common.exception.EmailFailedToSendException;
import com.resend.Resend;

import com.authplatform.backend.config.EmailProperties;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("resendEmailService")
public class ResendEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailService.class);
    private final Resend resend;
    private final EmailProperties emailProperties;

    public ResendEmailService(EmailProperties emailProperties) {
        this.resend = new Resend(emailProperties.apiKey());
        this.emailProperties = emailProperties;
    }

    @Override
    public void sendVerificationOtp(String email, String otp) {
        String html = EmailHtmlConstants.buildVerificationOtpEmail(otp);
        sendEmail(email, "Verify your email - Auth Platform", html);
    }

    private void sendEmail(String to, String subject, String html) {
        try {
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(emailProperties.fromEmail())
                    .to(to)
                    .subject(subject)
                    .html(html)
                    .build();

            CreateEmailResponse send = resend.emails().send(params);
        } catch (ResendException e) {
            throw new EmailFailedToSendException();
        }
    }
}
