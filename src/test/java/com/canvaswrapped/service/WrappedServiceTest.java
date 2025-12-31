package com.canvaswrapped.service;

import com.canvaswrapped.model.*;
import com.canvaswrapped.model.response.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@ExtendWith(MockitoExtension.class)
public class WrappedServiceTest {

    WrappedService wrappedService = new WrappedService();

    @Test
    void analyzeAssignmentsOrQuizzes_returnsCorrectStats_happyPath(){
        Double pointsPossible = 30d;
        Map<Long, Assignment> assignments = new HashMap<>();
        Map<Long, Double> scores = new HashMap<>();
        scores.put(1234L, 15d);
        scores.put(1235L, 20d);
        scores.put(1236L, 25d);
        scores.put(1237L, 30d);

        double average = scores.values()
                .stream()
                .mapToDouble(score -> score/pointsPossible) // score over points possible (avg over percentage)
                .average()
                .orElse(0d)
                * 100;

        for (var score: scores.entrySet()){
            assignments.put(score.getKey(), generateMockAssignment(score.getKey(), pointsPossible, score.getValue(), false));
        }

        ArrayList<Assignment> assignmentList = new ArrayList<>(assignments.values());
        AssignmentResponse ar = wrappedService.analyzeAssignmentsOrQuizzes(assignmentList, new ArrayList<>(List.of(new Course(1234L, "Test Course", "Test Course"))));

        Assertions.assertEquals(4, ar.total());
        Assertions.assertEquals(average, ar.average());
        Assertions.assertEquals(assignments.get(1237L), ar.first());
        Assertions.assertEquals(assignments.get(1236L), ar.second());
        Assertions.assertEquals(assignments.get(1235L), ar.third());
        Assertions.assertEquals(1234L, ar.first().getCourse().id());
        Assertions.assertEquals(assignments.get(1234L), ar.min());
    }

    @Test
    void analyzeAssignmentsOrQuizzes_handlesLessThanThreeValidItems(){

        Double pointsPossible = 30d;
        Map<Long, Assignment> assignments = new HashMap<>();
        Map<Long, Double> scores = new HashMap<>();
        scores.put(1235L, 20d);
        scores.put(1236L, 25d);

        double average = scores.values()
                .stream()
                .mapToDouble(score -> score/pointsPossible) // score over points possible (avg over percentage)
                .average()
                .orElse(0d)
                * 100;

        for (var score: scores.entrySet()){
            assignments.put(score.getKey(), generateMockAssignment(score.getKey(), pointsPossible, score.getValue(), false));
        }

        ArrayList<Assignment> assignmentList = new ArrayList<>(assignments.values());
        AssignmentResponse ar = wrappedService.analyzeAssignmentsOrQuizzes(assignmentList, new ArrayList<>(List.of(new Course(1234L, "Test Course", "Test Course"))));

        Assertions.assertEquals(2, ar.total());
        Assertions.assertEquals(average, ar.average());
        Assertions.assertEquals(assignments.get(1236L), ar.first());
        Assertions.assertEquals(assignments.get(1235L), ar.second());
        Assertions.assertNull(ar.third());
        Assertions.assertEquals(assignments.get(1235L), ar.min());
    }

    @Test
    void analyzeAssignmentsOrQuizzes_breaksTiesByHigherPointsPossible(){
        Map<Long, Assignment> assignments = new HashMap<>();
        Map<Long, Double> scores = new HashMap<>();
        scores.put(1235L, 20d);
        scores.put(1236L, 25d);
        scores.put(1237L, 30d);

        for (var score: scores.entrySet()){
            assignments.put(score.getKey(), generateMockAssignment(score.getKey(), score.getValue(), score.getValue(), false));
        }

        ArrayList<Assignment> assignmentList = new ArrayList<>(assignments.values());
        AssignmentResponse ar = wrappedService.analyzeAssignmentsOrQuizzes(assignmentList, new ArrayList<>(List.of(new Course(1234L, "Test Course", "Test Course"))));

        Assertions.assertEquals(assignments.get(1237L), ar.first());
        Assertions.assertEquals(assignments.get(1236L), ar.second());
        Assertions.assertEquals(assignments.get(1235L), ar.third());
        Assertions.assertEquals(assignments.get(1235L), ar.min());
    }

    @Test
    void analyzeAssignmentsOrQuizzes_ranksByPercentageNotRawPoints(){
        Map<Long, Assignment> assignments = new HashMap<>();
        Map<Long, Double> scores = new HashMap<>();
        scores.put(1235L, 9d);
        scores.put(1236L, 50d);
        Map<Long, Double> possiblePoints = new HashMap<>();
        possiblePoints.put(1235L, 10d);
        possiblePoints.put(1236L, 100d);

        for (var score: scores.entrySet()){
            assignments.put(score.getKey(), generateMockAssignment(score.getKey(), possiblePoints.get(score.getKey()), score.getValue(), false));
        }

        ArrayList<Assignment> assignmentList = new ArrayList<>(assignments.values());
        AssignmentResponse ar = wrappedService.analyzeAssignmentsOrQuizzes(assignmentList, new ArrayList<>(List.of(new Course(1234L, "Test Course", "Test Course"))));

        Assertions.assertEquals(assignments.get(1235L), ar.first());
        Assertions.assertEquals(assignments.get(1236L), ar.second());
        Assertions.assertNull(ar.third());
        Assertions.assertEquals(assignments.get(1236L), ar.min());
    }

    @Test
    void analyzeAssignmentsOrQuizzes_skipsInvalidItems_andUsesValidCountForAverage(){
        Map<Long, Assignment> assignments = new HashMap<>();
        Map<Long, Double> scores = new HashMap<>();
        scores.put(1235L, 15d);
        scores.put(1236L, 20d);
        scores.put(1237L, 25d);
        Map<Long, Double> possiblePoints = new HashMap<>();
        possiblePoints.put(1235L, 0d);
        possiblePoints.put(1236L, 30d);
        possiblePoints.put(1237L, 30d);

        double average = scores.entrySet()
                .stream()
                .filter(score -> score.getKey() != 1235L)
                .mapToDouble(score -> score.getValue()/possiblePoints.get(score.getKey()))
                .average()
                .orElse(0d)
                * 100;

        for (var score: scores.entrySet()){
            assignments.put(score.getKey(), generateMockAssignment(score.getKey(), possiblePoints.get(score.getKey()), score.getValue(), false));
        }

        ArrayList<Assignment> assignmentList = new ArrayList<>(assignments.values());
        AssignmentResponse ar = wrappedService.analyzeAssignmentsOrQuizzes(assignmentList, new ArrayList<>(List.of(new Course(1234L, "Test Course", "Test Course"))));

        Assertions.assertEquals(2, ar.total());
        Assertions.assertEquals(average, ar.average());
        Assertions.assertEquals(assignments.get(1237L), ar.first());
        Assertions.assertEquals(assignments.get(1236L), ar.second());
        Assertions.assertNull(ar.third());
        Assertions.assertEquals(assignments.get(1236L), ar.min());
    }

    @Test
    void analyzeAssignmentsOrQuizzes_allInvalid_returnsSafeResult(){
        int pointsPossible = 0;
        Map<Long, Assignment> assignments = new HashMap<>();
        Map<Long, Double> scores = new HashMap<>();
        scores.put(1235L, 10d);
        scores.put(1236L, 15d);
        scores.put(1237L, 20d);

        for (var score: scores.entrySet()){
            assignments.put(score.getKey(), generateMockAssignment(score.getKey(), pointsPossible, score.getValue(), false));
        }

        ArrayList<Assignment> assignmentList = new ArrayList<>(assignments.values());
        AssignmentResponse ar = wrappedService.analyzeAssignmentsOrQuizzes(assignmentList, new ArrayList<>(List.of(new Course(1234L, "Test Course", "Test Course"))));

        Assertions.assertEquals(0, ar.total());
        Assertions.assertNull(ar.average());
        Assertions.assertNull(ar.first());
        Assertions.assertNull(ar.second());
        Assertions.assertNull(ar.third());
        Assertions.assertNull(ar.min());
    }

    @Test
    void analyzeAssignmentsOrQuizzes_skipsWhenSubmissionIsNull(){

        double possiblePoints = 30d;
        Map<Long, Assignment> assignments = new HashMap<>();
        Map<Long, Double> scores = new HashMap<>();
        scores.put(1235L, 15d);
        scores.put(1236L, 20d);
        scores.put(1237L, 25d);

        double average = scores.entrySet()
                .stream()
                .filter(score -> score.getKey() != 1235L)
                .mapToDouble(score -> score.getValue()/possiblePoints)
                .average()
                .orElse(0d)
                * 100;

        for (var score: scores.entrySet()){
            assignments.put(score.getKey(), generateMockAssignment(score.getKey(), possiblePoints, score.getValue(), false));
        }

        Assignment nullSubmissionAssignment = assignments.get(1235L);
        nullSubmissionAssignment.setSubmission(null);
        assignments.replace(1235L, nullSubmissionAssignment);

        ArrayList<Assignment> assignmentList = new ArrayList<>(assignments.values());
        AssignmentResponse ar = wrappedService.analyzeAssignmentsOrQuizzes(assignmentList, new ArrayList<>(List.of(new Course(1234L, "Test Course", "Test Course"))));

        Assertions.assertEquals(2, ar.total());
        Assertions.assertEquals(average, ar.average());
        Assertions.assertEquals(assignments.get(1237L), ar.first());
        Assertions.assertEquals(assignments.get(1236L), ar.second());
        Assertions.assertNull(ar.third());
        Assertions.assertEquals(assignments.get(1236L), ar.min());
    }

    @Test
    void analyzeDiscussionEntries_countsPostsRepliesAndWords_happyPath(){
        Assignment as = generateMockAssignment(1234L, 30, 30, false);
        as.setSubmissionTypes(new ArrayList<>(List.of("discussion_topic")));

        DiscussionBoardEntry entry = new DiscussionBoardEntry("message", 1234L);
        DiscussionBoardEntry entry2 = new DiscussionBoardEntry("two messages", null);

        as.setSubmission(new Submission(
                1234L,
                30d,
                Optional.of(new ArrayList<>(List.of(entry, entry2))))
        );

        DiscussionResponse dr = wrappedService.analyzeDiscussionEntries(new ArrayList<>(List.of(as)));

        Assertions.assertEquals(1, dr.posts());
        Assertions.assertEquals(1, dr.replies());
        Assertions.assertEquals(3, dr.words());
    }

    @Test
    void analyzeDiscussionEntries_skipsWhenNonDiscussionSubmission(){
        Assignment as = generateMockAssignment(1234L, 30, 30, false);
        DiscussionBoardEntry entry = new DiscussionBoardEntry("message", 1234L);
        as.setSubmission(new Submission(
                1234L,
                30d,
                Optional.of(new ArrayList<>(List.of(entry))))
        );

        DiscussionResponse dr = wrappedService.analyzeDiscussionEntries(new ArrayList<>(List.of(as)));

        Assertions.assertEquals(0, dr.posts());
        Assertions.assertEquals(0, dr.replies());
        Assertions.assertEquals(0, dr.words());
    }

    @Test
    void analyzeDiscussionEntries_skipsWhenSubmissionIsNull(){
        Assignment as = generateMockAssignment(1234L, 30, 30, false);
        as.setSubmission(null);

        DiscussionResponse dr = wrappedService.analyzeDiscussionEntries(new ArrayList<>(List.of(as)));

        Assertions.assertEquals(0, dr.posts());
        Assertions.assertEquals(0, dr.replies());
        Assertions.assertEquals(0, dr.words());
    }

    @Test
    void analyzeDiscussionEntries_handlesEmptyList(){
        Assignment as = generateMockAssignment(1234L, 30, 30, false);
        as.setSubmission(new Submission(
                1234L,
                30d,
                Optional.of(new ArrayList<>())
        ));

        DiscussionResponse dr = wrappedService.analyzeDiscussionEntries(new ArrayList<>(List.of(as)));

        Assertions.assertEquals(0, dr.posts());
        Assertions.assertEquals(0, dr.replies());
        Assertions.assertEquals(0, dr.words());
    }

    @Test
    void analyzeDiscussionEntries_aggregatesAcrossMultipleAssignments(){
        Assignment as = generateMockAssignment(1234L, 30, 30, false);
        as.setSubmissionTypes(new ArrayList<>(List.of("discussion_topic")));
        Assignment as2 = generateMockAssignment(1235L, 30, 30, false);
        as2.setSubmissionTypes(new ArrayList<>(List.of("discussion_topic")));

        DiscussionBoardEntry entry = new DiscussionBoardEntry("message", 1234L);
        DiscussionBoardEntry entry2 = new DiscussionBoardEntry("two messages", null);

        as.setSubmission(new Submission(
                1234L,
                30d,
                Optional.of(new ArrayList<>(List.of(entry, entry2))))
        );
        as2.setSubmission(new Submission(
                1234L,
                30d,
                Optional.of(new ArrayList<>(List.of(entry))))
        );

        DiscussionResponse dr = wrappedService.analyzeDiscussionEntries(new ArrayList<>(List.of(as, as2)));

        Assertions.assertEquals(1, dr.posts());
        Assertions.assertEquals(2, dr.replies());
        Assertions.assertEquals(4, dr.words());
    }

    @Test
    void analyzeDiscussionEntries_handlesNullOrBlankMessage(){
        Assignment as = generateMockAssignment(1234L, 30, 30, false);
        as.setSubmissionTypes(new ArrayList<>(List.of("discussion_topic")));

        DiscussionBoardEntry entry = new DiscussionBoardEntry(null, 1234L);
        DiscussionBoardEntry entry2 = new DiscussionBoardEntry("", null);

        as.setSubmission(new Submission(
                1234L,
                30d,
                Optional.of(new ArrayList<>(List.of(entry, entry2))))
        );

        DiscussionResponse dr = wrappedService.analyzeDiscussionEntries(new ArrayList<>(List.of(as)));

        Assertions.assertEquals(1, dr.posts());
        Assertions.assertEquals(1, dr.replies());
        Assertions.assertEquals(0, dr.words());
    }

    @Test
    void analyzeDiscussionEntries_handlesHTMLMessage(){
        Assignment as = generateMockAssignment(1234L, 30, 30, false);
        as.setSubmissionTypes(new ArrayList<>(List.of("discussion_topic")));
        DiscussionBoardEntry entry = new DiscussionBoardEntry("""
                <p>Using the Exploratory Data Analysis, improve the model's accuracy in forecasting for the 'Power Losses' feature. Do not change the machine model parameters.</p>\\n<p>
                """, 1234L);

        as.setSubmission(new Submission(
                1234L,
                30d,
                Optional.of(new ArrayList<>(List.of(entry))))
        );

        DiscussionResponse dr = wrappedService.analyzeDiscussionEntries(new ArrayList<>(List.of(as)));

        Assertions.assertEquals(0, dr.posts());
        Assertions.assertEquals(1, dr.replies());
        Assertions.assertEquals(23, dr.words());
    }

    @Test
    void analyzeAttendance_correctStats_happyPath(){
        Assignment as = generateMockAssignment(1234L, 100, 80, false);
        as.setName("CS1250 Attendance");

        Attendance att = wrappedService.analyzeAttendance(new ArrayList<>(List.of(as)));

        Assertions.assertEquals(80, att.average());
    }

    @Test
    void analyzeAttendance_skipsNonAttendanceAssignments(){
        Assignment as = generateMockAssignment(1234L, 100, 80, false);

        Attendance att = wrappedService.analyzeAttendance(new ArrayList<>(List.of(as)));

        Assertions.assertNull(att.average());
    }

    @Test
    void analyzeAttendance_aggregatesAcrossAssignments(){
        Assignment as = generateMockAssignment(1234L, 100, 80, false);
        as.setName("CS1250 Attendance");
        Assignment as2 = generateMockAssignment(1235L, 100, 50, false);
        as2.setName("CS1230 Attendance");

        Attendance att = wrappedService.analyzeAttendance(new ArrayList<>(List.of(as, as2)));

        Assertions.assertEquals(65, att.average());
    }

    @Test
    void analyzeAttendance_skipsNullSubmissionAndNullSubmissionScore(){
        Assignment as = generateMockAssignment(1234L, 100, 80, false);
        as.setName("CS1250 Attendance");
        as.setSubmission(null);
        Assignment as2 = generateMockAssignment(1235L, 100, 50, false);
        as2.setName("CS1230 Attendance");
        as2.setSubmission(new Submission(
                1234L,
                null,
                null
        ));

        Attendance att = wrappedService.analyzeAttendance(new ArrayList<>(List.of(as, as2)));

        Assertions.assertNull(att.average());
    }

    @Test
    void analyzeAttendance_returnsOnEmptyArray(){
        Attendance att = wrappedService.analyzeAttendance(new ArrayList<>(List.of()));
        Assertions.assertNull(att.average());
    }

    @Test
    void analyzeCourses_returnsBestAndWorst_basedOnPerCourseAverage(){
        Course course1 = new Course(1234L, "CS1234", "CS1234");
        Assignment course1As1 = generateMockAssignment(1234L, 100, 80, false);
        Assignment course1As2 = generateMockAssignment(1235L, 100, 100, false);
        course1As1.setCourseId(1234L);
        course1As2.setCourseId(1234L);

        Course course2 = new Course(1235L, "CS1235", "CS1235");
        Assignment course2As1 = generateMockAssignment(1236L, 100, 95, false);
        course2As1.setCourseId(1235L);

        CourseResponse cr = wrappedService.analyzeCourses(new ArrayList<>(List.of(course1As1, course1As2, course2As1)),
                new ArrayList<>(List.of(course1, course2)));

        Assertions.assertEquals(course2, cr.max());
        Assertions.assertEquals(course1, cr.min());
    }

    @Test
    void analyzeCourses_handlesCourseIdNotInCoursesList_chooseNextBestExisting(){
        Assignment As1 = generateMockAssignment(1234L, 100, 100, false);
        As1.setCourseId(1234L);

        Course course1 = new Course(1235L, "CS1235", "CS1235");
        Assignment course1As1 = generateMockAssignment(1236L, 100, 95, false);
        course1As1.setCourseId(1235L);

        Course course2 = new Course(1236L, "CS1235", "CS1235");
        Assignment course2As1 = generateMockAssignment(1236L, 100, 90, false);
        course2As1.setCourseId(1236L);

        CourseResponse cr = wrappedService.analyzeCourses(new ArrayList<>(List.of(As1, course1As1, course2As1)),
                new ArrayList<>(List.of(course1, course2)));

        Assertions.assertEquals(course1, cr.max());
        Assertions.assertEquals(course2, cr.min());
    }

    @Test
    void analyzeCourses_singleCourseOnly_bestAndWorstAreSameCourse(){
        Course course1 = new Course(1235L, "CS1235", "CS1235");
        Assignment course1As1 = generateMockAssignment(1236L, 100, 95, false);
        course1As1.setCourseId(1235L);

        CourseResponse cr = wrappedService.analyzeCourses(new ArrayList<>(List.of(course1As1)),
                new ArrayList<>(List.of(course1)));

        Assertions.assertEquals(course1, cr.max());
        Assertions.assertEquals(course1, cr.min());
    }

    @Test
    void analyzeCourses_skipsInvalidAssignments_doesNotAffectCourseAverages(){
        Course course1 = new Course(1234L, "CS1234", "CS1234");
        Assignment course1As1 = generateMockAssignment(1234L, 0, 100, false);
        Assignment course1As2 = generateMockAssignment(1235L, 100, 80, false);
        course1As1.setCourseId(1234L);
        course1As2.setCourseId(1234L);

        Course course2 = new Course(1235L, "CS1235", "CS1235");
        Assignment course2As1 = generateMockAssignment(1236L, 100, 95, false);
        course2As1.setCourseId(1235L);

        CourseResponse cr = wrappedService.analyzeCourses(new ArrayList<>(List.of(course1As1, course1As2, course2As1)),
                new ArrayList<>(List.of(course1, course2)));

        Assertions.assertEquals(course2, cr.max());
        Assertions.assertEquals(course1, cr.min());
    }

    @Test
    void analyzeCourses_emptyAssignments_returnsNulls(){
        Course course1 = new Course(1234L, "CS1234", "CS1234");
        Course course2 = new Course(1235L, "CS1235", "CS1235");

        CourseResponse cr = wrappedService.analyzeCourses(new ArrayList<>(List.of()),
                new ArrayList<>(List.of(course1, course2)));

        Assertions.assertNull(cr.max());
        Assertions.assertNull(cr.min());
    }

    @Test
    void analyzeCourses_breakTiesAlphabetically(){
        Course course1 = new Course(1234L, "ABC", "CS1234");
        Assignment course1As1 = generateMockAssignment(1234L, 100, 80, false);
        course1As1.setCourseId(1234L);

        Course course2 = new Course(1235L, "BCD", "CS1235");
        Assignment course2As1 = generateMockAssignment(1236L, 100, 80, false);
        course2As1.setCourseId(1235L);

        CourseResponse cr = wrappedService.analyzeCourses(new ArrayList<>(List.of(course1As1, course2As1)),
                new ArrayList<>(List.of(course1, course2)));

        Assertions.assertEquals(course1, cr.max());
        Assertions.assertEquals(course2, cr.min());
    }

    @Test
    void analyzeHellWeek_groupsAssignmentsIntoCorrectWeek_andCorrectCountsPerDayOfWeek(){
        Assignment as1 = generateMockAssignment(1234L, 100, 80, false);
        as1.setDueAt("2025-12-22T12:00:00Z");
        Assignment as2 = generateMockAssignment(1235L, 100, 80, false);
        as2.setDueAt("2025-12-25T12:00:00Z");
        Assignment as3 = generateMockAssignment(1236L, 100, 80, false);
        as3.setDueAt("2025-12-13T12:00:00Z");

        HellWeek hw = wrappedService.analyzeHellWeek(new ArrayList<>(List.of(as1, as2, as3)));

        LocalDate start = Instant.parse("2025-12-21T12:00:00Z")
                .atZone(ZoneId.of("Asia/Manila"))
                .toLocalDate();

        Assertions.assertEquals(start, hw.start());
        Assertions.assertEquals(DayOfWeek.SUNDAY, hw.startDay());
        Assertions.assertEquals(new HashMap<>(Map.of(
                DayOfWeek.SUNDAY, 0,
                DayOfWeek.MONDAY, 1,
                DayOfWeek.TUESDAY, 0,
                DayOfWeek.WEDNESDAY, 0,
                DayOfWeek.THURSDAY, 1,
                DayOfWeek.FRIDAY, 0,
                DayOfWeek.SATURDAY, 0
        )), hw.days());
    }

    @Test
    void analyzeHellWeek_skipsNullDueAt(){
        Assignment as1 = generateMockAssignment(1234L, 100, 80, false);
        as1.setDueAt("2025-12-22T12:00:00Z");
        Assignment as2 = generateMockAssignment(1235L, 100, 80, false);
        as2.setDueAt("2025-12-25T12:00:00Z");
        Assignment as3 = generateMockAssignment(1236L, 100, 80, false);
        as3.setDueAt("2025-12-13T12:00:00Z");
        Assignment as4 = generateMockAssignment(1236L, 100, 80, false);
        as4.setDueAt(null);

        HellWeek hw = wrappedService.analyzeHellWeek(new ArrayList<>(List.of(as1, as2, as3)));

        LocalDate start = Instant.parse("2025-12-21T12:00:00Z")
                .atZone(ZoneId.of("Asia/Manila"))
                .toLocalDate();

        Assertions.assertEquals(start, hw.start());
        Assertions.assertEquals(DayOfWeek.SUNDAY, hw.startDay());
        Assertions.assertEquals(new HashMap<>(Map.of(
                DayOfWeek.SUNDAY, 0,
                DayOfWeek.MONDAY, 1,
                DayOfWeek.TUESDAY, 0,
                DayOfWeek.WEDNESDAY, 0,
                DayOfWeek.THURSDAY, 1,
                DayOfWeek.FRIDAY, 0,
                DayOfWeek.SATURDAY, 0
        )), hw.days());
    }

    @Test
    void analyzeHellWeek_returnWhenEmpty(){
        HellWeek hw = wrappedService.analyzeHellWeek(new ArrayList<>(List.of()));

        Assertions.assertNull(hw.start());
        Assertions.assertEquals(DayOfWeek.SUNDAY, hw.startDay());
        Assertions.assertEquals(new HashMap<>(Map.of(
                DayOfWeek.SUNDAY, 0,
                DayOfWeek.MONDAY, 0,
                DayOfWeek.TUESDAY, 0,
                DayOfWeek.WEDNESDAY, 0,
                DayOfWeek.THURSDAY, 0,
                DayOfWeek.FRIDAY, 0,
                DayOfWeek.SATURDAY, 0
        )), hw.days());
    }

    @Test
    void analyzeHellWeek_tieBetweenWeeks_chooseEarlierWeek(){
        Assignment as1 = generateMockAssignment(1234L, 100, 80, false);
        as1.setDueAt("2025-12-22T12:00:00Z");
        Assignment as2 = generateMockAssignment(1235L, 100, 80, false);
        as2.setDueAt("2025-12-25T12:00:00Z");
        Assignment as3 = generateMockAssignment(1236L, 100, 80, false);
        as3.setDueAt("2025-12-13T12:00:00Z");
        Assignment as4 = generateMockAssignment(1237L, 100, 80, false);
        as4.setDueAt("2025-12-12T12:00:00Z");

        HellWeek hw = wrappedService.analyzeHellWeek(new ArrayList<>(List.of(as1, as2, as3, as4)));

        LocalDate start = Instant.parse("2025-12-07T12:00:00Z")
                .atZone(ZoneId.of("Asia/Manila"))
                .toLocalDate();

        Assertions.assertEquals(start, hw.start());
        Assertions.assertEquals(DayOfWeek.SUNDAY, hw.startDay());
        Assertions.assertEquals(new HashMap<>(Map.of(
                DayOfWeek.SUNDAY, 0,
                DayOfWeek.MONDAY, 0,
                DayOfWeek.TUESDAY, 0,
                DayOfWeek.WEDNESDAY, 0,
                DayOfWeek.THURSDAY, 0,
                DayOfWeek.FRIDAY, 1,
                DayOfWeek.SATURDAY, 1
        )), hw.days());
    }

    @Test
    void analyzeHellWeek_handlesISOZTimestamp_correctDayOfWeek(){
        Assignment as1 = generateMockAssignment(1234L, 100, 80, false);
        as1.setDueAt("2025-08-31T16:30:00Z");

        HellWeek hw = wrappedService.analyzeHellWeek(new ArrayList<>(List.of(as1)));

        LocalDate start = Instant.parse("2025-08-31T12:00:00Z")
                .atZone(ZoneId.of("Asia/Manila"))
                .toLocalDate();

        Assertions.assertEquals(start, hw.start());
        Assertions.assertEquals(DayOfWeek.SUNDAY, hw.startDay());
        Assertions.assertEquals(new HashMap<>(Map.of(
                DayOfWeek.SUNDAY, 0,
                DayOfWeek.MONDAY, 1,
                DayOfWeek.TUESDAY, 0,
                DayOfWeek.WEDNESDAY, 0,
                DayOfWeek.THURSDAY, 0,
                DayOfWeek.FRIDAY, 0,
                DayOfWeek.SATURDAY, 0
        )), hw.days());
    }

    @Test
    void analyzeGroups_returnsCorrectCount_happyPath(){
        Course course = new Course(1234L, "Course 1", "Course 1");
        Group gr1 = new Group(1234L, "Group 1", 1234L);
        Group gr2 = new Group(1235L, "Group 2", 1234L);

        GroupResponse gr = wrappedService.analyzeGroups(new ArrayList<>(List.of(gr1, gr2)), new ArrayList<>(List.of(course)));

        Assertions.assertEquals(2, gr.total());
    }

    @Test
    void analyzeGroups_aggregatesAcrossDifferentCourses(){
        Course course1 = new Course(1234L, "Course 1", "Course 1");
        Course course2 = new Course(1235L, "Course 2", "Course 2");
        Group gr1 = new Group(1234L, "Group 1", 1234L);
        Group gr2 = new Group(1235L, "Group 2", 1235L);

        GroupResponse gr = wrappedService.analyzeGroups(new ArrayList<>(List.of(gr1, gr2)), new ArrayList<>(List.of(course1, course2)));

        Assertions.assertEquals(2, gr.total());
    }

    @Test
    void analyzeGroups_returnsOnEmptyGroups(){
        Course course1 = new Course(1234L, "Course 1", "Course 1");
        Course course2 = new Course(1235L, "Course 2", "Course 2");

        GroupResponse gr = wrappedService.analyzeGroups(new ArrayList<>(List.of()), new ArrayList<>(List.of(course1, course2)));

        Assertions.assertEquals(0, gr.total());
    }

    @Test
    void analyzeGroups_returnsOnEmptyCourses(){
        Group gr1 = new Group(1234L, "Group 1", 1234L);
        Group gr2 = new Group(1235L, "Group 2", 1235L);

        GroupResponse gr = wrappedService.analyzeGroups(new ArrayList<>(List.of(gr1, gr2)), new ArrayList<>(List.of()));

        Assertions.assertEquals(0, gr.total());
    }

    @Test
    void analyzeGroups_skipsNullCourseId(){
        Course course1 = new Course(1234L, "Course 1", "Course 1");
        Group gr1 = new Group(1234L, "Group 1", 1234L);
        Group gr2 = new Group(1235L, "Group 2", null);

        GroupResponse gr = wrappedService.analyzeGroups(new ArrayList<>(List.of(gr1, gr2)), new ArrayList<>(List.of(course1)));

        Assertions.assertEquals(1, gr.total());
    }

    @Test
    void analyzeArchetype_correctTransformation_happyPath(){
        String str = "{\"type\": \"The Steady Navigator\",\"message\": \"Keep shining brightly! Your consistent effort and positive energy are always valuable.\"}";

        Archetype ar = wrappedService.analyzeArchetype(str);

        Assertions.assertEquals("The Steady Navigator", ar.type());
        Assertions.assertEquals("Keep shining brightly! Your consistent effort and positive energy are always valuable.", ar.message());
    }

    @Test
    void analyzeArchetype_returnsNullOnEmptyString(){
        String str = "";

        Archetype ar = wrappedService.analyzeArchetype(str);

        Assertions.assertNull(ar.type());
        Assertions.assertNull(ar.message());
    }

    @Test
    void analyzeArchetype_returnWhenOnlyOneValidField(){
        String str = "{\"type\": \"The Steady Navigator\"}";

        Archetype ar = wrappedService.analyzeArchetype(str);

        Assertions.assertEquals("The Steady Navigator", ar.type());
        Assertions.assertNull(ar.message());
    }

    @Test
    void analyzeArchetype_stringifyNonStringField(){
        String str = "{\"type\": \"The Steady Navigator\",\"message\": 123}";

        Archetype ar = wrappedService.analyzeArchetype(str);

        Assertions.assertEquals("The Steady Navigator", ar.type());
        Assertions.assertEquals("123", ar.message());
    }

    @Test
    void analyzeArchetype_handleInvalidJsonString(){
        String str = "123{\"type\": \"The Steady Navigator\",\"message\": \"Keep shining brightly! Your consistent effort and positive energy are always valuable.\"}//";

        Archetype ar = wrappedService.analyzeArchetype(str);

        Assertions.assertEquals("The Steady Navigator", ar.type());
        Assertions.assertEquals("Keep shining brightly! Your consistent effort and positive energy are always valuable.", ar.message());
    }


    static Assignment generateMockAssignment(long id, double pointsPossible, double score, boolean isQuiz){
        return new Assignment(id,
            pointsPossible,
            "Test Assignment %d".formatted(id),
            new Date().toString(),
            new ArrayList<>(List.of("file_upload")),
            1234L,
            isQuiz,
            isQuiz,
            new Submission(
                1234L,
                score,
                null)
        );
    }

}
