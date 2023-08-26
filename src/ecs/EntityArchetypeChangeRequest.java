package ecs;

class EntityArchetypeChangeRequest // => package-private
{
	private final int entityID ;
	private final Archetype<?,?,?,?,?> oldArchetype;
	private final ComponentBundle<?,?,?,?,?> newBundle ;

	EntityArchetypeChangeRequest(Archetype<?,?,?,?,?> oldArchetype, int entityID,
								 ComponentBundle<?,?,?,?,?> newBundle)
	{
		this.oldArchetype = oldArchetype;
		this.entityID = entityID;
		this.newBundle = newBundle;
	}

	// getters
	public int getEntityID()
	{
		return entityID;
	}

	public Archetype<?, ?, ?, ?, ?> getOldArchetype()
	{
		return oldArchetype;
	}

	public ComponentBundle<?, ?, ?, ?, ?> getNewBundle()
	{
		return newBundle;
	}
}
