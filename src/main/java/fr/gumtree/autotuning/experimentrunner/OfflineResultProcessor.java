package fr.gumtree.autotuning.experimentrunner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.entity.ResponseGlobalBestParameter;
import fr.gumtree.autotuning.entity.ResponseLocalBestParameter;
import fr.gumtree.autotuning.fitness.Fitness;
import fr.gumtree.autotuning.fitness.LengthEditScriptFitness;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.gumtree.ExecutionTPEConfiguration;
import fr.gumtree.autotuning.gumtree.ParametersResolvers;
import fr.gumtree.autotuning.outils.DatOutputEngine;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine;
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
public class OfflineResultProcessor {

	ExhaustiveEngine tuningEngine = new ExhaustiveEngine();

	public OfflineResultProcessor(ExhaustiveEngine tuningEngine) {
		super();
		this.tuningEngine = tuningEngine;
	}

	public OfflineResultProcessor() {
		super();
		this.tuningEngine = new ExhaustiveEngine();
	}

	public List<File> retrievePairsToAnalyze(File rootFolder, int maxPerProject) throws IOException {
		return retrievePairsToAnalyze(rootFolder, maxPerProject, true);
	}

	public List<File> retrievePairsToAnalyze(File rootFolder, int maxPerProject, boolean checkSize) throws IOException {
		List<File> collected = new ArrayList<File>();
		int filesWithZeroES = 0;
		DatOutputEngine outputengine = new DatOutputEngine(null);

		for (File subset : rootFolder.listFiles()) {
			if (subset.getName().equals(".DS_Store")) {
				continue;
			}
			int countFilesPerProject = 0;

			for (File diffFolder : subset.listFiles()) {

				if (diffFolder.getName().equals(".DS_Store")) {
					continue;
				}

				for (File filesFromDiff : diffFolder.listFiles()) {

					if (filesFromDiff.getName().equals(".DS_Store")) {
						continue;
					}

					if (filesFromDiff.getName().startsWith("result_") && filesFromDiff.getName().endsWith(".zip")) {

						if (checkSize) {
							if (!outputengine.isEmpty(filesFromDiff)) {

								collected.add(filesFromDiff);
								countFilesPerProject++;
							} else {
								// System.out.println("File with no change " + filesFromDiff.getAbsolutePath());
								filesWithZeroES++;
							}
						} else {
							collected.add(filesFromDiff);
							countFilesPerProject++;
						}

					}
				}

				if (countFilesPerProject == maxPerProject) {
					break;
				}

			}
		}
		System.out.println("Files with zero ES: " + filesWithZeroES);
		return collected;
	}

	public ResponseGlobalBestParameter summarizeBestGlobal(File rootFolder, Fitness fitnessFunction, METRIC metric,
			Boolean ignoreTimeout) throws IOException {

		ExhaustiveEngine exa = new ExhaustiveEngine();

		DatOutputEngine outputengine = new DatOutputEngine(null);

		ResultByConfig results = new ResultByConfig();

		for (File subset : rootFolder.listFiles()) {
			if (subset.getName().equals(".DS_Store")) {
				continue;
			}

			for (File diffFolder : subset.listFiles()) {

				if (diffFolder.getName().equals(".DS_Store")) {
					continue;
				}

				for (File filesFromDiff : diffFolder.listFiles()) {

					if (filesFromDiff.getName().equals(".DS_Store")) {
						continue;
					}

					if (filesFromDiff.getName().startsWith("result_") && filesFromDiff.getName().endsWith(".zip")) {

						outputengine.readZipAndAdd(results, filesFromDiff);

					}
				}

			}
		}

		ResponseGlobalBestParameter best = exa.summarizeResultsForGlobal(results, fitnessFunction, metric,
				ignoreTimeout);
		return best;
	}

	public ResponseGlobalBestParameter summarizeBestGlobal(List<File> toProcess, Fitness fitness, METRIC metric,
			Boolean ignoreTimeout) throws IOException {

		ExhaustiveEngine exa = new ExhaustiveEngine();

		DatOutputEngine outputengine = new DatOutputEngine(null);

		ResultByConfig results = new ResultByConfig();

		for (File filesFromDiff : toProcess) {

			outputengine.readZipAndAdd(results, filesFromDiff);

		}

		ResponseGlobalBestParameter best = exa.summarizeResultsForGlobal(results, fitness, metric, ignoreTimeout);
		return best;
	}

	public ResponseLocalBestParameter summarizeBestLocal(List<File> toProcess, METRIC metric, String target)
			throws IOException {

		System.out.println("Amount of data " + toProcess.size());
		// Counter of number of times the config is the best (the shortest)

		ResponseLocalBestParameter resultAllFiles = new ResponseLocalBestParameter();

		ExhaustiveEngine exa = new ExhaustiveEngine();

		int totalAnalyzed = 0;

		DatOutputEngine outputengine = new DatOutputEngine(null);

		Map<File, BestOfFile> resultPerFile = new HashMap<>();

		for (File filesFromDiff : toProcess) {

			ResultByConfig resultDiff = new ResultByConfig();

			outputengine.readZipAndAdd(resultDiff, filesFromDiff);

			BestOfFile bestdata = exa.analyzeLocalResult(resultDiff, target);

			updateGeneralResults(resultAllFiles, bestdata);

			resultPerFile.put(filesFromDiff, bestdata);

			totalAnalyzed++;

		}

		List<String> bests = exa.findTheBestLocal(resultAllFiles);
		resultAllFiles.setBests(bests);
		resultAllFiles.setNumberOfEvaluatedPairs(totalAnalyzed);
		resultAllFiles.setResultPerFile(resultPerFile);

		return resultAllFiles;
	}

	private void updateGeneralResults(ResponseLocalBestParameter resultAllFiles, BestOfFile bestdata) {
		// We increment the best counter with the best from the result

		for (String minConfig : bestdata.getCurrentMinConfigs()) {

			int count = resultAllFiles.getCountBestByConfigurations().containsKey(minConfig)
					? resultAllFiles.getCountBestByConfigurations().get(minConfig)
					: 0;
			resultAllFiles.getCountBestByConfigurations().put(minConfig, count + 1);

		}
	}

	public ResponseLocalBestParameter summarizeBestLocal(File rootFolder, METRIC metric, String target)
			throws IOException {

		ResponseLocalBestParameter resultAllFiles = new ResponseLocalBestParameter();
		ExhaustiveEngine exa = new ExhaustiveEngine();
		int totalAnalyzed = 0;
		DatOutputEngine outputengine = new DatOutputEngine(null);

		System.out.println("Folders " + Arrays.toString(rootFolder.listFiles()));
		Map<File, BestOfFile> resultPerFile = new HashMap<>();

		for (File subset : rootFolder.listFiles()) {

			if (subset.getName().equals(".DS_Store")) {
				continue;
			}

			for (File diffFolder : subset.listFiles()) {

				System.out.println(diffFolder.getName());
				if (diffFolder.getName().equals(".DS_Store")) {
					continue;
				}

				for (File filesFromDiff : diffFolder.listFiles()) {

					if (filesFromDiff.getName().equals(".DS_Store")) {
						continue;
					}

					if (filesFromDiff.getName().startsWith("result_") && filesFromDiff.getName().endsWith(".zip")) {

						ResultByConfig resultDiff = new ResultByConfig();

						outputengine.readZipAndAdd(resultDiff, filesFromDiff);

						BestOfFile bestdata = exa.analyzeLocalResult(resultDiff, target);

						updateGeneralResults(resultAllFiles, bestdata);

						resultPerFile.put(filesFromDiff, bestdata);

						totalAnalyzed++;
					}
				}

			}
		}

		// ResponseBestParameter best = exa.findTheBestLocal(totalAnalyzed, freqBest);
		List<String> bests = exa.findTheBestLocal(resultAllFiles);
		resultAllFiles.setBests(bests);
		resultAllFiles.setNumberOfEvaluatedPairs(totalAnalyzed);
		resultAllFiles.setResultPerFile(resultPerFile);

		return resultAllFiles;
	}

	public Path createFileWithDataToAnalyze(List<File> collected) throws IOException {
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

	public void runCrossValidationTPE(File fileResults, int maxPerFile, METRIC metric, Fitness fitnessFunction)
			throws Exception {

		OfflineResultProcessor runner = new OfflineResultProcessor();
		List<File> collected = runner.retrievePairsToAnalyze(fileResults, maxPerFile, true);
		System.out.println("Collected " + collected.size());
		int n = collected.size();
		int k = 10;

		File[] array = new File[collected.size()];
		collected.toArray(array);

		smile.validation.Bag[] cvresult = CrossValidation.of(n, k);

		List<ResultComparisonTwoConfigurations> allBestComparison = new ArrayList<>();

		System.out.println(cvresult);

		ExecutionTPEConfiguration configuration = new ExecutionTPEConfiguration(metric, null, fitnessFunction);

		LengthEditScriptFitness fitness = new LengthEditScriptFitness();

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

			ResponseBestParameter bestTPEfromTraining = tpe.computeBestGlobalCache(fileWithData.toFile(), fitness,
					configuration);

			System.out.println("--Global TPE TESTING: ");
			ResponseGlobalBestParameter bestFromTesting = runner.summarizeBestGlobal(listTesting, fitness, metric,
					false);

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

	public void analyzeLocal(File fileResults, int maxPerProject, METRIC metric) throws IOException {
		OfflineResultProcessor runner = new OfflineResultProcessor();
		boolean checkEDsize = true;
		List<File> collected = runner.retrievePairsToAnalyze(fileResults, maxPerProject, checkEDsize);

		ResponseLocalBestParameter best = runner.summarizeBestLocal(collected, metric,
				ParametersResolvers.defaultConfiguration);

		analyzeBestWithLocal(best);
	}

	/**
	 * This allows us to create Table from paper
	 * 
	 * @param fileResults
	 * @throws IOException
	 */
	public void analyzeLocalAndCompareWithGlobal(File fileResults, int maxPerProject, METRIC metric)
			throws IOException {
		OfflineResultProcessor runner = new OfflineResultProcessor();
		boolean checkEDsize = true;
		List<File> collectedToAnalyze = runner.retrievePairsToAnalyze(fileResults, maxPerProject, checkEDsize);

		Fitness fitnessFunction = new LengthEditScriptFitness();

		System.out.println("--Global TRANING: ");
		ResponseGlobalBestParameter bestFromTraining = runner.summarizeBestGlobal(collectedToAnalyze, fitnessFunction,
				metric, false);

		System.out.println("--Local vs Default: ");
		ResponseLocalBestParameter bestVsDefault = runner.summarizeBestLocal(collectedToAnalyze, metric,
				ParametersResolvers.defaultConfiguration);
		// inspectResults(best);
		analyzeBestWithLocal(bestVsDefault);

		System.out.println("--Local vs Best Global: ");
		List<String> bestGlobal = bestFromTraining.getAllBest();
		System.out.println("Total best (" + bestGlobal.size() + ")" + bestGlobal);

		int i = 1;
		for (String oneBest : bestGlobal) {
			System.out.println("\nanalyzing one best " + i + " " + oneBest);
			ResponseLocalBestParameter bestVsOneLocal = runner.summarizeBestLocal(collectedToAnalyze, metric, oneBest);
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

	public void analyzeBestWithLocal(ResponseLocalBestParameter best) {

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

	public void analyzeBestWithGlobal(ResponseGlobalBestParameter best) {

		ResultByConfig values = best.getValuesPerConfig();

		List<Double> perBest = new ArrayList<>();

		int casesImprovement = 0;
		int casesWorst = 0;
		int casesAllEquals = 0;

		int casesBalance = 0;

		for (String oneBestConfig : best.getAllBest()) {

			ResultComparisonTwoConfigurations result = compareConfigs(values, perBest, oneBestConfig,
					ParametersResolvers.defaultConfiguration);

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

	public List<ResultComparisonTwoConfigurations> analyzeBestCrossValidation(ResponseBestParameter bestTraining,
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
					oneBestConfigFromTraining, ParametersResolvers.defaultConfiguration);

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
		System.out.println("Percentage best " + perBest);

		System.out.println("Total from testing set: " + bestTesting.getNumberOfEvaluatedPairs());

		System.out.println("Cases a Best produces more improvement " + casesImprovement);
		System.out.println("Cases a Best produces more worst " + casesWorst);
		System.out.println("Cases all equals " + casesAllEquals);
		System.out.println("Cases perfect balance " + casesBalance);

		return outBestComparison;
	}

	public void runCrossValidationExahustive(File fileResults, int maxPerProject, METRIC metric) throws IOException {

		OfflineResultProcessor runner = new OfflineResultProcessor();

		List<File> collected = runner.retrievePairsToAnalyze(fileResults, maxPerProject, true);
		System.out.println("Collected " + collected.size());
		int n = collected.size();
		int k = 10;

		File[] array = new File[collected.size()];
		collected.toArray(array);

		smile.validation.Bag[] cvresult = CrossValidation.of(n, k);

		List<ResultComparisonTwoConfigurations> allBestComparison = new ArrayList<>();

		System.out.println(cvresult);

		List<Double> valuesBest = new ArrayList<>();
		List<Double> valuesDefault = new ArrayList<>();

		LengthEditScriptFitness fitness = new LengthEditScriptFitness();

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
			ResponseGlobalBestParameter bestFromTraining = runner.summarizeBestGlobal(listTraining, fitness, metric,
					false);
			System.out.println("--Global TESTING: ");
			ResponseGlobalBestParameter bestFromTesting = runner.summarizeBestGlobal(listTesting, fitness, metric,
					false);

			String bestConfig = bestFromTraining.getAllBest().get(0);
			valuesBest.addAll(bestFromTesting.getValuesPerConfig().get(bestConfig));
			valuesDefault.addAll(bestFromTesting.getValuesPerConfig().get(ParametersResolvers.defaultConfiguration));

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

	public ResultComparisonTwoConfigurations compareConfigs(ResultByConfig values, List<Double> perBest,
			String oneBestConfig, String defaultConfig) {

		System.out.println("Comparing: oneBestConfig " + oneBestConfig + " " + defaultConfig);

		List<Double> valuesOneBest = values.get(oneBestConfig);
		List<Double> valuesDefault = values.get(defaultConfig);

		List<Double> resultsComparison = new ArrayList<>();
		ResultComparisonTwoConfigurations result = null;
		if (valuesDefault.size() == valuesOneBest.size()) {

			System.out.println();
			for (int i = 0; i < valuesDefault.size(); i++) {
				Double iVD = valuesDefault.get(i);
				Double iVB = valuesOneBest.get(i);

				double diff = iVB - iVD;
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

	private void storeInFile(List<Double> valuesBest, String string) throws IOException {
		File f = new File("./out/values_" + string + ".txt");
		FileWriter fw = new FileWriter(f);
		for (Double v : valuesBest) {
			fw.write(String.valueOf(v));
			fw.write("\n");
		}

		fw.flush();
		fw.close();
		System.out.println("Store " + f.getAbsolutePath());
	}

}
