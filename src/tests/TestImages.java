package tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import utility.Experiment;
import utility.Images;

public class TestImages
{
	@Test
	public void testExtractByteDataGreyscale()
	{
		// Predetermined correct byte values
		float[][] refValues = { { 50, 100, 150 }, { 200, 225, 25 }, { 75, 125, 175 } };

		Images s = new Images(Experiment.TEST_PATH + "testImage.png", Experiment.TEST_PATH + "testImage.png");
		s.init();

		float[][] imgBytes = s.fb1;

		assertEquals(imgBytes.length * imgBytes[0].length, refValues.length * refValues[0].length);

		for (int i = 0; i < imgBytes.length; i++)
		{
			for (int j = 0; j < imgBytes[0].length; j++)
			{
				assertEquals(refValues[i][j], imgBytes[i][j], 0.0001);
			}
		}
	}

	@Test
	public void testNormalizeGreyscale()
	{
		// Predetermined correct normalized byte values
		float[][] refValues = { { 31.875f, 95.625f, 159.375f }, { 223.125f, 255.000f, 0.000f }, { 63.750f, 127.500f, 191.250f } };

		Images s = new Images(Experiment.TEST_PATH + "testNorm1.png", Experiment.TEST_PATH + "testNorm2.png");
		s.init();
		float[][] img1Bytes = s.fb1;
		float[][] img2Bytes = s.fb2;
		float[][] nmBytes1 = new float[img1Bytes.length][img1Bytes[0].length];
		float[][] nmBytes2 = new float[img2Bytes.length][img2Bytes[0].length];
		Images.normalizeGreyscale(img1Bytes, img2Bytes, nmBytes1, nmBytes2);

		assertEquals(nmBytes1.length * nmBytes1[0].length, refValues.length * refValues[0].length);
		assertEquals(nmBytes2.length * nmBytes2[0].length, refValues.length * refValues[0].length);

		for (int i = 0; i < img1Bytes.length; i++)
		{
			for (int j = 0; j < img1Bytes[0].length; j++)
			{
				System.out.print(nmBytes2[i][j]);
				//assertEquals(refValues[i][j], nmBytes1[i][j], 0.0001);
			}
			System.out.println();
		}
	}

	@Test
	public void testApplyGaussianSmoothing()
	{
		// Predetermined correct smoothed byte values
		float[][] imgValues = { { 1, 150 }, { 200, 20 } };
		float[][] refValues = { { 160.316f, 84.390f }, { 109.324f, 169.791f } };

		float[][] gsBytes = Images.applyGaussianSmoothing(imgValues, 2, 0.8);
		
		assertEquals(imgValues.length * imgValues[0].length, refValues.length * refValues[0].length);

		for (int i = 0; i < gsBytes.length; i++)
		{
			for (int j = 0; j < gsBytes[0].length; j++)
			{
				assertEquals(refValues[i][j], gsBytes[i][j], 0.001f);
			}
		}
	}

	@Test
	public void testCreate1DGaussianKernel()
	{
		// Predetermined values for sigma of 0.8 and window size 5
		double[] refMatrix = { 0.498677, 0.228311, 0.021910, 0.000440, 0.000001 };

		double[] kernel = Images.create1DGaussianKernel(5, 0.8);

		for (int i = 0; i < kernel.length; i++)
		{
			assertEquals(refMatrix[i], kernel[i], 0.000001);
		}
	}

	/**
	 * When given indexes out of bounds, gpReflect should return the value at
	 * the reflecting index
	 */
	@Test
	public void testgpReflect()
	{
		double[][] testValues = { { 50, 100 }, { 200, 225 } };

		// Predetermined values for reflecting boundary conditions on the test
		// matrix loop
		float[] refValues = { 225, 200, 225, 200, 100, 50, 100, 50, 225, 200, 225, 200, 100, 50, 100, 50 };

		int c = 0;

		for (int i = -1; i <= testValues.length; i++)
		{
			for (int j = -1; j <= testValues[0].length; j++)
			{
				assertEquals(Images.gpReflect(testValues, j, i), refValues[c], 0);
				c++;
			}
		}
	}
}
