package com.symbolic.symbolic.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Custom response from User authentication requests consisting of a JSON web token string.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthResponse {
  private String token;
}
