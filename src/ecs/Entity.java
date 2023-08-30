package ecs;

public class Entity // package-private
{
	private final Archetype<?,?,?,?,?> archetype;
	private final int entityID ;
	private final ComponentBundle<?,?,?,?,?> bundle;

	public Entity(Archetype<?,?,?,?,?> archetype, int entityID, ComponentBundle<?,?,?,?,?> bundle)
	{
		this.archetype = archetype;
		this.entityID = entityID;
		this.bundle = bundle;
	}

	// getters
	public Archetype<?, ?, ?, ?, ?> getArchetype()
	{
		return archetype;
	}

	public int getEntityID()
	{
		return entityID;
	}

	public ComponentBundle<?, ?, ?, ?, ?> getBundle()
	{
		return bundle;
	}
}
