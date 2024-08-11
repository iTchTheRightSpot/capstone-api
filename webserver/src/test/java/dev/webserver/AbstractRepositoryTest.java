package dev.webserver;

import dev.webserver.payment.AddressRepository;
import dev.webserver.payment.PaymentAuthorizationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@DataJdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application.yml")
@Transactional
@Import(AbstractRepositoryTest.RepositoryConfigurations.class)
public abstract class AbstractRepositoryTest extends SuperAbstract {

    @TestConfiguration
    static class RepositoryConfigurations {
        @Autowired
        private JdbcClient client;

        @Bean
        public AddressRepository addressRepository() {
            return new AddressRepository(client);
        }

        @Bean
        public PaymentAuthorizationRepository paymentAuthorizationRepository() {
            return new PaymentAuthorizationRepository(client);
        }
    }
}