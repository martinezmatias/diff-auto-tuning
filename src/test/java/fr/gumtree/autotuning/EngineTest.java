package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
		reader.navigateMegaDiff("./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup, ASTMODE.GTSPOON, parallel);

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
		reader.navigateMegaDiff("./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup, ASTMODE.GTSPOON, parallel);

	}

	Matcher[] matchers = new Matcher[] {
			// Simple
			new CompositeMatchers.SimpleGumtree(),
			//
			new CompositeMatchers.ClassicGumtree(),
			//
			new CompositeMatchers.CompleteGumtreeMatcher(),
			//
			new CompositeMatchers.ChangeDistiller(),

			//
			// new CompositeMatchers.XyMatcher(),

	};

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
				ASTMODE.GTSPOON, parallel, matchers);

		assertNotNull(result);
		System.out.println(result);

	}

	@SuppressWarnings("deprecation")
	@Test
	public void testNavigate_SingleDiff_1_025055b307b6ef358d5153c7b50a1740e2b17f35() throws IOException {
		File rootMegadiff = new File(
				"/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded");
		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();

		String commitId = // "014af81101851b42c4c8b6216225c55d9d0b7ff3";//
				"025055b307b6ef358d5153c7b50a1740e2b17f35";
		// "010de14013c38b7f82e4755270e88a8249f3a825";
		// time 7.72 min file
		// nr_98_id_1_010de14013c38b7f82e4755270e88a8249f3a825_SimpleConveyer_GTSPOON.csv
		boolean parallel = true;

		int megadiff_id = 1;

		reader.setNrThreads(10);
		System.out.println(reader.getNrThreads());
		Map<String, Object> result = reader.navigateSingleDiffMegaDiff("./out/", rootMegadiff, megadiff_id, commitId,
				ASTMODE.GTSPOON,
				// ASTMODE.JDT,
				parallel, matchers);

		assertNotNull(result);
		Pair<Long, Integer> r1 = getResults(result);

		reader.setNrThreads(1);

		Map<String, Object> result2 = reader.navigateSingleDiffMegaDiff("./out/", rootMegadiff, megadiff_id, commitId,
				ASTMODE.GTSPOON, parallel, matchers);

		Pair<Long, Integer> r2 = getResults(result2);

		assertTrue(r1.first >= r2.first);
		assertTrue(r1.second >= r2.second);
	}

	public Pair<Long, Integer> getResults(Map<String, Object> result) {
		long time = 0;
		int total = 0;
		System.out.println(result);
		for (Object ob : ((List) result.get("MATCHERS"))) {
			Map dd = (Map) ob;
			List<Map> configs = (List<Map>) dd.get("r");
			for (Map config : configs) {

				if (config != null && config.get("TIME") != null)
					time += new Long(config.get("TIME").toString());
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
		reader.navigateMegaDiff("./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup, ASTMODE.GTSPOON, parallel,
				new Matcher[] { new CompositeMatchers.ChangeDistiller() });

	}

	@Test
	public void testTimeout() {

		File s = new File(
				"/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded/1/1_4be53ba794243204b135ea78a93ba3b5bb8afc31/CompositionScreen/1_4be53ba794243204b135ea78a93ba3b5bb8afc31_CompositionScreen_s.java");
		File t = new File(
				"/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded/1/1_4be53ba794243204b135ea78a93ba3b5bb8afc31/CompositionScreen/1_4be53ba794243204b135ea78a93ba3b5bb8afc31_CompositionScreen_s.java");

		TuningEngine reader = new TuningEngine();
		boolean parallel = false;
		reader.analyzeDiff("1_4be53ba794243204b135ea78a93ba3b5bb8afc31", s, t, ASTMODE.GTSPOON, parallel,
				new HashMap<String, Pair<Map, Map>>(), reader.getMatchers());
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
