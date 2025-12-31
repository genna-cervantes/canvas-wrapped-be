package com.canvaswrapped.model;

import com.canvaswrapped.model.response.*;
import org.springframework.expression.spel.ast.Assign;

public class WrappedResponse {
    private Attendance attendance;
    private AssignmentResponse assignments;
    private AssignmentResponse quizzes;
    private HellWeek hellWeek;
    private DiscussionResponse discussion;
    private CourseResponse courses;
    private GroupResponse groups;
    private Archetype archetype;

    public Attendance getAttendance() {
        return attendance;
    }

    public void setAttendance(Attendance attendance) {
        this.attendance = attendance;
    }

    public AssignmentResponse getAssignments() {
        return assignments;
    }

    public void setAssignments(AssignmentResponse assignments) {
        this.assignments = assignments;
    }

    public AssignmentResponse getQuizzes() {
        return quizzes;
    }

    public void setQuizzes(AssignmentResponse quizzes) {
        this.quizzes = quizzes;
    }

    public HellWeek getHellWeek() {
        return hellWeek;
    }

    public void setHellWeek(HellWeek hellWeek) {
        this.hellWeek = hellWeek;
    }

    public DiscussionResponse getDiscussion() {
        return discussion;
    }

    public void setDiscussion(DiscussionResponse discussion) {
        this.discussion = discussion;
    }

    public CourseResponse getCourses() {
        return courses;
    }

    public void setCourses(CourseResponse courses) {
        this.courses = courses;
    }

    public GroupResponse getGroups() {
        return groups;
    }

    public void setGroups(GroupResponse groups) {
        this.groups = groups;
    }

    public Archetype getArchetype() {
        return archetype;
    }

    public void setArchetype(Archetype archetype) {
        this.archetype = archetype;
    }
}



