package com.canvaswrapped.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;

public class Assignment {
    private Long id;
    @JsonProperty("points_possible")
    private Double pointsPossible;
    private String name;
    @JsonProperty("due_at")
    private String dueAt;
    @JsonProperty("submission_types")
    private ArrayList<String> submissionTypes;
    @JsonProperty("course_id")
    private Long courseId;
    @JsonProperty("is_quiz_lti_assignment")
    private Boolean isQuizLtiAssignment;
    @JsonProperty("is_quiz_assignment")
    private Boolean isQuizAssignment;
    private Double transformedScore;
    private Submission submission;
    private Course course;

    public Assignment(Long id, Double pointsPossible, String name, String dueAt, ArrayList<String> submissionTypes, Long courseId, Boolean isQuizAssignment, Boolean isQuizLtiAssignment, Submission submission) {
        this.submission = submission;
        this.isQuizAssignment = isQuizAssignment;
        this.isQuizLtiAssignment = isQuizLtiAssignment;
        this.courseId = courseId;
        this.submissionTypes = submissionTypes;
        this.dueAt = dueAt;
        this.name = name;
        this.pointsPossible = pointsPossible;
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getPointsPossible() {
        return pointsPossible;
    }

    public void setPointsPossible(Double pointsPossible) {
        this.pointsPossible = pointsPossible;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDueAt() {
        return dueAt;
    }

    public void setDueAt(String dueAt) {
        this.dueAt = dueAt;
    }

    public ArrayList<String> getSubmissionTypes() {
        return submissionTypes;
    }

    public void setSubmissionTypes(ArrayList<String> submissionTypes) {
        this.submissionTypes = submissionTypes;
    }

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public Boolean getIsQuizLtiAssignment() {
        return isQuizLtiAssignment;
    }

    public void setQuizLtiAssignment(Boolean quizLtiAssignment) {
        isQuizLtiAssignment = quizLtiAssignment;
    }

    public Boolean getIsQuizAssignment() {
        return isQuizAssignment;
    }

    public void setQuizAssignment(Boolean quizAssignment) {
        isQuizAssignment = quizAssignment;
    }

    public Double getTransformedScore() {
        return transformedScore;
    }

    public void setTransformedScore(Double transformedScore) {
        this.transformedScore = transformedScore;
    }

    public Submission getSubmission() {
        return submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
}
//    determine quiz:
//    is_quiz_assignment
//    "submission_types": [
//            "external_tool"
//        ], && is_quiz_lti_assignment

//    determine discussion topic
//    "submission_types": [
//            "discussion_topic"
//            ],

//    groups
//    concluded & course_id
