import org.json.simple.JSONObject;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.parser.*;

public class EmployeePayData {
	//Global var init
	static List<Rates> rates = new ArrayList<>();								//Dynamic list of rate objects
	static List<EmpTime> punches = new ArrayList<>();							//Dynamic list of employee objects
	static Duration fullTime = Duration.ofHours(40);							//Define full time cap
	static Duration timeHalf = Duration.ofHours(48);							//Define overtime cap
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		// file name is PayData.json
		processJSON("PayData.json"); 											
		calcNet();
        for (int i = 0; i < punches.size(); i++) {								//for each employee object
        	System.out.println(punches.get(i).toString());							//print the associated output
        }        
	}
	
	/**
	 * Process the JSON file
	 * Reads the file & separates it into two components- Job meta data & employee time data
	 * @param filename the name or file location of the JSON file
	 */
	public static void processJSON(String filename) {
		FileReader fileIn = null;
		Object jsonObj = null;
		
		//Initialize FileReader object
		try {
			fileIn = new FileReader(filename); //fetch file
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//Create JSONParser, pass FileReader object
		try {
			jsonObj = new JSONParser().parse(fileIn);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //parse JSON
        JSONObject jsonO = (JSONObject) jsonObj;
                
        
        //Create JSON Array objects to hold separate job meta data & employee time data
        JSONArray jobMeta = (JSONArray) jsonO.get("jobMeta");
        JSONArray empData = (JSONArray) jsonO.get("employeeData");        

        //Process the separate cases
        procRates(jobMeta);
        procEmp(empData);
        	
    }
	
	/**
	 * Process Rates Data
	 * Further processes the job meta data & dynamically creates Rates objects for further processing
	 * @param rawRates JSONArray object that holds the job meta data
	 */
	public static void procRates(JSONArray rawRates) {							//raw holds all the information of all the jobs
		String[] tempSplitHold;													
		List<String> values = new ArrayList<>();								//Hold each separate value within that array without extra characters
		String rawString = rawRates.toString();
        String[] splitAttributes= rawString.split(",");							//Split array into chunks- Each will hold data about a specific job title
        
        for(int i = 0; i < splitAttributes.length; i++) { 						//For each job
        	tempSplitHold = splitAttributes[i].split(":");							//Split further with : to separate the attribute from it's name
        	values.add(tempSplitHold[1]);											//Pass only the value, not the title of the attribute
        }
        
        //Create Rates object for each job
        for(int j = 3; j <= values.size(); j+= 3) {								//Incrementing by 3, since there are three values for each job
        	rates.add(new Rates(values.get(j-1), values.get(j-3), values.get(j-2))); //Constructor (title, ben rate, pay rate)
        }
	}
	
	/**
	 * Process Employee Time Data
	 * Further processes the Employee Time Punch details & dynamically creates EmpTime objects for further processing
	 * @param rawEmp JSONArray object that holds the employee time punch data for everyone
	 */
	public static void procEmp(JSONArray rawEmp) {								//raw holds all the information for each employee
		JSONArray times = null;
		String empName;
		
		//Split data into readable chunks
		for(int i = 0; i < rawEmp.size(); i++) {								//For each employee
			JSONObject emp = (JSONObject) rawEmp.get(i);							//Create a sep JSON array to itemize their time punches
			times = (JSONArray) emp.get("timePunch");								
			empName = (String) emp.get("employee");
			punches.add(new EmpTime(empName));										//Create EmpTime object (employee name)
			
			for(int j = 0; j < times.size(); j++) {									//For each time punch 
				JSONObject t = (JSONObject) times.get(j);								//Initialize JSON object
				punches.get(i).addPunch(t.get("start"), t.get("end"), t.get("job"));	//to pass start, end, & title to EmpTime object based on attribute name
			}
		}
	}

	/**
	 * Calculate Wages
	 */
	public static void calcNet() {
		//initialize variables
		String job; 															
		Duration dur = null;													
		float payRate;
		float benRate;
		
		//When splitting time across time type thresholds (reg time vs over time vs double time)
		long timeDif;
		long prevRate;
		long newRate;
		
		
		//for every employee
		for(int i = 0; i < punches.size(); i++) { 								//i represents all the data/punches for a single employee
			for(int j = 0; j < punches.get(i).getNumPunches(); j++) { 				//j represents individual punches
				job = punches.get(i).getTitle(j);										//get job title for this duration
				dur = punches.get(i).getDur(j); 										//get the duration of time
				punches.get(i).addNumHrs(dur);											//add that duration to counter within class
				payRate = getPayRate(job); 												
				benRate = getBenRate(job);												
				punches.get(i).addBenAmt(benRate * ((float) dur.getSeconds()/3600));	//Calculate ben rate; seconds/3600 gives a decimal format to multiply the rate against
				
				
				//Wage Calculations
				if(punches.get(i).getNumHours().getSeconds() <= fullTime.getSeconds()) { //at 40 hours?  no
					punches.get(i).addNet(payRate * ((float) dur.getSeconds()/3600));	
					
					
				} else if(punches.get(i).getNumHours().getSeconds() <= timeHalf.getSeconds()) {//at > 40 hours? yes. at 48 hours? no
					//Before calcing time & a half, need to split time to apply to correct hours
					timeDif = punches.get(i).getNumHours().getSeconds() - dur.getSeconds();
					
					if(timeDif < fullTime.getSeconds()) { //if the current duration minus the total belongs to the previous block, split the amount of time
						prevRate = fullTime.getSeconds() - timeDif;
						newRate = dur.getSeconds() - prevRate;
						punches.get(i).addNet(payRate * ((float) prevRate/3600));
						
						payRate *= 1.5;
						punches.get(i).addNet(payRate * ((float) newRate/3600));
					} else {
						payRate *= 1.5;
						punches.get(i).addNet(payRate * ((float) dur.getSeconds()/3600));
					}
					
				}
				else {//at > 48 hours? yes
					//Before calcing  double, need to account for hours the belong in the previous rate
					timeDif = punches.get(i).getNumHours().getSeconds() - dur.getSeconds();
					
					if(timeDif < timeHalf.getSeconds()) { //if the current duration minus the total belongs to the previous block, split the amount of time
						prevRate = timeHalf.getSeconds() - timeDif;
						newRate = dur.getSeconds() - prevRate;
						punches.get(i).addNet((payRate * 1.5f) * ((float) prevRate/3600));
						
						payRate *= 2;
						punches.get(i).addNet(payRate * ((float) newRate/3600));
					} else {
						payRate *= 2;
						punches.get(i).addNet(payRate * ((float) dur.getSeconds()/3600));
					}
					
				}
				
			}

		}

	}

	/**
	 * Get Pay Rate
	 * Compare job title to title in Rate objects to find correct pay rate
	 * @param job String of job title being searched for
	 * @return Pay rate for the job title as a float
	 */
	public static float getPayRate(String job) {
		for (int p = 0; p < rates.size(); p++) {
			if(rates.get(p).getTitle().equals(job)) {
				return rates.get(p).getPayRate();
			}
		}
		return 0;
	}
	
	/**
	 * Get Benefits Rate
	 * Compare job title to title in Rate objects to find correct benefits rate
	 * @param job String of job title being searched for
	 * @return Ben rate for the job title as a float
	 */
	public static float getBenRate(String job) {
		for (int b = 0; b < rates.size(); b++) {
			if(rates.get(b).getTitle().equals(job)) {
				return rates.get(b).getBenRate();
			}
		}
		return 0;
	}

}
