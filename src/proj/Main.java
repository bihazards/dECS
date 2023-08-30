package proj;

import ecs.ECS;
import printable.Printable;
import proj.systems.System1;
import proj.systems.System2;

public class Main extends Printable
{
	public static void main(String[] args)
	{
		long start = System.currentTimeMillis();
		ECS ecs = new ECS();

		ecs.addSystem(new System1());
		ecs.addSystem(new System2());
		ecs.update();
		ecs.printAllComponents();

		long end = System.currentTimeMillis();
		print("Runtime: ",(end-start)/1000f,"s");
	}

	/*
		ANTICIPATED ISSUE 1: What happens iff EM entityID list ends up out of sync
		w/ ids in CM? [can this happen beyond editing ECS?]
	 */
}