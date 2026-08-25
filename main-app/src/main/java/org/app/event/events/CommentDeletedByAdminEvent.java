package org.app.event.events;

import lombok.AllArgsConstructor;
import lombok.Data;

import lombok.NoArgsConstructor;
import org.app.user.model.User;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentDeletedByAdminEvent {

    private User user;

    private UUID reviewId;

    private UUID commentId;

    private String publisherUsername;

    private String reason;
}
