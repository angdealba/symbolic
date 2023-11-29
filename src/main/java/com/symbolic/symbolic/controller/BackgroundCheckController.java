package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.service.BackgroundCheckService;

import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements all functionality for the background check API.
 */
@RestController
@Secured({"ROLE_ADMIN", "ROLE_VACCINATION_RECORD_APP"})
@RequestMapping("/api")
public class BackgroundCheckController {
  @Autowired
  BackgroundCheckService backgroundCheckService;

  /* Object used to represent HTTP body requests */
  static class BackgroundCheckBody {
    private String id;
    private String vaccination;
    private String allergy;
    private String diagnosis;

    public String getId() {
      return id;
    }

    public String getAllergy() {
      return allergy;
    }

    public String getDiagnosis() {
      return diagnosis;
    }

    public String getVaccination() {
      return vaccination;
    }

    public void setAllergy(String allergy) {
      this.allergy = allergy;
    }

    public void setDiagnosis(String diagnosis) {
      this.diagnosis = diagnosis;
    }

    public void setId(String id) {
      this.id = id;
    }

    public void setVaccine(String vac) {
      this.vaccination = vac;
    }
  }

  /**
   * Parses a string input into a UUID object type for use in database lookup operations.
   *
   * @param uuidString a string value representing the UUID in the HTTP request.
   * @return A valid UUID if the string can be converted successfully, or null if it cannot.
   */
  private static UUID parseUuidFromString(String uuidString) {
    try {
      return UUID.fromString(uuidString);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }

  /**
   * Implements the /bgcheck endpoint for running a health background check.
   */

  // Run a BG check on the requested id
  @GetMapping("/bgcheck")
  public ResponseEntity<?> checkBackground(
      @RequestBody BackgroundCheckBody body) {

    if (body.getId() == null) {
      return new ResponseEntity<>("Missing ID", HttpStatus.BAD_REQUEST);
    }

    // Check for (mostly) empty input
    if (body.getVaccination() == null && body.getAllergy() == null
        && body.getDiagnosis() == null) {
      String errorMessage = "Missing at least one field to validate.";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    UUID id = parseUuidFromString(body.getId());
    if (id == null) {
      String errorMessage = "'id' field must contain a valid UUID value";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Map<String, Boolean> backgroundCheck
        = backgroundCheckService.getBackgroundCheck(id, body.getVaccination(),
        body.getAllergy(), body.getDiagnosis());

    if (backgroundCheck == null) {
      String errorMessage = "Requested patient not found.";
      return new ResponseEntity<>(errorMessage, HttpStatus.NOT_FOUND);
    }

    return new ResponseEntity<>(backgroundCheck, HttpStatus.OK);
  }
}