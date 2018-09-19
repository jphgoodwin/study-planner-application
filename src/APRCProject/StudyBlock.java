//Examination number: Y1466227

package APRCProject;

import java.util.Calendar;

public class StudyBlock implements StudyBlockInterface {
	
	private String subject;
	private Calendar startTime;
	private int duration;
	
	public StudyBlock(String subject, Calendar startTime, int duration) {
		this.subject = subject;
		this.startTime = startTime;
		this.duration = duration;
	}

	@Override
	public String getTopic() {
		return this.subject;
	}

	@Override
	public Calendar getStartTime() {
		return this.startTime;
	}

	@Override
	public int getDuration() {
		return this.duration;
	}

}
