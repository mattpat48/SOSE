package com.tourism.client;

import com.tourism.dto.EaasEvaluationRequest;
import com.tourism.dto.EaasEvaluationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class EaasClient {

    private final RestTemplate restTemplate;
    private static final String EAAS_BASE_URL = "http://localhost:8081/eaas";
    private static final String EAAS_EVALUATE_ENDPOINT = EAAS_BASE_URL + "/api/evaluate";

    @Autowired
    public EaasClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public EaasEvaluationResponse evaluatePlace(EaasEvaluationRequest request) {
        try {
            log.info("Calling EaaS evaluation endpoint: {}", EAAS_EVALUATE_ENDPOINT);
            ResponseEntity<EaasEvaluationResponse> response = restTemplate.postForEntity(
                EAAS_EVALUATE_ENDPOINT,
                request,
                EaasEvaluationResponse.class
            );

            if (response.getStatusCode() != HttpStatus.OK) {
                log.error("EaaS returned status: {}", response.getStatusCode());
                throw new RuntimeException("EaaS returned status: " + response.getStatusCode());
            }

            log.info("EaaS evaluation successful, decision: {}", response.getBody().getDecision());
            return response.getBody();
        } catch (RestClientException e) {
            log.error("Failed to communicate with EaaS service", e);
            throw new RuntimeException("Failed to communicate with EaaS: " + e.getMessage(), e);
        }
    }

}
