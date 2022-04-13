package fr.gumtree.autotuning.tpe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;

import org.junit.Test;

import com.github.gumtreediff.actions.Diff;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.gumtree.ASTMODE;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration;
import fr.gumtree.autotuning.gumtree.ExecutionExhaustiveConfiguration;
import fr.gumtree.autotuning.gumtree.ExecutionTPEConfiguration;
import fr.gumtree.autotuning.gumtree.GTProxy;
import fr.gumtree.autotuning.searchengines.TPEEngine;
import fr.gumtree.autotuning.treebuilder.ITreeBuilder;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class TPEEngineTest {

	@Test
	public void testTPEBridge_Local_Spoon() throws Exception {

		File fs = new File(
				"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_s.java");
		File ft = new File(
				"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_t.java");

		TPEEngine rp = new TPEEngine();

		ExecutionConfiguration configuration = new ExecutionTPEConfiguration();
		configuration.setAstmode(ASTMODE.GTSPOON);

		ResponseBestParameter bestConfig = rp.computeBestLocal(fs, ft, configuration);

		assertEquals(1, bestConfig.getNumberOfEvaluatedPairs());
		assertEquals(1d, bestConfig.getMetricValue(), 0);

	}

	@Test
	public void testTPEBridge_Local_spoon_2() throws Exception {

		File fs = new File(
				"./examples/3_04f0e8f7a3545cf877c10967396b06595d57c34a/JavaExtensions/3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions_s.java");
		File ft = new File(
				"./examples/3_04f0e8f7a3545cf877c10967396b06595d57c34a/JavaExtensions/3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions_t.java");

		TPEEngine rp = new TPEEngine();

		ExecutionConfiguration configuration = new ExecutionTPEConfiguration();
		configuration.setAstmode(ASTMODE.GTSPOON);

		ResponseBestParameter bestConfig = rp.computeBestLocal(fs, ft, configuration);

		JsonArray infoEvaluations = bestConfig.getInfoEvaluations();

		assertEquals(1, bestConfig.getNumberOfEvaluatedPairs());

		assertEquals(5d, bestConfig.getMetricValue(), 0.01);

		// assertEquals("CompleteGumtreeMatcher-bu_minsim-1.0-bu_minsize-1500-st_minprio-1-st_priocalc-size",
		// bestConfig.getBest());

		// let's compute the diff directly
		GTProxy proxy = new GTProxy();
		ITreeBuilder treebuilder = null;
		treebuilder = new SpoonTreeBuilder();

		Diff diff = proxy.run(treebuilder.build(fs), treebuilder.build(ft), bestConfig.getBest());

		assertEquals(diff.editScript.asList().size(), bestConfig.getMetricValue(), 0);

		assertEquals(5, diff.editScript.asList().size());

		System.out.println(infoEvaluations);
		// 100 + the re- evaluation of the best
		assertEquals(101, infoEvaluations.size());
		boolean existMin = false;
		for (JsonElement ieval : infoEvaluations) {
			if (ieval.getAsJsonObject().get("status").getAsString().equals("ok")) {

				JsonArray asJsonArray = ieval.getAsJsonObject().get("actions").getAsJsonArray();
				assertEquals(1, asJsonArray.size());
				int currentNrActions = asJsonArray.get(0).getAsJsonObject().get("nractions").getAsInt();
				assertTrue(currentNrActions >= diff.editScript.asList().size());
				if (currentNrActions == diff.editScript.asList().size())
					existMin = true;
			}

		}
		assertTrue(existMin);
	}

	@Test
	public void testTPBridge_Global_1() throws Exception {

		File fs = new File("./examples/input_multiple.txt");
		TPEEngine rp = new TPEEngine();

		ExecutionExhaustiveConfiguration config = new ExecutionExhaustiveConfiguration();
		config.setAstmode(ASTMODE.GTSPOON);
		ResponseBestParameter bestConfig = rp.computeBestGlobal(fs, config);
		System.out.println(bestConfig);

		assertEquals(1, bestConfig.getNumberOfEvaluatedPairs());

	}

	@Test
	public void testTPBridge_Global_saveFiles() throws Exception {

		File fs = new File("./examples/input_multiple.txt");
		TPEEngine rp = new TPEEngine();

		File outDirTemp = Files.createTempDirectory("testDAT_multiple_" + new Long((new Date()).getTime()).toString())
				.toFile();
		assertTrue(outDirTemp.exists());

		assertTrue(outDirTemp.list().length == 0);
		ExecutionConfiguration config = new ExecutionTPEConfiguration();
		config.setSaveScript(true);
		config.setDirDiffTreeSerialOutput(outDirTemp);

		config.setAstmode(ASTMODE.JDT);
		ResponseBestParameter bestConfig = rp.computeBestGlobal(fs, config);

		System.out.println(bestConfig);

		assertEquals(1, bestConfig.getNumberOfEvaluatedPairs());

		assertTrue(outDirTemp.exists());
		assertTrue(outDirTemp.listFiles().length > 0);

		System.out.println(Arrays.toString(outDirTemp.listFiles()));

	}

	@Test
	public void testTPEBridge_Local_SaveFile() throws Exception {

		File fs = new File(
				"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_s.java");
		File ft = new File(
				"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_t.java");

		TPEEngine rp = new TPEEngine();

		File outDirTemp = Files.createTempDirectory("testDAT_simple_" + new Long((new Date()).getTime()).toString())
				.toFile();

		assertTrue(outDirTemp.exists());

		assertTrue(outDirTemp.list().length == 0);

		ExecutionConfiguration config = new ExecutionTPEConfiguration();
		config.setSaveScript(true);
		config.setDirDiffTreeSerialOutput(outDirTemp);
		config.setAstmode(ASTMODE.JDT);
		ResponseBestParameter bestConfig = rp.computeBestLocal(fs, ft, config);

		assertEquals(1, bestConfig.getNumberOfEvaluatedPairs());
		assertEquals(2d, bestConfig.getMetricValue(), 0);

		assertTrue(outDirTemp.exists());
		assertTrue(outDirTemp.listFiles().length > 0);

		System.out.println(Arrays.toString(outDirTemp.listFiles()));
	}

	@Test
	public void testTPBridge_Global_2() throws Exception {

		File fs = new File("./examples/input_multiple2.txt");
		TPEEngine rp = new TPEEngine();

		ExecutionConfiguration config = new ExecutionTPEConfiguration();
		config.setAstmode(ASTMODE.GTSPOON);

		ResponseBestParameter bestConfig = rp.computeBestGlobal(fs, config);

		JsonArray infoEvaluations = bestConfig.getInfoEvaluations();

		System.out.println(bestConfig);

		assertEquals(2, bestConfig.getNumberOfEvaluatedPairs());
		assertEquals(3.0d, bestConfig.getMetricValue(), 0.001);

		// assertEquals("CompleteGumtreeMatcher-bu_minsim-1.0-bu_minsize-1500-st_minprio-1-st_priocalc-size",
		// bestConfig.getBest());

		// {"actions":[{"file":"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_s.java","nractions":1},{"file":"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_s.java","nractions":5}],"parameters":"CompleteGumtreeMatcher-bu_minsim-1.0-bu_minsize-1500-st_minprio-1-st_priocalc-size","status":"ok"}

		System.out.println(infoEvaluations);
		// 100 + the re- evaluation of the best
		assertEquals(101, infoEvaluations.size());
		boolean existMin = false;
		for (JsonElement ieval : infoEvaluations) {
			if (ieval.getAsJsonObject().get("status").getAsString().equals("ok")) {

				JsonArray asJsonArray = ieval.getAsJsonObject().get("actions").getAsJsonArray();
				assertEquals(2, asJsonArray.size());
				int currentNrActions = 0;

				for (JsonElement jsonElement : asJsonArray) {
					currentNrActions += jsonElement.getAsJsonObject().get("nractions").getAsInt();
				}
				if (currentNrActions == 6) // 5 + 1
					existMin = true;
			}

		}
		assertTrue(existMin);

	}

}
