package fr.gumtree.autotuning.experimentrunner;

import java.io.File;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.github.gumtreediff.matchers.Matcher;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration;
import fr.gumtree.autotuning.searchengines.ExhaustiveEngine;
import fr.gumtree.autotuning.treebuilder.ITreeBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class StructuredFolderfRunner {

	ExhaustiveEngine tuningEngine = new ExhaustiveEngine();

	private boolean overwriteresults = true;

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

					ResponseBestParameter response = tuningEngine.computeBestLocal(treeBuilder, previousVersion,
							postVersion, configuration);

				}
			}
		}
		System.out.println("Finished all diff from index " + begin + " to " + stop);

		long endTime = (new Date()).getTime();

		System.out.println("TOTAL Time " + ((endTime - initTime) / 1000) + " secs");

	}

	protected String calculatePathName(File fileModif, File parentFile) {
		return
		// The folder with the file name
		fileModif.getAbsolutePath() + File.separator + (parentFile.getName() + "_")
		// File name
				+ fileModif.getName();
	}

}
