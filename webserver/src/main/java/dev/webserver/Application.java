package dev.webserver;

import dev.webserver.enumeration.RoleEnum;
import dev.webserver.external.log.DiscordPayload;
import dev.webserver.payment.OrderHistoryDbMapper;
import dev.webserver.product.Variant;
import dev.webserver.user.Role;
import dev.webserver.user.RoleRepository;
import dev.webserver.user.User;
import dev.webserver.user.UserRepository;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ImportRuntimeHints(value = {MyRuntimeHints.class})
@RegisterReflectionForBinding(value = {Variant.class, OrderHistoryDbMapper.class, AbstractEnvironment.PaymentCredentialObj.class, DiscordPayload.class})
public class Application extends AbstractEnvironment {

    protected Application(final Environment environment) {
        super(environment);
    }

    public static void main(final String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(final UserRepository repository, final RoleRepository roleRepository) {
        return args -> {
            if (repository.userByPrincipal(developerEmail.trim()).isEmpty()) {
                final User user = repository.save(User.builder()
                        .email(developerEmail)
                        .firstname(developerFirstname)
                        .fullname(developerLastName)
                        .build());

                roleRepository.save(new Role(null, RoleEnum.USER, user.userId()));
                roleRepository.save(new Role(null, RoleEnum.EMPLOYEE, user.userId()));
                roleRepository.save(new Role(null, RoleEnum.DEVELOPER, user.userId()));
            }
        };
    }

}