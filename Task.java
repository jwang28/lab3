import java.util.*;
import java.io.*

public class Task(){
	private ArrayList <Activity> activities = new ArrayList<Activity>();
	int iterator, id, finishTime, computeTime, numBlocked;
	boolean aborted;

	//initialize with task number
	public Task(int i){
			this.iterator = 0;
			this.aborted = false;
			id = i + 1;
			comptueTime = 0;
			numBlocked = 0;
	}

	public ArrayList<Activity> getActivities(){
		return this.activities;
	}

	//public void reset (int i) i this needed

	//has another activity
	public Boolean hasNext(){
		if (iterator == activities.size() -1){
			return false;
		}
		return true;
	}

	public String getNext(){
		return activities.get(iterator);
	}

	public Boolean finished(){
		if (getNext().getType() == "terminate" && computeTime ==0){
			return true;
		}
		return false;
	}

	public Boolean aborted(){
		return aborted;
	}

	public void next(){
		iterator++;
	}

	public void finishTask()(int finish){
		this.finishTime = finish;
	}

	public void abortTask(){
		iterator = activities.size() - 1;
		aborted = true;
		numBlocked = 0;
	}

	public void compute(){
		computeTime--;
	}


	public void block(){
		numBlocked++;
	}













}