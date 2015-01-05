package tests;

import static org.junit.Assert.*;
import hs_pyrimidal.HornSchunckPyrimidal;

import org.junit.Test;

import utility.Experiment;
import utility.Images;

public class TestHornSchunckPyrimidal
{
	@Test
	public void testCheckScales()
	{
		HornSchunckPyrimidal hsp = new HornSchunckPyrimidal();
		assertEquals(hsp.checkScales(680, 480), 10);
	}
	
	@Test
	public void testCreateImagePyramid()
	{
		float[][] refValues = { { 0, 139.74f, 243.04f, 0 }, { 0, 0, 255, 0 }, { 255, 255, 255, 255 }, { 0, 0, 255, 0 } };

		Images testHS = new Images(Experiment.TEST_PATH + "testPyr.png", Experiment.TEST_PATH + "testPyr.png");
		testHS.init();

		HornSchunckPyrimidal hsp = new HornSchunckPyrimidal();
		Images[] output = hsp.createImagePyramid(testHS.fb1, testHS.fb2, 2, 0.65f, false);

		assertEquals(output[1].fb1.length, 4);
		assertEquals(output[1].fb1[0].length, 4);

		for (int i = 0; i < output[1].fb1.length; i++)
		{
			for (int j = 0; j < output[1].fb1[0].length; j++)
			{
				//assertEquals(output[1].fb1[i][j], refValues[i][j], 0.1);
			}
		}
	}

	@Test
	public void testGetInterpolationPoint()
	{
		float[][] refValues = { { 0, 0, 255, 255, 0, 0 }, { 0, 0, 255, 255, 0, 0 }, { 255, 255, 255, 255, 255, 255 },
				{ 255, 255, 255, 255, 255, 255 }, { 0, 0, 255, 255, 0, 0 }, { 0, 0, 255, 255, 0, 0 } };

		Images testHS = new Images(Experiment.TEST_PATH + "testPyr.png", Experiment.TEST_PATH + "testPyr.png");
		testHS.init();

		HornSchunckPyrimidal hsp = new HornSchunckPyrimidal();

		for (int i = 0; i < testHS.fb1.length; i++)
		{
			for (int j = 0; j < testHS.fb1[0].length; j++)
			{
				assertEquals(hsp.getInterpolationPoint(testHS.fb1, i, j, false), refValues[i][j], 0);
			}
		}
	}

	@Test
	public void testGetInterpolationPointBoundZero()
	{
		float[][] refValues = { { 0, 0, 0, 0, 0, 0 }, { 0, 0, 255, 255, 0, 0 }, { 0, 255, 255, 255, 0, 0 },
				{ 0, 255, 255, 255, 0, 0 }, { 0, 0, 0, 0, 0, 0 }, { 0, 0, 0, 0, 0, 0 } };

		Images testHS = new Images(Experiment.TEST_PATH + "testPyr.png", Experiment.TEST_PATH + "testPyr.png");
		testHS.init();

		HornSchunckPyrimidal hsp = new HornSchunckPyrimidal();

		for (int i = 0; i < testHS.fb1.length; i++)
		{
			for (int j = 0; j < testHS.fb1[0].length; j++)
			{
				assertEquals(hsp.getInterpolationPoint(testHS.fb1, i, j, true), refValues[i][j], 0);
			}
		}
	}

	@Test
	public void testDownsample()
	{
		float[][] refValues = { { 0, 0, 255, 0 }, { 0, 0, 255, 0 }, { 255, 255, 255, 255 }, { 0, 0, 255, 0 } };

		Images testHS = new Images(Experiment.TEST_PATH + "testPyr.png", Experiment.TEST_PATH + "testPyr.png");
		testHS.init();

		HornSchunckPyrimidal hsp = new HornSchunckPyrimidal();
		float[][] output = hsp.downSample(testHS.fb1, .65f, false, false);

		for (int i = 0; i < output.length; i++)
		{
			for (int j = 0; j < output[0].length; j++)
			{
				//assertEquals(output[i][j], refValues[i][j], 0.1);
			}
		}
	}

	@Test
	public void testGp()
	{
		HornSchunckPyrimidal hsp = new HornSchunckPyrimidal();
		assertEquals(hsp.gp(-5, 10), 0);
		assertEquals(hsp.gp(90, 10), 9);
	}

	@Test
	public void testGpNeumann()
	{
		float[][] refArr = { { 1, 2 }, { 3, 4 } };

		HornSchunckPyrimidal hsp = new HornSchunckPyrimidal();
		boolean[] out = {false};
		assertEquals(hsp.gpNeumann(refArr, 2, 3, out), 4, 0);
		assertEquals(hsp.gpNeumann(refArr, 0, 1, out), 2, 0);
		assertEquals(hsp.gpNeumann(refArr, -1, -1, out), 1, 0);
	}

	@Test
	public void testUpsample()
	{
		float[][] refValues = { { 0, 0, 0, 255, 255, 255, 0, 0, 0 }, { 0, 0, 0, 255, 255, 255, 0, 0, 0 }, { 0, 0, 0, 255, 255, 255, 0, 0, 0 },
				{ 255, 255, 255, 255, 255, 255, 255, 255, 255 }, { 255, 255, 255, 255, 255, 255, 255, 255, 255 },
				{ 255, 255, 255, 255, 255, 255, 255, 255, 255 }, { 0, 0, 0, 255, 255, 255, 0, 0, 0 }, { 0, 0, 0, 255, 255, 255, 0, 0, 0 },
				{ 0, 0, 0, 255, 255, 255, 0, 0, 0 } };

		Images testHS = new Images(Experiment.TEST_PATH + "testPyr.png", Experiment.TEST_PATH + "testPyr.png");
		testHS.init();

		HornSchunckPyrimidal hsp = new HornSchunckPyrimidal();
		float[][] output = hsp.upsample(testHS.fb1, 9, 9);

		for (int i = 0; i < output.length; i++)
		{
			for (int j = 0; j < output[0].length; j++)
			{
				//assertEquals(output[i][j], refValues[i][j], 0.1);
			}
		}
	}

	@Test
	public void testWarp()
	{
		float[][] refValues = { { 0, 255, 255, 0, 0, 0 }, { 255, 255, 255, 255, 255, 255 }, { 255, 255, 255, 255, 255, 255 },
				{ 0, 255, 255, 0, 0, 0 }, { 0, 255, 255, 0, 0, 0 }, { 0, 255, 255, 0, 0, 0 }, };

		float[][] u = { { 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1 }, { 1, 1, 1, 1, 1, 1 },
				{ 1, 1, 1, 1, 1, 1 } };

		float[][] v = u;

		Images test = new Images(Experiment.TEST_PATH + "testPyr.png", Experiment.TEST_PATH + "testPyr.png");
		test.init();

		HornSchunckPyrimidal hsp = new HornSchunckPyrimidal();
		float[][] out = hsp.warp(test.fb1, u, v, false);

		for (int i = 0; i < 6; i++)
		{
			for (int j = 0; j < 6; j++)
			{
				assertEquals(out[i][j], refValues[i][j], 0);
			}
		}
	}

	@Test
	public void testGradient()
	{
		float[][] refValuesX = { { 0, 127.5f, 127.5f, -127.5f, -127.5f, 0 }, { 0, 127.5f, 127.5f, -127.5f, -127.5f, 0 }, { 0, 0, 0, 0, 0, 0 },
				{ 0, 0, 0, 0, 0, 0 }, { 0, 127.5f, 127.5f, -127.5f, -127.5f, 0 }, { 0, 127.5f, 127.5f, -127.5f, -127.5f, 0 } };

		float[][] refValuesY = { { 0, 0, 0, 0, 0, 0 }, { 127.5f, 127.5f, 0, 0, 127.5f, 127.5f }, { 127.5f, 127.5f, 0, 0, 127.5f, 127.5f },
				{ -127.5f, -127.5f, 0, 0, -127.5f, -127.5f }, { -127.5f, -127.5f, 0, 0, -127.5f, -127.5f }, { 0, 0, 0, 0, 0, 0 } };

		Images test = new Images(Experiment.TEST_PATH + "testPyr.png", Experiment.TEST_PATH + "testPyr.png");
		test.init();

		HornSchunckPyrimidal hsp = new HornSchunckPyrimidal();
		float[][] Ix = new float[6][6];
		float[][] Iy = new float[6][6];
		hsp.gradient(test.fb1, Ix, Iy);

		for (int i = 0; i < 6; i++)
		{
			for (int j = 0; j < 6; j++)
			{
				assertEquals(Ix[i][j], refValuesX[i][j], 0);
				assertEquals(Iy[i][j], refValuesY[i][j], 0);
			}
		}
	}

	@Test
	public void testSOR()
	{

	}

	@Test
	public void testHS()
	{

	}
}
