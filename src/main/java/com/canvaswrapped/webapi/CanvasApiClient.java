package com.canvaswrapped.webapi;

import com.canvaswrapped.model.Assignment;
import com.canvaswrapped.model.Course;
import com.canvaswrapped.model.Group;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

@Component
public class CanvasApiClient implements ApiClient{
    private final WebClient apiClient;
    private final static int MAX_ATTEMPTS = 3;

    public CanvasApiClient(WebClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Mono<ArrayList<Group>> fetchGroups(String token){
        return apiClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("users/self/groups")
                        .queryParam("access_token", token)
                        .queryParam("per_page", 1000)
                        .build())
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ArrayList<Group>>() {})
                .retry(MAX_ATTEMPTS); // retries 2 times after the first failure = 3 attempts total
    }

    @Override
    public Mono<ArrayList<Course>> fetchCourses(String token){
        return apiClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("courses")
                        .queryParam("access_token", token)
                        .queryParam("enrollment_state", "active")
                        .build())
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ArrayList<Course>>() {})
                .retry(MAX_ATTEMPTS);
    }

    @Override
    public Mono<ArrayList<Assignment>> fetchAssignments(String token, Long courseId){
        return apiClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/courses/" + courseId + "/assignments")
                        .queryParam("access_token", token)
                        .queryParam("per_page", "100")
                        .queryParam("include", "submission")
                        .build())
                .headers(httpHeaders -> httpHeaders.setBearerAuth(token))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<ArrayList<Assignment>>() {})
                .retry(MAX_ATTEMPTS);
    }
}
