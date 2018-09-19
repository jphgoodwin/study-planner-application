//Examination number: Y1466227

package APRCProjectTest;

import static org.junit.Assert.*;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import APRCProject.StudyBlockInterface;
import APRCProject.StudyPlanner;
import APRCProject.StudyPlannerException;
import APRCProject.StudyPlannerInterface;
import APRCProject.TopicInterface;
import APRCProject.StudyPlannerInterface.CalendarEventType;

public class PlannerLevel2Test {
	
	private StudyPlannerInterface planner;

	@Before
	public void setUp() throws Exception {
		planner = new StudyPlanner();
	}

	@Test
	public void testDailyStartAndEndTimes() {
		//Add topic.
		planner.addTopic("CSYA", 500);
		//Create a Calendar instance to hold start time.
		Calendar startTime = new GregorianCalendar(2016, 11, 10, 8, 0);
		//Assert that the default daily start and end times are 9:00 and 17:00, respectively.
		assertTrue(planner.getDailyStartStudyTime().get(Calendar.HOUR_OF_DAY) == 9
				&& planner.getDailyStartStudyTime().get(Calendar.MINUTE) == 0
				&& planner.getDailyEndStudyTime().get(Calendar.HOUR_OF_DAY) == 17
				&& planner.getDailyEndStudyTime().get(Calendar.MINUTE) == 0);
		//Generate study plan using specified start time.
		planner.generateStudyPlan(startTime);
		//Get study plan and assign to a new list instance.
		List<StudyBlockInterface> studyPlan = planner.getStudyPlan();
		//Assert that the first study block starts at the daily start time,
		//and that once the daily end time is reached, the next block begins on the next day.
		assertTrue(studyPlan.get(0).getStartTime().get(Calendar.HOUR_OF_DAY) == planner.getDailyStartStudyTime().get(Calendar.HOUR_OF_DAY)
				&& studyPlan.get(0).getStartTime().get(Calendar.MINUTE) == planner.getDailyStartStudyTime().get(Calendar.MINUTE)
				&& studyPlan.get(8).getStartTime().get(Calendar.HOUR_OF_DAY) == planner.getDailyStartStudyTime().get(Calendar.HOUR_OF_DAY)
				&& studyPlan.get(8).getStartTime().get(Calendar.MINUTE) == planner.getDailyStartStudyTime().get(Calendar.MINUTE));
		//Change the daily start time to 10:00.
		Calendar dailyStartTime = new GregorianCalendar(2016, 11, 10, 10, 0);
		planner.setDailyStartStudyTime(dailyStartTime);
		//Change the daily end time to 16:30.
		Calendar dailyEndTime = new GregorianCalendar(2016, 11, 10, 16, 30);
		planner.setDailyEndStudyTime(dailyEndTime);
		//Generate study plan using specified start time.
		planner.generateStudyPlan(startTime);
		//Get study plan and assign to a studyPlan list.
		studyPlan = planner.getStudyPlan();
		//Assert that the first study block now starts at 10:00.
		assertTrue(studyPlan.get(0).getStartTime().get(Calendar.HOUR_OF_DAY) == 10
				&& studyPlan.get(0).getStartTime().get(Calendar.MINUTE) == 0);
		//Assert that the last study block in the day has a duration of 30mins instead of 60mins.
		assertTrue(studyPlan.get(6).getDuration() == 30);
		//Set the start time to 3 minutes before the daily end time.
		startTime = new GregorianCalendar(2016, 11, 10, 16, 27);
		//Generate study plan using specified start time.
		planner.generateStudyPlan(startTime);
		//Get study plan and assign to a studyPlan list.
		studyPlan = planner.getStudyPlan();
		//Assert that first study block begins the following day with a duration of 60mins.
		assertTrue(studyPlan.get(0).getStartTime().get(Calendar.HOUR_OF_DAY) == 10
				&& studyPlan.get(0).getStartTime().get(Calendar.MINUTE) == 0
				&& studyPlan.get(0).getDuration() == 60);
	}
	
	@Test
	public void testEventHandling() {
		//Add topic.
		planner.addTopic("CSYA", 500);
		//Create a Calendar instance to hold start time.
		Calendar startTime = new GregorianCalendar(2016, 11, 10, 9, 0);
		//Add events to study planner.
		Calendar eventTime_1 = new GregorianCalendar(2016, 11, 10, 10, 0);
    	Calendar eventTime_2 = new GregorianCalendar(2016, 11, 10, 12, 0);
		planner.addCalendarEvent("event 1", eventTime_1, 90);
    	planner.addCalendarEvent("event 2", eventTime_2, 60);
    	//Assert that the events have been added to the study planner.
    	assertEquals("event 1", planner.getCalendarEvents().get(0).getName());
    	assertEquals("event 2", planner.getCalendarEvents().get(1).getName());
    	//Generate study plan using specified start time.
		planner.generateStudyPlan(startTime);
		//Get study plan and assign to a new list instance.
		List<StudyBlockInterface> studyPlan = planner.getStudyPlan();
    	//Assert that the study blocks work around the events.
		assertTrue(studyPlan.get(1).getStartTime().get(Calendar.HOUR_OF_DAY) == 11
				&& studyPlan.get(1).getStartTime().get(Calendar.MINUTE) == 30
				&& studyPlan.get(1).getDuration() == 30
				&& studyPlan.get(2).getStartTime().get(Calendar.HOUR_OF_DAY) == 13
				&& studyPlan.get(2).getStartTime().get(Calendar.MINUTE) == 00
				&& studyPlan.get(2).getDuration() == 60);
	}
	
	@Test
	public void testSetTargetEvent() {
		//Add topic.
		planner.addTopic("CSYA", 500);
    	//Add two calendar events, one of which is suitable as a target event.
    	Calendar eventTime_1 = new GregorianCalendar(2017, 1, 9, 9, 0);
    	Calendar eventTime_2 = new GregorianCalendar(2016, 11, 25, 13, 0);
    	planner.addCalendarEvent("CSYA Exam", eventTime_1, 90, CalendarEventType.EXAM);
    	planner.addCalendarEvent("Christmas dinner", eventTime_2, 120);
    	//Assert that a StudyPlannerException is thrown if getTargetEvent() is called
    	//with no target event set.
    	try {
    		planner.getTopics().get(0).getTargetEvent();
    	}
    	catch (StudyPlannerException e) {
    		assertEquals("No target event set.", e.getMessage());
    	}
    	//Set the CSYA exam as the target event for the topic.
    	planner.getTopics().get(0).setTargetEvent(planner.getCalendarEvents().get(0));
    	//Assert that the target event has been set.
    	assertEquals("CSYA Exam", planner.getTopics().get(0).getTargetEvent().getName());
    	//Assert that an exception is thrown if you try and set Christmas dinner as a target event.
    	try {
    		planner.getTopics().get(0).setTargetEvent(planner.getCalendarEvents().get(1));
    	}
    	catch (StudyPlannerException e) {
    		assertEquals("Event type is invalid as a topic target.", e.getMessage());
    	}
	}
}
