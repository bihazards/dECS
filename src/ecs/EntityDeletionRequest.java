package ecs;

class EntityDeletionRequest // => package-private
{
	/* This contains the data needed for ECS to process the deletion of an Entity */
	private final Archetype<?,?,?,?,?> archetype;
	private final int entityID ;

	public EntityDeletionRequest(Archetype<?,?,?,?,?> archetype, int entityID)
	{
		this.archetype = archetype;
		this.entityID = entityID;
	}

	// getters
	public Archetype<?,?,?,?,?> getArchetype()
	{
		return archetype;
	}

	public int getEntityID()
	{
		return entityID;
	}
}
