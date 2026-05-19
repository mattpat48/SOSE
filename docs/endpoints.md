# REST API Endpoints Documentation

## DaaS (Data as a Service) - Port 8080

### Base URL
```
http://localhost:8080/daas/api
```

### Endpoints

#### 1. Search Places by Location
```
GET /places/search/location/{location}
```
**Description**: Filter places by geographic location

**Parameters**:
- `location` (path, required): Location name (e.g., "Venice")

**Response**:
```json
[
  {
    "uri": "http://tourism.example.org/place_gondola",
    "name": "Traditional Gondola Experience",
    "category": "Landmark",
    "location": "Venice",
    "accessibility": "WheelchairAccessible",
    "rating": 4.5,
    "sustainabilityLevel": "ModeratelySustainable",
    "crowdingLevel": "Medium",
    "ethicalRating": 3.7,
    "provenance": "TouristBoard",
    "lastUpdated": "2024-05-10T14:20:00"
  }
]
```

#### 2. Search Places by Category
```
GET /places/search/category/{category}
```
**Description**: Filter places by type/category

**Parameters**:
- `category` (path, required): Category (Museum, Restaurant, NaturalSite, Landmark, Church)

**Response**: Array of PlaceDTO objects

#### 3. Get Place Details
```
GET /places/{id}
```
**Description**: Retrieve detailed information for a specific place

**Parameters**:
- `id` (path, required): Place URI identifier (e.g., "place_accademia")

**Response**: Single PlaceDTO object with all fields populated

#### 4. Multi-Criteria Search (Complex Query)
```
GET /places/search/multi-criteria
```
**Description**: Advanced search combining multiple filters using SPARQL

**Query Parameters** (optional):
- `category` (string, default: "Museum"): Place category
- `accessibility` (string, default: "WheelchairAccessible"): WheelchairAccessible, PartiallyWheelchairAccessible, NotWheelchairAccessible
- `sustainability` (string, default: "Sustainable"): HighlySustainable, Sustainable, ModeratelySustainable, LowSustainability
- `minRating` (number, default: 4.0): Minimum rating threshold (0-5)

**Example**:
```
GET /places/search/multi-criteria?category=Museum&accessibility=WheelchairAccessible&sustainability=Sustainable&minRating=4.0
```

**Response**: Array of PlaceDTO objects matching all criteria

#### 5. Get Ethical Recommendation (Integration Endpoint)
```
POST /recommendations/ethical-recommendation
```
**Description**: Request an ethically evaluated recommendation for a place

**Request Body**:
```json
{
  "category": "Museum",
  "accessibility": "WheelchairAccessible",
  "sustainability": "Sustainable",
  "minRating": 4.0,
  "userContext": {
    "userId": "user_123",
    "preferences": ["cultural", "accessible", "sustainable"],
    "accessibilityNeeds": ["wheelchair", "hearingLoop"]
  }
}
```

**Response**:
```json
{
  "candidate": {
    "uri": "http://tourism.example.org/place_accademia",
    "name": "Gallerie dell'Accademia",
    "category": "Museum",
    "location": "Venice",
    "accessibility": "WheelchairAccessible",
    "rating": 4.6,
    "sustainabilityLevel": "Sustainable",
    "ethicalRating": 4.1
  },
  "decision": "PROCEED",
  "rationale": "Place meets accessibility requirements; sustainable option",
  "riskLevel": 0.2,
  "evaluationId": "audit_abc123",
  "appliedPolicies": [
    {
      "policyId": "POLICY_ACCESSIBILITY_001",
      "policyName": "Accessibility Fairness Policy",
      "domain": "accessibility",
      "riskAssessment": "LOW"
    }
  ],
  "auditTrail": [
    {
      "policyId": "POLICY_ACCESSIBILITY_001",
      "conditionEvaluated": "accessibility equals WheelchairAccessible",
      "result": true,
      "riskLevel": "LOW"
    }
  ]
}
```

---

## EaaS (Ethics as a Service) - Port 8081

### Base URL
```
http://localhost:8081/eaas/api
```

### Endpoints

#### 1. Evaluate Place Against Policies
```
POST /evaluate
```
**Description**: Evaluate a place against all loaded ethical policies

**Request Body**:
```json
{
  "placeData": {
    "name": "St. Mark's Basilica",
    "accessibility": "PartiallyWheelchairAccessible",
    "crowdingLevel": "High",
    "sustainabilityLevel": "Sustainable",
    "ethicalRating": 4.2,
    "provenance": "LinkedOpenData",
    "lastUpdated": "2024-05-15T10:30:00"
  },
  "userContext": {
    "userId": "user_123",
    "preferences": ["cultural"],
    "accessibilityNeeds": ["wheelchair"]
  }
}
```

**Response**:
```json
{
  "evaluationId": "eval_xyz789",
  "timestamp": "2024-05-19T15:45:00",
  "placeName": "St. Mark's Basilica",
  "decision": "REVISE",
  "riskLevel": "MEDIUM",
  "rationale": "Limited accessibility; suggest accessible alternative. Moderate crowding; recommend lower-impact time slot.",
  "appliedPolicies": [
    {
      "policyId": "POLICY_ACCESSIBILITY_001",
      "policyName": "Accessibility Fairness Policy",
      "domain": "accessibility",
      "riskAssessment": "MEDIUM"
    },
    {
      "policyId": "POLICY_SUSTAINABILITY_001",
      "policyName": "Sustainability & Over-Tourism Policy",
      "domain": "sustainability",
      "riskAssessment": "MEDIUM"
    },
    {
      "policyId": "POLICY_PROVENANCE_001",
      "policyName": "Data Provenance & Transparency Policy",
      "domain": "transparency",
      "riskAssessment": "LOW"
    }
  ],
  "auditTrail": [
    {
      "policyId": "POLICY_ACCESSIBILITY_001",
      "conditionEvaluated": "accessibility equals PartiallyWheelchairAccessible",
      "result": true,
      "riskLevel": "MEDIUM"
    },
    {
      "policyId": "POLICY_SUSTAINABILITY_001",
      "conditionEvaluated": "crowdingLevel equals High",
      "result": true,
      "riskLevel": "MEDIUM"
    }
  ]
}
```

#### 2. Health Check
```
GET /evaluate/health
```
**Description**: Check if EaaS service is running

**Response**:
```json
{
  "status": "EaaS is running"
}
```

---

## Decision Values

### Decision Outcomes
- **PROCEED**: Recommendation is ethical and meets all criteria
- **REVISE**: Recommendation has minor concerns that can be mitigated
- **ESCALATE**: Recommendation has significant concerns requiring manual review
- **REJECT**: Recommendation fails ethical criteria and cannot proceed

### Risk Levels
- **LOW**: Policy passes without concerns
- **MEDIUM**: Policy identifies moderate concerns
- **HIGH**: Policy identifies severe concerns

---

## Error Responses

### DaaS Error Response
```json
{
  "message": "SPARQL Query Execution Failed",
  "status": 400,
  "detail": "Invalid SPARQL syntax",
  "timestamp": "2024-05-19T15:45:00"
}
```

### EaaS Error Response
```json
{
  "evaluationId": null,
  "decision": "ERROR",
  "rationale": "Policy loading failed: File not found",
  "timestamp": "2024-05-19T15:45:00"
}
```

---

## Common Query Examples

### Find all accessible museums with sustainable rating >= 4.0
```bash
curl "http://localhost:8080/daas/api/places/search/multi-criteria?category=Museum&accessibility=WheelchairAccessible&sustainability=Sustainable&minRating=4.0"
```

### Get recommendation for accessible, sustainable museum
```bash
curl -X POST http://localhost:8080/daas/api/recommendations/ethical-recommendation \
  -H "Content-Type: application/json" \
  -d '{
    "category": "Museum",
    "accessibility": "WheelchairAccessible",
    "sustainability": "HighlySustainable",
    "minRating": 4.0,
    "userContext": {
      "userId": "user_demo",
      "preferences": ["cultural", "sustainable"],
      "accessibilityNeeds": ["wheelchair"]
    }
  }'
```

### Directly evaluate a place through EaaS
```bash
curl -X POST http://localhost:8081/eaas/api/evaluate \
  -H "Content-Type: application/json" \
  -d '{
    "placeData": {
      "name": "Test Place",
      "accessibility": "NotWheelchairAccessible",
      "crowdingLevel": "VeryHigh",
      "sustainabilityLevel": "LowSustainability",
      "provenance": "UserGenerated",
      "lastUpdated": "2023-01-01T00:00:00"
    }
  }'
```

Expected decision: **REJECT** (Accessibility HIGH, Sustainability HIGH, Provenance HIGH)
