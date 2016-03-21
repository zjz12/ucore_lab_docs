package l9;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;  

public class work {
	static int[] input = {2, 2, 3, 1, 2, 4, 2, 4, 0, 3};
	static int T = 4;
	static int memory_size = 3;
	static List<Integer> work = new ArrayList<Integer>();
	static List<Integer> memory = new ArrayList<Integer>();
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		memory.add(4);
		memory.add(3);
		memory.add(0);
		work.add(4);
		work.add(3);
		work.add(0);
		for(int i = 0;i < 10;i++) {
			System.out.println("i:"+(i+1));
			//更新工作集
			work.add(input[i]);
			while(work.size() > T)
				work.remove((int)0);	
			
			if(!memory.contains(input[i])){		//缺页
				System.out.println("缺页");
				memory.add(input[i]);
			}
			for(int j = 0;j < memory.size();j++){
				Integer tmp = new Integer(memory.get(j));
				if(!work.contains(tmp)){
					memory.remove(tmp);
				}
			}
			System.out.print("work:");
			for(Integer tmp : work){
				System.out.print(tmp+" ");
			}
			System.out.print("\nmemory:");
			for(Integer tmp : memory){
				System.out.print(tmp+" ");
			}
			System.out.println("");
			
		}
		
	}

}
