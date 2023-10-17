package ecs;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class EntityManager
{
	// EntityManager tings
	private final int maxEntities; // max size of entityIDs (cusomizable in ctor)
	private static final int ENTITYID_INVALID = -1;
	protected final Set<Integer> entityIDs; // note: use of Set vs List is ~2-3x slower
	// private Set<Integer> reservedEntityIDs = new HashSet<>(); // SCRAPPED?

	// ComponentManager tings
	protected final Map<Archetype<?, ?, ?, ?, ?>, Map<Integer, ComponentBundle<?, ?, ?, ?, ?>>> components = new ConcurrentHashMap<>();

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
	private boolean putEntity(int entityID, Object... components)
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
	public void changeEntityArchetype(int entityID, ChangeArchetypeEntityRequest changeArchetypeEntityRequest)
	{
		// printf("Starting CAER of +[%d], -[%d] to %s", changeArchetypeEntityRequest.componentsToAdd.size(), changeArchetypeEntityRequest.componentsToRemove.size(), changeArchetypeEntityRequest.getOldArchetype());
		Archetype<?, ?, ?, ?, ?> oldArchetype = changeArchetypeEntityRequest.getOldArchetype();

		Map<Integer, ComponentBundle<?, ?, ?, ?, ?>> archetypeComponentMap = this.components.get(oldArchetype);
		if (archetypeComponentMap == null)
		{
			// throw Exception - archetype doesn't exist => eID can't exist
			return;
		}

		ComponentBundle<?, ?, ?, ?, ?> oldBundle = archetypeComponentMap.get(entityID);
		if (oldBundle == null)
		{
			// throw Exception - eID doesn't exist
			return;
		}

		Set<Object> finalComponents = new HashSet<>();

		Set<Class<?>> componentsToRemove = changeArchetypeEntityRequest.componentsToRemove;
		ArrayList<?> componentsToAdd = new ArrayList<>(changeArchetypeEntityRequest.componentsToAdd);

		Object[] oldComponents = new Object[(int) (Math.ceil((float) oldBundle.size() / 5) * 5)]; // round up to nearest 5
		oldComponents[0] = oldBundle.getT1();
		oldComponents[1] = oldBundle.getT2();
		oldComponents[2] = oldBundle.getT3();
		oldComponents[3] = oldBundle.getT4();
		oldComponents[4] = oldBundle.getT5();
		if (oldComponents.length > 5) // standardize single size (5) as var?
		{
			ComponentBundle<?, ?, ?, ?, ?> oldBundleNested = (ComponentBundle<?, ?, ?, ?, ?>) oldBundle.getT5();
			oldComponents[4] = oldBundleNested.getT1();
			oldComponents[5] = oldBundleNested.getT2();
			oldComponents[6] = oldBundleNested.getT3();
			oldComponents[7] = oldBundleNested.getT4();
			oldComponents[8] = oldBundleNested.getT5();
		}

		for (Object oldComponent : oldComponents)
		{
			finalComponents.add(getNextBundleType(oldComponent,
					componentsToRemove, componentsToAdd, oldArchetype));
		}

		finalComponents.remove(Void.class); // remove any Voids iff present
		// process remaining componentsToAdd iff any remain and
		// finalComponents.size() permits.
		// +overflow will be added NEXT
		while (finalComponents.size() < 5 && componentsToAdd.size() > 0)
		{
			finalComponents.add(componentsToAdd.remove(0));
		}

		archetypeComponentMap.remove(entityID);
		if (finalComponents.size() > 0)
		{
			// postcond
			putEntity(entityID, finalComponents.toArray());
		} // else: exception?

		if (archetypeComponentMap.isEmpty())
		{
			this.components.remove(oldArchetype);
		}

		// print("Processed CAER -> ",ComponentBundle.bundleOf(finalComponents.toArray()[0], finalComponents.toArray()[1]));
		// prints("Processed CAER -> ");
		// print(finalComponents.toArray());
	}

	/* getNextBundleType()
	 * Used for changeEntityArchetype as a helper;
	 * Determines what the next element in the bundle should be based on
	 * Existing element types, removals, and additions*/
	private Object getNextBundleType(Object t,
									 Set<Class<?>> componentsToRemove,
									 ArrayList<?> componentsToAdd,
									 Archetype<?, ?, ?, ?, ?> oldArchetype)
	{
		Object _t = t;
		for (Class<?> componentToRemove : componentsToRemove)
		{
			// print("CAER: comparing ", componentToRemove, "to", t.getClass());
			if (!componentToRemove.equals(t.getClass())) // no match
			{
				continue;
			}

			_t = Void.class; // will be purged by primary method

			if (componentsToAdd.size() == 0) // add is empty
			{
				return _t;
			}

			// try to be next ToAdd component instead
			_t = componentsToAdd.remove(0); // -> Stack?

			if (oldArchetype.has(_t.getClass())) // class in archetype already
			{
				return Void.class;
			}
			return _t;
		}

		return t;
	}

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
