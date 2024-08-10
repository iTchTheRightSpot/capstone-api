package dev.webserver.user;

import dev.webserver.util.Page;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends CrudRepository<User, Long> {

    @Query(value = "SELECT * FROM user c WHERE c.email = :principal")
    Optional<User> userByPrincipal(String principal);

    @Query(value = "SELECT * FROM user u LIMIT :#{#page.size()} OFFSET :#{#page.offset()}")
    List<User> listOfUsers(final Page page);

    @Query("SELECT COUNT(user_id) FROM user")
    Integer countAllUsers();

    @Transactional
    @Modifying
    @Query("UPDATE user SET image_key = :image WHERE user_id = :userId")
    void updateUserImage(final long userId, final String imageKey);

}
