//Examination number: Y1466227

package APRCProject;

import java.util.Calendar;

import APRCProject.StudyPlannerInterface.CalendarEventType;

public class CalendarEvent implements CalendarEventInterface {
	
	private String name;
	private Calendar startTime;
	private int duration;
	private CalendarEventType type;
	private boolean validTopic;
	//javadoc type comment here.
	public CalendarEvent(String name, Calendar startTime, int duration) {
		this.name = name;
		this.startTime = startTime;
		this.duration = duration;
		this.type = CalendarEventType.OTHER;
		this.validTopic = false;
	}
	//javadoc type comment here.
	public CalendarEvent(String name, Calendar startTime, int duration, CalendarEventType type) {
		this.name = name;
		this.startTime = startTime;
		this.duration = duration;
		this.type = type;
		if (this.type == CalendarEventType.ESSAY || this.type == CalendarEventType.EXAM)
			this.validTopic = true;
		else
			this.validTopic = false;
	}
	
	@Override
	public String toString(){
		String output = new String();
		output = output.concat(this.name);
		output = output.concat("," + this.duration);
		output = output.concat("," + this.startTime.get(Calendar.YEAR) 
			+ "," + this.startTime.get(Calendar.MONTH)
			+ "," + this.startTime.get(Calendar.DATE)
			+ "," + this.startTime.get(Calendar.HOUR_OF_DAY)
			+ "," + this.startTime.get(Calendar.MINUTE));
		output = output.concat("," + this.type);
		return output;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Calendar getStartTime() {
		return this.startTime;
	}

	@Override
	public int getDuration() {
		return this.duration;
	}

	@Override
	public boolean isValidTopicTarget() {
		return this.validTopic;
	}

}
