package com.canvaswrapped.webapi;

import com.canvaswrapped.model.Assignment;
import com.canvaswrapped.model.Course;
import com.canvaswrapped.model.Group;
import reactor.core.publisher.Mono;

import java.util.ArrayList;

public interface ApiClient {
    Mono<ArrayList<Group>> fetchGroups(String token);

    Mono<ArrayList<Course>> fetchCourses(String token);

    Mono<ArrayList<Assignment>> fetchAssignments(String token, Long courseId);
}
