package dev.webserver;

import dev.webserver.auth.dto.RegisterDto;
import dev.webserver.auth.service.AuthService;
import dev.webserver.enumeration.RoleEnum;
import dev.webserver.graal.MyRuntimeHints;
import dev.webserver.payment.dto.PayloadMapper;
import dev.webserver.product.response.Variant;
import dev.webserver.thirdparty.PaymentCredentialObj;
import dev.webserver.user.repository.UserRepository;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ImportRuntimeHints(value = {MyRuntimeHints.class})
@RegisterReflectionForBinding(value = {Variant.class, PayloadMapper.class, PaymentCredentialObj.class})
public class Application {

    @Value(value = "${user.principal}")
    private String principal;
    @Value(value = "${user.password}")
    private String password;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    @Profile(value = "default")
    public CommandLineRunner commandLineRunner(AuthService service, UserRepository repository) {
        return args -> {
            if (repository.userByPrincipal(principal).isEmpty()) {
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
        };
    }

}