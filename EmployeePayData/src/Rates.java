
public class Rates {
	private String title[]; //Array lets us use split later to remove extra artifacts ( "}] )
	private float benRate;
	private float payRate;
	
	/**
	 * Constructor
	 * @param t The job title
	 * @param ben The benefits rate for that job title
	 * @param pay The pay rate for that job title
	 */
	public Rates(String t, String ben, String pay) { 
		String hold = t.substring(1); //Removes leading quote
		title = hold.split("\"");
		benRate = Float.parseFloat(ben);
		payRate = Float.parseFloat(pay);
	}
	
	/**
	 * Get Job Title String
	 * @return Job Title String
	 */
	public String getTitle() {
		return title[0]; //Only grabs Text; leaves extra artifacts
	}
	
	/**
	 * Get Benefits Rate for Job Title
	 * @return Ben Rate as a float
	 */
	public float getBenRate() {
		return benRate;
	}
	
	/**
	 * Get Pay Rate for Job Title
	 * @return Pay Rate as a float
	 */
	public float getPayRate() {
		return payRate;
	}
}
