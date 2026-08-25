package app.service;

import org.app.Application;
import org.app.actor.model.Actor;
import org.app.actor.repository.ActorRepository;
import org.app.actor.service.ActorService;
import org.app.web.dto.ActorForm;
import org.app.web.dto.AddActorDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


import static org.junit.jupiter.api.Assertions.*;


@ActiveProfiles("test")
@Transactional
@SpringBootTest(classes = Application.class)
class ActorServiceITest {

    @Autowired
    private ActorService actorService;

    @Autowired
    private ActorRepository actorRepository;

    @BeforeEach
    void setUp() {
        actorRepository.deleteAll();
    }

    @Test
    void addActor_shouldSaveActorToDatabase() {

        AddActorDto dto = AddActorDto.builder()
                .firstName("Leonardo")
                .lastName("DiCaprio")
                .birthDate(LocalDate.of(1974, 11, 11))
                .pictureUrl("https://example.com/leonardo.jpg")
                .age(51)
                .biography("American actor")
                .build();

        actorService.addActor(dto);

        List<Actor> actors = actorRepository.findAll();

        assertEquals(1, actors.size());

        Actor savedActor = actors.get(0);

        assertNotNull(savedActor.getId());
        assertEquals("Leonardo", savedActor.getFirstName());
        assertEquals("DiCaprio", savedActor.getLastName());
        assertEquals(LocalDate.of(1974, 11, 11), savedActor.getBirthDate());
        assertEquals("https://example.com/leonardo.jpg", savedActor.getPictureUrl());
        assertEquals(51, savedActor.getAge());
        assertEquals("American actor", savedActor.getBiography());

        assertNotNull(savedActor.getCreatedOn());
        assertNotNull(savedActor.getUpdatedOn());
    }

    @Test
    void getAllActorsFullNameAndId_shouldReturnActorsWithFullName() {

        Actor actor1 = Actor.builder()
                .firstName("Leonardo")
                .lastName("DiCaprio")
                .birthDate(LocalDate.of(1974, 11, 11))
                .age(51)
                .createdOn(LocalDate.of(2026, 1, 1).atStartOfDay())
                .updatedOn(LocalDate.of(2026, 1, 1).atStartOfDay())
                .build();

        Actor actor2 = Actor.builder()
                .firstName("Tom")
                .lastName("Hanks")
                .birthDate(LocalDate.of(1956, 7, 9))
                .age(70)
                .createdOn(LocalDate.of(2026, 1, 1).atStartOfDay())
                .updatedOn(LocalDate.of(2026, 1, 1).atStartOfDay())
                .build();

        actorRepository.saveAll(List.of(actor1, actor2));

        List<ActorForm> result = actorService.getAllActorsFullNameAndId();

        assertEquals(2, result.size());

        assertTrue(
                result.stream()
                        .anyMatch(actor ->
                                actor.getId().equals(actor1.getId())
                                        && actor.getFullName().equals("Leonardo DiCaprio"))
        );

        assertTrue(
                result.stream()
                        .anyMatch(actor ->
                                actor.getId().equals(actor2.getId())
                                        && actor.getFullName().equals("Tom Hanks"))
        );
    }

    @Test
    void getAllActorsFullNameAndId_whenNoActors_shouldReturnEmptyList() {

        List<ActorForm> result =
                actorService.getAllActorsFullNameAndId();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}