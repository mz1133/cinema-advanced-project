package org.app.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.app.subscription.model.Subscription;
import org.app.user.model.Role;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserHeaderDto {

    private UUID id;

    private String username;

    private String pictureUrl;

    private boolean isCanAddMovie;

    private Role role;

    private LocalDateTime createdOn;

    private Subscription subscription;

}
