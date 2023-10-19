package proj.systems;

import ecs.ECS;
import printable.Printable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class System extends Printable
{
	// This is project-specific and is not directly tied to the ECS.
	// How the user implements Systems is up to them.
	// Package "System" with the ECS as a basic version of what the user could use?

	public abstract void tick(ECS ecs);
}
