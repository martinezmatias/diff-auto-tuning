package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import com.github.gumtreediff.actions.Diff;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class TPEBridgeTest {

	@Test
	public void testTPEBridge_Simple_1() throws Exception {

		File fs = new File(
				"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_s.java");
		File ft = new File(
				"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_t.java");

		TPEEngine rp = new TPEEngine();
		ResponseBestParameter bestConfig = rp.computeBest(fs, ft);
		assertEquals("ClassicGumtree-bu_minsim-0.6-bu_minsize-1200-st_minprio-2-st_priocalc-height",
				bestConfig.getBest());
		assertEquals(1, bestConfig.getNumberOfEvaluatedPairs());
		assertEquals(1d, bestConfig.getMedian(), 0);
	}

	@Test
	public void testTPEBridge_Simple_2() throws Exception {

		File fs = new File(
				"./examples/3_04f0e8f7a3545cf877c10967396b06595d57c34a/JavaExtensions/3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions_s.java");
		File ft = new File(
				"./examples/3_04f0e8f7a3545cf877c10967396b06595d57c34a/JavaExtensions/3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions_t.java");

		TPEEngine rp = new TPEEngine();
		ResponseBestParameter bestConfig = rp.computeBest(fs, ft);
		assertEquals("CompleteGumtreeMatcher-bu_minsim-1.0-bu_minsize-1500-st_minprio-1-st_priocalc-size",
				bestConfig.getBest());

		assertEquals(1, bestConfig.getNumberOfEvaluatedPairs());

		// let's compute the diff directly
		GTProxy proxy = new GTProxy();
		ITreeBuilder treebuilder = null;
		treebuilder = new SpoonTreeBuilder();

		Diff diff = proxy.run(treebuilder.build(fs), treebuilder.build(ft), bestConfig.getBest());

		assertEquals(diff.editScript.asList().size(), bestConfig.getMedian(), 0);

		assertEquals(5, diff.editScript.asList().size());

		JsonArray infoEvaluations = rp.launcher.retrieveInfoSimple();

		System.out.println(infoEvaluations);
		// 100 + the re- evaluation of the best
		assertEquals(101, infoEvaluations.size());
		boolean existMin = false;
		for (JsonElement ieval : infoEvaluations) {
			if (ieval.getAsJsonObject().get("status").getAsString().equals("ok")) {

				assertEquals(1, ieval.getAsJsonObject().get("actions").getAsJsonArray());
				int currentNrActions = ieval.getAsJsonObject().get("actions").getAsJsonArray().get(0).getAsJsonObject()
						.get("nractions").getAsInt();
				assertTrue(currentNrActions >= diff.editScript.asList().size());
				if (currentNrActions == diff.editScript.asList().size())
					existMin = true;
			}

		}
		assertTrue(existMin);
	}

	@Test
	public void testTPBridgeMultiple_1() throws Exception {

		File fs = new File("./examples/input_multiple.txt");
		TPEEngine rp = new TPEEngine();
		ResponseBestParameter bestConfig = rp.computeBest(fs);
		System.out.println(bestConfig);

	}

}
