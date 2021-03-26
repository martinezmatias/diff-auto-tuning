package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.gumtree.ASTMODE;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine;

public class ExhaustiveEngineTest {

	@Test
	public void testExhaustive_Local_Simple_1() throws Exception {

		File fs = new File(
				"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_s.java");
		File ft = new File(
				"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_t.java");

		ExhaustiveEngine rp = new ExhaustiveEngine();

		ExecutionConfiguration ec = new ExecutionConfiguration();
		// Only for test
		ec.setMetric(METRIC.MEAN);
		ec.setSaveScript(true);

		ResponseBestParameter bestConfig = rp.computeBestLocal(fs, ft, ASTMODE.GTSPOON, ec);
		// assertEquals("ClassicGumtree-bu_minsim-0.6-bu_minsize-1200-st_minprio-2-st_priocalc-height",
		// bestConfig.getBest());
		// assertEquals(1, bestConfig.getNumberOfEvaluatedPairs());
		assertEquals(1d, bestConfig.getMedian(), 0);
		assertTrue(bestConfig.getAllBest().size() > 0);

		System.out.println(bestConfig.getAllBest());
		assertTrue(bestConfig.getAllBest()
				.contains("ClassicGumtree-bu_minsim-0.6-bu_minsize-1200-st_minprio-2-st_priocalc-height"));

	}

	@Test
	public void testExhaustive_Local_Simple_2() throws Exception {

		File fs = new File(
				"./examples/1_0007d191fec7fe2d6a0c4e87594cb286a553f92c/ASTInspector/1_0007d191fec7fe2d6a0c4e87594cb286a553f92c_ASTInspector_s.java");
		File ft = new File(
				"./examples/1_0007d191fec7fe2d6a0c4e87594cb286a553f92c/ASTInspector/1_0007d191fec7fe2d6a0c4e87594cb286a553f92c_ASTInspector_t.java");

		ExhaustiveEngine rp = new ExhaustiveEngine();
		ResponseBestParameter bestConfig = rp.computeBestLocal(fs, ft);
		// assertEquals("ClassicGumtree-bu_minsim-0.6-bu_minsize-1200-st_minprio-2-st_priocalc-height",
		// bestConfig.getBest());
		// assertEquals(1, bestConfig.getNumberOfEvaluatedPairs());
		assertEquals(1d, bestConfig.getMedian(), 0);
		assertTrue(bestConfig.getAllBest().size() > 0);

		System.out.println(bestConfig.getAllBest());
		// assertTrue(bestConfig.getAllBest()
		// .contains("ClassicGumtree-bu_minsim-0.6-bu_minsize-1200-st_minprio-2-st_priocalc-height"));

	}

	/// examples/megadiff-sample/1/1_831e3b0420e70f7c2695cb248dd8b488b1fd84b7/NewProductAtomView/1_831e3b0420e70f7c2695cb248dd8b488b1fd84b7_NewProductAtomView_s.java
	@Test
	public void testExhaustive_Local_Simple_3() throws Exception {

		File fs = new File(
				"./examples/megadiff-sample/1/1_831e3b0420e70f7c2695cb248dd8b488b1fd84b7/NewProductAtomView/1_831e3b0420e70f7c2695cb248dd8b488b1fd84b7_NewProductAtomView_s.java");
		File ft = new File(
				"./examples/megadiff-sample/1/1_831e3b0420e70f7c2695cb248dd8b488b1fd84b7/NewProductAtomView/1_831e3b0420e70f7c2695cb248dd8b488b1fd84b7_NewProductAtomView_t.java");

		ExhaustiveEngine rp = new ExhaustiveEngine();
		ResponseBestParameter bestConfig = rp.computeBestLocal(fs, ft);
		// assertEquals("ClassicGumtree-bu_minsim-0.6-bu_minsize-1200-st_minprio-2-st_priocalc-height",
		// bestConfig.getBest());
		// assertEquals(1, bestConfig.getNumberOfEvaluatedPairs());
		assertEquals(1d, bestConfig.getMedian(), 0);
		assertTrue(bestConfig.getAllBest().size() > 0);

		System.out.println(bestConfig.getAllBest());
		// assertTrue(bestConfig.getAllBest()
		// .contains("ClassicGumtree-bu_minsim-0.6-bu_minsize-1200-st_minprio-2-st_priocalc-height"));

	}

	/// examples/megadiff-sample/1/1_831e3b0420e70f7c2695cb248dd8b488b1fd84b7/NewProductAtomView/1_831e3b0420e70f7c2695cb248dd8b488b1fd84b7_NewProductAtomView_s.java
	@Test
	public void testExhaustive_Local_Simple_4_paralell() throws Exception {

		File fs = new File(
				"./examples/megadiff-sample/1/1_203910661b72775d1a983bf98c25ddde2d2898b9/Producto/1_203910661b72775d1a983bf98c25ddde2d2898b9_Producto_s.java");
		File ft = new File(
				"./examples/megadiff-sample/1/1_203910661b72775d1a983bf98c25ddde2d2898b9/Producto/1_203910661b72775d1a983bf98c25ddde2d2898b9_Producto_t.java");

		ExhaustiveEngine rp = new ExhaustiveEngine();
		ResponseBestParameter bestConfig = rp.computeBestLocal(fs, ft);
		// assertEquals("ClassicGumtree-bu_minsim-0.6-bu_minsize-1200-st_minprio-2-st_priocalc-height",
		// bestConfig.getBest());
		// assertEquals(1, bestConfig.getNumberOfEvaluatedPairs());
		assertEquals(1d, bestConfig.getMedian(), 0);
		assertTrue(bestConfig.getAllBest().size() > 0);

		System.out.println(bestConfig.getAllBest());
		// assertTrue(bestConfig.getAllBest()
		// .contains("ClassicGumtree-bu_minsim-0.6-bu_minsize-1200-st_minprio-2-st_priocalc-height"));

	}

	@Test
	public void testExhaustive_Local_Simple_4_serial() throws Exception {

		File fs = new File(
				"./examples/megadiff-sample/1/1_203910661b72775d1a983bf98c25ddde2d2898b9/Producto/1_203910661b72775d1a983bf98c25ddde2d2898b9_Producto_s.java");
		File ft = new File(
				"./examples/megadiff-sample/1/1_203910661b72775d1a983bf98c25ddde2d2898b9/Producto/1_203910661b72775d1a983bf98c25ddde2d2898b9_Producto_t.java");

		ExhaustiveEngine rp = new ExhaustiveEngine();
		ResponseBestParameter bestConfig = rp.computeBestLocal(fs, ft);
		// assertEquals("ClassicGumtree-bu_minsim-0.6-bu_minsize-1200-st_minprio-2-st_priocalc-height",
		// bestConfig.getBest());
		// assertEquals(1, bestConfig.getNumberOfEvaluatedPairs());
		assertEquals(1d, bestConfig.getMedian(), 0);
		assertTrue(bestConfig.getAllBest().size() > 0);

		System.out.println(bestConfig.getAllBest());
		// assertTrue(bestConfig.getAllBest()
		// .contains("ClassicGumtree-bu_minsim-0.6-bu_minsize-1200-st_minprio-2-st_priocalc-height"));

	}

	@Test
	public void testExhaustive_Global_Multiple_1() throws Exception {

		File fs = new File("./examples/input_multiple3.txt");

		ExhaustiveEngine rp = new ExhaustiveEngine();
		ResponseBestParameter bestConfig = rp.computeBestGlobal(fs);

		assertEquals(1d, bestConfig.getMedian(), 0);
		assertTrue(bestConfig.getAllBest().size() > 0);

		System.out.println(bestConfig.getAllBest());
		assertTrue(bestConfig.getAllBest()
				.contains("ClassicGumtree-bu_minsim-0.6-bu_minsize-1200-st_minprio-2-st_priocalc-height"));

	}

	@Test
	public void testExhaustive_Global_Multiple_2_small_files() throws Exception {

		File fs = new File("./examples/input_multiple_3_diffs.txt");

		ExhaustiveEngine rp = new ExhaustiveEngine();
		ExecutionConfiguration ec = new ExecutionConfiguration();
		// Only for test
		ec.setMetric(METRIC.MEAN);
		ec.setSaveScript(true);
		ResponseBestParameter bestConfig = rp.computeBestGlobal(fs, ASTMODE.GTSPOON, ec);

		assertEquals(5.3d, bestConfig.getMedian(), 0.1);

		// assertEquals(1d, bestConfig.getMedian(), 0);
		assertTrue(bestConfig.getAllBest().size() > 0);

		System.out.println(bestConfig.getAllBest());
		assertFalse(bestConfig.getAllBest()
				.contains("CompleteGumtreeMatcher-bu_minsim-1.0-bu_minsize-2000-st_minprio-4-st_priocalc-height"));

		assertFalse(bestConfig.getAllBest()
				.contains("CompleteGumtreeMatcher-bu_minsim-0.9-bu_minsize-2000-st_minprio-4-st_priocalc-height"));

		assertFalse(bestConfig.getAllBest().contains("XyMatcher-st_minprio-5-st_priocalc-size-xy_minsim-0.3"));

		assertFalse(bestConfig.getAllBest().contains("SimpleGumtree-st_minprio-5-st_priocalc-height"));

		//
		assertTrue(bestConfig.getAllBest().contains("SimpleGumtree-st_minprio-2-st_priocalc-size"));

		assertTrue(bestConfig.getAllBest()
				.contains("ClassicGumtree-bu_minsim-0.1-bu_minsize-100-st_minprio-1-st_priocalc-size"));

		System.out.println("--> " + bestConfig.getAllBest().size() + ": " + bestConfig.getAllBest());

	}

}
