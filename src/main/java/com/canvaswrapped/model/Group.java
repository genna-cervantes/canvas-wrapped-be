package com.canvaswrapped.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Group(Long id, String name, @JsonProperty("course_id") Long courseId) { }
