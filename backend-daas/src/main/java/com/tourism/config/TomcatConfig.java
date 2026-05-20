package com.tourism.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Slf4j
@Configuration
public class TomcatConfig {

    @Bean
    public WebServerFactory webServerFactory() {
        TomcatServletWebServerFactory factory = new TomcatServletWebServerFactory();
        
        // Set a writable temp directory
        File tempDir = new File(System.getProperty("user.dir") + "/target/tomcat-daas");
        tempDir.mkdirs();
        
        factory.setBaseDirectory(tempDir);
        
        // Disable temp dir creation
        factory.addInitializers(servletContext -> {
            System.setProperty("java.io.tmpdir", tempDir.getAbsolutePath());
        });
        
        log.info("Tomcat base directory set to: {}", tempDir.getAbsolutePath());
        
        return factory;
    }

}
