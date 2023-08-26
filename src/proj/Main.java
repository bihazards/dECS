package proj;

import ecs.Archetype;
import ecs.ECS;
import ecs.Entity;
import printable.Printable;
import proj.components.InputComponent;
import proj.components.RenderComponent;
import proj.components.TransformComponent;

import java.awt.*;

public class Main extends Printable
{
	public static void main(String[] args)
	{
		long start = System.currentTimeMillis();
		ECS ecs = new ECS();

		ecs.addEntity(new TransformComponent(3,14));

		ecs.addEntity(new RenderComponent(5));

		ecs.addEntity(new RenderComponent(5), new TransformComponent(16,12));

		ecs.addEntity(new TransformComponent(16,12), new RenderComponent(5));

		ecs.printAllComponents();

		print(ecs.getEntitiesByArchetype(Archetype.archetypeOf(TransformComponent.class,RenderComponent.class)).size()); // why does this not work anymore ?

		for (Entity entity : ecs.getEntitiesByArchetype(Archetype.archetypeOf(TransformComponent.class,RenderComponent.class)) )
		{
			TransformComponent transformComponent = entity.components.get(TransformComponent.class);
			RenderComponent renderComponent = entity.components.get(RenderComponent.class);
			// printf("tC[?null=%b], rC[?null=%b]",transformComponent==null,renderComponent==null);
			printf("[%d].T=(%d,%d); .R=(depth=%d)",entity.entityID,transformComponent.x,transformComponent.y,renderComponent.depth);
		}

		for (Entity entity : ecs.getEntitiesWith(TransformComponent.class))
		{
			TransformComponent transformComponent = entity.components.get(TransformComponent.class);

			printf("[%d].T=(%d,%d)",entity.entityID,transformComponent.x,transformComponent.y);
		}

		print(ecs.getEntitiesByArchetype(Archetype.archetypeOf(InputComponent.class)).size());

		// TEST needed: incorrect archetype, incorrect With() 	[done, done]
		// TEST needed: With()									[done]

		long end = System.currentTimeMillis();
		print("Runtime: ",(end-start)/1000f,"s");
	}

	/* */

	/*
		MAJOR ISSUE: now archetypes are combined...
		FIXED BY no longer needing getClass() in Archetype because all
				passed generic classes Tx = Class<E>
	 */

	/*
		MAJOR ISSUE: archetypes don't seem to be linked properly anymore
		FIXED BY passing CLASSES of proj.components to Archetype.archetypeOf()
	 */

	/*
		MAJOR ISSUE: getEntitiesByArchetype() no longer seems to work
		FIXED BY passing CLASSES of proj.components to Archetype.archetypeOf()
	 */

	/*
	* ISSUE: Archetypes must be in the same order every call :/
	* FIXED by changing hashing for Archetypes
	* */

	/* ISSUE: how to actually manipulate the proj.components when retrieving them?
	* - Can't simply use an array or List because how do you get a *specific*
	* proj.components?
	*
	* POSSIBLE SOLUTION: use a ComponentBundle<> generic?
	* _Component _component = bundle.get(_Component.class)
	* -> public <T extends Component> T ComponentBundle::get(Class<T> componentClass)
	* {
	* 	// check t1,t2,... for match w/ componentClass
	* 	// return match OR null
	* }
	 */

	/*
	* ANTICIPATED ISSUE 0: How would an entity's composition be altered post-creation?
		- An immutable CB is what's sent to Systems/acquirable by ECS getters
		- Even directly changing the value of the Map creates an incompatibility between the Archetype
		* and the nested ComponentBundle.

	* QUESTION: With reduced volume of Components, when would this actually happen? Most common changes are to physics,
		attacks, etc. which are now frequent elements.
	*
	* POSSIBLE SOLUTION 1: With reduced volume of Components, ensure entities never have to change structure after
			creation? Delete and recreate if alterations need to come?

	* POSSIBLE SOLUTION 2: Systems must be capable of storing, akin to Entity creation/removal, a list of operations to
			change the composition of entities. This would store the Archetype to perform the lookup of the entity at,
			then the ID, then an operation. ECS would handle this before proceeding to the next System (or alternatively/
			possibly preferably, at the end of the tick()).
	 */

	/*
		ANTICIPATED ISSUE 1: What happens iff EM entityID list ends up out of sync w/ ids in CM? [can this happen?]
	 */

	/*
		ANTICIPATED ISSUE 2: What happens iff EM entityID list ends up out of sync w/ ids in CM? [can this happen?]
	 */
}