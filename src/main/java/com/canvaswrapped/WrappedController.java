package com.canvaswrapped;

import com.canvaswrapped.model.*;
import com.canvaswrapped.service.CanvasService;
import com.canvaswrapped.service.GeminiService;
import com.canvaswrapped.service.WrappedService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@RestController
@CrossOrigin(origins = "https://canvas-wrapped-pi.vercel.app")
@RequestMapping("api/v1/wrapped")
public class WrappedController {

    private final Pattern quizPattern = Pattern.compile("(?i)\\b(quiz|lt|exam|test)\\b");
    private final Pattern attendancePattern = Pattern.compile("(?i)\\b(roll call|attendance|rollcall)\\b");

    private final WrappedService wrappedService;
    private final CanvasService canvasService;
    private final GeminiService geminiService;

    public WrappedController(WrappedService wrappedService, CanvasService canvasService, GeminiService geminiService){
        this.canvasService = canvasService;
        this.wrappedService = wrappedService;
        this.geminiService = geminiService;
    }

    @PostMapping
    public ResponseEntity<?> getWrapped(@Validated @RequestBody WrappedRequest body){

        String token = body.token();
        if (token.isBlank() || token.length() < 20) return ResponseEntity.badRequest().build();

        ArrayList<Course> courses = canvasService.requestCourses(token);
        ArrayList<Group> groups = canvasService.requestGroups(token);

        List<CompletableFuture<ArrayList<Assignment>>> futures = courses.stream()
                .map(course -> canvasService.requestAssignments(token, course.id())
                        .thenCompose(Mono::toFuture))
                .toList();
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        ArrayList<Assignment> flattenedAssignments = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Assignment> attendance = new ArrayList<>();
        ArrayList<Assignment> assignments = new ArrayList<>();
        ArrayList<Assignment> quizzes = new ArrayList<>();
        ArrayList<Assignment> discussionBoards = new ArrayList<>();

        for (var as : flattenedAssignments){
            if (((as.getIsQuizAssignment() != null && as.getIsQuizAssignment()) || (as.getIsQuizLtiAssignment() != null && as.getIsQuizLtiAssignment())) &&
                    (quizPattern.matcher(as.getName()).find())){
                quizzes.add(as);
            }else if (as.getSubmissionTypes().contains("discussion_topic")){
                discussionBoards.add(as);
            }else if (attendancePattern.matcher(as.getName()).find()){
                attendance.add(as);
            }else{
                assignments.add(as);
            }
        }

        WrappedResponse wrappedResponse = new WrappedResponse();

        ArrayList<Assignment> combined = new ArrayList<>(assignments);
        combined.addAll(quizzes);

        wrappedResponse.setAssignments(wrappedService.analyzeAssignmentsOrQuizzes(assignments, courses));
        wrappedResponse.setQuizzes(wrappedService.analyzeAssignmentsOrQuizzes(quizzes, courses));
        wrappedResponse.setDiscussion(wrappedService.analyzeDiscussionEntries(discussionBoards));
        wrappedResponse.setAttendance(wrappedService.analyzeAttendance(attendance));
        wrappedResponse.setCourses(wrappedService.analyzeCourses(combined, courses));
        wrappedResponse.setHellWeek(wrappedService.analyzeHellWeek(combined));
        wrappedResponse.setGroups(wrappedService.analyzeGroups(groups, courses));

        ObjectMapper om = new ObjectMapper();
        String archetype = geminiService.promptArchetype(om.writeValueAsString(wrappedResponse));
        wrappedResponse.setArchetype(wrappedService.analyzeArchetype(archetype));

        return ResponseEntity.ok(wrappedResponse);
    }

}
