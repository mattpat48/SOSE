# Tourism RDF Dataset Schema

## Ontology Overview

This document describes the RDF vocabulary and ontology used in the Tourism DaaS system.

---

## Namespace Declarations

```turtle
@prefix ex: <http://tourism.example.org/>
@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#>
@prefix owl: <http://www.w3.org/2002/07/owl#>
@prefix prov: <http://www.w3.org/ns/prov#>
@prefix xsd: <http://www.w3.org/2001/XMLSchema#>
```

---

## Core Classes

### Place Classes

#### ex:Place (Root Class)
Base class for all tourist attractions.
- **Type**: `owl:Class`
- **Description**: A location that can be visited or is of interest to tourists
- **Properties**:
  - `rdfs:label` (required): Human-readable name
  - `rdfs:comment` (optional): Description
  - `ex:hasCategory`: Classification (Museum, Restaurant, NaturalSite, Landmark)
  - `ex:belongsToLocation`: Geographic location
  - `ex:hasAccessibility`: Accessibility level
  - `ex:hasSustainability`: Sustainability rating
  - `ex:hasCrowdingLevel`: Tourist crowding level
  - `ex:hasRating`: User rating (0-5)
  - `ex:ethicalRating`: Ethical score (0-5)
  - `ex:lastUpdatedOn`: Data timestamp
  - `prov:wasAttributedTo`: Data provenance

#### ex:Museum
**Subclass of**: `ex:Place`
Example: Accademia Gallery, Doge's Palace

#### ex:Restaurant
**Subclass of**: `ex:Place`
Example: Café Florian

#### ex:NaturalSite
**Subclass of**: `ex:Place`
Example: Lagoon Nature Reserve

#### ex:Landmark
**Subclass of**: `ex:Place`
Example: Gondola Experience, Rialto Bridge

#### ex:Location (Geographic Class)
Represents geographic regions.
- **Type**: `owl:Class`
- **Examples**: Venice, Rome, Florence

---

## Properties

### Object Properties

#### ex:hasCategory
- **Domain**: `ex:Place`
- **Range**: Individual resources (Museum, Restaurant, etc.)
- **Description**: Assigns a category type to a place
- **Example**: `place_stmarks ex:hasCategory ex:Museum`

#### ex:belongsToLocation
- **Domain**: `ex:Place`
- **Range**: `ex:Location`
- **Description**: Associates a place with a geographic location
- **Example**: `place_stmarks ex:belongsToLocation ex:Venice`

#### ex:hasAccessibility
- **Domain**: `ex:Place`
- **Range**: Accessibility level resources
- **Description**: Indicates accessibility features
- **Possible Values**:
  - `ex:WheelchairAccessible` - Full wheelchair access
  - `ex:PartiallyWheelchairAccessible` - Limited wheelchair access
  - `ex:NotWheelchairAccessible` - No wheelchair access

#### ex:hasSustainability
- **Domain**: `ex:Place`
- **Range**: Sustainability rating resources
- **Description**: Indicates sustainability practices
- **Possible Values**:
  - `ex:HighlySustainable` - Strong sustainability commitment
  - `ex:Sustainable` - Good sustainability practices
  - `ex:ModeratelySustainable` - Some sustainability measures
  - `ex:LowSustainability` - Minimal sustainability

#### ex:hasCrowdingLevel
- **Domain**: `ex:Place`
- **Range**: Crowding level resources
- **Description**: Indicates typical tourist density
- **Possible Values**:
  - `ex:Low` - Few tourists
  - `ex:Medium` - Moderate foot traffic
  - `ex:High` - Many tourists
  - `ex:VeryHigh` - Extremely crowded (over-tourism)

---

### Datatype Properties

#### ex:hasRating
- **Domain**: `ex:Place`
- **Range**: `xsd:double`
- **Description**: User rating on scale 0-5
- **Example**: `place_stmarks ex:hasRating 4.8`

#### ex:ethicalRating
- **Domain**: `ex:Place`
- **Range**: `xsd:double`
- **Description**: System-computed ethical score 0-5
- **Example**: `place_stmarks ex:ethicalRating 4.2`

#### ex:lastUpdatedOn
- **Domain**: `ex:Place`
- **Range**: `xsd:dateTime`
- **Format**: ISO 8601 format
- **Example**: `place_stmarks ex:lastUpdatedOn "2024-05-15T10:30:00Z"^^xsd:dateTime`

---

### Provenance Properties

#### prov:wasAttributedTo
- **Domain**: `ex:Place`
- **Range**: Provenance source resources
- **Description**: Indicates data source/origin for transparency
- **Possible Values**:
  - `ex:LinkedOpenData` - Open data sources
  - `ex:TouristBoard` - Official tourism authorities
  - `ex:ConservationOrg` - Environmental/cultural organizations
  - `ex:CulturalHeritage` - Heritage databases
  - `ex:CommunityReview` - User-generated content
  - `ex:UnverifiedSource` - Unconfirmed sources (low trust)

---

## Dataset Statistics

### Venues in Dataset
| Place | Type | Location | Status |
|-------|------|----------|--------|
| St. Mark's Basilica | Museum | Venice | Partially Accessible, Sustainable |
| Gondola Experience | Landmark | Venice | Fully Accessible, Moderate |
| Lagoon Reserve | Nature Site | Venice | Fully Accessible, Highly Sustainable |
| Café Florian | Restaurant | Venice | Not Accessible, Low Sustainability |
| Accademia Gallery | Museum | Venice | Fully Accessible, Sustainable |
| Doge's Palace | Landmark | Venice | Partially Accessible, Sustainable |
| Rialto Bridge | Landmark | Venice | Not Accessible, Low Sustainability |
| San Giorgio Maggiore | Landmark | Venice | Fully Accessible, Highly Sustainable |

### Accessibility Distribution
- Fully Accessible: 4 places (50%)
- Partially Accessible: 2 places (25%)
- Not Accessible: 2 places (25%)

### Sustainability Distribution
- Highly Sustainable: 2 places (25%)
- Sustainable: 3 places (37.5%)
- Moderately Sustainable: 1 place (12.5%)
- Low Sustainability: 2 places (25%)

---

## Example Triples

### Complete example for one place (Accademia Gallery):

```turtle
ex:place_accademia
    a ex:Museum ;
    rdfs:label "Gallerie dell'Accademia" ;
    rdfs:comment "Major art museum housing Venetian Renaissance works and masterpieces" ;
    ex:hasCategory ex:Museum ;
    ex:belongsToLocation ex:Venice ;
    ex:hasAccessibility ex:WheelchairAccessible ;
    ex:hasSustainability ex:Sustainable ;
    ex:hasCrowdingLevel ex:Medium ;
    ex:hasRating 4.6 ;
    ex:ethicalRating 4.1 ;
    prov:wasAttributedTo ex:CulturalHeritage ;
    ex:lastUpdatedOn "2024-05-13T08:00:00Z"^^xsd:dateTime .
```

---

## SPARQL Query Patterns

### Pattern 1: Find all places in a location
```sparql
PREFIX ex: <http://tourism.example.org/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

SELECT ?place ?name ?category
WHERE {
    ?place a ex:Place ;
           rdfs:label ?name ;
           ex:hasCategory ?category ;
           ex:belongsToLocation ex:Venice .
}
```

### Pattern 2: Find accessible places
```sparql
PREFIX ex: <http://tourism.example.org/>

SELECT ?place ?name ?accessibility
WHERE {
    ?place ex:hasAccessibility ex:WheelchairAccessible ;
           rdfs:label ?name .
}
```

### Pattern 3: Multi-condition query (accessibility + sustainability + rating)
```sparql
PREFIX ex: <http://tourism.example.org/>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>

SELECT ?place ?name ?rating
WHERE {
    ?place a ex:Place ;
           rdfs:label ?name ;
           ex:hasRating ?rating ;
           ex:hasAccessibility ex:WheelchairAccessible ;
           ex:hasSustainability ?sust ;
           ex:hasCategory ex:Museum .
    FILTER(?sust IN (ex:Sustainable, ex:HighlySustainable) &&
           ?rating > 4.0)
} 
ORDER BY DESC(?rating)
```

### Pattern 4: Data freshness and provenance
```sparql
PREFIX ex: <http://tourism.example.org/>
PREFIX prov: <http://www.w3.org/ns/prov#>

SELECT ?place ?name ?source ?updated
WHERE {
    ?place rdfs:label ?name ;
           prov:wasAttributedTo ?source ;
           ex:lastUpdatedOn ?updated .
    FILTER(?source IN (ex:LinkedOpenData, ex:TouristBoard))
}
ORDER BY DESC(?updated)
```

---

## Design Decisions

### 1. Why Semantic Web (RDF)?
- Enables SPARQL queries for flexible filtering
- Supports linked data and semantic meaning
- Easy to extend with new properties or classes
- Promotes data interoperability

### 2. Why Include Ethics Properties?
- `ex:ethicalRating`: Allows pre-computation and storage of ethical scores
- `prov:wasAttributedTo`: Critical for transparency and bias detection
- `ex:lastUpdatedOn`: Essential for data freshness policy evaluation

### 3. Why Fixed Crowding Levels?
- Categorical (Low, Medium, High, VeryHigh) rather than numeric
- Easier to define thresholds in policies
- Matches qualitative descriptions in tourism data

### 4. Why Include Provenance?
- SOSE domain requires transparency
- Enables policy evaluation of data source trustworthiness
- Supports audit trail for decisions

---

## Extensibility

### Adding New Properties
```turtle
ex:wheelchairRestRooms a owl:DatatypeProperty ;
    rdfs:domain ex:Place ;
    rdfs:range xsd:boolean ;
    rdfs:label "Has wheelchair accessible rest rooms" .
```

### Adding New Categories
```turtle
ex:ArchaeologicalSite a owl:Class ;
    rdfs:subClassOf ex:Place .
```

### Adding New Locations
```turtle
ex:Rome a ex:Location ;
    rdfs:label "Rome" ;
    rdfs:comment "Capital city of Italy" .
```

---

## Data Validation Rules

- All places must have: `rdfs:label`, `ex:hasCategory`, `ex:belongsToLocation`
- Rating values: 0.0 - 5.0 (double)
- Ethical rating: 0.0 - 5.0 (double)
- Dates in ISO 8601 format with timezone
- Accessibility, Sustainability, Crowding must be IRIs to defined individuals
- Provenance source must be from controlled vocabulary

---

## Linked Datasets

To enhance the dataset, consider linking to:
- [DBpedia](https://www.dbpedia.org/) - for place descriptions
- [GeoNames](https://www.geonames.org/) - for geographic data
- [Wikidata](https://www.wikidata.org/) - for structured cultural data

Example extended query with DBpedia:
```sparql
PREFIX ex: <http://tourism.example.org/>
PREFIX dbo: <http://dbpedia.org/ontology/>
PREFIX dbr: <http://dbpedia.org/resource/>

SELECT ?place ?name ?dbpediaPage
WHERE {
    ?place rdfs:label ?name ;
           owl:sameAs ?dbpediaPage .
    ?dbpediaPage a dbo:Place .
}
```
