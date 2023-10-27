package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.Diagnosis;
import com.symbolic.symbolic.service.HistoricalDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HistoricalDataController {

   @Autowired
    HistoricalDataService historicalDataService;

    @GetMapping("/history/week")
    public ResponseEntity<?> getHistoricalDataByConditionWeek(
            @RequestParam("condition") String condition,
            @RequestParam(required = false) List<String> location

    ) {
        System.out.println("In history/week");
        if(location != null && location.size() == 1){
            return new ResponseEntity<>("Missing coordinate information", HttpStatus.BAD_REQUEST);
        }
        List<Diagnosis> result = historicalDataService.getHistoricalDataByCondition(condition, java.sql.Date.valueOf(LocalDate.now().minusWeeks(1)), java.sql.Date.valueOf(LocalDate.now()), location);
        if(result.isEmpty()){
            return new ResponseEntity<>("Empty result table", HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/history/month")
    public ResponseEntity<?> getHistoricalDataByConditionMonth(
            @RequestParam("condition") String condition,
            @RequestParam(required = false) List<String> location
    ) {
        if(location.size() == 1){
            return new ResponseEntity<>("Missing coordinate information", HttpStatus.BAD_REQUEST);
        }
        List<Diagnosis> result = historicalDataService.getHistoricalDataByCondition(condition, java.sql.Date.valueOf(LocalDate.now().minusMonths(1)), java.sql.Date.valueOf(LocalDate.now()), location);
        if(result.isEmpty()){
            return new ResponseEntity<>("Empty result table", HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/history/year")
    public ResponseEntity<?> getHistoricalDataByConditionYear(
            @RequestParam("condition") String condition,
            @RequestParam(required = false) List<String> location
    ) {
        if(location.size() == 1){
            return new ResponseEntity<>("Missing coordinate information", HttpStatus.BAD_REQUEST);
        }
        List<Diagnosis> result = historicalDataService.getHistoricalDataByCondition(condition, java.sql.Date.valueOf(LocalDate.now().minusYears(1)), java.sql.Date.valueOf(LocalDate.now()), location);
        if(result.isEmpty()){
            return new ResponseEntity<>("Empty result table", HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistoricalDataByCondition(
            @RequestParam("condition") String condition,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
            @RequestParam(required = false) List<String> location
    ) {
        if(location.size() == 1){
            return new ResponseEntity<>("Missing coordinate information", HttpStatus.BAD_REQUEST);
        }
        List<Diagnosis> result = historicalDataService.getHistoricalDataByCondition(condition, startDate, endDate, location);
        if(result.isEmpty()){
            return new ResponseEntity<>("Empty result table", HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping("/history/top-conditions")
    public ResponseEntity<?> getTopConditions(
            @RequestParam("location") List<String> location,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
            @RequestParam(defaultValue = "3") int N
    ) {
        if(location.size() == 1){
            return new ResponseEntity<>("Missing coordinate information", HttpStatus.BAD_REQUEST);
        }

        Map<String, Integer> result = historicalDataService.getTopConditions(location, startDate, endDate, N);
        if(result.isEmpty()){
            return new ResponseEntity<>("Empty result table", HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}

