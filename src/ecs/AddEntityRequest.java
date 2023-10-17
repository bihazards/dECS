package ecs;

public class AddEntityRequest implements Request
{
	private EntityManager entityManager;

	private final Object [] components ;

	public AddEntityRequest(EntityManager entityManager, Object ... components)
	{
		this.entityManager = entityManager;
		this.components = components;
	}
	
	// METHODS
	/// INHERITED
	public void process()
	{
		addEntity(components);
	}

	/* int addEntity()
	 *
	 * RETURNS: int id OR invalid id value
	 * */
	public int addEntity(Object... components)
	{
		// test iff new eID available
		// note: this ignores "reservedIDs" for now
		int entityID = EntityManager.ENTITYID_INVALID;

		if (components.length == 0 || components.length > 9)
		{
			return entityID;
		}

		if (entityManager.entityIDs.size() < entityManager.maxEntities)
		{
			for (int i = 0; i < entityManager.maxEntities; i++)
			{
				if (!entityManager.entityIDs.contains(i))
				{
					// reserve
					entityID = i;
					break;
				}
			}
		} // else: v

		if (entityID == EntityManager.ENTITYID_INVALID) // still invalid; no free id found
		{
			return entityID;
		}

		if (!entityManager.putEntity(entityID, components))
		{
			return EntityManager.ENTITYID_INVALID;
		}

		return entityID;
	}
}
