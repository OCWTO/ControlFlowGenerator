
public class BasicTest
{
	public static void main(String[] args)				//ENTRY BLOCK
	{
		int n = 10;										//Block 1 (in example it reads in but we simplified it)
		int i = 1; 										//Block 2
		int sum = 0;									//Block 3
		while (i<= n)									//Block 4		T-> Block 5, F->Block 12
		{
			sum = 0;									//Block 5
			int j = 1;										//Block 6
			while (j <= i) 								//Block 7		T-> Block 8, F->Block 10
			{	
				sum = sum + j;							//Block 8
				j = j + 1;								//Block 9
			}
			System.out.println("sum " + sum +". i" + i);//Block 10
			i = i + 1;									//Block 11
		}
		System.out.println("sum " + sum +". i" + i);	//Block 12
	}													//EXIT BLOCK
}