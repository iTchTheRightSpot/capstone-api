package dev.webserver.graal;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypes;
import org.springframework.orm.jpa.persistenceunit.PersistenceManagedTypesScanner;

import javax.sql.DataSource;

@Configuration
class JpaConfig {

    /**
     * Because of lazy loaded entities.
     * @see <a href="https://docs.spring.io/spring-framework/reference/core/aot.html">documentation</a>
     * */
    @Bean
    public PersistenceManagedTypes persistenceManagedTypes(ResourceLoader resourceLoader) {
        return new PersistenceManagedTypesScanner(resourceLoader)
                .scan("dev.webserver");
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean customDBEntityManagerFactory(
            DataSource dataSource,
            PersistenceManagedTypes managedTypes
    ) {
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setDataSource(dataSource);
        factoryBean.setManagedTypes(managedTypes);
        return factoryBean;
    }

}
