package dev.webserver.user;

import org.springframework.data.repository.CrudRepository;

public interface UserRoleRepository extends CrudRepository<ClientRole, Long> { }
