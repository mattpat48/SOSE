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

## Build & Run Instructions

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

## Run and Test

Questa sezione fornisce i comandi rapidi e le verifiche per avviare e testare l'intero sistema.

Prerequisiti:
- Java JDK 17+ (`java -version`)
- Maven 3.8+ (`mvn -v`) — su Debian/Ubuntu: `sudo apt install maven`
- Node.js 16+ e `npm` o `yarn` (`node -v`, `npm -v`)

Ordine di avvio consigliato:
1. Avvia prima l'EaaS (motore di policy) su porta 8081.
2. Avvia il DaaS su porta 8080 (fa richieste all'EaaS).
3. Avvia il frontend (Vite) su 5173.

Comandi rapidi (esegui nelle rispettive cartelle):
```bash
# 1) Backend EaaS
cd backend-eaas
mvn clean package
# in sviluppo: mvn spring-boot:run
java -jar target/backend-eaas-0.0.1-SNAPSHOT.jar

# 2) Backend DaaS
cd ../backend-daas
mvn clean package
# in sviluppo: mvn spring-boot:run
java -jar target/backend-daas-0.0.1-SNAPSHOT.jar

# 3) Frontend (dev)
cd ../frontend
npm install
npm run dev
```

Verifiche rapide (curl):
```bash
# Lista posti (DaaS)
curl http://localhost:8080/daas/api/places

# Ricerca per categoria
curl "http://localhost:8080/daas/api/places/search/category/Museum"

# Richiesta valutazione etica (DaaS orchestration -> EaaS)
curl -X POST http://localhost:8080/daas/api/recommendations/ethical-recommendation \
  -H "Content-Type: application/json" \
  -d '{"placeId":"http://example.org/places/1"}'
```

Note di debug:
- Se `mvn` non è trovato, installalo localmente o apri i progetti in IntelliJ/VSCode e usa la run configuration Maven.
- Se DaaS segnala errori di connessione verso EaaS, assicurati che EaaS sia avviato sulla porta corretta prima di lanciare DaaS.
- Per cambiare la porta all'avvio: `java -jar target/...jar --server.port=8082` o con Maven: `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8082`.

Suggerimento: se vuoi, posso aggiungere uno script `run-all.sh` che avvia i tre componenti in modo coordinato.

## API Endpoints

### DaaS Endpoints

- `GET /api/places/search/location/{location}` - Filter places by location
- `GET /api/places/search/category/{category}` - Filter places by category
- `GET /api/places/{id}` - Get place details
- `GET /api/places/search/multi-criteria` - Advanced search with multiple filters
  - Query params: `category`, `accessibility`, `sustainability`, `minRating`
- `POST /api/recommendations/ethical-recommendation` - Get ethically evaluated recommendation

### EaaS Endpoints

- `POST /api/evaluate` - Evaluate a place against policies
- `GET /api/evaluate/health` - Health check

## Demo Scenarios

### Scenario 1: PROCEED (Positive Recommendation)
**Request:**
```bash
curl -X GET "http://localhost:8080/daas/api/places/search/multi-criteria?category=Museum&accessibility=WheelchairAccessible&sustainability=Sustainable&minRating=4.0"
```
**Result:** Accademia Gallery → All policies PASS → Decision: **PROCEED**

### Scenario 2: REJECT (Ethical Concerns)
**Request:**
```bash
curl -X GET "http://localhost:8080/daas/api/places/search/multi-criteria?category=Restaurant&accessibility=NotWheelchairAccessible&sustainability=LowSustainability&minRating=4.0"
```
**Result:** Café Florian → Accessibility & Sustainability policies FAIL → Decision: **REJECT**

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

See [dataset/README.md](dataset/README.md) for detailed ontology documentation.

## Frontend Features

- **Search Form**: Filter places by category, accessibility, sustainability, and rating
- **Place List**: Browse search results with key attributes
- **Place Detail**: View detailed information and request ethical evaluation
- **Ethical Decision Display**: See evaluation result with rationale, applied policies, and audit trail

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

# Search by category
curl "http://localhost:8080/daas/api/places/search/category/Museum"

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
