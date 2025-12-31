package com.canvaswrapped.service;

import com.canvaswrapped.webapi.ApiClient;
import com.canvaswrapped.model.Assignment;
import com.canvaswrapped.model.Course;
import com.canvaswrapped.model.Group;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class CanvasService {

    private static final Logger log = LoggerFactory.getLogger(CanvasService.class);
    private final ApiClient apiClient;
    private static final int MAX_ATTEMPTS = 3;

    public CanvasService(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public ArrayList<Group> requestGroups(String token){
        Exception err = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++){
            try{
                return apiClient.fetchGroups(token).block();
            }catch(Exception e){
                err = e;
                // swallow and retry
            }
        }
        log.error("e: ", err);
        return new ArrayList<>(List.of());
    }

    public ArrayList<Course> requestCourses(String token){
        Exception err = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++){
            try{
                return apiClient.fetchCourses(token).block();
            }catch(Exception e){
                err = e;
                // swallow and retry
            }
        }

        log.error("e: ", err);
        return new ArrayList<>(List.of());
    }

    public CompletableFuture<Mono<ArrayList<Assignment>>> requestAssignments(String token, Long courseId){
        Mono<ArrayList<Assignment>> ass = Mono.just(new ArrayList<>(List.of()));
        Exception err = null;
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++){
            try{
                ass = apiClient.fetchAssignments(token, courseId);
            }catch(Exception e){
                err = e;
                // swallow and retry
            }
        }
        log.error("e: ", err);

        Mono<ArrayList<Assignment>> finalAss = ass;
        assert ass != null;
        return CompletableFuture.supplyAsync(() -> finalAss);
    }
}
