package com.symbolic.symbolic.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Custom response from User authentication requests consisting of a message and status code.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationResponse {
  private String message;
  private String statusCode;
}

