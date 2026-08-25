package app.service;


import org.app.actor.model.Actor;
import org.app.actor.repository.ActorRepository;
import org.app.actor.service.ActorService;
import org.app.web.dto.ActorForm;
import org.app.web.dto.AddActorDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ActorServiceUTest {

    @Mock
    private ActorRepository actorRepository;

    @InjectMocks
    private ActorService actorService;

    @Test
    void getAllActorsFullNameAndId_happyPath() {

        List<ActorForm> actors = List.of(
                new ActorForm(),
                new ActorForm()
        );

        when(actorRepository.getAllActorsFullNameAndId())
                .thenReturn(actors);

        List<ActorForm> result = actorService.getAllActorsFullNameAndId();

        assertEquals(actors, result);

        verify(actorRepository).getAllActorsFullNameAndId();
    }

    @Test
    void addActor_happyPath() {

        AddActorDto addActorDto = new AddActorDto();

        addActorDto.setFirstName("Tom");
        addActorDto.setLastName("Hanks");
        addActorDto.setAge(65);
        addActorDto.setBirthDate(LocalDate.of(1956, 7, 9));
        addActorDto.setPictureUrl("https://example.com/tom-hanks.jpg");

        actorService.addActor(addActorDto);

        ArgumentCaptor<Actor> actorCaptor =
                ArgumentCaptor.forClass(Actor.class);

        verify(actorRepository).save(actorCaptor.capture());

        Actor savedActor = actorCaptor.getValue();

        assertNotNull(savedActor);

        assertEquals("Tom", savedActor.getFirstName());
        assertEquals("Hanks", savedActor.getLastName());
        assertEquals(65, savedActor.getAge());
        assertEquals(
                LocalDate.of(1956, 7, 9),
                savedActor.getBirthDate()
        );
        assertEquals(
                "https://example.com/tom-hanks.jpg",
                savedActor.getPictureUrl()
        );

        assertNotNull(savedActor.getCreatedOn());
        assertNotNull(savedActor.getUpdatedOn());

        assertEquals(
                savedActor.getCreatedOn(),
                savedActor.getUpdatedOn()
        );
    }

}
