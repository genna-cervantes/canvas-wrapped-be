package com.canvaswrapped.service;

import com.canvaswrapped.webapi.ApiClient;
import com.canvaswrapped.model.Assignment;
import com.canvaswrapped.model.Course;
import com.canvaswrapped.model.Group;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@ExtendWith(MockitoExtension.class)
public class CanvasServiceTest {

    @Mock
    ApiClient apiClient;

    @InjectMocks
    CanvasService canvasService;

    @Test
    void requestGroups_retriesAndEventuallyReturnsValidJson(){
        String token = "";
        ArrayList<Group> groups = new ArrayList<>(List.of());

        Mockito.when(apiClient.fetchGroups(token))
                .thenThrow(new RuntimeException("timeout 1"))
                .thenThrow(new RuntimeException("timeout 2"))
                .thenReturn(Mono.just(groups));

        ArrayList<Group> gr = canvasService.requestGroups(token);

        Assertions.assertEquals(groups, gr);
    }

    @Test
    void requestCourses_retriesAndEventuallyReturnsValidJson(){
        String token = "";
        ArrayList<Course> courses = new ArrayList<>(List.of());

        Mockito.when(apiClient.fetchCourses(token))
                .thenThrow(new RuntimeException("timeout 1"))
                .thenThrow(new RuntimeException("timeout 2"))
                .thenReturn(Mono.just(courses));

        ArrayList<Course> cr = canvasService.requestCourses(token);

        Assertions.assertEquals(courses, cr);
    }

    @Test
    void requestAssignments_retriesAndEventuallyReturnsValidJson(){
        String token = "";
        Long courseId = 1234L;
        ArrayList<Assignment> as = new ArrayList<>(List.of());

        Mockito.when(apiClient.fetchAssignments(token, courseId))
                .thenThrow(new RuntimeException("timeout 1"))
                .thenThrow(new RuntimeException("timeout 2"))
                .thenReturn(Mono.just(new ArrayList<>(List.of())));

        CompletableFuture<Mono<ArrayList<Assignment>>> ar = canvasService.requestAssignments(token, courseId);

        Assertions.assertEquals(as, ar.join().block());
    }

}
