import java.io.*;
import java.util.*;

public class Main {
	public static void main (String[] args) throws FileNotFoundException{
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
		fifo (numTasks, numResources, tasks_fifo,resources);
		banker (numTasks, numResources, tasks_banker, resources);
		//printFinishTime("BANKER\'S", bankerTasks);
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
				if(t.getComputeTime() == 0){
					t.addBlock();
				}
				if (t.getComputeTime() > 0){
					t.compute();
				}
			}
			for (int i = 0; i<numResources; i++){ 
				available[i]+=releasedResources[i];
			}
			cycle++;
		}
		printFinishTime("FIFO", tasks);
	}
	public static void banker(int numTasks, int numResources, Task[] tasks, int[] resources){
		int cycle = 0;
		int tasksLeft = numTasks;
		int[] available = new int[numResources];
		int[][] max = new int[numTasks][numResources];
		int[][] need = new int[numTasks][numResources];
		ArrayList<Error> errors = new ArrayList<Error>();
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
				//immediately pull out of block and process
				if (t.getActivity().getType().equals("terminate")){
					ready.add(t);
				}
				else if (isSafe(t, available, allocated, need, t.getId() -1) && t.getComputeTime() == 0){
					t.setComputing(false);
					ready.add(t);
					//subtract here so other requests do not get granted
					available[t.getActivity().getResource()]-=t.getActivity().getNumber();
					need[t.getId() -1][t.getActivity().getResource()]-=t.getActivity().getNumber();
				}
				else{
					//System.out.println("back to block");
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
				tasksLeft = processBanker(tasks, available, allocated, need, blocked, max, releasedResources, errors, t.getId()-1, tasksLeft, cycle, resources);
				t.setProcessed(true);
			}
			for (Task t: wait){
				blocked.add(t);
			}
			for (int i =0; i<tasks.length; i++){
				//process not in blocked or not just released
				tasksLeft = processBanker(tasks, available, allocated, need, blocked, max, releasedResources, errors, i, tasksLeft, cycle, resources);
			}

			for (Task t: blocked){
				if(t.getComputeTime() == 0){
					t.addBlock();
				}
				if (t.getComputeTime() > 0){
					t.compute();
				}
				
			}
			for (int i = 0; i<numResources; i++){ 
				available[i]+=releasedResources[i];
			}

			cycle++;
		}
		
		printFinishTimeBanker("Banker's", tasks, errors);
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
	
	public static int processBanker(Task[] tasks, int[] available, int[][] allocated, int[][] need, Queue<Task> blocked, int[][] max,int[] releasedResources, ArrayList<Error> errors,int i, int numTasks, int cycle, int[] resources){
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
				if (cur.getNumber() > resources[cur.getResource()]){
					tasks[i].abortTask();
					errors.add(new Error("Banker", i + 1, cur.getResource(), cur.getNumber(),resources[cur.getResource()],cycle));

				}
				else{
					tasks[i].next();
					max[i][cur.getResource()] = cur.getNumber();
					need[i][cur.getResource()] = cur.getNumber();
				}
				
			}
			else if (cur.getType().equals("request")){
				if (cur.getNumber() > need[i][cur.getResource()]){
					tasks[i].abortTask();
					errors.add(new Error("Banker", i + 1, cur.getResource(), allocated[i][cur.getResource()],resources[cur.getResource()],cycle));
					releaseAll(tasks[i], available, allocated);

				}
				else{
					if (isSafe(tasks[i], available, allocated, need, i)){
						available[cur.getResource()]-=cur.getNumber();
						allocated[i][cur.getResource()]+=cur.getNumber();
						need[i][cur.getResource()]-=cur.getNumber();
						tasks[i].next();
					}
					else{
						blocked.add(tasks[i]);
					}
				}
			}
			else if (cur.getType().equals("release")){
				releasedResources[cur.getResource()]+=cur.getNumber();
				allocated[i][cur.getResource()]-=cur.getNumber();
				need[i][cur.getResource()]+=cur.getNumber();			
				tasks[i].next();
				if (tasks[i].getActivity().getType().equals("terminate")){
					releaseAll(tasks[i], available, allocated);

				}
			}
			
			else{
				//terminate	
				tasks[i].finishTask(cycle + tasks[i].getComputeTime());
				tasksLeft--;
			}
		}
		return tasksLeft;
	}
	public static void releaseAll(Task t, int[] available, int[][] allocated){
		for (int i = 0; i < available.length; i++){
			available[i]+=allocated[t.getId()-1][i];	
		}
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
			
			for (int row = 0 ; row<need.length; row++){
				available[cur.getResource()]-=cur.getNumber();
				need[task.getId()-1][cur.getResource()]-=cur.getNumber();
				int count = 0;
				for (int col = 0; col<need[row].length; col++){

					if (available[col] >= need[row][col]){
						count++;
					}
				}
				
				available[cur.getResource()]+=cur.getNumber();
				need[task.getId()-1][cur.getResource()]+=cur.getNumber();
				int resources = need[row].length;
				if (count == resources){
					return true;
				}
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
	public static void printFinishTimeBanker(String algo, Task[] tasks, ArrayList<Error> errors){
		int totalTime = 0;
		int totalBlockedTime = 0;
		for (int i =0; i< errors.size(); i++){
			Error error = errors.get(i);
			if (errors.get(i).getCycle() == 0){
				System.out.println("Banker aborts task " + error.getTask() + " before run begins: \n\t" +
					"claim for resource " + error.getResource() + " (" + error.getNumber() + ") exceeds number of units present (" + error.getAvail()+ ")");
			}
			else{
				System.out.println("During cycle " + error.getCycle() + "-"+ (error.getCycle()+1) + " of Banker's algorithms\n\t"+
					"Task "+ error.getTask() + "\'s request exceeds its claim; aborted; " + error.getNumber() + " units available next cycle");
			}
		}
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