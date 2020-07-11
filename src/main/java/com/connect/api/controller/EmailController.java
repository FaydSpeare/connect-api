package com.connect.api.controller;

import com.connect.api.dto.response.LoginResponse;
import com.connect.api.service.EmailService;
import com.connect.api.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/email")
@Slf4j
public class EmailController {

    private final EmailService emailService;

    private final UserService userService;

    public EmailController(EmailService emailService, UserService userService) {
        this.emailService = emailService;
        this.userService = userService;
    }

    @ResponseBody
    @GetMapping(value = "/password-reset/{username}")
    public LoginResponse sendResetPasswordEmail(@PathVariable String username) {
        Long userId = userService.getUserIdFromUsername(username);
        if (userId != null) {
            String email = userService.getEmail(userId);
            if (email != null) {
                if (emailService.sendForgotPasswordEmail(userId, email)) {
                    return new LoginResponse(true, null, "Reset email sent successfully");
                }
            }
        }
        return new LoginResponse(false, null, "Couldn't find user with specified username");
    }

    @ResponseBody
    @GetMapping(value = "/verify-email/{userId}")
    public LoginResponse sendVerifyEmail(@PathVariable Long userId) {
        String email = userService.getEmail(userId);
        if (email != null) {
            if (emailService.sendVerifyEmail(userId, email)) {
                return new LoginResponse(true, null, "Verify email sent successfully");
            }
        }
        return new LoginResponse(false, null, "Couldn't send email");
    }

}
