package printable;

public abstract class Printable
{
	public static void print(Object ... args)
	{
		for (Object arg : args)
		{
			System.out.print(arg);
		}
		System.out.println();
	}

	public static void printf(String unf, Object ... formatters)
	{
		System.out.println(String.format(unf,formatters));
	}
}