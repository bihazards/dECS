package ecs;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Entity
{
	public int entityID ;
	public ComponentBundle<?,?,?,?,?> components;

	public Entity(int entityID, ComponentBundle<?,?,?,?,?> components)
	{
		this.entityID = entityID;
		this.components = components;
	}

}
