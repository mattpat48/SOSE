package com.tourism.controller;

import com.tourism.dto.EaasEvaluationRequest;
import com.tourism.dto.EaasEvaluationResponse;
import com.tourism.exception.PolicyLoadingException;
import com.tourism.service.PolicyEvaluationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/evaluate")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000", "http://localhost:8080"})
public class EvaluationController {

    private final PolicyEvaluationService evaluationService;

    @Autowired
    public EvaluationController(PolicyEvaluationService evaluationService) {
        this.evaluationService = evaluationService;
    }

    @PostMapping
    public ResponseEntity<EaasEvaluationResponse> evaluate(
            @RequestBody EaasEvaluationRequest request) {

        log.info("Evaluating place: {}", request.getPlaceData().getName());

        try {
            EaasEvaluationResponse response = evaluationService.evaluateAgainstPolicies(request);
            return ResponseEntity.ok(response);
        } catch (PolicyLoadingException e) {
            log.error("Policy loading failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                EaasEvaluationResponse.builder()
                    .decision("ERROR")
                    .rationale("Policy loading failed: " + e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            log.error("Unexpected error during evaluation", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                EaasEvaluationResponse.builder()
                    .decision("ERROR")
                    .rationale("Evaluation failed: " + e.getMessage())
                    .build()
            );
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        log.info("Health check on /api/evaluate/health");
        return ResponseEntity.ok("{\"status\": \"EaaS is running\"}");
    }

}
