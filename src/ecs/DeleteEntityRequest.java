package ecs;

class DeleteEntityRequest // => package-private
{
	/* This contains the data needed for ECS to process the deletion of an Entity */
	private final Archetype<?,?,?,?,?> archetype;
	private final int entityID ;

	public DeleteEntityRequest(Archetype<?,?,?,?,?> archetype, int entityID)
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
