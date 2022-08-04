package fr.gumtree.autotuning.exhaustive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;

import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumtreeProperties;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.Tree;

import fr.gumtree.autotuning.entity.SingleDiffResult;
import fr.gumtree.autotuning.fitness.LengthEditScriptFitness;
import fr.gumtree.autotuning.gumtree.ASTMODE;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.gumtree.ExecutionExhaustiveConfiguration;
import fr.gumtree.autotuning.outils.Constants;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class TimeExecutionTest {

	@Test
	public void testCores() throws Exception {
		int cores = Runtime.getRuntime().availableProcessors();
		System.out.println(cores);
	}

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

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();

		GumtreeProperties properies = new GumtreeProperties();

		// CompleteGumtreeMatcher {st_priocalc=height, bu_minsim=0.9, st_minprio=2,
		// bu_minsize=1200}
		properies.put(ConfigurationOptions.st_priocalc, "height");
		properies.put(ConfigurationOptions.bu_minsim, 0.9);
		properies.put(ConfigurationOptions.st_minprio, 2);
		properies.put(ConfigurationOptions.bu_minsize, 1200);

		List<GumtreeProperties> combinations = new ArrayList<GumtreeProperties>();

		combinations.add(properies);
		ExecutionExhaustiveConfiguration config = new ExecutionExhaustiveConfiguration(METRIC.MEAN, ASTMODE.GTSPOON,
				new LengthEditScriptFitness());

		List<SingleDiffResult> resultParalel = engine.runSingleMatcherMultipleParameters(tl, tr, matcher, combinations,
				config);

		assertEquals(1, resultParalel.size());

		assertEquals(400d, new Double(resultParalel.get(0).get(Constants.TIME).toString()), 150);

		List<SingleDiffResult> resultS = engine.runSingleMatcherSerial(tl, tr, matcher, config, combinations);

		assertEquals(1, resultS.size());

		assertEquals(250d, new Double(resultS.get(0).get(Constants.TIME).toString()), 150);

	}

	@Test
	public void testManyConfigs_ClassicGumtreeMatcher1() throws Exception {

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

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();

		List<GumtreeProperties> combinations = new ArrayList<GumtreeProperties>();

		for (int i = 1000; i < 2000; i = i + 100) {
			GumtreeProperties properies = new GumtreeProperties();

			properies.put(ConfigurationOptions.st_priocalc, "height");
			properies.put(ConfigurationOptions.bu_minsim, 0.9);
			properies.put(ConfigurationOptions.st_minprio, 2);
			properies.put(ConfigurationOptions.bu_minsize, i);
			combinations.add(properies);
		}

		ExecutionExhaustiveConfiguration config = new ExecutionExhaustiveConfiguration(METRIC.MEAN, ASTMODE.GTSPOON,
				new LengthEditScriptFitness());
		// config.setNumberOfThreads(2);
		System.out.println("Serial");

		long init = (new Date()).getTime();
		List<SingleDiffResult> resultS = engine.runSingleMatcherSerial(tl, tr, matcher, config, combinations);

		assertEquals(10, resultS.size());

		for (SingleDiffResult singleDiffResult : resultS) {
			Double serial = new Double(singleDiffResult.get(Constants.TIME).toString());
			System.out.println(serial);
			assertTrue(serial > 200 && serial < 1000);

		}

		System.out.println("Total time sec " + ((new Date()).getTime() - init) / 1000);

		System.out.println("Paralell");

		init = (new Date()).getTime();

		List<SingleDiffResult> resultParalel = engine.runSingleMatcherMultipleParameters(tl, tr, matcher, combinations,
				config);

		assertEquals(10, resultParalel.size());

		for (SingleDiffResult singleDiffResult : resultParalel) {
			Double serial = new Double(singleDiffResult.get(Constants.TIME).toString());
			System.out.println(serial);
			assertTrue(serial > 900 && serial < 2500);

		}

		System.out.println("Total time sec " + ((new Date()).getTime() - init) / 1000);

	}

	@Test
	public void testManyConfigsSimple() throws Exception {

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

		long init = (new Date()).getTime();
		CompositeMatchers.SimpleGumtree matcher = new CompositeMatchers.SimpleGumtree();

		List<GumtreeProperties> combinations = new ArrayList<GumtreeProperties>();

		for (int i = 1000; i < 2000; i = i + 100) {
			GumtreeProperties properies = new GumtreeProperties();
			for (int mi = 1; mi <= 5; mi = mi + 1) {
				// CompleteGumtreeMatcher {st_priocalc=height, bu_minsim=0.9, st_minprio=2,
				// bu_minsize=1200}
				properies.put(ConfigurationOptions.st_priocalc, "height");
				properies.put(ConfigurationOptions.bu_minsim, 0.9);
				properies.put(ConfigurationOptions.st_minprio, mi);
				properies.put(ConfigurationOptions.bu_minsize, i);
				combinations.add(properies);
			}
		}

		ExecutionExhaustiveConfiguration config = new ExecutionExhaustiveConfiguration(METRIC.MEAN, ASTMODE.GTSPOON,
				new LengthEditScriptFitness());

		System.out.println("Serial");
		List<SingleDiffResult> resultS = engine.runSingleMatcherSerial(tl, tr, matcher, config, combinations);

		// assertEquals(10, resultS.size());

		DescriptiveStatistics statsS = new DescriptiveStatistics();
		for (SingleDiffResult singleDiffResult : resultS) {
			Double serial = new Double(singleDiffResult.get(Constants.TIME).toString());
			System.out.println(serial);
			assertTrue(serial > 0 && serial < 200);
			statsS.addValue(serial);

		}
		System.out.println("Total time sec " + ((new Date()).getTime() - init) / 1000);

		System.out.println("Paralell");

		init = (new Date()).getTime();

		List<SingleDiffResult> resultParalel = engine.runSingleMatcherMultipleParameters(tl, tr, matcher, combinations,
				config);

		// assertEquals(10, resultParalel.size());
		DescriptiveStatistics statsP = new DescriptiveStatistics();
		for (SingleDiffResult singleDiffResult : resultParalel) {
			Double serial = new Double(singleDiffResult.get(Constants.TIME).toString());
			System.out.println(serial);
			assertTrue(serial > 1 && serial < 200);
			statsP.addValue(serial);

		}
		System.out.println("Stat Serial");
		System.out.println(statsS);

		System.out.println("Stat Paralell");
		System.out.println(statsP);

		System.out.println("Total time sec " + ((new Date()).getTime() - init) / 1000);

	}

	@Test
	public void testConfigThreadVariability() throws Exception {

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

		Matcher matcher = new CompositeMatchers.ClassicGumtree();

		List<GumtreeProperties> combinations = new ArrayList<GumtreeProperties>();

		for (int i = 1000; i < 2000; i = i + 100) {
			GumtreeProperties properies = new GumtreeProperties();
			for (int mi = 1; mi <= 5; mi = mi + 1) {
				properies.put(ConfigurationOptions.st_priocalc, "height");
				properies.put(ConfigurationOptions.bu_minsim, 0.9);
				properies.put(ConfigurationOptions.st_minprio, mi);
				properies.put(ConfigurationOptions.bu_minsize, i);
				combinations.add(properies);
			}
		}

		System.out.println("Serial");
		long nns = (new Date()).getTime();
		ExecutionExhaustiveConfiguration config = new ExecutionExhaustiveConfiguration(METRIC.MEAN, ASTMODE.GTSPOON,
				new LengthEditScriptFitness());
		List<SingleDiffResult> resultS = engine.runSingleMatcherSerial(tl, tr, matcher, config, combinations);
		double timeserial = ((double) (new Date()).getTime() - nns) / 60;
		// assertEquals(10, resultS.size());

		DescriptiveStatistics statsS = new DescriptiveStatistics();
		for (SingleDiffResult singleDiffResult : resultS) {
			Double serial = new Double(singleDiffResult.get(Constants.TIME).toString());
			// System.out.println(serial);
			// assertTrue(serial > 0 && serial < 50);
			statsS.addValue(serial);

		}

		System.out.println("Paralell");

		// ExecutionExhaustiveConfiguration config = new
		// ExecutionExhaustiveConfiguration();

		List<Double> means = new ArrayList<>();
		List<Double> times = new ArrayList<>();
		for (int i = 1; i <= 10; i++) {

			config.setNumberOfThreads(i);
			long nn = (new Date()).getTime();
			List<SingleDiffResult> resultParalel = engine.runSingleMatcherMultipleParameters(tl, tr, matcher,
					combinations, config);
			times.add(((double) (new Date()).getTime() - nn) / 60);
			// assertEquals(10, resultParalel.size());
			DescriptiveStatistics statsP = new DescriptiveStatistics();
			for (SingleDiffResult singleDiffResult : resultParalel) {
				Double serial = new Double(singleDiffResult.get(Constants.TIME).toString());
				// System.out.println(serial);
				// assertTrue(serial > 1 && serial < 200);
				statsP.addValue(serial);

			}
			means.add(statsP.getMean());

		}
		System.out.println("Mean time by thread:");
		System.out.println(means);
		System.out.println("Time per exec:");
		System.out.println(times);
		System.out.println("Time serial:");
		System.out.println(timeserial);
	}

}
