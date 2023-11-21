package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.repository.DiagnosisRepository;
import com.symbolic.symbolic.service.BackgroundCheckService;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


/* Object used to represent HTTP body requests */
class BGCheckBody{
  Long id;
  String vaccine;
  String allergy;
  String diagnosis;

  public Long getId() {
    return id;
  }

  public String getAllergy() {
    return allergy;
  }

  public String getDiagnosis() {
    return diagnosis;
  }

  public String getVaccine() {
    return vaccine;
  }

  public void setAllergy(String allergy) {
    this.allergy = allergy;
  }

  public void setDiagnosis(String diagnosis) {
    this.diagnosis = diagnosis;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setVaccine(String vac) {
    this.vaccine = vac;
  }
}

/**
 * Implements all functionality for the background check API.
 */
@RestController
@RequestMapping("/api")
public class BackgroundCheckController {
  @Autowired
  BackgroundCheckService backgroundCheckService;


  /**
   * Implements the /bgcheck endpoint for running a health background check.
   */

  // Run a BG check on the requested id
  @GetMapping("/bgcheck")
  public ResponseEntity<?> checkBackground(
          @RequestBody BGCheckBody body) {

    if(body.getId() == null){
      return new ResponseEntity<>("Missing ID", HttpStatus.BAD_REQUEST);
    }
    // Check for (mostly) empty input
    if (body.getVaccine() == null && body.getAllergy() == null && body.getDiagnosis() == null) {
      String errorMessage = "Missing at least one field to validate.";
      return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
    }

    Map<String, Boolean> backgroundCheck
        = backgroundCheckService.getBackgroundCheck(body.getId(), body.getVaccine(), body.getAllergy(), body.getDiagnosis());

    if (backgroundCheck == null) {
      String errorMessage = "Empty patient table.";
      return new ResponseEntity<>(errorMessage, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return new ResponseEntity<>(backgroundCheck, HttpStatus.OK);
  }
}
