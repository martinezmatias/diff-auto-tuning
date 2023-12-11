package fr.gumtree.autotuning.experimentrunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.GumtreeProperties;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.utils.Pair;
import com.google.gson.JsonObject;

import fr.gumtree.autotuning.entity.CaseResult;
import fr.gumtree.autotuning.entity.MatcherResult;
import fr.gumtree.autotuning.entity.SingleDiffResult;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration;
import fr.gumtree.autotuning.outils.Constants;
import fr.gumtree.autotuning.outils.DatOutputEngine;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine;
import fr.gumtree.autotuning.treebuilder.ITreeBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class MegadiffRunner {

	ExhaustiveEngine tuningEngine = new ExhaustiveEngine();

	private boolean overwriteresults = true;

	public MegadiffRunner(ExhaustiveEngine tuningEngine) {
		super();
		this.tuningEngine = tuningEngine;
	}

	public MegadiffRunner() {
		super();
		this.tuningEngine = new ExhaustiveEngine();
	}

	public List<CaseResult> navigateMegaDiffAllMatchers(ITreeBuilder treeBuilder, String out, File path, int[] subsets,
			int begin, int stop, ExecutionConfiguration configuration) throws IOException {
		return this.navigateMegaDiff(treeBuilder, out, path, subsets, begin, stop, configuration,
				tuningEngine.allMatchers);
	}

	public List<CaseResult> navigateMegaDiff(ITreeBuilder treeBuilder, String out, File path, int[] subsets, int begin,
			int stop, ExecutionConfiguration configuration, String[] matchersString) throws Exception {

		if (matchersString == null || matchersString.length == 0) {
			System.out.println("Using default matchers " + Arrays.toString(tuningEngine.allMatchers));
			return this.navigateMegaDiff(treeBuilder, out, path, subsets, begin, stop, configuration,
					tuningEngine.allMatchers);
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

			return this.navigateMegaDiff(treeBuilder, out, path, subsets, begin, stop, configuration, newMatchers);
		}
	}

	/**
	 * Navigates megadiff datasets
	 * 
	 * @param path    path to megadiff root
	 * @param subsets subsets of megadiff to consider
	 * @param stop    max numbers of diff to analyze per subset
	 * @return
	 * @throws IOException
	 */
	public List<CaseResult> navigateMegaDiff(ITreeBuilder treeBuilder, String out, File path, int[] subsets, int begin,
			int stop, ExecutionConfiguration configuration, Matcher[] matchers) throws IOException {
		// tuningEngine.initCacheCombinationProperties();

		// System.out.println("Execution mode " + parallel);

		List<CaseResult> allCasesResults = new ArrayList<>();

		long initTime = (new Date()).getTime();

		for (int subset : subsets) {

			int nrCommit = 0;
			File pathSubset = new File(path.getAbsoluteFile() + File.separator + subset + File.separator);

			List<File> commits = Arrays.asList(pathSubset.listFiles());

			//
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

					String diffId = commit.getName() + "_" + fileModif.getName();

					File outResults = new File(out + File.separator + subset + File.separator + "nr_" + nrCommit
							+ "_id_" + diffId + "_" + treeBuilder.modelType().name() + ".csv");

					if (!overwriteresults && outResults.exists()) {
						System.out.println("Already analyzed: " + nrCommit + ": " + outResults.getName());
						continue;
					}

					Map<String, Pair<Map, Map>> treeProperties = new HashMap<>();

					long initdiff = (new Date()).getTime();

					System.out.println("\n---diff " + nrCommit + "/" + commits.size() + " id " + diffId);
					CaseResult fileResult = tuningEngine.analyzeCase(treeBuilder, diffId, previousVersion, postVersion,
							configuration, treeProperties, matchers);

					// This time includes the creation of tree
					long timediff = (new Date()).getTime() - initdiff;
					System.out.println("diff time " + timediff / 1000 + " sec, " + timediff + " milliseconds");

					fileResult.setFileName(fileModif.getName());
					fileResult.setCommit(commit.getName());
					fileResult.setDatasubset(Integer.toString(subset));

					// Saving in files
					outResults.getParentFile().mkdirs();

					DatOutputEngine.executionResultToCSV(outResults, fileResult);

					File treeFile = new File(out + File.separator + subset + File.separator + "metaInfo_nr_" + nrCommit
							+ "_id_" + diffId + "_" + treeBuilder.modelType().name() + ".csv");
					DatOutputEngine.metadataToCSV(treeFile, treeProperties, fileResult, this.tuningEngine.allMatchers);

					// Store the result:
					allCasesResults.add(fileResult);

				}
			}
		}
		System.out.println("Finished all diff from index " + begin + " to " + stop);

		long endTime = (new Date()).getTime();

		System.out.println("TOTAL Time " + ((endTime - initTime) / 1000) + " secs");

		return allCasesResults;
	}

	public CaseResult runSingleDiffMegaDiff(ITreeBuilder treeBuilder, String out, File path, int subset,
			String commitId, ExecutionConfiguration configuration) throws IOException {

		File pathSubset = new File(path.getAbsoluteFile() + File.separator + subset + File.separator);

		File commit = new File(
				pathSubset.getAbsolutePath() + File.separator + subset + "_" + commitId + File.separator);

		if (!commit.exists()) {
			throw new FileNotFoundException(commit.getAbsolutePath());
		}

		File fileModif = Arrays.asList(commit.listFiles()).stream().filter(e -> !".DS_Store".equals(e.getName()))
				.findFirst().get();

		String pathname = calculatePathName(fileModif, commit);

		File previousVersion = new File(pathname.trim() + "_s.java");
		File postVersion = new File(pathname.trim() + "_t.java");

		String diffId = commit.getName() + "_" + fileModif.getName();

		CaseResult fileResult = this.runSingleOnPairOfFiles(treeBuilder, out, subset, configuration, previousVersion,
				postVersion, diffId);

		// add the data specific to megadiff.

		fileResult.setCommit(commit.getName());
		fileResult.setDatasubset(Integer.toString(subset));
		// let's override the property
		fileResult.setFileName(fileModif.getName());

		return fileResult;

	}

	protected String calculatePathName(File fileModif, File parentFile) {
		return
		// The folder with the file name
		fileModif.getAbsolutePath() + File.separator + (parentFile.getName() + "_")
		// File name
				+ fileModif.getName();
	}

	public boolean isOverwriteResults() {
		return overwriteresults;
	}

	public void setOverwriteResults(boolean overrideResults) {
		this.overwriteresults = overrideResults;
	}

	public boolean isOverwriteresults() {
		return overwriteresults;
	}

	public void setOverwriteresults(boolean overwriteresults) {
		this.overwriteresults = overwriteresults;
	}

	public CaseResult runSingleOnPairOfFiles(ITreeBuilder treeBuilder, String out, int subset,
			ExecutionConfiguration configuration, File previousVersion, File postVersion, String diffId)
			throws IOException {
		Map<String, Pair<Map, Map>> treeProperties = new HashMap<>();

		long initTime = (new Date()).getTime();

		CaseResult fileResult = this.tuningEngine.analyzeCase(treeBuilder, diffId, previousVersion, postVersion,
				configuration, treeProperties, this.tuningEngine.allMatchers);

		fileResult.setFileName(postVersion.getName());

		File outResults = new File(out + diffId + ".csv");

		DatOutputEngine.executionResultToCSV(outResults, fileResult);

		File outdir = new File(out + File.separator + diffId + File.separator + "scripts");
		outdir.mkdirs();

		executionResultToUnifiedDiff(outdir, fileResult);

		long endTime = (new Date()).getTime();

		System.out.println("Time " + (endTime - initTime) / 1000);

		// File treeFile = new File(out + File.separator + subset + File.separator +
		// "metaInfo_nr_" + nrCommit + "_id_"
		// + diffId + "_" + this.treeBuilder.modelType().name() + ".csv");
		// metadataToCSV(treeFile, treeProperties, fileResult);
		return fileResult;
	}

	private void executionResultToUnifiedDiff(File outResults, CaseResult fileResult) {
		

		for (MatcherResult mr : fileResult.getResultByMatcher().values()) {

			for (SingleDiffResult sd : mr.getAlldiffresults()) {

				EditScript ed = new EditScript();

				List<Action> actions = sd.getDiff().editScript.asList();
				if (actions == null) {
					System.out.println("empty actions");
					continue;
				}

				for (Action ac : actions) {
					ed.add(ac);
				}

				JsonObject jso = new JsonObject();
				jso.addProperty("matcher", mr.getMatcherName());

				GumtreeProperties gttp = (GumtreeProperties) sd.get(Constants.CONFIG);

			
			}

		}

	}
}
