package com.tourism.controller;

import com.tourism.client.EaasClient;
import com.tourism.dto.*;
import com.tourism.service.TourismQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class RecommendationController {

    private final TourismQueryService queryService;
    private final EaasClient eaasClient;

    @Autowired
    public RecommendationController(TourismQueryService queryService, EaasClient eaasClient) {
        this.queryService = queryService;
        this.eaasClient = eaasClient;
    }

    @PostMapping("/ethical-recommendation")
    public ResponseEntity<EthicalRecommendationResponse> getEthicalRecommendation(
            @RequestBody RecommendationRequest request) {

        log.info("POST /api/recommendations/ethical-recommendation - category={}, accessibility={}, sustainability={}",
            request.getCategory(), request.getAccessibility(), request.getSustainability());

        try {
            // Step 1: Query DaaS for candidate places
            List<PlaceDTO> candidates = queryService.searchMultiCriteria(
                request.getCategory(),
                request.getAccessibility(),
                request.getSustainability(),
                request.getMinRating() != null ? request.getMinRating() : 4.0
            );

            if (candidates.isEmpty()) {
                log.warn("No matching places found for criteria");
                return ResponseEntity.ok(
                    EthicalRecommendationResponse.builder()
                        .decision("REJECT")
                        .rationale("No matching places found for the specified criteria")
                        .build()
                );
            }

            PlaceDTO candidate = candidates.get(0);
            log.info("Found candidate place: {}", candidate.getName());

            // Step 2: Call EaaS for ethical evaluation
            EaasEvaluationRequest eaasRequest = EaasEvaluationRequest.builder()
                .placeData(candidate)
                .userContext(request.getUserContext() != null ? 
                    EaasEvaluationRequest.UserContext.builder()
                        .userId(request.getUserContext().getUserId())
                        .preferences(request.getUserContext().getPreferences())
                        .accessibilityNeeds(request.getUserContext().getAccessibilityNeeds())
                        .build() 
                    : null)
                .build();

            EaasEvaluationResponse eaasResponse = eaasClient.evaluatePlace(eaasRequest);
            log.info("EaaS evaluation completed with decision: {}", eaasResponse.getDecision());

            // Step 3: Combine DaaS data with EaaS decision
            EthicalRecommendationResponse response = EthicalRecommendationResponse.builder()
                .candidate(candidate)
                .decision(eaasResponse.getDecision())
                .rationale(eaasResponse.getRationale())
                .riskLevel(Double.parseDouble(
                    switch(eaasResponse.getRiskLevel()) {
                        case "HIGH" -> "0.8";
                        case "MEDIUM" -> "0.5";
                        default -> "0.2";
                    }
                ))
                .evaluationId(eaasResponse.getEvaluationId())
                .appliedPolicies(eaasResponse.getAppliedPolicies().toArray(new EaasEvaluationResponse.AppliedPolicy[0]))
                .auditTrail(eaasResponse.getAuditTrail().toArray(new EaasEvaluationResponse.AuditTrail[0]))
                .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error processing ethical recommendation", e);
            return ResponseEntity.status(500).body(
                EthicalRecommendationResponse.builder()
                    .decision("ERROR")
                    .rationale("An error occurred during recommendation evaluation: " + e.getMessage())
                    .build()
            );
        }
    }

}
