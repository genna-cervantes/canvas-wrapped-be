package com.canvaswrapped.model.response;

import com.canvaswrapped.model.Assignment;

public record QuizResponse(int total, double average, Assignment max, Assignment min){}
