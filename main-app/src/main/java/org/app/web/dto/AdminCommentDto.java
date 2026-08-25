package org.app.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminCommentDto {

    private UUID commentId;

    private String content;

    private UUID publisherId;

    private boolean isDeleted;

    private String publisherUsername;

    private LocalDateTime createdOn;
}