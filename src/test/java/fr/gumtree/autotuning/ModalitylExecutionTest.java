package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumtreeProperties;
import com.github.gumtreediff.tree.Tree;

import fr.gumtree.autotuning.entity.SingleDiffResult;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration;
import fr.gumtree.autotuning.outils.Constants;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ModalitylExecutionTest {

	@Test
	public void test1() throws Exception {

		ExhaustiveEngine engine = new ExhaustiveEngine();
		File fs = new File(
				"./examples/megadiff-sample/1/1_203910661b72775d1a983bf98c25ddde2d2898b9/Producto/1_203910661b72775d1a983bf98c25ddde2d2898b9_Producto_s.java");
		File ft = new File(
				"./examples/megadiff-sample/1/1_203910661b72775d1a983bf98c25ddde2d2898b9/Producto/1_203910661b72775d1a983bf98c25ddde2d2898b9_Producto_t.java");

		Tree tl = null;
		Tree tr = null;
		SpoonTreeBuilder builder = new SpoonTreeBuilder();
		tl = builder.build(fs);
		tr = builder.build(ft);

		CompositeMatchers.CompleteGumtreeMatcher matcher = new CompositeMatchers.CompleteGumtreeMatcher();

		GumtreeProperties properies = new GumtreeProperties();

		// CompleteGumtreeMatcher {st_priocalc=height, bu_minsim=0.9, st_minprio=2,
		// bu_minsize=1200}
		properies.put(ConfigurationOptions.st_priocalc, "height");
		properies.put(ConfigurationOptions.bu_minsim, 0.9);
		properies.put(ConfigurationOptions.st_minprio, 2);
		properies.put(ConfigurationOptions.bu_minsize, 1200);

		List<GumtreeProperties> combinations = new ArrayList<GumtreeProperties>();

		combinations.add(properies);
		ExecutionConfiguration config = new ExecutionConfiguration();

		List<SingleDiffResult> resultParalel = engine.runInParallelMultipleConfigurations(tl, tr, matcher, combinations,
				config.getTimeOut(), config.getTimeUnit(), config.getNumberOfThreads());

		assertEquals(1, resultParalel.size());

		assertEquals(400d, new Double(resultParalel.get(0).get(Constants.TIME).toString()), 150);

		List<SingleDiffResult> resultS = engine.runInSerialMultipleConfiguration(tl, tr, matcher, combinations);

		assertEquals(1, resultS.size());

		assertEquals(250d, new Double(resultS.get(0).get(Constants.TIME).toString()), 150);

	}

	@Test
	public void testManyConfigs() throws Exception {

		ExhaustiveEngine engine = new ExhaustiveEngine();
		File fs = new File(
				"./examples/megadiff-sample/1/1_203910661b72775d1a983bf98c25ddde2d2898b9/Producto/1_203910661b72775d1a983bf98c25ddde2d2898b9_Producto_s.java");
		File ft = new File(
				"./examples/megadiff-sample/1/1_203910661b72775d1a983bf98c25ddde2d2898b9/Producto/1_203910661b72775d1a983bf98c25ddde2d2898b9_Producto_t.java");

		Tree tl = null;
		Tree tr = null;
		SpoonTreeBuilder builder = new SpoonTreeBuilder();
		tl = builder.build(fs);
		tr = builder.build(ft);

		CompositeMatchers.CompleteGumtreeMatcher matcher = new CompositeMatchers.CompleteGumtreeMatcher();

		List<GumtreeProperties> combinations = new ArrayList<GumtreeProperties>();

		for (int i = 1000; i < 2000; i = i + 100) {
			GumtreeProperties properies = new GumtreeProperties();

			// CompleteGumtreeMatcher {st_priocalc=height, bu_minsim=0.9, st_minprio=2,
			// bu_minsize=1200}
			properies.put(ConfigurationOptions.st_priocalc, "height");
			properies.put(ConfigurationOptions.bu_minsim, 0.9);
			properies.put(ConfigurationOptions.st_minprio, 2);
			properies.put(ConfigurationOptions.bu_minsize, 1200);
			combinations.add(properies);
		}

		ExecutionConfiguration config = new ExecutionConfiguration();

		System.out.println("Serial");
		List<SingleDiffResult> resultS = engine.runInSerialMultipleConfiguration(tl, tr, matcher, combinations);

		assertEquals(10, resultS.size());

		for (SingleDiffResult singleDiffResult : resultS) {
			Double serial = new Double(singleDiffResult.get(Constants.TIME).toString());
			System.out.println(serial);
			assertTrue(serial > 200 && serial < 700);

		}

		System.out.println("Paralell");

		List<SingleDiffResult> resultParalel = engine.runInParallelMultipleConfigurations(tl, tr, matcher, combinations,
				config.getTimeOut(), config.getTimeUnit(), config.getNumberOfThreads());

		assertEquals(10, resultParalel.size());

		for (SingleDiffResult singleDiffResult : resultParalel) {
			Double serial = new Double(singleDiffResult.get(Constants.TIME).toString());
			System.out.println(serial);
			assertTrue(serial > 900 && serial < 2500);

		}
	}

}
