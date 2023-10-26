package com.symbolic.symbolic.services;

import com.symbolic.symbolic.entity.Diagnosis;
import com.symbolic.symbolic.repository.DiagnosisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service

public class HistoricalDataService {

    @Autowired
    DiagnosisRepository diagnosisRepository;

    public List<Diagnosis> getHistoricalDataByCondition(String condition, Date startDate, Date endDate, String location) {


        return List.of();
    }

    public List<Diagnosis> getTopConditions(String location, Date startDate, Date endDate, int N) {

        return List.of();
    }
}
