package dev.webserver.user;

import org.springframework.data.domain.Page;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends CrudRepository<SarreBrandUser, Long> {

    /**
     * Returns an {@link Optional} where if is not empty a {@link SarreBrandUser}
     * object is returned.
     *
     * @param principal is the email of property of a {@link SarreBrandUser}
     *                  object.
     * @return An {@link Optional} of a {@link SarreBrandUser} or null.
     */
    @Query(value = "SELECT * FROM clientz c WHERE c.email = :principal")
    Optional<SarreBrandUser> userByPrincipal(@Param(value = "principal") String principal);

    /**
     * Returns a {@link Page} of {@link SarreBrandUser}.
     */
    @Query(value = "SELECT * FROM clientz u")
    Page<SarreBrandUser> allUsers();

}
