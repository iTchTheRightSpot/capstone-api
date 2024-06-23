package dev.webserver;

import dev.webserver.auth.dto.RegisterDto;
import dev.webserver.auth.service.AuthService;
import dev.webserver.enumeration.RoleEnum;
import dev.webserver.external.log.DiscordPayload;
import dev.webserver.payment.dto.PayloadMapper;
import dev.webserver.product.response.Variant;
import dev.webserver.external.PaymentCredentialObj;
import dev.webserver.user.entity.ClientRole;
import dev.webserver.user.entity.SarreBrandUser;
import dev.webserver.user.repository.UserRepository;
import dev.webserver.user.repository.UserRoleRepository;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableScheduling
@ImportRuntimeHints(value = {MyRuntimeHints.class})
@RegisterReflectionForBinding(value = {Variant.class, PayloadMapper.class, PaymentCredentialObj.class, DiscordPayload.class})
// https://docs.spring.io/spring-data/rest/reference/data-commons/repositories/core-extensions.html
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class Application {

    @Value(value = "${user.principal}")
    private String principal;
    @Value(value = "${user.password}")
    private String password;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @Profile(value = { "default", "aws", "native-test" })
    public CommandLineRunner commandLineRunner(
            AuthService service,
            UserRepository repository,
            UserRoleRepository roleRepository,
            PasswordEncoder encoder,
            Environment env
    ) {
        return args -> {
            if (repository.userByPrincipal(principal).isEmpty()) {
                if (env.matchesProfiles("native-test")) {
                    var user = repository.save(SarreBrandUser.builder()
                            .firstname("SEJU")
                            .lastname("Development")
                            .email(principal)
                            .password(encoder.encode(password))
                            .phoneNumber("0000000000")
                            .enabled(true)
                            .clientRole(new HashSet<>())
                            .build());
                    roleRepository.save(new ClientRole(RoleEnum.CLIENT, user));
                    roleRepository.save(new ClientRole(RoleEnum.WORKER, user));
                    roleRepository.save(new ClientRole(RoleEnum.NATIVE, user));
                } else {
                    var dto = new RegisterDto(
                            "SEJU",
                            "Development",
                            principal,
                            principal,
                            "0000000000",
                            password
                    );
                    service.register(null, dto, RoleEnum.WORKER);
                }
            }
        };
    }

}