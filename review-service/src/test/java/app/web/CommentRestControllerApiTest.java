package app.web;

import app.comment.service.CommentService;
import app.web.dto.CreateCommentDto;
import app.web.dto.DeleteCommentDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommentRestController.class)
class CommentRestControllerApiTest {

    @MockitoBean
    private CommentService commentService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void postRequestToCreateComment_happyPath_returnsCreated() throws Exception {

        UUID publisherId = UUID.randomUUID();
        UUID reviewId = UUID.randomUUID();

        CreateCommentDto dto = CreateCommentDto.builder()
                .publisherId(publisherId)
                .publisherUsername("testUser")
                .content("This is a valid comment")
                .reviewId(reviewId)
                .isDeleted(false)
                .build();

        MockHttpServletRequestBuilder request =
                post("/api/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isCreated());

        verify(commentService).createComment(any(CreateCommentDto.class));
    }

    @Test
    void postRequestToCreateComment_withoutRequestBody_returnsBadRequest()
            throws Exception {

        MockHttpServletRequestBuilder request =
                post("/api/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(commentService, never())
                .createComment(any(CreateCommentDto.class));
    }

    @Test
    void postRequestToCreateComment_withEmptyContent_returnsBadRequest()
            throws Exception {

        UUID reviewId = UUID.randomUUID();

        CreateCommentDto dto = CreateCommentDto.builder()
                .publisherId(UUID.randomUUID())
                .publisherUsername("testUser")
                .content("")
                .reviewId(reviewId)
                .isDeleted(false)
                .build();

        MockHttpServletRequestBuilder request =
                post("/api/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(commentService, never())
                .createComment(any(CreateCommentDto.class));
    }

    @Test
    void postRequestToCreateComment_withNullContent_returnsBadRequest()
            throws Exception {

        UUID reviewId = UUID.randomUUID();

        CreateCommentDto dto = CreateCommentDto.builder()
                .publisherId(UUID.randomUUID())
                .publisherUsername("testUser")
                .content(null)
                .reviewId(reviewId)
                .isDeleted(false)
                .build();

        MockHttpServletRequestBuilder request =
                post("/api/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(commentService, never())
                .createComment(any(CreateCommentDto.class));
    }

    @Test
    void postRequestToCreateComment_withContentShorterThanFiveCharacters_returnsBadRequest()
            throws Exception {

        CreateCommentDto dto = CreateCommentDto.builder()
                .publisherId(UUID.randomUUID())
                .publisherUsername("testUser")
                .content("test")
                .reviewId(UUID.randomUUID())
                .build();

        MockHttpServletRequestBuilder request =
                post("/api/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(commentService, never())
                .createComment(any(CreateCommentDto.class));
    }

    @Test
    void postRequestToCreateComment_withContentLongerThan300Characters_returnsBadRequest()
            throws Exception {

        String content = "a".repeat(301);

        CreateCommentDto dto = CreateCommentDto.builder()
                .publisherId(UUID.randomUUID())
                .publisherUsername("testUser")
                .content(content)
                .reviewId(UUID.randomUUID())
                .build();

        MockHttpServletRequestBuilder request =
                post("/api/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(commentService, never())
                .createComment(any(CreateCommentDto.class));
    }


    @Test
    void postRequestToCreateComment_withoutReviewId_returnsBadRequest()
            throws Exception {

        CreateCommentDto dto = CreateCommentDto.builder()
                .publisherId(UUID.randomUUID())
                .publisherUsername("testUser")
                .content("This is a valid comment")
                .reviewId(null)
                .build();

        MockHttpServletRequestBuilder request =
                post("/api/comments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(commentService, never())
                .createComment(any(CreateCommentDto.class));
    }

    @Test
    void postRequestToDeleteComment_happyPath_returnsOk()
            throws Exception {

        DeleteCommentDto dto = DeleteCommentDto.builder()
                .reviewId(UUID.randomUUID())
                .publisherId(UUID.randomUUID())
                .commentId(UUID.randomUUID())
                .reason("Inappropriate content")
                .build();

        MockHttpServletRequestBuilder request =
                post("/api/comments/delete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isOk());

        verify(commentService)
                .deleteComment(any(DeleteCommentDto.class));
    }

    @Test
    void postRequestToDeleteComment_withoutRequestBody_returnsBadRequest()
            throws Exception {

        MockHttpServletRequestBuilder request =
                post("/api/comments/delete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON);

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(commentService, never())
                .deleteComment(any(DeleteCommentDto.class));
    }

    @Test
    void postRequestToDeleteComment_withoutReviewId_returnsBadRequest()
            throws Exception {

        DeleteCommentDto dto = DeleteCommentDto.builder()
                .reviewId(null)
                .publisherId(UUID.randomUUID())
                .commentId(UUID.randomUUID())
                .reason("Inappropriate content")
                .build();

        MockHttpServletRequestBuilder request =
                post("/api/comments/delete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(commentService, never())
                .deleteComment(any(DeleteCommentDto.class));
    }

    @Test
    void postRequestToDeleteComment_withoutPublisherId_returnsBadRequest()
            throws Exception {

        DeleteCommentDto dto = DeleteCommentDto.builder()
                .reviewId(UUID.randomUUID())
                .publisherId(null)
                .commentId(UUID.randomUUID())
                .reason("Inappropriate content")
                .build();

        MockHttpServletRequestBuilder request =
                post("/api/comments/delete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(commentService, never())
                .deleteComment(any(DeleteCommentDto.class));
    }

    @Test
    void postRequestToDeleteComment_withoutCommentId_returnsBadRequest()
            throws Exception {

        DeleteCommentDto dto = DeleteCommentDto.builder()
                .reviewId(UUID.randomUUID())
                .publisherId(UUID.randomUUID())
                .commentId(null)
                .reason("Inappropriate content")
                .build();

        MockHttpServletRequestBuilder request =
                post("/api/comments/delete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(commentService, never())
                .deleteComment(any(DeleteCommentDto.class));
    }

    @Test
    void postRequestToDeleteComment_withoutReason_returnsBadRequest()
            throws Exception {

        DeleteCommentDto dto = DeleteCommentDto.builder()
                .reviewId(UUID.randomUUID())
                .publisherId(UUID.randomUUID())
                .commentId(UUID.randomUUID())
                .reason(null)
                .build();

        MockHttpServletRequestBuilder request =
                post("/api/comments/delete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto));

        mockMvc.perform(request)
                .andExpect(status().isBadRequest());

        verify(commentService, never())
                .deleteComment(any(DeleteCommentDto.class));
    }
}