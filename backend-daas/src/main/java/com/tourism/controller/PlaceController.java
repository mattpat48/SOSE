package com.tourism.controller;

import com.tourism.dto.PlaceDTO;
import com.tourism.service.TourismQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/places")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class PlaceController {

    private final TourismQueryService queryService;

    @Autowired
    public PlaceController(TourismQueryService queryService) {
        this.queryService = queryService;
    }

    @GetMapping("/search/location/{location}")
    public ResponseEntity<List<PlaceDTO>> searchByLocation(@PathVariable String location) {
        log.info("GET /api/places/search/location/{}", location);
        return ResponseEntity.ok(queryService.searchPlacesByLocation(location));
    }

    @GetMapping("/search/category/{category}")
    public ResponseEntity<List<PlaceDTO>> searchByCategory(@PathVariable String category) {
        log.info("GET /api/places/search/category/{}", category);
        return ResponseEntity.ok(queryService.searchByCategory(category));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlaceDTO> getPlaceDetails(@PathVariable String id) {
        log.info("GET /api/places/{}", id);
        PlaceDTO place = queryService.getPlaceById(id);
        if (place == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(place);
    }

    @GetMapping("/search/multi-criteria")
    public ResponseEntity<List<PlaceDTO>> multiCriteriaSearch(
            @RequestParam(defaultValue = "Museum") String category,
            @RequestParam(defaultValue = "WheelchairAccessible") String accessibility,
            @RequestParam(defaultValue = "Sustainable") String sustainability,
            @RequestParam(defaultValue = "4.0") Double minRating) {

        log.info("GET /api/places/search/multi-criteria - category={}, accessibility={}, sustainability={}, minRating={}",
            category, accessibility, sustainability, minRating);

        return ResponseEntity.ok(
            queryService.searchMultiCriteria(category, accessibility, sustainability, minRating)
        );
    }

    @GetMapping
    public ResponseEntity<String> healthCheck() {
        log.info("Health check on /api/places");
        return ResponseEntity.ok("{\"status\": \"DaaS is running\"}");
    }

}
