package com.tourism.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Configuration
public class RdfStoreConfig {

    @Bean
    public Dataset rdfDataset() {
        log.info("Initializing RDF Dataset Store (in-memory)");
        return DatasetFactory.createTxnMem();
    }

    @Bean
    public Model tourismModel(Dataset dataset) {
        log.info("Loading Tourism RDF Model from Turtle file");
        Model model = dataset.getDefaultModel();
        
        try (InputStream is = getClass().getResourceAsStream("/rdf/tourism.ttl")) {
            if (is == null) {
                log.error("RDF Turtle file not found: /rdf/tourism.ttl");
                throw new RuntimeException("Failed to load RDF model: file not found");
            }
            model.read(is, null, "TURTLE");
            log.info("Successfully loaded {} statements into RDF model", model.size());
        } catch (IOException e) {
            log.error("Failed to load RDF Turtle file", e);
            throw new RuntimeException("Failed to load RDF model", e);
        }
        
        return model;
    }

}
