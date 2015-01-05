package hs_pyrimidal;

import java.text.DecimalFormat;

import utility.Images;
import utility.VectorField;

/**
 * 
 * @author Connor Fox
 * 
 */
public class HornSchunckPyrimidal
{
	public float			alpha;
	public float			alpha2;
	public int				nWarps;
	public float			stopCrt;
	public int				maxIterations;
	public int				nScales;
	public float			dFactor;

	public final float		SOR_W				= 1.9f;
	public final float		PRESMOOTHING_SIGMA	= 0.8f;

	// An aribitrary constant to initilize variables so we know if they are being set correctly.
	public final int		BAD_VALUE			= 6661289;
	
	private DecimalFormat trunc = new DecimalFormat("##.################");
	private Interpolator	ipl;

	/**
	 * Set the parameters to default values ref: Meinhardt-Lopis 167
	 */
	public HornSchunckPyrimidal()
	{
		alpha = 15;
		nWarps = 5;
		stopCrt = 0.0001f;
		maxIterations = 150;
		nScales = 5;
		dFactor = 0.65f;

		ipl = new Interpolator();
	}

	public void setParams(float alpha, int nWarps, float stopCrt, int maxIterations, int nScales, float dFactor)
	{
		this.alpha = alpha;
		this.nWarps = nWarps;
		this.stopCrt = stopCrt;
		this.maxIterations = maxIterations;
		this.nScales = nScales;
		this.dFactor = dFactor;
	}

	/**
	 * Check that the given number of scales will not go below 16 x 16.
	 * 
	 * @param width
	 * @param height
	 * @return
	 */
	public int checkScales(int width, int height)
	{
		int newScales = (int) (1 + Math.log(Math.hypot(width, height) / 16) / Math.log(1 / dFactor));
		return newScales;
	}

	/**
	 * Run the pyrimidal Horn-Schunck approximation.
	 * 
	 * @param t1
	 * @param t2
	 * @return
	 */
	public VectorField run(float[][] t1, float[][] t2)
	{
		alpha2 = alpha * alpha;

		// Set the best value for nScales
		int ns = nScales;
		int check = checkScales(t1.length, t1[0].length);
		if (check < ns) ns = check;

		// Create the scales (0 = original image)
		Images[] iScales = createImagePyramid(t1, t2, ns, dFactor, true);
		VectorField[] vScales = new VectorField[ns];

		// Initialize VectorField scales
		vScales[0] = new VectorField(iScales[0].height, iScales[0].width);
		for (int i = 1; i < vScales.length; i++)
		{
			vScales[i] = new VectorField((int) Math.ceil(vScales[i - 1].height * dFactor), (int) Math.ceil(vScales[i - 1].width * dFactor));
		}

		System.out.format("Coarse Image check (5,0): %5.30f", iScales[ns - 1].fb2[0][5]);
		
		for (int i = ns - 1; i >= 0; i--)
		{
			// Compute optical flow at scale i
			System.out.println("Computing scale " + i + " with alpha=" + alpha + "...");
			hs(iScales[i].fb1, iScales[i].fb2, vScales[i].u, vScales[i].v);

			// Skip if on last scale
			if (i != 0)
			{
				// upsample vectors to next scale
				vScales[i - 1].u = upsample(vScales[i].u, vScales[i - 1].width, vScales[i - 1].height);
				vScales[i - 1].v = upsample(vScales[i].v, vScales[i - 1].width, vScales[i - 1].height);

				// Scale flow
				for (int y = 0; y < iScales[i - 1].height; y++)
				{
					for (int x = 0; x < iScales[i - 1].width; x++)
					{
						vScales[i - 1].u[y][x] *= 1.0 / dFactor;
						vScales[i - 1].v[y][x] *= 1.0 / dFactor;
					}
				}
			}
		}

		// The final scale of vectors is our result.
		return vScales[0];
	}

	/**
	 * Compute one SOR iteration at a point
	 * 
	 * @param u
	 * @param v
	 * @param Au
	 * @param Av
	 * @param Du
	 * @param Dv
	 * @param D
	 * @param px
	 * @param py
	 * @return
	 */
	public float sor(float[][] u, float[][] v, final float[][] Au, final float[][] Av, final float[][] Du, final float[][] Dv, final float[][] D,
			final int[] px, final int[] py)
	{
		final float divU = (float) ((1.0 / 12.0) * (u[py[1]][px[1]] + u[py[1]][px[2]] + u[py[2]][px[1]] + u[py[2]][px[2]]) + (1.0 / 6.0)
				* (u[py[1]][px[0]] + u[py[0]][px[1]] + u[py[2]][px[0]] + u[py[0]][px[2]]));

		final float divV = (float) ((1.0 / 12.0) * (v[py[1]][px[1]] + v[py[1]][px[2]] + v[py[2]][px[1]] + v[py[2]][px[2]]) + (1.0 / 6.0)
				* (v[py[1]][px[0]] + v[py[0]][px[1]] + v[py[2]][px[0]] + v[py[0]][px[2]]));

		final int y = py[0];
		final int x = px[0];

		// Store previous u & v values
		final float u1 = u[y][x];
		final float v1 = v[y][x];

		// Calculate new u & v values
		u[y][x] = (float) ((1.0 - SOR_W) * u1 + SOR_W * (Au[y][x] - D[y][x] * v[y][x] + alpha2 * divU) / Du[y][x]);
		v[y][x] = (float) ((1.0 - SOR_W) * v1 + SOR_W * (Av[y][x] - D[y][x] * u[y][x] + alpha2 * divV) / Dv[y][x]);

		return (u[y][x] - u1) * (u[y][x] - u1) + (v[y][x] - v1) * (v[y][x] - v1);
	}

	/**
	 * Compute the Horn-Schunck optical flow for one scale.
	 * 
	 * @param t1 first frame
	 * @param t2 second frame
	 * @param u x vector field
	 * @param v y vector field
	 */
	public void hs(float[][] t1, float[][] t2, float[][] u, float[][] v)
	{
		int height = t1.length;
		int width = t1[0].length;
		
		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				t1[i][j] = Float.parseFloat(trunc.format(t1[i][j]));
				t2[i][j] = Float.parseFloat(trunc.format(t2[i][j]));
			}
		}

		// Warped image, x and y derivatives
		float[][] t2Warp;
		float[][] t2WarpY;
		float[][] t2WarpX;

		// x and y derivatives (centered differences)
		float[][] t2x = new float[height][width];
		float[][] t2y = new float[height][width];

		// Constants
		float[][] Au = new float[height][width];
		float[][] Av = new float[height][width];
		float[][] Du = new float[height][width];
		float[][] Dv = new float[height][width];
		float[][] D = new float[height][width];

		System.out.println("Preforming Horn-Schunck on " + width + "x" + height + " scale image...");

		if (t2.length != height || t2[0].length != width) System.out.println("ALERT! Image sequence dimensions don't match!");

		// Compute gradient of second image
		gradient(t2, t2x, t2y);

//		System.out.println(t2[0][0]);
//		System.out.println(t2x[0][0]);
//		System.out.println(t2y[0][0]);

		for (int warp = 0; warp < nWarps; warp++)
		{
			System.out.print("Warp " + warp + ":");

			// I2(x + h), I2y(x + h), I2x(x + h)
			t2Warp = warp(t2, u, v, true);
			t2WarpY = warp(t2y, u, v, true);
			t2WarpX = warp(t2x, u, v, true);
			
			//if (warp == 0) System.out.format("t2Warp[0][5]: %5.20f\n", t2Warp[0][5]);

			// Load constants
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					final float t2d = t2WarpX[y][x] * u[y][x] + t2WarpY[y][x] * v[y][x];
					final float d = t1[y][x] - t2Warp[y][x] + t2d;

					Au[y][x] = d * t2WarpX[y][x];
					Av[y][x] = d * t2WarpY[y][x];
					Du[y][x] = t2WarpX[y][x] * t2WarpX[y][x] + alpha2;
					Dv[y][x] = t2WarpY[y][x] * t2WarpY[y][x] + alpha2;
					D[y][x] = t2WarpX[y][x] * t2WarpY[y][x];
				}
			}

			float nIter = 0;
			float error = 1000;

			// Repeat SOR until error is acceptable or max iterations reached
			while (error > stopCrt && nIter < maxIterations)
			{
				nIter++;
				error = 0;

				int[] px = new int[3];
				int[] py = new int[3];

				// Do 1 SOR computation for every flow value in the image
				for (int y = 0; y < height; y++)
				{
					for (int x = 0; x < width; x++)
					{
						// neighbor x coordinates
						px[0] = x;
						px[1] = gp(x - 1, width);
						px[2] = gp(x + 1, width);

						// neighbor y coordinates
						py[0] = y;
						py[1] = gp(y - 1, height);
						py[2] = gp(y + 1, height);

						// Calculate a sor iteration on the current pixel
						error += sor(u, v, Au, Av, Du, Dv, D, px, py);
					}
				}

//				 //SOR central part of image
//				 for (int y = 1; y < height - 1; y++)
//				 {
//				 for (int x = 1; x < width - 1; x++)
//				 {
//				 px[0] = x; px[1] = x - 1; px[2] = x + 1;
//				 py[0] = y; py[1] = y - 1; py[2] = y + 1;
//				
//				 error += sor(u, v, Au, Av, Du, Dv, D, px, py);
//				 }
//				 }
//				
//				 //first and last rows
//				 for (int x = 1; x < width - 1; x++)
//				 {
//				 px[0] = x; px[1] = x - 1; px[2] = x + 1;
//				 py[0] = 0; py[1] = 0; py[2] = 1;
//				 error += sor(u, v, Au, Av, Du, Dv, D, px, py);
//				
//				 px[0] = x; px[1] = x - 1; px[2] = x + 1;
//				 py[0] = height - 1; py[1] = height - 2; py[2] = height - 1;
//				 error += sor(u, v, Au, Av, Du, Dv, D, px, py);
//				 }
//				
//				 //first and last columns
//				 for (int y = 1; y < height - 1; y++)
//				 {
//				 px[0] = 0; px[1] = 0; px[2] = 1;
//				 py[0] = y; py[1] = y - 1; py[2] = y + 1;
//				 error += sor(u, v, Au, Av, Du, Dv, D, px, py);
//				
//				 px[0] = width - 1; px[1] = width - 2; px[2] = width - 1;
//				 py[0] = y; py[1] = y - 1; py[2] = y + 1;
//				 error += sor(u, v, Au, Av, Du, Dv, D, px, py);
//				 }
//				
//				 //up left corner
//				 px[0] = 0; px[1] = 0; px[2] = 1;
//				 py[0] = 0; py[1] = 0; py[2] = 1;
//				 error += sor(u, v, Au, Av, Du, Dv, D, px, py);
//				
//				 //up right corner
//				 px[0] = width - 1; px[1] = width - 2; px[2] = width - 1;
//				 py[0] = 0; py[1] = 0; py[2] = 1;
//				 error += sor(u, v, Au, Av, Du, Dv, D, px, py);
//				
//				 //bottom left corner
//				 px[0] = 0; px[1] = 0; px[2] = 1;
//				 py[0] = height - 1; py[1] = height - 2; py[2] = height - 1;
//				 error += sor(u, v, Au, Av, Du, Dv, D, px, py);
//				
//				 //bottom right corner
//				 px[0] = width - 1; px[1] = width - 2; px[2] = width - 1;
//				 py[0] = height - 1; py[1] = height - 2; py[2] = height - 1;
//				 error += sor(u, v, Au, Av, Du, Dv, D, px, py);

				// total error this iteration
				error = (float) Math.sqrt(error / (width * height));
				
				if (warp == 0) System.out.format("error: %5.20f\n", error);
			}

			System.out.println(" Iterations: " + nIter + "(" + error + ")");
		}
	}

	/**
	 * 
	 * @param x
	 * @param w
	 * @return
	 */
	public int gp(int x, int w)
	{
		int p = x;
		if (x < 0) p = 0;
		else if (x >= w) p = w - 1;

		return p;
	}

	/**
	 * Compute the x & y gradient of an image with centered differences.
	 * 
	 * @param img
	 * @param Ix
	 * @param Iy
	 */
	public void gradient(final float[][] img, float[][] Ix, float[][] Iy)
	{
		int height = img.length;
		int width = img[0].length;
		int h = height - 1;
		int w = width - 1;

		// Compute gradient in central image space
		for (int y = 1; y < h; y++)
		{
			for (int x = 1; x < w; x++)
			{
				Ix[y][x] = (float) (0.5 * (img[y][x + 1] - img[y][x - 1]));
				Iy[y][x] = (float) (0.5 * (img[y + 1][x] - img[y - 1][x]));
			}
		}

		// First & last row
		for (int x = 1; x < w; x++)
		{
			// First row
			Ix[0][x] = (float) (0.5 * (img[0][x + 1] - img[0][x - 1]));
			Iy[0][x] = (float) (0.5 * (img[1][x] - img[0][x]));

			// Last row
			Ix[h][x] = (float) (0.5 * (img[h][x + 1] - img[h][x - 1]));
			Iy[h][x] = (float) (0.5 * (img[h][x] - img[h - 1][x]));
		}

		// First & last column
		for (int y = 1; y < h; y++)
		{
			// First column
			Ix[y][0] = (float) (0.5 * (img[y][1] - img[y][0]));
			Iy[y][0] = (float) (0.5 * (img[y + 1][0] - img[y - 1][0]));

			// Last column
			Ix[y][w] = (float) (0.5 * (img[y][w] - img[y][w - 1]));
			Iy[y][w] = (float) (0.5 * (img[y + 1][w] - img[y - 1][w]));
		}

		// Top left
		Ix[0][0] = (float) (0.5 * (img[0][1] - img[0][0]));
		Iy[0][0] = (float) (0.5 * (img[1][0] - img[0][0]));

		// Top right
		Ix[0][w] = (float) (0.5 * (img[0][w] - img[0][w - 1]));
		Iy[0][w] = (float) (0.5 * (img[1][w] - img[0][w]));

		// Bottom left
		Ix[h][0] = (float) (0.5 * (img[h][1] - img[h][0]));
		Iy[h][0] = (float) (0.5 * (img[h][0] - img[h - 1][0]));

		// bottom right
		Ix[h][w] = (float) (0.5 * (img[h][w] - img[h][w - 1]));
		Iy[h][w] = (float) (0.5 * (img[h][w] - img[h - 1][w]));
	}

	/**
	 * 
	 * @param f1
	 * @param f2
	 * @param nScales
	 * @param dFactor
	 * @return
	 */
	public Images[] createImagePyramid(float[][] f1, float[][] f2, int nScales, float dFactor, boolean smooth)
	{
		Images[] imgScales = new Images[nScales];
		imgScales[0] = new Images(f1.length, f1[0].length);

		Images.normalizeGreyscale(f1, f2, imgScales[0].fb1, imgScales[0].fb2);

		if (smooth)
		{
			imgScales[0].fb1 = Images.applyGaussianSmoothing(imgScales[0].fb1, 5, PRESMOOTHING_SIGMA);
			imgScales[0].fb2 = Images.applyGaussianSmoothing(imgScales[0].fb2, 5, PRESMOOTHING_SIGMA);
		}

		// Create the pyramid at each scale
		for (int i = 1; i < nScales; i++)
		{
			float[][] sImg1 = downSample(imgScales[i - 1].fb1, dFactor, smooth, false);
			float[][] sImg2 = downSample(imgScales[i - 1].fb2, dFactor, smooth, false);

			imgScales[i] = new Images(sImg1, sImg2);
		}

		return imgScales;
	}

	/**
	 * 
	 * 
	 * @param img
	 * @param dFactor
	 * @return
	 */
	public float[][] downSample(float[][] img, float dFactor, boolean smooth, boolean boundZero)
	{
		final int oldHeight = img.length;
		final int oldWidth = img[0].length;

		final int newWidth = (int) ((float) oldWidth * dFactor + 0.5);
		final int newHeight = (int) ((float) oldHeight * dFactor + 0.5);
		
		if (smooth)
		{
			final float sigma = (float) (0.6f * Math.sqrt(((1.0 / (dFactor * dFactor))) - 1.0));
			img = Images.applyGaussianSmoothing(img, 5, sigma);
		}
		
		float[][] scaledImage = new float[newHeight][newWidth];

		for (int y = 0; y < newHeight; y++)
		{
			for (int x = 0; x < newWidth; x++)
			{
				final float oldY = (float) y / dFactor;
				final float oldX = (float) x / dFactor;

				scaledImage[y][x] = getInterpolationPoint(img, oldX, oldY, boundZero);
			}
		}

		return scaledImage;
	}

	/**
	 * Compute the bicubic interpolation
	 * 
	 * @param img
	 * @param x
	 * @param y
	 * @return
	 */
	public float getInterpolationPoint(float[][] img, double x, double y, boolean boundZero)
	{
		double[][] kern = new double[4][4];
		float newValue = 0.0f;
		boolean[] out = {false};

		int sx = (x < 0) ? -1 : 1;
		int sy = (y < 0) ? -1 : 1;
		
//		int nx = img[0].length;
//		int ny = img.length;

		int xn = gp((int) x, img[0].length);
		int yn = gp((int) y, img.length);
		
//		int xx, yy, mx, my, dx, dy, ddx, ddy;
//		
//		xx = gp((int) x, nx, out);
//		yy = gp((int) y, ny, out);
//		mx = gp((int) x - sx, nx, out);
//		my = gp((int) y - sy, ny, out);
//		dx = gp((int) x + sx, nx, out);
//		dy = gp((int) y + sy, ny, out);
//		ddx = gp((int) x + sx * 2, nx, out);
//		ddy = gp((int) y + sy * 2, ny, out);
//		
//		final float p11 = img[my][mx];
//		final float p12 = img[my][xx];
//		final float p13 = img[my][dx];
//		final float p14 = img[my][ddx];
//
//		final float p21 = img[yy][mx];
//		final float p22 = img[yy][xx];
//		final float p23 = img[yy][dx];
//		final float p24 = img[yy][ddx];
//
//		final float p31 = img[dy][mx];
//		final float p32 = img[dy][xx];
//		final float p33 = img[dy][dx];
//		final float p34 = img[dy][ddx]; 
//
//		final float p41 = img[ddy][mx];
//		final float p42 = img[ddy][xx];
//		final float p43 = img[ddy][dx];
//		final float p44 = img[ddy][ddx];
//		
//		kern = new double[][] {{p11, p21, p31, p41},
//							   {p12, p22, p32, p42},
//							   {p13, p23, p33, p43},
//							   {p14, p24, p34, p44}};

		kern[0][0] = gpNeumann(img, (int) y - sy, (int) x - sx, out);
		kern[1][0] = gpNeumann(img, (int) y - sy, (int) x, out);
		kern[2][0] = gpNeumann(img, (int) y - sy, (int) x + sx, out);
		kern[3][0] = gpNeumann(img, (int) y - sy, (int) x + 2 * sx, out);

		kern[0][1] = gpNeumann(img, (int) y, (int) x - sx, out);
		kern[1][1] = gpNeumann(img, (int) y, (int) x, out);
		kern[2][1] = gpNeumann(img, (int) y, (int) x + sx, out);
		kern[3][1] = gpNeumann(img, (int) y, (int) x + 2 * sx, out);

		kern[0][2] = gpNeumann(img, (int) y + sy, (int) x - sx, out);
		kern[1][2] = gpNeumann(img, (int) y + sy, (int) x, out);
		kern[2][2] = gpNeumann(img, (int) y + sy, (int) x + sx, out);
		kern[3][2] = gpNeumann(img, (int) y + sy, (int) x + 2, out);

		kern[0][3] = gpNeumann(img, (int) y + 2 * sy, (int) x - sx, out);
		kern[1][3] = gpNeumann(img, (int) y + 2 * sy, (int) x, out);
		kern[2][3] = gpNeumann(img, (int) y + 2 * sy, (int) x + sx, out);
		kern[3][3] = gpNeumann(img, (int) y + 2 * sy, (int) x + 2 * sx, out);

		if (out[0] && boundZero)
		{
			newValue = 0.0f;
		}

		else
		{
			newValue = (float) ipl.getBicubic(kern, x - xn, y - yn);
		}

		return newValue;
	}

//	public int gp(int x, int w, boolean[] out)
//	{
//		if (x < 0)
//		{
//			x = 0;
//			out[0] = true;
//		}
//		else if (x >= w)
//		{
//			x = w - 1;
//			out[0] = true;
//		}
//
//		return x;
//	}

	/**
	 * Get pixel method with Neumann boundary conditions
	 * 
	 * @param arr Pixel data array
	 * @param x The x positon of the desired point
	 * @param y The y position of the desired point
	 * @return The value of the pixel at (x, y) unless it falls outside the boundary of the image in which case the
	 * Neumann boundary condition is returned.
	 */
	public float gpNeumann(float[][] arr, int y, int x, boolean[] out)
	{
		float px = 0;

		int height = arr.length;
		int width = arr[0].length;

		if (x < 0)
		{
			x = 0;
			out[0] = true;
		}

		else if (x >= width)
		{
			x = width - 1;
			out[0] = true;
		}

		if (y < 0)
		{
			y = 0;
			out[0] = true;
		}

		else if (y >= height)
		{
			y = height - 1;
			out[0] = true;
		}

		px = arr[y][x];

		return px;
	}

	/**
	 * 
	 * @param dFactor
	 * @return
	 */
	public int getBestScales(float dFactor, float maxMotion)
	{
		int nScales = BAD_VALUE;
		nScales = (int) (Math.log(maxMotion) / Math.log(dFactor) + 1);
		return nScales;
	}

	/**
	 * Upscale a 2d array of vectors to a new size using bicubic interpolation.
	 * 
	 * @param img
	 * @param newWidth
	 * @param newHeight
	 */
	public float[][] upsample(float[][] field, int newWidth, int newHeight)
	{
		int oldHeight = field.length;
		int oldWidth = field[0].length;

		float ufx = ((float) newWidth / oldWidth);
		float ufy = ((float) newHeight / oldHeight);

		float[][] scaledImage = new float[newHeight][newWidth];

		for (int y = 0; y < newHeight; y++)
		{
			for (int x = 0; x < newWidth; x++)
			{
				float oldY = (float) y / ufy;
				float oldX = (float) x / ufx;

				scaledImage[y][x] = getInterpolationPoint(field, oldX, oldY, false);
			}
		}

		return scaledImage;
	}

	/**
	 * Move the pixels of an image along a vector.
	 * 
	 * @param image The image to be warped
	 * @param u The x component of the warping vector field
	 * @param v The y component of the warping vector field
	 * @param boundZero Whether to use a zero boundary condition for points outside the image area
	 * @return The result of warping the original image along the given vectors
	 */
	public float[][] warp(float[][] image, float[][] u, float[][] v, boolean boundZero)
	{
		float[][] warpedImage = new float[image.length][image[0].length];

		int h = image.length;
		int w = image[0].length;

		for (int y = 0; y < h; y++)
		{
			for (int x = 0; x < w; x++)
			{
				final float newX = (float) (x + u[y][x]);
				final float newY = (float) (y + v[y][x]);

				warpedImage[y][x] = getInterpolationPoint(image, newX, newY, boundZero);
			}
		}

		return warpedImage;
	}
}
