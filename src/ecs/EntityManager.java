package ecs;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManager
{
	// EntityManager tings
	private final int maxEntities; // max size of entityIDs (cusomizable in ctor)
	private static final int ENTITYID_INVALID = -1;
	public final Set<Integer> entityIDs; // note: use of Set vs List is ~2-3x slower
	// private Set<Integer> reservedEntityIDs = new HashSet<>(); // SCRAPPED?

	// Entities (stored as ComponentBundles, keyed by Integer) keyed by Archetype
	public final Map<Archetype<?, ?, ?, ?, ?>, Map<Integer, ComponentBundle<?, ?, ?, ?, ?>>> components = new ConcurrentHashMap<>();

	// CONSTRUCTOR
	public EntityManager(int maxEntities)
	{
		this.maxEntities = maxEntities;
		entityIDs = new HashSet<>(maxEntities); // define capacity at ENTITIES_MAX instead of default (16) [this value should be lower based on ENTITIES_MAX size]
	}

	// METHODS
	/// ADD/CREATE
	/* int addEntity()
	 *
	 * RETURNS: int id OR invalid id value
	 * */
	public int addEntity(Object... components)
	{
		// test iff new eID available
		// note: this ignores "reservedIDs" for now
		int entityID = ENTITYID_INVALID;

		if (components.length == 0 || components.length > 9)
		{
			return entityID;
		}

		if (entityIDs.size() < maxEntities)
		{
			for (int i = 0; i < maxEntities; i++)
			{
				if (!entityIDs.contains(i))
				{
					// reserve
					entityID = i;
					break;
				}
			}
		} // else: v

		if (entityID == ENTITYID_INVALID) // still invalid; no free id found
		{
			return entityID;
		}

		if (!putEntity(entityID, components))
		{
			return ENTITYID_INVALID;
		}

		return entityID;
	}

	/* putEntity()
	 * - This version is intended for use when an id is already reserevd.
	 * - returns true if successful */
	public boolean putEntity(int entityID, Object... components)
	{
		// create Archetype
		Archetype<?, ?, ?, ?, ?> archetype;
		ComponentBundle<?, ?, ?, ?, ?> bundle;

		int max = 5;
		if (components.length > 5)
		{
			max = ((int) Math.ceil((float) components.length / 5) * 5) - 1; // [6-9] => 9
		}

		Class<?>[] archetypeComponentClasses = new Class<?>[max];
		Object[] bundleComponents = new Object[max];

		for (int i = 0; i < max; i++) // codify max archetype/cb value instead of magic number?
		{
			if (i < components.length)
			{
				archetypeComponentClasses[i] = components[i].getClass();
				bundleComponents[i] = components[i];
			} else
			{
				archetypeComponentClasses[i] = ECSUtil.NONE;
				bundleComponents[i] = ECSUtil.NONE;
			}
		}

		// duplicate protection
		Set<Class<?>> componentTypes = new HashSet<>();
		for (Class<?> archetypeComponentClass : archetypeComponentClasses)
		{
			if (componentTypes.contains(archetypeComponentClass) && archetypeComponentClass != ECSUtil.NONE)
			{
				return false; // tried to send multiple of a given component
			}
			componentTypes.add(archetypeComponentClass);
		}

		//
		if (max == 5)
		{
			archetype = Archetype.archetypeOf(archetypeComponentClasses[0],
					archetypeComponentClasses[1], archetypeComponentClasses[2],
					archetypeComponentClasses[3], archetypeComponentClasses[4]);
			bundle = ComponentBundle.bundleOf(bundleComponents[0],
					bundleComponents[1], bundleComponents[2],
					bundleComponents[3], bundleComponents[4]);
		} else if (max == 9)
		{
			archetype = Archetype.archetypeOf(archetypeComponentClasses[0],
					archetypeComponentClasses[1], archetypeComponentClasses[2],
					archetypeComponentClasses[3], archetypeComponentClasses[4],
					archetypeComponentClasses[5], archetypeComponentClasses[6],
					archetypeComponentClasses[7], archetypeComponentClasses[8]);
			bundle = ComponentBundle.bundleOf(bundleComponents[0],
					bundleComponents[1], bundleComponents[2],
					bundleComponents[3], bundleComponents[4],
					bundleComponents[5], bundleComponents[6],
					bundleComponents[7], bundleComponents[8]
			);
		} else // expansions available here
		{
			return false; // throw Exception
		}

		// test iff archetype exists OR add
		Map<Integer, ComponentBundle<?, ?, ?, ?, ?>> archetypeComponents = this.components.get(archetype);
		if (archetypeComponents == null) // iff not exists...
		{
			archetypeComponents = new HashMap<>(); // create...
			this.components.put(archetype, archetypeComponents); // ...and put() in big map
		}

		// finally, add
		entityIDs.add(entityID);
		archetypeComponents.put(entityID, bundle); // iff there is an entity there, too bad
		return true;
	}

	/// CHANGE


	/// REMOVE/DELETE
	public void removeEntity(DeleteEntityRequest deleteEntityRequest)
	{
		int entityID = deleteEntityRequest.getEntityID();
		Archetype<?, ?, ?, ?, ?> archetype = deleteEntityRequest.getArchetype();
		//
		Map<Integer, ComponentBundle<?, ?, ?, ?, ?>> archetypeComponentMap = this.components.get(archetype);
		if (archetypeComponentMap != null)
		{
			entityIDs.remove(entityID);
			archetypeComponentMap.remove(entityID);

			if (archetypeComponentMap.isEmpty())
			{
				this.components.remove(archetype);
			}
		}
	}

	/// OTHER
	public int size() // taxing; careful
	{
		// find a more efficient way? - ie. changing an int from add/removeEntity()
		return entityIDs.size();
	}
}
