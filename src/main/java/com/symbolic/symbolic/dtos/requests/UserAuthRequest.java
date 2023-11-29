package com.symbolic.symbolic.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Custom User authentication request consisting of a username and password.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserAuthRequest {
  private String name;
  private String password;
}
