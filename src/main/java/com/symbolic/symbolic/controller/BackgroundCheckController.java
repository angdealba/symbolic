package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.repository.DiagnosisRepository;
import com.symbolic.symbolic.service.BackgroundCheckService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements all functionality for the background check API.
 */
@RestController
@RequestMapping("/api")
public class BackgroundCheckController {
  @Autowired
  DiagnosisRepository diagnosisRepository;

  /**
   * Implements the /bgcheck endpoint for running a health background check.
   */
  // Run a BG check on the requested id
  @GetMapping("/bgcheck")
  public ResponseEntity<?> checkBackground(
      @RequestParam("id") Long id,
      @RequestParam(value = "vaccine", required = false) String vac,
      @RequestParam(value = "allergy", required = false) String allergy,
      @RequestParam(value = "diagnosis", required = false) String diagnosis) {

    // Check for (mostly) empty input
    if (vac == null && allergy == null && diagnosis == null) {
      String errorMessage = "Missing at least one field to validate.";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    BackgroundCheckService backgroundCheckService = new BackgroundCheckService();
    Map<String, Boolean> backgroundCheck
        = backgroundCheckService.getBackgroundCheck(id, vac, allergy, diagnosis);

    if (backgroundCheck == null) {
      String errorMessage = "Empty patient table.";
      return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return new ResponseEntity<>(backgroundCheck, HttpStatus.OK);
  }
}
