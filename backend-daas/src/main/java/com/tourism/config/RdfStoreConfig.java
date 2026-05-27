package com.tourism.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

        Path externalDataset = findExternalDataset();
        try (InputStream is = openDataset(externalDataset)) {
            model.read(is, null, "TURTLE");
            log.info("Successfully loaded {} statements into RDF model", model.size());
        } catch (IOException e) {
            log.error("Failed to load RDF Turtle file", e);
            throw new RuntimeException("Failed to load RDF model", e);
        }
        
        return model;
    }

    private Path findExternalDataset() {
        List<Path> candidates = List.of(
            Path.of("dataset", "tourism.ttl"),
            Path.of("..", "dataset", "tourism.ttl"),
            Path.of("backend-daas", "src", "main", "resources", "rdf", "tourism.ttl")
        );

        return candidates.stream()
            .map(Path::toAbsolutePath)
            .map(Path::normalize)
            .filter(Files::isRegularFile)
            .findFirst()
            .orElse(null);
    }

    private InputStream openDataset(Path externalDataset) throws IOException {
        if (externalDataset != null) {
            log.info("Loading RDF dataset from external file: {}", externalDataset);
            return new FileInputStream(externalDataset.toFile());
        }

        log.info("Loading RDF dataset from classpath: /rdf/tourism.ttl");
        InputStream classpathDataset = getClass().getResourceAsStream("/rdf/tourism.ttl");
        if (classpathDataset == null) {
            log.error("RDF Turtle file not found in external paths or classpath");
            throw new IOException("RDF Turtle file not found");
        }
        return classpathDataset;
    }

}
