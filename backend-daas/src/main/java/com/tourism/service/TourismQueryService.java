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
        "PREFIX prov: <http://www.w3.org/ns/prov#> ";

    @Autowired
    public TourismQueryService(Dataset dataset) {
        this.dataset = dataset;
        log.info("TourismQueryService initialized with RDF Dataset");
    }

    public List<PlaceDTO> searchPlacesByLocation(String location) {
        log.debug("Searching places by location: {}", location);
        String query = PREFIX +
            "SELECT ?place ?name ?category ?accessibility ?rating ?crowding ?sustainability ?provenance ?updated " +
            "WHERE { " +
            "  ?place a ex:Place ; rdfs:label ?name ; " +
            "    ex:hasCategory ?category ; " +
            "    ex:hasAccessibility ?accessibility ; " +
            "    ex:hasCrowdingLevel ?crowding ; " +
            "    ex:hasSustainability ?sustainability ; " +
            "    ex:hasRating ?rating ; " +
            "    prov:wasAttributedTo ?provenance ; " +
            "    ex:lastUpdatedOn ?updated ; " +
            "    ex:belongsToLocation ?loc . " +
            "  FILTER(regex(str(?loc), '" + location + "', 'i')) " +
            "} LIMIT 20";

        return executeSparqlQuery(query, this::mapToPlaceDTO);
    }

    public List<PlaceDTO> searchByCategory(String category) {
        log.debug("Searching places by category: {}", category);
        String query = PREFIX +
            "SELECT ?place ?name ?category ?accessibility ?rating ?crowding ?sustainability ?provenance ?updated " +
            "WHERE { " +
            "  ?place a ex:Place ; rdfs:label ?name ; " +
            "    ex:hasCategory ?category ; " +
            "    ex:hasAccessibility ?accessibility ; " +
            "    ex:hasCrowdingLevel ?crowding ; " +
            "    ex:hasSustainability ?sustainability ; " +
            "    ex:hasRating ?rating ; " +
            "    prov:wasAttributedTo ?provenance ; " +
            "    ex:lastUpdatedOn ?updated . " +
            "  FILTER(regex(str(?category), '" + category + "', 'i')) " +
            "} LIMIT 20";

        return executeSparqlQuery(query, this::mapToPlaceDTO);
    }

    public List<PlaceDTO> searchMultiCriteria(String category, String accessibility, 
                                              String sustainability, Double minRating) {
        log.debug("Searching by multi-criteria: category={}, accessibility={}, sustainability={}, minRating={}",
            category, accessibility, sustainability, minRating);

        String query = PREFIX +
            "SELECT ?place ?name ?category ?accessibility ?rating ?crowding ?sustainability ?provenance ?updated " +
            "WHERE { " +
            "  ?place a ex:Place ; rdfs:label ?name ; " +
            "    ex:hasCategory ?cat ; " +
            "    ex:hasAccessibility ?acc ; " +
            "    ex:hasSustainability ?sust ; " +
            "    ex:hasCrowdingLevel ?crowding ; " +
            "    ex:hasRating ?rating ; " +
            "    prov:wasAttributedTo ?provenance ; " +
            "    ex:lastUpdatedOn ?updated ; " +
            "    ex:hasCategory ?category ; " +
            "    ex:hasAccessibility ?accessibility ; " +
            "    ex:hasSustainability ?sustainability . " +
            "  FILTER( " +
            "    regex(str(?cat), '" + category + "', 'i') && " +
            "    regex(str(?acc), '" + accessibility + "', 'i') && " +
            "    regex(str(?sust), '" + sustainability + "', 'i') && " +
            "    ?rating > " + minRating + " " +
            "  ) " +
            "} ORDER BY DESC(?rating) LIMIT 20";

        return executeSparqlQuery(query, this::mapToPlaceDTO);
    }

    public PlaceDTO getPlaceById(String id) {
        log.debug("Fetching place by ID: {}", id);
        String query = PREFIX +
            "SELECT ?place ?name ?description ?category ?location ?accessibility " +
            "       ?rating ?crowding ?sustainability ?ethicalRating ?provenance ?updated " +
            "WHERE { " +
            "  ?place a ex:Place ; rdfs:label ?name ; " +
            "    ex:belongsToLocation ?location ; " +
            "    ex:hasCategory ?category ; " +
            "    ex:hasAccessibility ?accessibility ; " +
            "    ex:hasCrowdingLevel ?crowding ; " +
            "    ex:hasSustainability ?sustainability ; " +
            "    ex:hasRating ?rating ; " +
            "    ex:ethicalRating ?ethicalRating ; " +
            "    prov:wasAttributedTo ?provenance ; " +
            "    ex:lastUpdatedOn ?updated . " +
            "  OPTIONAL { ?place rdfs:comment ?description . } " +
            "  FILTER(regex(str(?place), '" + id + "')) " +
            "} LIMIT 1";

        List<PlaceDTO> results = executeSparqlQuery(query, this::mapToPlaceDTO);
        return results.isEmpty() ? null : results.get(0);
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

        // Optional fields
        if (qs.contains("?description")) {
            Literal descLit = qs.getLiteral("?description");
            dto.setDescription(descLit.getString());
        }

        if (qs.contains("?rating")) {
            Literal ratingLit = qs.getLiteral("?rating");
            dto.setRating(ratingLit.getDouble());
        }

        if (qs.contains("?ethicalRating")) {
            Literal ethicalLit = qs.getLiteral("?ethicalRating");
            dto.setEthicalRating(ethicalLit.getDouble());
        }

        if (qs.contains("?category")) {
            Resource catResource = qs.getResource("?category");
            dto.setCategory(catResource.getLocalName());
        }

        if (qs.contains("?location")) {
            Resource locResource = qs.getResource("?location");
            dto.setLocation(locResource.getLocalName());
        }

        if (qs.contains("?accessibility")) {
            Resource accResource = qs.getResource("?accessibility");
            dto.setAccessibility(accResource.getLocalName());
        }

        if (qs.contains("?crowding")) {
            Resource crowdResource = qs.getResource("?crowding");
            dto.setCrowdingLevel(crowdResource.getLocalName());
        }

        if (qs.contains("?sustainability")) {
            Resource sustResource = qs.getResource("?sustainability");
            dto.setSustainabilityLevel(sustResource.getLocalName());
        }

        if (qs.contains("?provenance")) {
            Resource provResource = qs.getResource("?provenance");
            dto.setProvenance(provResource.getLocalName());
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
