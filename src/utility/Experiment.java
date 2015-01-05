package utility;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import hs_classic.HornSchunck;
import hs_pyrimidal.HornSchunckPyrimidal;

public class Experiment
{
	ObjectIO<Images>				io;
	ArrayList<Integer>				testAlphas;
	ArrayList<Images>				testSequences;
	public ArrayList<VectorField>	groundTruths;
	ArrayList<VectorField>			testOutputs;

	public static final String		SEQ_PATH		= "testSequences/";
	public static final String		TEST_PATH		= "junitTesting/";
	public static final String		EX_PATH			= "examples/";
	public static final String		OUTPUT_PATH		= "output/";
	public static final String		SAVE_PATH		= "savedObjects/";
	public static final String		TRUTH_PATH		= "groundTruth/";
	public static final String[]	SEQUENCE_NAMES	= new String[] { "Dimetrodon", "Grove2", "Grove3", "Hydrangea", "RubberWhale", "Urban2",
			"Urban3", "Venus"						};

	public static final String[]	OP_COLUMNS		= new String[] { "alpha", "EPE", "AAE" };
	public static final String[]	EXP_COLUMNS		= new String[] { "alpha", "EPE", "AAE" };

	public Experiment()
	{
		testSequences = new ArrayList<Images>();
		testOutputs = new ArrayList<VectorField>();
		groundTruths = new ArrayList<VectorField>();
		io = new ObjectIO<Images>(Experiment.SAVE_PATH);
	}

	public static void main(String[] args)
	{
		Experiment exp = new Experiment();
		exp.init();

		// exp.runAllOPPilots();

		Images testy = new Images(Experiment.SEQ_PATH.concat("Venus/frame10.png"), Experiment.SEQ_PATH.concat("Venus/frame11.png"));
		testy.init();

		HornSchunckPyrimidal hsp = new HornSchunckPyrimidal();
		hsp.setParams(8, 5, 0.0001f, 150, 10, 0.65f);
		VectorField james = hsp.run(testy.fb1, testy.fb2);

		james.drawToImageColorModel("OUTPUT");

		//System.out.println("EPE: " + james.endPointError(exp.groundTruths.get(2)));
		//System.out.println("AAE: " + james.angularError(exp.groundTruths.get(2)));
			
		//VectorField phsGrove3 = VectorField.readFromFlo("grove3out.flo");
		
		exp.groundTruths.get(7).drawToImageColorModel("GROUNDTRUTH");

		//System.out.println("Author EPE: " + phsGrove3.endPointError(exp.groundTruths.get(2)));
		//System.out.println("Author AAE: " + phsGrove3.angularError(exp.groundTruths.get(2)));
		
		//exp.runDataTrend();

		// exp.runEXP1();

		// exp.runEXP2();
	}

	/**
	 * Write a set of values to a given CSV file.
	 * 
	 * @param values
	 * @param filename
	 */
	public void resultsToCSV(String columnNames[], float[][] values, String filename)
	{
		try
		{
			PrintWriter out = new PrintWriter(new FileWriter(new File(filename + ".csv")));

			for (String s : columnNames)
			{
				out.print(s + ", ");
			}
			out.println();

			for (float[] vals : values)
			{
				for (float v : vals)
				{
					out.print(v + ", ");
				}
				out.println();
			}

			out.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Run the OP pilots for every initialized test sequence.
	 */
	public float[][][] runAllOPPilots()
	{
		float[][][] results = new float[testSequences.size()][8][3];

		for (int i = 0; i < 8; i++)
		{
			results[i] = runOPPilot(testSequences.get(i), groundTruths.get(i));
			resultsToCSV(OP_COLUMNS, results[i], "output/pilots/" + Experiment.SEQUENCE_NAMES[i] + "_opPilot");
		}

		return results;
	}

	/**
	 * Run a single OP pilot for a single given test sequence. Run each test sequence with alpha values incrementing by
	 * 5. The optimal alpha value to test will be selected from the best output.
	 */
	public float[][] runOPPilot(Images seq, VectorField gt)
	{
		HornSchunckPyrimidal hsp = new HornSchunckPyrimidal();
		float[][] results = new float[9][3];

		// alpha 5 -> 45, incrementing by 5
		for (int i = 1; i <= 9; i++)
		{
			int nextAlpha = i * 5;
			hsp.setParams(nextAlpha, 5, 0.0001f, 150, 10, 0.65f);
			VectorField OPout = hsp.run(seq.fb1, seq.fb2);
			results[i - 1][0] = nextAlpha;
			results[i - 1][1] = OPout.endPointError(gt);
			results[i - 1][2] = OPout.angularError(gt);
		}

		return results;
	}

	public void runEXP1()
	{
		// OP pilot = 15
		float[][] result = runEXP1testSequence(testSequences.get(0), groundTruths.get(0), 6, 24);
		resultsToCSV(EXP_COLUMNS, result, "output/experiment1/" + Experiment.SEQUENCE_NAMES[0] + "_exp1");

		result = runEXP1testSequence(testSequences.get(1), groundTruths.get(1), 6, 24);
		resultsToCSV(EXP_COLUMNS, result, "output/experiment1/" + Experiment.SEQUENCE_NAMES[1] + "_exp1");

		result = runEXP1testSequence(testSequences.get(2), groundTruths.get(2), 6, 24);
		resultsToCSV(EXP_COLUMNS, result, "output/experiment1/" + Experiment.SEQUENCE_NAMES[2] + "_exp1");

		result = runEXP1testSequence(testSequences.get(3), groundTruths.get(3), 6, 24);
		resultsToCSV(EXP_COLUMNS, result, "output/experiment1/" + Experiment.SEQUENCE_NAMES[3] + "_exp1");

		result = runEXP1testSequence(testSequences.get(5), groundTruths.get(5), 6, 24);
		resultsToCSV(EXP_COLUMNS, result, "output/experiment1/" + Experiment.SEQUENCE_NAMES[5] + "_exp1");

		// OP pilot = 10
		result = runEXP1testSequence(testSequences.get(4), groundTruths.get(4), 1, 19);
		resultsToCSV(EXP_COLUMNS, result, "output/experiment1/" + Experiment.SEQUENCE_NAMES[4] + "_exp1");

		result = runEXP1testSequence(testSequences.get(6), groundTruths.get(6), 1, 19);
		resultsToCSV(EXP_COLUMNS, result, "output/experiment1/" + Experiment.SEQUENCE_NAMES[6] + "_exp1");

		result = runEXP1testSequence(testSequences.get(7), groundTruths.get(7), 1, 19);
		resultsToCSV(EXP_COLUMNS, result, "output/experiment1/" + Experiment.SEQUENCE_NAMES[7] + "_exp1");
	}

	private float[][] runEXP1testSequence(Images seq, VectorField gt, int low, int high)
	{
		HornSchunckPyrimidal hsp = new HornSchunckPyrimidal();
		float[][] results = new float[high - low + 1][3];

		for (int i = low; i <= high; i++)
		{
			hsp.setParams(i, 5, 0.0001f, 150, 10, 0.65f);

			VectorField EXPout = hsp.run(testSequences.get(0).fb1, testSequences.get(0).fb2);
			EXPout = hsp.run(seq.fb1, seq.fb2);
			results[i - low][0] = i;
			results[i - low][1] = EXPout.endPointError(gt);
			results[i - low][2] = EXPout.angularError(gt);
		}

		return results;
	}

	// Checking the HornSchunck error values for each test sequence
	public void runEXP2()
	{
		HornSchunck hs = new HornSchunck(15, 150);
		float[][] output = new float[8][3];

		for (int i = 0; i < 8; i++)
		{
			Images imgs = testSequences.get(i);
			float[][] u = new float[imgs.height][imgs.width];
			float[][] v = new float[imgs.height][imgs.width];
			hs.run(u, v, imgs.fb1, imgs.fb2);
			VectorField result = new VectorField(u, v);
			output[i][0] = 15;
			output[i][1] = result.endPointError(groundTruths.get(i));
			output[i][2] = result.angularError(groundTruths.get(i));
		}

		resultsToCSV(EXP_COLUMNS, output, "output/experiment2/run1");
	}
	
	public void runDataTrend()
	{
		HornSchunckPyrimidal hsp = new HornSchunckPyrimidal();
		float[][] results = new float[9][3];
		
		VectorField[] phs = new VectorField[9];
		phs[0] = VectorField.readFromFlo("phs_flo files/grove3out11.flo");
		phs[1] = VectorField.readFromFlo("phs_flo files/grove3out12.flo");
		phs[2] = VectorField.readFromFlo("phs_flo files/grove3out13.flo");
		phs[3] = VectorField.readFromFlo("phs_flo files/grove3out14.flo");
		phs[4] = VectorField.readFromFlo("phs_flo files/grove3out15.flo");
		phs[5] = VectorField.readFromFlo("phs_flo files/grove3out16.flo");
		phs[6] = VectorField.readFromFlo("phs_flo files/grove3out17.flo");
		phs[7] = VectorField.readFromFlo("phs_flo files/grove3out18.flo");
		phs[8] = VectorField.readFromFlo("phs_flo files/grove3out19.flo");
		
		for (int i = 0; i < phs.length; i++)
		{
			results[i][0] = i + 11;
			results[i][1] = phs[i].endPointError(groundTruths.get(2));
			results[i][2] = phs[i].angularError(groundTruths.get(2));
		}
		
		resultsToCSV(OP_COLUMNS, results, "dataTrend");
	}

	public void runHSC()
	{
		HornSchunck hs = new HornSchunck(500, 200);
		Images test = new Images("examples/testDiagMv1.png", "examples/testDiagMv2.png");
		test.init();

		float[][] u = new float[test.height][test.width];
		float[][] v = new float[test.height][test.width];

		hs.run(u, v, test.fb1, test.fb2);
		VectorField out = new VectorField(u, v);
		out.drawToImageColorModel("testColor");
	}

	/**
	 * Initialize the test data.
	 */
	public void init()
	{
		testSequences.add(new Images(Experiment.SEQ_PATH.concat("Dimetrodon/frame10.png"), Experiment.SEQ_PATH.concat("Dimetrodon/frame11.png")));
		testSequences.add(new Images(Experiment.SEQ_PATH.concat("Grove2/frame10.png"), Experiment.SEQ_PATH.concat("Grove2/frame11.png")));
		testSequences.add(new Images(Experiment.SEQ_PATH.concat("Grove3/frame10.png"), Experiment.SEQ_PATH.concat("Grove3/frame11.png")));
		testSequences.add(new Images(Experiment.SEQ_PATH.concat("Hydrangea/frame10.png"), Experiment.SEQ_PATH.concat("Hydrangea/frame11.png")));
		testSequences.add(new Images(Experiment.SEQ_PATH.concat("RubberWhale/frame10.png"), Experiment.SEQ_PATH.concat("RubberWhale/frame11.png")));
		testSequences.add(new Images(Experiment.SEQ_PATH.concat("Urban2/frame10.png"), Experiment.SEQ_PATH.concat("Urban2/frame11.png")));
		testSequences.add(new Images(Experiment.SEQ_PATH.concat("Urban3/frame10.png"), Experiment.SEQ_PATH.concat("Urban3/frame11.png")));
		testSequences.add(new Images(Experiment.SEQ_PATH.concat("Venus/frame10.png"), Experiment.SEQ_PATH.concat("Venus/frame11.png")));

		// initialize the images
		for (Images i : testSequences)
		{
			i.init();
		}

		groundTruths.add(VectorField.readFromFlo(Experiment.TRUTH_PATH.concat("Dimetrodon/flow10.flo")));
		groundTruths.add(VectorField.readFromFlo(Experiment.TRUTH_PATH.concat("Grove2/flow10.flo")));
		groundTruths.add(VectorField.readFromFlo(Experiment.TRUTH_PATH.concat("Grove3/flow10.flo")));
		groundTruths.add(VectorField.readFromFlo(Experiment.TRUTH_PATH.concat("Hydrangea/flow10.flo")));
		groundTruths.add(VectorField.readFromFlo(Experiment.TRUTH_PATH.concat("RubberWhale/flow10.flo")));
		groundTruths.add(VectorField.readFromFlo(Experiment.TRUTH_PATH.concat("Urban2/flow10.flo")));
		groundTruths.add(VectorField.readFromFlo(Experiment.TRUTH_PATH.concat("Urban3/flow10.flo")));
		groundTruths.add(VectorField.readFromFlo(Experiment.TRUTH_PATH.concat("Venus/flow10.flo")));
	}
}
