package org.app.reviewclient;

import org.app.web.dto.CustomPageDto;
import org.app.web.dto.ViewReviewsAndCommentsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "review-service", url = "http://localhost:8081")
public interface ReviewClient {


    @GetMapping("/api/reviews/movies/{id}")
    CustomPageDto<ViewReviewsAndCommentsDto> getReviewMovie(
            @PathVariable("id") UUID id,
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );
}
