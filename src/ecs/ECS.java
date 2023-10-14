package ecs;

import printable.Printable;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ECS extends Printable
{
	// EntityManager tings
	private final int maxEntities; // max size of entityIDs (cusomizable in ctor)
	private static final int ENTITYID_INVALID = -1;
	private final Set<Integer> entityIDs; // note: use of Set vs List is ~2-3x slower
	// private Set<Integer> reservedEntityIDs = new HashSet<>();

	// ComponentManager tings
	private final Map<Archetype<?, ?, ?, ?, ?>, Map<Integer, ComponentBundle<?, ?, ?, ?, ?>>> components = new ConcurrentHashMap<>();

	// Requests
	private final List<ComponentBundle<?, ?, ?, ?, ?>> entitiesToAdd = new ArrayList<>();
	private final List<DeleteEntityRequest> entitiesToDelete = new ArrayList<>();
	private final HashMap<Integer, ChangeArchetypeEntityRequest> entitiesToChange = new HashMap<>();

	// ctor
	public ECS()
	{
		this(1000); // default
	}

	public ECS(int maxEntities)
	{
		// instantiate collections here

		this.maxEntities = maxEntities;
		entityIDs = new HashSet<>(maxEntities); // define capacity at ENTITIES_MAX instead of default (16) [this value should be lower based on ENTITIES_MAX size]
	}

	// methods
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

	private Archetype<?, ?, ?, ?, ?> nestArchetypes(Object... archetypeComponentClasses)
	{
		// number of nests is based on classes % 4 (mod) and classes / 4 (whole)
		// iff mod=1 or whole=0 => nests = min(0,whole - 1)
		// [ie. 5/4=>mod=1,whole=1=>1-1=>0][ie2. 2/4=>whole=0=>0]
		// eliff mod>1 (implied: and whole>0) => nests = whole
		// [ie. 6/4=>mod=2,whole=1=>1][ie2. 7/4=>mod=3,whole=1=>1]
		// eliff mod==0 (implied: and whole>0) => nests = whole-1
		// [ie. 8/4=>mod=0,whole=2=>2-1=>1]
		Object t5 = archetypeComponentClasses[4];
		if (archetypeComponentClasses.length > 5) // MAGIC NUMBER
		{
			t5 = nestArchetypes(ECSUtil.copyOfRangeAndFill(archetypeComponentClasses, 5, archetypeComponentClasses.length, ECSUtil.NONE));
		}
		return Archetype.archetypeOf(archetypeComponentClasses[0],
				archetypeComponentClasses[1], archetypeComponentClasses[2],
				archetypeComponentClasses[3], t5);
	}

	public void changeEntityArchetype(int entityID, ChangeArchetypeEntityRequest changeArchetypeEntityRequest)
	{
		printf("Starting CAER of +[%d], -[%d] to %s", changeArchetypeEntityRequest.componentsToAdd.size(), changeArchetypeEntityRequest.componentsToRemove.size(), changeArchetypeEntityRequest.getOldArchetype());
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
		prints("Processed CAER -> ");
		print(finalComponents.toArray());
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
			print("CAER: comparing ", componentToRemove, "to", t.getClass());
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
			_t = componentsToAdd.remove(0);

			if (oldArchetype.has(_t.getClass())) // class in archetype already
			{
				return Void.class;
			}
			return _t;
		}

		return t;
	}

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

	//
	public final void requestAddEntity(ComponentBundle<?, ?, ?, ?, ?> components)
	{
		entitiesToAdd.add(components);
	}

	public final void requestDeleteEntity(Entity entity)
	{
		entitiesToDelete.add(new DeleteEntityRequest(entity.getArchetype(), entity.getEntityID()));
	}

	private ChangeArchetypeEntityRequest requestChangeEntityArchetype(Entity entity, Object... components)
	{
		if (components.length == 0)
		{
			// throw Exception;
			return null;
		}

		// check iff entity has change request already
		int entityID = entity.getEntityID();

		ChangeArchetypeEntityRequest changeArchetypeEntityRequest = entitiesToChange.get(entityID); // iff exists already, use existing request
		if (changeArchetypeEntityRequest == null) // doesn't exist already
		{
			changeArchetypeEntityRequest = new ChangeArchetypeEntityRequest(entity.getArchetype());
			entitiesToChange.put(entityID, changeArchetypeEntityRequest);
		}
		return changeArchetypeEntityRequest;
	}

	public void requestAddComponent(Entity entity, Object... componentsToAdd)
	{ // note: can be called w/ 0-length; can fix with a definite param before varargs but eh
		ChangeArchetypeEntityRequest changeArchetypeEntityRequest = requestChangeEntityArchetype(entity, componentsToAdd);

		for (Object componentToAdd : componentsToAdd)
		{
			changeArchetypeEntityRequest.componentsToAdd.add(componentToAdd);
		}
	}

	public void requestRemoveComponent(Entity entity, Class<?>... componentsToRemove)
	{
		ChangeArchetypeEntityRequest changeArchetypeEntityRequest = requestChangeEntityArchetype(entity, componentsToRemove);

		for (Class<?> componentToRemove : componentsToRemove)
		{
			changeArchetypeEntityRequest.componentsToRemove.add(componentToRemove);
		}
	}

	//
	public List<Entity> entitiesWithArchetype(Archetype<?, ?, ?, ?, ?> archetype)
	{
		// Map<Integer,ArrayList<Component>> entityMap = new HashMap<>();
		// return this.proj.components.get(archetype);
		List<Entity> entities = new ArrayList<>();
		Map<Integer, ComponentBundle<?, ?, ?, ?, ?>> entityMap = this.components.get(archetype);
		print("searching for archetype ", archetype);
		if (entityMap != null)
		{
			print("found archetype ", archetype);
			for (Map.Entry<Integer, ComponentBundle<?, ?, ?, ?, ?>> entityEntry : entityMap.entrySet())
			{
				// key = eID
				/*if (entityIDs.contains(entityEntry.getKey())) // eID is invalid -> has been removed from EM
				{
					continue ;
					// removeEntity() ?
				}*/ // removed validation; removeEntity() should be handling this
				addEntryToEntityList(entities, archetype, entityEntry);
			}
		}
		return entities;
	}

	// what will the use case be for such a specific parse as opposed to eWCs()?
	public List<Entity> entitiesWithArchetypes(Archetype<?, ?, ?, ?, ?>... archetypes)
	{
		List<Entity> entities = new ArrayList<>();

		for (Archetype<?, ?, ?, ?, ?> archetype : archetypes)
		{
			Map<Integer, ComponentBundle<?, ?, ?, ?, ?>> entityMap = this.components.get(archetype);
			print("searching for archetype ", archetype);

			if (entityMap == null)
			{
				continue;
			}

			for (Map.Entry<Integer, ComponentBundle<?, ?, ?, ?, ?>> entityEntry : entityMap.entrySet())
			{
				// key = eID
				/*if (entityIDs.contains(entityEntry.getKey())) // eID is invalid -> has been removed from EM
				{
					continue;
					// removeEntity() ?
				}*/
				addEntryToEntityList(entities, archetype, entityEntry);
			}
		}
		return entities;
	}

	public List<Entity> entitiesWithComponents(Class<?>... componentClasses)
	{
		List<Entity> entities = new ArrayList<>();

		aLoop:
		for (Map.Entry<Archetype<?, ?, ?, ?, ?>, Map<Integer, ComponentBundle<?, ?, ?, ?, ?>>> archetypeEntry : this.components.entrySet())
		{
			// test has proj.components
			Archetype<?, ?, ?, ?, ?> archetype = archetypeEntry.getKey();

			for (Class<?> componentClass : componentClasses)
			{
				print("searching for entities w/ ", componentClass, "in", archetype);
				if (!archetype.has(componentClass))
				{
					continue aLoop;
				}
			}
			prints("found entities w/ ");
			for (Class<?> componentClass : componentClasses)
			{
				prints(componentClass.toString());
				prints(",");
			}
			print("in ", archetype);

			// add
			Map<Integer, ComponentBundle<?, ?, ?, ?, ?>> archetypeComponents = archetypeEntry.getValue();
			for (Map.Entry<Integer, ComponentBundle<?, ?, ?, ?, ?>> entityEntry : archetypeComponents.entrySet())
			{
				addEntryToEntityList(entities, archetype, entityEntry);
			}
		}
		return entities;
	}

	private void addEntryToEntityList(List<Entity> entities, Archetype<?, ?, ?, ?, ?> archetype, Map.Entry<Integer, ComponentBundle<?, ?, ?, ?, ?>> entityEntry)
	{
		entities.add(entryToEntity(archetype, entityEntry));
	}

	private Entity entryToEntity(Archetype<?, ?, ?, ?, ?> archetype, Map.Entry<Integer, ComponentBundle<?, ?, ?, ?, ?>> entityEntry)
	{
		return new Entity(archetype, entityEntry.getKey(), entityEntry.getValue());
	}

	public int size() // taxing; careful
	{
		// find a more efficient way? - ie. changing an int from add/removeEntity()
		return entityIDs.size();
	}

	//

	public void process()
	{
		// process requests
		for (ComponentBundle<?, ?, ?, ?, ?> bundle : entitiesToAdd)
		{
			addEntity(bundle); // if one of the ? is Void, that's fine
		}
		entitiesToAdd.clear();

		for (DeleteEntityRequest deleteEntityRequest : entitiesToDelete)
		{
			removeEntity(deleteEntityRequest);
			printf("Processed DER of [%d]@%s; new size -> %d", deleteEntityRequest.getEntityID(), deleteEntityRequest.getArchetype(), size());
		}
		entitiesToDelete.clear();

		for (Map.Entry<Integer, ChangeArchetypeEntityRequest> entityArchetypeChangeRequestEntry : entitiesToChange.entrySet())
		{
			int entityID = entityArchetypeChangeRequestEntry.getKey();
			ChangeArchetypeEntityRequest changeArchetypeEntityRequest = entityArchetypeChangeRequestEntry.getValue();

			changeEntityArchetype(entityID, changeArchetypeEntityRequest);
		}
		entitiesToChange.clear();
	}

	//
	public void printAllComponents()
	{
		for (Map.Entry<Archetype<?, ?, ?, ?, ?>, Map<Integer, ComponentBundle<?, ?, ?, ?, ?>>> archetypeEntry : this.components.entrySet())
		{
			Archetype<?, ?, ?, ?, ?> archetype = archetypeEntry.getKey();
			Map<Integer, ComponentBundle<?, ?, ?, ?, ?>> archetypeComponents = archetypeEntry.getValue();

			print("Archetype: ", archetype);
			for (Map.Entry<Integer, ComponentBundle<?, ?, ?, ?, ?>> archetypeComponentEntry : archetypeComponents.entrySet())
			{
				int entityID = archetypeComponentEntry.getKey();
				ComponentBundle<?, ?, ?, ?, ?> componentBundle = archetypeComponentEntry.getValue();

				print(entityID, "\t|\t", componentBundle.toString());
			}
			print("--------------------------");
		}
	}
}
