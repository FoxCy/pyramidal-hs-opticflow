package hs_pyrimidal;

/**
 * 
 * @author Connor Fox
 * 
 * A class for computing cubic and bicubic interpolation at a point.
 */
public class Interpolator
{
	/**
	 * Compute a one-dimensional cubic interpolation at a point.
	 * 
	 * @param p An array of 4 points from which to interpolate the point.
	 * @param x A point at which to compute the interpolation.
	 * @return The value of the interpolation.
	 */
	public double getCubic(double[] p, double x)
	{
		double value = Double.NaN;
		value = p[1] + (x * (p[2] - p[0] + x * (2 * p[0] - 5 * p[1] + 4 * p[2] - p[3] + x * (3 * (p[1] - p[2]) + p[3] - p[0])))) / 2.0;
		return value;
	}

	/**
	 * Compute a two-dimensional cubic interpolation at a point.
	 * 
	 * @param points A 2-D 4x4 array of points to interpolate.
	 * @param x The x position of the point at which the interpolation will be computed.
	 * @param y The y position of the point at which the interpolation will be computed.
	 * @return The value of the interpolation.
	 */
	public double getBicubic(double[][] points, double x, double y)
	{
		double value = -1;
		double[] cols = new double[4];

		// Interpolate each column individually
		cols[0] = getCubic(points[0], y);
		cols[1] = getCubic(points[1], y);
		cols[2] = getCubic(points[2], y);
		cols[3] = getCubic(points[3], y);

		// Interpolate the rows from the column values
		value = getCubic(cols, x);

		return value;
	}
}
