package com.canvaswrapped.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

public record Submission (Long id, Double score, @JsonProperty("discussion_entries") Optional<List<DiscussionBoardEntry>> discussionEntries) { }
    //    Submission
//    assignment & quiz
//    id
//    score

//    discussion
//    discussion_entries [] -> {message, parentId}
