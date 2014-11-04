public class BasicTest3
{
	public static void main(String[] args)				//ENTRY BLOCK
	{
		int n = 10;										//Block 1 (in example it reads in but we simplified it)
		int i = 1; 										//Block 2
		int sum = 0;									//Block 3
		while (i<= n)									//Block 4		T-> Block 5, F->Block 10
		{
			while (i<= n)								//Block 5		T->Block 6, F-> Block 4
			{
				while (i<= n)							//Block 6		T->Block 7, F-> Block 5
				{
					System.out.println("n");			//Block 7
				}
					while (i<= n)						//Block 8		T->Block 8, F-> Block 6
					{
						System.out.println("bob");		//Block 9
						return null;					//Block 10
					}
				}
			}
			System.out.println("fail?");				//Block 11
			System.out.println("n");					//Block 12
		}
		System.out.println("done");						//Block 12
}