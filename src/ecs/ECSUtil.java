package ecs;

public class ECSUtil
{
	protected static final Class<?> NONE = Void.class; // <?> ?

	// move to a custom Array Util class
	protected static <T> T [] copyOfRangeAndFill(T [] src, int from, int to, T noValue)
	{
		if (from >= src.length || from > to || src == null)
		{
			// exception
			return null;
		}
		Object [] finalArray = new Object[to-from];

		for (int i = from; i < to; i++)
		{
			if (i >= src.length)
			{
				finalArray[i] = noValue;
			} else
			{
				finalArray[i] = src[i];
			}
		}
		return (T []) finalArray ;
	}
}
