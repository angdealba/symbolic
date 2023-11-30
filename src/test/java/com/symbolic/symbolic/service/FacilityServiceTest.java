package com.symbolic.symbolic.service;

import com.symbolic.symbolic.entity.Facility;
import com.symbolic.symbolic.repository.FacilityRepository;
import com.symbolic.symbolic.service.FacilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Tag("UnitTest")
public class FacilityServiceTest {

    @InjectMocks
    private FacilityService facilityService;

    @Mock
    private FacilityRepository facilityRepository;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testSearchFacility() {
        // Set up mock data
        Double latitude = 40.0;
        Double longitude = 74.0;
        String specialization = "Cardiology";

        // Mock search on facilityRepository
        Double minLatitude = latitude - 0.1;
        Double maxLatitude = latitude + 0.1;
        Double minLongitude = longitude - 0.1;
        Double maxLongitude = longitude + 0.1;
        Facility mockFacility1 = new Facility(40.05, 74.05, "Cardiology");
        Facility mockFacility2 = new Facility(38.9, 78.9, "Orthopedics");

        when(facilityRepository.findByLatitudeBetweenAndLongitudeBetween(minLatitude, maxLatitude, minLongitude, maxLongitude))
                .thenReturn(Arrays.asList(mockFacility1));


        // Mock Test
        List<Facility> result = facilityService.searchFacility(latitude, longitude, specialization);

        assertEquals(1, result.size());
        assertEquals("Cardiology", result.get(0).getSpecialization());
    }
}
