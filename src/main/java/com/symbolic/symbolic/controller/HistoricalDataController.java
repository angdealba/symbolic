package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.services.HistoricalDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api")
public class HistoricalDataController {

   @Autowired
    HistoricalDataService historicalDataService;

    @GetMapping("/history/week")
    public ResponseEntity<?> getHistoricalDataByConditionWeek(
            @RequestParam("condition") String condition,
            @RequestParam(required = false) String location
    ) {

        return ResponseEntity.ok(historicalDataService.getHistoricalDataByCondition(condition, java.sql.Date.valueOf(LocalDate.now().minusWeeks(1)), java.sql.Date.valueOf(LocalDate.now()), location));
    }

    @GetMapping("/history/month")
    public ResponseEntity<?> getHistoricalDataByConditionMonth(
            @RequestParam("condition") String condition,
            @RequestParam(required = false) String location
    ) {

        return ResponseEntity.ok(historicalDataService.getHistoricalDataByCondition(condition, java.sql.Date.valueOf(LocalDate.now().minusMonths(1)), java.sql.Date.valueOf(LocalDate.now()), location));
    }

    @GetMapping("/history/year")
    public ResponseEntity<?> getHistoricalDataByConditionYear(
            @RequestParam("condition") String condition,
            @RequestParam(required = false) String location
    ) {

        return ResponseEntity.ok(historicalDataService.getHistoricalDataByCondition(condition, java.sql.Date.valueOf(LocalDate.now().minusYears(1)), java.sql.Date.valueOf(LocalDate.now()), location));
    }

    @GetMapping("/history")
    public ResponseEntity<?> getHistoricalDataByCondition(
            @RequestParam("condition") String condition,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
            @RequestParam(required = false) String location
    ) {

        return ResponseEntity.ok(historicalDataService.getHistoricalDataByCondition(condition, startDate, endDate, location));
    }

    @GetMapping("/history/top-conditions")
    public ResponseEntity<?> getTopConditions(
            @RequestParam("location") String location,
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date startDate,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date endDate,
            @RequestParam(required = false) int N
    ) {

        return ResponseEntity.ok(historicalDataService.getTopConditions(location, startDate, endDate, N));
    }
}

