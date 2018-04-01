import java.io.*;
import java.util.*;

public class Main {
	public static void main (String[] args) throws FileNotFoundException{
		/*int numTasks=0, numResources =0;*/
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
			tasks[id - 1].getActivities().add(new Activity (type, id, delay, resource, number));
		}
		//call fifo and banker
		fifo (numTasks, numResources, tasks,resources);
	}

	//fifo
	public static void fifo(int numTasks, int numResources, Task[] tasks, int[] resources){
		int cycle = 0;
		int tasksLeft = numTasks;
		int[] available = new int[numResources];
		//copy array of available resources
		for (int i = 0; i<numResources; i++){
			available[i] = resources[i];
		}

		int[][] allocated = new int[numTasks][numResources];
		Queue<Task> blocked = new LinkedList<Task>();
		ArrayList<Task> ready = new ArrayList<Task>();
		ArrayList<Task> wait = new ArrayList<Task>();

		//process activities
		while(!checkFinish(tasks)){
			for (Task t: tasks){
				t.setProcessed(false);
			}
			ready.clear();
			wait.clear();
			
			int[] releasedResources = new int[numResources];
			for (int i =0; i<releasedResources.length; i++){
				releasedResources[i] = 0;
			}
			//check if blocked tasks can be run
			while (blocked.peek()!=null){
				Task t = blocked.poll();
				if (t.getActivity().getType().equals("terminate")){
					ready.add(t);
				}
				else if (canAllocate(t, available) && t.getComputeTime() == 0){
					t.setComputing(false);
					ready.add(t);
					available[t.getActivity().getResource()]-=t.getActivity().getNumber();
				}
				else{
					wait.add(t);
				}
			}
			for (Task t: ready){
				blocked.remove(t);
				if (!t.getActivity().getType().equals("terminate")){
					available[t.getActivity().getResource()]+=t.getActivity().getNumber();
				}
				tasksLeft = processActivities(tasks, available, allocated, blocked, releasedResources, t.getId()-1, tasksLeft, cycle);
				t.setProcessed(true);
			}
			for (Task t: wait){
				blocked.add(t);
			}
			for (int i =0; i<tasks.length; i++){
				//not in blocked or not just released
				tasksLeft = processActivities(tasks, available, allocated, blocked, releasedResources, i, tasksLeft, cycle);
			}

			//check deadlock and process
			if (blocked.size() == tasksLeft && !allWaiting(tasks) && blocked.size() > 1){

				for (int i = 0; i<tasks.length; i++){

					if (!tasks[i].isFinished() && !tasks[i].isAborted()){
						if (canAllocate(tasks[i],available)) {
							break;
						}
						tasks[i].abortTask();
						tasks[i].finishTask(0);
						for (int j = 0; j<numResources; j++){
							available[j] += allocated[i][j];
						}
						blocked.remove(tasks[i]);						
					}
				}
			}
			for (Task t: blocked){
				if (t.getComputeTime() > 0){
					t.compute();
				}
				if(!t.isComputing()){
					t.addBlock();
				}
				
			}
			for (int i = 0; i<numResources; i++){ 
				available[i]+=releasedResources[i];
			}
			cycle++;
		}
		


		printFinishTime(tasks);
	}
	public static int processActivities(Task[] tasks, int[] available, int[][] allocated, Queue<Task> blocked, int[] releasedResources, int i, int numTasks, int cycle){
		int tasksLeft = numTasks;
		if (!blocked.contains(tasks[i]) && !tasks[i].isAborted() && !tasks[i].isFinished() && !tasks[i].processed()){
			Activity cur = tasks[i].getActivity();
			
			if (cur.getDelay()>0 && !cur.isDelayed()){
				tasks[i].setComputeTime(cur.getDelay());
				blocked.add(tasks[i]);
				cur.setDelayed();
				tasks[i].setComputing(true);
			}

			else if(cur.getType().equals("initiate")){
				tasks[i].next();
			}
			else if (cur.getType().equals("request")){
				if (cur.getNumber() <= available[cur.getResource()]){
					available[cur.getResource()]-=cur.getNumber();
					allocated[i][cur.getResource()]+=cur.getNumber();
					tasks[i].next();
				}
				else{
					blocked.add(tasks[i]);
				}
			}
			else if (cur.getType().equals("release")){
				releasedResources[cur.getResource()]+=cur.getNumber();
				allocated[i][cur.getResource()]-=cur.getNumber();
				tasks[i].next();
			}
			else{	
				tasks[i].finishTask(cycle + tasks[i].getComputeTime());
				tasksLeft--;
			}
		}
		return tasksLeft;
	}
	
	public static boolean allWaiting(Task[] tasks){
		for (Task t: tasks){
			int comp = t.getComputeTime();
			if (!t.isFinished() && t.getComputeTime() == 0){
				return false;
			}
		}
		return true;
	}
	
	public static boolean canAllocate (Task task, int[] available) {
		Activity cur = task.getActivity();
		if (cur.getResource() >=0){
			if (available[cur.getResource()] - cur.getNumber() >= 0){
				return true;
			}	
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
	
	public static void printFinishTime(Task[] tasks){
		int totalTime = 0;
		int totalBlockedTime = 0;
		System.out.println("          FIFO");
		for(int i=0;i<tasks.length;i++){
			System.out.print("Task " + tasks[i].getId()+"       ");
			if(tasks[i].isAborted()){
				System.out.println("Aborted");
			}
			else{
				totalTime+=tasks[i].getFinishTime();
				totalBlockedTime+=tasks[i].getNumBlocked();
				System.out.print(tasks[i].getFinishTime()+"    ");
				System.out.print(tasks[i].getNumBlocked()+"    ");
				double temp = ((double)tasks[i].getNumBlocked()/tasks[i].getFinishTime()) * 100;
				System.out.println((int) temp + "%");
			}
		}
		System.out.print("Total        ");
		
		
		double print = (double)totalBlockedTime/totalTime;
		System.out.print(totalTime+"    ");
		System.out.print(totalBlockedTime+"    ");				
		System.out.print((int)(print*100) + "%");
		System.out.println();
	}





























}