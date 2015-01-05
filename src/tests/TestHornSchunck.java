package tests;

import static org.junit.Assert.*;
import hs_classic.HornSchunck;

import org.junit.Test;

import utility.Experiment;
import utility.Images;

public class TestHornSchunck
{
	@Test
	public void testRunHSniter1()
	{
		float[][] refX = { { 0, 0 }, { 0.993886f, 0 } };
		float[][] refY = { { 0, 0 }, { 0, 0 } };

		Images testMv = new Images(Experiment.TEST_PATH + "test1.png", Experiment.TEST_PATH + "test2.png");
		testMv.init();

		float[][] u = new float[testMv.height][testMv.width];
		float[][] v = new float[testMv.height][testMv.width];

		HornSchunck hs = new HornSchunck(1, 10);
		hs.run(u, v, testMv.fb1, testMv.fb2);

		for (int i = 0; i < testMv.height; i++)
		{
			for (int j = 0; j < testMv.width; j++)
			{
				assertEquals(u[i][j], refX[i][j], 0.0001);
				assertEquals(v[i][j], refY[i][j], 0.0001);
			}
		}
	}

	@Test
	public void testRunHSniter2()
	{
		float[][] refX = { { 0.0015f, 0.0828f }, { 0.9974f, 0.2484f } };
		float[][] refY = { { 0, 0 }, { 0, 0 } };

		Images testMv = new Images(Experiment.TEST_PATH + "test1.png", Experiment.TEST_PATH + "test2.png");
		testMv.init();

		float[][] u = new float[testMv.height][testMv.width];
		float[][] v = new float[testMv.height][testMv.width];

		HornSchunck hs = new HornSchunck(2, 10);
		hs.run(u, v, testMv.fb1, testMv.fb2);

		for (int i = 0; i < testMv.height; i++)
		{
			for (int j = 0; j < testMv.width; j++)
			{
				assertEquals(u[i][j], refX[i][j], 0.1);
				assertEquals(v[i][j], refY[i][j], 0.1);
			}
		}
	}

	@Test
	public void testHSalphaZero()
	{
		float[][] refX = { { 0, 0 }, { 0.993886f, 0 } };
		float[][] refY = { { 0, 0 }, { 0, 0 } };

		Images testMv = new Images(Experiment.TEST_PATH + "test1.png", Experiment.TEST_PATH + "test2.png");
		testMv.init();

		float[][] u = new float[testMv.height][testMv.width];
		float[][] v = new float[testMv.height][testMv.width];

		HornSchunck hs = new HornSchunck(1, 0);
		hs.run(u, v, testMv.fb1, testMv.fb2);

		for (int i = 0; i < testMv.height; i++)
		{
			for (int j = 0; j < testMv.width; j++)
			{
				assertEquals(u[i][j], refX[i][j], 0.1);
				assertEquals(v[i][j], refY[i][j], 0.1);
			}
		}
	}

	@Test
	public void testComputeImageDerivativesSmall()
	{
		float[][] refX = { { 127.5f, 0 }, { 127.5f, 0 } };
		float[][] refY = { { 0, 0 }, { 0, 0 } };
		float[][] refT = { { 0, 0 }, { -127.5f, 0 } };

		Images testMv = new Images(Experiment.TEST_PATH + "test1.png", Experiment.TEST_PATH + "test2.png");
		testMv.init();

		int h = testMv.height;
		int w = testMv.width;

		float[][] Ix = new float[h][w];
		float[][] Iy = new float[h][w];
		float[][] It = new float[h][w];

		HornSchunck hs = new HornSchunck(0, 0);
		hs.computeImageDerivatives(Ix, Iy, It, testMv.fb1, testMv.fb2);

		for (int i = 0; i < testMv.height; i++)
		{
			for (int j = 0; j < testMv.width; j++)
			{
				assertEquals(Ix[i][j], refX[i][j], 0.1);
				assertEquals(Iy[i][j], refY[i][j], 0.1);
				assertEquals(It[i][j], refT[i][j], 0.1);
			}
		}
	}

	@Test
	public void testComputeLocalAveragesSmall()
	{
		float[][] refU = { { 0, 0 }, { 0, 0 } };
		float[][] refV = { { 0, 0 }, { 0, 0 } };

		float[][] uBar = new float[refU.length][refU[0].length];
		float[][] vBar = new float[refV.length][refV[0].length];

		// result for this method is unaffected by iterations and alpha
		HornSchunck hs = new HornSchunck(0, 0);
		hs.computeLocalAverages(uBar, vBar, refU, refV);

		for (int i = 0; i < uBar.length; i++)
		{
			for (int j = 0; j < uBar[0].length; j++)
			{
				assertEquals(uBar[i][j], refU[i][j], 0.1);
				assertEquals(vBar[i][j], refV[i][j], 0.1);
			}
		}
	}

	@Test
	public void testComputeImageDerivatives()
	{
		float[][] refX = { { 0, -63.75f, 0, 63.75f, 0, 0 }, { 0, -127.5f, 0, 127.5f, 0, 0 }, { 0, -127.5f, 0, 127.5f, 0, 0 },
				{ 0, -127.5f, 0, 127.5f, 0, 0 }, { 0, -63.75f, 0, 63.75f, 0, 0 }, { 0, 0, 0, 0, 0, 0 } };

		float[][] refY = { { 0, -63.75f, -127.5f, -63.75f, 0, 0 }, { 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0 },
				{ 0, 63.75f, 127.5f, 63.75f, 0, 0 }, { 0, 0, 0, 0, 0, 0 } };

		float[][] refT = { { 0, 63.75f, 0, -63.75f, 0, 0 }, { 0, 127.5f, 0, -127.5f, 0, 0 }, { 0, 127.5f, 0, -127.5f, 0, 0 },
				{ 0, 127.5f, 0, -127.5f, 0, 0 }, { 0, 63.75f, 0, -63.75f, 0, 0 }, { 0, 0, 0, 0, 0, 0 } };

		Images rightMv = new Images(Experiment.TEST_PATH + "testHS1.png", Experiment.TEST_PATH + "testHS2.png");
		rightMv.init();

		int h = rightMv.height;
		int w = rightMv.width;

		float[][] Ix = new float[h][w];
		float[][] Iy = new float[h][w];
		float[][] It = new float[h][w];

		HornSchunck hs = new HornSchunck(0, 0);
		hs.computeImageDerivatives(Ix, Iy, It, rightMv.fb1, rightMv.fb2);

		for (int i = 0; i < rightMv.height; i++)
		{
			for (int j = 0; j < rightMv.width; j++)
			{
				assertEquals(Ix[i][j], refX[i][j], 0.1);
				assertEquals(Iy[i][j], refY[i][j], 0.1);
				assertEquals(It[i][j], refT[i][j], 0.1);
			}
		}
	}

	@Test
	public void testComputeLocalAverages()
	{
		float[][] refU = { { 1, 2, 1 }, { 1, 3, 1 }, { 0, 4, 1 } };
		float[][] refV = { { 1, 0, 1 }, { 3, 3, 1 }, { 5, 1, 0 } };

		float[][] uBar = new float[refU.length][refU[0].length];
		float[][] vBar = new float[refV.length][refV[0].length];

		// result for this method is unaffected by iterations and alpha
		HornSchunck hs = new HornSchunck(1, 10);
		hs.computeLocalAverages(uBar, vBar, refU, refV);

		assertEquals(uBar[1][1], 1.583, 0.001);
		assertEquals(vBar[1][1], 1.416, 0.001);
	}

	@Test
	public void testGpNeumann()
	{
		float[][] refArr = { { 0, 1, 2 }, { 3, 4, 5 }, { 6, 7, 8 } };

		HornSchunck hs = new HornSchunck(1, 10);
		assertEquals(hs.gpNeumann(refArr, -1, 2), 6, 0);
		assertEquals(hs.gpNeumann(refArr, 1, -5), 1, 0);
		assertEquals(hs.gpNeumann(refArr, 2, 9), 8, 0);
		assertEquals(hs.gpNeumann(refArr, 8, 1), 5, 0);
		assertEquals(hs.gpNeumann(refArr, 1, 1), 4, 0);
	}
}
