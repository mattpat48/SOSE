# Tourism DaaS + EaaS Application

A Service-Oriented Application demonstrating Data as a Service and Ethics as a Service concepts in the tourism domain.

## Project Overview

This application implements a tourism recommendation system that integrates two key services:
- **DaaS (Data as a Service)**: Exposes tourism data through REST APIs backed by RDF/SPARQL
- **EaaS (Ethics as a Service)**: Evaluates recommendations against ethical policies for accessibility, sustainability, and data provenance

## Architecture

```
┌─────────────────────────────────┐
│  Frontend (Vue 3 + Vite)        │
│  http://localhost:5173          │
└──────────────┬──────────────────┘
               │
        ┌──────┴──────────────┐
        ↓                     ↓
┌──────────────┐   ┌──────────────────┐
│  DaaS REST   │   │  EaaS Evaluator  │
│  Port: 8080  │   │  Port: 8081      │
└──────────────┘   └──────────────────┘
     ↓                    ↑
┌──────────────────────────┐
│   RDF Dataset (Jena)     │
│   + JSON Policies        │
└──────────────────────────┘
```

## Prerequisites

- Java 17+
- Maven 3.8+
- Node.js 16+ (for frontend)
- npm or yarn

## Project Structure

```
SOSE/
├── backend-daas/           # Data as a Service (Spring Boot + Jena)
│   ├── pom.xml
│   └── src/main/java/com/tourism/
├── backend-eaas/           # Ethics as a Service (Spring Boot)
│   ├── pom.xml
│   └── src/main/java/com/tourism/
├── frontend/               # Vue 3 + Vite Client
│   ├── package.json
│   └── src/
├── dataset/                # RDF Dataset & Documentation
│   └── tourism.ttl
├── docs/                   # Project Documentation
├── README.md               # This file
└── .gitignore
```

## Build & Run Instructions (Individual Components)

### 1. Build Backend Services

#### DaaS Service
```bash
cd backend-daas
mvn clean package
java -jar target/backend-daas-1.0.0.jar
```
DaaS will start on `http://localhost:8080/daas`

#### EaaS Service
```bash
cd backend-eaas
mvn clean package
java -jar target/backend-eaas-1.0.0.jar
```
EaaS will start on `http://localhost:8081/eaas`

### 2. Run Frontend

```bash
cd frontend
npm install
npm run dev
```
Frontend will start on `http://localhost:5173`

### 3. Health Checks

- DaaS: `curl http://localhost:8080/daas/api/places`
- EaaS: `curl http://localhost:8081/eaas/api/evaluate/health`

## Build & Run Instructions (Entire Project)

Start everything with:
```bash
./run-all.sh
```

## API Endpoints

### DaaS Endpoints

- `GET /api/places` - List all places in JSON format
- `GET /api/places/health` - Health check for DaaS
- `GET /api/places/search/location/{location}` - Filter places by location
- `GET /api/places/search/category/{category}` - Filter places by category
- `GET /api/places/{id}` - Get place details
- `GET /api/places/search/basic` - Tourism search by city, category and minimum rating
  - Query params: `location`, `category`, `minRating`
- `GET /api/places/search/ethical` - Optional ethical pre-filtering search
  - Query params: `location`, `category`, `accessibility`, `sustainability`, `minRating`
- `GET /api/places/search/multi-criteria` - Advanced search with multiple filters
  - Query params: `category`, `accessibility`, `sustainability`, `minRating`
- `POST /api/recommendations/ethical-recommendation` - Get ethically evaluated recommendation

### EaaS Endpoints

- `POST /api/evaluate` - Evaluate a place against policies
- `GET /api/evaluate/health` - Health check

## Demo Scenarios

### Scenario 1: PROCEED or REVISE (Positive Recommendation)
**Request:**
```bash
curl -X GET "http://localhost:8080/daas/api/places/search/basic?location=Venice&category=Museum&minRating=4.0"
```
**Result:** Accademia Gallery → DaaS returns candidate places, then EaaS evaluates accessibility/provenance/sustainability → Decision: **PROCEED** or **REVISE** depending on the selected candidate.

### Scenario 2: ESCALATE or REJECT (Ethical Concerns)
**Request:**
```bash
curl -X GET "http://localhost:8080/daas/api/places/search/ethical?location=Venice&category=Restaurant&accessibility=NotWheelchairAccessible&sustainability=LowSustainability&minRating=4.0"
```
**Result:** Café Florian or Rialto Bridge → accessibility and crowding/sustainability concerns → Decision: **ESCALATE** or **REJECT**.

### Example Audit Trace

The EaaS response includes:
- `decision` and `riskLevel`
- `rationale`
- `appliedPolicies`
- `auditTrail`
- `evaluationId`

This satisfies the requirement for a traceable governance decision separate from case analysis.

## Policies

The system evaluates recommendations against three policies:

1. **Accessibility Policy** (`accessibility-policy.json`)
   - Ensures recommendations are accessible to users with mobility needs
   - Decision: REJECT if not accessible, REVISE if partial, PROCEED if full

2. **Sustainability Policy** (`sustainability-policy.json`)
   - Prevents over-tourism and unsustainable practices
   - Decision: ESCALATE if high crowding/low sustainability, REVISE if moderate, PROCEED if low impact

3. **Provenance Policy** (`provenance-policy.json`)
   - Ensures data comes from trustworthy, up-to-date sources
   - Decision: REJECT if unverified, REVISE if outdated, PROCEED if trusted & current

## RDF Dataset

The tourism dataset includes 8 example places in Venice:
- St. Mark's Basilica (Museum, Partially Accessible, Sustainable)
- Gondola Experience (Accessible, Moderate Crowding)
- Lagoon Nature Reserve (Highly Sustainable, Low Crowding)
- Café Florian (Not Accessible, Low Sustainability, Over-touristic)
- Accademia Gallery (Accessible, Sustainable)
- Doge's Palace (Partially Accessible)
- Rialto Bridge (Not Accessible, High Crowding)
- San Giorgio Maggiore (Accessible, Sustainable)

??? See [dataset/README.md](dataset/README.md) for detailed ontology documentation.

## Frontend Features

- **Search Form**: Main search by city, category, and rating, plus an optional ethical filter section
- **Place List**: Browse search results with key attributes
- **Place Detail**: View detailed information and request ethical evaluation
- **Ethical Decision Display**: See evaluation result with rationale, applied policies, and audit trail

## Coverage vs. Homework Requirements

- RDF dataset adapted for tourism domain: yes
- REST endpoints: more than 5 meaningful endpoints: yes
- SPARQL queries, JSON output, separation of concerns: yes
- External JSON policies: 3 policies in `backend-eaas/src/main/resources/policies/`: yes
- Example requests for positive and negative cases: yes
- Audit trace and decision explanation: yes
- Working client showing DaaS + EaaS interaction: yes

## Development Notes

### Technology Stack
- **Backend**: Spring Boot 3.2, Apache Jena 4.10.0, Maven
- **Frontend**: Vue 3, Vite, Axios
- **Data Storage**: In-memory RDF (Jena), JSON policies
- **Database**: H2 (optional for audit logging)

### Extending the System

1. **Add More Places**: Edit `backend-daas/src/main/resources/rdf/tourism.ttl`
2. **Create New Policies**: Add JSON files to `backend-eaas/src/main/resources/policies/`
3. **Customize Frontend**: Modify Vue components in `frontend/src/components/`

## Testing

### Manual Testing with Curl

```bash
# List places
curl http://localhost:8080/daas/api/places

# Basic tourism search by city, type and rating
curl "http://localhost:8080/daas/api/places/search/basic?location=Venice&category=Museum&minRating=4.0"

# Optional ethical pre-filtering search
curl "http://localhost:8080/daas/api/places/search/ethical?location=Venice&category=Museum&accessibility=WheelchairAccessible&sustainability=Sustainable&minRating=4.0"

# Get place details
curl "http://localhost:8080/daas/api/places/place_accademia"

# Request ethical recommendation
curl -X POST http://localhost:8080/daas/api/recommendations/ethical-recommendation \
  -H "Content-Type: application/json" \
  -d '{
    "category": "Museum",
    "accessibility": "WheelchairAccessible",
    "sustainability": "Sustainable",
    "minRating": 4.0,
    "userContext": {
      "userId": "user123",
      "preferences": ["cultural", "accessible"],
      "accessibilityNeeds": ["wheelchair"]
    }
  }'
```

## Documentation

- [API Endpoints Documentation](docs/endpoints.md)
- [Dataset Schema](docs/dataset-schema.md)
- [Demo Scenarios & Examples](docs/demo-scenarios.md)

## Team & Presentation

**Presentation Date:** Thursday, May 28, 2026  
**Expected Duration:** 10-12 minutes per team member

**Presentation Topics:**
- Domain motivation (Tourism + Ethics)
- Architecture overview
- RDF dataset design
- DaaS REST API walkthrough
- EaaS policy engine
- Live demo (both scenarios)
- Audit trail & transparency
- Critical reflection on system limitations

## Known Limitations & Design Decisions

1. **In-Memory RDF Store**: Suitable for demo; use TDB2 for production persistence
2. **Static Policies**: Policies are loaded at startup; runtime reload not implemented
3. **Dataset Size**: Small curated dataset (8 places) chosen for demo clarity
4. **Risk Aggregation**: Uses maximum risk across policies (conservative approach)
5. **No Authentication**: Frontend and services are open; add security for production

## Future Enhancements

- [ ] WebSocket support for real-time recommendations
- [ ] Integration with GIS/mapping services
- [ ] Machine learning for policy optimization
- [ ] Advanced audit trail with temporal queries
- [ ] Multi-language support for policies and UI
- [ ] Caching layer for frequently queried places

## License

Educational project - No license specified

## Contact

For questions or issues, refer to the project scope document and course materials.
