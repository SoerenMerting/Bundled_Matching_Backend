package test;

public class Methods {

	/**
	 * Compares two matrices. Returns an the indices (i, j) of a field that does not match. -1 if all fields are equal
	 * @param matrix1
	 * @param matrix2
	 * @return
	 */
	public static int[] compareMatrix(double[][] matrix1, double[][] matrix2)
	{
		if(matrix1.length != matrix2.length)
			throw new IllegalArgumentException("Sizes of first dimension do not match, first: "
												+ matrix1.length + "; second: " + matrix2.length);
		if(matrix1[0].length != matrix2[0].length)
			throw new IllegalArgumentException("Sizes of second dimension do not match");
		
		for(int i = 0; i < matrix1.length; i++)
		{
			for(int j = 0; j < matrix1[0].length; j++)
			{
				if(matrix1[i][j] != matrix2[i][j])
					return new int[]{i, j};
			}
		}
		
		return new int[]{-1, -1};
	}
	
	/**
	 * Compares to vectors. Returns the index of a filed that does not match. -1 if the vectors are the same
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public static int compareVector(double[] vector1, double[] vector2)
	{
		if(vector1.length != vector2.length)
			throw new IllegalArgumentException("Sizes of vectors do not match, first: " + vector1.length + "; second: " + vector2.length);
		
		for(int i = 0; i < vector1.length; i++)
		{
			if(vector1[i] != vector2[i])
				return i;
		}
		
		return -1;
	}
	
	/**
	 * Compares to vectors. Returns the index of a filed that does not match. -1 if the vectors are the same.
	 * This function allows for a deviation
	 * @param vector1
	 * @param vector2
	 * @return
	 */
	public static int compareVector(double[] vector1, double[] vector2, double error)
	{
		if(vector1.length != vector2.length)
			throw new IllegalArgumentException("Sizes of vectors do not match");
		
		for(int i = 0; i < vector1.length; i++)
		{
			if(vector1[i] + error < vector2[i] || vector1[i] - error > vector2[i])
				return i;
		}
		
		return -1;
	}
}
