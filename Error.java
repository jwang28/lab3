import java.util.*;
/*
Error class stores information on the task and conditions of program when the task was aborted. Used to print status at the end
*/

public class Error{
	private String algo;
	private int task, resource, number, avail, cycle; 
	
	public Error(String algo, int task, int resource, int number, int avail, int cycle){
		this.algo = algo;
		this.task= task;
		this.resource = resource;
		this.number = number;
		this.avail = avail;
		this.cycle = cycle;
	}

	public int getTask(){
		return this.task;
	}
	public int getResource(){
		return this.resource;
	}
	public int getNumber(){
		return this.number;
	}
	public int getAvail(){
		return this.avail;
	}
	public int getCycle(){
		return this.cycle;
	}
}