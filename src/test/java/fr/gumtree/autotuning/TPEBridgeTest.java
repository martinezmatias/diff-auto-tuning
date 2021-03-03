package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import com.github.gumtreediff.actions.Diff;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class TPEBridgeTest {

	@Test
	public void testTPEBridge_1() throws Exception {

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
	public void testTPEBridge_2() throws Exception {

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

	}
}
