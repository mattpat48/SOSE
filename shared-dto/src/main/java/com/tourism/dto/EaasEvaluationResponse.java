package com.tourism.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EaasEvaluationResponse {
    private String evaluationId;
    private LocalDateTime timestamp;
    private String placeName;
    private String decision; // PROCEED, REVISE, ESCALATE, REJECT
    private String riskLevel; // LOW, MEDIUM, HIGH
    private String rationale;
    private List<AppliedPolicy> appliedPolicies;
    private List<AuditTrail> auditTrail;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AppliedPolicy {
        private String policyId;
        private String policyName;
        private String domain;
        private String riskAssessment;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AuditTrail {
        private String policyId;
        private String conditionEvaluated;
        private boolean result;
        private String riskLevel;
    }
}
