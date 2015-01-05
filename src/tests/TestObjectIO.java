package tests;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import utility.Experiment;
import utility.Images;
import utility.ObjectIO;

public class TestObjectIO
{
	@Test
	public void testWriteAndRead()
	{
		Images save, load;
		ObjectIO<Images> io;
		
		io = new ObjectIO<Images>("iotestcheck");
		save = new Images(Experiment.TEST_PATH + "testImage.png", Experiment.TEST_PATH + "testImage.png");
		save.init();
		
		io.write(save, "testfile");
		
		File dir = new File("iotestcheck");
		assertTrue(dir.exists());
		
		File check = new File("iotestcheck/testfile");
		assertTrue(check.exists());
		
		load = io.read("testfile");
		assertEquals(load.height, save.height);
		assertEquals(load.width, save.width);
		assertEquals(load.frame1path, save.frame1path);
		assertEquals(load.frame2path, save.frame2path);
		
		for (int i = 0; i < load.fb1.length; i++)
		{
			for (int j = 0; j < load.fb1[0].length; j++)
			{
				assertEquals(load.fb1[i][j], save.fb1[i][j], 0);
				assertEquals(load.fb2[i][j], save.fb2[i][j], 0);
			}
		}
		
		check.delete();
		dir.delete();
	}
}
