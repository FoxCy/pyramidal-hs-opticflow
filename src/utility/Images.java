package utility;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.imageio.ImageIO;

public class Images implements Serializable
{
	private static final long	serialVersionUID	= 282984093637143045L;

	public String				frame1path, frame2path;

	// original frame images
	public transient BufferedImage	f1, f2;

	// frame byte data (y, x)
	public float[][]				fb1, fb2;

	// image width and height (both frames must be consistent)
	public int						width, height;

	public Images(int height, int width)
	{
		fb1 = new float[height][width];
		fb2 = new float[height][width];
		
		this.width = width;
		this.height = height;
	}
	
	/**
	 * Constructor for a two image sequence (default/standard)
	 * 
	 * @param frame1 The first image frame
	 * @param frame2 The second image frame
	 */
	public Images(String frame1path, String frame2path)
	{
		this.frame1path = frame1path;
		this.frame2path = frame2path;
	}

	/**
	 * 
	 * @param imgBytes1
	 * @param imgBytes2
	 */
	public Images(float[][] imgBytes1, float[][] imgBytes2)
	{
		fb1 = imgBytes1;
		fb2 = imgBytes2;
		
		height = fb1.length;
		width = fb1[0].length;
	}

	/**
	 * Initialize the byte holder variables for the images
	 */
	public void init()
	{
		try
		{
			this.f1 = ImageIO.read(new File(frame1path));
			this.f2 = ImageIO.read(new File(frame2path));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		width = f1.getWidth();
		height = f1.getHeight();

		fb1 = to2dArray(extractByteDataGrayscale(f1), width);
		fb2 = to2dArray(extractByteDataGrayscale(f2), width);
	}

	/**
	 * Write a 2d array of floats to an image and save the image to a file
	 * 
	 * @param values
	 * @param fileName
	 * @return
	 */
	public static BufferedImage floatsToImage(float[][] values, String fileName)
	{
		int w = values[0].length;
		int h = values.length;

		byte[] bytes = new byte[w * h];
		BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_GRAY);

		for (int i = 0; i < values.length; i++)
		{
			for (int j = 0; j < values[0].length; j++)
			{
				bytes[(i * values[0].length) + j] = (byte) values[i][j];
			}
		}

		byte[] imgData = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
		System.arraycopy(bytes, 0, imgData, 0, bytes.length);

		try
		{
			ImageIO.write(img, "png", new File(fileName));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return img;
	}

	/**
	 * Normalize an array of greyscale byte data Returns unsigned bytes as a 2-D array of floats
	 * @param i1 frame 1 input
	 * @param i2 frame 2 input
	 * @param n1 frame 1 output
	 * @param n2 frame 2 output
	 */
	public static void normalizeGreyscale(final float[][] i1, final float[][] i2, float[][] n1, float[][] n2)
	{
		float maxValue = Float.MIN_VALUE;
		float minValue = Float.MAX_VALUE;
		float diff = -1;
		float v = -1;

		// Determine value range
		for (int i = 0; i < i1.length; i++)
		{
			for (int j = 0; j < i1[0].length; j++)
			{
				v = i1[i][j];

				if (v > maxValue)
				{
					maxValue = v;
				}

				else if (v < minValue)
				{
					minValue = v;
				}
				
				v = i2[i][j];
				
				if (v > maxValue)
				{
					maxValue = v;
				}

				else if (v < minValue)
				{
					minValue = v;
				}
			}
		}

		diff = maxValue - minValue;

		// Normalize all bytes according to algorithm:
		// (I[n] - min) * (255 / max - min)
		for (int i = 0; i < i1.length; i++)
		{
			for (int j = 0; j < i1[0].length; j++)
			{
				n1[i][j] = ((i1[i][j] - minValue) * (255 / diff));
				n2[i][j] = ((i2[i][j] - minValue) * (255 / diff));
			}
		}
	}

	/**
	 * Smooth the image using the gaussian blur function Use reflecting boundary conditions
	 */
	public static float[][] applyGaussianSmoothing(float[][] image, int radius, double sigma)
	{
		int h = image.length;
		int w = image[0].length;
		double sum = 0;
		int size = (int) (radius * sigma) + 1;
		
		if (size > w)
			System.err.println("Gaussian Smooth: sigma too large for window size.");

		//Create a tempory working array of the image as doubles.
		double[][] tmpImg = new double[h][w];
		for (int i = 0; i < h; i++)
		{
			for (int j = 0; j < w; j++)
			{
				tmpImg[i][j] = image[i][j];
			}
		}
		
		double[][] gsImgX = new double[h][w];
		float[][] gsImgY = new float[h][w];
		double[] kernel = create1DGaussianKernel(size, sigma);
		
		//Normalize kernel again (may not be nessecary)
		double n = 0;
		for (double d : kernel)
		{
			n += d;
		}
		
		n *= 2;
		n -= kernel[0];
		
		for (int i = 0; i < size; i++)
		{
			kernel[i] /= n;
		}
		
		// convolve in x
		for (int y = 0; y < h; y++)
		{
			for (int x = 0; x < w; x++)
			{
				sum = gpReflect(tmpImg, x, y) * kernel[0]; // TODO doubles instead of floats for intermediate arrays

				for (int i = 1; i < size; i++)
				{
					sum += gpReflect(tmpImg, x + i, y) * kernel[i];
					sum += gpReflect(tmpImg, x - i, y) * kernel[i];
				}

				gsImgX[y][x] = sum;
			}
		}

		// convolve in y
		for (int y = 0; y < h; y++)
		{
			for (int x = 0; x < w; x++)
			{
				sum = gpReflect(gsImgX, x, y) * kernel[0];

				for (int i = 1; i < size; i++)
				{
					sum += gpReflect(gsImgX, x, y + i) * kernel[i];
					sum += gpReflect(gsImgX, x, y - i) * kernel[i];
				}

				gsImgY[y][x] = (float) sum;
			}
		}

		return gsImgY;
	}

	/**
	 * Gets the value of the point at a given x and y with reflecting boundary conditions
	 * 
	 * @param arr
	 * @param x
	 * @param y
	 * @return
	 */
	public static double gpReflect(double[][] arr, int x, int y)
	{
		int h = arr.length - 1;
		int w = arr[0].length - 1;

		if (x < 0)
		{
			x = 0 - x;
		}

		else if (x > w)
		{
			x = w - (x - w);
		}

		if (y < 0)
		{
			y = 0 - y;
		}

		else if (y > h)
		{
			y = h - (y - h);
		}

		return arr[y][x];
	}

	/**
	 * 
	 * @param size
	 * @param sigma
	 * @return a kernel matrix for the Gaussian blur
	 */
	public static double[] create1DGaussianKernel(int size, double sigma)
	{
		double[] kernel = new double[size];
		double constant = (sigma * Math.sqrt(2.0 * 3.1415926));

		for (int i = 0; i < size; i++)
		{
			kernel[i] = 1 / constant * Math.exp(-i * i / (2 * sigma * sigma));
			//kernel[i] = (Math.pow(Math.E, -((i * i) / (2 * sigma * sigma))) / constant);
		}

		return kernel;
	}

	/**
	 * Extract the needed byte data of each pixel. Byte data is stored in BGR order. All inputs must have bit depth 8.
	 */
	public static float[] extractByteDataGrayscale(BufferedImage image)
	{
		// IMPORTANT: convert all images to use 8-bit depth grayscale colormodel
		if (image.getType() != BufferedImage.TYPE_BYTE_GRAY)
		{
			BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
			tmp.getGraphics().drawImage(image, 0, 0, null);
			image = tmp;
		}

		WritableRaster raster = image.getRaster();
		DataBufferByte data = (DataBufferByte) raster.getDataBuffer();
		byte[] bytes = data.getData();

		float[] unsignedBytes = new float[bytes.length];

		// unsign the bytes (trust me, this is nessecary)
		for (int i = 0; i < bytes.length; i++)
		{
			unsignedBytes[i] = (short) (bytes[i] & 0xff);
		}

		return unsignedBytes;
	}

	/**
	 * 
	 * @param d
	 * @param width
	 * @return
	 */
	public static float[][] to2dArray(float[] d, int width)
	{
		float[][] dd = new float[d.length / width][width];

		for (int i = 0; i < d.length; i++)
		{
			dd[i / width][i % width] = d[i];
		}

		return dd;
	}
}
