package proj.systems;

import ecs.ECS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface System
{

	public abstract void tick(ECS ecs);
}
