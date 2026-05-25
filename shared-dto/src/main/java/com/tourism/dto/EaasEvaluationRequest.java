package com.tourism.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EaasEvaluationRequest {
    private PlaceDTO placeData;
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
