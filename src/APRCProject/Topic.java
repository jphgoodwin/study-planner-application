//Examination number: Y1466227

package APRCProject;

public class Topic implements TopicInterface {
	
	private String subject;
	
	/**
	 * The duration of a topic is the cumulative amount of time that should be
	 * spent studying a topic. For example, a topic of duration 240 minutes
	 * would be completed by four study sessions of 60 minutes, eight of 30 
	 * minutes, etc. These sessions could take place in a single day, or be
	 * completed over multiple days.
	 */
	private int duration;
	
	private CalendarEventInterface targetEvent;
	
	public Topic(String name, int duration) {
		this.subject = name;
		this.duration = duration;
	}

	@Override
	public String getSubject() {
		return this.subject;
	}

	@Override
	public int getDuration() {
		return this.duration;
	}

	@Override
	public void setTargetEvent(CalendarEventInterface target) throws StudyPlannerException {
		if (!target.isValidTopicTarget()) {
			throw new StudyPlannerException("Event type is invalid as a topic target.");
		}
		else {
			this.targetEvent = target;
		}
	}

	@Override
	public CalendarEventInterface getTargetEvent() throws StudyPlannerException {
		if (this.targetEvent == null) {
			throw new StudyPlannerException("No target event set.");
		}
		else
			return this.targetEvent;
	}

}
