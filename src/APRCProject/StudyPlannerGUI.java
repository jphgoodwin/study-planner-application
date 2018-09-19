//Examination number: Y1466227

package APRCProject;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import APRCProject.StudyPlannerInterface.CalendarEventType;

public class StudyPlannerGUI extends JFrame implements StudyPlannerGUIInterface {
	//JButton fields.
	private JButton generateButton;
	private JButton exitButton;
	private JButton saveButton;
	private JButton loadButton;
	private JButton addTopicButton;
	private JButton deleteTopicButton;
	private JButton addEventButton;
	private JButton setTargetButton;
	//JList fields.
	private JList<String> topicList;
	private JList<String> studyPlan;
	//JLabel fields.
	private JLabel topicLabel;
	private JLabel planLabel;
	private JLabel topicNameLabel;
	private JLabel topicDurationLabel;
	private JLabel eventNameLabel;
	private JLabel eventDurationLabel;
	private JLabel eventTypeLabel;
	private JLabel yearLabel;
	private JLabel monthLabel;
	private JLabel dateLabel;
	private JLabel hourLabel;
	private JLabel minuteLabel;
	//JTextFields.
	private JTextField topicNameField;
	private JTextField topicDurationField;
	private JTextField eventNameField;
	private JTextField eventDurationField;
	private JTextField yearField;
	private JTextField monthField;
	private JTextField dateField;
	private JTextField hourField;
	private JTextField minuteField;
	//JPanel fields.
	private JPanel topButtonPanel;
	private JPanel topicFieldsPanel;
	private JPanel eventFieldsPanel;
	private JPanel dateFieldsPanel;
	private JPanel listsPanel;
	//JComboBox and JScrollPane fields.
	private JComboBox eventTypeOptions;
	private JScrollPane topicListScrollPane;
	private JScrollPane studyPlanScrollPane;
	//StudyPlanner field.
	private StudyPlanner planner;
	
	//Constructor
	public StudyPlannerGUI(StudyPlanner simToUse) {
		//Construct Class using JFrame constructor.
		super("Study Planner");
		//Assign specified StudyPlanner to planner.
		this.planner = simToUse;
		
		//Construct the top button panel and add it to the content pane.
		this.constructTopButtonPanel();
		this.topButtonPanel.setPreferredSize(new Dimension(1100, 40));
		getContentPane().add(this.topButtonPanel);
		
		//Construct the topic fields panel.
		this.constructTopicFieldsPanel();
		//Construct the add topic button.
		this.constructAddTopicButton();
		//Create a verticalBox and add topicFieldsPanel and addTopicButton to it.
		Box verticalBox_1 = Box.createVerticalBox();
		verticalBox_1.add(this.topicFieldsPanel);
		verticalBox_1.add(this.addTopicButton);
		verticalBox_1.add(Box.createRigidArea(new Dimension(200, 250)));
		
		//Construct the event fields panel.
		this.constructEventFieldsPanel();
		//Construct the date fields panel.
		this.constructDateFieldsPanel();
		//Construct the add event button.
		this.constructAddEventButton();
		//Create a verticalBox and add eventFieldsPanel, dateFieldsPanel and addEventButton to it.
		Box verticalBox_2 = Box.createVerticalBox();
		verticalBox_2.add(this.eventFieldsPanel);
		verticalBox_2.add(this.dateFieldsPanel);
		verticalBox_2.add(this.addEventButton);
		
		//Add verticalBox_1 and verticalBox_2 to a horizontal box,
		//which will then be added to the content pane.
		Box horizontalBox = Box.createHorizontalBox();
		horizontalBox.add(verticalBox_1);
		horizontalBox.add(verticalBox_2);
		horizontalBox.setPreferredSize(new Dimension(1100, 300));
		getContentPane().add(horizontalBox);
		
		//Construct the list panel.
		this.constructListPanel();
		//Add listPanel to the content pane.
		getContentPane().add(this.listsPanel);
		//Set the layout of the content pane to BoxLayout along the page axis.
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.PAGE_AXIS));
		//Pack the Frame.
		pack();
	}
	//Method constructs top button panel.
	private void constructTopButtonPanel() {
		this.topButtonPanel = new JPanel(new FlowLayout());
		
		this.generateButton = new JButton("Generate Study Plan");
		this.generateButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					planner.generateStudyPlan();
				}
				catch (StudyPlannerException e) {
					JOptionPane.showMessageDialog(getContentPane(), e.getMessage());
				}
			}
		});
		this.topButtonPanel.add(this.generateButton);
		
		this.exitButton = new JButton("Exit Program");
		this.exitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				int choice = JOptionPane.showConfirmDialog(getContentPane(), "Are you sure you want to exit?");
				if (choice == 0) {
					System.exit(0);
				}
			}
		});
		this.topButtonPanel.add(this.exitButton);
		
		this.saveButton = new JButton("Save");
		this.saveButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String filename = (String) JOptionPane.showInputDialog(getContentPane(),
					"Enter filename ending with .txt", "Save", JOptionPane.PLAIN_MESSAGE,
					null, null, "data.txt");
				try {
					FileOutputStream outputStream = new FileOutputStream(filename);
					planner.saveData(outputStream);
					outputStream.close();
					JOptionPane.showMessageDialog(getContentPane(), "Saved successfully.");
				}
				catch (IOException e) {
					JOptionPane.showMessageDialog(getContentPane(), e.getMessage());
				}
				catch (NullPointerException e) {
					
				}
			}
		});
		this.topButtonPanel.add(this.saveButton);
		
		this.loadButton = new JButton("Load");
		this.loadButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				String filename = (String) JOptionPane.showInputDialog(getContentPane(),
					"Enter filename ending with .txt", "Load", JOptionPane.PLAIN_MESSAGE,
					null, null, "data.txt");
				try {
					FileInputStream inputStream = new FileInputStream(filename);
					planner.loadData(inputStream);
					inputStream.close();
				}
				catch (IOException e) {
					JOptionPane.showMessageDialog(getContentPane(), e.getMessage());
				}
				catch (StudyPlannerException e) {
					JOptionPane.showMessageDialog(getContentPane(), e.getMessage());
				}
				catch (NullPointerException e) {
					
				}
			}
		});
		this.topButtonPanel.add(this.loadButton);
	}
	//Method constructs topic fields panel.
	private void constructTopicFieldsPanel() {
		GridLayout topicPanelLayout = new GridLayout(0,2);
		topicPanelLayout.setHgap(5);
		topicPanelLayout.setVgap(5);
		this.topicFieldsPanel = new JPanel();
		topicPanelLayout.layoutContainer(this.topicFieldsPanel);
		this.topicFieldsPanel.setLayout(topicPanelLayout);
		
		this.topicNameLabel = new JLabel("Name:");
		this.topicFieldsPanel.add(this.topicNameLabel);
		
		this.topicNameField = new JTextField(200);
		this.topicNameField.setEditable(true);
		this.topicNameField.setSize(200, 10);
		this.topicFieldsPanel.add(this.topicNameField);
		this.topicNameLabel.setLabelFor(this.topicNameField);
		
		this.topicDurationLabel = new JLabel("Duration(minutes):");
		this.topicFieldsPanel.add(this.topicDurationLabel);
		
		this.topicDurationField = new JTextField(200);
		this.topicDurationField.setEditable(true);
		this.topicFieldsPanel.add(this.topicDurationField);
		this.topicDurationLabel.setLabelFor(this.topicDurationField);
	}
	//Method constructs add topic button.
	private void constructAddTopicButton() {
		this.addTopicButton = new JButton("Add Topic");
		this.addTopicButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent argo0) {
				try {
					String name = topicNameField.getText();
					int duration = Integer.parseInt(topicDurationField.getText().trim());
					planner.addTopic(name, duration);
					topicNameField.setText("");
					topicDurationField.setText("");
				}
				catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(getContentPane(), e.getMessage());
				}
				catch (StudyPlannerException e) {
					JOptionPane.showMessageDialog(getContentPane(), e.getMessage());
				}
			}
		});
	}
	//Method constructs event fields panel.
	private void constructEventFieldsPanel() {
		GridLayout eventPanelLayout = new GridLayout(0,2);
		eventPanelLayout.setHgap(5);
		eventPanelLayout.setVgap(5);
		this.eventFieldsPanel = new JPanel();
		eventPanelLayout.layoutContainer(this.eventFieldsPanel);
		this.eventFieldsPanel.setLayout(eventPanelLayout);
		
		this.eventNameLabel = new JLabel("Name:");
		this.eventFieldsPanel.add(this.eventNameLabel);
		
		this.eventNameField = new JTextField(200);
		this.eventNameField.setEditable(true);
		this.eventNameField.setSize(200, 10);
		this.eventFieldsPanel.add(this.eventNameField);
		this.eventNameLabel.setLabelFor(this.eventNameField);
		
		this.eventDurationLabel = new JLabel("Duration(minutes):");
		this.eventFieldsPanel.add(this.eventDurationLabel);
		
		this.eventDurationField = new JTextField(200);
		this.eventDurationField.setEditable(true);
		this.eventDurationField.setSize(200, 10);
		this.eventFieldsPanel.add(this.eventDurationField);
		this.eventDurationLabel.setLabelFor(this.eventDurationField);
		
		this.eventTypeLabel = new JLabel("Event Type:");
		this.eventFieldsPanel.add(this.eventTypeLabel);
		
		String[] eventTypes = {"Exam", "Essay", "Other"};
		this.eventTypeOptions = new JComboBox(eventTypes);
		
		this.eventFieldsPanel.add(this.eventTypeOptions);
	}
	//Method constructs date fields panel.
	private void constructDateFieldsPanel() {
		GridLayout datePanelLayout = new GridLayout(6,0);
		datePanelLayout.setHgap(5);
		datePanelLayout.setVgap(5);
		this.dateFieldsPanel = new JPanel();
		datePanelLayout.layoutContainer(this.dateFieldsPanel);
		this.dateFieldsPanel.setLayout(datePanelLayout);
		
		this.yearLabel = new JLabel("Year:");
		this.dateFieldsPanel.add(this.yearLabel);
		
		this.yearField = new JTextField(200);
		this.yearField.setEditable(true);
		this.yearField.setSize(40, 10);
		this.dateFieldsPanel.add(this.yearField);
		this.yearLabel.setLabelFor(yearField);
		
		this.monthLabel = new JLabel("Month:");
		this.dateFieldsPanel.add(this.monthLabel);
		
		this.monthField = new JTextField(200);
		this.monthField.setEditable(true);
		this.monthField.setSize(20, 10);
		this.dateFieldsPanel.add(this.monthField);
		this.monthLabel.setLabelFor(monthField);
		
		this.dateLabel = new JLabel("Date:");
		this.dateFieldsPanel.add(this.dateLabel);
		
		this.dateField = new JTextField(200);
		this.dateField.setEditable(true);
		this.dateField.setSize(20, 10);
		this.dateFieldsPanel.add(this.dateField);
		this.dateLabel.setLabelFor(dateField);
		
		this.hourLabel = new JLabel("Hour:");
		this.dateFieldsPanel.add(this.hourLabel);
		
		this.hourField = new JTextField(200);
		this.hourField.setEditable(true);
		this.hourField.setSize(20, 10);
		this.dateFieldsPanel.add(this.hourField);
		this.hourLabel.setLabelFor(hourField);
		
		this.minuteLabel = new JLabel("Minute:");
		this.dateFieldsPanel.add(this.minuteLabel);
		
		this.minuteField = new JTextField(200);
		this.minuteField.setEditable(true);
		this.minuteField.setSize(20, 10);
		this.dateFieldsPanel.add(this.minuteField);
		this.minuteLabel.setLabelFor(minuteField);
	}
	//Method constructs add event button.
	private void constructAddEventButton() {
		this.addEventButton = new JButton("Add Event");
		this.addEventButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					String name = eventNameField.getText();
					int duration = Integer.parseInt(eventDurationField.getText().trim());
					CalendarEventType type = getEventTypeSelected();
					int year = Integer.parseInt(yearField.getText().trim());
					int month = Integer.parseInt(monthField.getText().trim()) - 1;
					int date = Integer.parseInt(dateField.getText().trim());
					int hour = Integer.parseInt(hourField.getText().trim());
					int minute = Integer.parseInt(minuteField.getText().trim());
					GregorianCalendar startTime = new GregorianCalendar(year, month, date, hour, minute);
					
					planner.addCalendarEvent(name, startTime, duration, type);
					
					eventNameField.setText("");
					eventDurationField.setText("");
					yearField.setText("");
					monthField.setText("");
					dateField.setText("");
					hourField.setText("");
					minuteField.setText("");
				}
				catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(getContentPane(), e.getMessage());
				}
				catch (StudyPlannerException e) {
					JOptionPane.showMessageDialog(getContentPane(), e.getMessage());
				}
			}
		});
	}
	//Method constructs list panel.
	private void constructListPanel() {
		this.listsPanel = new JPanel(new FlowLayout());
		
		this.topicLabel = new JLabel("Topics:");
		this.listsPanel.add(this.topicLabel);
		
		String[] data = {"one", "two", "three", "four"};
		this.topicList = new JList<String>(data);
		this.topicList.setVisibleRowCount(10);
		this.topicListScrollPane = new JScrollPane(this.topicList);
		this.listsPanel.add(this.topicListScrollPane);
		this.topicLabel.setLabelFor(this.topicListScrollPane);
		
		this.deleteTopicButton = new JButton("Delete Topic");
		this.deleteTopicButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				ArrayList<String> topicsFromList = new ArrayList<String>(topicList.getSelectedValuesList());
				try {
					boolean empty = true;
					for (String s: topicsFromList) {
						planner.deleteTopic(s.split(":")[0]);
						empty = false;
					}
					if (empty) {
						JOptionPane.showMessageDialog(getContentPane(), "Please select a topic to delete.");
					}
				}
				catch (StudyPlannerException e) {
					JOptionPane.showMessageDialog(getContentPane(), e.getMessage());
				}
				catch (NullPointerException e) {
					JOptionPane.showMessageDialog(getContentPane(), e.getMessage());
				}
			}
		});
		this.listsPanel.add(this.deleteTopicButton);
		
		this.setTargetButton = new JButton("Set Topic Target");
		this.setTargetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					if (studyPlan.getSelectedIndex() != -1 && topicList.getSelectedIndex() != -1) {
						String eventName = studyPlan.getSelectedValue().split("=>")[1];
						planner.getTopics().get(topicList.getSelectedIndex()).setTargetEvent(getEvent(eventName));
						JOptionPane.showMessageDialog(getContentPane(), "Target set.");
					}
					else {
						JOptionPane.showMessageDialog(getContentPane(), "Please select a topic and a valid target event.");
					}
				}
				catch (StudyPlannerException e) {
					JOptionPane.showMessageDialog(getContentPane(), e.getMessage());
				}
				catch (NullPointerException e) {
					JOptionPane.showMessageDialog(getContentPane(), e.getMessage());
				}
			}
		});
		this.listsPanel.add(this.setTargetButton);
		
		this.planLabel = new JLabel("Study Plan:");
		this.listsPanel.add(this.planLabel);
		
		data = new String[] {"one", "two", "three", "four"};
		this.studyPlan = new JList<String>(data);
		this.studyPlan.setVisibleRowCount(10);
		this.studyPlanScrollPane = new JScrollPane(this.studyPlan);
		this.listsPanel.add(this.studyPlanScrollPane);
		this.planLabel.setLabelFor(this.studyPlanScrollPane);
		this.listsPanel.setPreferredSize(new Dimension(1100, 200));
	}
	//Method returns CalendarEventInterface with given event name.
	private CalendarEventInterface getEvent(String eventName) throws StudyPlannerException {
		int index = -1;
		for (int i=0; i < planner.getCalendarEvents().size(); ++i) {
			if (planner.getCalendarEvents().get(i).getName().equals(eventName)) {
				index = i;
			}
		}
		if (index != -1) {
			return planner.getCalendarEvents().get(index);
		}
		else {
			throw new StudyPlannerException("You haven't selected a valid event.");
		}
	}

	@Override
	public void notifyModelHasChanged() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				updateDisplay();
			}
		});
	}
	//Method updates the GUI, re-populating all visible lists.
	private void updateDisplay() {
		if (this.planner == null) {
			//Nothing to update from, so do nothing.
		}
		else {
			List<String> topicData = new ArrayList<String>();
			for (TopicInterface t : this.planner.getTopics()) {
				topicData.add(t.getSubject() + ": (" + t.getDuration() + ")");
			}
			this.topicList.setListData(topicData.toArray(new String[1]));
			List<String> eventData = new ArrayList<String>();
			List<Calendar> startTimes = new ArrayList<Calendar>();
			for (StudyBlockInterface ev : planner.getStudyPlan()) {
				eventData.add(writeDate(ev.getStartTime().get(Calendar.YEAR), ev.getStartTime().get(Calendar.MONTH), ev.getStartTime().get(Calendar.DATE)) 
						+ " " + writeTime(ev.getStartTime().get(Calendar.HOUR_OF_DAY), ev.getStartTime().get(Calendar.MINUTE)) 
						+ " (" + ev.getDuration() + ")=>" + ev.getTopic() + getTopicTarget(ev.getTopic()));
				
				startTimes.add(ev.getStartTime());
			}
			for (CalendarEventInterface ev : planner.getCalendarEvents()) {
				eventData.add(writeDate(ev.getStartTime().get(Calendar.YEAR), ev.getStartTime().get(Calendar.MONTH), ev.getStartTime().get(Calendar.DATE)) 
						+ " " + writeTime(ev.getStartTime().get(Calendar.HOUR_OF_DAY), ev.getStartTime().get(Calendar.MINUTE))
						+ " (" + ev.getDuration() + ")=>" + ev.getName());
				startTimes.add(ev.getStartTime());
			}
			this.sortLists((ArrayList<Calendar>) startTimes, (ArrayList<String>) eventData);
			this.studyPlan.setListData(eventData.toArray(new String[1]));
			pack();
		}
	}
	//Method returns the name of a topics target event if it has one, and an empty String if it doesn't.
	private String getTopicTarget(String topicName) {
		int index = -1;
		for (int i=0; i < planner.getTopics().size(); ++i) {
			if (planner.getTopics().get(i).getSubject().equals(topicName)) {
				index = i;
			}
		}
		if (index != -1) {
			try {
				String eventName = planner.getTopics().get(index).getTargetEvent().getName();
				return " (" + eventName + ")";
			}
			catch (StudyPlannerException e) {
				return "";
			}
		}
		else
			return "";
		
	}
	//Method sorts lists according to start time.
	private void sortLists(ArrayList<Calendar> startTimes, ArrayList<String> list) {
		Boolean swapped = true;
		while (swapped) {
			swapped = false;
			for (int i=0; i < startTimes.size() - 1; ++i) {
				if (startTimes.get(i).getTimeInMillis() > startTimes.get(i + 1).getTimeInMillis()) {
					Collections.swap(startTimes, i, i + 1);
					Collections.swap(list, i, i + 1);
					swapped = true;
				}
			}
		}
		
	}
	//Method returns a String representation of the time, 
	//given an integer number of hours and minutes.
	private String writeTime(int hrs, int mins) {
		String hours;
		String minutes;
		if (hrs < 10) {
			hours = new String("0" + hrs);
		}
		else {
			hours = new String("" + hrs);
		}
		if (mins < 10) {
			minutes = new String("0" + mins);
		}
		else {
			minutes = new String("" + mins);
		}
		return hours.concat(":" + minutes);
	}
	//Method returns a String representation of the date, 
	//given and integer number of years, months and date. 
	private String writeDate(int year, int month, int date) {
		String yyyy;
		String mm;
		String dd;
		if (month < 10) {
			mm = new String("0" + (month + 1));
		}
		else {
			mm = new String("" + (month + 1));
		}
		if (date < 10) {
			dd = new String("0" + date);
		}
		else {
			dd = new String("" + date);
		}
		yyyy = new String("" + year);
		return new String(dd + "/" + mm + "/" + yyyy);
	}
	//Method converts the event type selected in the combo box to a CalendarEventType
	//and returns the CalendarEventType.
	private CalendarEventType getEventTypeSelected() {
		
		if (this.eventTypeOptions.getSelectedIndex() == 0) {
			return CalendarEventType.EXAM;
		}
		else if (this.eventTypeOptions.getSelectedIndex() == 1) {
			return CalendarEventType.ESSAY;
		}
		else if (this.eventTypeOptions.getSelectedIndex() == 2) {
			return CalendarEventType.OTHER;
		}
		else {
			return CalendarEventType.OTHER;
		}
	}
	//Main method.
	public static void main(String[] args) {
		StudyPlanner planner = new StudyPlanner();
    	
    	StudyPlannerGUI gui = new StudyPlannerGUI(planner);
    	gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	
    	planner.setGUI(gui);
    	
    	gui.setVisible(true);
    	gui.updateDisplay();
	}

}
