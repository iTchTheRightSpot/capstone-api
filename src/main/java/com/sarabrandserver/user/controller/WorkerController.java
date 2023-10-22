package com.sarabrandserver.user.controller;

import com.sarabrandserver.user.res.UserResponse;
import com.sarabrandserver.user.service.WorkerService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/v1/worker/user")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ROLE_WORKER')")
public class WorkerController {

    private final WorkerService workerService;

    @GetMapping(produces = "application/json")
    public Page<UserResponse> allUsers(
            @NotNull @RequestParam(name = "page", defaultValue = "0") Integer page,
            @NotNull @RequestParam(name = "size", defaultValue = "20") Integer size
    ) {
        return this.workerService.allUsers(page, Math.min(size, 20));
    }

}
