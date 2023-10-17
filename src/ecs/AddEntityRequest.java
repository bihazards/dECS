package ecs;

public class AddEntityRequest
{
	public final Object [] components ;

	public AddEntityRequest(Object ... components)
	{
		this.components = components;
	}
}
