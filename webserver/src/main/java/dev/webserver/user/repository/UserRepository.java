package dev.webserver.user.repository;

import dev.webserver.user.entity.SarreBrandUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<SarreBrandUser, Long> {

    /**
     * Returns an {@link Optional} where if is not empty a {@link SarreBrandUser}
     * object is returned.
     *
     * @param principal is the email of property of a {@link SarreBrandUser}
     *                  object.
     * @return An {@link Optional} of a {@link SarreBrandUser} or null.
     */
    @Query(value = "SELECT c FROM SarreBrandUser c WHERE c.email = :principal")
    Optional<SarreBrandUser> userByPrincipal(@Param(value = "principal") String principal);

    /**
     * Returns a {@link Page} of {@link SarreBrandUser}.
     */
    @Query(value = "SELECT u FROM SarreBrandUser u")
    Page<SarreBrandUser> allUsers(Pageable page);

}
