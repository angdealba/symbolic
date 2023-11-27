package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.dtos.requests.UserAuthRequest;
import com.symbolic.symbolic.dtos.responses.UserAuthResponse;
import com.symbolic.symbolic.dtos.responses.UserRegistrationResponse;
import com.symbolic.symbolic.entity.User;
import com.symbolic.symbolic.service.UserAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements all functionality for the client data API.
 */
@RestController
@RequestMapping("/api/client")
public class UserController {
    @Autowired
    private  UserAuthService userAuthService;

    @PostMapping("/register")
    public ResponseEntity<UserRegistrationResponse> register(
            @RequestBody User registerRequest
    ) {
        return ResponseEntity.ok(userAuthService.register(registerRequest));
    }

    @PostMapping("/authenticate")
    public ResponseEntity<UserAuthResponse>   authenticate(
            @RequestBody UserAuthRequest userAuthRequest
    ) {
        return ResponseEntity.ok(userAuthService.authenticate(userAuthRequest));
    }
}
