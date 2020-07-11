package com.connect.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final String RESET_LINK = "http://d38r3swoojgzyc.cloudfront.net/update-password/";
    private static final String VERIFY_LINK = "http://d38r3swoojgzyc.cloudfront.net/verify/";

    private final CodeService codeService;

    public EmailService(CodeService codeService) {
        this.codeService = codeService;
    }

    public boolean sendVerifyEmail(Long userId, String email) {
        String code = codeService.createCode(userId);
        String subject = "Verify Email";
        String body = "Head to this link to verify your email: " + VERIFY_LINK + code;
        return this.send(subject, email, body);
    }

    public boolean sendForgotPasswordEmail(Long userId, String email) {
        String code = codeService.createCode(userId);
        String subject = "Password Reset";
        String body = "Head to this link to reset your password: " + RESET_LINK + code;
        return this.send(subject, email, body);
    }

    private boolean send(String subject, String email, String body) {
        try {
            AmazonSESSSender.sendEmail(subject, email, body);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
