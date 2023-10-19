package proj;

import ecs.Archetype;
import ecs.ECS;
import printable.Printable;
import proj.components.RenderComponent;
import proj.components.TransformComponent;
import proj.systems.UnitTestSystem1;
import proj.systems.UnitTestSystem2;
import proj.systems.System;

public class Main extends Printable
{
	public static void main(String[] args)
	{
		long start = time();
		ECS ecs = new ECS();

		for (System system : new System[]{new UnitTestSystem1(),new UnitTestSystem2()})
		{
			system.tick(ecs);
		}
		ecs.process();
		print(ecs); // should be [1] entity, w/ an rC

		long end = time();
		print("Runtime: ",(end-start)/1000f,"s");
	}

	public static long time() // for ease
	{
		return java.lang.System.currentTimeMillis();
	}
}