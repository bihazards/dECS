package ecs;

import java.util.Objects;


public class ComponentBundle<T1, T2, T3, T4, T5>
{

	private final T1 t1;
	private final T2 t2;
	private final T3 t3;
	private final T4 t4;
	private final T5 t5;
	private final int size;

	private ComponentBundle(int size, T1 t1, T2 t2, T3 t3, T4 t4, T5 t5)
	{
		this.t1 = t1;
		this.t2 = t2;
		this.t3 = t3;
		this.t4 = t4;
		this.t5 = t5;

		// calculate size
		this.size = size;
	}

	public <T> T get(Class<T> componentClass)
	{
		if (componentClass == ComponentBundle.class)
		{
			return null;
		}

		if (t1.getClass() == componentClass)
		{
			return (T) t1;
		} else if (t2.getClass() == componentClass)
		{
			return (T) t2;
		} else if (t3.getClass() == componentClass)
		{
			return (T) t3;
		} else if (t4.getClass() == componentClass)
		{
			return (T) t4;
		} else if (t5.getClass() == componentClass)
		{
			return (T) t5;
		} else if (t5.getClass() == ComponentBundle.class)
		{
			return ((ComponentBundle<?,?,?,?,?>) t5).get(componentClass);
		}
		return null;
	}

	// helper
	private static <T1, T2, T3, T4, T5> ComponentBundle<T1, T2, T3, T4, T5> generate(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5)
	{
		// validate int size?
		int size = 0;
		if (t1 != ECSUtil.NONE) size ++;
		if (t2 != ECSUtil.NONE) size ++;
		if (t3 != ECSUtil.NONE) size ++;
		if (t4 != ECSUtil.NONE) size ++;
		if (t5 != ECSUtil.NONE)
		{
			if (t5.getClass() == ComponentBundle.class) size++;
			else size += ((ComponentBundle<?, ?, ?, ?, ?>) t5).size();
		}
		return new ComponentBundle<>(size, t1, t2, t3, t4, t5);
	}

	// generator
	/// (5->1)
	public static <T1, T2, T3, T4, T5> ComponentBundle<T1, T2, T3, T4, T5> bundleOf(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5)
	{
		return generate(t1, t2, t3, t4, t5);
	}

	/*public static <T1, T2, T3, T4> ComponentBundle<T1, T2, T3, T4, Class<?>> bundleOf(T1 t1, T2 t2, T3 t3, T4 t4)
	{
		return generate(t1, t2, t3, t4, NO_CLASS);
	}

	public static <T1, T2, T3> ComponentBundle<T1, T2, T3, Class<?>, Class<?>> bundleOf(T1 t1, T2 t2, T3 t3)
	{
		return generate(t1, t2, t3, NO_CLASS, NO_CLASS);
	}

	public static <T1, T2> ComponentBundle<T1, T2, Class<?>, Class<?>, Class<?>> bundleOf(T1 t1, T2 t2)
	{
		return generate(t1, t2, NO_CLASS, NO_CLASS, NO_CLASS);
	}

	public static <T1> ComponentBundle<T1, Class<?>, Class<?>, Class<?>, Class<?>> bundleOf(T1 t1)
	{
		return generate(t1, NO_CLASS, NO_CLASS, NO_CLASS, NO_CLASS);
	}*/

	/// extensions (9->6)
	public static <T1,T2,T3,T4,T5,T6,T7,T8,T9> ComponentBundle<T1,T2,T3,T4,ComponentBundle<T5,T6,T7,T8,T9>> bundleOf(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8, T9 t9)
	{
		return generate(t1,t2,t3,t4,generate(t5,t6,t7,t8,t9));
	}

	/*public static <T1,T2,T3,T4,T5,T6,T7,T8> ComponentBundle<T1,T2,T3,T4,ComponentBundle<T5,T6,T7,T8,Class<?>>> bundleOf(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7, T8 t8)
	{
		return generate(t1,t2,t3,t4,generate(t5,t6,t7,t8,NO_CLASS));
	}

	public static <T1,T2,T3,T4,T5,T6,T7> ComponentBundle<T1,T2,T3,T4,ComponentBundle<T5,T6,T7,Class<?>,Class<?>>> bundleOf(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6, T7 t7)
	{
		return generate(t1,t2,t3,t4,generate(t5,t6,t7,NO_CLASS,NO_CLASS));
	}

	public static <T1,T2,T3,T4,T5,T6> ComponentBundle<T1,T2,T3,T4,ComponentBundle<T5,T6,Class<?>,Class<?>,Class<?>>> bundleOf(T1 t1, T2 t2, T3 t3, T4 t4, T5 t5, T6 t6)
	{
		return generate(t1,t2,t3,t4,generate(t5,t6,NO_CLASS,NO_CLASS,NO_CLASS));
	}*/

	// getter stuff
	public boolean has(Class<?> componentClass)
	{
		return t1.getClass() == componentClass ||
				t2.getClass() == componentClass ||
				t3.getClass() == componentClass ||
				t4.getClass() == componentClass ||
				t5.getClass() == componentClass ||
				(t5.getClass() == ComponentBundle.class
						&& ((ComponentBundle<?,?,?,?,?>) t5).has(componentClass));

	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		ComponentBundle<?, ?, ?, ?, ?> cb = (ComponentBundle<?, ?, ?, ?, ?>) o;
		return t1.equals(cb.t1) &&
				t2.equals(cb.t2) &&
				t3.equals(cb.t3) &&
				t4.equals(cb.t4) &&
				t5.equals(cb.t5); // allows equality with different param order
	}

	@Override
	public int hashCode()
	{
		// using hashCodes in an unordered manner allows ComponentBundles
		// with different component class orders to be treated as equal
		return Objects.hash(t1, t2, t3, t4, t5);
	}

	@Override
	public String toString()
	{
		return new StringBuilder(t1.toString()).append(", ")
				.append(t2.toString()).append(", ")
				.append(t3.toString()).append(", ")
				.append(t4.toString()).append(", ")
				.append(t5.toString())
				.toString(); // specifically chosen over StringBuffer; don't need synchronization
	}

	// added for ECS conversion of CB -> Archetype
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
