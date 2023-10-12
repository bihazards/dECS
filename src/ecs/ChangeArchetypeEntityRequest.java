package ecs;

import java.util.HashSet;
import java.util.Set;

class ChangeArchetypeEntityRequest // => package-private
{
	private final Archetype<?,?,?,?,?> oldArchetype;

	Set<Class<?>> componentsToRemove = new HashSet<>();
	Set<Object> componentsToAdd = new HashSet<>();

	ChangeArchetypeEntityRequest(Archetype<?,?,?,?,?> oldArchetype)
	{
		this.oldArchetype = oldArchetype;
	}

	//

	public Archetype<?,?,?,?,?> getOldArchetype()
	{
		return oldArchetype;
	}
}
