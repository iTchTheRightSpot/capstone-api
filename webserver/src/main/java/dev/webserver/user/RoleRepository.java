package dev.webserver.user;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RoleRepository extends CrudRepository<Role, Long> {
    @Query("SELECT * FROM role WHERE user_id = :userId")
    List<Role> allRolesByUserId(final long userId);
}
