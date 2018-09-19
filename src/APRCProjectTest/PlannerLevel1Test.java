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

public class PlannerLevel1Test {
	
	private StudyPlannerInterface planner;

	@Before
	public void setUp() throws Exception {
		planner = new StudyPlanner();
	}

	@Test
	public void testStudyBlocksAlternate() {
		//Add topics.
		planner.addTopic("CSYA", 500);
		planner.addTopic("SWEN", 500);
		//Generate study plan.
		planner.generateStudyPlan();
		//Get study plan and assign to a new list instance.
		List<StudyBlockInterface> studyPlan = planner.getStudyPlan();
		//Assert that the study blocks alternate between topics.
		assertEquals("CSYA", studyPlan.get(0).getTopic());
		assertEquals("SWEN", studyPlan.get(1).getTopic());
		assertEquals("CSYA", studyPlan.get(2).getTopic());
	}
	
	@Test
	public void testStudyPlannerExceptionsAreThrown() {
		//Tests that a StudyPlannerException is thrown when client tries to
    	//generate a new study plan without any topics.
		try {
			planner.generateStudyPlan();
		}
		catch (StudyPlannerException e) {
			assertEquals("No topics in topic list.", e.getMessage());
		}
		//Tests that a StudyPlannerException is thrown when client tries to
    	//add two topics with the same subject name.
		try {
			planner.addTopic("CSYA", 500);
			planner.addTopic("CSYA", 500);
		}
		catch (StudyPlannerException e) {
			assertEquals("Topic name already exists.", e.getMessage());
		}
	}
	
	@Test
	public void testBlockLengths() {
		//Add a topic with a non-multiple of 60 duration.
		planner.addTopic("CSYA", 500);
		//Create a Calendar instance to hold start time.
		Calendar startTime = new GregorianCalendar(2016, 11, 10, 9, 0);
		//Generate a study plan using specified start time.
		planner.generateStudyPlan(startTime);
		//Get study plan and assign to a new list instance.
		List<StudyBlockInterface> studyPlan = planner.getStudyPlan();
		//Assert that the study block lengths are the default 60mins.
		assertEquals(60, studyPlan.get(0).getDuration());
		assertEquals(60, studyPlan.get(1).getDuration());
		//Assert that the final study block has a reduced duration.
		assertTrue(studyPlan.get(studyPlan.size() - 1).getDuration() < 60);
		//Set the block size to 40.
		planner.setBlockSize(40);
		//Generate a new study plan with this block size.
		planner.generateStudyPlan(startTime);
		//Assign this plan to studyPlan list.
		studyPlan = planner.getStudyPlan();
		//Assert that the study block now have this duration.
		assertEquals(40, studyPlan.get(0).getDuration());
		assertEquals(40, studyPlan.get(1).getDuration());
	}

}
