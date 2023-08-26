package ecs;

import java.util.ArrayList;
import java.util.List;

public abstract class System
{
	public final List<ComponentBundle<?,?,?,?,?>> entitiesToCreate = new ArrayList<>();
	public final List<EntityDeletionRequest> entitiesToRemove = new ArrayList<>();
	public final List<EntityArchetypeChangeRequest> entitiesToChange = new ArrayList<>();

	public abstract void tick();

	public final void queueAddEntity(ComponentBundle<?,?,?,?,?> componentBundle)
	{
		entitiesToCreate.add(componentBundle);
	}

	public final void requestEntityDeletion(Archetype<?,?,?,?,?> archetype, int entityID)
	{
		entitiesToRemove.add(new EntityDeletionRequest(archetype, entityID));
	}

	public final void requestEntityArchetypeChange(Archetype<?,?,?,?,?> oldArchetype, int entityID, ComponentBundle<?,?,?,?,?> newBundle)
	{
		entitiesToChange.add(new EntityArchetypeChangeRequest(oldArchetype, entityID, newBundle));
	}
}
