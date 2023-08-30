package ecs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class System
{
	public final List<ComponentBundle<?,?,?,?,?>> entitiesToCreate = new ArrayList<>();
	public final List<EntityDeletionRequest> entitiesToRemove = new ArrayList<>();
	public final HashMap<Integer,EntityArchetypeChangeRequest> entitiesToChange = new HashMap<>();

	public abstract void tick(ECS ecs);

	public final void queueAddEntity(ComponentBundle<?,?,?,?,?> componentBundle)
	{
		entitiesToCreate.add(componentBundle);
	}

	public final void requestEntityDeletion(Entity entity)
	{
		entitiesToRemove.add(new EntityDeletionRequest(entity.getArchetype(), entity.getEntityID()));
	}

	private EntityArchetypeChangeRequest requestEntityArchetypeChange(Entity entity, Object ... components)
	{
		if (components.length == 0)
		{
			// throw Exception;
			return null ;
		}

		// check iff entity has change request already
		int entityID = entity.getEntityID();

		EntityArchetypeChangeRequest entityArchetypeChangeRequest = entitiesToChange.get(entityID);
		if (entityArchetypeChangeRequest == null) // doesn't exist already
		{
			entityArchetypeChangeRequest = new EntityArchetypeChangeRequest(entity.getArchetype());
			entitiesToChange.put(entityID, entityArchetypeChangeRequest);
		}
		return entityArchetypeChangeRequest;
	}

	public final void requestEntityAddComponent(Entity entity, Object ... componentsToAdd)
	{ // note: can be called w/ 0-length; can fix with a definite param before varargs but eh
		EntityArchetypeChangeRequest entityArchetypeChangeRequest = requestEntityArchetypeChange(entity, componentsToAdd);

		for (Object componentToAdd : componentsToAdd)
		{
			entityArchetypeChangeRequest.componentsToAdd.add(componentToAdd);
		}
	}

	public final void requestEntityRemoveComponent(Entity entity, Class<?> ... componentsToRemove)
	{
		EntityArchetypeChangeRequest entityArchetypeChangeRequest = requestEntityArchetypeChange(entity, componentsToRemove);

		for (Class<?> componentToRemove : componentsToRemove)
		{
			entityArchetypeChangeRequest.componentsToRemove.add(componentToRemove);
		}
	}

	/* NEXT:
		- request removal of components rather than wholesale component change
		- using a Map to clump moves/deletion from the same archetype together
			- ArchetypeChangeRequest would no longer need old archetype? */

}
