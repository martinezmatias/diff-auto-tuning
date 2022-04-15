package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;
import org.junit.Test;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.entity.ResponseGlobalBestParameter;
import fr.gumtree.autotuning.entity.ResponseLocalBestParameter;
import fr.gumtree.autotuning.experimentrunner.StructuredFolderfRunner;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.gumtree.ExecutionTPEConfiguration;
import fr.gumtree.autotuning.gumtree.ExecutionTPEConfiguration.TYPESearch;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine.BestOfFile;
import fr.gumtree.autotuning.searchengines.ResultByConfig;
import fr.gumtree.autotuning.searchengines.TPEEngine;
import smile.math.MathEx;
import smile.validation.Bag;
import smile.validation.CrossValidation;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ResultProcessorTest {

	private static final String results_path = // "/Users/matias/develop/gt-tuning/results/resultsdatv3/";//
			"/Users/matias/develop/gt-tuning/results/resultsdatv2";

	final String defaultConfiguration = "ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height";

	@Test
	public void testCrossValidation() throws IOException {
		int n = 13000;
		int k = 5;
		smile.validation.Bag[] cvresult = CrossValidation.of(n, k);

		// LOOCV.

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
		int maxPerProject = 1000;
		METRIC metric = METRIC.MEAN;
		runCrossValidationExahustive(fileResults, maxPerProject, metric);

	}

	@Test
	public void testCrossValidationGlobalJDT() throws IOException {

		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");

		int maxPerProject = 100;
		METRIC metric = METRIC.MEAN;
		runCrossValidationExahustive(fileResults, maxPerProject, metric);

	}

	@Test
	public void testTPECrossValidationGlobalJDT() throws Exception {

		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");
		int maxPerProject = 100;
		METRIC metric = METRIC.MEAN;
		runCrossValidationTPE(fileResults, maxPerProject, metric);

	}

	@Test
	public void testTPECrossValidationGlobalSpoon() throws Exception {

		File fileResults = new File(results_path + "/outDAT2_Spoon_onlyresult/");
		int maxPerProject = 100;
		METRIC metric = METRIC.MEAN;
		runCrossValidationTPE(fileResults, maxPerProject, metric);

	}

	@Test
	public void testTPEJDTGlobal() throws Exception {

		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");

		StructuredFolderfRunner runner = new StructuredFolderfRunner();
		List<File> collected = runner.retrievePairsToAnalyze(fileResults, 100, true);

		Path fileWithData = createFileWithDataToAnalyze(collected);

		TPEEngine tpe = new TPEEngine();

		ExecutionTPEConfiguration configuration = new ExecutionTPEConfiguration();
		configuration.setNumberOfAttempts(98);
		configuration.setSearchType(TYPESearch.TPE);
		ResponseBestParameter bestTPE = tpe.computeBestGlobalCache(fileWithData.toFile(), configuration);

		assertEquals(147.80, bestTPE.getMetricValue(), 0.1);
		System.out.println("Best TPE " + bestTPE);
	}

	@Test
	public void testRandomTPEJDTGlobal() throws Exception {

		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");

		StructuredFolderfRunner runner = new StructuredFolderfRunner();
		List<File> collected = runner.retrievePairsToAnalyze(fileResults, 100, true);

		Path fileWithData = createFileWithDataToAnalyze(collected);

		TPEEngine tpe = new TPEEngine();

		ExecutionTPEConfiguration configuration = new ExecutionTPEConfiguration();
		configuration.setNumberOfAttempts(98);
		configuration.setSearchType(TYPESearch.RANDOM);
		configuration.setRandomseed(12);
		ResponseBestParameter bestRandomTPE = tpe.computeBestGlobalCache(fileWithData.toFile(), configuration);

		assertEquals(149.17, bestRandomTPE.getMetricValue(), 0.1);
		System.out.println("Best TPE " + bestRandomTPE);
	}

	private Path createFileWithDataToAnalyze(List<File> collected) throws IOException {
		Path fileWithData = Files.createTempFile("files", ".txt");
		System.out.println(fileWithData);

		FileWriter fr = new FileWriter(fileWithData.toFile());

		for (File file : collected) {
			fr.write(file.getAbsolutePath());
			fr.write("\n");
		}
		fr.close();
		return fileWithData;
	}

	private void runCrossValidationTPE(File fileResults, int maxPerFile, METRIC metric) throws Exception {
		StructuredFolderfRunner runner = new StructuredFolderfRunner();
		List<File> collected = runner.retrievePairsToAnalyze(fileResults, maxPerFile, true);
		System.out.println("Collected " + collected.size());
		int n = collected.size();
		int k = 10;

		File[] array = new File[collected.size()];
		collected.toArray(array);

		smile.validation.Bag[] cvresult = CrossValidation.of(n, k);

		List<ResultComparisonTwoConfigurations> allBestComparison = new ArrayList<>();

		System.out.println(cvresult);

		for (int i = 0; i < cvresult.length; i++) {

			System.out.println("\n***********Fold :" + i + "/" + cvresult.length);
			Bag bag = cvresult[i];

			File[] training = MathEx.slice(array, bag.samples);
			File[] testing = MathEx.slice(array, bag.oob);

			List<File> listTraining = Arrays.asList(training);
			List<File> listTesting = Arrays.asList(testing);

			Path fileWithData = createFileWithDataToAnalyze(listTraining);

			System.out.println("sample (" + listTraining.size() + ")");
			System.out.println("test (" + listTesting.size() + ")");

			System.out.println("--Global TPE TRANING: ");
			// ResponseGlobalBestParameter bestFromTraining =
			// runner.summarizeBestGlobal(listTraining, metric, false);

			TPEEngine tpe = new TPEEngine();

			ResponseBestParameter bestTPEfromTraining = tpe.computeBestGlobalCache(fileWithData.toFile(),
					new ExecutionTPEConfiguration());

			System.out.println("--Global TPE TESTING: ");
			ResponseGlobalBestParameter bestFromTesting = runner.summarizeBestGlobal(listTesting, metric, false);

			List<ResultComparisonTwoConfigurations> foldBestComparison = analyzeBestCrossValidation(bestTPEfromTraining,
					bestFromTesting);
			allBestComparison.addAll(foldBestComparison);

		}
		System.out.println("****Final results");
		DescriptiveStatistics statsBest = new DescriptiveStatistics();
		DescriptiveStatistics statsWorst = new DescriptiveStatistics();
		DescriptiveStatistics statsEquals = new DescriptiveStatistics();

		for (ResultComparisonTwoConfigurations resultComparisonTwoConfigurations : allBestComparison) {
			statsBest.addValue(resultComparisonTwoConfigurations.getBetterBestPer());
			statsWorst.addValue(resultComparisonTwoConfigurations.getWorstBestPer());
			statsEquals.addValue(resultComparisonTwoConfigurations.getEqualsBestPer());
		}

		System.out.println(
				"Best " + statsBest.getMean() + " Worst " + statsWorst.getMean() + " Equals" + statsEquals.getMean());
	}

	private void runCrossValidationExahustive(File fileResults, int maxPerProject, METRIC metric) throws IOException {
		StructuredFolderfRunner runner = new StructuredFolderfRunner();
		List<File> collected = runner.retrievePairsToAnalyze(fileResults, maxPerProject, true);
		System.out.println("Collected " + collected.size());
		int n = collected.size();
		int k = 10;

		File[] array = new File[collected.size()];
		collected.toArray(array);

		smile.validation.Bag[] cvresult = CrossValidation.of(n, k);

		List<ResultComparisonTwoConfigurations> allBestComparison = new ArrayList<>();

		System.out.println(cvresult);

		List<Integer> valuesBest = new ArrayList<>();
		List<Integer> valuesDefault = new ArrayList<>();

		for (int i = 0; i < cvresult.length; i++) {

			System.out.println("\n***********Fold :" + i + "/" + cvresult.length);
			Bag bag = cvresult[i];

			File[] training = MathEx.slice(array, bag.samples);
			File[] testing = MathEx.slice(array, bag.oob);

			List<File> listTraining = Arrays.asList(training);
			List<File> listTesting = Arrays.asList(testing);

			System.out.println("sample (" + listTraining.size() + ")");
			System.out.println("test (" + listTesting.size() + ")");

			System.out.println("--Global TRANING: ");
			ResponseGlobalBestParameter bestFromTraining = runner.summarizeBestGlobal(listTraining, metric, false);
			System.out.println("--Global TESTING: ");
			ResponseGlobalBestParameter bestFromTesting = runner.summarizeBestGlobal(listTesting, metric, false);

			String bestConfig = bestFromTraining.getAllBest().get(0);
			valuesBest.addAll(bestFromTesting.getValuesPerConfig().get(bestConfig));
			valuesDefault.addAll(bestFromTesting.getValuesPerConfig().get(defaultConfiguration));

			List<ResultComparisonTwoConfigurations> foldBestComparison = analyzeBestCrossValidation(bestFromTraining,
					bestFromTesting);
			allBestComparison.addAll(foldBestComparison);

		}

		System.out.println("****Final results");
		DescriptiveStatistics statsBest = new DescriptiveStatistics();
		DescriptiveStatistics statsWorst = new DescriptiveStatistics();
		DescriptiveStatistics statsEquals = new DescriptiveStatistics();

		for (ResultComparisonTwoConfigurations resultComparisonTwoConfigurations : allBestComparison) {
			statsBest.addValue(resultComparisonTwoConfigurations.getBetterBestPer());
			statsWorst.addValue(resultComparisonTwoConfigurations.getWorstBestPer());
			statsEquals.addValue(resultComparisonTwoConfigurations.getEqualsBestPer());
		}

		storeInFile(valuesBest, "best");
		storeInFile(valuesDefault, "default");
		System.out.println(" " + valuesBest.size());
		System.out.println(" " + valuesDefault.size());
		System.out.println(
				"Best " + statsBest.getMean() + " Worst " + statsWorst.getMean() + " Equals" + statsEquals.getMean());
	}

	private void storeInFile(List<Integer> valuesBest, String string) throws IOException {
		File f = new File("./out/values_" + string + ".txt");
		FileWriter fw = new FileWriter(f);
		for (Integer v : valuesBest) {
			fw.write(String.valueOf(v));
			fw.write("\n");
		}

		fw.flush();
		fw.close();
		System.out.println("Store " + f.getAbsolutePath());
	}

	@Test
	public void testCollectFolders() throws IOException {

		File fileResults = new File(results_path + "/outDAT2_SPOON_onlyresult/");

		StructuredFolderfRunner runner = new StructuredFolderfRunner();
		List<File> collected = runner.retrievePairsToAnalyze(fileResults, 10);

		assertEquals(130, collected.size());
		assertEquals(10, collected.stream().filter(e -> e.getAbsolutePath().contains("git-struts")).count());
		assertEquals(10, collected.stream().filter(e -> e.getAbsolutePath().contains("git-tomcat")).count());
	}

	@Test
	@Deprecated // Use cross validation instead
	public void testExhaustiveGlobalSpoon() throws IOException {
		File fileResults = new File(results_path + "/outDAT2_SPOON_onlyresult/");

		StructuredFolderfRunner runner = new StructuredFolderfRunner();

		boolean ignoreTimeout = true;
		ResponseGlobalBestParameter best = runner.summarizeBestGlobal(fileResults, METRIC.MEAN, ignoreTimeout);

		inspectResults(best);
		analyzeBestWithGlobal(best);
	}

	@Test
	@Deprecated // Use cross validation instead
	public void testExhaustiveLocalSpoon() throws IOException {
		File fileResults = new File(results_path + "/outDAT2_SPOON_onlyresult/");

		StructuredFolderfRunner runner = new StructuredFolderfRunner();

		ResponseLocalBestParameter best = runner.summarizeBestLocal(fileResults, METRIC.MEAN, defaultConfiguration);

		// inspectResults(best);
		analyzeBestWithLocal(best);
	}

	@Test
	public void testExhaustiveGlobalJDT() throws IOException {
		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");

		StructuredFolderfRunner runner = new StructuredFolderfRunner();
		boolean ignoreTimeout = false;

		ResponseGlobalBestParameter best = runner.summarizeBestGlobal(fileResults, METRIC.MEAN, ignoreTimeout);

		inspectResults(best);
		analyzeBestWithGlobal(best);

	}

	@Test
	public void testExhaustiveLocalJDT() throws IOException {
		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");

		analyzeLocal(fileResults);
	}

	@Test
	public void testExhaustiveLocalJDTComparisonGlobalForPaper() throws IOException {
		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");
		int maxPerProject = 1000;
		analyzeLocalAndCompareWithGlobal(fileResults, maxPerProject);
	}

	@Test
	public void testExhaustiveLocalSpoonComparisonGlobalForPaper() throws IOException {
		File fileResults = new File(results_path + "/outDAT2_SPOON_onlyresult/");
		int maxPerProject = 1000;
		analyzeLocalAndCompareWithGlobal(fileResults, maxPerProject);
	}

	private void analyzeLocal(File fileResults) throws IOException {
		StructuredFolderfRunner runner = new StructuredFolderfRunner();
		boolean checkEDsize = true;
		List<File> collected = runner.retrievePairsToAnalyze(fileResults, 100, checkEDsize);

		ResponseLocalBestParameter best = runner.summarizeBestLocal(collected, METRIC.MEAN, defaultConfiguration);

		// inspectResults(best);
		analyzeBestWithLocal(best);
	}

	/**
	 * This allows us to create Table from paper
	 * 
	 * @param fileResults
	 * @throws IOException
	 */
	private void analyzeLocalAndCompareWithGlobal(File fileResults, int maxPerProject) throws IOException {
		StructuredFolderfRunner runner = new StructuredFolderfRunner();
		boolean checkEDsize = true;
		List<File> collectedToAnalyze = runner.retrievePairsToAnalyze(fileResults, maxPerProject, checkEDsize);

		//
		METRIC metric = METRIC.MEAN;
		System.out.println("--Global TRANING: ");
		ResponseGlobalBestParameter bestFromTraining = runner.summarizeBestGlobal(collectedToAnalyze, metric, false);

		System.out.println("--Local vs Default: ");
		ResponseLocalBestParameter bestVsDefault = runner.summarizeBestLocal(collectedToAnalyze, METRIC.MEAN,
				defaultConfiguration);
		// inspectResults(best);
		analyzeBestWithLocal(bestVsDefault);

		System.out.println("--Local vs Best Global: ");
		List<String> bestGlobal = bestFromTraining.getAllBest();
		System.out.println("Total best (" + bestGlobal.size() + ")" + bestGlobal);

		int i = 1;
		for (String oneBest : bestGlobal) {
			System.out.println("\nanalyzing one best " + i + " " + oneBest);
			ResponseLocalBestParameter bestVsOneLocal = runner.summarizeBestLocal(collectedToAnalyze, METRIC.MEAN,
					oneBest);
			// inspectResults(best);
			analyzeBestWithLocal(bestVsOneLocal);
			i++;
		}

	}

	public class ResultComparisonTwoConfigurations {

		public long equalsB = 0;
		public long betterBest = 0;
		public long worstBest = 0;
		public long total = 0;
		public String oneBestConfig;
		public String defaultConfig;

		public ResultComparisonTwoConfigurations(String oneBestConfig, String defaultConfig, long betterBest,
				long worstBest, long equalsB, long total) {
			super();
			this.oneBestConfig = oneBestConfig;
			this.defaultConfig = defaultConfig;
			this.betterBest = betterBest;
			this.worstBest = worstBest;
			this.equalsB = equalsB;
			this.total = total;
		}

		public long getEqualsB() {
			return equalsB;
		}

		public void setEqualsB(long equalsB) {
			this.equalsB = equalsB;
		}

		public long getBetterBest() {
			return betterBest;
		}

		public Double getBetterBestPer() {
			return (double) betterBest / (double) total;
		}

		public Double getEqualsBestPer() {
			return (double) equalsB / (double) total;
		}

		public Double getWorstBestPer() {
			return (double) worstBest / (double) total;
		}

		public void setBetterBest(long betterBest) {
			this.betterBest = betterBest;
		}

		public long getWorstBest() {
			return worstBest;
		}

		public void setWorstBest(long worstBest) {
			this.worstBest = worstBest;
		}

		public long getTotal() {
			return total;
		}

		public void setTotal(long total) {
			this.total = total;
		}

	}

	private void analyzeBestWithLocal(ResponseLocalBestParameter best) {

		int casesImprovement = 0;
		int casesWorst = 0;
		int casesAllEquals = 0;
		int total = 0;
		for (BestOfFile oneBestConfig : best.getResultPerFile().values()) {
			total++;
			// System.out.println("compare "+ oneBestConfig.getMinBest() + " " +
			// oneBestConfig.getMinDefault());
			if (oneBestConfig.getMinBest() < oneBestConfig.getMinDefault())
				casesImprovement++;

			else if (oneBestConfig.getMinBest() > oneBestConfig.getMinDefault())
				casesWorst++;

			else
				casesAllEquals++;

		}

		System.out.println(
				"Files local improve " + casesImprovement + " " + ((double) casesImprovement / (double) total));
		System.out.println("Files local worse " + casesWorst + " " + ((double) casesWorst / (double) total));// must
		// be
		// zero
		System.out.println("Cases  equals " + casesAllEquals + " " + ((double) casesAllEquals / (double) total));

	}

	private void analyzeBestWithGlobal(ResponseGlobalBestParameter best) {

		ResultByConfig values = best.getValuesPerConfig();

		List<Double> perBest = new ArrayList<>();

		int casesImprovement = 0;
		int casesWorst = 0;
		int casesAllEquals = 0;

		int casesBalance = 0;

		for (String oneBestConfig : best.getAllBest()) {

			ResultComparisonTwoConfigurations result = compareConfigs(values, perBest, oneBestConfig,
					defaultConfiguration);

			if (result != null) {

				if (result.betterBest > result.worstBest)
					casesImprovement++;

				if (result.betterBest < result.worstBest)
					casesWorst++;

				if (result.worstBest > 0 && result.worstBest == result.betterBest) {
					casesBalance++;
				}

				if (result.equalsB > 0 && result.worstBest == 0 && result.betterBest == 0) {
					casesAllEquals++;
				}
			}

		}
		System.out.println(perBest);

		System.out.println("Cases a Best produces more improvement " + casesImprovement);
		System.out.println("Cases a Best produces more worst " + casesWorst);
		System.out.println("Cases all equals " + casesAllEquals);
		System.out.println("Cases perfect balance " + casesBalance);

	}

	private List<ResultComparisonTwoConfigurations> analyzeBestCrossValidation(ResponseBestParameter bestTraining,
			ResponseGlobalBestParameter bestTesting) {

		List<Double> perBest = new ArrayList<>();
		List<ResultComparisonTwoConfigurations> outBestComparison = new ArrayList<>();
		int casesImprovement = 0;
		int casesWorst = 0;
		int casesAllEquals = 0;

		int casesBalance = 0;

		System.out.println("\nAnalyzing Improvement best from training (" + bestTraining.getAllBest().size() + ") "
				+ bestTraining.getAllBest());
		// We take the best from Training
		for (String oneBestConfigFromTraining : bestTraining.getAllBest()) {

			// We take the results from Testing
			ResultByConfig valuesFromTesting = bestTesting.getValuesPerConfig();

			ResultComparisonTwoConfigurations result = compareConfigs(valuesFromTesting, perBest,
					oneBestConfigFromTraining, defaultConfiguration);

			if (result != null) {

				outBestComparison.add(result);

				if (result.betterBest > result.worstBest)
					casesImprovement++;

				if (result.betterBest < result.worstBest)
					casesWorst++;

				if (result.worstBest > 0 && result.worstBest == result.betterBest) {
					casesBalance++;
				}

				if (result.equalsB > 0 && result.worstBest == 0 && result.betterBest == 0) {
					casesAllEquals++;
				}
			}

		}
		System.out.println(perBest);

		System.out.println("Total from testing set: " + bestTesting.getNumberOfEvaluatedPairs());

		System.out.println("Cases a Best produces more improvement " + casesImprovement);
		System.out.println("Cases a Best produces more worst " + casesWorst);
		System.out.println("Cases all equals " + casesAllEquals);
		System.out.println("Cases perfect balance " + casesBalance);

		return outBestComparison;
	}

	private ResultComparisonTwoConfigurations compareConfigs(ResultByConfig values, List<Double> perBest,
			String oneBestConfig, String defaultConfig) {

		System.out.println("oneBestConfig " + oneBestConfig + " " + defaultConfig);

		List<Integer> valuesOneBest = values.get(oneBestConfig);
		List<Integer> valuesDefault = values.get(defaultConfig);

		List<Integer> resultsComparison = new ArrayList<>();
		ResultComparisonTwoConfigurations result = null;
		if (valuesDefault.size() == valuesOneBest.size()) {

			System.out.println();
			for (int i = 0; i < valuesDefault.size(); i++) {
				Integer iVD = valuesDefault.get(i);
				Integer iVB = valuesOneBest.get(i);

				int diff = iVB - iVD;
				resultsComparison.add(diff);
			}

			long equalsB = resultsComparison.stream().filter(e -> e == 0).count();
			long betterBest = resultsComparison.stream().filter(e -> e < 0).count();
			long worstBest = resultsComparison.stream().filter(e -> e > 0).count();

			perBest.add(((double) betterBest / (double) valuesOneBest.size()));

			System.out.println("\n**Comparing best " + oneBestConfig + "with " + defaultConfig + "Samples to analyze "
					+ valuesDefault.size());
			System.out.println("#### Best " + betterBest + ", worst " + worstBest + ", equals " + equalsB);
			System.out.println("-");

			result = new ResultComparisonTwoConfigurations(oneBestConfig, defaultConfig, betterBest, worstBest, equalsB,
					valuesOneBest.size());

		} else {
			System.err.println("Error different size");
		}
		return result;
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

	public static void main(String[] args) throws IOException {
		ResultProcessor p = new ResultProcessor();
		p.process();

	}

}
