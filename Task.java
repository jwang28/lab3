import java.util.*;
import java.io.*;

public class Task{
	private ArrayList <Activity> activities = new ArrayList<Activity>();
	private ArrayList<Integer> claimed = new ArrayList<Integer>();
	private ArrayList<Integer> used = new ArrayList<Integer>();
	private int iterator, id, finishTime, computeTime, numBlocked, max;
	boolean aborted, finished, computing, processed, isClaimed;

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
	public void setIsClaimed(boolean isClaimed){
		this.isClaimed = isClaimed;
	}
	public boolean isClaimed(){
		return this.isClaimed;
	}
	public void setUsed(int index, int max){
		used.add(index, max);
	}
	public int getUsed(int index){
		if (used.size() > index){
			return used.get(index);
		}
		return 0;
	}
	public void setMax(int index, int max){
		claimed.add(index, max);
	}
	public int getMax (int index){
		return claimed.get(index);
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