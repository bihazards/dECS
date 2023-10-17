package proj.systems;

import ecs.*;
import printable.Printable;
import proj.components.InputComponent;
import proj.components.RenderComponent;
import proj.components.TransformComponent;

public class UnitTestSystem1 implements System
{
	public void tick(ECS ecs)
	{
		// Test 1: Simple addition
		ecs.requestAddEntity(new TransformComponent(3,14));
		// Test 2: Addition of second archetype
		ecs.requestAddEntity(new RenderComponent(5));
		// Test 3: Interchangeable order testing + 3rd archetype
		ecs.requestAddEntity(new RenderComponent(5), new TransformComponent(16,12));
		ecs.requestAddEntity(new TransformComponent(16,12), new RenderComponent(5));
		ecs.process();

		Printable.print("all components [should be 4 entities->2 w/ rC+tC]");
		ecs.printAllComponents();
		Printable.print(ecs.entitiesWithArchetype(Archetype.archetypeOf(TransformComponent.class,RenderComponent.class)).size()); // why does this not work anymore ?

		// testing searching by archetype
		for (Entity entity : ecs.entitiesWithArchetype(Archetype.archetypeOf(TransformComponent.class,RenderComponent.class)) )
		{
			TransformComponent transformComponent = entity.getBundle().get(TransformComponent.class);
			RenderComponent renderComponent = entity.getBundle().get(RenderComponent.class);
			// printf("tC[?null=%b], rC[?null=%b]",transformComponent==null,renderComponent==null);
			Printable.printf("[%d].T=(%d,%d); .R=(depth=%d)",entity.getEntityID(),transformComponent.x,transformComponent.y,renderComponent.depth);
		}

		// testing searching by one specific component
		for (Entity entity : ecs.entitiesWithComponents(TransformComponent.class))
		{
			TransformComponent transformComponent = entity.getBundle().get(TransformComponent.class);

			Printable.printf("[%d].T=(%d,%d)",entity.getEntityID(),transformComponent.x,transformComponent.y);
		}

		Printable.print(ecs.entitiesWithArchetype(Archetype.archetypeOf(InputComponent.class)).size()); // expected => 0

		// TEST needed: incorrect archetype, incorrect With() 	[done, done]
		// TEST needed: With()									[done]

		// test EntityDeletionRequest
		for (Entity entity : ecs.entitiesWithComponents(RenderComponent.class))
		{
			ecs.requestDeleteEntity(entity);
		}

		// testing dupe components
		int id = ecs.requestAddEntity(new RenderComponent(0), new TransformComponent(1,2), new TransformComponent(2,3));
		ecs.process();
		Printable.print("id=",id);
		ecs.printAllComponents(); // should be 4 entities still, as EDR doesn't process immediately (yet)
		//
		Printable.print("UNIT TEST 1 COMPLETE");
		Printable.print("--------------------");
	}
}
