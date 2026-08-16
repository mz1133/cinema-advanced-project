package org.app.actor.repository;

import org.app.actor.model.Actor;
import org.app.web.dto.ActorForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ActorRepository extends JpaRepository<Actor, UUID> {

    @Query("SELECT new org.app.web.dto.ActorForm(a.id, CONCAT(a.firstName, ' ', a.lastName)) FROM Actor a")
    List<ActorForm> getAllActorsFullNameAndId();
}
