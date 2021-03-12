package fr.gumtree.autotuning.experimentrunner;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumtreeProperties;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.utils.Pair;
import com.google.gson.JsonObject;

import fr.gumtree.autotuning.entity.CaseResult;
import fr.gumtree.autotuning.entity.MatcherResult;
import fr.gumtree.autotuning.entity.SingleDiffResult;
import fr.gumtree.autotuning.outils.Constants;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine.PARALLEL_EXECUTION;
import fr.gumtree.autotuning.treebuilder.ITreeBuilder;
import fr.gumtree.treediff.jdt.TreeDiffFormatBuilder;

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
			int begin, int stop, PARALLEL_EXECUTION parallel) throws IOException {
		return this.navigateMegaDiff(treeBuilder, out, path, subsets, begin, stop, parallel, tuningEngine.allMatchers);
	}

	public List<CaseResult> navigateMegaDiff(ITreeBuilder treeBuilder, String out, File path, int[] subsets, int begin,
			int stop, PARALLEL_EXECUTION parallel, String[] matchersString) throws Exception {

		if (matchersString == null || matchersString.length == 0) {
			System.out.println("Using default matchers " + Arrays.toString(tuningEngine.allMatchers));
			return this.navigateMegaDiff(treeBuilder, out, path, subsets, begin, stop, parallel,
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

			return this.navigateMegaDiff(treeBuilder, out, path, subsets, begin, stop, parallel, newMatchers);
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
			int stop, PARALLEL_EXECUTION parallel, Matcher[] matchers) throws IOException {
		// tuningEngine.initCacheCombinationProperties();

		System.out.println("Execution mode " + parallel);

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
							parallel, treeProperties, matchers);

					// This time includes the creation of tree
					long timediff = (new Date()).getTime() - initdiff;
					System.out.println("diff time " + timediff / 1000 + " sec, " + timediff + " milliseconds");

					fileResult.setFileName(fileModif.getName());
					fileResult.setCommit(commit.getName());
					fileResult.setDatasubset(Integer.toString(subset));

					// Saving in files
					outResults.getParentFile().mkdirs();

					this.executionResultToCSV(outResults, fileResult);

					File treeFile = new File(out + File.separator + subset + File.separator + "metaInfo_nr_" + nrCommit
							+ "_id_" + diffId + "_" + treeBuilder.modelType().name() + ".csv");
					this.metadataToCSV(treeFile, treeProperties, fileResult);

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
			String commitId, PARALLEL_EXECUTION parallel) throws IOException {

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

		CaseResult fileResult = this.runSingleOnPairOfFiles(treeBuilder, out, subset, parallel, previousVersion,
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

	public void metadataToCSV(File nameFile, Map<String, Pair<Map, Map>> treeProperties, CaseResult fileResult)
			throws IOException {

		String sep = ",";
		String endline = "\n";
		String header = "DIFFID" + sep + "L_" + Constants.SIZE + sep + "L_" + Constants.HEIGHT + sep + "L_"
				+ Constants.STRUCTHASH + sep + "R_" + Constants.SIZE + sep + "R_" + Constants.HEIGHT + sep + "R_"
				+ Constants.STRUCTHASH + sep + Constants.TIME_TREES_PARSING + sep + Constants.TIME_ALL_MATCHER_DIFF;

		for (Matcher matcher : this.tuningEngine.allMatchers) {
			header += (sep + matcher.getClass().getSimpleName());
		}

		header += endline;

		String row = "";
		Collection<MatcherResult> matchersInfo = fileResult.getResultByMatcher().values();

		if (matchersInfo == null) {
			System.err.println("Problems when saving results: No matchers for identifier " + nameFile.getName());
			return;
		}

		for (String id : treeProperties.keySet()) {

			Pair<Map, Map> t = treeProperties.get(id);
			row += id + sep;
			row += t.first.get(Constants.SIZE) + sep;
			row += t.first.get(Constants.HEIGHT) + sep;
			row += t.first.get(Constants.STRUCTHASH) + sep;
			row += t.second.get(Constants.SIZE) + sep;
			row += t.second.get(Constants.HEIGHT) + sep;
			row += t.second.get(Constants.STRUCTHASH) + sep;

			// Times:

			row += fileResult.getTimeParsing() + sep;
			row += fileResult.getTimeMatching() + sep;

			for (Matcher matcher : this.tuningEngine.allMatchers) {
				Optional<MatcherResult> findFirst = matchersInfo.stream()
						.filter(e -> e.getMatcherName().equals(matcher.getClass().getSimpleName())).findFirst();
				if (findFirst.isPresent()) {
					MatcherResult pM = findFirst.get();

					row += pM.getTimeAllConfigs() + sep;
				} else {
					row += "" + sep;
				}
			}
			row += endline;
		}

		FileWriter fw = new FileWriter(nameFile);
		fw.write(header + row);
		fw.close();

	}

	public CaseResult runSingleOnPairOfFiles(ITreeBuilder treeBuilder, String out, int subset,
			PARALLEL_EXECUTION parallel, File previousVersion, File postVersion, String diffId) throws IOException {
		Map<String, Pair<Map, Map>> treeProperties = new HashMap<>();

		long initTime = (new Date()).getTime();

		CaseResult fileResult = this.tuningEngine.analyzeCase(treeBuilder, diffId, previousVersion, postVersion,
				parallel, treeProperties, this.tuningEngine.allMatchers);

		fileResult.setFileName(postVersion.getName());

		File outResults = new File(out + diffId + ".csv");

		executionResultToCSV(outResults, fileResult);

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

	/**
	 * Store the data in a csv file
	 * 
	 * @param out
	 * @param fileresult
	 * @param astmodel
	 * @throws IOException
	 */
	public void executionResultToCSV(File out, CaseResult fileresult) throws IOException {

		String sep = ",";
		String endline = "\n";
		String header = "";

		Collection<MatcherResult> matchers = fileresult.getResultByMatcher().values();

		if (matchers == null) {
			System.err.println("Problems when saving results: No matchers for identifier " + out.getName());
			return;
		}

		String row = "";
		boolean first = true;
		FileWriter fw = new FileWriter(out);
		for (MatcherResult map : matchers) {

			if (map == null || map.getMatcher() == null) {
				System.out.println("No matcher in results ");
				continue;
			}

			String xmatcher = map.getMatcherName().toString();

			List<SingleDiffResult> configs = (List<SingleDiffResult>) map.getAlldiffresults();
			for (Map<String, Object> config : configs) {
				// re-init the row

				if (config == null)
					continue;

				GumtreeProperties gtp = (config.containsKey(Constants.CONFIG))
						? (GumtreeProperties) config.get(Constants.CONFIG)
						: new GumtreeProperties();
				if (config.get(Constants.TIMEOUT) != null) {

					row = xmatcher + sep;

					row += "" + sep;

					row += "" + sep;

					//
					row += "" + sep;
					row += "" + sep;
					row += "" + sep;
					row += "" + sep;
					row += "" + sep;
					row += "" + sep;
					//
					row += "" + sep;

					row += "" + sep;

					// TIMEout
					row += config.get(Constants.TIMEOUT) + sep;

				} else {

					row = xmatcher + sep;

					row += config.get(Constants.NRACTIONS) + sep;

					row += config.get(Constants.NRROOTS) + sep;

					//
					row += config.get(Constants.NR_INSERT) + sep;
					row += config.get(Constants.NR_DELETE) + sep;
					row += config.get(Constants.NR_UPDATE) + sep;
					row += config.get(Constants.NR_MOVE) + sep;
					row += config.get(Constants.NR_TREEINSERT) + sep;
					row += config.get(Constants.NR_TREEDELETE) + sep;

					//
					row += config.get(Constants.TIME) + sep;

					row += 0 + sep;// gtp.getProperties().keySet().size()
					// TIMEout
					row += "0" + sep;
				}
				if (first) {
					header += Constants.MATCHER + sep;
					header += Constants.NRACTIONS + sep;
					header += Constants.NRROOTS + sep;

					header += Constants.NR_INSERT + sep;
					header += Constants.NR_DELETE + sep;
					header += Constants.NR_UPDATE + sep;
					header += Constants.NR_MOVE + sep;
					header += Constants.NR_TREEINSERT + sep;
					header += Constants.NR_TREEDELETE + sep;

					header += Constants.TIME + sep;
					header += "NROPTIONS" + sep;
					header += Constants.TIMEOUT + sep;

				}

				for (ConfigurationOptions confOption : ConfigurationOptions.values()) {
					if (first) {
						header += confOption.name() + sep;

					}
					row += ((gtp.get(confOption) != null) ? gtp.get(confOption) : "") + sep;

				}

				if (first) {
					header += endline;
					first = false;
					fw.write(header);
				}

				row += endline;
				fw.write(row);
				fw.flush();
			}

		}

		fw.close();
		System.out.println("Saved file " + out.getAbsolutePath());

	}

	private void executionResultToUnifiedDiff(File outResults, CaseResult fileResult) {
		TreeDiffFormatBuilder builder = new TreeDiffFormatBuilder(false, false);

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

				// save(builder, outResults, jso, sd.getDiff(), gttp, mr.getMatcherName());
			}

		}

	}
}
