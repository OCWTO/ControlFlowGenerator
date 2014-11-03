public class BasicTest4
{
	public static void main(String[] args)				//ENTRY BLOCK
	{
		int n = 10;										//Block 1 (in example it reads in but we simplified it)
		int i = 1; 										//Block 2
		int sum = 0;									//Block 3
		while (i<= n)									//Block 4		T-> Block 5, F->Block 10
		{
			if (i > n)
			{
				throw new Exception();
			}
			else if(i< n)
			{
				return null;
			}
			else
			{
				while(i==n)
				{
					System.out.println("stuff");
				}
			}
		}
		System.out.println("done");
}