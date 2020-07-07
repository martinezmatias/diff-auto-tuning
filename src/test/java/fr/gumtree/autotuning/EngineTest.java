package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumTreeProperties;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.utils.Pair;

import fr.gumtree.autotuning.TuningEngine.ASTMODE;
import fr.gumtree.autotuning.TuningEngine.PARALLEL_EXECUTION;

public class EngineTest {

	@Test
	public void testNavigate() throws IOException {
		File rootMegadiff = new File(
				"/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded");
		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();

		boolean parallel = true;
		// Let's try with sets between 1 and 20
		int[] megadiff_ids = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
		// let's simply try 1 diff per group
		int limitDiffPerGroup = 1;
		reader.navigateMegaDiff("./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup, ASTMODE.GTSPOON,
				PARALLEL_EXECUTION.PROPERTY_LEVEL);

	}

	@Test
	public void testNavigate_SingleDiff() throws IOException {
		File rootMegadiff = new File(
				"/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded");
		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();

		boolean parallel = true;
		// Let's try with set 1
		int[] megadiff_ids = new int[] { 1 };
		// let's simply try 1 diff per group
		int limitDiffPerGroup = 1;
		reader.navigateMegaDiff("./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup, ASTMODE.GTSPOON,
				PARALLEL_EXECUTION.PROPERTY_LEVEL);

	}

	@Test
	public void testNavigate_SingleDiff_1_831e3b() throws IOException {
		File rootMegadiff = new File(
				"/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded");
		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();

		String commitId = "831e3b0420e70f7c2695cb248dd8b488b1fd84b7";

		boolean parallel = false;

		int megadiff_id = 1;

		Map<String, Object> result = reader.navigateSingleDiffMegaDiff("./out/", rootMegadiff, megadiff_id, commitId,
				ASTMODE.GTSPOON, PARALLEL_EXECUTION.PROPERTY_LEVEL);

		assertNotNull(result);
		System.out.println(result);

	}

	/// Diff in one annotation
	@Test
	public void testNavigate_SingleDiff_1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e() throws IOException {
		File rootMegadiff = new File(
				"/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded");
		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();

		String commitId = "02f3fd442349d4e7fdfc9c31a82bb1638db8495e";

		boolean parallel = false;

		int megadiff_id = 1;

		Map<String, Object> result = reader.navigateSingleDiffMegaDiff("./out/", rootMegadiff, megadiff_id, commitId,
				ASTMODE.GTSPOON, PARALLEL_EXECUTION.NONE);

		assertNotNull(result);
		System.out.println(result);

	}

	@SuppressWarnings("deprecation")
	@Test
	public void testNavigate_CompareTimeouts() throws IOException {
		File rootMegadiff = new File(
				"/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded");
		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();

		String commitId = // "014af81101851b42c4c8b6216225c55d9d0b7ff3";//
				"025055b307b6ef358d5153c7b50a1740e2b17f35";
		// "010de14013c38b7f82e4755270e88a8249f3a825";
		// time 7.72 min file
		// nr_98_id_1_010de14013c38b7f82e4755270e88a8249f3a825_SimpleConveyer_GTSPOON.csv

		int megadiff_id = 1;

		reader.setNrThreads(10);
		System.out.println(reader.getNrThreads());

		long tinit = (new Date()).getTime();

		Map<String, Object> result = reader.navigateSingleDiffMegaDiff("./out/", rootMegadiff, megadiff_id, commitId,
				ASTMODE.GTSPOON, PARALLEL_EXECUTION.PROPERTY_LEVEL);
		long tpropertyparalel = (new Date()).getTime() - tinit;

		assertNotNull(result);
		Pair<Long, Integer> r1 = getResults(result);

		reader.setNrThreads(1);

		tinit = (new Date()).getTime();
		Map<String, Object> result2 = reader.navigateSingleDiffMegaDiff("./out/", rootMegadiff, megadiff_id, commitId,
				ASTMODE.GTSPOON, PARALLEL_EXECUTION.NONE);
		long tpnoneparalel = (new Date()).getTime() - tinit;

		Pair<Long, Integer> r2 = getResults(result2);

		Long timeSerial = r2.first;
		Long timePropertyParallel = r1.first;
		assertTrue(timePropertyParallel >= timeSerial);

		Integer executionSerial = r2.second;
		Integer executionsPropertiesParallel = r1.second;
		assertTrue(executionsPropertiesParallel >= executionSerial);

		System.out.println("Total execution time property parallel " + timePropertyParallel / 1000 + ", none parallel "
				+ timeSerial / 1000);

		System.out.println(tpnoneparalel / 1000 + " None sec vs  property paralel" + tpropertyparalel / 1000);
		assertTrue(tpnoneparalel > tpropertyparalel);
		System.out.println("Matcher callable");
		tinit = (new Date()).getTime();
		Map<String, Object> result3 = reader.navigateSingleDiffMegaDiff("./out/", rootMegadiff, megadiff_id, commitId,
				ASTMODE.GTSPOON, PARALLEL_EXECUTION.NONE);
		long tmatcherparallel = (new Date()).getTime() - tinit;

		Pair<Long, Integer> r3 = getResults(result3);

		System.out.println("Results Matcher callable");

		// vs property paralell
		Long timeMatcherParallel = r3.first;
		assertTrue(timeMatcherParallel < timePropertyParallel);
		Integer executionsMatcherParallel = r3.second;
		assertTrue(executionsMatcherParallel >= executionsPropertiesParallel);

		System.out.println("Total execution time matcher parallel " + timeMatcherParallel / 1000
				+ ", property paralell " + timePropertyParallel / 1000);

		System.out.println(tmatcherparallel / 1000 + " matcher sec vs  property paralel" + tpropertyparalel / 1000);
		// 10 threads...

		assertTrue(tmatcherparallel >= tpropertyparalel);

		// vs Serial

		//
//		assertTrue(timeMatcherParallel < timeSerial);
		System.out.println(
				"Total execution time matcher parallel " + timeMatcherParallel / 1000 + ", none " + timeSerial / 1000);

		assertTrue(executionsMatcherParallel >= executionSerial);
		System.out.println(tmatcherparallel / 1000 + " matcher sec vs  none paralel" + tpnoneparalel / 1000);

		assertTrue(tmatcherparallel < tpnoneparalel);

	}

	public Pair<Long, Integer> getResults(Map<String, Object> result) {
		long time = 0;
		int total = 0;
		System.out.println(result);
		for (Object matcher : ((List) result.get(TuningEngine.MATCHERS))) {
			Map propertiesOfMatcher = (Map) matcher;
			List<Map> configs = (List<Map>) propertiesOfMatcher.get(TuningEngine.CONFIGS);
			for (Map config : configs) {

				if (config != null && config.get(TuningEngine.TIME) != null)
					time += new Long(config.get(TuningEngine.TIME).toString());
				total += 1;
			}

		}
		System.out.println(total + " time sum " + time / 1000);
		return new Pair(time, total);
	}

	@Test
	public void testNavigate_SingleMatcher() throws IOException {
		String pathMegadiff = "/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded";
		File rootMegadiff = new File(pathMegadiff);
		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();

		boolean parallel = true;
		// Let's try with set 1
		int[] megadiff_ids = new int[] { 1 };
		// let's simply try 1 diff per group
		int limitDiffPerGroup = 1;
		reader.navigateMegaDiff("./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup, ASTMODE.GTSPOON,
				PARALLEL_EXECUTION.PROPERTY_LEVEL, new Matcher[] { new CompositeMatchers.ChangeDistiller() });

	}

	@Test
	public void testNavigate_SingleMatcherMatcherParallelTimeout() throws IOException {
		String pathMegadiff = "/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded";
		File rootMegadiff = new File(pathMegadiff);
		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();
		reader.setTimeOutSeconds(0);

		// Let's try with set 1
		int[] megadiff_ids = new int[] { 1 };
		// let's simply try 1 diff per group
		int limitDiffPerGroup = 1;
		reader.navigateMegaDiff("./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup, ASTMODE.GTSPOON,
				PARALLEL_EXECUTION.MATCHER_LEVEL, new Matcher[] { new CompositeMatchers.ChangeDistiller() });

	}

	@Test
	public void testNavigate_SingleMatcherMatcherParallelTimeoutAllMatchers() throws IOException {
		String pathMegadiff = "/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded";
		File rootMegadiff = new File(pathMegadiff);
		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();
		reader.setTimeOutSeconds(10);

		// Let's try with set 1
		int[] megadiff_ids = new int[] { 1 };
		// let's simply try 1 diff per group
		int limitDiffPerGroup = 1;
		reader.navigateMegaDiff("./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup, ASTMODE.GTSPOON,
				PARALLEL_EXECUTION.MATCHER_LEVEL);

	}

	@Test
	public void testTimeout() {

		File s = new File(
				"/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded/1/1_4be53ba794243204b135ea78a93ba3b5bb8afc31/CompositionScreen/1_4be53ba794243204b135ea78a93ba3b5bb8afc31_CompositionScreen_s.java");
		File t = new File(
				"/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded/1/1_4be53ba794243204b135ea78a93ba3b5bb8afc31/CompositionScreen/1_4be53ba794243204b135ea78a93ba3b5bb8afc31_CompositionScreen_s.java");

		TuningEngine reader = new TuningEngine();

		reader.analyzeDiff("1_4be53ba794243204b135ea78a93ba3b5bb8afc31", s, t, ASTMODE.GTSPOON,
				PARALLEL_EXECUTION.PROPERTY_LEVEL, new HashMap<String, Pair<Map, Map>>(), reader.getMatchers());
	}

	@Test
	public void testDomain() {

		IntParameterDomain intdomSz = new IntParameterDomain(ConfigurationOptions.GT_BUM_SZT, Integer.class, 1000, 100,
				2000, 100);

		Integer[] intervalInt = intdomSz.computeInterval();

		System.out.println(Arrays.toString(intervalInt));
		assertEquals(20, intervalInt.length);

		DoubleParameterDomain doubleDomainSMT = new DoubleParameterDomain(ConfigurationOptions.GT_BUM_SMT, Double.class,
				0.5, 0.1, 1.0, 0.1);

		Double[] intervalD = doubleDomainSMT.computeInterval();
		System.out.println(Arrays.toString(intervalD));
		assertEquals(10, intervalD.length);

		List<ParameterDomain> domains = new ArrayList<>();

		IntParameterDomain intdomMH = new IntParameterDomain(ConfigurationOptions.GT_STM_MH, Integer.class, 2, 1, 5, 1);

		Integer[] intervalIntMH = intdomMH.computeInterval();

		System.out.println(Arrays.toString(intervalIntMH));
		assertEquals(5, intervalIntMH.length);

		// Add all domains
		domains.add(intdomSz);
		domains.add(doubleDomainSMT);
		domains.add(intdomMH);

		// Cartesian

		TuningEngine engine = new TuningEngine();

		List<GumTreeProperties> combinations = engine.computeCartesianProduct(domains);

		int expectedCombinations = intervalInt.length * intervalD.length * intervalIntMH.length;

		assertEquals(20 * 10 * 5, expectedCombinations);

		assertEquals(expectedCombinations, combinations.size());

		System.out.println("Combinations " + combinations.size());
		int i = 0;
		for (GumTreeProperties gumTreeProperties : combinations) {
			System.out.println("Combination " + ++i + ": " + gumTreeProperties.get(ConfigurationOptions.GT_BUM_SMT)
					+ " " + gumTreeProperties.get(ConfigurationOptions.GT_BUM_SZT) + " "
					+ gumTreeProperties.get(ConfigurationOptions.GT_STM_MH) + "all "
					+ gumTreeProperties.getProperties());

		}

		IntParameterDomain intAnother = new IntParameterDomain(ConfigurationOptions.GT_STM_MH, Integer.class, 2,
				new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8 });

		assertEquals(8, intAnother.computeInterval().length);

	}

}
