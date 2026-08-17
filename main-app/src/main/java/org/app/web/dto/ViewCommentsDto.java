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
public class ViewCommentsDto {

    private UUID id;

    private String content;

    private String publisherUsername;

    private LocalDateTime createdDate;

    private LocalDateTime updatedOn;
}
