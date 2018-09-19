//Examination number: Y1466227

package APRCProject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Scanner;

public class StudyPlanner implements StudyPlannerInterface {
	//Member fields.
	//ArrayLists that hold the list of Topics, list of StudyBlocks and list of events, respectively.
	private StudyList<TopicInterface> topics = new StudyList<TopicInterface>();
	private StudyList<StudyBlockInterface> plan = new StudyList<StudyBlockInterface>();
	private StudyList<CalendarEventInterface> events = new StudyList<CalendarEventInterface>();
	
	//Field that holds the designated gui for the system.
	private StudyPlannerGUIInterface gui;
	
	//Calendar fields.
	private StudyCalendar dailyStartTime;
	private StudyCalendar dailyEndTime;
	
	//StudyBlock fields.
	private int blockSize = 60;
	private int breakLength = 0;
	
	//Generate study block fields.
	private int variableBlockSize;
	private int variableBreakLength;
	
	//Constructor.
	public StudyPlanner() {
		this.dailyStartTime = new StudyCalendar();
		this.dailyStartTime.set(Calendar.HOUR_OF_DAY, 9);
		this.dailyStartTime.set(Calendar.MINUTE, 0);
		this.dailyEndTime = new StudyCalendar();
		this.dailyEndTime.set(Calendar.HOUR_OF_DAY, 17);
		this.dailyEndTime.set(Calendar.MINUTE, 0);
	}
	//Inner class that extends GregorianCalendar, overriding the clone method
	//to allow deep copying of class instance.
	private class StudyCalendar extends GregorianCalendar {
		
		public StudyCalendar() {
			super();
		}
		public StudyCalendar(int year, int month, int date, int hour, int minute) {
			super(year, month, date, hour, minute);
		}
		
		@Override
		public String toString() {
			String output = new String();
			output = output.concat(String.valueOf(this.get(Calendar.YEAR)));
			output = output.concat("," + this.get(Calendar.MONTH));
			output = output.concat("," + this.get(Calendar.DATE));
			output = output.concat("," + this.get(Calendar.HOUR_OF_DAY));
			output = output.concat("," + this.get(Calendar.MINUTE));
			return output;
		}
		
		@Override
		public StudyCalendar clone() {
			StudyCalendar output = new StudyCalendar(this.get(Calendar.YEAR), this.get(Calendar.MONTH), 
					this.get(Calendar.DATE), this.get(Calendar.HOUR_OF_DAY), this.get(Calendar.MINUTE));
			return output;
		}
	}
	//Inner class that extends ArrayList<E>, overriding the clone method to allow
	//deep copying of class instance.
	private class StudyList<E> extends ArrayList<E> {
		public StudyList() {
			super();
		}
		
		@Override
		public StudyList<E> clone() {
			StudyList<E> output = new StudyList<E>();
			for (E e : this) {
				output.add(e);
			}
			return output;
		}
	}
	//Method that checks that a study block or break won't clash with any events in event list.
	//Returns -1 if no clash is found, and if a clash is found the event index is returned.
	private int checkEventClash(final Calendar currentTime, final int duration) {
		int eventIndex = -1;
		for (int i=0; i < this.events.size(); ++i) {
			if (currentTime.get(Calendar.YEAR) == this.events.get(i).getStartTime().get(Calendar.YEAR) 
					&& currentTime.get(Calendar.MONTH) == this.events.get(i).getStartTime().get(Calendar.MONTH) 
					&& currentTime.get(Calendar.DATE) == this.events.get(i).getStartTime().get(Calendar.DATE) 
					&& currentTime.get(Calendar.HOUR_OF_DAY)*60 + currentTime.get(Calendar.MINUTE) + duration 
					> this.events.get(i).getStartTime().get(Calendar.HOUR_OF_DAY)*60 + this.events.get(i).getStartTime().get(Calendar.MINUTE)
					) {
				if (currentTime.get(Calendar.HOUR_OF_DAY)*60 + currentTime.get(Calendar.MINUTE) 
					< this.events.get(i).getStartTime().get(Calendar.HOUR_OF_DAY)*60 + this.events.get(i).getStartTime().get(Calendar.MINUTE)
					+ this.events.get(i).getDuration()) {
					eventIndex = i;
				}
			}
		}
		return eventIndex;
	}
	//Method that checks that a study block or break doesn't extend beyond the end of the day.
	//Returns 1 if day's end time is exceeded, and -1 if it isn't.
	private int checkDayExceeded(final Calendar currentTime, final int duration) {
		if (currentTime.get(Calendar.HOUR_OF_DAY)*60 + currentTime.get(Calendar.MINUTE) + duration 
				> this.dailyEndTime.get(Calendar.HOUR_OF_DAY)*60 + this.dailyEndTime.get(Calendar.MINUTE)) {
			return 1;
		}
		else {
			return -1;
		}
	}
	//Method that returns the difference in time in minutes between a given time and event.
	private int getReducedDuration(final Calendar currentTime, final Calendar event) {
		return event.get(Calendar.HOUR_OF_DAY)*60 + event.get(Calendar.MINUTE) 
		- (currentTime.get(Calendar.HOUR_OF_DAY)*60 + currentTime.get(Calendar.MINUTE));
	}
	
	private int getReducedBreakLength(final Calendar currentTime, final Calendar event, int block) {
		return event.get(Calendar.HOUR_OF_DAY)*60 + event.get(Calendar.MINUTE) 
		- (currentTime.get(Calendar.HOUR_OF_DAY)*60 + currentTime.get(Calendar.MINUTE) + block);
	}
	//Method that rolls on the time of a given Calendar object by a given amount.
	private void rollOnTime(Calendar time, int amount) {
		if (amount/60 + time.get(Calendar.HOUR_OF_DAY) + time.get(Calendar.MINUTE)/60 > 24) {
			time.roll(Calendar.DATE, 1);
		}
		if (amount + time.get(Calendar.MINUTE) >= 60) {
			time.roll(Calendar.HOUR_OF_DAY, (time.get(Calendar.MINUTE) + amount)/60);
		}
		time.roll(Calendar.MINUTE, amount);
	}
	//Method that rolls on the time to the study start time of the next day.
	private void rollOnDay(Calendar time) {
		time.roll(Calendar.DATE, 1);
		time.set(Calendar.HOUR_OF_DAY, this.dailyStartTime.get(Calendar.HOUR_OF_DAY));
		time.set(Calendar.MINUTE, this.dailyStartTime.get(Calendar.MINUTE));
	}
	//Method that handles a clash between a study block and Calendar event.
	//If the study block can be shortened without reducing the duration below 10 minutes,
	//the method does this. If not the method rolls on the time to after the event. It also
	//alters the break length accordingly.
	private void handleEventClash(Calendar currentTime, final CalendarEventInterface event, int maxDuration) {
		int reducedDuration = this.getReducedDuration(currentTime, event.getStartTime());
		if (reducedDuration < 10) {
			this.rollOnTime(currentTime, reducedDuration + event.getDuration());
			this.variableBlockSize = maxDuration;
			this.variableBreakLength = this.breakLength;
		}
		else {
			this.variableBlockSize = reducedDuration;
			this.variableBreakLength = 0;
		}
	}
	//Method that handles the case in which the study block exceeds the end of the study day.
	//If the study block can be shortened without reducing the duration below 10 minutes,
	//the method does this. If not the method rolls on to the start of the next study day.
	//It also alters the break length accordingly.
	private void handleDayExceeded(Calendar currentTime, int maxDuration) {
		int reducedDuration = this.getReducedDuration(currentTime, this.dailyEndTime);
		if (reducedDuration < 10) {
			this.rollOnDay(currentTime);
			this.variableBlockSize = maxDuration;
			this.variableBreakLength = this.breakLength;
		}
		else {
			this.variableBlockSize = reducedDuration;
			this.variableBreakLength = 0;
		}
	}
	
	/**
	 * {@inheritDoc} and throws a StudyPlannerException if TopicInterface has already been added.
	 */
	@Override
	public void addTopic(String name, int duration) throws StudyPlannerException {
		//Iterates through topics and checks to see if TopicInterface has already been added.
		boolean alreadyPresent = false;
		for (TopicInterface t : this.topics) {
			if (t.getSubject().equals(name)) {
				alreadyPresent = true;
			}
		}
		
		if (alreadyPresent) {
			throw new StudyPlannerException("Topic name already exists.");
		}
		else {
			this.topics.add(new Topic(name, duration));
			if (gui != null) {
				gui.notifyModelHasChanged();
			}
		}
	}
	
	/**
	 * {@inheritDoc} unless it is not found in the set of topics,
	 * in which case a StudyPlannerException is thrown.
	 */
	@Override
	public void deleteTopic(String topic) throws StudyPlannerException {
		//Iterates through topics and checks to see if TopicInterface exists.
		int topicIndex = -1;
		for (int i=0; i < this.topics.size(); ++i) {
			if (topics.get(i).getSubject().equals(topic)) {
				topicIndex = i;
			}
		}
		if (topicIndex == -1) {
			throw new StudyPlannerException("Topic not contained in list of topics.");
		}
		else {
			this.topics.remove(topicIndex);
			if (gui != null) {
				gui.notifyModelHasChanged();
			}
		}
	}

	@Override
	public List<TopicInterface> getTopics() {
		return this.topics;
	}

	@Override
	public List<StudyBlockInterface> getStudyPlan() {
		return this.plan;
	}
	
	@Override
	public void generateStudyPlan() throws StudyPlannerException {
		//Check to see that there are actually topics to study,
		//and if there aren't, throw an exception.
		if (topics == null) {
			throw new StudyPlannerException("No topics in topic list.");
		}
		else {
			
			int counter = 0;
			
			//Assign a new instance of ArrayList of StudyBlockInterfaces to plan.
			this.plan = new StudyList<StudyBlockInterface>();
			//Variable that holds totalDuration of all TopicsInterfaces.
			int totalDuration = 0;
			//Iterate through topics and add each TopicInterface duration to totalDuration.
			for (TopicInterface t : this.topics) {
				totalDuration += t.getDuration();
			}
			//Create a Calendar instance to hold time/date 
			//and instantiate with the current system time.
			StudyCalendar currentTime = new StudyCalendar();
			//If the current time is before the daily start time, change the current time (start time)
			//to the daily start time.
			
			if (currentTime.get(Calendar.HOUR_OF_DAY) < this.dailyStartTime.get(Calendar.HOUR_OF_DAY)) {
				currentTime.set(Calendar.HOUR_OF_DAY, this.dailyStartTime.get(Calendar.HOUR_OF_DAY));
				currentTime.set(Calendar.MINUTE, this.dailyStartTime.get(Calendar.MINUTE));
			}
			//Variable holds accumulated duration after each pass of while loop.
			int currentDuration = 0;
			//ArrayList holds the topics left, and so allows for topics
			//to be removed as they are completed.
			StudyList<TopicInterface> topicsLeft = this.topics.clone();
			//ArrayList holds the number of blocks of each topic.
			StudyList<Integer> topicDuration = new StudyList<Integer>();
			//One position in numberOfBlocks is created for each TopicInterface
			//in topics, and each position is instantiated with a value of 0.
			for (TopicInterface t : this.topics) {
				topicDuration.add(0);
			}
			//Run while currentDuration is less that totalDuration.
			while (currentDuration < totalDuration && topicsLeft.size() > 0) {
				//Create an ongoing total.
				int ongoingTotal = 0;
				//Create an ArrayList to hold the indices of the topics
				//that are completed with each run through the list of Topics.
				ArrayList<Integer> topicsCompleted = new ArrayList<Integer>();
				//Iterate through the remaining Topics (all Topics from topics
				//should be present on first run.
				for (int i=0; i < topicsLeft.size(); ++i) {
					//If the current amount of time studied is less than the Topic duration, then
					//proceed to add a new StudyBlock to plan, else mark Topic down as completed.
					if (topicDuration.get(i) < topicsLeft.get(i).getDuration()) {
						//If there is not enough time left in the Topic for a full block,
						//create a study block of reduced size.
						if (topicsLeft.get(i).getDuration() - topicDuration.get(i) < this.blockSize) {
							//Create variables to hold the reduced block size and break length, which will
							//be used to instantiate the study block and roll the current time on.
							this.variableBlockSize = topicsLeft.get(i).getDuration() - topicDuration.get(i);
							this.variableBreakLength = this.breakLength;
							//While true check for and handle event and daily end time clashes.
							while (true) {
								//Check to see if the study block will exceed the end of the study day,
								//and if so, call the handleDayExceeded() method to handle the clash.
								if (this.checkDayExceeded(currentTime, this.variableBlockSize) == 1) {
									this.handleDayExceeded(currentTime, topicsLeft.get(i).getDuration() - topicDuration.get(i));
								}
								//Check to see if the study block will clash with any events, and if so,
								//call the handleEventClash() method to handle the clash.
								else if (this.checkEventClash(currentTime, this.variableBlockSize) != -1) {
									this.handleEventClash(currentTime, this.events.get(this.checkEventClash(currentTime, this.variableBlockSize)),
											topicsLeft.get(i).getDuration() - topicDuration.get(i));
								}
								//Check to see if the study break will exceed the end of the day, and if so,
								//reduce the break length.
								else if (this.checkDayExceeded(currentTime, this.variableBlockSize + this.variableBreakLength) == 1) {
									this.variableBreakLength = this.getReducedBreakLength(currentTime, this.dailyEndTime, this.variableBlockSize);
								}
								//Check to see if the study break will clash with any events, and if so,
								//reduced the break length.
								else if (this.checkEventClash(currentTime, this.variableBlockSize + this.variableBreakLength) != -1){
									this.variableBreakLength = this.getReducedBreakLength(currentTime, 
											this.events.get(this.checkEventClash(currentTime, this.variableBlockSize + this.variableBreakLength)).getStartTime(),
											this.variableBlockSize);
								}
								//Once all clashes have been avoided, break from loop.
								else
									break;
							}
							//Add a new study block to the study plan, with the reduced block size
							//and current time after the above clash avoidance.
							this.plan.add(new StudyBlock(topicsLeft.get(i).getSubject(), currentTime.clone(), this.variableBlockSize));
							//Roll on the time by the block size.
							this.rollOnTime(currentTime, this.variableBlockSize);
							//If there is a break, add it as a study block to the study plan,
							//and roll on the current time.
							if (this.variableBreakLength > 0) {
								this.plan.add(new StudyBlock("(break)", currentTime.clone(), this.variableBreakLength));
								this.rollOnTime(currentTime, this.variableBreakLength);
							}
							//Add the reduced block size to the ongoing total.
							ongoingTotal += this.variableBlockSize;
							//Add the reduced block size to the topic's ongoing duration.
							topicDuration.set(i, topicDuration.get(i) + this.variableBlockSize);
						}
						else {
							//Create variables to hold the block size and break length, which will
							//be used to instantiate the study block and roll the current time on.
							this.variableBlockSize = this.blockSize;
							this.variableBreakLength = this.breakLength;
							//While true check for and handle event and daily end time clashes.
							while (true) {
								//Check to see if the study block will exceed the end of the study day,
								//and if so, call the handleDayExceeded() method to handle the clash.
								if (this.checkDayExceeded(currentTime, this.variableBlockSize) == 1) {
									this.handleDayExceeded(currentTime, this.blockSize);
								}
								//Check to see if the study block will clash with any events, and if so,
								//call the handleEventClash() method to handle the clash.
								else if (this.checkEventClash(currentTime, this.variableBlockSize) != -1) {
									this.handleEventClash(currentTime, this.events.get(this.checkEventClash(currentTime, this.variableBlockSize)),
											this.blockSize);
								}
								//Check to see if the study break will exceed the end of the day, and if so,
								//reduce the break length.
								else if (this.checkDayExceeded(currentTime, this.variableBlockSize + this.variableBreakLength) == 1) {
									this.variableBreakLength = this.getReducedBreakLength(currentTime, this.dailyEndTime, this.variableBlockSize);
								}
								//Check to see if the study break will clash with any events, and if so,
								//reduced the break length.
								else if (this.checkEventClash(currentTime, this.variableBlockSize + this.variableBreakLength) != -1){
									this.variableBreakLength = this.getReducedBreakLength(currentTime, 
											this.events.get(this.checkEventClash(currentTime, this.variableBlockSize + this.variableBreakLength)).getStartTime(),
											this.variableBlockSize);
								}
								//Once all clashes have been avoided, break from loop.
								else
									break;
							}
							//Add a new study block to the study plan, with the block size
							//and current time after the above clash avoidance.
							this.plan.add(new StudyBlock(topicsLeft.get(i).getSubject(), currentTime.clone(), this.variableBlockSize));
							//Roll on the time by the block size.
							this.rollOnTime(currentTime, this.variableBlockSize);
							//If there is a break, add it as a study block to the study plan,
							//and roll on the current time.
							if (this.variableBreakLength > 0) {
								this.plan.add(new StudyBlock("(break)", currentTime.clone(), this.variableBreakLength));
								this.rollOnTime(currentTime, this.variableBreakLength);
							}
							//Add the block size to the ongoing total.
							ongoingTotal += this.variableBlockSize;
							//Add the block size to the topic's ongoing duration.
							topicDuration.set(i, topicDuration.get(i) + this.variableBlockSize);
						}
					}
					else {
						//Add index to list of completed indices.
						topicsCompleted.add(i);
					}
				}
				//Add the ongoing total duration from that run through topics to currentDuration.
				currentDuration += ongoingTotal;
				//Iterate through topicsCompleted and remove topics with specified indices
				//from topicsLeft and numberOfBlocks.
				for (Integer index : topicsCompleted) {
					topicsLeft.remove((int) index);
					topicDuration.remove((int) index);
				}
			}
			//Notify the gui that the model has changed.
			if (gui != null) {
				gui.notifyModelHasChanged();
			}
		}
	}
	
	@Override
	public void generateStudyPlan(Calendar startStudy) throws StudyPlannerException {
		if (topics == null) {
			throw new StudyPlannerException("No topics in topics list.");
		}
		else {
			
			int counter = 0;
			
			//Assign a new instance of ArrayList of StudyBlockInterfaces to plan.
			this.plan = new StudyList<StudyBlockInterface>();
			//Variable that holds totalDuration of all TopicsInterfaces.
			int totalDuration = 0;
			//Iterate through topics and add each TopicInterface duration to totalDuration.
			for (TopicInterface t : this.topics) {
				totalDuration += t.getDuration();
			}
			//Create a Calendar instance to hold time/date 
			//and instantiate with the startStudy parameter.
			
			StudyCalendar currentTime = new StudyCalendar(startStudy.get(Calendar.YEAR), startStudy.get(Calendar.MONTH), 
					startStudy.get(Calendar.DATE), startStudy.get(Calendar.HOUR_OF_DAY), startStudy.get(Calendar.MINUTE));
			//If the current time is before the daily start time, change the current time (start time)
			//to the daily start time.
			if (currentTime.get(Calendar.HOUR_OF_DAY) < this.dailyStartTime.get(Calendar.HOUR_OF_DAY)) {
				currentTime.set(Calendar.HOUR_OF_DAY, this.dailyStartTime.get(Calendar.HOUR_OF_DAY));
				currentTime.set(Calendar.MINUTE, this.dailyStartTime.get(Calendar.MINUTE));
			}
			
			//Variable holds accumulated duration after each pass of while loop.
			int currentDuration = 0;
			//ArrayList holds the topics left, and so allows for topics
			//to be removed as they are completed.
			StudyList<TopicInterface> topicsLeft = this.topics.clone();
			//ArrayList holds the number of blocks of each topic.
			StudyList<Integer> topicDuration = new StudyList<Integer>();
			//One position in numberOfBlocks is created for each TopicInterface
			//in topics, and each position is instantiated with a value of 0.
			for (TopicInterface t : this.topics) {
				topicDuration.add(0);
			}
			//Run while currentDuration is less that totalDuration.
			while (currentDuration < totalDuration && topicsLeft.size() > 0) {
				//Create an ongoing total.
				int ongoingTotal = 0;
				//Create an ArrayList to hold the indices of the topics
				//that are completed with each run through the list of Topics.
				ArrayList<Integer> topicsCompleted = new ArrayList<Integer>();
				//Iterate through the remaining Topics (all Topics from topics
				//should be present on first run.
				for (int i=0; i < topicsLeft.size(); ++i) {
					//If the current amount of time studied is less than the Topic duration, then
					//proceed to add a new StudyBlock to plan, else mark Topic down as completed.
					if (topicDuration.get(i) < topicsLeft.get(i).getDuration()) {
						//If there is not enough time left in the Topic for a full block,
						//create a study block of reduced size.
						if (topicsLeft.get(i).getDuration() - topicDuration.get(i) < this.blockSize) {
							//Create variables to hold the reduced block size and break length, which will
							//be used to instantiate the study block and roll the current time on.
							this.variableBlockSize = topicsLeft.get(i).getDuration() - topicDuration.get(i);
							this.variableBreakLength = this.breakLength;
							//While true check for and handle event and daily end time clashes.
							while (true) {
								//Check to see if the study block will exceed the end of the study day,
								//and if so, call the handleDayExceeded() method to handle the clash.
								if (this.checkDayExceeded(currentTime, this.variableBlockSize) == 1) {
									this.handleDayExceeded(currentTime, topicsLeft.get(i).getDuration() - topicDuration.get(i));
								}
								//Check to see if the study block will clash with any events, and if so,
								//call the handleEventClash() method to handle the clash.
								else if (this.checkEventClash(currentTime, this.variableBlockSize) != -1) {
									this.handleEventClash(currentTime, this.events.get(this.checkEventClash(currentTime, this.variableBlockSize)),
											topicsLeft.get(i).getDuration() - topicDuration.get(i));
								}
								//Check to see if the study break will exceed the end of the day, and if so,
								//reduce the break length.
								else if (this.checkDayExceeded(currentTime, this.variableBlockSize + this.variableBreakLength) == 1) {
									this.variableBreakLength = this.getReducedBreakLength(currentTime, this.dailyEndTime, this.variableBlockSize);
								}
								//Check to see if the study break will clash with any events, and if so,
								//reduced the break length.
								else if (this.checkEventClash(currentTime, this.variableBlockSize + this.variableBreakLength) != -1){
									this.variableBreakLength = this.getReducedBreakLength(currentTime, 
											this.events.get(this.checkEventClash(currentTime, this.variableBlockSize + this.variableBreakLength)).getStartTime(),
											this.variableBlockSize);
								}
								//Once all clashes have been avoided, break from loop.
								else
									break;
							}
							//Add a new study block to the study plan, with the reduced block size
							//and current time after the above clash avoidance.
							this.plan.add(new StudyBlock(topicsLeft.get(i).getSubject(), currentTime.clone(), this.variableBlockSize));
							//Roll on the time by the block size.
							this.rollOnTime(currentTime, this.variableBlockSize);
							//If there is a break, add it as a study block to the study plan,
							//and roll on the current time.
							if (this.variableBreakLength > 0) {
								this.plan.add(new StudyBlock("(break)", currentTime.clone(), this.variableBreakLength));
								this.rollOnTime(currentTime, this.variableBreakLength);
							}
							//Add the reduced block size to the ongoing total.
							ongoingTotal += this.variableBlockSize;
							//Add the reduced block size to the topic's ongoing duration.
							topicDuration.set(i, topicDuration.get(i) + this.variableBlockSize);
						}
						else {
							//Create variables to hold the block size and break length, which will
							//be used to instantiate the study block and roll the current time on.
							this.variableBlockSize = this.blockSize;
							this.variableBreakLength = this.breakLength;
							//While true check for and handle event and daily end time clashes.
							while (true) {
								//Check to see if the study block will exceed the end of the study day,
								//and if so, call the handleDayExceeded() method to handle the clash.
								if (this.checkDayExceeded(currentTime, this.variableBlockSize) == 1) {
									this.handleDayExceeded(currentTime, this.blockSize);
								}
								//Check to see if the study block will clash with any events, and if so,
								//call the handleEventClash() method to handle the clash.
								else if (this.checkEventClash(currentTime, this.variableBlockSize) != -1) {
									this.handleEventClash(currentTime, this.events.get(this.checkEventClash(currentTime, this.variableBlockSize)),
											this.blockSize);
								}
								//Check to see if the study break will exceed the end of the day, and if so,
								//reduce the break length.
								else if (this.checkDayExceeded(currentTime, this.variableBlockSize + this.variableBreakLength) == 1) {
									this.variableBreakLength = this.getReducedBreakLength(currentTime, this.dailyEndTime, this.variableBlockSize);
								}
								//Check to see if the study break will clash with any events, and if so,
								//reduced the break length.
								else if (this.checkEventClash(currentTime, this.variableBlockSize + this.variableBreakLength) != -1){
									this.variableBreakLength = this.getReducedBreakLength(currentTime, 
											this.events.get(this.checkEventClash(currentTime, this.variableBlockSize + this.variableBreakLength)).getStartTime(),
											this.variableBlockSize);
								}
								//Once all clashes have been avoided, break from loop.
								else
									break;
							}
							//Add a new study block to the study plan, with the block size
							//and current time after the above clash avoidance.
							this.plan.add(new StudyBlock(topicsLeft.get(i).getSubject(), currentTime.clone(), this.variableBlockSize));
							//Roll on the time by the block size.
							this.rollOnTime(currentTime, this.variableBlockSize);
							//If there is a break, add it as a study block to the study plan,
							//and roll on the current time.
							if (this.variableBreakLength > 0) {
								this.plan.add(new StudyBlock("(break)", currentTime.clone(), this.variableBreakLength));
								this.rollOnTime(currentTime, this.variableBreakLength);
							}
							//Add the block size to the ongoing total.
							ongoingTotal += this.variableBlockSize;
							//Add the block size to the topic's ongoing duration.
							topicDuration.set(i, topicDuration.get(i) + this.variableBlockSize);
						}
					}
					else {
						//Add index to list of completed indices.
						topicsCompleted.add(i);
					}
				}
				//Add the ongoing total duration from that run through topics to currentDuration.
				currentDuration += ongoingTotal;
				//Iterate through topicsCompleted and remove topics with specified indices
				//from topicsLeft and numberOfBlocks.
				for (Integer index : topicsCompleted) {
					topicsLeft.remove((int) index);
					topicDuration.remove((int) index);
				}
			}
			//Notify the gui that the model has changed.
			if (gui != null) {
				gui.notifyModelHasChanged();
			}
		}
	}

	@Override
	public void setGUI(StudyPlannerGUIInterface gui) {
		this.gui = gui;
	}

	@Override
	public void setBlockSize(int size) throws StudyPlannerException {
		//If client tries to set a block size shorter than 10 minutes,
		//throw a StudyPlannerException.
		if (size < 10) {
			throw new StudyPlannerException("Block size is shorter than minimum of 10 minutes.");
		}
		else
			this.blockSize = size;
	}

	@Override
	public void setBreakLength(int i) {
		this.breakLength = i;
	}

	@Override
	public void setDailyStartStudyTime(Calendar startTime) throws StudyPlannerException {
		//Store the the number of hours of the daily end time and specified start time
		//as double variables.
		double endTimeHours = this.dailyEndTime.get(Calendar.HOUR_OF_DAY)
				+ this.dailyEndTime.get(Calendar.MINUTE) / 60;
		double startTimeHours = startTime.get(Calendar.HOUR_OF_DAY)
				+ startTime.get(Calendar.MINUTE) / 60;
		//If the start time is after the end time throw an exception,
		//else assign start time to member field.
		if (startTimeHours > endTimeHours) {
			throw new StudyPlannerException("Start time is after end time.");
		}
		else {
			this.dailyStartTime = new StudyCalendar();
			this.dailyStartTime.set(Calendar.HOUR_OF_DAY, startTime.get(Calendar.HOUR_OF_DAY));
			this.dailyStartTime.set(Calendar.MINUTE, startTime.get(Calendar.MINUTE));
		}
			
	}

	@Override
	public void setDailyEndStudyTime(Calendar endTime) throws StudyPlannerException {
		//Store the number of hours of the daily start time and specified end time
		//as double variables.
		double endTimeHours = endTime.get(Calendar.HOUR_OF_DAY)
				+ endTime.get(Calendar.MINUTE) / 60;
		double startTimeHours = this.dailyStartTime.get(Calendar.HOUR_OF_DAY)
				+ this.dailyStartTime.get(Calendar.MINUTE) / 60;
		//If the start time is after the end time throw an exception,
		//else assign end time to member field.
		if (startTimeHours > endTimeHours) {
			throw new StudyPlannerException("End time is before start time.");
		}
		else {
			this.dailyEndTime = new StudyCalendar();
			this.dailyEndTime.set(Calendar.HOUR_OF_DAY, endTime.get(Calendar.HOUR_OF_DAY));
			this.dailyEndTime.set(Calendar.MINUTE, endTime.get(Calendar.MINUTE));
		}
	}

	@Override
	public Calendar getDailyStartStudyTime() {
		return this.dailyStartTime;
	}

	@Override
	public Calendar getDailyEndStudyTime() {
		return this.dailyEndTime;
	}

	@Override
	public void addCalendarEvent(String eventName, Calendar startTime, int duration) throws StudyPlannerException {
		if (this.checkEventClash(startTime, duration) != -1) {
			throw new StudyPlannerException("Event clash");
		}
		else
			this.events.add(new CalendarEvent(eventName, startTime, duration));
		
		if (gui != null) {
			gui.notifyModelHasChanged();
		}
	}

	@Override
	public void addCalendarEvent(String eventName, Calendar startTime, int duration, CalendarEventType type) throws StudyPlannerException {
		if (this.checkEventClash(startTime, duration) != -1) {
			throw new StudyPlannerException("Event clash");
		}
		else
			this.events.add(new CalendarEvent(eventName, startTime, duration, type));
		
		if (gui != null) {
			gui.notifyModelHasChanged();
		}
	}

	@Override
	public List<CalendarEventInterface> getCalendarEvents() {
		return this.events;
	}

	@Override
	public void saveData(OutputStream saveStream) {
		//Create a PrintWriter to send text to the bufferWriter.
		PrintWriter writer = null;
		//Wrap the OutputStream in an OutputStreamWriter to allow character stream conversion.
		OutputStreamWriter outputWriter = new OutputStreamWriter(saveStream);
		//Wrap the OutputStreamWriter in a BufferedWriter in order to buffer output before flushing.
		BufferedWriter bufferWriter = new BufferedWriter(outputWriter);
		//Wrap the BufferedWriter in the PrintWriter writer.
		writer = new PrintWriter(bufferWriter);
		//For each Topic in topics, print the Topic details to the output stream using comma separation between variables.
		for (int i=0; i < this.topics.size(); ++i) {
			//Print the subject and duration to the output stream.
			writer.print(this.topics.get(i).getSubject() + "," + this.topics.get(i).getDuration() + ",");
			//Check to see if the Topic has a target event. If it does, print the event details.
			//Else catch the exception thrown and print "##" to the output stream.
			try {
				this.topics.get(i).getTargetEvent();
				writer.print(this.topics.get(i).getTargetEvent());
			}
			catch (StudyPlannerException e) {
				writer.print("##");
			}
			writer.println();
		}
		//Print a line containing "####" to signify the end of the array.
		writer.print("####");
		writer.println();
		//For each StudyBlock in plan, print the StudyBlock details to the output stream using comma separation between variables.
		for (int i=0; i < this.plan.size(); ++i) {
			//Print the subject, duration, and start time details to the output stream.
			writer.print(this.plan.get(i).getTopic() + "," + this.plan.get(i).getDuration() + ","
					+ this.plan.get(i).getStartTime());
			writer.println();
		}
		//Print a line containing "####" to signify the end of the array.
		writer.print("####");
		writer.println();
		//For each CalendarEvent in events, print the CalendarEvent details to the output stream using comma separation between variables.
		for (int i=0; i < this.events.size(); ++i) {
			//Print the CalendarEvent details to the output stream.
			writer.print(this.events.get(i));
			writer.println();
		}
		//Print a line containing "####" to signify the end of the array.
		writer.print("####");
		writer.println();
		//Print the dailyStartTime to the output stream.
		writer.print(this.dailyStartTime);
		writer.println();
		//Print a line containing "####" to signify the end of the item.
		writer.print("####");
		writer.println();
		//Print the dailyEndTime to the output stream.
		writer.print(this.dailyEndTime);
		//Close the writer, flushing the buffered characters to the output stream.
		writer.close();
	}

	@Override
	public void loadData(InputStream loadStream) throws StudyPlannerException {
		if (loadStream == null) {
			throw new StudyPlannerException("Invalid or empty file.");
		}
		else {
			//Create a Scanner which will be used to read in text from BufferedReader.
			Scanner reader = null;
			//Create a String to hold each line obtained by the Scanner as necessary.
			String line;
			//Assign empty lists to topics, plan and events.
			this.topics = new StudyList<TopicInterface>();
			this.plan = new StudyList<StudyBlockInterface>();
			this.events = new StudyList<CalendarEventInterface>();
			//Create variables to hold values being read.
			String subjectName;
			int subjectDuration;
			String eventName;
			int eventDuration;
			int year;
			int month;
			int date;
			int hours;
			int minutes;
			String eventTypeString;
			//Wrap the InputStream in an InputStreamReader, to allow conversion from byte stream to character stream.
			InputStreamReader inputReader = new InputStreamReader(loadStream);
			//Wrap the InputStreamReader in a BufferedReader, in order to buffer input for reading.
			BufferedReader bufferedReader = new BufferedReader(inputReader);
			//Wrap bufferedReader in the Scanner reader.
			reader = new Scanner(bufferedReader);
			//Create a counter to hold number of item blocks read from input stream.
			int progressCounter = 0;
			//Continue looping while reader has another line to read.
			while (reader.hasNextLine()) {
				//Assign next line in reader to String line.
				line = reader.nextLine();
				//If the line equals "####" increment the progressCounter and move onto the next line.
				if (line.equals("####")) {
					++progressCounter;
					line = reader.nextLine();
				}
				//As the progressCounter value increases, switch between the relevant fields.
				switch (progressCounter) {
					//topics
					case 0:
						//Split line into array of Strings and assign relevant elements to variables.
						subjectName = line.split(",")[0];
						subjectDuration = Integer.parseInt(line.split(",")[1]);
						//Add a new Topic, instantiated with the variables read from the file, to topics.
						topics.add(new Topic(subjectName, subjectDuration));
						//If the Topic has a target event, extract the event information from the file,
						//using it to instantiate a new CalendarEvent and setting this event as the Topic's target.
						if (!line.split(",")[2].equals("##")) {
							//Split line into array of Strings and assign relevant elements to variables.
							eventName = line.split(",")[2];
							eventDuration = Integer.parseInt(line.split(",")[3]);
							year = Integer.parseInt(line.split(",")[4]);
							month = Integer.parseInt(line.split(",")[5]);
							date = Integer.parseInt(line.split(",")[6]);
							hours = Integer.parseInt(line.split(",")[7]);
							minutes = Integer.parseInt(line.split(",")[8]);
							eventTypeString = line.split(",")[9];
							//Determine the event type, and set as Topic's target accordingly.
							if (eventTypeString.equals("EXAM")) {
								topics.get(topics.size() - 1).setTargetEvent(new CalendarEvent(eventName, 
										new StudyCalendar(year, month, date, hours, minutes), 
										eventDuration, CalendarEventType.EXAM));
							}
							else if (eventTypeString.equals("ESSAY")) {
								topics.get(topics.size() - 1).setTargetEvent(new CalendarEvent(eventName, 
										new StudyCalendar(year, month, date, hours, minutes), 
										eventDuration, CalendarEventType.ESSAY));
							}
						}
						break;
					//plan
					case 1:
						//Split line into array of Strings and assign relevant elements to variables.
						subjectName = line.split(",")[0];
						subjectDuration = Integer.parseInt(line.split(",")[1]);
						year = Integer.parseInt(line.split(",")[2]);
						month = Integer.parseInt(line.split(",")[3]);
						date = Integer.parseInt(line.split(",")[4]);
						hours = Integer.parseInt(line.split(",")[5]);
						minutes = Integer.parseInt(line.split(",")[6]);
						//Add a new StudyBlock, instantiated with the variables read from the file, to plan.
						this.plan.add(new StudyBlock(subjectName, new StudyCalendar(year, month, date, hours, minutes), 
								subjectDuration));
						break;
					//events
					case 2:
						//Split line into array of Strings and assign relevant elements to variables.
						eventName = line.split(",")[0];
						eventDuration = Integer.parseInt(line.split(",")[1]);
						year = Integer.parseInt(line.split(",")[2]);
						month = Integer.parseInt(line.split(",")[3]);
						date = Integer.parseInt(line.split(",")[4]);
						hours = Integer.parseInt(line.split(",")[5]);
						minutes = Integer.parseInt(line.split(",")[6]);
						eventTypeString = line.split(",")[7];
						//Determine event type, instantiate and add event to events accordingly.
						if (eventTypeString.equals("EXAM")) {
							this.events.add(new CalendarEvent(eventName, new StudyCalendar(year, month, date, hours, minutes), 
									eventDuration, CalendarEventType.EXAM));
						}
						else if (eventTypeString.equals("ESSAY")) {
							this.events.add(new CalendarEvent(eventName, new StudyCalendar(year, month, date, hours, minutes), 
									eventDuration, CalendarEventType.ESSAY));
						}
						else if (eventTypeString.equals("OTHER")) {
							this.events.add(new CalendarEvent(eventName, new StudyCalendar(year, month, date, hours, minutes), 
									eventDuration, CalendarEventType.OTHER));
						}
						break;
					//dailyStartTime
					case 3:
						//Split line into array of Strings and assign relevant elements to variables.
						year = Integer.parseInt(line.split(",")[0]);
						month = Integer.parseInt(line.split(",")[1]);
						date = Integer.parseInt(line.split(",")[2]);
						hours = Integer.parseInt(line.split(",")[3]);
						minutes = Integer.parseInt(line.split(",")[4]);
						//Assign a new StudyCalendar object, instantiated using the variables from the file,
						//to the dailyStartTime member field.
						this.setDailyStartStudyTime(new StudyCalendar(year, month, date, hours, minutes));
						break;
					//dailyEndTime
					case 4:
						//Split line into array of Strings and assign relevant elements to variables.
						year = Integer.parseInt(line.split(",")[0]);
						month = Integer.parseInt(line.split(",")[1]);
						date = Integer.parseInt(line.split(",")[2]);
						hours = Integer.parseInt(line.split(",")[3]);
						minutes = Integer.parseInt(line.split(",")[4]);
						//Assign a new StudyCalendar object, instantiated using the variables from the file,
						//to the dailyEndTime member field.
						this.setDailyEndStudyTime(new StudyCalendar(year, month, date, hours, minutes));
						break;
				}
			}
			//Close the reader.
			reader.close();
			if (gui != null) {
				gui.notifyModelHasChanged();
			}
		}
		
	}

}
