package com.sarabrandserver.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "api/v1/client/user")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ROLE_CLIENT')")
public class ClientController { }