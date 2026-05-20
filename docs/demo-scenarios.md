# Demo Scenarios & Test Cases

This document outlines the two required demo scenarios and how to execute them.

## Setup Instructions

Before running the demo scenarios, ensure:

1. **Install Maven** (if not already installed):
   ```bash
   # Ubuntu/Debian
   sudo apt-get install maven

   # macOS
   brew install maven
   ```

2. **Verify Java 17+**:
   ```bash
   java -version
   ```

3. **Navigate to project root**:
   ```bash
   cd /path/to/SOSE
   ```

---

## Building the Project

### Build Both Backends
```bash
# DaaS
cd backend-daas
mvn clean package -DskipTests
cd ..

# EaaS
cd backend-eaas
mvn clean package -DskipTests
cd ..
```

### Setup Frontend
```bash
cd frontend
npm install
cd ..
```

---

## Running the Application

### Terminal 1: Start DaaS Service
```bash
cd backend-daas
java -jar target/backend-daas-1.0.0.jar
```
Expected output:
```
Started DaasApplication in X.XXX seconds
```

### Terminal 2: Start EaaS Service
```bash
cd backend-eaas
java -jar target/backend-eaas-1.0.0.jar
```
Expected output:
```
Started EaasApplication in X.XXX seconds
```

### Terminal 3: Start Frontend
```bash
cd frontend
npm run dev
```
Frontend will be available at `http://localhost:5173`

---

## Demo Scenario 1: PROCEED / REVISE (Positive Path)

**Objective**: Search by city, type and rating, then evaluate the selected place ethically.

### Manual API Test

**Step 1: Search for places by city, type and rating**
```bash
curl -X GET "http://localhost:8080/daas/api/places/search/basic?location=Venice&category=Museum&minRating=4.0"
```

**Expected Result**: Array containing at least:
- Accademia Gallery (4.6 rating)
- other Venice museums matching the threshold

**Step 2: Optional ethical pre-filtering search**
```bash
curl -X GET "http://localhost:8080/daas/api/places/search/ethical?location=Venice&category=Museum&accessibility=WheelchairAccessible&sustainability=Sustainable&minRating=4.0"
```

**Step 3: Request ethical recommendation for Accademia Gallery**
```bash
curl -X POST http://localhost:8080/daas/api/recommendations/ethical-recommendation \
  -H "Content-Type: application/json" \
  -d '{
    "category": "Museum",
    "accessibility": "WheelchairAccessible",
    "sustainability": "Sustainable",
    "minRating": 4.0,
    "userContext": {
      "userId": "demo_user_1",
      "preferences": ["cultural", "accessible", "sustainable"],
      "accessibilityNeeds": ["wheelchair"]
    }
  }'
```

**Expected Response**:
```json
{
  "candidate": {
    "name": "Gallerie dell'Accademia",
    "accessibility": "WheelchairAccessible",
    "rating": 4.6,
    "sustainabilityLevel": "Sustainable"
  },
  "decision": "PROCEED",
  "riskLevel": 0.2,
  "rationale": "...",
  "appliedPolicies": [
    {
      "policyId": "POLICY_ACCESSIBILITY_001",
      "riskAssessment": "LOW"
    },
    {
      "policyId": "POLICY_SUSTAINABILITY_001",
      "riskAssessment": "LOW"
    },
    {
      "policyId": "POLICY_PROVENANCE_001",
      "riskAssessment": "LOW"
    }
  ]
}
```

**Decision**: ✅ **PROCEED** or ⚠️ **REVISE** depending on the selected candidate.
- Accessibility: Place is wheelchair accessible
- Sustainability: Place has sustainable rating
- Provenance: Data from trusted source (LinkedOpenData) and recent

---

## Demo Scenario 2: REJECT / ESCALATE (Negative Path)

**Objective**: Search by city, type and rating, then show that EaaS can reject or escalate a problematic place.

### Manual API Test

**Step 1: Search for places by city, type and rating**
```bash
curl -X GET "http://localhost:8080/daas/api/places/search/basic?location=Venice&category=Restaurant&minRating=4.0"
```

**Step 2: Optional ethical pre-filtering search**
```bash
curl -X GET "http://localhost:8080/daas/api/places/search/ethical?location=Venice&category=Restaurant&accessibility=NotWheelchairAccessible&sustainability=LowSustainability&minRating=4.0"
```

**Step 3: Request evaluation for Café Florian (purposely problematic)**
```bash
curl -X POST http://localhost:8080/daas/api/recommendations/ethical-recommendation \
  -H "Content-Type: application/json" \
  -d '{
    "category": "Restaurant",
    "accessibility": "NotWheelchairAccessible",
    "sustainability": "LowSustainability",
    "minRating": 4.0,
    "userContext": {
      "userId": "demo_user_2",
      "preferences": ["luxury"],
      "accessibilityNeeds": ["wheelchair"]
    }
  }'
```

**Expected Response**:
```json
{
  "candidate": {
    "name": "Café Florian",
    "accessibility": "NotWheelchairAccessible",
    "rating": 4.1,
    "sustainabilityLevel": "LowSustainability",
    "crowdingLevel": "VeryHigh"
  },
  "decision": "REJECT",
  "riskLevel": 0.8,
  "rationale": "Not accessible; recommendation cannot proceed. High over-tourism risk...",
  "appliedPolicies": [
    {
      "policyId": "POLICY_ACCESSIBILITY_001",
      "riskAssessment": "HIGH"
    },
    {
      "policyId": "POLICY_SUSTAINABILITY_001",
      "riskAssessment": "HIGH"
    }
  ],
  "auditTrail": [
    {
      "policyId": "POLICY_ACCESSIBILITY_001",
      "conditionEvaluated": "accessibility equals NotWheelchairAccessible",
      "result": true,
      "riskLevel": "HIGH"
    },
    {
      "policyId": "POLICY_SUSTAINABILITY_001",
      "conditionEvaluated": "crowdingLevel equals VeryHigh",
      "result": true,
      "riskLevel": "HIGH"
    }
  ]
}
```

**Decision**: ❌ **REJECT** or 🔶 **ESCALATE** depending on the policy combination.
- Accessibility: Place is not wheelchair accessible (HIGH risk)
- Sustainability: Very high crowding level (HIGH risk)
- Provenance: Data is recent (LOW risk, not influential)

---

## Frontend Demo Flow

### Scenario 1 through UI:

1. Open `http://localhost:5173`
2. Select filters:
   - City: **Venice**
   - Category: **Museum**
   - Min Rating: **4.0**
3. Click **Search Places**
4. Click on **Accademia Gallery** card
5. Click **🔍 Request Ethical Recommendation**
6. View result: Should show **✅ PROCEED** badge

### Scenario 2 through UI:

1. Use the ethical search section for a restaurant in Venice
2. Request recommendation for Café Florian
3. View result: Should show **❌ REJECT** or **🔶 ESCALATE** badge with explanation

---

## Key Points for Evaluation

**DaaS Multi-Condition Query (Requirement 4)**:
- `/api/places/search/basic` is the primary tourism search used by the client
- `/api/places/search/ethical` is the optional ethical pre-filter section
- `/api/places/search/multi-criteria` remains available for complex demo cases
- Queries combine category, location, accessibility, sustainability, and rating with SPARQL and JSON output

**EaaS Policy Evaluation (Requirement 5)**:
- Loads 3 external JSON policy files at startup
- Each policy has multiple conditions
- Evaluates all conditions independently
- Aggregates risk: MAX() across all policies
- Maps risk to decision: HIGH→REJECT, MEDIUM→REVISE/ESCALATE, LOW→PROCEED
- Ignores caller-supplied risk values (only uses data)
- Returns structured response with audit trail

**Audit Trail**:
- Full trail of which conditions triggered
- Result of each condition evaluation (true/false)
- Risk level for each triggered condition
- Applied policies with their assessments
- Transparent decision derivation

---

## Troubleshooting

### Services fail to start
**Check**:
- Port 8080, 8081, 5173 are not already in use
- Java 17+ is installed
- RDF file exists at `backend-daas/src/main/resources/rdf/tourism.ttl`
- Policy files exist in `backend-eaas/src/main/resources/policies/*.json`

### CORS errors in frontend
- Both services have CORS configured
- Frontend request headers should include `Origin: http://localhost:5173`
- Services should return `Access-Control-Allow-Origin` headers

### SPARQL query errors
- Check RDF ontology namespace prefixes match queries
- Ensure place URIs match expected format: `ex:place_*`
- Review Jena logs for query syntax errors

### Frontend can't connect to backend
- Verify both backend services are running on correct ports
- Check firewall settings
- Verify `API_BASE_URL` in `App.vue` is correct

---

## Evaluation Artifacts

For the final evaluation, ensure the following are present:

✅ **Code** (all backends & frontend)
✅ **RDF Dataset** (`backend-daas/src/main/resources/rdf/tourism.ttl`)
✅ **Policy Files** (3 JSON files in `backend-eaas/src/main/resources/policies/`)
✅ **Documentation** (this file + README.md + endpoints.md)
✅ **Successful Demo Runs** (both scenarios passing)
✅ **Audit Trail Examples** (from both scenarios)

---

## Example Requests to Include in the Report

### Request leading to PROCEED or REVISE
```bash
curl -X POST http://localhost:8080/daas/api/recommendations/ethical-recommendation \
  -H "Content-Type: application/json" \
  -d '{
    "category": "Museum",
    "accessibility": "WheelchairAccessible",
    "sustainability": "Sustainable",
    "minRating": 4.0,
    "userContext": {
      "userId": "user_proceed",
      "preferences": ["cultural", "accessible"],
      "accessibilityNeeds": ["wheelchair"]
    }
  }'
```

### Request leading to ESCALATE or REJECT
```bash
curl -X POST http://localhost:8080/daas/api/recommendations/ethical-recommendation \
  -H "Content-Type: application/json" \
  -d '{
    "category": "Restaurant",
    "accessibility": "NotWheelchairAccessible",
    "sustainability": "LowSustainability",
    "minRating": 4.0,
    "userContext": {
      "userId": "user_reject",
      "preferences": ["luxury"],
      "accessibilityNeeds": ["wheelchair"]
    }
  }'
```

---

## Time Estimates for Demo

- Setup (install deps, build, start services): 5-10 minutes
- Scenario 1 (basic search + PROCEED/REVISE): 3 minutes
- Scenario 2 (problematic place + REJECT/ESCALATE): 3 minutes
- Audit trail walkthrough: 2 minutes
- Q&A and reflection: 5 minutes

**Total estimated demo time**: 15-20 minutes (within 10-12 min/person guideline with discussion)
