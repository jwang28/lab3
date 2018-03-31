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
		//call fifo and banker
		fifo (numTasks, numResources, tasks,resources);
	}

	//fifo
	public static void fifo(int numTasks, int numResources, Task[] tasks, int[] resources){
		int cycle = 0;
		int[] available = new int[numResources];
		//copy array of available resources
		for (int i = 0; i<numResources; i++){
			available[i] = resources[i];
		}

		int[][] allocated = new int[numTasks][numResources];
		Queue<Task> blocked = new LinkedList<Task>();
		ArrayList<Task> ready = new ArrayList<Task>();

		

		//process activities
		while(!checkFinish(tasks)){
			System.out.println("******** cycle " + cycle + "*********");
			//place at end?
			
			int[] releasedResources = new int[numResources];
			for (int i =0; i<releasedResources.length; i++){
				releasedResources[i] = 0;
			}
			//check if blocked tasks can be run
			for (int i = 0; i< tasks.length; i++){
				System.out.println(tasks.length);
				Task t = tasks[i];
				if (blocked.contains(t) && canAllocate(t, available)){
					available[t.getActivity().getResource()]-=t.getActivity().getNumber();
					allocated[i][t.getActivity().getResource()]+=t.getActivity().getNumber();
					t.next();
					ready.add(t);
				}
			}
			for (Task t: ready){
				blocked.remove(t);
				System.out.println("remove from blocked");
			}
			for (int i =0; i<tasks.length; i++){
				//not in blocked or not just released
				if (!blocked.contains(tasks[i]) && !tasks[i].isAborted() && !tasks[i].isFinished()){
					Activity cur = tasks[i].getActivity();

					if(cur.getType().equals("initiate")){
						System.out.println("initiate");
						if (cur.getNumber() > numResources){
							tasks[i].next();
						}
						else{
							tasks[i].abortTask();
						}
					}
					else if (cur.getType().equals("request")){
						System.out.println("request");
						if (cur.getNumber() <= available[cur.getResource()]){
							available[cur.getResource()]-=cur.getNumber();
							allocated[i][cur.getResource()]+=cur.getNumber();
							tasks[i].next();
						}
						else{
							tasks[i].setComputeTime(cur.getDelay());
							blocked.add(tasks[i]);
						}
					}
					else if (cur.getType().equals("release")){
						System.out.println("release");
						releasedResources[cur.getResource()]+=cur.getNumber();
						allocated[i][cur.getResource()]-=cur.getNumber();
						tasks[i].next();
					}
					else{	
						System.out.println("else: " + cur.getType());
						tasks[i].finishTask(cycle);
						System.out.println("finished!!!");
					}
				}
				System.out.println(tasks[i].getActivity().getType());
				/*if(tasks[i].getFinishTime() == 0){
					tasks[i].finishTask(cycle);
					System.out.println("finished!!!");
				}*/
			}

			//process blocked
			for (Task t: blocked){
				if (t.getComputeTime() > 1){
					t.compute();
				}
				t.addBlock();
			}
			//check deadlock and process
			if (blocked.size() == numTasks){
				System.out.println("deadlock");
				for (int i = 0; i<tasks.length; i++){
					if (!tasks[i].isFinished() && !tasks[i].isAborted()){
						tasks[i].abortTask();

						for (int j = 0; j<numResources; j++){
							available[j] += allocated[i][j];
							System.out.println(allocated[i][j] + " resources allocated back\n" + "resource 1: " + available[j]);
						}
						blocked.remove(tasks[i]);
						if (canAllocate(tasks[i],available)) {
							break;
						}
					}
				}
			}
			for (int i = 0; i<numResources; i++){ 
				available[i]+=releasedResources[i];
			}
			cycle++;
		}
		


		printFinishTime(tasks);
	}
	/*public static boolean isDeadlock(Task[] task, int[] available){
		
	}*/

	public static Boolean canAllocate (Task task, int[] available) {
		Activity cur = task.getActivity();
		System.out.println("here " + cur.getType() + " " + cur.getResource());
		if (available[cur.getResource()] - cur.getNumber() >= 0){
			return true;
		}
		return false;
	}

	public static boolean checkFinish(Task[] task){
		for (int i = 0; i<task.length; i++){
			if (!task[i].isFinished()){
				return false;
			}
		}
		return true;
	}
	public static int numFinished(Task[] task){
		int finished = 0;
		for (int i = 0; i<task.length; i++){
			if (task[i].isFinished()){
				finished++;
			}
		}
		return finished;
	}
	public static void printFinishTime(Task[] tasks){
		int totalTime = 0;
		int totalBlockedTime = 0;
		System.out.println("          FIFO");
		//DecimalFormat dfPrint = new DecimalFormat("####");
		for(int i=0;i<tasks.length;i++){
			System.out.print("Task " + tasks[i].getId()+"       ");
			if(tasks[i].isAborted()){
				System.out.print("Aborted");
			}
			else{
				totalTime+=tasks[i].getFinishTime();
				totalBlockedTime+=tasks[i].getNumBlocked();
				System.out.print("finishTime: "  + tasks[i].getFinishTime()+"    ");
				System.out.print("numBlocked: " +tasks[i].getNumBlocked()+"    ");
				System.out.println((double)tasks[i].getNumBlocked()/tasks[i].getFinishTime());
			}
			System.out.println();
		}
		System.out.print("Total        ");
		
		
		double print = (double)totalBlockedTime/totalTime;
		System.out.print(totalTime+"    ");
		System.out.print(totalBlockedTime+"    ");				
		System.out.print((print*100)+"%");
		System.out.println();
	}


	//print methods





























}