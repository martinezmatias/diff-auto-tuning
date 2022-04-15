package fr.gumtree.autotuning.experimentrunner;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.gumtreediff.matchers.Matcher;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.entity.ResponseGlobalBestParameter;
import fr.gumtree.autotuning.entity.ResponseLocalBestParameter;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.outils.DatOutputEngine;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine.BestOfFile;
import fr.gumtree.autotuning.searchengines.ResultByConfig;
import fr.gumtree.autotuning.treebuilder.ITreeBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class StructuredFolderfRunner {

	ExhaustiveEngine tuningEngine = new ExhaustiveEngine();

	public StructuredFolderfRunner(ExhaustiveEngine tuningEngine) {
		super();
		this.tuningEngine = tuningEngine;
	}

	public StructuredFolderfRunner() {
		super();
		this.tuningEngine = new ExhaustiveEngine();
	}

	/**
	 * All matches by default
	 */
	public void navigateFolder(ITreeBuilder treeBuilder, String out, File path, String[] subsets, int begin, int stop,
			ExecutionConfiguration configuration) throws Exception {
		this.navigateFolder(treeBuilder, out, path, subsets, begin, stop, configuration, tuningEngine.allMatchers);
	}

	/**
	 * This filter the matchers
	 * 
	 * @param treeBuilder
	 * @param out
	 * @param path
	 * @param subsets
	 * @param begin
	 * @param stop
	 * @param configuration
	 * @param matchersString
	 * @throws Exception
	 */
	public void navigateFolder(ITreeBuilder treeBuilder, String out, File path, String[] subsets, int begin, int stop,
			ExecutionConfiguration configuration, String[] matchersString) throws Exception {

		if (matchersString == null || matchersString.length == 0) {
			System.out.println("Using default matchers " + Arrays.toString(tuningEngine.allMatchers));
			this.navigateFolder(treeBuilder, out, path, subsets, begin, stop, configuration, tuningEngine.allMatchers);
		} else {
			System.out.println("Using existing matchers " + Arrays.toString(matchersString));
			List<Matcher> selectedMatchers = new ArrayList<Matcher>();

			for (String mS : matchersString) {
				for (Matcher matcher : tuningEngine.allMatchers) {
					if (matcher.getClass().getSimpleName().toLowerCase().equals(mS.toLowerCase())) {
						selectedMatchers.add(matcher);
					}
				}
			}
			if (selectedMatchers.isEmpty()) {
				throw new IllegalArgumentException("Any matcher found:  " + Arrays.toString(matchersString));
			}
			System.out.println("Selected matchers " + selectedMatchers);
			Matcher[] newMatchers = new Matcher[selectedMatchers.size()];
			selectedMatchers.toArray(newMatchers);

			this.navigateFolder(treeBuilder, out, path, subsets, begin, stop, configuration, newMatchers);
		}
	}

	/**
	 * Navigates megadiff datasets
	 * 
	 * @param path    path to megadiff root
	 * @param subsets subsets of megadiff to consider
	 * @param stop    max numbers of diff to analyze per subset
	 * @return
	 * @throws Exception
	 * @throws NoSuchAlgorithmException
	 */
	public void navigateFolder(ITreeBuilder treeBuilder, String out, File path, String[] subsets, int begin, int stop,
			ExecutionConfiguration configuration, Matcher[] matchers) throws Exception {

		long initTime = (new Date()).getTime();

		for (String subset : subsets) {

			int nrCommit = 0;
			File pathSubset = new File(path.getAbsoluteFile() + File.separator + subset + File.separator);

			List<File> commits = Arrays.asList(pathSubset.listFiles());

			File outFileSubset = new File(out + File.separator + subset + File.separator);
			outFileSubset.mkdirs();
			configuration.setDirDiffTreeSerialOutput(outFileSubset);

			Collections.sort(commits);

			for (File commit : commits) {

				if (".DS_Store".equals(commit.getName()))
					continue;

				nrCommit++;

				if (nrCommit <= begin) {
					System.out.println("Skip " + nrCommit + ": " + commit.getName());
					continue;
				}

				if (nrCommit > stop) {
					System.out.println("Reach max " + nrCommit);
					break;
				}

				if (commit.list() == null)
					continue;

				for (File fileModif : commit.listFiles()) {
					if (".DS_Store".equals(fileModif.getName()))
						continue;

					String pathname = calculatePathName(fileModif, commit);

					File previousVersion = new File(pathname.trim() + "_s.java");
					File postVersion = new File(pathname.trim() + "_t.java");

					if (!previousVersion.exists() || !postVersion.exists()) {
						System.err.println("Missing file in diff " + pathname + " " + commit.getName());

						continue;
					}
					tuningEngine.computeBestLocal(treeBuilder, previousVersion, postVersion,
							configuration.getFitnessFunction(), configuration);

				}
			}
		}
		System.out.println("Finished all diff from index " + begin + " to " + stop);

		long endTime = (new Date()).getTime();

		System.out.println("TOTAL Time " + ((endTime - initTime) / 1000) + " secs");

	}

	public ResponseBestParameter summarizeBestGlobal(File rootFolder) throws IOException {
		return summarizeBestGlobal(rootFolder, METRIC.MEDIAN, false);
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

	public ResponseGlobalBestParameter summarizeBestGlobal(File rootFolder, METRIC metric, Boolean ignoreTimeout)
			throws IOException {

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

		ResponseGlobalBestParameter best = exa.summarizeResultsForGlobal(results, metric, ignoreTimeout);
		return best;
	}

	public ResponseGlobalBestParameter summarizeBestGlobal(List<File> toProcess, METRIC metric, Boolean ignoreTimeout)
			throws IOException {

		ExhaustiveEngine exa = new ExhaustiveEngine();

		DatOutputEngine outputengine = new DatOutputEngine(null);

		ResultByConfig results = new ResultByConfig();

		for (File filesFromDiff : toProcess) {

			outputengine.readZipAndAdd(results, filesFromDiff);

		}

		ResponseGlobalBestParameter best = exa.summarizeResultsForGlobal(results, metric, ignoreTimeout);
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

	protected String calculatePathName(File fileModif, File parentFile) {
		return
		// The folder with the file name
		fileModif.getAbsolutePath() + File.separator + (parentFile.getName() + "_")
		// File name
				+ fileModif.getName();
	}

}
