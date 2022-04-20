package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.entity.ResponseGlobalBestParameter;
import fr.gumtree.autotuning.entity.ResponseLocalBestParameter;
import fr.gumtree.autotuning.experimentrunner.OfflineResultProcessor;
import fr.gumtree.autotuning.fitness.Fitness;
import fr.gumtree.autotuning.fitness.LengthEditScriptFitness;
import fr.gumtree.autotuning.gumtree.ASTMODE;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.gumtree.ExecutionTPEConfiguration;
import fr.gumtree.autotuning.gumtree.ExecutionTPEConfiguration.TYPESearch;
import fr.gumtree.autotuning.searchengines.TPEEngine;
import smile.validation.Bag;
import smile.validation.CrossValidation;

/**
 * 
 * @author Matias Martinez
 *
 */
public class OffLineResultProcessorTest {

	OfflineResultProcessor processor = new OfflineResultProcessor();

	private static final String results_path = // "/Users/matias/develop/gt-tuning/results/resultsdatv3/";//
			"/Users/matias/develop/gt-tuning/results/resultsv4/";

	final String defaultConfiguration = "ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height";

	@Test
	public void testSmileFrameworkCrossValidation() throws IOException {
		int n = 13000;
		int k = 5;
		smile.validation.Bag[] cvresult = CrossValidation.of(n, k);

		assertEquals(k, cvresult.length);

		System.out.println(cvresult);
		for (int i = 0; i < cvresult.length; i++) {
			Bag bag = cvresult[i];
			System.out.println("sample (" + bag.samples.length + ")" + Arrays.toString(bag.samples));
			System.out.println("test (" + bag.oob.length + ")" + Arrays.toString(bag.oob));
		}

	}

	@Test
	public void testCrossValidationGlobalSpoon() throws IOException {

		File fileResults = new File(results_path + "/outDAT2_SPOON_onlyresult/");
		int maxPerProject = 5000;
		METRIC metric = METRIC.MEDIAN;
		processor.runCrossValidationExahustive(fileResults, maxPerProject, metric, "ExaSpoon_" + maxPerProject + "_");

	}

	@Test
	public void testCrossValidationGlobalJDT() throws IOException {

		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");

		int maxPerProject = 1000000;
		METRIC metric = METRIC.MEAN;
		processor.runCrossValidationExahustive(fileResults, maxPerProject, metric, "ExaJDT");

	}

	@Test
	public void testTPECrossValidationGlobalJDT() throws Exception {

		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");
		int maxPerProject = 100;
		METRIC metric = METRIC.MEAN;
		processor.runCrossValidationTPE(fileResults, maxPerProject, metric, new LengthEditScriptFitness());

	}

	@Test
	public void testTPECrossValidationGlobalSpoon() throws Exception {

		File fileResults = new File(results_path + "/outDAT2_Spoon_onlyresult/");
		int maxPerProject = 100;
		METRIC metric = METRIC.MEAN;
		processor.runCrossValidationTPE(fileResults, maxPerProject, metric, new LengthEditScriptFitness());

	}

	@Test
	public void testTPEJDTGlobal() throws Exception {

		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");

		OfflineResultProcessor runner = new OfflineResultProcessor();
		List<File> collected = runner.retrievePairsToAnalyze(fileResults, 100, true);

		Path fileWithData = processor.createFileWithDataToAnalyze(collected);

		TPEEngine tpe = new TPEEngine();

		ExecutionTPEConfiguration configuration = new ExecutionTPEConfiguration(METRIC.MEAN, ASTMODE.JDT,
				new LengthEditScriptFitness());
		configuration.setNumberOfAttempts(98);
		configuration.setSearchType(TYPESearch.TPE);

		LengthEditScriptFitness fitness = new LengthEditScriptFitness();
		ResponseBestParameter bestTPE = tpe.computeBestGlobalCache(fileWithData.toFile(), fitness, configuration);

		assertEquals(147.80, bestTPE.getMetricValue(), 0.1);
		System.out.println("Best TPE " + bestTPE);
	}

	@Test
	public void testRandomTPEJDTGlobal() throws Exception {

		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");

		OfflineResultProcessor runner = new OfflineResultProcessor();
		int maxPerProject = 100;
		List<File> collected = runner.retrievePairsToAnalyze(fileResults, maxPerProject, true);

		Path fileWithData = processor.createFileWithDataToAnalyze(collected);

		TPEEngine tpe = new TPEEngine();

		ExecutionTPEConfiguration configuration = new ExecutionTPEConfiguration(METRIC.MEAN, ASTMODE.JDT,
				new LengthEditScriptFitness());
		configuration.setNumberOfAttempts(98);
		configuration.setSearchType(TYPESearch.RANDOM);
		configuration.setRandomseed(12);
		LengthEditScriptFitness fitness = new LengthEditScriptFitness();
		ResponseBestParameter bestRandomTPE = tpe.computeBestGlobalCache(fileWithData.toFile(), fitness, configuration);

		assertEquals(149.17, bestRandomTPE.getMetricValue(), 0.1);
		System.out.println("Best TPE " + bestRandomTPE);
	}

	@Test
	public void testCollectFolders() throws IOException {

		File fileResults = new File(results_path + "/outDAT2_SPOON_onlyresult/");

		OfflineResultProcessor runner = new OfflineResultProcessor();
		List<File> collected = runner.retrievePairsToAnalyze(fileResults, 10);

		assertEquals(130, collected.size());
		assertEquals(10, collected.stream().filter(e -> e.getAbsolutePath().contains("git-struts")).count());
		assertEquals(10, collected.stream().filter(e -> e.getAbsolutePath().contains("git-tomcat")).count());
	}

	@Test
	@Deprecated // Use cross validation instead
	public void testExhaustiveGlobalSpoon() throws IOException {
		File fileResults = new File(results_path + "/outDAT2_SPOON_onlyresult/");
		Fitness fitnessFunction = new LengthEditScriptFitness();
		OfflineResultProcessor runner = new OfflineResultProcessor();

		boolean ignoreTimeout = true;
		ResponseGlobalBestParameter best = runner.summarizeBestGlobal(fileResults, fitnessFunction, METRIC.MEAN,
				ignoreTimeout);

		inspectResults(best);
		processor.analyzeBestWithGlobal(best);
	}

	@Test
	@Deprecated // Use cross validation instead
	public void testExhaustiveLocalSpoon() throws IOException {
		File fileResults = new File(results_path + "/outDAT2_SPOON_onlyresult/");

		OfflineResultProcessor runner = new OfflineResultProcessor();

		ResponseLocalBestParameter best = runner.summarizeBestLocal(fileResults, METRIC.MEAN, defaultConfiguration);

		// inspectResults(best);
		processor.analyzeBestWithLocal(best);
	}

	private void inspectResults(ResponseGlobalBestParameter best) {
		System.out.println("Best " + best);

		boolean configDefaultIsAnalyzed = best.getAllConfigs().contains(defaultConfiguration);
		System.out.println("Best is analyzed " + configDefaultIsAnalyzed + " best value: " + best.getMetricValue());
		assertTrue(configDefaultIsAnalyzed);

		boolean configDefaultIsBest = best.getAllBest().contains(defaultConfiguration);
		System.out.println("Best is default " + configDefaultIsBest + " value: "
				+ best.getMetricValueByConfiguration().get(defaultConfiguration));
	}

	@Test
	public void testExhaustiveGlobalJDT() throws IOException {
		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");

		OfflineResultProcessor runner = new OfflineResultProcessor();
		boolean ignoreTimeout = false;
		Fitness fitnessFunction = new LengthEditScriptFitness();

		ResponseGlobalBestParameter best = runner.summarizeBestGlobal(fileResults, fitnessFunction, METRIC.MEAN,
				ignoreTimeout);

		inspectResults(best);
		processor.analyzeBestWithGlobal(best);

	}

	@Test
	public void testExhaustiveLocalJDT() throws IOException {
		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");

		int maxPerProject = 100;
		processor.analyzeLocal(fileResults, maxPerProject, METRIC.MEAN);
	}

	@Test
	public void testExhaustiveLocalJDTComparisonGlobalForPaper() throws IOException {
		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");
		int maxPerProject = 100;
		processor.analyzeLocalAndCompareWithGlobal(fileResults, maxPerProject, METRIC.MEAN);
	}

	@Test
	public void testExhaustiveLocalSpoonComparisonGlobalForPaper() throws IOException {
		File fileResults = new File(results_path + "/outDAT2_SPOON_onlyresult/");
		int maxPerProject = 1000;
		processor.analyzeLocalAndCompareWithGlobal(fileResults, maxPerProject, METRIC.MEAN);
	}

}
