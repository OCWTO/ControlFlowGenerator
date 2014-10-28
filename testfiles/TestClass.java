
public class TestClass
{
	int a, b, c, d;
	String ab, cd;
	
	public TestClass()
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
		}
	}
	
	public static void main(String[] args)
	{
		TestClass test = new TestClass();
		
		test.print1();
		test.print2();
		test.print3();
		test.print4();
	}

	private void print4()
	{
		System.out.println("nothing");
	}

	private void print3()
	{
		System.out.println("nothing");	
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
