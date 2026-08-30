package com.finz.user.controller;

import com.finz.common.security.CurrentUserProvider;
import com.finz.user.dto.UserResponse;
import com.finz.user.entity.User;
import com.finz.user.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final CurrentUserProvider currentUserProvider;

    public UserController(UserService userService, CurrentUserProvider currentUserProvider) {
        this.userService = userService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/me")
    public UserResponse me() {
        User user = userService.getById(currentUserProvider.getCurrentUserId());
        return UserResponse.from(user);
    }
}
