import java.util.*;

public class Activity{
	private String type;
	private int id, delay, resource, number; //for resources, check how it's stored
	public Activity(String type, int id, int delay, int resource, int number){
		this.type = type;
		this.id = id;
		this.delay = delay;
		this.resource = resource -1;
		this.number = number;
	}

	public String getType(){
		return this.type;
	}
	public int getId(){
		return this.id;
	}
	public int getDelay(){
		return this.delay;
	}
	public int getResource(){
		return this.resource;
	}
	public int getNumber(){
		return this.number;
	}
}