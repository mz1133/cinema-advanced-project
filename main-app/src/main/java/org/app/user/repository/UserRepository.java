package org.app.user.repository;


import org.app.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    Optional<User> findByUsernameOrEmail(String usernameOrEmail, String usernameOrEmail1);

    Page<User> findAllByIdNot(Pageable pageable, UUID currentUserId);

    Page<User> findByIdAndIdNot(UUID keywordId, UUID currentUserId, Pageable pageable);

    Page<User> findByUsernameAndIdNot(String keyword, Pageable pageable, UUID currentUserId);
}
