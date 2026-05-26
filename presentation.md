# Tourism Ethics-as-a-Service (EaaS) Presentation
**Estimated Total Duration:** ~30-35 Minutes (Designed for a 3-member group)

---

## 1. Selected Domain and Motivation (Time: 3-4 mins)
**Speaker Notes / Talking Points:**
- **Domain Overview**: We chose the Tourism & Travel Recommendations domain. Tourism is a sector heavily driven by data (ratings, reviews, popularity).
- **The Core Problem**: Standard tourism recommendation algorithms are not neutral. Recommending a place based solely on a high rating (e.g., 4.8 stars) can unintentionally promote:
  - **Over-tourism / Gentrification**: Flooding small, vulnerable areas with tourists.
  - **Exclusion**: Recommending inaccessible places to users with mobility needs, effectively excluding them.
  - **Misinformation**: Recommending places based on outdated or commercially biased, unverified data.
- **The Solution (EaaS)**: By introducing an Ethics-as-a-Service layer, we shift from purely "data-driven" to "value-driven" recommendations. We ensure that our system mathematically promotes sustainability, enforces fairness (accessibility), and maintains transparency regarding data provenance.

---

## 2. Overall Application Architecture (Time: 4-5 mins)
**Speaker Notes / Talking Points:**
- **Separation of Concerns**: We architected the system into three completely decoupled components. This enforces a strict boundary between simply *providing data* and *making ethical governance decisions*.
- **1. The Client (Frontend)**: 
  - A lightweight Vue 3 + Vite Single Page Application using Axios to handle asynchronous HTTP requests to the DaaS.
- **2. DaaS (Data-as-a-Service)**: 
  - Built with Spring Boot 3.2 running on an embedded Tomcat server (Port 8080).
  - Integrates **Apache Jena ARQ** as an embedded in-memory RDF engine. At startup, a configuration bean loads our `.ttl` dataset into an Apache Jena `Dataset` instance in memory. 
  - Uses `@RestController` to expose standard REST APIs, serving mapped Java `PlaceDTO` objects serialized into JSON.
- **3. EaaS (Ethics-as-a-Service)**:
  - An independent Spring Boot policy engine (Port 8081).
  - It holds *no* tourism data. It evaluates raw JSON payloads against external JSON policy files using a custom evaluation engine.
  - It includes Spring Data JPA and H2 boilerplate, which allows the structured audit trails it generates to be persisted to a relational database for compliance and governance tracking.

---

## 3. RDF Dataset Description and Main SPARQL Queries (Time: 4-5 mins)
**Speaker Notes / Talking Points:**
- **Dataset Entities**: We modeled our data in Turtle (`tourism.ttl`), defining resources of type `:Place` in Venice. Key properties include standard data (`:name`, `:category`, `:rating`) combined with ethically relevant metadata (`:accessibilityScore`, `:sustainabilityScore`, `:crowding`, and `dct:source`).
- **Jena Integration**: In the DaaS, the `TourismQueryService` class utilizes Jena's `QueryExecutionFactory` to execute queries. We iterate over the `ResultSet`, taking each `QuerySolution` and mapping RDF `Literal` and `Resource` nodes directly into our `PlaceDTO` Java class.
- **Deep Dive into the SPARQL Query**: 
  Below is our Multi-Criteria Search. Notice how it uses `regex` to filter on string patterns from the semantic graph, binding values to variables like `?acc` and `?sust` which are then evaluated dynamically in the filter clause.

```sparql
PREFIX ex: <http://tourism.example.org/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
SELECT ?place ?name ?cat ?rating ?acc ?sust ?crowding
WHERE {
  ?place a ?type ;
    rdfs:label ?name ;
    ex:hasCategory ?cat ;
    ex:hasRating ?rating ;
    ex:hasAccessibility ?acc ;
    ex:hasSustainability ?sust ;
    ex:hasCrowdingLevel ?crowding .
  ?type rdfs:subClassOf* ex:Place .
  
  FILTER(
    regex(str(?cat), 'Museum', 'i') && 
    ?rating >= 4.0
  )
} ORDER BY DESC(?rating) LIMIT 20
```

---

## 4. DaaS REST APIs Description (Time: 3-4 mins)
**Speaker Notes / Talking Points:**
The DaaS acts as the primary gateway, exposing a full suite of APIs handled by `PlaceController` and `RecommendationController`.
- **All Core Data Endpoints**:
  - `GET /api/places`: Lists all places in the system.
  - `GET /api/places/{id}`: Retrieves details for a specific place using its RDF URI.
  - `GET /api/places/search/location/{location}`: Filters places by city/location.
  - `GET /api/places/search/category/{category}`: Filters places strictly by category.
  - `GET /api/places/search/basic`: A composite search utilizing `location`, `category`, and `minRating`.
  - `GET /api/places/search/ethical`: A pre-filtering endpoint that lets the UI filter out non-ethical places *before* the deep evaluation (e.g., using `accessibility=WheelchairAccessible`).
  - `GET /api/places/search/multi-criteria`: Advanced semantic search with parameters matching multiple fields simultaneously.
  - `GET /api/places/health`: Simple health probe.

**The Orchestrator Endpoint - Technical Deep Dive**:
- `POST /api/recommendations/ethical-recommendation`
  This is the system's bridge. When called, the `RecommendationController` performs three synchronous steps:
  1. **Query DaaS**: It calls `queryService.searchMultiCriteria` passing the user's category, accessibility, and rating preferences. It extracts the top candidate `PlaceDTO` from the Jena RDF graph.
  2. **Orchestrate RPC**: It constructs an `EaasEvaluationRequest`, appending the `PlaceDTO` payload and an optional `UserContext`. Using Spring's `RestTemplate` wrapped in an `EaasClient` component, it executes a synchronous HTTP POST to the EaaS instance running on port 8081.
  3. **Stitch Response**: It catches the `EaasEvaluationResponse`, extracts the ethical `decision`, `riskLevel`, `rationale`, and `auditTrail`, and stitches them together with the original `PlaceDTO` into a composite `EthicalRecommendationResponse` returned to the frontend.

---

## 5. EaaS Workflow & Engine Mechanics (Time: 4-5 mins)
**Speaker Notes / Talking Points:**
- **Zero-Trust Input**: The EaaS does not blindly trust a "risk score" passed by the caller. It requires raw data (e.g., "crowding level is HIGH") and computes the risk itself.
- **The Evaluation Flow**:
  1. **Ingestion**: Receives a structured `EvaluationRequest`.
  2. **Policy Loading**: Loads external JSON policies (meaning policies can be updated without recompiling the Java code).
  3. **Condition Matching**: Iterates over the `conditions` array in the JSON policies, mapping input fields to operators (e.g., `operator: "equals", value: "NotWheelchairAccessible"`).
  4. **Risk Calculation**: Maps matched conditions to a Risk Level (`LOW`, `MEDIUM`, `HIGH`).
  5. **Aggregation**: If multiple policies trigger, the engine uses a *conservative aggregation* strategy (max risk). If one policy says `PROCEED` and another says `REJECT`, the final decision is `REJECT`.

---

## 6. Policies Defined by the Group (Time: 4 mins)
**Speaker Notes / Talking Points:**
We defined three core domains of ethical concern using structured JSON files:

1. **Accessibility Fairness Policy (`accessibility-policy.json`)**:
   - *Logic*: If `accessibility == NotWheelchairAccessible`, risk is `HIGH` $\rightarrow$ `REJECT`. If `PartiallyWheelchairAccessible`, risk is `MEDIUM` $\rightarrow$ `REVISE`.
2. **Sustainability & Over-Tourism Policy (`sustainability-policy.json`)**:
   - *Logic*: If `crowdingLevel == VeryHigh` or `hasSustainability == LowSustainability`, risk is `HIGH` $\rightarrow$ `ESCALATE` (requires human review).
3. **Data Provenance & Transparency Policy (`provenance-policy.json`)**:
   - Evaluates exactly where the dataset originated to prevent relying on biased or commercially skewed data. 
   - *How it defines sources*: The JSON policy enforces a strict `operator: "equals"` check. If `provenance == "UnverifiedSource"`, it is immediately flagged as `HIGH` risk $\rightarrow$ `REJECT`. Conversely, authoritative sources are defined via an explicit `operator: "in"` array constraint containing strictly: `["LinkedOpenData", "CulturalHeritage", "TouristBoard", "ConservationOrg"]`.
   - *Data Age check*: The policy also checks temporal validity; if `dataAgeSinceUpdate > 180` days, the engine flags a `MEDIUM` risk demanding the data be verified.

---

## 7. Example Request Evaluated by EaaS (Time: 2-3 mins)
**Speaker Notes / Talking Points:**
Let's look under the hood at an actual payload for a problematic place, like "Café Florian".

**DaaS sends to EaaS:**
```json
{
  "placeId": "http://example.org/tourism/place_cafe_florian",
  "accessibility": "NotWheelchairAccessible",
  "crowdingLevel": "High",
  "provenance": "commercial"
}
```

**EaaS Evaluates:**
- Accessibility Policy reads `"NotWheelchairAccessible"` $\rightarrow$ **HIGH Risk** (`REJECT`).
- Sustainability Policy reads `"High"` crowding $\rightarrow$ **MEDIUM Risk** (`REVISE`).
- *Aggregation*: Engine takes the maximum risk.
- **Final Decision**: `REJECT`.

---

## 8. Audit and Provenance of the Decision (Time: 2-3 mins)
**Speaker Notes / Talking Points:**
- **Why Auditing matters?**: In automated decision systems, users (and regulators) need to know *why* a decision was made. If an algorithm denies a user a recommendation, it must provide a transparent, legally sound trace.
- **Separation of Case Analysis vs. Governance**: 
  - *Case Analysis*: The DaaS stating a raw fact: "this cafe has stairs".
  - *Governance*: The EaaS appending an ethical judgment: "Place is not wheelchair accessible; recommendation cannot proceed for users with mobility needs."
- **The Trace Mechanism**: Our engine returns an `evaluationId` paired with an `auditTrail` array. The array explicitly lists the exact fields evaluated (`accessibility`, `crowdingLevel`) and the triggered thresholds.
- **Persistence**: Because EaaS includes Spring Data JPA, every evaluation payload and resulting decision can be serialized and logged to a database (H2), guaranteeing historical non-repudiation for civic boards monitoring algorithm bias.

---

## 9. Live Demo (Time: 4-5 mins)
**Speaker Notes / Talking Points:**
*(Walk the audience through these steps live)*

1. **Service Initialization**:
   - Show terminals booting up the EaaS (`mvn spring-boot:run` on 8081).
   - Show terminal booting up the DaaS (`mvn spring-boot:run` on 8080).
   - Show Vite frontend running (`npm run dev` on 5173).
2. **Scenario 1: The "Happy Path" (PROCEED)**: 
   - Search for "Museum" in "Venice".
   - Pick "Accademia Gallery" (Accessible, Sustainable).
   - Trigger the ethical evaluation. 
   - *Show the audience*: The green `PROCEED` badge and read out the positive audit trail.
3. **Scenario 2: The "Ethical Block" (REJECT/ESCALATE)**:
   - Search for "Restaurant" in "Venice".
   - Pick "Café Florian".
   - Trigger the ethical evaluation. 
   - *Show the audience*: The red `REJECT/ESCALATE` badge. Highlight the exact policy explanation that blocked the recommendation.

---

## 10. Short Critical Reflection & Limitations (Time: 2-3 mins)
**Speaker Notes / Talking Points:**
- **System Limitations**: 
  - Our risk aggregation uses a strict `MAX(risk)` function. This is conservative and safe, but inflexible. In the real world, risks might be weighted (e.g., severe crowding vs minor accessibility issues).
  - Our policies are static JSON files. Ideally, we would integrate a dynamic, temporal rule engine (like Drools) that adjusts policies based on the time of day (e.g., crowding is only an issue at 2 PM, not 9 AM).
- **What should NOT be decided automatically?**:
  - We must not fully automate complex socio-cultural trade-offs. For example, a historically significant building may physically be unable to be made wheelchair accessible without destroying its heritage. A blunt algorithm simply rejects it.
  - Cases like this represent a "clash of ethical values" (Heritage vs. Accessibility). These instances should not be decided by an IF-statement. The system should yield an `ESCALATE` status, passing the audit trail to a human curator or civic board to make a nuanced, qualitative judgment.
