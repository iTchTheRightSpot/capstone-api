package com.sarabrandserver.migration;

import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Configuration;

/**
 * As flyway automatic detection of Java migrations are not supported in native image
 * <a href="https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-with-GraalVM">...</a>
 * Workaround is <a href="https://github.com/spring-projects/spring-boot/issues/33458">...</a>
 */
@Configuration
public class JavaMigrationsFlywayCustomizer implements FlywayConfigurationCustomizer {

    @Override
    public void customize(FluentConfiguration configuration) {
        configuration
                .javaMigrations(
                        new V1__init_migration(),
                        new V2__init_migration(),
                        new V3__init_migration(),
                        new V4__init_migration(),
                        new V5__init_migration()
                );
    }

}
