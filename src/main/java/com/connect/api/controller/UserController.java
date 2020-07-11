package com.connect.api.controller;

import com.connect.api.dto.request.CommentRequest;
import com.connect.api.dto.request.CreateUserRequest;
import com.connect.api.dto.response.LoginResponse;
import com.connect.api.dto.response.UpdateDetailsResponse;
import com.connect.api.dto.response.UserProfileResponse;
import com.connect.api.dto.response.UserSearchResponse;
import com.connect.api.service.CodeService;
import com.connect.api.service.UserService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/user")
public class UserController {

    private final CodeService codeService;

    private final UserService userService;

    public UserController(CodeService codeService, UserService userService) {
        this.codeService = codeService;
        this.userService = userService;
    }

    @ResponseBody
    @PostMapping(value = "/create-user")
    public LoginResponse createNewUser(@RequestBody CreateUserRequest request) {
        return userService.addNewUser(request);
    }

    @ResponseBody
    @GetMapping(value = "/{username}/{password}/validate")
    public LoginResponse validateUserLogin(@PathVariable String username, @PathVariable String password) {
        return userService.verifyLogin(username, password);
    }

    @ResponseBody
    @GetMapping(value="/user-profile/{userId}")
    public UserProfileResponse getUserProfile(@PathVariable Long userId) {
        return userService.getUserProfile(userId);
    }

    @ResponseBody
    @PostMapping(value = "/user-image/{userId}")
    public void uploadUserProfileImage(@RequestParam("file") MultipartFile file, @PathVariable Long userId) throws IOException {
        System.out.println("File upload request received");
        userService.saveProfileImage(userId, file);
    }

    @ResponseBody
    @PostMapping(value="/comment")
    public void addComment(@RequestBody CommentRequest commentRequest) {
        System.out.println("Comment request");
        userService.addComment(commentRequest);
    }

    @ResponseBody
    @GetMapping(value="/username/{username}")
    public UserSearchResponse doesUserExist(@PathVariable String username) {
        Long userId = userService.getUserIdFromUsername(username);
        UserSearchResponse response = new UserSearchResponse();
        response.setUserId(userId);
        response.setErrorMessage(userId == null ? "Unable to find user" : "Successfully found user");
        return response;

    }

    @ResponseBody
    @GetMapping(value="/{userId}/update-password/{password}")
    public UpdateDetailsResponse updatePassword(@PathVariable Long userId, @PathVariable String password) {
        return userService.updatePassword(userId, password);
    }

    @ResponseBody
    @GetMapping(value="/{userId}/update-email/{email}")
    public UpdateDetailsResponse updateEmail(@PathVariable Long userId, @PathVariable String email) {
        return userService.updateEmail(userId, email);
    }

    @ResponseBody
    @GetMapping(value="/verify-email")
    public UpdateDetailsResponse verifyEmail(@RequestParam String code) {
        Long userId = codeService.isCodeValid(code);
        if (userId != null) {
            UpdateDetailsResponse response = userService.verifyEmail(userId);
            if (response.isValid()) {
                codeService.removeCode(code);
            }
            return response;
        }
        return new UpdateDetailsResponse(false, "Link is invalid or has already been used");
    }

    @ResponseBody
    @GetMapping(value="/reset-password/{password}")
    public UpdateDetailsResponse resetPassword(@PathVariable String password, @RequestParam String code) {
        Long userId = codeService.isCodeValid(code);
        if (userId != null) {
            UpdateDetailsResponse response = userService.updatePassword(userId, password);
            if (response.isValid()) {
                codeService.removeCode(code);
            }
            return response;
        }
        return new UpdateDetailsResponse(false, "Link is invalid or has already been used");
    }

    @ResponseBody
    @GetMapping(value="/delete-account/{userId}/{password}")
    public Boolean resetPassword(@PathVariable Long userId, @PathVariable String password) {
        if (userService.verifyPassword(userId, password)) {
            userService.deleteUser(userId);
            return true;
        }
        return false;
    }

}
