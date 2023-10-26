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

    // Run a complete BG check on the requested id
    @GetMapping("/bgcheck")
    public ResponseEntity<?> checkBackground(@RequestParam("id") Long id) {

        // Check vaccination records


        // Check allergy records


        // Check diagnosis records


        // Check insurance records


    }

    // Run a BG check for the specified fields
    @GetMapping("/bgcheck")
    public ResponseEntity<?> checkBackground(@RequestParam("id") Long id,
                                             @RequestParam("vaccination") String vac) {



    }

    @GetMapping("/bgcheck")
    public ResponseEntity<?> checkBackground(@RequestParam("id") Long id,
                                             @RequestParam("allergy") String allergy) {



    }

    @GetMapping("/bgcheck")
    public ResponseEntity<?> checkBackground(@RequestParam("id") Long id,
                                             @RequestParam("diagnosis") String diagnosis) {



    }

    @GetMapping("/bgcheck")
    public ResponseEntity<?> checkBackground(@RequestParam("id") Long id,
                                             @RequestParam("insurance") String ins) {



    }
}
