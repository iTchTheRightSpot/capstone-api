package com.sarabrandserver;

import com.sarabrandserver.auth.dto.RegisterDTO;
import com.sarabrandserver.auth.service.AuthService;
import com.sarabrandserver.graal.MyRuntimeHints;
import com.sarabrandserver.order.dto.PayloadMapper;
import com.sarabrandserver.product.dto.VariantMapper;
import com.sarabrandserver.thirdparty.PaymentCredentialObj;
import com.sarabrandserver.user.repository.UserRepository;
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
@RegisterReflectionForBinding(value = {VariantMapper.class, PayloadMapper.class, PaymentCredentialObj.class})
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
            if (repository.findByPrincipal(principal).isEmpty()) {
                var dto = new RegisterDTO(
                        "SEJU",
                        "Development",
                        principal,
                        principal,
                        "0000000000",
                        password
                );
                service.workerRegister(dto);
            }
        };
    }

}