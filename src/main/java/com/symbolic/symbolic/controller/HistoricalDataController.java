package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.Diagnosis;
import com.symbolic.symbolic.service.HistoricalDataService;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Implements all functionality for the historical data API.
 */
@RestController
@RequestMapping("/api")
public class HistoricalDataController {

  @Autowired
  HistoricalDataService historicalDataService;

  /* Object used to represent HTTP body requests */
  static class HistoricalDataBody {
    String condition;
    List<Double> location;
    Date startDate;
    Date endDate;
    int count;

    public String getCondition() {
      return condition;
    }

    public Date getEndDate() {
      return endDate;
    }

    public Date getStartDate() {
      return startDate;
    }

    public List<Double> getLocation() {
      return location;
    }

    public int getN() {
      return count;
    }

    public void setCondition(String condition) {
      this.condition = condition;
    }

    public void setEndDate(Date endDate) {
      this.endDate = endDate;
    }

    public void setLocation(List<Double> location) {
      this.location = location;
    }

    public void setStartDate(Date startDate) {
      this.startDate = startDate;
    }

    public void setN(int n) {
      count = n;
    }
  }

  /**
   * Retrieves a list of Diagnoses that meet the specified search criteria in the past week.
   */
  @GetMapping("/history/week")
  public ResponseEntity<?> getHistoricalDataByConditionWeek(
          @RequestBody HistoricalDataBody body
  ) {
    List<Double> location = body.getLocation();
    String condition = body.getCondition();

    if (condition == null) {
      return new ResponseEntity<>("Missing Condition Parameter", HttpStatus.BAD_REQUEST);
    }

    if (location != null) {
      if (location.size() != 2) {
        return new ResponseEntity<>("Incorrect coordinate information", HttpStatus.BAD_REQUEST);
      }
      if (location.get(0) < 0 || location.get(1) < 0) {
        return new ResponseEntity<>("Incorrect coordinate information", HttpStatus.BAD_REQUEST);
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
          @RequestBody HistoricalDataBody body
  ) {
    List<Double> location = body.getLocation();
    String condition = body.getCondition();

    if (condition == null) {
      return new ResponseEntity<>("Missing Condition Parameter", HttpStatus.BAD_REQUEST);
    }
    if (location != null) {
      if (location.size() != 2) {
        return new ResponseEntity<>("Missing coordinate information", HttpStatus.BAD_REQUEST);
      }
      if (location.get(0) < 0 || location.get(1) < 0) {
        return new ResponseEntity<>("Incorrect coordinate information", HttpStatus.BAD_REQUEST);
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
      @RequestBody HistoricalDataBody body
  ) {
    List<Double> location = body.getLocation();
    String condition = body.getCondition();

    if (condition == null) {
      return new ResponseEntity<>("Missing Condition Parameter", HttpStatus.BAD_REQUEST);
    }
    if (location != null) {
      if (location.size() != 2) {
        return new ResponseEntity<>("Missing coordinate information", HttpStatus.BAD_REQUEST);
      }
      if (location.get(0) < 0 || location.get(1) < 0) {
        return new ResponseEntity<>("Incorrect coordinate information", HttpStatus.BAD_REQUEST);
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
      @RequestBody HistoricalDataBody body
  ) {

    List<Double> location = body.getLocation();
    String condition = body.getCondition();
    Date startDate = body.getStartDate();
    Date endDate = body.getEndDate();

    if (condition == null) {
      return new ResponseEntity<>("Missing Condition Parameter", HttpStatus.BAD_REQUEST);
    }
    if (location != null) {
      if (location.size() != 2) {
        return new ResponseEntity<>("Missing coordinate information", HttpStatus.BAD_REQUEST);
      }
      if (location.get(0) < 0 || location.get(1) < 0) {
        return new ResponseEntity<>("Incorrect coordinate information", HttpStatus.BAD_REQUEST);
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
          @RequestBody HistoricalDataBody body
  ) {
    List<Double> location = body.getLocation();
    int n = body.getN();

    if (n == 0) {
      n = 3;
    }

    if (location.size() != 2) {
      return new ResponseEntity<>("Missing coordinate information", HttpStatus.BAD_REQUEST);
    }
    if (location.get(0) < 0 || location.get(1) < 0) {
      return new ResponseEntity<>("Incorrect coordinate information", HttpStatus.BAD_REQUEST);
    }

    Date startDate = body.getStartDate();
    Date endDate = body.getEndDate();
    Map<String, Integer> result = historicalDataService.getTopConditions(
        location, startDate, endDate, n);
    if (result.isEmpty()) {
      return new ResponseEntity<>("No results found", HttpStatus.NOT_FOUND);
    }
    return new ResponseEntity<>(result, HttpStatus.OK);
  }
}

