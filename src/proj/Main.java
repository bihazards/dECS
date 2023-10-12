package proj;

import ecs.Archetype;
import ecs.ECS;
import printable.Printable;
import proj.components.RenderComponent;
import proj.components.TransformComponent;
import proj.systems.UnitTestSystem1;
import proj.systems.UnitTestSystem2;

public class Main extends Printable
{
	public static void main(String[] args)
	{
		long start = System.currentTimeMillis();
		ECS ecs = new ECS();

		ecs.addSystem(new UnitTestSystem1());
		ecs.addSystem(new UnitTestSystem2());
		ecs.update();
		ecs.printAllComponents(); // should be [1] entity, w/ an rC

		// ecs.addEntity(new RenderComponent(1),new TransformComponent(1,2),new TransformComponent(1,2));

		long end = System.currentTimeMillis();
		print("Runtime: ",(end-start)/1000f,"s");
	}
}