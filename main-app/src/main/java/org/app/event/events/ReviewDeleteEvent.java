package org.app.event.events;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.app.user.model.User;



@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewDeleteEvent {


    private User user;
    private String reasonMessage;



}
