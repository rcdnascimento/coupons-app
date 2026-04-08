package com.coupons.auth.infra.persistence;

import com.coupons.auth.domain.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query(
            "SELECT u FROM User u WHERE LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%'))"
                    + " OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%'))")
    List<User> searchByNameOrEmailContaining(@Param("q") String q, Pageable pageable);
}
