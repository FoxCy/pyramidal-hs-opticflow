package tests;

import static org.junit.Assert.*;
import hs_pyrimidal.Interpolator;

import org.junit.Test;

public class TestInterpolator
{
	@Test
	public void testGetCubic()
	{
		float refValue = 4;
		double[] points = { 1, 2, 3, 4 };
		int polPoint = 2;

		Interpolator pol = new Interpolator();
		assertEquals(pol.getCubic(points, polPoint), refValue, 0.0001);
	}

	@Test
	public void testGetBicubic()
	{
		float refValue = 2.0f;
		double[][] points = { { 1, 8, 3, 4 }, { 1, 2, 3, 4 }, { 1, 1, 3, 4 }, { 1, 2, 4, 4 } };

		int polX = 0;
		int polY = 0;

		Interpolator pol = new Interpolator();
		assertEquals(pol.getBicubic(points, polX, polY), refValue, 0.0001);
	}
}
