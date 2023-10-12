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

	// SystemManager tings
	private final List<System> systems = new ArrayList<>();

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

		/*switch (components.length)
		{
			case 1:
				archetype = Archetype.archetypeOf(components[0].getClass());
				bundle = ComponentBundle.bundleOf(components[0]);
				break;
			case 2:
				archetype = Archetype.archetypeOf(components[0].getClass(), components[1].getClass());
				bundle = ComponentBundle.bundleOf(components[0], components[1]);
				break;
			case 3:
				archetype = Archetype.archetypeOf(components[0].getClass(), components[1].getClass(), components[2].getClass());
				bundle = ComponentBundle.bundleOf(components[0], components[1], components[2]);
				break;
			case 4:
				archetype = Archetype.archetypeOf(components[0].getClass(), components[1].getClass(), components[2].getClass(), components[3].getClass());
				bundle = ComponentBundle.bundleOf(components[0], components[1], components[2], components[3]);
				break;
			case 5:
				archetype = Archetype.archetypeOf(components[0].getClass(), components[1].getClass(), components[2].getClass(), components[3].getClass(), components[4].getClass());
				bundle = ComponentBundle.bundleOf(components[0], components[1], components[2], components[3], components[4]);
				break;
			default:
				// Exception?
				return;
		}*/

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


		// systematically build these using recursion/loop @ last index??

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

	private Archetype<?,?,?,?,?> nestArchetypes(Object ... archetypeComponentClasses)
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
			t5 = nestArchetypes(ECSUtil.copyOfRangeAndFill(archetypeComponentClasses,5,archetypeComponentClasses.length,ECSUtil.NONE));
		}
		return Archetype.archetypeOf(archetypeComponentClasses[0],
				archetypeComponentClasses[1], archetypeComponentClasses[2],
				archetypeComponentClasses[3], t5);
	}

	public void changeEntityArchetype(int entityID, EntityArchetypeChangeRequest entityArchetypeChangeRequest)
	{
		printf("Starting EACR of +[%d], -[%d] to %s", entityArchetypeChangeRequest.componentsToAdd.size(), entityArchetypeChangeRequest.componentsToRemove.size(), entityArchetypeChangeRequest.getOldArchetype());
		Archetype<?, ?, ?, ?, ?> oldArchetype = entityArchetypeChangeRequest.getOldArchetype();

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

		Set<Class<?>> componentsToRemove = entityArchetypeChangeRequest.componentsToRemove;
		ArrayList<?> componentsToAdd = new ArrayList<>(entityArchetypeChangeRequest.componentsToAdd);

		// this can be made more efficient w/ an array(list) of Ts w/ start index...
		// ...based on size()
		/*switch (oldBundle.size())
		{
			case 9:
			case 8:
			case 7:
			case 6:
			case 5:
				finalComponents.add(getNextBundleType(oldBundle.getT5(),
						componentsToRemove, componentsToAdd, oldArchetype));
			case 4:
				finalComponents.add(getNextBundleType(oldBundle.getT4(),
						componentsToRemove, componentsToAdd, oldArchetype));
			case 3:
				finalComponents.add(getNextBundleType(oldBundle.getT3(),
						componentsToRemove, componentsToAdd, oldArchetype));
			case 2:
				finalComponents.add(getNextBundleType(oldBundle.getT2(),
						componentsToRemove, componentsToAdd, oldArchetype));
			case 1:
				finalComponents.add(getNextBundleType(oldBundle.getT1(),
						componentsToRemove, componentsToAdd, oldArchetype));
				break;
			default:
				// exception?
				return;
		}*/
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

		// print("Processed EACR -> ",ComponentBundle.bundleOf(finalComponents.toArray()[0], finalComponents.toArray()[1]));
		prints("Processed EACR -> ");
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
			print("EACR: comparing ", componentToRemove, "to", t.getClass());
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

	public void removeEntity(EntityDeletionRequest entityDeletionRequest)
	{
		int entityID = entityDeletionRequest.getEntityID();
		Archetype<?, ?, ?, ?, ?> archetype = entityDeletionRequest.getArchetype();
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
				addEntityIntoList(entities, archetype, entityEntry);
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
				addEntityIntoList(entities, archetype, entityEntry);
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
				addEntityIntoList(entities, archetype, entityEntry);
			}
		}
		return entities;
	}

	public void addSystem(System system)
	{
		this.systems.add(system);
	}

	private void addEntityIntoList(List<Entity> entities, Archetype<?, ?, ?, ?, ?> archetype, Map.Entry<Integer, ComponentBundle<?, ?, ?, ?, ?>> entityEntry)
	{
		entities.add(entityFromEntry(archetype, entityEntry));
	}

	private Entity entityFromEntry(Archetype<?, ?, ?, ?, ?> archetype, Map.Entry<Integer, ComponentBundle<?, ?, ?, ?, ?>> entityEntry)
	{
		return new Entity(archetype, entityEntry.getKey(), entityEntry.getValue());
	}

	public int size() // taxing; careful
	{
		// find a more efficient way? - ie. changing an int from add/removeEntity()
		return entityIDs.size();
	}

	//

	public void update()
	{
		for (System system : systems)
		{
			system.tick(this); // temporary: will in the future send EM and CM

			// process requests
			for (ComponentBundle<?, ?, ?, ?, ?> bundle : system.entitiesToCreate)
			{
				addEntity(bundle); // if one of the ? is Void, that's fine
			}
			system.entitiesToCreate.clear();

			for (EntityDeletionRequest entityDeletionRequest : system.entitiesToRemove)
			{
				removeEntity(entityDeletionRequest);
				printf("Processed EDR of [%d]@%s; new size -> %d", entityDeletionRequest.getEntityID(), entityDeletionRequest.getArchetype(), size());
			}
			system.entitiesToRemove.clear();

			for (Map.Entry<Integer, EntityArchetypeChangeRequest> entityArchetypeChangeRequestEntry : system.entitiesToChange.entrySet())
			{
				int entityID = entityArchetypeChangeRequestEntry.getKey();
				EntityArchetypeChangeRequest entityArchetypeChangeRequest = entityArchetypeChangeRequestEntry.getValue();

				changeEntityArchetype(entityID, entityArchetypeChangeRequest);
			}
			system.entitiesToChange.clear();
		}
	}

	/*public void render(Graphics graphics)
	{

	}*/

	//
	/*public Archetype<?, ?, ?, ?, ?> bundleToArchetype(ComponentBundle<?, ?, ?, ?, ?> bundle)
	{
		Object t5 = bundle.getT5();

		return Archetype.archetypeOf(bundle.getT1(),
				bundle.getT2(), bundle.getT3(), bundle.getT4(), t5);
	}*/

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
