package com.emmanuel.sarabrandserver.auth.service;

import com.emmanuel.sarabrandserver.auth.dto.LoginDTO;
import com.emmanuel.sarabrandserver.auth.dto.RegisterDTO;
import com.emmanuel.sarabrandserver.enumeration.RoleEnum;
import com.emmanuel.sarabrandserver.exception.DuplicateException;
import com.emmanuel.sarabrandserver.user.entity.ClientRole;
import com.emmanuel.sarabrandserver.user.entity.SaraBrandUser;
import com.emmanuel.sarabrandserver.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.Setter;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.session.ConcurrentSessionControlAuthenticationStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

import java.util.HashSet;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@Service @Setter
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolderStrategy;
    private final ConcurrentSessionControlAuthenticationStrategy strategy;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authManager,
            SecurityContextRepository securityContextRepository,
            ConcurrentSessionControlAuthenticationStrategy strategy
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.securityContextRepository = securityContextRepository;
        this.strategy = strategy;
        // Info about securitycontextholder https://stackoverflow.com/questions/74458719/isnt-securitycontextholder-a-bean
        this.securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();
    }

    /**
     * Responsible for registering a new worker. Logic is throw an error if client has a role of Worker or else add
     * ROLE worker to client.
     * @param dto of type WorkerRegisterDTO
     * @throws DuplicateException when user principal exists and has a role of worker
     * @return ResponseEntity of type HttpStatus
     * */
    @Transactional
    public ResponseEntity<?> workerRegister(RegisterDTO dto) {
        var client = this.userRepository
                .workerExists(dto.getEmail().trim(), dto.getUsername().trim())
                .orElse(createUser(dto));

        // Note User and Role tables have a relationship fetch type EAGER
        boolean isAdmin = client.getClientRole().stream().anyMatch(role -> role.getRole().equals(RoleEnum.WORKER));

        if (isAdmin) {
            throw new DuplicateException(dto.getUsername() + " exists");
        }

        client.addRole(new ClientRole(RoleEnum.WORKER));

        this.userRepository.save(client);
        return new ResponseEntity<>(CREATED);
    }

    /**
     * Method is responsible for registering a new user who isn't a worker
     * @param dto of type ClientRegisterDTO
     * @throws DuplicateException when user principal exists
     * @return ResponseEntity of type HttpStatus
     * */
    @Transactional
    public ResponseEntity<?> clientRegister(RegisterDTO dto) {
        if (this.userRepository.principalExists(dto.getEmail().trim(), dto.getUsername().trim()) > 0) {
            throw new DuplicateException(dto.getEmail() + " exists");
        }
        this.userRepository.save(createUser(dto));
        return new ResponseEntity<>(CREATED);
    }

    /**
     * Basically logs in a user based on credentials stored in the DB.
     * @param dto consist of principal and password.
     * @param request of HttpServletRequest
     * @param response of HttpServletResponse
     * @throws AuthenticationException is thrown when credentials do not exist, bad credentials account is locked e.t.c.
     * @return ResponseEntity
     * */
    @Transactional
    public ResponseEntity<?> login(LoginDTO dto, HttpServletRequest request, HttpServletResponse response) {
        var authenticated = this.authManager
                .authenticate(UsernamePasswordAuthenticationToken.unauthenticated(dto.getPrincipal(), dto.getPassword()));

        // Create a new context
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authenticated);

        // Update SecurityContextHolder and Strategy
        this.securityContextHolderStrategy.setContext(context);
        this.securityContextRepository.saveContext(context, request, response);

        // Needed to put a constraint on user session
        this.strategy.onAuthentication(authenticated, request, response);

        return new ResponseEntity<>(OK);
    }

    /** Create a new Clientz object */
    private SaraBrandUser createUser(RegisterDTO dto) {
        var client = SaraBrandUser.builder()
                .firstname(dto.getFirstname().trim())
                .lastname(dto.getLastname().trim())
                .email(dto.getEmail().trim())
                .username(dto.getUsername().trim())
                .phoneNumber(dto.getPhone().trim())
                .password(passwordEncoder.encode(dto.getPassword()))
                .enabled(true)
                .credentialsNonExpired(true)
                .accountNonExpired(true)
                .accountNoneLocked(true)
                .clientRole(new HashSet<>())
                .build();
        client.addRole(new ClientRole(RoleEnum.CLIENT));
        return client;
    }

}
