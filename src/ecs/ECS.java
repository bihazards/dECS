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
	private final Map<Archetype<?,?,?,?,?>, Map<Integer,ComponentBundle<?,?,?,?,?>>> components = new ConcurrentHashMap<>();

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
	public int addEntity(Object ... components)
	{
		// test iff new eID available
		// note: this ignores "reservedIDs" for now
		int entityID = ENTITYID_INVALID;

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

		putEntity(entityID, components);

		return entityID;
	}

	/* putEntity()
	* - This version is intended for use when an id is already reserevd.*/
	private void putEntity(int entityID, Object ... components)
	{
		// create Archetype
		// move this to its own class? ArchetypeBuilder? Archetype.archetypeOf(Component ...) ?
		Archetype<?,?,?,?,?> archetype ;
		ComponentBundle<?,?,?,?,?> bundle ;
		switch (components.length)
		{
			case 1:
				archetype = Archetype.archetypeOf(components[0].getClass());
				bundle = ComponentBundle.bundleOf(components[0]);
				break;
			case 2:
				archetype = Archetype.archetypeOf(components[0].getClass(),components[1].getClass());
				bundle = ComponentBundle.bundleOf(components[0],components[1]);
				break;
			case 3:
				archetype = Archetype.archetypeOf(components[0].getClass(),components[1].getClass(),components[2].getClass());
				bundle = ComponentBundle.bundleOf(components[0],components[1],components[2]);
				break;
			case 4:
				archetype = Archetype.archetypeOf(components[0].getClass(),components[1].getClass(),components[2].getClass(),components[3].getClass());
				bundle = ComponentBundle.bundleOf(components[0],components[1],components[2],components[3]);
				break;
			case 5:
				archetype = Archetype.archetypeOf(components[0].getClass(),components[1].getClass(),components[2].getClass(),components[3].getClass(),components[4].getClass());
				bundle = ComponentBundle.bundleOf(components[0],components[1],components[2],components[3],components[4]);
				break;
			default:
				// Exception?
				return ;
		}

		// test iff archetype exists OR add
		Map<Integer,ComponentBundle<?,?,?,?,?>> archetypeComponents = this.components.get(archetype);
		if (archetypeComponents == null) // iff not exists...
		{
			archetypeComponents = new HashMap<>(); // create...
			this.components.put(archetype,archetypeComponents); // ...and put() in big map
		}

		// finally, add
		entityIDs.add(entityID);
		archetypeComponents.put(entityID,bundle); // iff there is an entity there, too bad

	}

	public void changeEntityArchetype(int entityID, EntityArchetypeChangeRequest entityArchetypeChangeRequest)
	{
		printf("Starting EACR of +[%d], -[%d] to %s",entityArchetypeChangeRequest.componentsToAdd.size(), entityArchetypeChangeRequest.componentsToRemove.size(), entityArchetypeChangeRequest.getOldArchetype());
		Archetype<?,?,?,?,?> oldArchetype = entityArchetypeChangeRequest.getOldArchetype();

		Map<Integer,ComponentBundle<?,?,?,?,?>> archetypeComponentMap = this.components.get(oldArchetype);
		if (archetypeComponentMap == null)
		{
			// throw Exception - archetype doesn't exist => eID can't exist
			return ;
		}

		ComponentBundle<?,?,?,?,?> oldBundle = archetypeComponentMap.get(entityID);
		if (oldBundle == null)
		{
			// throw Exception - eID doesn't exist
			return ;
		}

		Set<Object> finalComponents = new HashSet<>();

		Set<Class<?>> componentsToRemove = entityArchetypeChangeRequest.componentsToRemove;
		ArrayList<?> componentsToAdd = new ArrayList<>(entityArchetypeChangeRequest.componentsToAdd);

		// this can be made more efficient w/ an array(list) of Ts w/ start index...
		// ...based on size()
		switch (oldBundle.size())
		{
			case 5:
				finalComponents.add(getNextBundleType(oldBundle.getT5(),
						componentsToRemove,componentsToAdd,oldArchetype));
			case 4:
				finalComponents.add(getNextBundleType(oldBundle.getT4(),
						componentsToRemove,componentsToAdd,oldArchetype));
			case 3:
				finalComponents.add(getNextBundleType(oldBundle.getT3(),
						componentsToRemove,componentsToAdd,oldArchetype));
			case 2:
				finalComponents.add(getNextBundleType(oldBundle.getT2(),
						componentsToRemove,componentsToAdd,oldArchetype));
			case 1:
				finalComponents.add(getNextBundleType(oldBundle.getT1(),
						componentsToRemove,componentsToAdd,oldArchetype));
				break;
			default:
				// exception?
				return;
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

	/* getNextArchetypeType()
	* Used for changeEntityArchetype as a helper */
	private Object getNextBundleType(Object t,
									 Set<Class<?>> componentsToRemove,
									 ArrayList<?> componentsToAdd,
									 Archetype<?,?,?,?,?> oldArchetype)
	{
		Object _t = t;
		for (Class<?> componentToRemove : componentsToRemove)
		{
			print("EACR: comparing ",componentToRemove,"to",t.getClass());
			if (!componentToRemove.equals(t.getClass())) // no match
			{
				continue ;
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
				return Void.class ;
			}
			return _t;
		}

		return t;
	}

	public void removeEntity(EntityDeletionRequest entityDeletionRequest)
	{
		int entityID = entityDeletionRequest.getEntityID();
		Archetype<?,?,?,?,?> archetype = entityDeletionRequest.getArchetype();
		//
		Map<Integer,ComponentBundle<?,?,?,?,?>> archetypeComponentMap = this.components.get(archetype);
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
	public List<Entity> entitiesWithArchetype(Archetype<?,?,?,?,?> archetype)
	{
		// Map<Integer,ArrayList<Component>> entityMap = new HashMap<>();
		// return this.proj.components.get(archetype);
		List<Entity> entities = new ArrayList<>();
		Map<Integer,ComponentBundle<?,?,?,?,?>> entityMap = this.components.get(archetype);
		print("searching for archetype ",archetype);
		if (entityMap != null)
		{
			print("found archetype ",archetype);
			for (Map.Entry<Integer,ComponentBundle<?,?,?,?,?>> entityEntry : entityMap.entrySet())
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
	public List<Entity> entitiesWithArchetypes(Archetype<?,?,?,?,?> ... archetypes)
	{
		List<Entity> entities = new ArrayList<>();

		for (Archetype<?,?,?,?,?> archetype : archetypes)
		{
			Map<Integer,ComponentBundle<?,?,?,?,?>> entityMap = this.components.get(archetype);
			print("searching for archetype ",archetype);

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

	public List<Entity> entitiesWithComponents(Class<?> ... componentClasses)
	{
		List<Entity> entities = new ArrayList<>();

		aLoop: for (Map.Entry<Archetype<?,?,?,?,?>,Map<Integer,ComponentBundle<?,?,?,?,?>>> archetypeEntry : this.components.entrySet())
		{
			// test has proj.components
			Archetype<?,?,?,?,?> archetype = archetypeEntry.getKey();

			for (Class<?> componentClass : componentClasses)
			{
				print("searching for entities w/ ",componentClass,"in",archetype);
				if (!archetype.has(componentClass))
				{
					continue aLoop;
				}
			}
			print("found entities w/ ",componentClasses.toString(),"in",archetype);

			// add
			Map<Integer,ComponentBundle<?,?,?,?,?>> archetypeComponents = archetypeEntry.getValue();
			for (Map.Entry<Integer,ComponentBundle<?,?,?,?,?>> entityEntry : archetypeComponents.entrySet())
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

	private void addEntityIntoList(List<Entity> entities, Archetype<?,?,?,?,?> archetype, Map.Entry<Integer,ComponentBundle<?,?,?,?,?>> entityEntry)
	{
		entities.add(entityFromEntry(archetype, entityEntry));
	}

	private Entity entityFromEntry(Archetype<?,?,?,?,?> archetype, Map.Entry<Integer,ComponentBundle<?,?,?,?,?>> entityEntry)
	{
		return new Entity(archetype, entityEntry.getKey(),entityEntry.getValue());
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
			for (ComponentBundle<?,?,?,?,?> bundle : system.entitiesToCreate)
			{
				addEntity(bundle); // if one of the ? is Void, that's fine
			}
			system.entitiesToCreate.clear();

			for (EntityDeletionRequest entityDeletionRequest : system.entitiesToRemove)
			{
				removeEntity(entityDeletionRequest);
				printf("Processed EDR of [%d]@%s; new size -> %d",entityDeletionRequest.getEntityID(),entityDeletionRequest.getArchetype(),size());
			}
			system.entitiesToRemove.clear();

			for (Map.Entry<Integer,EntityArchetypeChangeRequest> entityArchetypeChangeRequestEntry : system.entitiesToChange.entrySet())
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
	public Archetype<?,?,?,?,?> bundleToArchetype(ComponentBundle<?,?,?,?,?> bundle)
	{
		Object t5 = bundle.getT5();
		/*if (bundle.getT5().getClass() == Archetype.class)
		{
			t5 = bundleToArchetype((ComponentBundle<?,?,?,?,?>) bundle.getT5()); // will recurse through
		}*/
		return Archetype.archetypeOf(bundle.getT1(),
				bundle.getT2(), bundle.getT3(), bundle.getT4(), t5);
	}

	//
	public void printAllComponents()
	{
		for (Map.Entry<Archetype<?,?,?,?,?>,Map<Integer,ComponentBundle<?,?,?,?,?>>> archetypeEntry : this.components.entrySet())
		{
			Archetype<?,?,?,?,?> archetype = archetypeEntry.getKey();
			Map<Integer,ComponentBundle<?,?,?,?,?>> archetypeComponents = archetypeEntry.getValue();

			print("Archetype: ",archetype);
			for (Map.Entry<Integer,ComponentBundle<?,?,?,?,?>> archetypeComponentEntry : archetypeComponents.entrySet())
			{
				int entityID = archetypeComponentEntry.getKey();
				ComponentBundle<?,?,?,?,?> componentBundle = archetypeComponentEntry.getValue();

				print(entityID,"\t|\t",componentBundle.toString());
			}
			print("--------------------------");
		}
	}
	/*
	- Don't tick() systems from here ?
	 */
	/* NEXT (all kind of related):
	* - reserving IDs (such as for death)*/
}
