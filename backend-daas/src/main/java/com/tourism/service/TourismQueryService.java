package com.tourism.service;

import com.tourism.dto.PlaceDTO;
import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Slf4j
@Service
public class TourismQueryService {

    private final Dataset dataset;
    private static final String PREFIX =
        "PREFIX ex: <http://tourism.example.org/> " +
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
        "PREFIX prov: <http://www.w3.org/ns/prov#> " +
        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> ";

    @Autowired
    public TourismQueryService(Dataset dataset) {
        this.dataset = dataset;
        log.info("TourismQueryService initialized with RDF Dataset");
    }

    public List<PlaceDTO> searchPlacesByLocation(String location) {
        log.debug("Searching places by location: {}", location);
        String query = PREFIX +
            "SELECT ?place ?name ?category ?rating ?accessibility ?sustainability ?crowding " +
            "WHERE { " +
            "  ?place a ?type ; " +
            "    rdfs:label ?name ; " +
            "    ex:belongsToLocation ?loc ; " +
            "    ex:hasCategory ?category ; " +
            "    ex:hasRating ?rating ; " +
            "    ex:hasAccessibility ?accessibility ; " +
            "    ex:hasSustainability ?sustainability ; " +
            "    ex:hasCrowdingLevel ?crowding . " +
            "  ?type rdfs:subClassOf* ex:Place . " +
            "  ?loc rdfs:label ?locName . " +
            "  FILTER(regex(?locName, '" + location + "', 'i')) " +
            "} LIMIT 20";

        return executeSparqlQuery(query, this::mapToPlaceDTO);
    }

    public List<PlaceDTO> searchByCategory(String category) {
        log.debug("Searching places by category: {}", category);
        String query = PREFIX +
            "SELECT ?place ?name ?cat ?rating ?accessibility ?sustainability ?crowding " +
            "WHERE { " +
            "  ?place a ?type ; " +
            "    rdfs:label ?name ; " +
            "    ex:hasCategory ?cat ; " +
            "    ex:hasRating ?rating ; " +
            "    ex:hasAccessibility ?accessibility ; " +
            "    ex:hasSustainability ?sustainability ; " +
            "    ex:hasCrowdingLevel ?crowding . " +
            "  ?type rdfs:subClassOf* ex:Place . " +
            "  FILTER(regex(str(?cat), '" + category + "', 'i')) " +
            "} LIMIT 20";

        return executeSparqlQuery(query, this::mapToPlaceDTO);
    }

    public List<PlaceDTO> searchMultiCriteria(String category, String accessibility, 
                                              String sustainability, Double minRating) {
        log.debug("Searching by multi-criteria: category={}, accessibility={}, sustainability={}, minRating={}",
            category, accessibility, sustainability, minRating);

        String query = PREFIX +
            "SELECT ?place ?name ?cat ?rating ?acc ?sust ?crowding " +
            "WHERE { " +
            "  ?place a ?type ; " +
            "    rdfs:label ?name ; " +
            "    ex:hasCategory ?cat ; " +
            "    ex:hasRating ?rating ; " +
            "    ex:hasAccessibility ?acc ; " +
            "    ex:hasSustainability ?sust ; " +
            "    ex:hasCrowdingLevel ?crowding . " +
            "  ?type rdfs:subClassOf* ex:Place . " +
            "  FILTER( " +
            "    regex(str(?cat), '" + category + "', 'i') && " +
            "    regex(str(?acc), '" + accessibility + "', 'i') && " +
            "    regex(str(?sust), '" + sustainability + "', 'i') && " +
            "    ?rating >= " + minRating + " " +
            "  ) " +
            "} ORDER BY DESC(?rating) LIMIT 20";

        return executeSparqlQuery(query, this::mapToPlaceDTO);
    }

    public List<PlaceDTO> searchBasic(String location, String category, Double minRating) {
        log.debug("Searching basic criteria: location={}, category={}, minRating={}",
            location, category, minRating);

        String query = PREFIX +
            "SELECT ?place ?name ?cat ?location ?rating ?acc ?sust ?crowding ?ethical ?provenance ?updated " +
            "WHERE { " +
            "  ?place a ?type ; " +
            "    rdfs:label ?name ; " +
            "    ex:belongsToLocation ?location ; " +
            "    ex:hasCategory ?cat ; " +
            "    ex:hasRating ?rating ; " +
            "    ex:ethicalRating ?ethical ; " +
            "    ex:hasAccessibility ?acc ; " +
            "    ex:hasSustainability ?sust ; " +
            "    ex:hasCrowdingLevel ?crowding ; " +
            "    prov:wasAttributedTo ?provenance ; " +
            "    ex:lastUpdatedOn ?updated . " +
            "  ?type rdfs:subClassOf* ex:Place . " +
            "  ?location rdfs:label ?locLabel . " +
            "  FILTER( " +
            "    regex(str(?locLabel), '" + location + "', 'i') && " +
            "    regex(str(?cat), '" + category + "', 'i') && " +
            "    ?rating >= " + minRating + " " +
            "  ) " +
            "} ORDER BY DESC(?rating) LIMIT 100";

        return executeSparqlQuery(query, this::mapToPlaceDTO);
    }

    public List<PlaceDTO> searchEthical(String location, String category, String accessibility,
                                        String sustainability, Double minRating) {
        log.debug("Searching ethical criteria: location={}, category={}, accessibility={}, sustainability={}, minRating={}",
            location, category, accessibility, sustainability, minRating);

        String query = PREFIX +
            "SELECT ?place ?name ?cat ?location ?rating ?acc ?sust ?crowding ?ethical ?provenance ?updated " +
            "WHERE { " +
            "  ?place a ?type ; " +
            "    rdfs:label ?name ; " +
            "    ex:belongsToLocation ?location ; " +
            "    ex:hasCategory ?cat ; " +
            "    ex:hasRating ?rating ; " +
            "    ex:ethicalRating ?ethical ; " +
            "    ex:hasAccessibility ?acc ; " +
            "    ex:hasSustainability ?sust ; " +
            "    ex:hasCrowdingLevel ?crowding ; " +
            "    prov:wasAttributedTo ?provenance ; " +
            "    ex:lastUpdatedOn ?updated . " +
            "  ?type rdfs:subClassOf* ex:Place . " +
            "  ?location rdfs:label ?locLabel . " +
            "  FILTER( " +
            "    regex(str(?locLabel), '" + location + "', 'i') && " +
            "    regex(str(?cat), '" + category + "', 'i') && " +
            "    regex(str(?acc), '" + accessibility + "', 'i') && " +
            "    regex(str(?sust), '" + sustainability + "', 'i') && " +
            "    ?rating >= " + minRating + " " +
            "  ) " +
            "} ORDER BY DESC(?rating) LIMIT 100";

        return executeSparqlQuery(query, this::mapToPlaceDTO);
    }

    public PlaceDTO getPlaceById(String id) {
        log.debug("Fetching place by ID: {}", id);
        String query = PREFIX +
            "SELECT ?place ?name ?cat ?location ?rating ?acc ?sust ?crowding ?ethical ?provenance ?updated " +
            "WHERE { " +
            "  ?place a ?type ; " +
            "    rdfs:label ?name ; " +
            "    ex:belongsToLocation ?location ; " +
            "    ex:hasCategory ?cat ; " +
            "    ex:hasRating ?rating ; " +
            "    ex:ethicalRating ?ethical ; " +
            "    ex:hasAccessibility ?acc ; " +
            "    ex:hasSustainability ?sust ; " +
            "    ex:hasCrowdingLevel ?crowding ; " +
            "    prov:wasAttributedTo ?provenance ; " +
            "    ex:lastUpdatedOn ?updated . " +
            "  ?type rdfs:subClassOf* ex:Place . " +
            "  FILTER(regex(str(?place), '" + id + "')) " +
            "} LIMIT 1";

        List<PlaceDTO> results = executeSparqlQuery(query, this::mapToPlaceDTO);
        return results.isEmpty() ? null : results.get(0);
    }

    public List<PlaceDTO> listAllPlaces() {
        String query = PREFIX +
            "SELECT ?place ?name ?cat ?location ?rating ?acc ?sust ?crowding ?ethical ?provenance ?updated " +
            "WHERE { " +
            "  ?place a ?type ; " +
            "    rdfs:label ?name ; " +
            "    ex:belongsToLocation ?location ; " +
            "    ex:hasCategory ?cat ; " +
            "    ex:hasRating ?rating ; " +
            "    ex:ethicalRating ?ethical ; " +
            "    ex:hasAccessibility ?acc ; " +
            "    ex:hasSustainability ?sust ; " +
            "    ex:hasCrowdingLevel ?crowding ; " +
            "    prov:wasAttributedTo ?provenance ; " +
            "    ex:lastUpdatedOn ?updated . " +
            "  ?type rdfs:subClassOf* ex:Place . " +
            "} ORDER BY DESC(?rating) LIMIT 100";

        return executeSparqlQuery(query, this::mapToPlaceDTO);
    }

    private <T> List<T> executeSparqlQuery(String queryStr, Function<QuerySolution, T> mapper) {
        List<T> results = new ArrayList<>();

        try (QueryExecution qe = QueryExecutionFactory.create(queryStr, dataset)) {
            ResultSet rs = qe.execSelect();
            while (rs.hasNext()) {
                results.add(mapper.apply(rs.next()));
            }
            log.debug("Query returned {} results", results.size());
        } catch (QueryException e) {
            log.error("SPARQL Query execution failed", e);
            throw e;
        }

        return results;
    }

    private PlaceDTO mapToPlaceDTO(QuerySolution qs) {
        PlaceDTO dto = new PlaceDTO();

        // Mandatory fields
        if (qs.contains("?place")) {
            Resource placeResource = qs.getResource("?place");
            dto.setUri(placeResource.getURI());
        }

        if (qs.contains("?name")) {
            Literal nameLit = qs.getLiteral("?name");
            dto.setName(nameLit.getString());
        }

        if (qs.contains("?rating")) {
            Literal ratingLit = qs.getLiteral("?rating");
            dto.setRating(ratingLit.getDouble());
        }

        if (qs.contains("?cat")) {
            Resource catResource = qs.getResource("?cat");
            dto.setCategory(catResource.getLocalName());
        }

        if (qs.contains("?catLabel")) {
            Literal catLit = qs.getLiteral("?catLabel");
            dto.setCategory(catLit.getString());
        }

        if (qs.contains("?location")) {
            Resource locResource = qs.getResource("?location");
            dto.setLocation(locResource.getLocalName());
        }

        if (qs.contains("?accLabel")) {
            Literal accLit = qs.getLiteral("?accLabel");
            dto.setAccessibility(accLit.getString());
        }

        if (qs.contains("?acc")) {
            Resource accResource = qs.getResource("?acc");
            dto.setAccessibility(accResource.getLocalName());
        }

        if (qs.contains("?crowding")) {
            Resource crowdResource = qs.getResource("?crowding");
            dto.setCrowdingLevel(crowdResource.getLocalName());
        }

        if (qs.contains("?sustLabel")) {
            Literal sustLit = qs.getLiteral("?sustLabel");
            dto.setSustainabilityLevel(sustLit.getString());
        }

        if (qs.contains("?sust")) {
            Resource sustResource = qs.getResource("?sust");
            dto.setSustainabilityLevel(sustResource.getLocalName());
        }

        if (qs.contains("?ethical")) {
            Literal ethicalLit = qs.getLiteral("?ethical");
            dto.setEthicalRating(ethicalLit.getDouble());
        }

        if (qs.contains("?provenance")) {
            Resource provenanceResource = qs.getResource("?provenance");
            dto.setProvenance(provenanceResource.getLocalName());
        }

        if (qs.contains("?updated")) {
            Literal updatedLit = qs.getLiteral("?updated");
            try {
                dto.setLastUpdated(LocalDateTime.parse(
                    updatedLit.getString(),
                    DateTimeFormatter.ISO_DATE_TIME
                ));
            } catch (Exception e) {
                log.warn("Failed to parse datetime: {}", updatedLit.getString());
            }
        }

        return dto;
    }

}
