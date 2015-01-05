package utility;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import javax.imageio.ImageIO;

public class VectorField
{
	public float[][]			u;
	public float[][]			v;

	public int					height;
	public int					width;

	public static final float	UNKNOWN	= 1e9f;

	public VectorField(float[][] u, float[][] v)
	{
		this.u = u;
		this.v = v;

		this.height = u.length;
		this.width = u[0].length;
	}

	public VectorField(int height, int width)
	{
		u = new float[height][width];
		v = new float[height][width];

		this.height = height;
		this.width = width;
	}

	/**
	 * Create a vector field from a .flo file
	 * 
	 * @param fileName
	 * @return
	 */
	public static VectorField readFromFlo(String fileName)
	{
		float[][] gu = new float[0][0];
		float[][] gv = new float[0][0];
		byte[] b;

		try
		{
			FileInputStream in = new FileInputStream(new File(fileName));
			b = new byte[4];
			in.read(b);

			float f = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat();
			if (f != 202021.25f)
			{
				System.err.println("ERROR: Could not read .flo; incorrect PIEH.");
				in.close();
				return null;
			}

			in.read(b);
			int w = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
			in.read(b);
			int h = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();

			gu = new float[h][w];
			gv = new float[h][w];

			for (int y = 0; y < h; y++)
			{
				for (int x = 0; x < w; x++)
				{
					in.read(b);
					gu[y][x] = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat();

					in.read(b);
					gv[y][x] = ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getFloat();
				}
			}

			in.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return new VectorField(gu, gv);
	}

	/**
	 * Draw the vector field to an image visualization based on the middlebury color model
	 * 
	 * @param fileName
	 * @return
	 */
	public BufferedImage drawToImageColorModel(String fileName)
	{
		BufferedImage model = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		double angle;
		double dist;
		double maxD = Double.MIN_VALUE;
		double minD = Double.MAX_VALUE;
		double diff;
		int rgb;

		try
		{
			BufferedImage colorWheel = ImageIO.read(new File("colormodel.png"));
			int centerX = (colorWheel.getWidth() / 2) - 1;
			int centerY = (colorWheel.getHeight() / 2) - 1;
			int endX, endY;

			// Get max and min vectors
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{
					if (u[y][x] <= UNKNOWN && v[y][x] <= UNKNOWN)
					{
						dist = Math.sqrt(u[y][x] * u[y][x] + v[y][x] * v[y][x]);

						if (dist > maxD) maxD = dist;
						if (dist < minD) minD = dist;
					}
				}
			}

			diff = maxD - minD;

			// Normalize and paint
			for (int y = 0; y < height; y++)
			{
				for (int x = 0; x < width; x++)
				{

					if (u[y][x] > UNKNOWN || v[y][x] > UNKNOWN)
					{
						model.setRGB(x, y, 16777215);
					}

					else
					{
						angle = Math.atan2(u[y][x], v[y][x]);
						dist = Math.sqrt(u[y][x] * u[y][x] + v[y][x] * v[y][x]);
						dist = (dist - minD) * (centerX / diff);

						endX = (int) Math.round(centerX + dist * Math.sin(angle));
						endY = (int) Math.round(centerY + dist * Math.cos(angle));

						rgb = colorWheel.getRGB(endX, endY);
						model.setRGB(x, y, rgb);
					}

				}
			}
		}

		catch (IOException e)
		{
			e.printStackTrace();
		}

		if (fileName != null)
		{
			try
			{
				ImageIO.write(model, "png", new File(fileName + ".png"));
			}

			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return model;
	}

	/**
	 * Draw the vector field to an image to allow easy visualization of field activity.
	 * 
	 * @param cellSize
	 * @return
	 */
	public BufferedImage drawToImage(int cellSize, String fileName, int cellCoverage)
	{
		BufferedImage model = new BufferedImage(width * cellSize, height * cellSize, BufferedImage.TYPE_BYTE_GRAY);

		Graphics canvas = model.getGraphics();
		canvas.setColor(Color.WHITE);
		canvas.fillRect(0, 0, width * cellSize, height * cellSize);
		canvas.setColor(Color.BLACK);

		int center = cellSize / 2;
		int len = center;

		double dist;
		double maxD = Double.MIN_VALUE;
		double minD = Double.MAX_VALUE;
		double diff;
		double angle = 0;

		int startX;
		int startY;
		int endX;
		int endY;

		// double[][] averagesX = new double[u.length / cellCoverage][u[0].length / cellCoverage];
		// double[][] averagesY = new double[v.length / cellCoverage][v[0].length / cellCoverage];
		//
		// for (int y = 0; y < averagesX.length; y++)
		// {
		// for (int x = 0; x < averagesX[0].length; x++)
		// {
		//
		// }
		// }

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				dist = Math.sqrt(u[y][x] * u[y][x] + v[y][x] * v[y][x]);

				if (dist > maxD) maxD = dist;
				if (dist < minD) minD = dist;
			}
		}

		diff = maxD - minD;

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				angle = Math.atan2(u[y][x], v[y][x]);
				dist = Math.sqrt(u[y][x] * u[y][x] + v[y][x] * v[y][x]);

				// This is essentially a normalization for drawing the vectors
				dist = (dist - minD) * (len / diff);

				startX = (cellSize * x) + center;
				startY = (cellSize * y) + center;
				endX = (int) Math.round(startX + dist * Math.sin(angle));
				endY = (int) Math.round(startY + dist * Math.cos(angle));

				canvas.drawOval(startX - 1, startY - 1, 2, 2);
				canvas.drawLine(startX, startY, endX, endY);
			}
		}

		if (fileName != null)
		{
			try
			{
				ImageIO.write(model, "png", new File(fileName + ".png"));
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}

		return model;
	}

	public void toCSV(String filename)
	{
		PrintWriter out;
		try
		{
			out = new PrintWriter(new FileOutputStream(new File(filename + ".csv")));

			for (int i = 0; i < u.length; i++)
			{
				for (int j = 0; j < u[0].length; j++)
				{
					out.print(u[i][j] + ":" + v[i][j] + ", ");
				}
				out.println();
			}

			out.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Calculate the average end point error between this object and a ground truth object.
	 * 
	 * @param groundTruth
	 * @return
	 */
	public float endPointError(VectorField groundTruth)
	{
		float average = 0;
		int size = width * height;

		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				// If ground truth value is unknown for this point, omit it from the average.
				if (groundTruth.u[i][j] > UNKNOWN || groundTruth.v[i][j] > UNKNOWN)
				{
					size--;
				}

				else
				{
					average += Math.sqrt((groundTruth.u[i][j] - u[i][j]) * (groundTruth.u[i][j] - u[i][j]) + (groundTruth.v[i][j] - v[i][j])
							* (groundTruth.v[i][j] - v[i][j]));
				}
			}
		}

		average /= size;
		return average;
	}

	/**
	 * Calculate the average angular error between this object and a ground truth object.
	 * 
	 * @param groundTruth
	 * @return
	 */
	public float angularError(VectorField groundTruth)
	{
		float average = 0;
		int size = width * height;

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				if (groundTruth.u[y][x] > UNKNOWN || groundTruth.v[y][x] > UNKNOWN)
				{
					size--;
				}
				
				else
				{
					float n1 = (float) Math.sqrt(u[y][x] * u[y][x] + v[y][x] * v[y][x] + 1.0);
					float n2 = (float) Math.sqrt(groundTruth.u[y][x] * groundTruth.u[y][x] + groundTruth.v[y][x] * groundTruth.v[y][x] + 1.0);
					
					float a1 = u[y][x] / n1;
					float a2 = v[y][x] / n1;
					float a3 = (float) (1.0 / n1);

					float b1 = groundTruth.u[y][x] / n2;
					float b2 = groundTruth.v[y][x] / n2;
					float b3 = (float) (1.0 / n2);

					// Algebraic definition of the dot product (A1 * B1 + A2 * B2 + A3 * B3)
					float dotProduct = a1 * b1 + a2 * b2 + a3 * b3;
					
					float product = (float) Math.acos(dotProduct);
					if (Float.isNaN(product))
						product = 0;
					
					average += product;
				}
			}
		}

		
		//System.out.println(average);
		average /= size;
		return (float) Math.toDegrees(average);
	}

	/**
	 * 
	 * @param groundTruth
	 * @return
	 */
	public float[] maxEPE(VectorField groundTruth)
	{
		float max = Float.MIN_VALUE;
		int[] maxXY = new int[2];

		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				float dist = (float) Math.sqrt((groundTruth.u[i][j] - u[i][j]) * (groundTruth.u[i][j] - u[i][j]) + (groundTruth.v[i][j] - v[i][j])
						* (groundTruth.v[i][j] - v[i][j]));
				if (dist > max)
				{
					max = dist;
					maxXY[0] = j;
					maxXY[1] = i;
				}
			}
		}

		return new float[] { maxXY[0], maxXY[1], max };
	}

	/**
	 * 
	 * @param groundTruth
	 * @return
	 */
	public float[] minEPE(VectorField groundTruth)
	{
		float min = Float.MAX_VALUE;
		int[] minXY = new int[2];

		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				float dist = (float) Math.sqrt((groundTruth.u[i][j] - u[i][j]) * (groundTruth.u[i][j] - u[i][j]) + (groundTruth.v[i][j] - v[i][j])
						* (groundTruth.v[i][j] - v[i][j]));
				if (dist < min)
				{
					min = dist;
					minXY[0] = j;
					minXY[1] = i;
				}
			}
		}

		return new float[] { minXY[0], minXY[1], min };
	}

	public float[] maxAE(VectorField groundTruth)
	{
		float max = Float.MIN_VALUE;
		int[] maxXY = new int[2];

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				float n1 = (float) Math.sqrt(u[y][x] * u[y][x] + v[y][x] * v[y][x]);
				float n2 = (float) Math.sqrt(groundTruth.u[y][x] * groundTruth.u[y][x] + groundTruth.v[y][x] * groundTruth.v[y][x]);

				float a1 = u[y][x] / n1;
				float a2 = v[y][x] / n1;
				float a3 = 1.0f / n1;

				float b1 = groundTruth.u[y][x] / n2;
				float b2 = groundTruth.v[y][x] / n2;
				float b3 = 1.0f / n2;

				// Algebraic definition of the dot product (A1 * B1 + A2 * B2 + A3 * B3)
				float dotProduct = a1 * b1 + a2 * b2 + a3 * b3;
				float dist = (float) Math.acos(dotProduct);

				if (dist > max)
				{
					max = dist;
					maxXY[0] = x;
					maxXY[1] = y;
				}
			}
		}

		return new float[] { maxXY[0], maxXY[1], max };
	}

	public float[] minAE(VectorField groundTruth)
	{
		float min = Float.MAX_VALUE;
		int[] minXY = new int[2];

		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				float n1 = (float) Math.sqrt(u[y][x] * u[y][x] + v[y][x] * v[y][x]);
				float n2 = (float) Math.sqrt(groundTruth.u[y][x] * groundTruth.u[y][x] + groundTruth.v[y][x] * groundTruth.v[y][x]);

				float a1 = u[y][x] / n1;
				float a2 = v[y][x] / n1;
				float a3 = 1.0f / n1;

				float b1 = groundTruth.u[y][x] / n2;
				float b2 = groundTruth.v[y][x] / n2;
				float b3 = 1.0f / n2;

				// Algebraic definition of the dot product (A1 * B1 + A2 * B2 + A3 * B3)
				float dotProduct = a1 * b1 + a2 * b2 + a3 * b3;
				float dist = (float) Math.acos(dotProduct);

				if (dist > min)
				{
					min = dist;
					minXY[0] = x;
					minXY[1] = y;
				}
			}
		}

		return new float[] { minXY[0], minXY[1], min };
	}

	public float[] maxVector()
	{
		int[] maxXY = new int[2];
		float max = Float.MIN_VALUE;

		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				float dist = (float) Math.sqrt(u[i][j] * u[i][j] + v[i][j] * v[i][j]);
				if (dist > max)
				{
					max = dist;
					maxXY[0] = j;
					maxXY[1] = i;
				}
			}
		}

		return new float[] { maxXY[0], maxXY[1], max };
	}

	public float[] minVector()
	{
		int[] minXY = new int[2];
		float min = Float.MAX_VALUE;

		for (int i = 0; i < height; i++)
		{
			for (int j = 0; j < width; j++)
			{
				float dist = (float) Math.sqrt(u[i][j] * u[i][j] + v[i][j] * v[i][j]);
				if (dist < min)
				{
					min = dist;
					minXY[0] = j;
					minXY[1] = i;
				}
			}
		}

		return new float[] { minXY[0], minXY[1], min };
	}

	public float averageVector()
	{
		return (Float) null;
	}
}
