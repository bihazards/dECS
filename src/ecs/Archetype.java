package ecs;

import java.util.Objects;

public class Archetype<T1, T2, T3, T4, T5> // optional: Tx extends Component
{

	private T1 t1;
	private T2 t2;
	private T3 t3;
	private T4 t4;
	private T5 t5;
	private final boolean hasArchetype;
	private final int size;

	private Archetype(int size, T1 t1, T2 t2, T3 t3, T4 t4, T5 t5)
	{
		this.t1 = t1;
		this.t2 = t2;
		this.t3 = t3;
		this.t4 = t4;
		this.t5 = t5;

		hasArchetype = t5.getClass() == Archetype.class;
		this.size = size;
	}

	// private factory
	private static <T1, T2, T3, T4, T5> Archetype<T1, T2, T3, T4, T5> generate(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5)
	{ // ~=archetypeOfHelper
		if (t1.getClass() != Class.class ||
				t2.getClass() != Class.class ||
				t3.getClass() != Class.class ||
				t4.getClass() != Class.class ||
				(t5.getClass() != Class.class && t5.getClass() != Archetype.class))
		{
			// throw Exception
			return null; // can't generate an incorrect one
		}

		// calculate size
		int size = 0;
		if (t1 != ECSUtil.NONE) size ++;
		if (t2 != ECSUtil.NONE) size ++;
		if (t3 != ECSUtil.NONE) size ++;
		if (t4 != ECSUtil.NONE) size ++;
		if (t5 != ECSUtil.NONE)
		{
			if (t5.getClass() == Archetype.class) size++;
			else size += ((Archetype<?, ?, ?, ?, ?>) t5).size();
		}
		return new Archetype<>(size, t1, t2, t3, t4, t5);
	}

	// public factory
	/// 5->1
	public static <T1, T2, T3, T4, T5> Archetype<T1, T2, T3, T4, T5> archetypeOf(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5)
	{
		return generate(t1, t2, t3, t4, t5);
	}

	public static <T1, T2, T3, T4> Archetype<T1, T2, T3, T4, Class<?>> archetypeOf(T1 t1, T2 t2, T3 t3, T4 t4)
	{
		return generate(t1, t2, t3, t4, ECSUtil.NONE);
	}

	public static <T1, T2, T3> Archetype<T1, T2, T3, Class<?>, Class<?>> archetypeOf(T1 t1, T2 t2, T3 t3)
	{
		return generate(t1, t2, t3, ECSUtil.NONE, ECSUtil.NONE);
	}

	public static <T1, T2> Archetype<T1, T2, Class<?>, Class<?>, Class<?>> archetypeOf(T1 t1, T2 t2)
	{
		return generate(t1, t2, ECSUtil.NONE, ECSUtil.NONE, ECSUtil.NONE);
	}

	public static <T1> Archetype<T1, Class<?>, Class<?>, Class<?>, Class<?>> archetypeOf(T1 t1)
	{
		return generate(t1, ECSUtil.NONE, ECSUtil.NONE, ECSUtil.NONE, ECSUtil.NONE);
	}
	
	/// extensions (9->6)
	public static <T1,T2,T3,T4,T5,T6,T7,T8,T9> Archetype<T1,T2,T3,T4,Archetype<T5,T6,T7,T8,T9>> archetypeOf(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9)
	{
		return generate(t1,t2,t3,t4,generate(t5,t6,t7,t8,t9));
	}

	public static <T1,T2,T3,T4,T5,T6,T7,T8> Archetype<T1,T2,T3,T4,Archetype<T5,T6,T7,T8,Class<?>>> archetypeOf(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8)
	{
		return generate(t1,t2,t3,t4,generate(t5,t6,t7,t8, ECSUtil.NONE));
	}

	public static <T1,T2,T3,T4,T5,T6,T7> Archetype<T1,T2,T3,T4,Archetype<T5,T6,T7,Class<?>,Class<?>>> archetypeOf(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7)
	{
		return generate(t1,t2,t3,t4,generate(t5,t6,t7, ECSUtil.NONE, ECSUtil.NONE));
	}

	public static <T1,T2,T3,T4,T5,T6> Archetype<T1,T2,T3,T4,Archetype<T5,T6,Class<?>,Class<?>,Class<?>>> archetypeOf(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6)
	{
		return generate(t1,t2,t3,t4,generate(t5,t6, ECSUtil.NONE, ECSUtil.NONE, ECSUtil.NONE));
	}

	/*
	// To add more Archetypes:
	public static <T1,...,Tx> Archetype<T1,...,Archetype<T5,T6,[Class<?> or T7-Tx]>> archetypeOf()
	{
		return generate(x,t1,t2,t3,t4,generate(x-5,t5,t6,...));
	}
	 */

	//
	public boolean has(Class<?> componentClass)
	{
		return t1 == componentClass ||
				t2 == componentClass ||
				t3 == componentClass ||
				t4 == componentClass ||
				t5 == componentClass ||
				(hasArchetype &&
						((Archetype<?, ?, ?, ?, ?>) t5).has(componentClass));

	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		return hashCode() == o.hashCode(); // allows equality with different param order
	}

	@Override
	public int hashCode()
	{
		// using hashCodes in an unordered manner allows Archetypes
		// with different component class orders to be treated as equal
		return hashCode2(t1) *
				hashCode2(t2) *
				hashCode2(t3) *
				hashCode2(t4) *
				hashCode2(t5); // supports nested Archetypes
	}

	/* public <T> int hashCode2()
	Helper for hashCode():
	- returns 1 iff NO_CLASS
	- returns hashCode() of Archetype iff Archetype
	- Otherwise: returns Objects.hashCode()

	This allows an Archetype of 5 NO_CLASS with no nested Archetype to equals()
	an Archetype of infinite NO_CLASS using a nested Archetype.
	 */
	private <T> int hashCode2(T t)
	{
		// is NO_CLASS => don't affect total
		if (t == ECSUtil.NONE)
		{
			return 1;
		}

		// is Archetype => use hashCodes of T1..T5
		if (t.getClass() == getClass())
		{
			return t.hashCode();
		}

		// etc.
		return Objects.hashCode(t);
	}

	@Override
	public String toString()
	{
		/*Printable.print(t1,"=",t1.toString().split(" ")[1]);
		Printable.print(t2,"=",t2.toString().split(" ")[1]);
		Printable.print(t3,"=",t3.toString().split(" ")[1]);
		Printable.print(t4,"=",t4.toString().split(" ")[1]);
		Printable.print(t5,"=",t5.toString().split(" ")[1]);*/
		return new StringBuilder(t1.toString().replace("class ","")).append(',')
				.append(t2.toString().replace("class ","")).append(',')
				.append(t3.toString().replace("class ","")).append(',')
				.append(t4.toString().replace("class ","")).append(',')
				.append(t5.toString().replace("class ",""))
				.toString(); // specifically chosen over StringBuffer; don't need synchronization
	}

	// getters
	public T1 getT1()
	{
		return t1;
	}

	public T2 getT2()
	{
		return t2;
	}

	public T3 getT3()
	{
		return t3;
	}

	public T4 getT4()
	{
		return t4;
	}

	public T5 getT5()
	{
		return t5;
	}

	public int size()
	{
		return size;
	}
}
