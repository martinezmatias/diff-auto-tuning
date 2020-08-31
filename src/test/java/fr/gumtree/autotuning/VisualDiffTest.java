package fr.gumtree.autotuning;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumTreeProperties;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;

public class VisualDiffTest {

	@Test
	public void testDiffForTestJDT() throws IOException {
		/**
		 * ./gumtree webdiff
		 * /Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded/1/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_s.java
		 * /Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded/1/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_t.java
		 * --port 4568
		 */
		File rootMegadiff = new File(
				"/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded");
		assertTrue(rootMegadiff.exists());
		String diffId = "1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService";
		String fl = rootMegadiff.getAbsolutePath()
				+ "/1/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/1/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_t.java";
		System.out.println(fl);

		File fileLeftt = new File(fl);
		String lc = new String(Files.readAllBytes(fileLeftt.toPath()));
		ITree tl = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

		File fileRight = new File(fr);
		lc = new String(Files.readAllBytes(fileRight.toPath()));
		ITree tr = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

		GumTreeProperties properies = new GumTreeProperties();

		properies.getProperties().clear();
		properies.put(ConfigurationOptions.GT_BUM_SMT, 0.7);
		properies.put(ConfigurationOptions.GT_BUM_SZT, 1900);
		properies.put(ConfigurationOptions.GT_STM_MH, 2);

		Matcher matcher = new CompositeMatchers.ClassicGumtree();
		File fout = new File("/Users/matias/Downloads/");
		String name = "default";
		ResultVisualizer rv = new ResultVisualizer();
		rv.saveVisualization(fileLeftt, tl, fileRight, tr, properies, matcher, fout, diffId, name);
		System.out.println("End");

	}

	@Test
	public void testDiffForTestJDT2() throws Exception {

		String row = "nr_563_id_1_0649f1cc087cefe312edb4dc21dc7e01ebb2b401_Compile_JDT.csv,ClassicGumtree_0.1_1500_1,8,ClassicGumtree_0.5_1000_2,102,94,ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1500@GT_STM_MH@1,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@2";
		// "nr_796_id_3_054edb17739fc164d8f56aef4dbb7ff9eaf9e88c_AvailableServiceBindingController_JDT.csv,ClassicGumtree_0.1_1400_1,8,ClassicGumtree_0.5_1000_2,94,86,ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1400@GT_STM_MH@1,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@2";
		String pathMegadiff = "/Users/matias/develop/newAstorexecution/megadiff-expansion-temp/";
		String pathOut = "/Users/matias/Downloads/";
		ResultVisualizer rv = new ResultVisualizer();
		rv.saveVisualizationRow(row, pathMegadiff, pathOut, true);

	}

	@Test
	public void testAllJDT() throws Exception {
		String pathMegadiff = "/Users/matias/develop/newAstorexecution/megadiff-expansion-temp/";
		String pathOut = "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/manual_analysis/casesJDT/";
		String pathResults = "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/manual_analysis/revisionstoanalyze_merge_gtJDT_7_CDJDT_6_18.csv";
		boolean isJDT = true;
		read(pathResults, pathMegadiff, pathOut, isJDT);

	}

	@Test
	public void testAllSpoon() throws Exception {
		String pathMegadiff = "/Users/matias/develop/newAstorexecution/megadiff-expansion-temp/";
		String pathOut = "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/manual_analysis/casesSpoon/";
		String pathResults = "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/manual_analysis/revisionstoanalyze_merge_gt_8_cd_7_18.csv";
		boolean isJDT = false;//
		read(pathResults, pathMegadiff, pathOut, isJDT);

	}

	public void read(String pathResults, String pathMegadiff, String pathOut, boolean isJDTmodel) throws Exception {
		BufferedReader reader;
		ResultVisualizer rv = new ResultVisualizer();

		try {
			reader = new BufferedReader(new FileReader(pathResults));
			String line = reader.readLine();
			System.out.println("Ignore the first one: " + line);
			line = reader.readLine();

			// we store the hash of the lines and the distance
			Map<String, Integer> linesAndDifferences = new HashMap<>();

			while (line != null) {
				// read next line

				if (line.trim().isEmpty())
					continue;

				// get the distance
				String distance = line.split(",")[5].replace(".0", "");

				int nrchangesBest = new Integer(line.split(",")[2].replace(".0", ""));

				if (nrchangesBest < 25) {

					linesAndDifferences.put(line, new Integer(distance));

				}
				line = reader.readLine();
			}
			reader.close();

			List<String> lines = new ArrayList(linesAndDifferences.keySet());

			Collections.sort(lines, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {

					return Integer.compare(linesAndDifferences.get(o2), linesAndDifferences.get(o1));
				}

			});

			System.out.println("total configs " + lines.size());

			int i = 0;
			int limit = 50;
			List<String> savedlines = new ArrayList();

			for (String iline : lines) {

				System.out.println(iline + ", " + linesAndDifferences.get(iline));

				System.out.println(i + "/" + limit);
				boolean saved = rv.saveVisualizationRow(iline, pathMegadiff, pathOut, isJDTmodel);
				if (saved)
					savedlines.add(iline);

				i++;

				if (i > limit)
					break;

			}
			System.out.println("\nResults: ");
			// for (int j = 0; j < limit; j++) {
			for (String iLines : savedlines) {
				System.out.println(iLines + ", " + linesAndDifferences.get(iLines));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testDiffForTestSpoonFromDataEquals() throws Exception {
		String diffid = "nr_108_id_39_adb8bb2c8109c50d183a28e607165075fc137881_pchestPlayerListener_GTSPOON";
		// "nr_48_id_40_8b1e497d0b847e5dcdd555155288ddc880487458_LanguageChooser_GTSPOON";
		runDiffs(diffid);

	}

	@Test
	public void testDiffForTestSpoonFromDataBestBetter() throws Exception {
		String diffid = "nr_515_id_16_0923d69a8ca45222cabd82a80eaa88da9f132e9a_BrowserActivity_GTSPOON";
		// "nr_48_id_40_8b1e497d0b847e5dcdd555155288ddc880487458_LanguageChooser_GTSPOON";
		runDiffs(diffid);

	}

	@Test
	public void testDiffForTestSpoonFromDataBestBetter2() throws Exception {
		String diffid = "nr_981_id_16_1105f66802c4bcaef10ce4463bcc0868211d7639_AbstractPollingIoProcessor_GTSPOON";

		runDiffs(diffid);

	}

	public void runDiffs(String diffid) throws Exception {
		String row = diffid + ".csv,ClassicGumtree_0.1_2000_1,8,ClassicGumtree_0.5_1000_1,102,94,"
				+ "ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1500@GT_STM_MH@1,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@1";
		// "nr_796_id_3_054edb17739fc164d8f56aef4dbb7ff9eaf9e88c_AvailableServiceBindingController_JDT.csv,ClassicGumtree_0.1_1400_1,8,ClassicGumtree_0.5_1000_2,94,86,ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1400@GT_STM_MH@1,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@2";
		String pathMegadiff = "/Users/matias/develop/newAstorexecution/megadiff-expansion-temp/";
		String pathOut = "/Users/matias/Downloads/results/visualizations/";
		ResultVisualizer rv = new ResultVisualizer();
		boolean isJDT = diffid.contains("JDT");
		rv.saveVisualizationRow(row, pathMegadiff, pathOut, isJDT);
	}
	// failing visualization
	// casesSpoon/28_3cd5c04e548142ef3dc562194a469ed1dc7c4ae2_CalendarApplication/vanillaDiffView_default_ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@1.html

	@Test
	public void testDiffForTestSpoonFromDatVisualization() throws Exception {
		String diffid = "nr_000_id_28_3cd5c04e548142ef3dc562194a469ed1dc7c4ae2_CalendarApplication_GTSPOON";

		runDiffs(diffid);

	}

	// https://uphf.github.io/dat/evaluation/casesSpoon/26_07dab009ba690ebba019fbfaa1be784187b3d568_TechEditWizardData/vanillaDiffView_best_SimpleGumtree@GT_BUM_SMT_SBUP@0.1@GT_STM_MH@1.html
	//
	@Test
	public void testDiffForTestSpoonFromDatVisualization2() throws Exception {
		String diffid = "nr_000_id_28_3cd5c04e548142ef3dc562194a469ed1dc7c4ae2_CalendarApplication_GTSPOON";

		runDiffs(diffid);

	}

	// nr_795_id_1_091b6095bc00185aa972302f5990cdef03daf13c_ListViewer_GTSPOON.cs
	@Test
	public void testDiffForTestSpoonFromDatVisualizationNoChangesGDT() throws Exception {
		String diffid = "nr_795_id_1_091b6095bc00185aa972302f5990cdef03daf13c_ListViewer_JDT";

		runDiffs(diffid);

	}

	@Test
	public void testAutoTune1() throws Exception {
		// beating cases (8):RA
		// [('nr_1963_id_1_15f9c41758202a81c30c83b0e68453133bfd01a7_DjangoRegressionTests_JDT.csv',
		// 'ClassicGumtree_0.1_1300_2'),
		// ('nr_421_id_1_04de552e4f4b49e9a497c4ac6c4583487754aa95_MagPiApplication_JDT.csv',
		// 'ClassicGumtree_0.1_100_5'),
		// ('nr_2436_id_1_1bb89a0c23a9fae922da897a2ae225998c7c0a64_PhoneWindowManager_JDT.csv',
		// 'ClassicGumtree_0.1_100_2'),
		// ('nr_1063_id_1_0c269563127dd8d4b92d05cb89b160f47162785e_ErlangFormattingModelBuilder_JDT.csv',
		// 'ClassicGumtree_1_600_3'),
		// ('nr_1354_id_1_0f659d2023e82d76ced94c8834b6ece056043956_FXDLexer_JDT.csv',
		// 'ClassicGumtree_0.1_1600_2'),
		// ('nr_2727_id_1_1ecb49c737338d930396781bd1aa7c2581c8302f_NetworkClient_JDT.csv',
		// 'ClassicGumtree_0.9_1200_2'),
		// ('nr_1060_id_1_0c252e3a77274dc2f5daa4a7dd967ad38c4dda43_ActiveLayerTreeCellRenderer_JDT.csv',
		// 'ClassicGumtree_0.8_700_2'),
		// ('nr_2799_id_1_1f9074caec848a331dd43ed2c26b2eb760c2c0f7_Options_JDT.csv',
		// 'ClassicGumtree_0.9_1000_2')]

		String diffid = "nr_1963_id_1_15f9c41758202a81c30c83b0e68453133bfd01a7_DjangoRegressionTests_JDT.csv";
		System.out.println(diffid + "best auto vs default ");
		String row = diffid + ".csv,ClassicGumtree_0.1_1300_2,8,ClassicGumtree_0.5_1000_1,102,94,"
				+ "ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1300@GT_STM_MH@2,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@1";
		// "nr_796_id_3_054edb17739fc164d8f56aef4dbb7ff9eaf9e88c_AvailableServiceBindingController_JDT.csv,ClassicGumtree_0.1_1400_1,8,ClassicGumtree_0.5_1000_2,94,86,ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1400@GT_STM_MH@1,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@2";
		String pathMegadiff = "/Users/matias/develop/newAstorexecution/megadiff-expansion-temp/";
		String pathOut = "/Users/matias/Downloads/results/visualizations/";
		ResultVisualizer rv = new ResultVisualizer();
		boolean isJDT = diffid.contains("JDT");
		rv.saveVisualizationRow(row, pathMegadiff, pathOut, isJDT);

		System.out.println("***********");
		System.out.println(diffid + "best auto vs BestHyperOptimization ");
		row = diffid + ".csv,ClassicGumtree_0.1_1300_2,8,SimpleGumtree_0.1_1,102,94,"
				+ "ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1300@GT_STM_MH@2,SimpleGumtree@GT_BUM_SMT_SBUP@0.1@GT_STM_MH@1";
		// "nr_796_id_3_054edb17739fc164d8f56aef4dbb7ff9eaf9e88c_AvailableServiceBindingController_JDT.csv,ClassicGumtree_0.1_1400_1,8,ClassicGumtree_0.5_1000_2,94,86,ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1400@GT_STM_MH@1,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@2";
		pathMegadiff = "/Users/matias/develop/newAstorexecution/megadiff-expansion-temp/";
		pathOut = "/Users/matias/Downloads/results/visualizations/";
		rv = new ResultVisualizer();
		isJDT = diffid.contains("JDT");
		rv.saveVisualizationRow(row, pathMegadiff, pathOut, isJDT);
	}

	@Test
	public void testAutoTune2() throws Exception {
		// beating cases (8):
		// [('nr_1963_id_1_15f9c41758202a81c30c83b0e68453133bfd01a7_DjangoRegressionTests_JDT.csv',
		// 'ClassicGumtree_0.1_1300_2'),
		// ('nr_421_id_1_04de552e4f4b49e9a497c4ac6c4583487754aa95_MagPiApplication_JDT.csv',
		// 'ClassicGumtree_0.1_100_5'),
		// ('nr_2436_id_1_1bb89a0c23a9fae922da897a2ae225998c7c0a64_PhoneWindowManager_JDT.csv',
		// 'ClassicGumtree_0.1_100_2'),
		// ('nr_1063_id_1_0c269563127dd8d4b92d05cb89b160f47162785e_ErlangFormattingModelBuilder_JDT.csv',
		// 'ClassicGumtree_1_600_3'),
		// ('nr_1354_id_1_0f659d2023e82d76ced94c8834b6ece056043956_FXDLexer_JDT.csv',
		// 'ClassicGumtree_0.1_1600_2'),
		// ('nr_2727_id_1_1ecb49c737338d930396781bd1aa7c2581c8302f_NetworkClient_JDT.csv',
		// 'ClassicGumtree_0.9_1200_2'),
		// ('nr_1060_id_1_0c252e3a77274dc2f5daa4a7dd967ad38c4dda43_ActiveLayerTreeCellRenderer_JDT.csv',
		// 'ClassicGumtree_0.8_700_2'),
		// ('nr_2799_id_1_1f9074caec848a331dd43ed2c26b2eb760c2c0f7_Options_JDT.csv',
		// 'ClassicGumtree_0.9_1000_2')]

		String diffid = "nr_421_id_1_04de552e4f4b49e9a497c4ac6c4583487754aa95_MagPiApplication_JDT.csv";
		System.out.println(diffid + "best auto vs default ");
		String row = diffid + ".csv,ClassicGumtree_0.1_100_4,8,ClassicGumtree_0.5_1000_1,102,94,"
				+ "ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@100@GT_STM_MH@4,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@1";
		// "nr_796_id_3_054edb17739fc164d8f56aef4dbb7ff9eaf9e88c_AvailableServiceBindingController_JDT.csv,ClassicGumtree_0.1_1400_1,8,ClassicGumtree_0.5_1000_2,94,86,ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1400@GT_STM_MH@1,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@2";
		String pathMegadiff = "/Users/matias/develop/newAstorexecution/megadiff-expansion-temp/";
		String pathOut = "/Users/matias/Downloads/results/visualizations/";
		ResultVisualizer rv = new ResultVisualizer();
		boolean isJDT = diffid.contains("JDT");
		rv.saveVisualizationRow(row, pathMegadiff, pathOut, isJDT);

		System.out.println("***********");
		System.out.println(diffid + "best auto vs BestHyperOptimization ");
		row = diffid + ".csv,ClassicGumtree_0.1_100_4,8,SimpleGumtree_0.1_1,102,94,"
				+ "ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@100@GT_STM_MH@4,SimpleGumtree@GT_BUM_SMT_SBUP@0.1@GT_STM_MH@1";
		// "nr_796_id_3_054edb17739fc164d8f56aef4dbb7ff9eaf9e88c_AvailableServiceBindingController_JDT.csv,ClassicGumtree_0.1_1400_1,8,ClassicGumtree_0.5_1000_2,94,86,ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1400@GT_STM_MH@1,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@2";
		pathMegadiff = "/Users/matias/develop/newAstorexecution/megadiff-expansion-temp/";
		pathOut = "/Users/matias/Downloads/results/visualizations/";
		rv = new ResultVisualizer();
		isJDT = diffid.contains("JDT");
		rv.saveVisualizationRow(row, pathMegadiff, pathOut, isJDT);
	}

	// 17\_10964b90244f8e1047fb0881d4975805c507cd54
	@Test
	public void testAutoTune3() throws Exception {
		// beating cases (8):
		// [('nr_1963_id_1_15f9c41758202a81c30c83b0e68453133bfd01a7_DjangoRegressionTests_JDT.csv',
		// 'ClassicGumtree_0.1_1300_2'),
		// ('nr_421_id_1_04de552e4f4b49e9a497c4ac6c4583487754aa95_MagPiApplication_JDT.csv',
		// 'ClassicGumtree_0.1_100_5'),
		// ('nr_2436_id_1_1bb89a0c23a9fae922da897a2ae225998c7c0a64_PhoneWindowManager_JDT.csv',
		// 'ClassicGumtree_0.1_100_2'),
		// ('nr_1063_id_1_0c269563127dd8d4b92d05cb89b160f47162785e_ErlangFormattingModelBuilder_JDT.csv',
		// 'ClassicGumtree_1_600_3'),
		// ('nr_1354_id_1_0f659d2023e82d76ced94c8834b6ece056043956_FXDLexer_JDT.csv',
		// 'ClassicGumtree_0.1_1600_2'),
		// ('nr_2727_id_1_1ecb49c737338d930396781bd1aa7c2581c8302f_NetworkClient_JDT.csv',
		// 'ClassicGumtree_0.9_1200_2'),
		// ('nr_1060_id_1_0c252e3a77274dc2f5daa4a7dd967ad38c4dda43_ActiveLayerTreeCellRenderer_JDT.csv',
		// 'ClassicGumtree_0.8_700_2'),
		// ('nr_2799_id_1_1f9074caec848a331dd43ed2c26b2eb760c2c0f7_Options_JDT.csv',
		// 'ClassicGumtree_0.9_1000_2')]

		String diffid = "nr_1063_id_1_0c269563127dd8d4b92d05cb89b160f47162785e_ErlangFormattingModelBuilder_JDT.csv";
		System.out.println(diffid + "best auto vs default ");
		String row = diffid + ".csv,ClassicGumtree_1_600_3,8,ClassicGumtree_0.5_1000_1,102,94,"
				+ "ClassicGumtree@GT_BUM_SMT@1@GT_BUM_SZT@600@GT_STM_MH@3,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@1";
		// "nr_796_id_3_054edb17739fc164d8f56aef4dbb7ff9eaf9e88c_AvailableServiceBindingController_JDT.csv,ClassicGumtree_0.1_1400_1,8,ClassicGumtree_0.5_1000_2,94,86,ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1400@GT_STM_MH@1,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@2";
		String pathMegadiff = "/Users/matias/develop/newAstorexecution/megadiff-expansion-temp/";
		String pathOut = "/Users/matias/Downloads/results/visualizations/";
		ResultVisualizer rv = new ResultVisualizer();
		boolean isJDT = diffid.contains("JDT");
		rv.saveVisualizationRow(row, pathMegadiff, pathOut, isJDT);

		System.out.println("***********");
		System.out.println(diffid + "best auto vs BestHyperOptimization ");
		row = diffid + ".csv,ClassicGumtree_1_600_3,8,SimpleGumtree_0.1_1,102,94,"
				+ "ClassicGumtree@GT_BUM_SMT@1@GT_BUM_SZT@600@GT_STM_MH@3,SimpleGumtree@GT_BUM_SMT_SBUP@0.1@GT_STM_MH@1";
		// "nr_796_id_3_054edb17739fc164d8f56aef4dbb7ff9eaf9e88c_AvailableServiceBindingController_JDT.csv,ClassicGumtree_0.1_1400_1,8,ClassicGumtree_0.5_1000_2,94,86,ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1400@GT_STM_MH@1,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@2";
		pathMegadiff = "/Users/matias/develop/newAstorexecution/megadiff-expansion-temp/";
		pathOut = "/Users/matias/Downloads/results/visualizations/";
		rv = new ResultVisualizer();
		isJDT = diffid.contains("JDT");
		rv.saveVisualizationRow(row, pathMegadiff, pathOut, isJDT);
	}
	// 'nr_2436_id_1_1bb89a0c23a9fae922da897a2ae225998c7c0a64_PhoneWindowManager_JDT.csv',
	// 'ClassicGumtree_0.1_100_2', True

	@Test
	public void testAutoTune4TrueBeatsall() throws Exception {
		// beating cases (11): [{'diff':
		// 'nr_1963_id_1_15f9c41758202a81c30c83b0e68453133bfd01a7_DjangoRegressionTests_JDT.csv',
		// 'minConfigFoundAutotune': 'ClassicGumtree_0.1_1300_1',
		// 'isBestSearchSmallerThanDefault': True, 'minSizeFound': 1.0, 'sizeDefault':
		// 130.0, 'sizeBest': 124.0}, {'diff':
		// 'nr_421_id_1_04de552e4f4b49e9a497c4ac6c4583487754aa95_MagPiApplication_JDT.csv',
		// 'minConfigFoundAutotune': 'ClassicGumtree_0.1_100_4',
		// 'isBestSearchSmallerThanDefault': False, 'minSizeFound': 4.0, 'sizeDefault':
		// 11.0, 'sizeBest': 11.0}, {'diff':
		// 'nr_2436_id_1_1bb89a0c23a9fae922da897a2ae225998c7c0a64_PhoneWindowManager_JDT.csv',
		// 'minConfigFoundAutotune': 'ClassicGumtree_0.1_100_1',
		// 'isBestSearchSmallerThanDefault': True, 'minSizeFound': 2327.0,
		// 'sizeDefault': 2375.0, 'sizeBest': 2359.0}, {'diff':
		// 'nr_1063_id_1_0c269563127dd8d4b92d05cb89b160f47162785e_ErlangFormattingModelBuilder_JDT.csv',
		// 'minConfigFoundAutotune': 'ClassicGumtree_1_600_2',
		// 'isBestSearchSmallerThanDefault': False, 'minSizeFound': 11.0, 'sizeDefault':
		// 16.0, 'sizeBest': 24.0}, {'diff':
		// 'nr_1354_id_1_0f659d2023e82d76ced94c8834b6ece056043956_FXDLexer_JDT.csv',
		// 'minConfigFoundAutotune': 'ClassicGumtree_0.1_1600_1',
		// 'isBestSearchSmallerThanDefault': True, 'minSizeFound': 5.0, 'sizeDefault':
		// 21.0, 'sizeBest': 15.0}, {'diff':
		// 'nr_2727_id_1_1ecb49c737338d930396781bd1aa7c2581c8302f_NetworkClient_JDT.csv',
		// 'minConfigFoundAutotune': 'ClassicGumtree_0.9_1200_1',
		// 'isBestSearchSmallerThanDefault': True, 'minSizeFound': 6.0, 'sizeDefault':
		// 13.0, 'sizeBest': 7.0}, {'diff':
		// 'nr_1060_id_1_0c252e3a77274dc2f5daa4a7dd967ad38c4dda43_ActiveLayerTreeCellRenderer_JDT.csv',
		// 'minConfigFoundAutotune': 'ClassicGumtree_0.8_700_1',
		// 'isBestSearchSmallerThanDefault': True, 'minSizeFound': 13.0, 'sizeDefault':
		// 27.0, 'sizeBest': 21.0}, {'diff':
		// 'nr_2799_id_1_1f9074caec848a331dd43ed2c26b2eb760c2c0f7_Options_JDT.csv',
		// 'minConfigFoundAutotune': 'ClassicGumtree_0.9_1000_1',
		// 'isBestSearchSmallerThanDefault': False, 'minSizeFound': 6.0, 'sizeDefault':
		// 10.0, 'sizeBest': 10.0}, {'diff':
		// 'nr_2661_id_1_1e07e10a361ffe8e8b2227010f068ae25a1d8475_HTMLGameBrowser_JDT.csv',
		// 'minConfigFoundAutotune': 'ClassicGumtree_0.1_1600_1',
		// 'isBestSearchSmallerThanDefault': False, 'minSizeFound': 1.0, 'sizeDefault':
		// 7.0, 'sizeBest': 33.0}, {'diff':
		// 'nr_888_id_1_0a1d12636bb02888904b18aaf07b06596fb62b46_InsertionSort_JDT.csv',
		// 'minConfigFoundAutotune': 'ClassicGumtree_0.8_300_1',
		// 'isBestSearchSmallerThanDefault': False, 'minSizeFound': 21.0, 'sizeDefault':
		// 26.0, 'sizeBest': 27.0}, {'diff':
		// 'nr_2868_id_1_204a06e965d90f2405ab541c9eef079d4e28a167_Proj1_JDT.csv',
		// 'minConfigFoundAutotune': 'ClassicGumtree_0.1_1100_1',
		// 'isBestSearchSmallerThanDefault': False, 'minSizeFound': 187.0,
		// 'sizeDefault': 193.0, 'sizeBest': 202.0}]

		String diffid = "nr_2436_id_1_1bb89a0c23a9fae922da897a2ae225998c7c0a64_PhoneWindowManager_JDT.csv";
		System.out.println(diffid + "best auto vs default ");
		String row = diffid + ".csv,ClassicGumtree_0.1_100_1,8,ClassicGumtree_0.5_1000_1,102,94,"
				+ "ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@100@GT_STM_MH@1,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@1";
		// "nr_796_id_3_054edb17739fc164d8f56aef4dbb7ff9eaf9e88c_AvailableServiceBindingController_JDT.csv,ClassicGumtree_0.1_1400_1,8,ClassicGumtree_0.5_1000_2,94,86,ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1400@GT_STM_MH@1,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@2";
		String pathMegadiff = "/Users/matias/develop/newAstorexecution/megadiff-expansion-temp/";
		String pathOut = "/Users/matias/Downloads/results/visualizations/";
		ResultVisualizer rv = new ResultVisualizer();
		boolean isJDT = diffid.contains("JDT");
		rv.saveVisualizationRow(row, pathMegadiff, pathOut, isJDT);

		System.out.println("***********");
		System.out.println(diffid + "best auto vs BestHyperOptimization ");
		row = diffid + ".csv,ClassicGumtree_0.1_100_1,8,SimpleGumtree_0.1_1,102,94,"
				+ "ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@100@GT_STM_MH@1,SimpleGumtree@GT_BUM_SMT_SBUP@0.1@GT_STM_MH@1";
		// "nr_796_id_3_054edb17739fc164d8f56aef4dbb7ff9eaf9e88c_AvailableServiceBindingController_JDT.csv,ClassicGumtree_0.1_1400_1,8,ClassicGumtree_0.5_1000_2,94,86,ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1400@GT_STM_MH@1,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@2";
		pathMegadiff = "/Users/matias/develop/newAstorexecution/megadiff-expansion-temp/";
		pathOut = "/Users/matias/Downloads/results/visualizations/";
		rv = new ResultVisualizer();
		isJDT = diffid.contains("JDT");
		rv.saveVisualizationRow(row, pathMegadiff, pathOut, isJDT);
	}
}
