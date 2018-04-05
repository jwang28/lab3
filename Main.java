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
		Task[] tasks_fifo = new Task[numTasks];
		Task[] tasks_banker = new Task[numTasks];
		for (int i = 0; i<numTasks; i++){
			tasks_fifo[i] = new Task(i);
			tasks_banker[i] = new Task(i);
		}
		while (input.hasNext()){
			
			String type = input.next();
			int id = input.nextInt();
			int delay = input.nextInt();
			int resource = input.nextInt();
			int number = input.nextInt();
			tasks_fifo[id - 1].getActivities().add(new Activity (type, id, delay, resource, number));

			tasks_banker[id - 1].getActivities().add(new Activity (type, id, delay, resource, number));
		}
		//call fifo and banker
		Task[] fifoTasks = fifo (numTasks, numResources, tasks_fifo,resources);
		printFinishTime("FIFO", fifoTasks);
		Task[] bankerTasks = banker (numTasks, numResources, tasks_banker, resources);
		printFinishTime("BANKER\'S", bankerTasks);
	}

	//fifo
	public static Task[] fifo(int numTasks, int numResources, Task[] tasks, int[] resources){
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
				if(t.getComputeTime() == 0){
					t.addBlock();
				}
				if (t.getComputeTime() > 0){
					t.compute();
				}
				/*if(!t.isComputing()){
					t.addBlock();
				}*/
				
			}
			for (int i = 0; i<numResources; i++){ 
				available[i]+=releasedResources[i];
			}
			cycle++;
		}
		


		return tasks;
	}
	public static Task[] banker(int numTasks, int numResources, Task[] tasks, int[] resources){
		int cycle = 0;
		int tasksLeft = numTasks;
		int[] available = new int[numResources];
		int[][] max = new int[numTasks][numResources];
		int[][] need = new int[numTasks][numResources];
		//copy array of available resources
		for (int i = 0; i<numResources; i++){
			available[i] = resources[i];
		}

		int[][] allocated = new int[numTasks][numResources];
		Queue<Task> blocked = new LinkedList<Task>();
		ArrayList<Task> ready = new ArrayList<Task>();
		ArrayList<Task> wait = new ArrayList<Task>();
		for (int i = 0; i<numTasks; i++){
			for (int j = 0; j<numResources; j++){
				max[i][j]=0;
				need[i][j]=0;
				allocated[i][j]=0;
			}
		}

		//process activities
		while(!checkFinish(tasks)){
			//for testing purposes
			/*if (cycle > 10){
				break;
			}*/
			System.out.println("***************" + cycle + "***************");
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
				System.out.println("check block");
				Task t = blocked.poll();
				System.out.println(t.getId());
				//immediately pull out of block and process
				if (t.getActivity().getType().equals("terminate")){
					ready.add(t);
				}
				else if (isSafe(t, available, allocated, need, t.getId() -1) && t.getComputeTime() == 0){
					System.out.println("allocated");
					t.setComputing(false);
					ready.add(t);
					//subtract here so other requests do not get granted
					available[t.getActivity().getResource()]-=t.getActivity().getNumber();
					System.out.println(available[t.getActivity().getResource()]);
					need[t.getId() -1][t.getActivity().getResource()]-=t.getActivity().getNumber();
				}
				else{
					System.out.println("back to block");
					wait.add(t);
				}
			}
			for (Task t: ready){
				blocked.remove(t);
				if (!t.getActivity().getType().equals("terminate")){
					available[t.getActivity().getResource()]+=t.getActivity().getNumber();
					need[t.getId() -1][t.getActivity().getResource()]+=t.getActivity().getNumber();
				}
				//process activities in block/ready
				tasksLeft = processBanker(tasks, available, allocated, need, blocked, max, releasedResources, t.getId()-1, tasksLeft, cycle, resources);
				t.setProcessed(true);
			}
			for (Task t: wait){
				blocked.add(t);
			}
			for (int i =0; i<tasks.length; i++){
				//process not in blocked or not just released
				tasksLeft = processBanker(tasks, available, allocated, need, blocked, max, releasedResources, i, tasksLeft, cycle, resources);
			}

			for (Task t: blocked){
				if(t.getComputeTime() == 0){
					t.addBlock();
				}
				if (t.getComputeTime() > 0){
					t.compute();
				}
				/*if(!t.isComputing()){
					t.addBlock();
				}*/
				
			}
			for (int i = 0; i<numResources; i++){ 
				available[i]+=releasedResources[i];
			}

			cycle++;
		}
		
		return tasks;

		//printFinishTime(tasks);
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
	
	public static int processBanker(Task[] tasks, int[] available, int[][] allocated, int[][] need, Queue<Task> blocked, int[][] max,int[] releasedResources, int i, int numTasks, int cycle, int[] resources){
		int tasksLeft = numTasks;
		if (!blocked.contains(tasks[i]) && !tasks[i].isAborted() && !tasks[i].isFinished() && !tasks[i].processed()){
			Activity cur = tasks[i].getActivity();
			
			if (cur.getDelay()>0 && !cur.isDelayed()){
				System.out.println("Task " + tasks[i].getId() + " is delayed on " + tasks[i].getActivity().getType());
				tasks[i].setComputeTime(cur.getDelay());
				blocked.add(tasks[i]);
				cur.setDelayed();
				tasks[i].setComputing(true);
			}

			else if(cur.getType().equals("initiate")){
				System.out.println("initiate" + cur.getResource());
				if (cur.getNumber() > resources[cur.getResource()]){
					System.out.println("task " + tasks[i].getId() + " is aborted");
					tasks[i].abortTask();
				}
				else{
					tasks[i].next();
					max[i][cur.getResource()] = cur.getNumber();
					need[i][cur.getResource()] = cur.getNumber();
				}
				
			}
			else if (cur.getType().equals("request")){
				System.out.println(tasks[i].getId() + " request " + cur.getNumber());
				System.out.println("available: " + available[cur.getResource()] + " used: " + tasks[i].getUsed(cur.getResource()));
/*				if ((tasks[i].getMax(cur.getResource()) >= (tasks[i].getUsed(cur.getResource()) + cur.getNumber())) && ((available[cur.getResource()] - allotted[temp]) >= cur.getNumber())) {
*/				
				if (cur.getNumber() > need[i][cur.getResource()]){
					System.out.println("abort on request, requested: " + cur.getNumber() + " but only needs " + need[i][cur.getResource()]);
					tasks[i].abortTask();
					releaseAll(tasks[i], available, allocated);

				}
				else{
/*					System.out.println("i is: " + i + " resource is: " + cur.getResource());
*/					/*if (available[cur.getResource()] >= need[i][cur.getResource()]){
						available[cur.getResource()]-=cur.getNumber();
						allocated[i][cur.getResource()]+=cur.getNumber();
						need[i][cur.getResource()]-=cur.getNumber();
						tasks[i].next();
						System.out.println("granted");
					}*/
					if (isSafe(tasks[i], available, allocated, need, i)){
						available[cur.getResource()]-=cur.getNumber();
						allocated[i][cur.getResource()]+=cur.getNumber();
						need[i][cur.getResource()]-=cur.getNumber();
						tasks[i].next();
						System.out.println("granted");
					}
					else{
						//block
						System.out.println("blocked");
						blocked.add(tasks[i]);
						//tasks[i].setMax(cur.getResource(), tasks[i].getMax(cur.getResource()) + cur.getNumber());
					}
				}


				
			}
			else if (cur.getType().equals("release")){
				System.out.println("release resource " + cur.getResource() + " units: " + cur.getNumber());
				releasedResources[cur.getResource()]+=cur.getNumber();
				allocated[i][cur.getResource()]-=cur.getNumber();
				need[i][cur.getResource()]+=cur.getNumber();
/*				allotted[cur.getResource()]-=tasks[i].getMax(cur.getResource());
*/				//tasks[i].setUsed(cur.getResource(), 0);

				tasks[i].next();
				//tasks[i].setUsed(cur.getResource(), tasks[i].getUsed(cur.getResource()) - cur.getNumber());
				if (tasks[i].getActivity().getType().equals("terminate")){
					releaseAll(tasks[i], available, allocated);

				}
			}
			
			else{
				//terminate	
				System.out.println("else " + cur.getType());
				//releast all resources
				// releaseAll(tasks[i], allotted, available.length);
				tasks[i].finishTask(cycle + tasks[i].getComputeTime());
				tasksLeft--;
			}
		}
		return tasksLeft;
	}
	public static void releaseAll(Task t, int[] available, int[][] allocated){
		for (int i = 0; i < available.length; i++){
			available[i]+=allocated[t.getId()-1][i];
			//don't need allocate and need because will never process this task again...but tbd
/*			allotted[i]-=t.getMax(i);
*/		}
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
	public static boolean isSafe (Task task, int[] available, int[][] allocated, int[][] need, int i) {
		Activity cur = task.getActivity();

		if(cur.getNumber() <= need[i][cur.getResource()] && available[cur.getResource()] >= need[i][cur.getResource()]){
			System.out.println("enough resources but is it safe");
			
			for (int row = 0 ; row<need.length; row++){
				available[cur.getResource()]-=cur.getNumber();
				need[task.getId()-1][cur.getResource()]-=cur.getNumber();
				System.out.println("check task: " + row);
				int count = 0;
				for (int col = 0; col<need[row].length; col++){
					System.out.print("avail: " + available[col] + " vs " + need[row][col]);

					if (available[col] >= need[row][col]){
						System.out.println(" ok ");
						count++;
					}
					else{
						System.out.println();
					}

				}
				
				available[cur.getResource()]+=cur.getNumber();
				need[task.getId()-1][cur.getResource()]+=cur.getNumber();
				int resources = need[row].length;
				if (count == resources){
					System.out.println("return true!");
					return true;
				}
				
			}
			
		}
		return false;
		

		/*Activity cur = task.getActivity();

		if (cur.getResource() >=0){
			if (task.getMax(cur.getResource()) >= (task.getUsed(cur.getResource()) + cur.getNumber()) && (available[cur.getResource()] - allotted[cur.getResource()]) >= cur.getNumber()) {
			if ((available[cur.getResource()] - allotted[cur.getResource()] + task.getUsed(cur.getResource())) - cur.getNumber() >= 0){
			boolean req = false;
			if(task.getUsed(cur.getResource()) > 0){
				if (available[cur.getResource()] >= cur.getNumber()){
					req = true;
				}
			}
			else{
				if (available[cur.getResource()] - allotted[cur.getResource()] >= cur.getNumber()){
					req = true;
				}
			}
			if (req){	
				return true;
			}
		}
		return false;*/
	}

	public static boolean checkFinish(Task[] task){
		for (int i = 0; i<task.length; i++){
			if (!task[i].isFinished()){
				return false;
			}
		}
		return true;
	}
	
	public static void printFinishTime(String algo, Task[] tasks){
		int totalTime = 0;
		int totalBlockedTime = 0;
		System.out.println("          " + algo);
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