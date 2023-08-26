package ecs;

import printable.Printable;

import java.util.Objects;

public class Archetype<T1,T2,T3,T4,T5> // optional: Tx extends Component
{
	private T1 t1 ;
	private T2 t2 ;
	private T3 t3 ;
	private T4 t4 ;
	private T5 t5 ;

	private Archetype(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5)
	{
		this.t1 = t1;
		this.t2 = t2;
		this.t3 = t3;
		this.t4 = t4;
		this.t5 = t5;
	}

	public static <T1, T2, T3, T4, T5> Archetype<T1,T2,T3,T4,T5> archetypeOf(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5)
	{
		return new Archetype<T1,T2,T3,T4,T5>(t1,t2,t3,t4,t5);
	}

	public static <T1, T2, T3, T4> Archetype<T1,T2,T3,T4,Class<Void>> archetypeOf(T1 t1, T2 t2, T3 t3, T4 t4)
	{
		return new Archetype<T1,T2,T3,T4,Class<Void>>(t1,t2,t3,t4,Void.class);
	}

	public static <T1, T2, T3> Archetype<T1,T2,T3,Class<Void>,Class<Void>> archetypeOf(T1 t1, T2 t2, T3 t3)
	{
		return new Archetype<T1,T2,T3,Class<Void>,Class<Void>>(t1,t2,t3,Void.class,Void.class);
	}

	public static <T1, T2> Archetype<T1,T2,Class<Void>,Class<Void>,Class<Void>> archetypeOf(T1 t1, T2 t2)
	{
		return new Archetype<T1,T2,Class<Void>,Class<Void>,Class<Void>>(t1,t2,Void.class,Void.class,Void.class);
	}

	public static <T1> Archetype<T1,Class<Void>,Class<Void>,Class<Void>,Class<Void>> archetypeOf(T1 t1)
	{
		return new Archetype<T1,Class<Void>,Class<Void>,Class<Void>,Class<Void>>(t1,Void.class,Void.class,Void.class,Void.class);
	}

	//
	public boolean has(Class<?> componentClass)
	{
		return t1 == componentClass ||
				t2 == componentClass ||
				t3 == componentClass ||
				t4 == componentClass ||
				t5 == componentClass ;

	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		return hashCode() == ((Archetype<?,?,?,?,?>) o).hashCode(); // allows equality with different param order
	}

	@Override
	public int hashCode()
	{
		// using hashCodes in an unordered manner allows Archetypes
		// with different component class orders to be treated as equal
		return Objects.hashCode(t1)*
				Objects.hashCode(t2)*
				Objects.hashCode(t3)*
				Objects.hashCode(t4)*
				Objects.hashCode(t5);
	}

	@Override
	public String toString()
	{
		/*Printable.print(t1,"=",t1.toString().split(" ")[1]);
		Printable.print(t2,"=",t2.toString().split(" ")[1]);
		Printable.print(t3,"=",t3.toString().split(" ")[1]);
		Printable.print(t4,"=",t4.toString().split(" ")[1]);
		Printable.print(t5,"=",t5.toString().split(" ")[1]);*/
		return new StringBuilder(t1.toString().split(" ")[1])
				.append(t2.toString().split(" ")[1])
				.append(t3.toString().split(" ")[1])
				.append(t4.toString().split(" ")[1])
				.append(t5.toString().split(" ")[1])
				.toString(); // specifically chosen over StringBuffer; don't need synchronization
	}

	/* Fixed NPE w/ NullComponent
	* Updated -> Used Class<Void>, Void.class instead */
}
