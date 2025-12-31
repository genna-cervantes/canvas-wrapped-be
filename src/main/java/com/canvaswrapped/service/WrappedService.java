package com.canvaswrapped.service;

import com.canvaswrapped.model.Assignment;
import com.canvaswrapped.model.Course;
import com.canvaswrapped.model.Group;
import com.canvaswrapped.model.response.*;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class WrappedService {

//    TODO sync with controller
    private final Pattern attendancePattern = Pattern.compile("(?i)\\b(roll call|attendance|rollcall)\\b");

    public AssignmentResponse analyzeAssignmentsOrQuizzes(ArrayList<Assignment> arr, ArrayList<Course> courses){

        Map<Long, Course> courseMap = courses.stream().collect(Collectors.toMap(Course::id, c -> c));
        int totalArrNum = 0;

        double totalScore = 0d;

        Assignment firstAs = null;
        Assignment secondAs = null;
        Assignment thirdAs = null;
        Assignment minAs = null;

        double min = 100;

        for (var as : arr){
            if (as.getSubmission() == null || as.getSubmission().score() == null || as.getPointsPossible() == null || as.getPointsPossible() == 0 ){
                continue;
            }

            totalArrNum++;

            double transformedScore = (as.getSubmission().score() / as.getPointsPossible()) * 100;
            totalScore += transformedScore;
            as.setTransformedScore(transformedScore);
            as.setCourse(courseMap.get(as.getCourseId()));

            if (transformedScore <= min){
                if (transformedScore < min){
                    min = transformedScore;
                    minAs = as;
                }else {
                    if (as.getPointsPossible() < (minAs != null ? minAs.getPointsPossible() : 9999)){
                        min = transformedScore;
                        minAs = as;
                    }
                }
            }

            if (transformedScore < (thirdAs != null ? thirdAs.getTransformedScore() : 0)){
                continue;
            }
            if (transformedScore < (secondAs != null ? secondAs.getTransformedScore() : 0)){
                if (transformedScore == (thirdAs != null ? thirdAs.getTransformedScore() : 0)){
                    if (as.getPointsPossible() > (thirdAs != null ? thirdAs.getPointsPossible() : 100)) {
                        thirdAs = as;
                    }
                }else{
                    thirdAs = as;
                }
                continue;
            }
            if (transformedScore < (firstAs != null ? firstAs.getTransformedScore() : 0)){
                if (transformedScore == (secondAs != null ? secondAs.getTransformedScore() : 0)){
                    if (as.getPointsPossible() > (secondAs != null ? secondAs.getPointsPossible() : 100)) {
                        Assignment tempAs = secondAs;
                        secondAs = as;
                        thirdAs = tempAs;
                    }
                }else{
                    Assignment tempAs = secondAs;
                    secondAs = as;
                    thirdAs = tempAs;
                }
                continue;
            }
            if (transformedScore == (firstAs != null ? firstAs.getTransformedScore() : 0)){
                if (as.getPointsPossible() > (firstAs != null ? firstAs.getPointsPossible() : 100)) {
                    Assignment tempAs = firstAs;
                    Assignment temp2As = secondAs;

                    firstAs = as;
                    secondAs = tempAs;
                    thirdAs = temp2As;
                }
            }else{
                Assignment tempAs = firstAs;
                Assignment temp2As = secondAs;

                firstAs = as;
                secondAs = tempAs;
                thirdAs = temp2As;
            }
        }

        Double average = totalArrNum == 0 ? null : totalScore/totalArrNum;

        return new AssignmentResponse(totalArrNum, average, firstAs, secondAs, thirdAs, minAs);
    }

    public DiscussionResponse analyzeDiscussionEntries(ArrayList<Assignment> entries){

        int totalPosts = 0;
        int totalReplies = 0;
        int totalWords = 0;

        for (var entry : entries){
            if (entry.getSubmission() == null || !entry.getSubmissionTypes().contains("discussion_topic")) continue;

            for (var dentry : entry.getSubmission().discussionEntries().orElse(List.of())){
                if (dentry.parentId() == null){
                    totalPosts++;
                }else{
                    totalReplies++;
                }

                String message = dentry.message() != null ? dentry.message() : "";
                int wordCount = (int) Arrays.stream(message.split("\\s+|\\\\n|<[^>]*>"))
                        .filter(s -> !s.isBlank())
                        .count();
                totalWords += wordCount;
            }
        }

        return new DiscussionResponse(totalPosts, totalReplies, totalWords);
    }

    public Attendance analyzeAttendance(ArrayList<Assignment> arr){

        double totalScore = 0;
        int count = 0;

        for (var as: arr){
            if (!attendancePattern.matcher(as.getName()).find() || as.getSubmission() == null || as.getSubmission().score() == null){
                continue;
            }
            double transformedScore = (as.getSubmission().score() / as.getPointsPossible()) * 100;
            totalScore += transformedScore;
            count++;
        }

        Double avg = count != 0 ? totalScore/count : null;

        return new Attendance(avg);
    }

    public CourseResponse analyzeCourses(ArrayList<Assignment> arr, ArrayList<Course> courses){

        Map<Long, Course> courseMap = courses.stream().collect(Collectors.toMap(Course::id, c -> c));

        Map<Long, Double> courseTotals = new HashMap<>();

        Map<Long, Double> courseScores = new HashMap<>();
        Map<Long, Integer> courseLength = new HashMap<>();

        for (var as : arr) {
            if (as.getSubmission() == null || as.getSubmission().score() == null || as.getPointsPossible() == null || as.getPointsPossible() == 0) {
                continue;
            }
            double transformedScore = (as.getSubmission().score() / as.getPointsPossible()) * 100;
            if (courseScores.containsKey(as.getCourseId())){
                double curr = courseScores.get(as.getCourseId());
                courseScores.replace(as.getCourseId(), curr+transformedScore);
                courseLength.replace(as.getCourseId(), courseLength.get(as.getCourseId())+1);
            }else{
                courseScores.put(as.getCourseId(), transformedScore);
                courseLength.put(as.getCourseId(), 1);
            }
        }

        for (var pair : courseScores.entrySet()){
            double agg = pair.getValue() / courseLength.get(pair.getKey());
            courseTotals.put(pair.getKey(), agg);
        }

        long best = 0;
        long worst = 0;

        double bestScore = 0;
        double worstScore = 100;

        for (var pair : courseTotals.entrySet()){
            if (pair.getValue() >= bestScore && courseMap.get(pair.getKey()) != null){
                if (pair.getValue() == bestScore){
                    if (courseMap.get(pair.getKey()).name().compareTo(courseMap.get(best).name()) < 0){
                        bestScore = pair.getValue();
                        best = pair.getKey();
                    }
                }else{
                    bestScore = pair.getValue();
                    best = pair.getKey();
                }
            }
            if (pair.getValue() <= worstScore && courseMap.get(pair.getKey()) != null){
                if (pair.getValue() == worstScore){
                    if (courseMap.get(pair.getKey()).name().compareTo(courseMap.get(worst).name()) > 0){
                        worstScore = pair.getValue();
                        worst = pair.getKey();
                    }
                }else{
                    worstScore = pair.getValue();
                    worst = pair.getKey();
                }
            }
        }

        return new CourseResponse(courseMap.get(best), courseMap.get(worst));
    }

    public HellWeek analyzeHellWeek(ArrayList<Assignment> arr){
        Map<LocalDate, Integer> weeks = new HashMap<>();
        Map<LocalDate, ArrayList<Assignment>> asPerWk = new HashMap<>();

        List<Assignment> safeArr = arr != null ? arr : List.of();
        for (var as : safeArr){
            if (as.getDueAt() == null) continue;

            Instant instant = Instant.parse(as.getDueAt());
            LocalDate closestSunday = closestSundayBefore(instant);

            if (weeks.containsKey(closestSunday)){
                weeks.put(closestSunday, weeks.get(closestSunday)+1);
                ArrayList<Assignment> currAs = asPerWk.get(closestSunday);
                currAs.add(as);
                asPerWk.put(closestSunday, currAs);
            }else{
                asPerWk.put(closestSunday, new ArrayList<>(List.of(as)));
                weeks.put(closestSunday, 1);
            }
        }

        LocalDate topSunday = null;
        Integer top = 0;
        for (var wk : weeks.entrySet()){
            if (wk.getValue() >= top){
                if (wk.getValue().equals(top)){
                    if (wk.getKey().isBefore(topSunday)){
                        top = wk.getValue();
                        topSunday = wk.getKey();
                    }
                }else{
                    top = wk.getValue();
                    topSunday = wk.getKey();
                }
            }
        }

        Map<DayOfWeek, Integer> hellWk = new HashMap<>();
        hellWk.put(DayOfWeek.SUNDAY, 0);
        hellWk.put(DayOfWeek.MONDAY, 0);
        hellWk.put(DayOfWeek.TUESDAY, 0);
        hellWk.put(DayOfWeek.WEDNESDAY, 0);
        hellWk.put(DayOfWeek.THURSDAY, 0);
        hellWk.put(DayOfWeek.FRIDAY, 0);
        hellWk.put(DayOfWeek.SATURDAY, 0);
        List<Assignment> safeAsPerWk = asPerWk.get(topSunday) != null ? asPerWk.get(topSunday) : List.of();
        for (var as : safeAsPerWk){
            Instant instant = Instant.parse(as.getDueAt());
            DayOfWeek dow = dayInWeek(instant);

            Integer curr = hellWk.get(dow);
            hellWk.replace(dow, curr+1);
        }

        return new HellWeek(topSunday, DayOfWeek.SUNDAY, hellWk);
    }

    public GroupResponse analyzeGroups(ArrayList<Group> groups, ArrayList<Course> courses){
        Set<Long> courseIds = courses.stream().map(Course::id).collect(Collectors.toSet());

        int groupCount = 0;
        for (var group: groups){
            if (group.courseId() == null) continue;

            if (courseIds.contains(group.courseId())){
                groupCount++;
            }
        }

        return new GroupResponse(groupCount);
    }

    public Archetype analyzeArchetype(String arch){
        ObjectMapper om = new ObjectMapper();

        String cleaned = arch.replaceAll("^[^{]*|[^}]*$", "");

        if (cleaned.isBlank()) return new Archetype(null, null);

        return om.readValue(cleaned, Archetype.class);
    }

    private DayOfWeek dayInWeek(Instant instant){
        return instant
                .atZone(ZoneId.of("Asia/Manila"))
                .toLocalDate()
                .getDayOfWeek();
    }

    private LocalDate closestSundayBefore(Instant instant){
        return instant
                .atZone(ZoneId.of("Asia/Manila"))
                .toLocalDate()
                .with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
    }

}
