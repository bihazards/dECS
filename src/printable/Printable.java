package printable;

public abstract class Printable
{
	public static void prints(Object ... args) // print same line
	{
		for (Object arg : args)
		{
			System.out.print(arg);
		}
	}

	public static void print(Object ... args) // print + nl
	{
		prints(args);
		System.out.println();
	}

	public static void printf(String unf, Object ... formatters)
	{
		System.out.println(String.format(unf,formatters));
	}
}