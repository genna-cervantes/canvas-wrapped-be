package com.canvaswrapped.model.response;

import com.canvaswrapped.model.Assignment;

public record AssignmentResponse(int total, Double average, Assignment first, Assignment second, Assignment third, Assignment min){}
