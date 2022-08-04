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
