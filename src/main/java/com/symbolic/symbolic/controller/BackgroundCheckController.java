package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.service.BackgroundCheckService;
import com.symbolic.symbolic.repository.DiagnosisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api")
public class BackgroundCheckController {
    @Autowired
    DiagnosisRepository diagnosisRepository;

    // Run a BG check on the requested id
    @GetMapping("/bgcheck")
    public ResponseEntity<?> checkBackground(@RequestParam("id") Long id,
                                             @RequestParam(required=false) String vac,
                                             @RequestParam(required=false) String allergy,
                                             @RequestParam(required=false) String diagnosis) {

        // Check for (mostly) empty input
        if (vac == null && allergy == null && diagnosis == null) {
            String errorMessage = "Missing at least one field to validate.";
            return new ResponseEntity<>(errorMessage, HttpStatus.BAD_REQUEST);
        }

        BackgroundCheckService backgroundCheckService = new BackgroundCheckService();
        Map<String, Boolean> backgroundCheck = backgroundCheckService.getBGCheck(id, vac, allergy, diagnosis);

        return new ResponseEntity<>(backgroundCheck, HttpStatus.OK);
    }
}
