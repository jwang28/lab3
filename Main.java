import java.io.*;
import java.util.*;

public class Main {
	public static void main (String[] args) throws FileNotFoundException{
		/*int numTasks=0, numResources =0;*/
		System.out.println("start program");
		File f = new File (args[0]);

		Scanner input = new Scanner(f);
		int numTasks = input.nextInt();
		int numResources = input.nextInt();
				/*try{
			input = new Scanner(f);
			numTasks = input.nextInt();
			numResources = input.nextInt();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}*/
		//do I need to capture the number of resources?
		int[] resources = new int[numResources];
		for (int i = 0; i<numResources; i++){
			resources[i] = input.nextInt();
		}

		//initialize array of tasks
		Task[] tasks = new Task[numTasks];
		for (int i = 0; i<numTasks; i++){
			tasks[i] = new Task(i);
		}
		while (input.hasNext()){
			
			String type = input.next();
			int id = input.nextInt();
			int delay = input.nextInt();
			int resource = input.nextInt();
			int number = input.nextInt();
			System.out.println("type is: " + type + ' ' + id + " " + delay + ' ' + resource + ' ' + number);
			tasks[id - 1].getActivities().add(new Activity (type, id, delay, resource, number));
		}
	}
}