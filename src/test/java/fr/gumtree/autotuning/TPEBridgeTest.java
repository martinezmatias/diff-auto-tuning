package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import fr.gumtree.autotuning.entity.ResponseBestParameter;

/**
 * 
 * @author Matias Martinez
 *
 */
public class TPEBridgeTest {

	@Test
	public void testTPEBridge() throws Exception {

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
}
