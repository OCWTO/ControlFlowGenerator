
public class TestClass
{
	int a, b, c, d;
	String ab, cd;
	
	public TestClass(int a)
	{
		a=0;b=1;c=2;d=3;
		
		ab="Bob";
		cd="Craig";
		
		verify();
	}
	
	private void verify()
	{
		if(ab.equals(cd))
		{
			System.out.println("woops");
			System.out.println("2");
			System.out.println("3");

		}
		
		if(false)
			System.out.println("another test");
	}
	
	public static void main(String[] args)
	{
		TestClass test;
		test = new TestClass(9);
		
		test.print1();
		test.print2();
		test.print3("Bob");
		test.print4();
		
		if(false)
		{
			System.out.println("done");
		}
	}

	private void print4()
	{
		System.out.println("nothing");
	}

	private void print3(String name)
	{
		System.out.println(name);	
	}

	private void print2()
	{
		System.out.println("nothing");
	}

	private void print1()
	{
		System.out.println("nothing");
	}
}
