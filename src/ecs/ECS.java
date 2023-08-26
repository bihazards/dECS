package ecs;

import printable.Printable;

import java.lang.ref.WeakReference;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ECS extends Printable
{
	/*
		Problem to resolve - how to remove an entity from a map => WRs
	 */

	// EntityManager tings
	private final int maxEntities; // max size of entityIDs (cusomizable in ctor)
	private static final int ENTITYID_INVALID = -1;
	private final Set<Integer> entityIDs; // note: use of Set vs List is ~2-3x slower
	// private Set<Integer> reservedEntityIDs = new HashSet<>();

	// ComponentManager tings
	private final Map<Archetype<?,?,?,?,?>, Map<WeakReference<Integer>,ComponentBundle<?,?,?,?,?>>> components = new ConcurrentHashMap<>();

	// SystemManager tings
	private final List<System> systems = new ArrayList<>();

	// ctor
	public ECS()
	{
		this(20); // default
	}

	public ECS(int maxEntities)
	{
		// instantiate collections here

		this.maxEntities = maxEntities;
		entityIDs = new HashSet<>(maxEntities); // define capacity at ENTITIES_MAX instead of default (16) [this value should be lower based on ENTITIES_MAX size]
	}

	// methods
	public void method(Object ... args)
	{

	}

	/*public void reserveEntityID()
	{

	}*/

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
				return entityID;
		}

		// test iff archetype exists OR add
		Map<WeakReference<Integer>,ComponentBundle<?,?,?,?,?>> archetypeComponents = this.components.get(archetype);
		if (archetypeComponents == null) // iff not exists...
		{
			archetypeComponents = new HashMap<>(); // create...
			this.components.put(archetype,archetypeComponents); // ...and put() in big map
		}

		// finally, add
		entityIDs.add(entityID);
		archetypeComponents.put(new WeakReference<Integer>(entityID),bundle); // iff there is an entity there, too bad
		return entityID;
	}

	public <T1,T2,T3,T4,T5> void removeEntity(ComponentBundle<T1,T2,T3,T4,T5> components, int entityID)
	{
		Archetype<T1,T2,T3,T4,T5> archetype = Archetype.archetypeOf(components.getT1(),
				components.getT2(), components.getT3(), components.getT4(), components.getT5());

		if (this.components.containsKey(archetype))
		{

		}
	}

	//
	public List<Entity> getEntitiesByArchetype(Archetype<?,?,?,?,?> archetype)
	{
		// Map<WeakReference<Integer>,ArrayList<Component>> entityMap = new HashMap<>();
		// return this.proj.components.get(archetype);
		List<Entity> entities = new ArrayList<>();
		Map<WeakReference<Integer>,ComponentBundle<?,?,?,?,?>> entityMap = this.components.get(archetype);
		print("searching for archetype ",archetype);
		if (entityMap != null)
		{
			print("found archetype ",archetype);
			for (Map.Entry<WeakReference<Integer>,ComponentBundle<?,?,?,?,?>> entityEntry : entityMap.entrySet())
			{
				if (entityEntry.getKey().get() == null) // eID is invalid -> has been removed from EM
				{
					continue ;
					// removeEntity() ?
				}
				addEntityIntoList(entities, entityEntry);
			}
		}
		return entities;
	}

	public List<Entity> getEntitiesWith(Class<?> ... componentClasses)
	{
		List<Entity> entities = new ArrayList<>();

		aLoop: for (Map.Entry<Archetype<?,?,?,?,?>,Map<WeakReference<Integer>,ComponentBundle<?,?,?,?,?>>> archetypeEntry : this.components.entrySet())
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
			Map<WeakReference<Integer>,ComponentBundle<?,?,?,?,?>> archetypeComponents = archetypeEntry.getValue();
			for (Map.Entry<WeakReference<Integer>,ComponentBundle<?,?,?,?,?>> entityEntry : archetypeComponents.entrySet())
			{
				addEntityIntoList(entities, entityEntry);
			}
		}
		return entities;
	}

	public void addEntityIntoList(List<Entity> entities, Map.Entry<WeakReference<Integer>,ComponentBundle<?,?,?,?,?>> entityEntry)
	{
		entities.add(entityFromEntry(entityEntry));
	}

	public Entity entityFromEntry(Map.Entry<WeakReference<Integer>,ComponentBundle<?,?,?,?,?>> entityEntry)
	{
		return new Entity(entityEntry.getKey().get(),entityEntry.getValue());
	}

	//

	public void tick()
	{
		// update reservedIDs [ignore for now]
		// tick systems *in order*
		for (System system : systems)
		{
			system.tick();
			// spawn queued entities
		}
	}

	/*public void render(Graphics graphics)
	{

	}*/
	//
	public void printAllComponents()
	{
		for (Map.Entry<Archetype<?,?,?,?,?>,Map<WeakReference<Integer>,ComponentBundle<?,?,?,?,?>>> archetypeEntry : this.components.entrySet())
		{
			Archetype<?,?,?,?,?> archetype = archetypeEntry.getKey();
			Map<WeakReference<Integer>,ComponentBundle<?,?,?,?,?>> archetypeComponents = archetypeEntry.getValue();

			print("Archetype: ",archetype);
			for (Map.Entry<WeakReference<Integer>,ComponentBundle<?,?,?,?,?>> archetypeComponentEntry : archetypeComponents.entrySet())
			{
				int entityID = archetypeComponentEntry.getKey().get();
				ComponentBundle<?,?,?,?,?> componentBundle = archetypeComponentEntry.getValue();

				print(entityID,"\t|\t",componentBundle.toString());
			}
			print("--------------------------");
		}
	}
	/* HOW TO HANDLE ENTITY REMOVAL? */
	/*
	- Don't tick() systems from here ?
	 */
	/* NEXT (all kind of related):
	* - removeEntity()
	* - Entity Commands: the ability for outside classes to declare a change to
	* 					the structure/archetype of an entityID
	* - reserving IDs (such as for death)*/
}
