package ecs;

import java.util.HashSet;
import java.util.Set;

class EntityArchetypeChangeRequest // => package-private
{
	private final Archetype<?,?,?,?,?> oldArchetype;

	Set<Class<?>> componentsToRemove = new HashSet<>();
	Set<Object> componentsToAdd = new HashSet<>();

	EntityArchetypeChangeRequest(Archetype<?,?,?,?,?> oldArchetype)
	{
		this.oldArchetype = oldArchetype;
	}

	//

	public Archetype<?,?,?,?,?> getOldArchetype()
	{
		return oldArchetype;
	}
}
