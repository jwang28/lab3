import java.util.*;
import java.io.*;
/*
The Task class stores the processing time, blocking/computing time, and activities processed*/
public class Task{
	private ArrayList <Activity> activities = new ArrayList<Activity>();
	private int iterator, id, finishTime, computeTime, numBlocked;
	boolean aborted, finished, computing, processed;

	//initialize with task number
	public Task(int i){
			this.finished = false;
			this.finishTime=0;
			this.iterator = 0;
			this.aborted = false;
			id = i + 1;
			computeTime = 0;
			numBlocked = 0;
			computing = false;
			processed = false;
	}
	
	
	public int getFinishTime(){
		return this.finishTime;
	}
	public void setProcessed(boolean processed){
		this.processed = processed;
	}
	
	public boolean processed(){
		return this.processed;
	}
	public void setComputing(boolean computing){
		this.computing = computing;
	}
	
	public boolean isComputing(){
		return this.computing;
	}
	public int getNumBlocked(){
		return this.numBlocked;
	}
	public Activity getActivity(){
		return this.activities.get(iterator);
	}
	public ArrayList<Activity> getActivities(){
		return this.activities;
	}

	//public void reset (int i) i this needed
	public int getId(){
		return this.id;
	}
	//has another activity
	public Boolean hasNext(){
		if (iterator == activities.size() -1){
			return false;
		}
		return true;
	}


	public Boolean isFinished(){
		return this.finished;
	}

	public Boolean isAborted(){
		return aborted;
	}

	public void next(){
		this.iterator++;
	}

	public void finishTask(int finish){
		this.finishTime = finish;
		this.finished = true;
	}

	public void abortTask(){
		iterator = activities.size() - 1;
		finished=true;
		aborted = true;
		/*numBlocked = 0;*/
	}
	public void setComputeTime(int computeTime){
		this.computeTime = computeTime;
	}
	public int getComputeTime(){
		return this.computeTime;
	}
	public void compute(){
		computeTime--;
	}


	public void addBlock(){
		numBlocked++;
	}
	public int getIterator(){
		return this.iterator;
	}
}