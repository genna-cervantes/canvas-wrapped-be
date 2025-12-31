package com.canvaswrapped.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Course (Long id, String name, @JsonProperty("course_code") String courseCode) { }
