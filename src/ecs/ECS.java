package ecs;

import ecs.requests.AddEntityRequest;
import ecs.requests.ChangeArchetypeEntityRequest;
import ecs.requests.DeleteEntityRequest;
import printable.Printable;

import java.util.*;
import java.util.List;

public class ECS extends Printable
{
	private EntityManager entityManager;

	// Requests
	private final List<AddEntityRequest> addEntityRequests = new ArrayList<>();
	private final List<DeleteEntityRequest> deleteEntityRequests = new ArrayList<>();
	private final HashMap<Integer, ChangeArchetypeEntityRequest> changeArchetypeEntityRequests = new HashMap<>();

	// CONSTRUCTOR(s)
	public ECS()
	{
		this(1000); // default
	}

	public ECS(int maxEntities)
	{
		entityManager = new EntityManager(maxEntities);
	}

	// METHODS
	/*private Archetype<?, ?, ?, ?, ?> nestArchetypes(Object... archetypeComponentClasses)
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
	}*/ // SCRAPPED?

	/// REQUESTS
	public final void requestAddEntity(Object ... components)
	{
		addEntityRequests.add(new AddEntityRequest(components));
	}

	public final void requestDeleteEntity(Entity entity)
	{
		deleteEntityRequests.add(new DeleteEntityRequest(entity.getArchetype(), entity.getEntityID()));
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

		ChangeArchetypeEntityRequest changeArchetypeEntityRequest = changeArchetypeEntityRequests.get(entityID); // iff exists already, use existing request
		if (changeArchetypeEntityRequest == null) // doesn't exist already
		{
			changeArchetypeEntityRequest = new ChangeArchetypeEntityRequest(entity.getArchetype());
			changeArchetypeEntityRequests.put(entityID, changeArchetypeEntityRequest);
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

	/// QUERY METHODS
	public List<Entity> entitiesWithArchetype(Archetype<?, ?, ?, ?, ?> archetype)
	{
		// Map<Integer,ArrayList<Component>> entityMap = new HashMap<>();
		// return this.proj.components.get(archetype);
		List<Entity> entities = new ArrayList<>();
		Map<Integer, ComponentBundle<?, ?, ?, ?, ?>> entityMap = entityManager.components.get(archetype);
		// print("searching for archetype ", archetype);
		if (entityMap != null)
		{
			// print("found archetype ", archetype);
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

	public List<Entity> entitiesWithArchetypes(Archetype<?, ?, ?, ?, ?>... archetypes)
	{
		// what will the use case be for such a specific parse as opposed to eWCs()?
		List<Entity> entities = new ArrayList<>();

		for (Archetype<?, ?, ?, ?, ?> archetype : archetypes)
		{
			Map<Integer, ComponentBundle<?, ?, ?, ?, ?>> entityMap = entityManager.components.get(archetype);
			// print("searching for archetype ", archetype);

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
		for (Map.Entry<Archetype<?, ?, ?, ?, ?>, Map<Integer, ComponentBundle<?, ?, ?, ?, ?>>> archetypeEntry : entityManager.components.entrySet())
		{
			// test has proj.components
			Archetype<?, ?, ?, ?, ?> archetype = archetypeEntry.getKey();

			for (Class<?> componentClass : componentClasses)
			{
				// print("searching for entities w/ ", componentClass, "in", archetype);
				if (!archetype.has(componentClass))
				{
					continue aLoop;
				}
			}
			/*prints("found entities w/ ");
			for (Class<?> componentClass : componentClasses)
			{
				prints(componentClass.toString());
				prints(",");
			}
			print("in ", archetype);*/

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

	/// PROCESS()
	/* process()
	* Called as a middleman to execute all Requests/Commands
	* */
	public void process()
	{
		for (AddEntityRequest addEntityRequest : addEntityRequests)
		{
			entityManager.addEntity(addEntityRequest.components); // if one of the ? is Void, that's fine
		}
		addEntityRequests.clear();

		for (DeleteEntityRequest deleteEntityRequest : deleteEntityRequests)
		{
			entityManager.removeEntity(deleteEntityRequest);
			// printf("Processed DER of [%d]@%s; new size -> %d", deleteEntityRequest.getEntityID(), deleteEntityRequest.getArchetype(), size());
		}
		deleteEntityRequests.clear();

		for (Map.Entry<Integer, ChangeArchetypeEntityRequest> entityArchetypeChangeRequestEntry : changeArchetypeEntityRequests.entrySet())
		{
			int entityID = entityArchetypeChangeRequestEntry.getKey();
			ChangeArchetypeEntityRequest changeArchetypeEntityRequest = entityArchetypeChangeRequestEntry.getValue();

			entityManager.changeEntityArchetype(entityID, changeArchetypeEntityRequest);
		}
		changeArchetypeEntityRequests.clear();
	}

	//
	public void printAllComponents()
	{
		for (Map.Entry<Archetype<?, ?, ?, ?, ?>, Map<Integer, ComponentBundle<?, ?, ?, ?, ?>>> archetypeEntry : entityManager.components.entrySet())
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
