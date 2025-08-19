import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class EmpTime {
	private String name;									//Employee name
	private List<Duration> dur = new ArrayList<>();			//List of punch time durations
	private List<String> title = new ArrayList<>();			//List of corresponding job titles to each clock in
	private float net = 0; 									//net dollar amount accrued as EmployeePayData.java runs
	private Duration numHrs = Duration.ofHours(0); 			//counter to determine what hours belong to which time bracket
	private float benAmt = 0;								//benefits amount accrued as EmployeePayData.java runs
	private float regular;									//seconds at regular rate
	private float overtime;									//seconds at overtime
	private float doubletime;								//seconds at double overtime
	
	/**
	 * Constructor
	 * @param n Employee name
	 */
	public EmpTime(String n) {
		name = n;
	}
	
	/**
	 * Add Clock in
	 * @param s Start time Object
	 * @param e End time Object
	 * @param t Job title for time punch
	 */
	public void addPunch(Object s, Object e, Object t) {
		DateTimeFormatter dtForm = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		
		//Parse object to LocalDateTime for duration calculations
		LocalDateTime start = LocalDateTime.parse((String) s, dtForm);
		LocalDateTime end = LocalDateTime.parse((String) e, dtForm);
		
		//Set time punch duration & job title
		dur.add(Duration.between(start, end));
		title.add((String) t);
	}
	
	/**
	 * Set Net Amount
	 * @param n Amount to add the the net total
	 */
	public void addNet(float n) {
		net += n;
	}
	
	/**
	 * Set Total Number of Hours
	 * Allows us to assess where the overtime & double time cut offs are
	 * @param h Time to add to the net total as a Duration
	 */
	public void addNumHrs(Duration h) {
		numHrs = numHrs.plus(h);
	}
	
	/**
	 * Set Benefits Total Amount
	 * @param b Amount to add as a float
	 */
	public void addBenAmt(float b) {
		benAmt += b;
	}
	
	/**
	 * Get Name
	 * @return Name of employee as a String
	 */
	public String getName() {
		return name;
	}

	/**
	 * Get Total Number of Hours
	 * @return Total Number of Hours as a Duration
	 */
	public Duration getNumHours() {
		return numHrs;
	}
	
	/**
	 * Get number of time punches
	 * @return Number of individual time punches as an integer
	 */
	public int getNumPunches() {
		return dur.size();
	}
	
	/**
	 * Get Duration of individual time punch
	 * @param index Index of time punch
	 * @return Duration of time in the corresponding time punch
	 */
	public Duration getDur(int index) {
		return dur.get(index);
	}
	
	/**
	 * Get list of Duration of time punches
	 * Primarily to aid in testing
	 * @return List of Duration objects, each corresponding to a time punch
	 */
	public List<Duration> getDur() {
		return dur;
	}
	
	/**
	 * Get Job Title
	 * @param index Index of the punch in
	 * @return Corresponding Job Title as a string
	 */
	public String getTitle(int index) {
		return title.get(index);
	}
	
	/**
	 * Get Net Amount
	 * @return Total Net Amount accrued as a float
	 */
	public float getNet() {
		return net;
	}
	
	/**
	 * Get Benefits Amount
	 * @return Total Benefits Amount accrued as a float
	 */
	public float getBenAmt() {
		return benAmt;
	}

	/**
	 * To String Method to print output
	 */
	public String toString() {
		String out = "Employee: " + name + "\n";
		float seconds = numHrs.getSeconds();								//Total number of time in seconds
		if(seconds <= 40*3600) {											//if hours accrued are less than 40 hrs
			regular = seconds/3600;											
			out += "Regular Time: " + formatDec(regular) + "\n";
		} else if(seconds <= 48*3600) {										//if hours accrued are more than 40, less than 48
			regular = 40;
			overtime = (seconds - (40*3600)) /3600;
			out += "Regular Time: 40\nOvertime: " + formatDec(overtime) + "\n";
		} else {															//if hours accrued are more than 48
			regular = 40;
			overtime = 8;
			doubletime = (seconds - (48*3600)) /3600;
			out += "Regular Time: 40\nOvertime: 8\nDoubletime: " + formatDec(doubletime) + "\n";
		}
		
		out += "Wage Total: " + formatDec(net) + "\nBenefits Total: " + formatDec(benAmt) + "\n\n";
		return out;
	}
	
	/**
	 * Format as a decimal with 4 places
	 * @param x float to format
	 * @return String of float formatted to 4 decimal places
	 */
	private String formatDec(float x) {
		String formatted = String.format("%.4f", x);
		return formatted;
	}
}
