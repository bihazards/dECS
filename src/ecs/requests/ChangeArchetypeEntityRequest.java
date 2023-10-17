package ecs.requests;

import ecs.Archetype;
import ecs.ComponentBundle;
import ecs.Entity;
import ecs.EntityManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ChangeArchetypeEntityRequest implements Request // => package-private
{
	// private final Archetype<?,?,?,?,?> oldArchetype;
	private final Entity entity;
	private final EntityManager entityManager;

	public final Set<Class<?>> componentsToRemove;
	public final Set<Object> componentsToAdd;

	public ChangeArchetypeEntityRequest(Entity entity, EntityManager entityManager)
	{
		this.entity = entity;
		this.entityManager = entityManager;
		this.componentsToAdd = new HashSet<>();
		this.componentsToRemove = new HashSet<>();
	}

	// METHODS
	/// UNIQUE
	private Archetype<?,?,?,?,?> getOldArchetype()
	{
		return entity.getArchetype();
	}

	private int getEntityID()
	{
		return entity.getEntityID();
	}

	/// INHERITED
	public void process()
	{
		changeEntityArchetype(getEntityID());
	}

	private void changeEntityArchetype(int entityID)
	{
		Archetype<?, ?, ?, ?, ?> oldArchetype = getOldArchetype();

		Map<Integer, ComponentBundle<?, ?, ?, ?, ?>> archetypeComponentMap = entityManager.entityBundlesByArchetype.get(oldArchetype);
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

		Set<Class<?>> componentsToRemove = this.componentsToRemove;
		ArrayList<?> componentsToAdd = new ArrayList<>(this.componentsToAdd);

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
			entityManager.putEntity(entityID, finalComponents.toArray());
		} // else: exception?

		if (archetypeComponentMap.isEmpty())
		{
			entityManager.entityBundlesByArchetype.remove(oldArchetype);
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
}
