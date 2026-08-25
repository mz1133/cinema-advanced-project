package org.app.actor.service;

import lombok.extern.slf4j.Slf4j;
import org.app.actor.model.Actor;
import org.app.actor.repository.ActorRepository;
import org.app.web.dto.ActorForm;
import org.app.web.dto.AddActorDto;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
@Slf4j
@Service
public class ActorService {

    private final static LocalDateTime LOCAL_DATE_TIME_NOW = LocalDateTime.now();

    private final ActorRepository actorRepository;

    public ActorService(ActorRepository actorRepository) {
        this.actorRepository = actorRepository;
    }

    public List<ActorForm> getAllActorsFullNameAndId() {
        return actorRepository.getAllActorsFullNameAndId();
    }

    public void addActor(AddActorDto addActorDto) {

        Actor actor = buildActor(addActorDto);

        actorRepository.save(actor);

        log.info(String.format("Actor with id: %s has been created successfully",  actor.getId()));
    }

    private Actor buildActor(AddActorDto addActorDto) {

        return Actor.builder()
                .firstName(addActorDto.getFirstName())
                .lastName(addActorDto.getLastName())
                .age(addActorDto.getAge())
                .birthDate(addActorDto.getBirthDate())
                .pictureUrl(addActorDto.getPictureUrl())
                .biography(addActorDto.getBiography())
                .createdOn(LOCAL_DATE_TIME_NOW)
                .updatedOn(LOCAL_DATE_TIME_NOW)
                .build();

    }
}
