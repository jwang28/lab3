import java.util.*;

public class Activity{
	private String type;
	private int id, delay, resource, number;
	public Activity(String type, int id, int delay, int resource, int number){
		this.type = type;
		this.id = id;
		this.delay = delay;
		this.resource = resource;
		this.number = number;
	}

	public String getType(){
		return this.type;
	}
	public int getId(){
		return this.id;
	}
}