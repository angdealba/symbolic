package com.symbolic.symbolic.service;

import com.symbolic.symbolic.configuration.security.JwtService;
import com.symbolic.symbolic.dtos.requests.UserAuthRequest;
import com.symbolic.symbolic.dtos.responses.UserAuthResponse;
import com.symbolic.symbolic.dtos.responses.UserRegistrationResponse;
import com.symbolic.symbolic.entity.Role;
import com.symbolic.symbolic.entity.User;
import com.symbolic.symbolic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Custom Authentication service that handles the interaction between user operations and the DB.
 */
@Service
@RequiredArgsConstructor
public class UserAuthService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  /**
   * Service command for handling a User registration event by saving it to the database.
   *
   * @param client the new user to be added to the database
   * @return a custom response object containing a message and status code
   */
  public UserRegistrationResponse register(User client) {
    var clientDetails = User.builder()
        .name(client.getName())
        .password(passwordEncoder.encode(client.getPassword()))
        .role(Role.VACCINATION_RECORD_APP)
        .description(client.getDescription())
        .build();
    userRepository.save(clientDetails);

    return UserRegistrationResponse.builder()
        .message("Registration staged successfully")
        .statusCode("0")
        .build();

  }

  /**
   * Service command for handling an authentication event by retrieving it from the database.
   *
   * @param userAuthRequest the authentication details to be checked
   * @return a JSON web token for the authenticated user
   */
  public UserAuthResponse authenticate(UserAuthRequest userAuthRequest) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            userAuthRequest.getName(),
            userAuthRequest.getPassword()
        )
    );

    var user = userRepository.findByName(userAuthRequest.getName())
        .orElseThrow();

    var jwtToken = jwtService.generateToken(user);
    return UserAuthResponse.builder()
        .token(jwtToken)
        .build();
  }
}
