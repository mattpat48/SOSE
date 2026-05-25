package com.tourism.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlaceDTO {
    private String uri;
    private String name;
    private String description;
    private String category;
    private String location;
    private String accessibility;
    private Double rating;
    private String crowdingLevel;
    private String sustainabilityLevel;
    private Double ethicalRating;
    private String provenance;
    private LocalDateTime lastUpdated;
}
