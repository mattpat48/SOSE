package com.tourism.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationRequest {
    private String category;
    private String accessibility;
    private String sustainability;
    private Double minRating;
    private UserContext userContext;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UserContext {
        private String userId;
        private List<String> preferences;
        private List<String> accessibilityNeeds;
    }
}
