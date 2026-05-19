package com.tourism.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {
    private String policyId;
    private String policyName;
    private String version;
    private String description;
    private String domain;
    private List<Condition> conditions;
    private DecisionConfig decision;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Condition {
        private String field;
        private String operator; // equals, in, greaterThan, lessThan, regex
        private Object value;
        private String unit; // for time-based conditions
        private String riskLevel; // LOW, MEDIUM, HIGH
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DecisionConfig {
        private String riskThreshold;
        private Map<String, DecisionAction> action;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DecisionAction {
        private String decision;
        private String explanation;
    }

    public DecisionAction getDecisionAction(String riskLevel) {
        if (decision != null && decision.getAction() != null) {
            return decision.getAction().get(riskLevel);
        }
        return null;
    }
}
