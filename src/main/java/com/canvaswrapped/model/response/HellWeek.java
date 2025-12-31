package com.canvaswrapped.model.response;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Map;

public record HellWeek(LocalDate start, DayOfWeek startDay, Map<DayOfWeek, Integer> days){}
