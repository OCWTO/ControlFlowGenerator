public class BasicTest5
{
	public static void main(String[] args)				//ENTRY BLOCK
	{
		int n = 10;										//Block 1 (in example it reads in but we simplified it)
		int i = 1; 										//Block 2
		int sum = 0;									//Block 3

		if(n == i)										//Block 4
		{
			System.out.println(n);						//Block 5
			System.out.println(i);						//Block 6
			
			while(n == 0)								//Block 7
			{
				System.out.println("more");				//Block 8
			}
		}
		System.out.println(sum);						//Block 9
}