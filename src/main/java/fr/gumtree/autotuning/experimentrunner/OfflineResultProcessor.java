package fr.gumtree.autotuning.experimentrunner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
import fr.gumtree.autotuning.searchengines.MapList;
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

	String outFolder = Long.toString((new Date()).getTime());

	String outDir = "./out/" + outFolder + File.separator;

	Map<File, ResultByConfig> cacheResultsByConfigs = new HashMap<>();

	public OfflineResultProcessor(ExhaustiveEngine tuningEngine) {
		super();
		new File(outDir).mkdirs();
		this.tuningEngine = tuningEngine;
	}

	public OfflineResultProcessor() {
		super();
		new File(outDir).mkdirs();
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

		// System.out.println("Amount of data " + toProcess.size());
		// Counter of number of times the config is the best (the shortest)

		ResponseLocalBestParameter resultAllFiles = new ResponseLocalBestParameter();

		resultAllFiles.setTargetConfig(target);

		ExhaustiveEngine exa = new ExhaustiveEngine();

		int totalAnalyzed = 0;

		DatOutputEngine outputengine = new DatOutputEngine(null);

		Map<File, BestOfFile> resultPerFile = new HashMap<>();

		for (File filesFromDiff : toProcess) {

			ResultByConfig resultDiff = new ResultByConfig();

			if (cacheResultsByConfigs.containsKey(filesFromDiff)) {

				resultDiff = cacheResultsByConfigs.get(filesFromDiff);

			} else {

				resultDiff = new ResultByConfig();
				outputengine.readZipAndAdd(resultDiff, filesFromDiff);
				cacheResultsByConfigs.put(filesFromDiff, resultDiff);
			}

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
	@Deprecated // We integrate it with Global exaustive analysis
	public void analyzeLocalAndCompareWithGlobal(File fileResults, int maxPerProject, METRIC metric)
			throws IOException {
		OfflineResultProcessor runner = new OfflineResultProcessor();
		boolean checkEDsize = true;
		List<File> collectedToAnalyze = runner.retrievePairsToAnalyze(fileResults, maxPerProject, checkEDsize);

		Fitness fitnessFunction = new LengthEditScriptFitness();

		System.out.println("\n--Global TRANING: ");
		ResponseGlobalBestParameter bestFromTraining = runner.summarizeBestGlobal(collectedToAnalyze, fitnessFunction,
				metric, false);

		System.out.println("\n--Local vs Default: ");
		ResponseLocalBestParameter bestVsDefault = runner.summarizeBestLocal(collectedToAnalyze, metric,
				ParametersResolvers.defaultConfiguration);

		analyzeBestWithLocal(bestVsDefault);

		System.out.println("\n--Local vs Best Global: ");
		List<String> bestGlobal = bestFromTraining.getAllBest();
		System.out.println("Total best (" + bestGlobal.size() + ")" + bestGlobal);
		int i = 1;
		for (String oneBest : bestGlobal) {
			System.out.println("\nanalyzing one best " + i + " " + oneBest);
			ResponseLocalBestParameter bestLocalvsOneBestGlobal = runner.summarizeBestLocal(collectedToAnalyze, metric,
					oneBest);
			// inspectResults(best);
			analyzeBestWithLocal(bestLocalvsOneBestGlobal);
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

		public String detailsRun = "";

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

		public String getDetailsRun() {
			return detailsRun;
		}

		public void setDetailsRun(String detailsRun) {
			this.detailsRun = detailsRun;
		}

	}

	public ResultComparisonTwoConfigurations analyzeBestWithLocal(ResponseLocalBestParameter best) {

		int casesImprovement = 0;
		int casesWorst = 0;
		int casesAllEquals = 0;
		int total = 0;
		for (BestOfFile oneBestConfig : best.getResultPerFile().values()) {
			total++;

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

		ResultComparisonTwoConfigurations comparisons = new ResultComparisonTwoConfigurations("local",
				best.getTargetConfig(), casesImprovement, casesWorst, casesAllEquals, total);
		return comparisons;

		// fw.write(String.format("%i,%s,%d,%d,%d,%d,%f,%f,%f\n", i, oneBest,
		// casesImprovement, casesWorst, casesAllEquals,
		// (total), ((double) casesImprovement / (double) total), ((double) casesWorst /
		// (double) total),
		// ((double) casesAllEquals / (double) total)));

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

	public void runCrossValidationExahustive(File fileResults, int maxPerProject, METRIC metric, String outputKey)
			throws IOException {

		OfflineResultProcessor runner = new OfflineResultProcessor();

		List<File> collected = runner.retrievePairsToAnalyze(fileResults, maxPerProject, true);
		System.out.println("Collected " + collected.size());
		int n = collected.size();
		int k = 10;

		File[] array = new File[collected.size()];
		collected.toArray(array);

		smile.validation.Bag[] cvresult = CrossValidation.of(n, k);

		List<ResultComparisonTwoConfigurations> allBestGlobalComparison = new ArrayList<>();
		List<ResultComparisonTwoConfigurations> allLocalVsBestGlobalComparison = new ArrayList<>();
		List<ResultComparisonTwoConfigurations> allLocalVsDefaultComparison = new ArrayList<>();

		System.out.println(cvresult);

		List<Double> valuesBest = new ArrayList<>();
		List<Double> valuesDefault = new ArrayList<>();

		LengthEditScriptFitness fitness = new LengthEditScriptFitness();

		File fouttraining = new File(outDir + outputKey + "_info_training_best.csv");
		FileWriter fwTraining = new FileWriter(fouttraining);

		File foutTesting = new File(outDir + outputKey + "_info_testing_best.csv");
		FileWriter fwTesting = new FileWriter(foutTesting);

		MapList<String, Double> bestAllFolds = new MapList<>();

		// For each fold
		for (int i = 0; i < cvresult.length; i++) {

			System.out.println("\n***********Fold :" + i + "/" + cvresult.length);
			Bag bag = cvresult[i];

			File[] training = MathEx.slice(array, bag.samples);
			File[] testing = MathEx.slice(array, bag.oob);

			List<File> listTraining = Arrays.asList(training);
			List<File> listTesting = Arrays.asList(testing);

			storesFiles(listTraining, i, "training", outputKey);
			storesFiles(listTesting, i, "testing", outputKey);

			System.out.println("sample (" + listTraining.size() + ")");
			System.out.println("test (" + listTesting.size() + ")");

			System.out.println("--Global TRANING: ");
			ResponseGlobalBestParameter bestFromTraining = runner.summarizeBestGlobal(listTraining, fitness, metric,
					false);
			System.out.println("--Global TESTING: ");
			ResponseGlobalBestParameter bestFromTesting = runner.summarizeBestGlobal(listTesting, fitness, metric,
					false);

			// Just the values of one of the best to compute the pvalue
			String bestConfig = bestFromTraining.getAllBest().get(0);
			valuesBest.addAll(bestFromTesting.getValuesPerConfig().get(bestConfig));
			valuesDefault.addAll(bestFromTesting.getValuesPerConfig().get(ParametersResolvers.defaultConfiguration));

			// For each of the best from Training, save data
			int posInTraining = 0;
			int posInTrain = 0;
			for (String oneBest : bestFromTraining.getAllBest()) {
				String summary = createLineSummary(i, bestFromTraining, posInTraining, oneBest);
				fwTraining.write(summary);

				summary = createLineSummary(i, bestFromTraining, posInTrain, oneBest);
				fwTesting.write(summary);

				// We put the value from testing
				bestAllFolds.add(oneBest, bestFromTesting.getMetricValueByConfiguration().get(oneBest));
				posInTraining++;
				posInTrain++;
			}

			fwTraining.flush();
			fwTesting.flush();

			// Now, compute performance of Best on training

			List<ResultComparisonTwoConfigurations> foldBestComparison = analyzeBestCrossValidation(bestFromTraining,
					bestFromTesting);
			for (ResultComparisonTwoConfigurations rc : foldBestComparison) {
				rc.setDetailsRun("Fold_" + i);
				allBestGlobalComparison.add(rc);
			}

			/// Now local analysis

			/// Comparing best local Vs default
			System.out.println("\n--Local vs Default: (on testing)");
			ResponseLocalBestParameter bestVsDefault = runner.summarizeBestLocal(listTesting, metric,
					ParametersResolvers.defaultConfiguration);

			ResultComparisonTwoConfigurations comparisonLocalDefault = analyzeBestWithLocal(bestVsDefault);
			comparisonLocalDefault.setDetailsRun("Fold_" + i);
			allLocalVsDefaultComparison.add(comparisonLocalDefault);

			/// Comparing best local Vs default

			System.out.println("\n--Local vs Best Global: ");
			List<String> bestGlobal = bestFromTraining.getAllBest();
			System.out.println("Total best from Global (" + bestGlobal.size() + ")" + bestGlobal);
			int ibestLocal = 1;
			for (String oneBestGlobal : bestGlobal) {
				System.out.println(
						"\n" + ibestLocal + "/" + bestGlobal.size() + ") analyzing one best " + " " + oneBestGlobal);
				ResponseLocalBestParameter bestVsOneLocal = runner.summarizeBestLocal(listTesting, metric,
						oneBestGlobal);
				ResultComparisonTwoConfigurations resultLocalComparison = analyzeBestWithLocal(bestVsOneLocal);
				resultLocalComparison.setDetailsRun("Fold_" + i);
				ibestLocal++;

				allLocalVsBestGlobalComparison.add(resultLocalComparison);

			}
			// To avoid heap explosion
			this.cacheResultsByConfigs.clear();

		}

		fwTraining.close();
		fwTesting.close();

		System.out.println("****Final results");

		/// Saving locals
		File fcomparison = new File(outDir + outputKey + "_comparison_global_best_default_testing.csv");
		FileWriter fwcomp = new FileWriter(fcomparison);

		File fsum = new File(outDir + outputKey + "_summary_global_best_default_testing.csv");
		FileWriter fwSum = new FileWriter(fsum);

		saveMeasuresOnFile(allBestGlobalComparison, fwcomp, fwSum);

		fwSum.close();
		fwcomp.close();

		File fLocalVsBest = new File(outDir + outputKey + "comparison_local_vs_best_testing.csv");
		FileWriter fwLocalVsBest = new FileWriter(fLocalVsBest);

		File fsuml = new File(outDir + outputKey + "_summary_local_vs_best_testing.csv");
		FileWriter fwSuml = new FileWriter(fsuml);

		saveMeasuresOnFile(allLocalVsBestGlobalComparison, fwLocalVsBest, fwSuml);

		fwLocalVsBest.close();
		fwSuml.close();

		File fLocalVsDefault = new File(outDir + outputKey + "comparison_local_vs_defaul_testing.csv");
		FileWriter fwLocalVsDefault = new FileWriter(fLocalVsDefault);

		File fsumld = new File(outDir + outputKey + "_summary_local_vs_default_testing.csv");
		FileWriter fwSumld = new FileWriter(fsumld);

		saveMeasuresOnFile(allLocalVsBestGlobalComparison, fwLocalVsDefault, fwSumld);

		fwLocalVsDefault.close();
		fwSumld.close();

		storeInFile(valuesBest, "best", outputKey);
		storeInFile(valuesDefault, "default", outputKey);
		System.out.println(" " + valuesBest.size());
		System.out.println(" " + valuesDefault.size());

		int maxBest = bestAllFolds.keySet().stream().map(e -> bestAllFolds.get(e).size()).distinct()
				.max(Comparator.comparing(Integer::valueOf)).get();

		List<String> mostRecurrentBest = bestAllFolds.keySet().stream()
				.filter(e -> bestAllFolds.get(e).size() == maxBest).collect(Collectors.toList());
		System.out.println("Config with max number best " + maxBest);

		System.out.println(mostRecurrentBest.size() + " " + mostRecurrentBest);
		storeBestFromAllTraining(mostRecurrentBest, outputKey);

	}

	private void saveMeasuresOnFile(List<ResultComparisonTwoConfigurations> allBestGlobalComparison, FileWriter fwcomp,
			FileWriter fwSum) throws IOException {
		DescriptiveStatistics statsBest = new DescriptiveStatistics();
		DescriptiveStatistics statsWorst = new DescriptiveStatistics();
		DescriptiveStatistics statsEquals = new DescriptiveStatistics();

		// For each of the best from Global, save data
		for (ResultComparisonTwoConfigurations resultComparisonTwoConfigurations : allBestGlobalComparison) {
			statsBest.addValue(resultComparisonTwoConfigurations.getBetterBestPer());
			statsWorst.addValue(resultComparisonTwoConfigurations.getWorstBestPer());
			statsEquals.addValue(resultComparisonTwoConfigurations.getEqualsBestPer());

			fwcomp.write(String.format("%s,%s,%s,%d,%d,%d,%f,%f,%f,\n",
					resultComparisonTwoConfigurations.getDetailsRun(), resultComparisonTwoConfigurations.defaultConfig,
					resultComparisonTwoConfigurations.oneBestConfig, resultComparisonTwoConfigurations.getBetterBest(),
					resultComparisonTwoConfigurations.getEqualsB(), resultComparisonTwoConfigurations.getWorstBest(),
					resultComparisonTwoConfigurations.getBetterBestPer(),
					resultComparisonTwoConfigurations.getEqualsBestPer(),
					resultComparisonTwoConfigurations.getWorstBestPer()));
		}

		System.out.println("All Best comparisons " + allBestGlobalComparison.size());

		String sumLine = "Mean Best " + statsBest.getMean() + " Worst " + statsWorst.getMean() + " Equals "
				+ statsEquals.getMean() + "Total  Mean"
				+ (statsBest.getMean() + statsWorst.getMean() + statsEquals.getMean()) + "\n";

		String sumLine2 = "Median Best " + statsBest.getPercentile(50) + " Worst " + statsWorst.getPercentile(50)
				+ " Equals " + statsEquals.getPercentile(50) + "\n";

		String sumLine3 = "Stdev Best " + statsBest.getStandardDeviation() + " Worst "
				+ statsWorst.getStandardDeviation() + " Equals " + statsEquals.getStandardDeviation() + "\n";

		System.out.println(sumLine);

		// Storing summary:
		fwSum.write(sumLine);
		fwSum.write(sumLine2);
		fwSum.write(sumLine3);
	}

	private String createLineSummary(int i, ResponseGlobalBestParameter bestFromTraining, int posInTraining,
			String oneBest) {
		String summary = String.format("%d,%d,%f,%s,%s\n", i, posInTraining,
				bestFromTraining.getMetricValueByConfiguration().get(oneBest).doubleValue(), oneBest,
				bestFromTraining.getValuesPerConfig().get(oneBest).stream().map(e -> (e != Double.MAX_VALUE) ? e : 9988)
						.map(d -> d.toString()).collect(Collectors.joining(",")));
		return summary;
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

	private void storeBestFromAllTraining(List<String> valuesBest, String outputKey) throws IOException {
		File f = new File(outDir + outputKey + "_bestConfigs_" + ".txt");
		FileWriter fw = new FileWriter(f);
		for (String v : valuesBest) {
			fw.write(String.valueOf(v));
			fw.write("\n");
		}

		fw.flush();
		fw.close();
		System.out.println("Store " + f.getAbsolutePath());
	}

	private void storeInFile(List<Double> valuesBest, String string, String outputKey) throws IOException {
		File f = new File(outDir + outputKey + "_values_" + string + ".txt");
		FileWriter fw = new FileWriter(f);
		for (Double v : valuesBest) {
			fw.write(String.valueOf(v));
			fw.write("\n");
		}

		fw.flush();
		fw.close();
		System.out.println("Store " + f.getAbsolutePath());
	}

	private void storesFiles(List<File> listTraining, int i, String string, String outputKey) throws IOException {
		File f = new File(outDir + outputKey + "_files_" + string + i + ".txt");
		FileWriter fw = new FileWriter(f);
		for (File v : listTraining) {
			fw.write(v.getAbsolutePath());
			fw.write("\n");
		}

		fw.flush();
		fw.close();
		System.out.println("Store  " + f.getAbsolutePath());

	}

}
