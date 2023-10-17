package ecs;

import java.util.Map;

public class DeleteEntityRequest implements Request // => package-private
{
	/* This contains the data needed for ECS to process the deletion of an Entity */
	private final Entity entity;
	private final EntityManager entityManager;

	public DeleteEntityRequest(Entity entity, EntityManager entityManager)
	{
		this.entity = entity;
		this.entityManager = entityManager;
	}

	// METHODS
	/// INHERITED
	public void process()
	{
		removeEntity();
	}

	public void removeEntity()
	{
		int entityID = getEntityID();
		Archetype<?, ?, ?, ?, ?> archetype = getArchetype();
		
		//
		Map<Integer, ComponentBundle<?, ?, ?, ?, ?>> archetypeComponentMap = entityManager.components.get(archetype);
		if (archetypeComponentMap != null)
		{
			entityManager.entityIDs.remove(entityID);
			archetypeComponentMap.remove(entityID);

			if (archetypeComponentMap.isEmpty())
			{
				entityManager.components.remove(archetype);
			}
		}
	}

	/// GETTERS
	public Archetype<?,?,?,?,?> getArchetype()
	{
		return entity.getArchetype();
	}

	public int getEntityID()
	{
		return entity.getEntityID();
	}
}
