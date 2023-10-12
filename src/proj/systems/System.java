package proj.systems;

import ecs.ECS;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface System
{
	// This is project-specific and is not directly tied to the ECS.
	// How the user implements Systems is up to them.
	// Package "System" with the ECS as a basic version of what the user could use?

	public void tick(ECS ecs);
}
