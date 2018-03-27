import java.io.*;
import java.util.*;

public class Main {
	public static void main (String[] args) {
		int numTasks, numResources;
		System.out.println("start program");
		Scanner input = new Scanner (System.in);
		FIle f = new File (args[0]);
		try{
			Scanner input = new Scanner(f);
			numTasks = input.nextInt();
			numResources = input.nextInt();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(0);
		}

		//initialize array of tasks
		Task[] tasks = new Task[numTasks];
		for (int i = 0; i<numTasks; i++){
			tasks[i] = new Task(i);
		}
		while (input.hasNext()){
			String type;
			int id, delay, resource, number;
			for (int i = 0; i<5; i++){
				type = input.next();
				id = input.nextInt();
				delay = input.nextInt();
				resource = input.nextInt();
				number = input.nextInt();
			}
			tasks[id - 1].getActivity().add(new Activity (type, id, delay, resource, number);
		}
	}
}