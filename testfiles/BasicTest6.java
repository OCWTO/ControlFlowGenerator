public class BasicTest6
{
	public static void main(String[] args)				//ENTRY BLOCK
	{
		int n = 10;										//Block 1 (in example it reads in but we simplified it)
		int i = 1; 										//Block 2
		int sum = 0;									//Block 3
		
		//throw Exception();
		//return null;									//Block 4
		
		
		while(n>i)
		{
			System.out.println();
			System.out.println();
			System.out.println();
			System.out.println();
		
			if(i>n)
			{
				System.out.println();
				System.out.println();
			}
			else
			{
				System.out.println();
			}
			System.out.println();
		}
		System.out.println();
		
		do
		{
			System.out.println("L");
		}
		while(n != i);
		
		
		switch(n)
		{
			case 1:
				System.out.println("1");
				break;
			case 2:
				System.out.println("2");
				break;
			case 3:
				System.out.println("3");
				break;
			default:
				System.out.println("IDK");
				break;
		}
		
		for(int a = 0; a < sum; a++)
		{
			System.out.println(a);
			System.out.println(a);
			System.out.println(a);
			System.out.println(a);
			System.out.println(a);
		}
		
		while (i<= n)									//Block 4		T-> Block 5, F->Block 10
		{
			//return null;
		
			if(sum == 0)
			{
				System.out.println("mo");
			}
			else if (sum > 0 )
			{
				System.out.println("another");
			}
			else
			{
				System.out.println(":(");
			}
			while (i<= n)								//Block 5		T->Block 6, F-> Block 4
			{
				return null;							//Block 6
			
				while (i<= n)							//Block 6		T->Block 7, F-> Block 5
				{
					while (i<= n)						//Block 7		T->Block 8, F-> Block 6
					{
						System.out.println("bob");		//Block 8
						return null;					//Block 9
					}
				}
			}
		}
		System.out.println("done");						//Block 10
}