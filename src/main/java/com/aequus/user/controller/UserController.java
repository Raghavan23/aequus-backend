package com.aequus.user.controller;

import com.aequus.common.security.CurrentUserProvider;
import com.aequus.user.dto.UserResponse;
import com.aequus.user.entity.User;
import com.aequus.user.service.UserService;
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
