package fr.gumtree.autotuning.deprecated;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.github.difflib.algorithm.DiffException;

import fr.gumtree.autotuning.experimentrunner.OfflineResultProcessor;
import fr.gumtree.autotuning.experimentrunner.OfflineResultProcessor.Difference;
import fr.gumtree.autotuning.gumtree.ParametersResolvers;
import fr.gumtree.autotuning.outils.ResultVisualizer;

public class NewVisTest {

	/**
	 * Case one of the paper
	 * 
	 * @throws IllegalAccessError
	 * @throws IOException
	 * @throws DiffException
	 * @throws Exception
	 */
	@Test
	public void testCase1a04d92() throws IllegalAccessError, IOException, DiffException, Exception {

		ResultVisualizer rv = new ResultVisualizer();

		String hash = "a04d92895d21ab99e1d79481b11f5c8cb6fe09c1";
		String fname = "Interpreter";

		String filename = "/Users/matias/develop/gt-tuning/data-cvs-vintage/git-log4j/" + hash + "/" + fname + "/"
				+ hash + "_" + fname;
		String diffId = hash + "/" + fname;

		String bestConfig = "HybridGumtree-bu_minsize-200-st_minprio-1-st_priocalc-size";// "ClassicGumtree-bu_minsim-0.2-bu_minsize-600-st_minprio-1-st_priocalc-size";
		// String defaultConfig =
		// "ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height";
		boolean isJDTModel = true;
		String pathOut = "./outVisTemp/";

		rv.computeDiff(filename, diffId, bestConfig, ParametersResolvers.defaultConfiguration, isJDTModel, pathOut);

	}

	/**
	 * Case one of the paper
	 * 
	 * @throws IllegalAccessError
	 * @throws IOException
	 * @throws DiffException
	 * @throws Exception
	 */
	@Test
	public void testCase3_6867bd1e() throws IllegalAccessError, IOException, DiffException, Exception {

		ResultVisualizer rv = new ResultVisualizer();

		// 6867bd1e49b49b7469b7a1d5de54af177a95fbfc/PanelWindowContainer/vanillaDiffView_default_ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height.html

		String hash = "6867bd1e49b49b7469b7a1d5de54af177a95fbfc";
		String fname = "PanelWindowContainer";

		String filename = "/Users/matias/develop/gt-tuning/data-cvs-vintage/git-jEdit/" + hash + "/" + fname + "/"
		// Note that we modified the file name, we minimized the diff
				+ "c2_" + hash + "_" + fname;
		String diffId = hash + "/" + fname;

		System.out.println("Source " + filename);
		// The best using size maps on the ST, so dont need the BU.
		// "HybridGumtree-bu_minsize-200-st_minprio-1-st_priocalc-size
		// But, if we force to fail the ST (using -st_priocalc-height), the hybrid match
		// in the simpleLastChanceMatch phase.
		// The classic, using
		// ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height,
		// does not arrive to match in ST and it does not enter to the last phase
		String bestConfig = "HybridGumtree-bu_minsize-200-st_minprio-1-st_priocalc-size";

		// "ClassicGumtree-bu_minsim-0.2-bu_minsize-600-st_minprio-1-st_priocalc-size";
		// String defaultConfig =
		// "ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height";
		boolean isJDTModel = true;
		String pathOut = "./outVisTemp/";

		rv.computeDiff(filename, diffId, bestConfig,
				"ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height"// ParametersResolvers.defaultConfiguration
				, isJDTModel, pathOut);

	}

	@Test
	public void testCase2_b06e0b() throws IllegalAccessError, IOException, DiffException, Exception {

		ResultVisualizer rv = new ResultVisualizer();

		// 6867bd1e49b49b7469b7a1d5de54af177a95fbfc/PanelWindowContainer/vanillaDiffView_default_ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height.html

		String hash = "b06e0b92da9ad1737600d996fc6f46e25cfd291b";
		String fname = "TagAttributeInfo";

		String filename = "/Users/matias/develop/gt-tuning/data-cvs-vintage/git-tomcat/" + hash + "/" + fname + "/"
				+ "c3_" + hash + "_" + fname;
		String diffId = hash + "/" + fname;

		System.out.println("Source " + filename);

		// The hibrid Optimize in the ricovery solves the updates, the move remains

		String bestConfig = "HybridGumtree-bu_minsize-200-st_minprio-1-st_priocalc-height";

		// "ClassicGumtree-bu_minsim-0.2-bu_minsize-600-st_minprio-1-st_priocalc-size";
		// String defaultConfig =
		// "ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height";
		boolean isJDTModel = true;
		String pathOut = "./outVisTemp/";

		rv.computeDiff(filename, diffId, bestConfig,
				// ParametersResolvers.defaultConfiguration,
				"ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height", isJDTModel, pathOut);

	}

	@Test
	public void testVis() throws IllegalAccessError, IOException, DiffException, Exception {

		ResultVisualizer rv = new ResultVisualizer();

		String filename = "/Users/matias/develop/gt-tuning/data-cvs-vintage/git-argouml/0bf2efa250ee33804ebd94fe84264178556a88e8/ClassdiagramEdge/0bf2efa250ee33804ebd94fe84264178556a88e8_ClassdiagramEdge";
		String diffId = "0bf2efa250ee33804ebd94fe84264178556a88e8/ClassdiagramEdge";
		String bestConfig = "ClassicGumtree-bu_minsim-0.2-bu_minsize-600-st_minprio-1-st_priocalc-size";
		// String defaultConfig =
		// "ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height";
		boolean isJDTModel = true;
		String pathOut = "./outVis/";

		rv.computeDiff(filename, diffId, bestConfig, ParametersResolvers.defaultConfiguration, isJDTModel, pathOut);

	}

	@Test
	public void testVis2() throws IllegalAccessError, IOException, DiffException, Exception {

		ResultVisualizer rv = new ResultVisualizer();

		String filename = "/Users/matias/develop/gt-tuning/data-cvs-vintage/git-argouml/0af75f67e87f6c882b697ab69047fbbba932b040/ActionAddExistingNode/0af75f67e87f6c882b697ab69047fbbba932b040_ActionAddExistingNode";
		String diffId = "0af75f67e87f6c882b697ab69047fbbba932b040/ActionAddExistingNode";
		String bestConfig = "ClassicGumtree-bu_minsim-0.2-bu_minsize-600-st_minprio-1-st_priocalc-size";
		// String defaultConfig =
		// "ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height";
		boolean isJDTModel = true;
		String pathOut = "./outVis/";

		rv.computeDiff(filename, diffId, bestConfig, ParametersResolvers.defaultConfiguration, isJDTModel, pathOut);

		// fb97efc69a6a861c7d88371781eb42cafa61139c_Ajp13

	}

	@Test
	public void testVisSpoon() throws IllegalAccessError, IOException, DiffException, Exception {

		ResultVisualizer rv = new ResultVisualizer();

		String hash = "fb97efc69a6a861c7d88371781eb42cafa61139c";
		String fname = "Ajp13";

		String filename = "/Users/matias/develop/gt-tuning/data-cvs-vintage/git-tomcat/" + hash + "/" + fname + "/"
				+ hash + "_" + fname;
		String diffId = hash + "/" + fname;

		String bestConfig = "ClassicGumtree-bu_minsim-0.3-bu_minsize-800-st_minprio-1-st_priocalc-size";

		boolean isJDTModel = false;
		String pathOut = "./outVis/";

		rv.computeDiff(filename, diffId, bestConfig, ParametersResolvers.defaultConfiguration, isJDTModel, pathOut);

		// fb97efc69a6a861c7d88371781eb42cafa61139c_Ajp13

	}

	@Test
	public void testVisComJDT() throws IllegalAccessError, IOException, DiffException, Exception {

		String results_path = "/Users/matias/develop/gt-tuning/results/resultsv4/";

		OfflineResultProcessor of = new OfflineResultProcessor();

		String defaultConfig = "ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height";
		String bestConfig = "HybridGumtree-bu_minsize-200-st_minprio-1-st_priocalc-size";
		String pathname = results_path + "/outDAT2_JDT_onlyresult/";
		boolean isJDTModel = true;
		executeComp(of, defaultConfig, bestConfig, pathname, isJDTModel);

	}

	@Test
	public void testVisComSpoon() throws IllegalAccessError, IOException, DiffException, Exception {

		String results_path = "/Users/matias/develop/gt-tuning/results/resultsv4/";

		OfflineResultProcessor of = new OfflineResultProcessor();

		// String defaultConfig =
		// "ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height";
		String bestConfig = "ClassicGumtree-bu_minsim-0.3-bu_minsize-800-st_minprio-1-st_priocalc-size";

		String pathname = results_path + "/outDAT2_SPOON_onlyresult/";

		boolean isJDTModel = false;
		executeComp(of, ParametersResolvers.defaultConfiguration, bestConfig, pathname, isJDTModel);

	}

	private void executeComp(OfflineResultProcessor of, String defaultConfig, String bestConfig, String pathname,
			boolean isJDTModel) throws IllegalAccessError, DiffException, Exception {

		ResultVisualizer rv = new ResultVisualizer();

		List<Difference> diffs = of.findDifferences(defaultConfig, bestConfig, new File(pathname));

		diffs.sort((e1, e2) -> Integer.compare(e2.getImprovementC2(), e1.getImprovementC2()));

		long countImprovementBest = diffs.stream().filter(e -> e.getImprovementC2() > 0).count();
		long countEquals = diffs.stream().filter(e -> e.getImprovementC2() == 0).count();
		long countWorse = diffs.stream().filter(e -> e.getImprovementC2() < 0).count();

		exportDiff(bestConfig, isJDTModel, rv, diffs);

		System.out.println(String.format("best %d equals %d worse %d ", countImprovementBest, countEquals, countWorse));
	}

	private void exportDiff(String bestConfig, boolean isJDTModel, ResultVisualizer rv, List<Difference> diffs)
			throws IOException, Exception, IllegalAccessError, DiffException {

		for (Difference difference : diffs) {
			System.out.println(difference);

			if (difference.getImprovementC2() == 0) {
				// System.out.println("Same lenght");
				continue;
			}

			File f = new File(difference.getId());

			String[] ps = f.getAbsoluteFile().getParentFile().getName().split("_");

			String project = f.getAbsoluteFile().getParentFile().getParentFile().getName();

			String hash = ps[0];
			String fname = ps[1];

			String root = "/Users/matias/develop/gt-tuning/data-cvs-vintage/";
			String filename = root + project + "/" + hash + "/" + fname + "/" + hash + "_" + fname;

			String diffId = hash + "/" + fname;

			String pathOut = "./outVis/" + ((isJDTModel) ? "JDT/" : "SPOON/") + "/d_" + difference.getImprovementC2();

			// System.out.println(filename);

			// rv.computeDiff(filename, diffId, bestConfig,
			// ParametersResolvers.defaultConfiguration, isJDTModel, pathOut);

		}
	}

}
