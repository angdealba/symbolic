package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.dtos.requests.UserAuthRequest;
import com.symbolic.symbolic.dtos.responses.UserAuthResponse;
import com.symbolic.symbolic.dtos.responses.UserRegistrationResponse;
import com.symbolic.symbolic.entity.User;
import com.symbolic.symbolic.service.UserAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
  private UserAuthService userAuthService;

  /**
   * Controller command for handling a User registration event by saving it to the database.
   *
   * @param registerRequest a User object that will be registered with the service
   * @return an HTTP response entity containing an error message or custom response object
   */
  @PostMapping("/register")
  public ResponseEntity<?> register(
      @RequestBody User registerRequest
  ) {
    if (registerRequest.getName() == null) {
      String errorMessage = "Missing 'name' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (registerRequest.getPassword() == null) {
      String errorMessage = "Missing 'password' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else {
      return ResponseEntity.ok(userAuthService.register(registerRequest));
    }
  }

  /**
   * Controller command for handling an authentication event by retrieving it from the database.
   *
   * @param userAuthRequest the authentication details to be checked
   * @return an HTTP response entity containing an error message or a JSON web token for the user
   */
  @PostMapping("/authenticate")
  public ResponseEntity<?> authenticate(
      @RequestBody UserAuthRequest userAuthRequest
  ) {
    if (userAuthRequest.getName() == null) {
      String errorMessage = "Missing 'name' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else if (userAuthRequest.getPassword() == null) {
      String errorMessage = "Missing 'password' field in request body";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    } else {
      return ResponseEntity.ok(userAuthService.authenticate(userAuthRequest));
    }
  }
}
