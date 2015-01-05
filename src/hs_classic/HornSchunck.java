package hs_classic;

public class HornSchunck
{
	public int	iterations;
	public int	alpha;

	public HornSchunck(int iterations, int alpha)
	{
		this.iterations = iterations;
		this.alpha = alpha;
	}

	public void run(float[][] u, float[][] v, float t1[][], float[][] t2)
	{
		int h = t1.length;
		int w = t1[0].length;

		int alpha2 = alpha * alpha;

		float[][] uBar = new float[h][w];
		float[][] vBar = new float[h][w];

		float[][] Ix = new float[h][w];
		float[][] Iy = new float[h][w];
		float[][] It = new float[h][w];

		computeImageDerivatives(Ix, Iy, It, t1, t2);

		float top = 0;
		float bottom = 0;

		// Compute multiple iterations to propagate vectors all over the image
		for (int i = 0; i < iterations; i++)
		{
			computeLocalAverages(uBar, vBar, u, v);

			for (int j = 0; j < h; j++)
			{
				for (int l = 0; l < w; l++)
				{
					top = Ix[j][l] * uBar[j][l] + Iy[j][l] * vBar[j][l] + It[j][l];
					bottom = alpha2 + Ix[j][l] * Ix[j][l] + Iy[j][l] * Iy[j][l];

					// Prevent divide by zero.
					if (bottom == 0)
					{
						bottom = 1;
					}

					u[j][l] = uBar[j][l] - Ix[j][l] * (top / bottom);
					v[j][l] = vBar[j][l] - Iy[j][l] * (top / bottom);
				}
			}
		}
	}

	/**
	 * Compute the partial image derivatives for the gradient of the image
	 * between both frames
	 * 
	 * @param Ix An array to store the X gradient values
	 * @param Iy An array to store the Y gradient values
	 * @param It An array to store the time gradient values
	 * @param t1 First frame
	 * @param t2 Second frame
	 */
	public void computeImageDerivatives(float[][] Ix, float[][] Iy, float[][] It, float[][] t1, float[][] t2)
	{
		int h = t1.length;
		int w = t1[0].length;

		for (int y = 0; y < h; y++)
		{
			for (int x = 0; x < w; x++)
			{
				Ix[y][x] = (gpNeumann(t1, x + 1, y) - gpNeumann(t1, x, y) + gpNeumann(t1, x + 1, y + 1) - gpNeumann(t1, x, y + 1)
						+ gpNeumann(t2, x + 1, y) - gpNeumann(t2, x, y) + gpNeumann(t2, x + 1, y + 1) - gpNeumann(t2, x, y + 1)) / 4;

				Iy[y][x] = (gpNeumann(t1, x, y + 1) - gpNeumann(t1, x, y) + gpNeumann(t1, x + 1, y + 1) - gpNeumann(t1, x + 1, y)
						+ gpNeumann(t2, x, y + 1) - gpNeumann(t2, x, y) + gpNeumann(t2, x + 1, y + 1) - gpNeumann(t2, x + 1, y)) / 4;

				It[y][x] = (gpNeumann(t2, x, y) - gpNeumann(t1, x, y) + gpNeumann(t2, x + 1, y) - gpNeumann(t1, x + 1, y) + gpNeumann(t2, x, y + 1)
						- gpNeumann(t1, x, y + 1) + gpNeumann(t2, x + 1, y + 1) - gpNeumann(t1, x + 1, y + 1)) / 4;
			}
		}
	}

	/**
	 * 
	 * @param uBar x local averages
	 * @param vBar y local averages
	 * @param u x component of flow field
	 * @param v y component of flow field
	 */
	public void computeLocalAverages(float[][] uBar, float[][] vBar, float[][] u, float[][] v)
	{
		int h = u.length;
		int w = u[0].length;

		for (int y = 0; y < h; y++)
		{
			for (int x = 0; x < w; x++)
			{
				uBar[y][x] = ((gpNeumann(u, x - 1, y) + gpNeumann(u, x + 1, y) + gpNeumann(u, x, y - 1) + gpNeumann(u, x, y + 1)) / 6)
						+ ((gpNeumann(u, x - 1, y - 1) + gpNeumann(u, x + 1, y - 1) + gpNeumann(u, x - 1, y + 1) + gpNeumann(u, x + 1, y + 1)) / 12);

				vBar[y][x] = ((gpNeumann(v, x - 1, y) + gpNeumann(v, x + 1, y) + gpNeumann(v, x, y - 1) + gpNeumann(v, x, y + 1)) / 6)
						+ ((gpNeumann(v, x - 1, y - 1) + gpNeumann(v, x + 1, y - 1) + gpNeumann(v, x - 1, y + 1) + gpNeumann(v, x + 1, y + 1)) / 12);
			}
		}
	}

	/**
	 * get pixel method for Neumann boundary conditions
	 */
	public float gpNeumann(float[][] arr, int x, int y)
	{
		int h = arr.length;
		int w = arr[0].length;

		if (y < 0)
		{
			y = 0;
		}

		else if (y >= h)
		{
			y = h - 1;
		}

		if (x < 0)
		{
			x = 0;
		}

		else if (x >= w)
		{
			x = w - 1;
		}

		return arr[y][x];
	}
}
