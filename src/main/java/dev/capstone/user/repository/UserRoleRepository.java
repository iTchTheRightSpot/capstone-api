<<<<<<<< HEAD:webserver/src/main/java/dev/webserver/user/repository/UserRoleRepository.java
package dev.webserver.user.repository;

import dev.webserver.user.entity.ClientRole;
========
package dev.capstone.user.repository;

import dev.capstone.user.entity.ClientRole;
>>>>>>>> 38dca43c14b569b33b94a23c1bdce50584a67195:src/main/java/dev/capstone/user/repository/UserRoleRepository.java
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRoleRepository extends JpaRepository<ClientRole, Long> { }
