package com.tourism.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EthicalRecommendationResponse {
    private PlaceDTO candidate;
    private String decision;
    private String rationale;
    private Double riskLevel;
    private String evaluationId;
    private EaasEvaluationResponse.AppliedPolicy[] appliedPolicies;
    private EaasEvaluationResponse.AuditTrail[] auditTrail;
}
