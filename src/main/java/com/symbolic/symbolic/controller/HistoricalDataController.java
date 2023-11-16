package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.Diagnosis;
import com.symbolic.symbolic.service.HistoricalDataService;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements all functionality for the historical data API.
 */
@RestController
@RequestMapping("/api")
public class HistoricalDataController {

  @Autowired
  HistoricalDataService historicalDataService;

  /**
   * Retrieves a list of Diagnoses that meet the specified search criteria in the past week.
   */
  @GetMapping("/history/week")
  public ResponseEntity<?> getHistoricalDataByConditionWeek(
      @RequestParam("condition") String condition,
      @RequestParam(required = false) List<String> location

  ) {
    if (location != null) {
      if (location.size() == 1) {
        return new ResponseEntity<>("Missing coordinate information", HttpStatus.BAD_REQUEST);
      }
    }

    List<Diagnosis> result = historicalDataService.getHistoricalDataByCondition(condition,
        java.sql.Date.valueOf(LocalDate.now().minusWeeks(1)),
        java.sql.Date.valueOf(LocalDate.now()), location);
    if (result.isEmpty()) {
      return new ResponseEntity<>("No results found", HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  /**
   * Retrieves a list of Diagnoses that meet the specified search criteria in the past month.
   */
  @GetMapping("/history/month")
  public ResponseEntity<?> getHistoricalDataByConditionMonth(
      @RequestParam("condition") String condition,
      @RequestParam(required = false) List<String> location
  ) {
    if (location != null) {
      if (location.size() == 1) {
        return new ResponseEntity<>("Missing coordinate information", HttpStatus.BAD_REQUEST);
      }
    }
    List<Diagnosis> result = historicalDataService.getHistoricalDataByCondition(condition,
        java.sql.Date.valueOf(LocalDate.now().minusMonths(1)),
        java.sql.Date.valueOf(LocalDate.now()), location);
    if (result.isEmpty()) {
      return new ResponseEntity<>("No results found", HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  /**
   * Retrieves a list of Diagnoses that meet the specified search criteria in the past year.
   */
  @GetMapping("/history/year")
  public ResponseEntity<?> getHistoricalDataByConditionYear(
      @RequestParam("condition") String condition,
      @RequestParam(required = false) List<String> location
  ) {
    if (location != null) {
      if (location.size() == 1) {
        return new ResponseEntity<>("Missing coordinate information", HttpStatus.BAD_REQUEST);
      }
    }
    List<Diagnosis> result = historicalDataService.getHistoricalDataByCondition(condition,
        java.sql.Date.valueOf(LocalDate.now().minusYears(1)),
        java.sql.Date.valueOf(LocalDate.now()), location);
    if (result.isEmpty()) {
      return new ResponseEntity<>("No results found", HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  /**
   * Retrieves a list of Diagnoses that meet the specified search criteria with a custom date range.
   */
  @GetMapping("/history")
  public ResponseEntity<?> getHistoricalDataByCondition(
      @RequestParam("condition") String condition,
      @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
      @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
      @RequestParam(required = false) List<String> location
  ) {
    if (location != null) {
      if (location.size() == 1) {
        return new ResponseEntity<>("Missing coordinate information", HttpStatus.BAD_REQUEST);
      }
    }
    List<Diagnosis> result = historicalDataService.getHistoricalDataByCondition(
        condition, startDate, endDate, location);
    if (result.isEmpty()) {
      return new ResponseEntity<>("No results found", HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }

  /**
   * Retrieves a map of the top most prevalent Diagnoses and the frequencies over the time range.
   */
  @GetMapping("/history/top-conditions")
  public ResponseEntity<?> getTopConditions(
      @RequestParam("location") List<String> location,
      @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
      @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
      @RequestParam(defaultValue = "3") int n
  ) {
    if (location.size() == 1) {
      return new ResponseEntity<>("Missing coordinate information", HttpStatus.BAD_REQUEST);
    }

    Map<String, Integer> result = historicalDataService.getTopConditions(
        location, startDate, endDate, n);
    if (result.isEmpty()) {
      return new ResponseEntity<>("No results found", HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}

