package com.symbolic.symbolic.controller;

import com.symbolic.symbolic.entity.Patient;
import com.symbolic.symbolic.repository.AppointmentRepository;
import com.symbolic.symbolic.repository.DiagnosisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class BackgroundCheckController {
    @Autowired
    DiagnosisRepository diagnosisRepository;

    @GetMapping("/bgcheck")
    public ResponseEntity<?> checkBackground(@RequestParam("id") Long id,
                                             @RequestParam("vaccination") String vac,
                                             @RequestParam("allergy") String allergy,
                                             @RequestParam("diagnosis") String diagnosis,
                                             @RequestParam("insurance") String ins) {

        

    }
}
