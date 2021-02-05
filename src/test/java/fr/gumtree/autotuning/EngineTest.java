package fr.gumtree.autotuning;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.utils.Pair;

import fr.gumtree.autotuning.TuningEngine.PARALLEL_EXECUTION;
import fr.gumtree.autotuning.entity.CaseResult;
import fr.gumtree.autotuning.entity.MatcherResult;
import fr.gumtree.autotuning.entity.SingleDiffResult;

public class EngineTest {

	final File rootMegadiff = new File("./examples/megadiff-sample");

	@Test
	public void testNavigate() throws IOException {

		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();

		boolean parallel = true;
		// Let's try with sets between 1 and 20
		int[] megadiff_ids = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
		// let's simply try 1 diff per group
		int limitDiffPerGroup = 1;
		reader.navigateMegaDiffAllMatchers("./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup,
				PARALLEL_EXECUTION.PROPERTY_LEVEL);

	}

	@Test
	public void testNavigate_SingleDiff() throws IOException {

		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();

		boolean parallel = true;
		// Let's try with set 1
		int[] megadiff_ids = new int[] { 1 };
		// let's simply try 1 diff per group
		int limitDiffPerGroup = 1;
		reader.navigateMegaDiffAllMatchers("./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup,
				PARALLEL_EXECUTION.PROPERTY_LEVEL);

	}

	@Test
	public void testNavigate_SingleDiff_1_831e3b() throws IOException {

		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();

		String commitId = "831e3b0420e70f7c2695cb248dd8b488b1fd84b7";

		boolean parallel = false;

		int megadiff_id = 1;

		CaseResult result = reader.navigateSingleDiffMegaDiff("./out/", rootMegadiff, megadiff_id, commitId,
				PARALLEL_EXECUTION.PROPERTY_LEVEL);

		assertNotNull(result);
		System.out.println(result);

	}

	/// Diff in one annotation
	@Test
	public void testNavigate_SingleDiff_1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e() throws IOException {

		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();

		String commitId = "02f3fd442349d4e7fdfc9c31a82bb1638db8495e";

		boolean parallel = false;

		int megadiff_id = 1;

		CaseResult result = reader.navigateSingleDiffMegaDiff("./out/", rootMegadiff, megadiff_id, commitId,
				PARALLEL_EXECUTION.NONE);

		assertNotNull(result);
		System.out.println(result);

	}

	@Test
	public void testNavigate_SingleDiff_1_() throws IOException {

		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();

		String commitId = "0a664d752c4b0e5a7fb6f06d005181a0c9dc2905";

		int megadiff_id = 1;

		CaseResult result = reader.navigateSingleDiffMegaDiff("./out/", rootMegadiff, megadiff_id, commitId,
				PARALLEL_EXECUTION.NONE);

		assertNotNull(result);
		System.out.println(result);

	}

	@SuppressWarnings("deprecation")
	@Test
	public void testNavigate_CompareTimeouts() throws IOException {

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

		CaseResult result = reader.navigateSingleDiffMegaDiff("./out/", rootMegadiff, megadiff_id, commitId,
				PARALLEL_EXECUTION.PROPERTY_LEVEL);
		long tpropertyparalel = (new Date()).getTime() - tinit;

		assertNotNull(result);
		Pair<Long, Integer> r1 = computeTotalTime(result);

		reader.setNrThreads(1);

		tinit = (new Date()).getTime();
		CaseResult result2 = reader.navigateSingleDiffMegaDiff("./out/", rootMegadiff, megadiff_id, commitId,
				PARALLEL_EXECUTION.NONE);
		long tpnoneparalel = (new Date()).getTime() - tinit;

		Pair<Long, Integer> r2 = computeTotalTime(result2);

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
		CaseResult result3 = reader.navigateSingleDiffMegaDiff("./out/", rootMegadiff, megadiff_id, commitId,
				PARALLEL_EXECUTION.NONE);
		long tmatcherparallel = (new Date()).getTime() - tinit;

		Pair<Long, Integer> r3 = computeTotalTime(result3);

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

	/**
	 * Computes the number of configurations executed and the total time of those
	 * configurations.
	 * 
	 * @param result
	 * @return
	 */
	public Pair<Long, Integer> computeTotalTime(CaseResult result) {
		long time = 0;
		int total = 0;
		System.out.println(result);
		for (MatcherResult propertiesOfMatcher : (result.getResultByMatcher().values())) {

			List<SingleDiffResult> configs = propertiesOfMatcher.getAlldiffresults();
			for (SingleDiffResult config : configs) {

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

		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();

		boolean parallel = true;
		// Let's try with set 1
		int[] megadiff_ids = new int[] { 1 };
		// let's simply try 1 diff per group
		int limitDiffPerGroup = 1;
		reader.navigateMegaDiff("./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup,
				PARALLEL_EXECUTION.PROPERTY_LEVEL, new Matcher[] { new CompositeMatchers.ChangeDistiller() });

	}

	@Test
	public void testNavigate_SingleMatcherMatcherParallelTimeout() throws IOException {

		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();
		reader.setTimeOutSeconds(0);

		// Let's try with set 1
		int[] megadiff_ids = new int[] { 1 };
		// let's simply try 1 diff per group
		int limitDiffPerGroup = 1;
		reader.navigateMegaDiff("./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup,
				PARALLEL_EXECUTION.MATCHER_LEVEL, new Matcher[] { new CompositeMatchers.ChangeDistiller() });

	}

	@Test
	public void testNavigate_SingleMatcherMatcherParallelTimeoutAllMatchers() throws IOException {

		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();
		reader.setTimeOutSeconds(10);

		// Let's try with set 1
		int[] megadiff_ids = new int[] { 1 };
		// let's simply try 1 diff per group
		int limitDiffPerGroup = 1;
		reader.navigateMegaDiffAllMatchers("./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup,
				PARALLEL_EXECUTION.MATCHER_LEVEL);

	}

	@Test
	public void testTimeout() {

		File s = new File(rootMegadiff.getAbsoluteFile()
				+ "/1/1_4be53ba794243204b135ea78a93ba3b5bb8afc31/CompositionScreen/1_4be53ba794243204b135ea78a93ba3b5bb8afc31_CompositionScreen_s.java");
		File t = new File(rootMegadiff.getAbsoluteFile()
				+ "/1/1_4be53ba794243204b135ea78a93ba3b5bb8afc31/CompositionScreen/1_4be53ba794243204b135ea78a93ba3b5bb8afc31_CompositionScreen_s.java");

		TuningEngine reader = new TuningEngine();

		CaseResult caseResult = reader.analyzeCase("1_4be53ba794243204b135ea78a93ba3b5bb8afc31", s, t,
				PARALLEL_EXECUTION.PROPERTY_LEVEL, new HashMap<String, Pair<Map, Map>>(), reader.getMatchers());

		assertNotNull(caseResult);
		assertNull(caseResult.getFromException());
	}

}
