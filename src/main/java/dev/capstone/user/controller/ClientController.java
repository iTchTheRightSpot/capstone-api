<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/user/controller/ClientController.java
package dev.webserver.user.controller;
========
package dev.capstone.user.controller;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/user/controller/ClientController.java

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "${api.endpoint.baseurl}client/user")
@RequiredArgsConstructor
@PreAuthorize(value = "hasRole('ROLE_CLIENT')")
public class ClientController { }