package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine;

public class ExhaustiveEngineTest {

	@Test
	public void testExhaustive_Simple_1() throws Exception {

		File fs = new File(
				"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_s.java");
		File ft = new File(
				"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_t.java");

		ExhaustiveEngine rp = new ExhaustiveEngine();
		ResponseBestParameter bestConfig = rp.computeBestLocal(fs, ft);
		// assertEquals("ClassicGumtree-bu_minsim-0.6-bu_minsize-1200-st_minprio-2-st_priocalc-height",
		// bestConfig.getBest());
		// assertEquals(1, bestConfig.getNumberOfEvaluatedPairs());
		assertEquals(1d, bestConfig.getMedian(), 0);
		assertTrue(bestConfig.getAllBest().size() > 0);

		System.out.println(bestConfig.getAllBest());
		assertTrue(bestConfig.getAllBest()
				.contains("ClassicGumtree-bu_minsim-0.6-bu_minsize-1200-st_minprio-2-st_priocalc-height"));

	}
}
