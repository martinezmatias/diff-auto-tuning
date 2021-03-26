package fr.gumtree.autotuning.deprecated;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;

import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.utils.Pair;

import fr.gumtree.autotuning.entity.CaseResult;
import fr.gumtree.autotuning.entity.MatcherResult;
import fr.gumtree.autotuning.entity.SingleDiffResult;
import fr.gumtree.autotuning.experimentrunner.MegadiffRunner;
import fr.gumtree.autotuning.gumtree.ExecutionExhaustiveConfiguration;
import fr.gumtree.autotuning.outils.Constants;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine.PARALLEL_EXECUTION;
import fr.gumtree.autotuning.treebuilder.ITreeBuilder;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;

@Deprecated
@Ignore
public class EngineTest {

	final File rootMegadiff = new File("./examples/megadiff-sample");
	private ITreeBuilder treeBuilder = new SpoonTreeBuilder();

	@Test
	public void testNavigate() throws IOException {

		assertTrue(rootMegadiff.exists());

		ExhaustiveEngine reader = new ExhaustiveEngine();

		// Let's try with sets between 1 and 20
		int[] megadiff_ids = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
		// let's simply try 1 diff per group
		int limitDiffPerGroup = 1;

		MegadiffRunner runner = new MegadiffRunner(reader);

		ExecutionExhaustiveConfiguration configuration = new ExecutionExhaustiveConfiguration();

		configuration.setParalelisationMode(PARALLEL_EXECUTION.PROPERTY_LEVEL);

		runner.navigateMegaDiffAllMatchers(treeBuilder, "./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup,
				configuration);

	}

	@Test
	public void testNavigate_SingleDiff() throws IOException {

		assertTrue(rootMegadiff.exists());

		ExhaustiveEngine reader = new ExhaustiveEngine();

		MegadiffRunner runner = new MegadiffRunner(reader);

		// Let's try with set 1
		int[] megadiff_ids = new int[] { 1 };

		ExecutionExhaustiveConfiguration configuration = new ExecutionExhaustiveConfiguration();

		configuration.setParalelisationMode(PARALLEL_EXECUTION.PROPERTY_LEVEL);

		// let's simply try 1 diff per group
		int limitDiffPerGroup = 1;
		runner.navigateMegaDiffAllMatchers(treeBuilder, "./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup,
				configuration);

	}

	@Test
	public void testNavigate_SingleDiff_macher() throws IOException {

		assertTrue(rootMegadiff.exists());

		ExhaustiveEngine reader = new ExhaustiveEngine();
		MegadiffRunner runner = new MegadiffRunner(reader);

		// Let's try with set 1
		int[] megadiff_ids = new int[] { 1 };
		// let's simply try 1 diff per group
		int limitDiffPerGroup = 1;

		ExecutionExhaustiveConfiguration configuration = new ExecutionExhaustiveConfiguration();
		configuration.setNumberOfThreads(10);
		configuration.setParalelisationMode(PARALLEL_EXECUTION.MATCHER_LEVEL);

		runner.navigateMegaDiffAllMatchers(treeBuilder, "./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup,
				configuration);

	}

	@SuppressWarnings("deprecation")
	@Test
	public void testNavigate_CompareTimeouts() throws IOException {

		assertTrue(rootMegadiff.exists());
		ExecutionExhaustiveConfiguration configuration = new ExecutionExhaustiveConfiguration();
		ExhaustiveEngine reader = new ExhaustiveEngine();
		MegadiffRunner runner = new MegadiffRunner(reader);

		String commitId = // "014af81101851b42c4c8b6216225c55d9d0b7ff3";//
				"025055b307b6ef358d5153c7b50a1740e2b17f35";
		// "010de14013c38b7f82e4755270e88a8249f3a825";
		// time 7.72 min file
		// nr_98_id_1_010de14013c38b7f82e4755270e88a8249f3a825_SimpleConveyer_GTSPOON.csv

		int megadiff_id = 1;

		configuration.setNumberOfThreads(10);
		configuration.setParalelisationMode(PARALLEL_EXECUTION.PROPERTY_LEVEL);

		long tinit = (new Date()).getTime();

		CaseResult result = runner.runSingleDiffMegaDiff(treeBuilder, "./out/", rootMegadiff, megadiff_id, commitId,
				configuration);
		long tpropertyparalel = (new Date()).getTime() - tinit;

		assertNotNull(result);
		Pair<Long, Integer> r1 = computeTotalTime(result);

		configuration.setNumberOfThreads(10);
		configuration.setParalelisationMode(PARALLEL_EXECUTION.NONE);

		tinit = (new Date()).getTime();
		CaseResult result2 = runner.runSingleDiffMegaDiff(treeBuilder, "./out/", rootMegadiff, megadiff_id, commitId,
				configuration);
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

		configuration.setParalelisationMode(PARALLEL_EXECUTION.NONE);

		CaseResult result3 = runner.runSingleDiffMegaDiff(treeBuilder, "./out/", rootMegadiff, megadiff_id, commitId,
				configuration);
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

				if (config != null && config.get(Constants.TIME) != null)
					time += new Long(config.get(Constants.TIME).toString());
				total += 1;
			}

		}
		System.out.println(total + " time sum " + time / 1000);
		return new Pair(time, total);
	}

	@Test
	public void testNavigate_SingleMatcher() throws IOException {

		assertTrue(rootMegadiff.exists());

		ExhaustiveEngine reader = new ExhaustiveEngine();
		MegadiffRunner runner = new MegadiffRunner(reader);

		// Let's try with set 1
		int[] megadiff_ids = new int[] { 1 };
		// let's simply try 1 diff per group
		int limitDiffPerGroup = 1;

		ExecutionExhaustiveConfiguration configuration = new ExecutionExhaustiveConfiguration();

		configuration.setParalelisationMode(PARALLEL_EXECUTION.PROPERTY_LEVEL);

		runner.navigateMegaDiff(treeBuilder, "./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup, configuration,
				new Matcher[] { new CompositeMatchers.ChangeDistiller() });

	}

	@Test
	public void testNavigate_SingleMatcherMatcherParallelTimeout() throws IOException {

		assertTrue(rootMegadiff.exists());

		ExhaustiveEngine reader = new ExhaustiveEngine();

		ExecutionExhaustiveConfiguration configuration = new ExecutionExhaustiveConfiguration();
		configuration.setTimeOut(0);
		configuration.setParalelisationMode(PARALLEL_EXECUTION.MATCHER_LEVEL);

		MegadiffRunner runner = new MegadiffRunner(reader);
		// Let's try with set 1
		int[] megadiff_ids = new int[] { 1 };
		// let's simply try 1 diff per group
		int limitDiffPerGroup = 1;
		runner.navigateMegaDiff(treeBuilder, "./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup, configuration,
				new Matcher[] { new CompositeMatchers.ChangeDistiller() });

	}

	@Test
	public void testNavigate_SingleMatcherMatcherParallelTimeoutAllMatchers() throws IOException {

		assertTrue(rootMegadiff.exists());

		ExhaustiveEngine reader = new ExhaustiveEngine();
		ExecutionExhaustiveConfiguration configuration = new ExecutionExhaustiveConfiguration();
		configuration.setTimeOut(0);
		configuration.setParalelisationMode(PARALLEL_EXECUTION.MATCHER_LEVEL);

		MegadiffRunner runner = new MegadiffRunner(reader);

		// Let's try with set 1
		int[] megadiff_ids = new int[] { 1 };
		// let's simply try 1 diff per group
		int limitDiffPerGroup = 1;
		runner.navigateMegaDiffAllMatchers(treeBuilder, "./out/", rootMegadiff, megadiff_ids, 0, limitDiffPerGroup,
				configuration);

	}

	@Test
	public void testTimeout() {

		File s = new File(rootMegadiff.getAbsoluteFile()
				+ "/1/1_4be53ba794243204b135ea78a93ba3b5bb8afc31/CompositionScreen/1_4be53ba794243204b135ea78a93ba3b5bb8afc31_CompositionScreen_s.java");
		File t = new File(rootMegadiff.getAbsoluteFile()
				+ "/1/1_4be53ba794243204b135ea78a93ba3b5bb8afc31/CompositionScreen/1_4be53ba794243204b135ea78a93ba3b5bb8afc31_CompositionScreen_s.java");

		ExhaustiveEngine reader = new ExhaustiveEngine();
		ExecutionExhaustiveConfiguration configuration = new ExecutionExhaustiveConfiguration();
		configuration.setParalelisationMode(PARALLEL_EXECUTION.PROPERTY_LEVEL);

		CaseResult caseResult = reader.analyzeCase(treeBuilder, "1_4be53ba794243204b135ea78a93ba3b5bb8afc31", s, t,
				configuration, new HashMap<String, Pair<Map, Map>>(), reader.getMatchers());

		assertNotNull(caseResult);
		assertNull(caseResult.getFromException());
	}

}
