package proj.systems;

import ecs.Archetype;
import ecs.ECS;
import ecs.Entity;
import printable.Printable;
import proj.components.RenderComponent;
import proj.components.TransformComponent;

public class UnitTestSystem2 extends System
{
	public void tick(ECS ecs)
	{
		// testing CAERs
		for (Entity entity : ecs.entitiesWithArchetype(Archetype.archetypeOf(TransformComponent.class)))
		{
			ecs.requestAddComponent(entity, new RenderComponent(5));
			ecs.requestRemoveComponent(entity, TransformComponent.class);
		}

		print("UNIT TEST 2 COMPLETE");
		print("--------------------");
	}

	/* ISSUE - CAERs print that they're processed properly in the console,
	* but ultimately do not show that they were.
	*
	* UPDATE - They print, but the new Component is type Class; the old Component
	* is the correct type.
	*
	* FIXED - System method was passing .getClass() of each component*/

	/*
		- CAERs work for ADDing components, but not for REMOVing.
		- Removals do seemingly nothing => CAER reads addition properly but not removal.

	 FIXED - changed componentsToRemove to also reference Class<?> and fixed ECS
	 references.
	 */
}
