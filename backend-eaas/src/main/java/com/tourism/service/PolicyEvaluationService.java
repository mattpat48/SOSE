package com.tourism.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tourism.dto.EaasEvaluationRequest;
import com.tourism.dto.EaasEvaluationResponse;
import com.tourism.dto.PlaceDTO;
import com.tourism.exception.PolicyLoadingException;
import com.tourism.model.Policy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PolicyEvaluationService {

    private final ObjectMapper objectMapper;
    private List<Policy> policies;
    private static final String POLICIES_PATH = "classpath:policies/*.json";

    @Autowired
    public PolicyEvaluationService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.loadPolicies();
    }

    private void loadPolicies() {
        policies = new ArrayList<>();
        try {
            log.info("Loading policies from: {}", POLICIES_PATH);
            Resource[] resources = new PathMatchingResourcePatternResolver()
                .getResources(POLICIES_PATH);

            log.info("Found {} policy files", resources.length);
            for (Resource resource : resources) {
                try {
                    Policy policy = objectMapper.readValue(resource.getInputStream(), Policy.class);
                    policies.add(policy);
                    log.info("Loaded policy: {} ({})", policy.getPolicyId(), policy.getPolicyName());
                } catch (Exception e) {
                    log.error("Failed to load policy from {}", resource.getFilename(), e);
                }
            }
            log.info("Successfully loaded {} policies", policies.size());
        } catch (IOException e) {
            log.error("Failed to load policies from classpath", e);
            throw new PolicyLoadingException("Failed to load policies from classpath", e);
        }
    }

    public EaasEvaluationResponse evaluateAgainstPolicies(EaasEvaluationRequest request) {
        log.info("Evaluating place: {}", request.getPlaceData().getName());

        EaasEvaluationResponse response = EaasEvaluationResponse.builder()
            .evaluationId(UUID.randomUUID().toString())
            .timestamp(LocalDateTime.now())
            .placeName(request.getPlaceData().getName())
            .build();

        List<EaasEvaluationResponse.AuditTrail> auditTrail = new ArrayList<>();
        List<EaasEvaluationResponse.AppliedPolicy> appliedPolicies = new ArrayList<>();

        String finalDecision = "PROCEED";
        String maxRiskLevel = "LOW";

        // Evaluate against each policy
        for (Policy policy : policies) {
            log.debug("Evaluating policy: {}", policy.getPolicyId());
            String policyRiskLevel = evaluatePolicyConditions(policy, request.getPlaceData(), auditTrail);

            // Update decision based on highest risk
            if (compareRiskLevels(policyRiskLevel, maxRiskLevel) > 0) {
                maxRiskLevel = policyRiskLevel;
                Policy.DecisionAction action = policy.getDecisionAction(maxRiskLevel);
                if (action != null) {
                    finalDecision = action.getDecision();
                }
            }

            EaasEvaluationResponse.AppliedPolicy appliedPolicy = EaasEvaluationResponse.AppliedPolicy.builder()
                .policyId(policy.getPolicyId())
                .policyName(policy.getPolicyName())
                .domain(policy.getDomain())
                .riskAssessment(policyRiskLevel)
                .build();
            appliedPolicies.add(appliedPolicy);
        }

        response.setDecision(finalDecision);
        response.setRiskLevel(maxRiskLevel);
        response.setAppliedPolicies(appliedPolicies);
        response.setAuditTrail(auditTrail);
        response.setRationale(generateRationale(finalDecision, appliedPolicies, response.getAuditTrail()));

        log.info("Evaluation result: decision={}, riskLevel={}", finalDecision, maxRiskLevel);
        return response;
    }

    private String evaluatePolicyConditions(Policy policy, PlaceDTO place,
                                           List<EaasEvaluationResponse.AuditTrail> auditTrail) {
        String highestRisk = "LOW";

        for (Policy.Condition condition : policy.getConditions()) {
            boolean conditionMet = evaluateCondition(condition, place);

            if (conditionMet) {
                EaasEvaluationResponse.AuditTrail trail = EaasEvaluationResponse.AuditTrail.builder()
                    .policyId(policy.getPolicyId())
                    .conditionEvaluated(formatConditionDescription(condition))
                    .result(true)
                    .riskLevel(condition.getRiskLevel())
                    .build();
                auditTrail.add(trail);

                if (compareRiskLevels(condition.getRiskLevel(), highestRisk) > 0) {
                    highestRisk = condition.getRiskLevel();
                }
            }
        }

        return highestRisk;
    }

    private boolean evaluateCondition(Policy.Condition condition, PlaceDTO place) {
        String field = condition.getField();
        if (field == null) return false;

        Object fieldValue = getFieldValue(field, place);
        if (fieldValue == null) return false;

        switch (condition.getOperator()) {
            case "equals":
                return fieldValue.toString().equalsIgnoreCase(condition.getValue().toString());

            case "in":
                if (condition.getValue() instanceof List) {
                    List<?> values = (List<?>) condition.getValue();
                    return values.stream()
                        .anyMatch(v -> v.toString().equalsIgnoreCase(fieldValue.toString()));
                }
                return false;

            case "greaterThan":
                try {
                    long fieldDays = parseFieldAsDays(fieldValue);
                    long conditionDays = ((Number) condition.getValue()).longValue();
                    return fieldDays > conditionDays;
                } catch (Exception e) {
                    log.warn("Failed to compare greaterThan condition", e);
                    return false;
                }

            case "regex":
                return fieldValue.toString().matches(condition.getValue().toString());

            default:
                log.warn("Unknown operator: {}", condition.getOperator());
                return false;
        }
    }

    private long parseFieldAsDays(Object fieldValue) {
        if (fieldValue instanceof LocalDateTime) {
            return ChronoUnit.DAYS.between((LocalDateTime) fieldValue, LocalDateTime.now());
        }
        return 0;
    }

    private Object getFieldValue(String field, PlaceDTO place) {
        switch (field) {
            case "accessibility":
                return place.getAccessibility();
            case "crowdingLevel":
                return place.getCrowdingLevel();
            case "hasSustainability":
                return place.getSustainabilityLevel();
            case "provenance":
                return place.getProvenance();
            case "lastUpdatedOn":
                return place.getLastUpdated();
            case "dataAgeSinceUpdate":
                if (place.getLastUpdated() != null) {
                    return ChronoUnit.DAYS.between(place.getLastUpdated(), LocalDateTime.now());
                }
                return 0;
            case "ethicalRating":
                return place.getEthicalRating();
            case "rating":
                return place.getRating();
            default:
                return null;
        }
    }

    private String formatConditionDescription(Policy.Condition condition) {
        return condition.getField() + " " + condition.getOperator() + " " + condition.getValue();
    }

    private String generateRationale(String decision, List<EaasEvaluationResponse.AppliedPolicy> appliedPolicies,
                                     List<EaasEvaluationResponse.AuditTrail> auditTrail) {
        StringBuilder rationale = new StringBuilder();

        for (EaasEvaluationResponse.AppliedPolicy policy : appliedPolicies) {
            List<EaasEvaluationResponse.AuditTrail> policyTrails = auditTrail.stream()
                .filter(t -> t.getPolicyId().equals(policy.getPolicyId()))
                .collect(Collectors.toList());

            if (!policyTrails.isEmpty()) {
                if (rationale.length() > 0) {
                    rationale.append(" ");
                }
                rationale.append("Policy [").append(policy.getDomain()).append("]: ");
                rationale.append(policyTrails.get(0).getRiskLevel()).append(" risk");
            }
        }

        return rationale.length() > 0 ? rationale.toString() : "Evaluation completed with " + decision;
    }

    private int compareRiskLevels(String risk1, String risk2) {
        int risk1Score = riskScore(risk1);
        int risk2Score = riskScore(risk2);
        return Integer.compare(risk1Score, risk2Score);
    }

    private int riskScore(String risk) {
        switch (risk) {
            case "HIGH":
                return 3;
            case "MEDIUM":
                return 2;
            case "LOW":
                return 1;
            default:
                return 0;
        }
    }

}
