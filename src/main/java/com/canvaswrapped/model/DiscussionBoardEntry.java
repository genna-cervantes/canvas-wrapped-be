package com.canvaswrapped.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DiscussionBoardEntry(String message, @JsonProperty("parent_id") Long parentId) { }
