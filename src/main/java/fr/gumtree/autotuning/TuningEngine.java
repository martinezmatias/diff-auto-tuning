package fr.gumtree.autotuning;

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
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.gumtreediff.actions.ChawatheScriptGenerator;
import com.github.gumtreediff.actions.Diff;
import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.TreeDelete;
import com.github.gumtreediff.actions.model.TreeInsert;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.CompositeMatchers.CompositeMatcher;
import com.github.gumtreediff.matchers.ConfigurableMatcher;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumtreeProperties;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.utils.Pair;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.gumtree.autotuning.entity.CaseResult;
import fr.gumtree.autotuning.entity.MatcherResult;
import fr.gumtree.autotuning.entity.SingleDiffResult;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;
import fr.gumtree.treediff.jdt.TreeDiffFormatBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class TuningEngine {
	// TODO: CHange to enum
	public static final String NR_TREEDELETE = "NR_TREEDELETE";
	public static final String NR_TREEINSERT = "NR_TREEINSERT";
	public static final String NR_MOVE = "NR_MOVE";
	public static final String NR_UPDATE = "NR_UPDATE";
	public static final String NR_DELETE = "NR_DELETE";
	public static final String NR_INSERT = "NR_INSERT";
	public static final String CONFIGS = "r";
	public static final String MATCHER = "MATCHER";
	public static final String TIME_MATCHER_ALL_CONFIGS = "TIME_MATCHER_ALL_CONFIGS";
	public static final String MATCHERS = "MATCHERS";
	public static final String TRIGHT = "TRIGHT";
	public static final String TLEFT = "TLEFT";
	public static final String CONFIG = "CONFIG";
	public static final String TIME = "TIME";
	public static final String NRROOTS = "NRROOTS";
	public static final String NRACTIONS = "NRACTIONS";
	public static final String STRUCTHASH = "STRUCTHASH";
	public static final String HEIGHT = "HEIGHT";
	public static final String SIZE = "SIZE";
	public static final String MEGADIFFSET = "MEGADIFFSET";
	public static final String COMMIT = "COMMIT";
	public static final String FILE = "FILE";
	public static final String TIMEOUT = "TIMEOUT";

	// TIMES:
	public static final String TIME_ALL_MATCHER_DIFF = "TIME_ALL_MATCHER_DIFF";
	public static final String TIME_TREES_PARSING = "TIME_TREES_PARSING";

	private ITreeBuilder treeBuilder = new SpoonTreeBuilder();

	long timeOutSeconds = 60 * 60; // 60 min

	public enum PARALLEL_EXECUTION {
		MATCHER_LEVEL, PROPERTY_LEVEL, NONE
	}

	public enum ASTMODE {
		GTSPOON, JDT
	};

	// TODO: we cannot use the same generator when we execute on parallel.
	private ChawatheScriptGenerator editScriptGenerator = new ChawatheScriptGenerator();

	Matcher[] allMatchers = new Matcher[] {
			//
			new CompositeMatchers.SimpleGumtree(),
			//
			new CompositeMatchers.ClassicGumtree(),
			//
			new CompositeMatchers.CompleteGumtreeMatcher(),
			//
			new CompositeMatchers.ChangeDistiller(),
			//
			new CompositeMatchers.XyMatcher(),

	};
	/**
	 * Indicates the number of threads used in the property parallelization.
	 */
	private int nrThreads = 10;

	private boolean overwriteresults = false;

	Map<String, List<GumtreeProperties>> cacheCombinations = new HashMap<String, List<GumtreeProperties>>();

	public TuningEngine() {
		super();
		// Not necessary here
		// initCacheCombinationProperties();
	}

	public void initCacheCombinationProperties() {
		this.cacheCombinations.clear();
		for (Matcher matcher : allMatchers) {
			List<GumtreeProperties> allCombinations = computesCombinations(matcher);
			this.cacheCombinations.put(matcher.getClass().getCanonicalName(), allCombinations);

		}
	}

	public List<CaseResult> navigateMegaDiffAllMatchers(String out, File path, int[] subsets, int begin, int stop,
			PARALLEL_EXECUTION parallel) throws IOException {
		return this.navigateMegaDiff(out, path, subsets, begin, stop, parallel, this.allMatchers);
	}

	public List<CaseResult> navigateMegaDiff(String out, File path, int[] subsets, int begin, int stop,
			PARALLEL_EXECUTION parallel, String[] matchersString) throws Exception {

		if (matchersString == null || matchersString.length == 0) {
			System.out.println("Using default matchers " + Arrays.toString(this.allMatchers));
			return this.navigateMegaDiff(out, path, subsets, begin, stop, parallel, this.allMatchers);
		} else {
			System.out.println("Using existing matchers " + Arrays.toString(matchersString));
			List<Matcher> selectedMatchers = new ArrayList<Matcher>();

			for (String mS : matchersString) {
				for (Matcher matcher : this.allMatchers) {
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

			return this.navigateMegaDiff(out, path, subsets, begin, stop, parallel, newMatchers);
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
	public List<CaseResult> navigateMegaDiff(String out, File path, int[] subsets, int begin, int stop,
			PARALLEL_EXECUTION parallel, Matcher[] matchers) throws IOException {
		this.initCacheCombinationProperties();

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
							+ "_id_" + diffId + "_" + this.treeBuilder.modelType().name() + ".csv");

					if (!overwriteresults && outResults.exists()) {
						System.out.println("Already analyzed: " + nrCommit + ": " + outResults.getName());
						continue;
					}

					Map<String, Pair<Map, Map>> treeProperties = new HashMap<>();

					long initdiff = (new Date()).getTime();

					System.out.println("\n---diff " + nrCommit + "/" + commits.size() + " id " + diffId);
					CaseResult fileResult = analyzeCase(diffId, previousVersion, postVersion, parallel, treeProperties,
							matchers);

					// This time includes the creation of tree
					long timediff = (new Date()).getTime() - initdiff;
					System.out.println("diff time " + timediff / 1000 + " sec, " + timediff + " milliseconds");

					fileResult.setFileName(fileModif.getName());
					fileResult.setCommit(commit.getName());
					fileResult.setDatasubset(Integer.toString(subset));

					// Saving in files
					outResults.getParentFile().mkdirs();

					executionResultToCSV(outResults, fileResult);

					File treeFile = new File(out + File.separator + subset + File.separator + "metaInfo_nr_" + nrCommit
							+ "_id_" + diffId + "_" + this.treeBuilder.modelType().name() + ".csv");
					metadataToCSV(treeFile, treeProperties, fileResult);

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

	public CaseResult runSingleDiffMegaDiff(String out, File path, int subset, String commitId,
			PARALLEL_EXECUTION parallel) throws IOException {

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

		CaseResult fileResult = runSingleOnPairOfFiles(out, subset, parallel, previousVersion, postVersion, diffId);

		// add the data specific to megadiff.

		fileResult.setCommit(commit.getName());
		fileResult.setDatasubset(Integer.toString(subset));
		// let's override the property
		fileResult.setFileName(fileModif.getName());

		return fileResult;

	}

	public CaseResult runSingleOnPairOfFiles(String out, int subset, PARALLEL_EXECUTION parallel, File previousVersion,
			File postVersion, String diffId) throws IOException {
		Map<String, Pair<Map, Map>> treeProperties = new HashMap<>();

		long initTime = (new Date()).getTime();

		CaseResult fileResult = analyzeCase(diffId, previousVersion, postVersion, parallel, treeProperties,
				allMatchers);

		fileResult.setFileName(postVersion.getName());

		File outResults = new File(out + diffId + ".csv");

		executionResultToCSV(outResults, fileResult);

		executionResultToUnifiedDiff(outResults, fileResult);

		long endTime = (new Date()).getTime();

		System.out.println("Time " + (endTime - initTime) / 1000);

		// File treeFile = new File(out + File.separator + subset + File.separator +
		// "metaInfo_nr_" + nrCommit + "_id_"
		// + diffId + "_" + this.treeBuilder.modelType().name() + ".csv");
		// metadataToCSV(treeFile, treeProperties, fileResult);
		return fileResult;
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

				//
				GumtreeProperties gttp = (GumtreeProperties) sd.get(CONFIG);

				Map<String, Object> propertiesMap = toGumtreePropertyToMap(gttp);

				jso.addProperty("matcher", mr.getMatcherName());

				for (String pKey : propertiesMap.keySet()) {

					jso.addProperty(pKey, propertiesMap.get(pKey).toString());

				}

				JsonElement js = builder.build(null, null, sd.getDiff(), jso);

			}

		}

	}

	/**
	 * 
	 * @param diffId
	 * @param previousVersion
	 * @param postVersion
	 * @param parallel
	 * @param treeProperties
	 * @param fileResult
	 * @return
	 */
	public CaseResult analyzeCase(String diffId, File previousVersion, File postVersion, PARALLEL_EXECUTION parallel,
			Map<String, Pair<Map, Map>> treeProperties, Matcher[] matchers) {
		try {
			Tree tl = this.treeBuilder.build(previousVersion);
			Tree tr = this.treeBuilder.build(postVersion);
			long init = (new Date()).getTime();

			long endTree = (new Date()).getTime();
			treeProperties.put(diffId, new Pair<Map, Map>(extractTreeFeaturesMap(tl), extractTreeFeaturesMap(tr)));

			long endFeatures = (new Date()).getTime();
			CaseResult result = null;
			if (parallel.equals(PARALLEL_EXECUTION.MATCHER_LEVEL))
				result = analyzeDiffByMatcherThread(tl, tr, parallel, matchers);
			else
				result = analyzeDiffByPropertyParallel(tl, tr, parallel, matchers);

			long endMatching = (new Date()).getTime();

			long timeParsing = endTree - init;
			long timeFeaturing = endFeatures - endTree;
			long timeMatching = endMatching - endFeatures;
			System.out.println("Time tree " + timeParsing + ", time features " + timeFeaturing + ", time matching "
					+ timeMatching);

			if (result != null) {
				result.setTimeParsing(timeParsing);
				result.setTimeMatching(timeMatching);
			}

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			CaseResult caseEx = new CaseResult();
			caseEx.setFromException(e);
			return caseEx;
		}
	}

	/**
	 * Computes diffs for all matches passed as parameters for all properties
	 * (potentially in parallel) from each matcher It executes in parallel by
	 * property from a Matcher. This means that the matchers are executed in
	 * sequence.
	 * 
	 * @param tl
	 * @param tr
	 * @param parallel
	 * @param matchers
	 * @return
	 */
	public CaseResult analyzeDiffByPropertyParallel(Tree tl, Tree tr, PARALLEL_EXECUTION parallel, Matcher[] matchers) {

		CaseResult resultsForCase = new CaseResult();

		for (Matcher matcher : matchers) {
			try {

				MatcherResult resultFromMatcher = runSingleMatcherMultipleConfigurations(tl, tr, matcher,
						PARALLEL_EXECUTION.PROPERTY_LEVEL.equals(parallel) && this.nrThreads > 1);

				resultsForCase.getResultByMatcher().put(matcher, resultFromMatcher);
			} catch (Exception e) {
				System.err.println("Problems with matcher " + matcher.getClass().getSimpleName());
				e.printStackTrace();
			}
		}
		return resultsForCase;
	}

	public CaseResult analyzeDiffByMatcherThread(Tree tl, Tree tr, PARALLEL_EXECUTION parallel, Matcher[] matchers) {
		// List<MatcherResult> matcherResults = new ArrayList<>();

		CaseResult fileResult = new CaseResult();

		ExecutorService executor = Executors.newFixedThreadPool(matchers.length);
		List<Callable<MatcherResult>> callables = new ArrayList<>();

		try {

			for (Matcher matcher : matchers) {

				callables.add(new MatcherCallable(tl, tr, matcher));
			}

			List<Future<MatcherResult>> result = executor.invokeAll(callables, this.timeOutSeconds, TimeUnit.SECONDS);

			executor.shutdown();

			List<MatcherResult> collectedResults = result.stream().map(e -> {
				try {
					if (e.isDone() && !e.isCancelled()) {

						return e.get();
					} else {

						return returnEmptyResult(matchers, result, e, ERROR_TYPE.TIMEOUT);

					}

				} catch (Exception e1) {
					System.err.println("Problems when collecting data");
					e1.printStackTrace();
					return returnEmptyResult(matchers, result, e, ERROR_TYPE.EXCEPTION);
				}
			}).collect(Collectors.toList());

			for (MatcherResult matcherResult : collectedResults) {
				fileResult.getResultByMatcher().put(matcherResult.getMatcher(), matcherResult);
			}

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return fileResult;
	}

	enum ERROR_TYPE {
		TIMEOUT, EXCEPTION
	}

	public MatcherResult returnEmptyResult(Matcher[] matchers, List<Future<MatcherResult>> result,
			Future<MatcherResult> e, ERROR_TYPE errortype) {
		MatcherResult resultFromCancelled = new MatcherResult();

		int indexFuture = result.indexOf(e);
		if (indexFuture >= 0) {

			Matcher matcher = matchers[indexFuture];
			System.out.println("Timeout for " + matcher.getClass().getSimpleName());

			resultFromCancelled.setMatcherName(matcher.getClass().getSimpleName());

			List<SingleDiffResult> alldiffresults = new ArrayList<>();

			resultFromCancelled.setAlldiffresults(alldiffresults);

			List<GumtreeProperties> combinations = getPropertiesCombinations(matcher);

			for (GumtreeProperties GumtreeProperties : combinations) {

				SingleDiffResult notFinishedConfig = new SingleDiffResult();
				notFinishedConfig.put(TIMEOUT, errortype.ordinal() + 1);
				notFinishedConfig.put(CONFIG, GumtreeProperties);

				alldiffresults.add(notFinishedConfig);

			}
		}
		return resultFromCancelled;
	}

	/**
	 * Executes a Matches
	 * 
	 * @param tl
	 * @param tr
	 * @param matcher
	 * @param parallel
	 * @return
	 */
	public MatcherResult runSingleMatcherMultipleConfigurations(Tree tl, Tree tr, Matcher matcher, boolean parallel) {
		long initMatcher = (new Date()).getTime();
		List<GumtreeProperties> combinations = null;

		MatcherResult result = new MatcherResult();

		String matcherName = matcher.getClass().getSimpleName();

		result.setMatcherName(matcherName);
		result.setMatcher(matcher);

		List<SingleDiffResult> alldiffresults = new ArrayList<>();

		result.setAlldiffresults(alldiffresults);

		if (matcher instanceof ConfigurableMatcher) {

			combinations = getPropertiesCombinations(matcher);

		} else {
			// The matcher does not allow customization
			combinations = new ArrayList<GumtreeProperties>();
			GumtreeProperties properies = new GumtreeProperties();
			combinations.add(properies);

		}

		if (!parallel) {
			for (GumtreeProperties aGumtreeProperties : combinations) {

				SingleDiffResult resDiff = runDiff(tl, tr, matcher, aGumtreeProperties);

				alldiffresults.add(resDiff);

			}
		} else {
			// parallel
			try {
				List<SingleDiffResult> results = runInParallelMultipleConfigurations(nrThreads, tl, tr, matcher,
						combinations, this.timeOutSeconds);
				for (SingleDiffResult iResult : results) {

					alldiffresults.add(iResult);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//

		long timeAllConfigs = ((new Date()).getTime() - initMatcher);
		result.setTimeAllConfigs(timeAllConfigs);
		System.out.println("End execution Matcher " + matcherName + ", time " + timeAllConfigs
				+ " milliseconds, Nr_config: " + combinations.size());
		return result;

	}

	public List<GumtreeProperties> getPropertiesCombinations(Matcher matcher) {

		if (this.cacheCombinations.isEmpty()) {
			this.initCacheCombinationProperties();
		}
		return this.cacheCombinations.get(matcher.getClass().getCanonicalName());
	}

	public List<GumtreeProperties> computesCombinations(Matcher matcher) {
		List<GumtreeProperties> combinations;
		ConfigurableMatcher configurableMatcher = (ConfigurableMatcher) matcher;

		// We collect the options of the matcher
		Set<ConfigurationOptions> options = configurableMatcher.getApplicableOptions();

		List<ParameterDomain> domains = new ArrayList<>();

		// We collect the domains
		for (ConfigurationOptions option : options) {

			ParameterDomain<?> paramOption = ParametersResolvers.parametersDomain.get(option);
			if (paramOption != null) {
				domains.add(paramOption);
			} else {
				System.err.println("Missing config for " + option);
				throw new RuntimeException("Missing config for " + option);
			}
		}

		// Now, the CartesianProduct of all options
		combinations = computeCartesianProduct(domains);
		return combinations;
	}

	public class MatcherCallable implements Callable<MatcherResult> {
		Tree tl;
		Tree tr;
		Matcher matcher;

		public MatcherCallable(Tree tl, Tree tr, Matcher matcher) {
			this.tl = tl;
			this.tr = tr;
			this.matcher = matcher;
		}

		@Override
		public MatcherResult call() throws Exception {

			return runSingleMatcherMultipleConfigurations(tl, tr, matcher, false);
		}
	}

	public class DiffCallable implements Callable<SingleDiffResult> {
		Tree tl;
		Tree tr;
		Matcher matcher;
		GumtreeProperties aGumtreeProperties;

		public DiffCallable(Tree tl, Tree tr, Matcher matcher, GumtreeProperties aGumtreeProperties) {
			this.tl = tl;
			this.tr = tr;
			this.matcher = matcher;
			this.aGumtreeProperties = aGumtreeProperties;
		}

		@Override
		public SingleDiffResult call() throws Exception {

			return runDiff(tl, tr,
					// TODO: Workaround: we cannot used the same instance of a matcher to match in
					// parallel two diffs
					matcher.getClass().newInstance()
					// matcher
					// matcher
					, aGumtreeProperties);
		}

	}

	/**
	 * Executes a list of configuration from a Matcher in parallel.
	 * 
	 * @param nrThreads
	 * @param tl
	 * @param tr
	 * @param matcher
	 * @param combinations
	 * @param timeoutSeconds
	 * @return
	 * @throws Exception
	 */
	public List<SingleDiffResult> runInParallelMultipleConfigurations(int nrThreads, Tree tl, Tree tr, Matcher matcher,
			List<GumtreeProperties> combinations, long timeoutSeconds) throws Exception {

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(nrThreads);

		List<DiffCallable> callables = new ArrayList<>();

		for (GumtreeProperties aGumtreeProperties : combinations) {
			callables.add(new DiffCallable(tl, tr, matcher, aGumtreeProperties));
		}

		List<Future<SingleDiffResult>> result = executor.invokeAll(callables, timeoutSeconds, TimeUnit.SECONDS);

		executor.shutdown();

		return result.stream().map(e -> {
			try {
				if (e.isDone() && !e.isCancelled())
					return e.get();
				else {

					SingleDiffResult notFinishedConfig = new SingleDiffResult();
					notFinishedConfig.put(TIMEOUT, "true");

					int indexFuture = result.indexOf(e);
					if (indexFuture >= 0) {
						// Store the properties of the future not finished
						DiffCallable icallable = callables.get(indexFuture);
						notFinishedConfig.put(CONFIG, icallable.aGumtreeProperties);

					}

					return notFinishedConfig;
				}
			} catch (Exception e1) {
				System.err.println("Exception by collecting the results in Property Parallel");
				e1.printStackTrace();
				return null;
			}
		}).collect(Collectors.toList());
	}

	/**
	 * Computes the diff given a matcher and property.
	 * 
	 * @param tl
	 * @param tr
	 * @param matcher
	 * @param aGumtreeProperties
	 * @return
	 */
	public SingleDiffResult runDiff(Tree tl, Tree tr, Matcher matcher, GumtreeProperties aGumtreeProperties) {
		long initSingleDiff = new Date().getTime();
		SingleDiffResult resultDiff = new SingleDiffResult();

		// Calling directly to GT.core
		Diff diff = computeDiff(tl, tr, matcher, aGumtreeProperties);
		List<Action> actionsAll = diff.editScript.asList();

		long endSingleDiff = new Date().getTime();

		resultDiff.put(NRACTIONS, actionsAll.size());
		resultDiff.put(NR_INSERT, actionsAll.stream().filter(e -> e instanceof Insert).count());
		resultDiff.put(NR_DELETE, actionsAll.stream().filter(e -> e instanceof Delete).count());
		resultDiff.put(NR_UPDATE, actionsAll.stream().filter(e -> e instanceof Update).count());
		resultDiff.put(NR_MOVE, actionsAll.stream().filter(e -> e instanceof Move).count());
		resultDiff.put(NR_TREEINSERT, actionsAll.stream().filter(e -> e instanceof TreeInsert).count());
		resultDiff.put(NR_TREEDELETE, actionsAll.stream().filter(e -> e instanceof TreeDelete).count());
		resultDiff.put(TIME, (endSingleDiff - initSingleDiff));

		resultDiff.put(CONFIG, aGumtreeProperties);

		resultDiff.setDiff(diff);

		return resultDiff;

	}

	public Diff computeDiff(Tree tl, Tree tr, Matcher matcher, GumtreeProperties properies) {

		return computeDiff(tl, tr, matcher, new ChawatheScriptGenerator(), properies);
	}

	public Diff computeDiff(Tree tl, Tree tr, Matcher matcher, EditScriptGenerator edGenerator,
			GumtreeProperties properies) {

		CompositeMatcher cm = (CompositeMatcher) matcher;
		cm.configure(properies);

		MappingStore mappings = matcher.match(tl, tr);

		EditScript actions = edGenerator.computeActions(mappings);

		Diff diff = new Diff(null, null, mappings, actions);

		// List<Action> actionsAll = actions.asList();
		return diff;
	}

	public List<GumtreeProperties> computeCartesianProduct(List<ParameterDomain> domains) {

		return cartesianProduct(0, domains);
	}

	private List<GumtreeProperties> cartesianProduct(int index, List<ParameterDomain> domains) {

		List<GumtreeProperties> ret = new ArrayList<>();

		if (index == domains.size()) {
			ret.add(new GumtreeProperties());
		} else {

			ParameterDomain domainOfParameters = domains.get(index);
			for (Object valueFromDomain : domainOfParameters.computeInterval()) {
				List<GumtreeProperties> configurationFromOthersDomains = cartesianProduct(index + 1, domains);
				for (GumtreeProperties configFromOthers : configurationFromOthersDomains) {

					ConfigurationOptions value = ConfigurationOptions.valueOf(domainOfParameters.getId());
					if (value != null) {
						configFromOthers.put(value, valueFromDomain);
						ret.add(configFromOthers);
					}
				}
			}
		}
		return ret;
	}

	protected String calculatePathName(File fileModif, File parentFile) {
		return
		// The folder with the file name
		fileModif.getAbsolutePath() + File.separator + (parentFile.getName() + "_")
		// File name
				+ fileModif.getName();
	}

	public Matcher[] getMatchers() {
		return allMatchers;
	}

	public void setMatchers(Matcher[] matchers) {
		this.allMatchers = matchers;
	}

	/**
	 * Store the data in a csv file
	 * 
	 * @param out
	 * @param fileresult
	 * @param astmodel
	 * @throws IOException
	 */
	private void executionResultToCSV(File out, CaseResult fileresult) throws IOException {

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

				GumtreeProperties gtp = (config.containsKey(CONFIG)) ? (GumtreeProperties) config.get(CONFIG)
						: new GumtreeProperties();
				if (config.get(TIMEOUT) != null) {

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
					row += config.get(TIMEOUT) + sep;

				} else {

					row = xmatcher + sep;

					row += config.get(NRACTIONS) + sep;

					row += config.get(NRROOTS) + sep;

					//
					row += config.get(NR_INSERT) + sep;
					row += config.get(NR_DELETE) + sep;
					row += config.get(NR_UPDATE) + sep;
					row += config.get(NR_MOVE) + sep;
					row += config.get(NR_TREEINSERT) + sep;
					row += config.get(NR_TREEDELETE) + sep;

					//
					row += config.get(TIME) + sep;

					row += 0 + sep;// gtp.getProperties().keySet().size()
					// TIMEout
					row += "0" + sep;
				}
				if (first) {
					header += MATCHER + sep;
					header += NRACTIONS + sep;
					header += NRROOTS + sep;

					header += NR_INSERT + sep;
					header += NR_DELETE + sep;
					header += NR_UPDATE + sep;
					header += NR_MOVE + sep;
					header += NR_TREEINSERT + sep;
					header += NR_TREEDELETE + sep;

					header += TIME + sep;
					header += "NROPTIONS" + sep;
					header += TIMEOUT + sep;

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

	public JsonObject extractTreeFeatures(Tree tl) {
		JsonObject thFeatures = new JsonObject();
		thFeatures.addProperty(SIZE, tl.getMetrics().size);
		thFeatures.addProperty(HEIGHT, tl.getMetrics().height);
		thFeatures.addProperty(STRUCTHASH, tl.getMetrics().structureHash);
		return thFeatures;
	}

	public Map<String, Object> extractTreeFeaturesMap(Tree tl) {
		Map<String, Object> fileresult = new HashMap<>();
		fileresult.put(SIZE, tl.getMetrics().size);
		fileresult.put(HEIGHT, tl.getMetrics().height);
		fileresult.put(STRUCTHASH, tl.getMetrics().structureHash);
		return fileresult;
	}

	private void metadataToCSV(File nameFile, Map<String, Pair<Map, Map>> treeProperties, CaseResult fileResult)
			throws IOException {

		String sep = ",";
		String endline = "\n";
		String header = "DIFFID" + sep + "L_" + SIZE + sep + "L_" + HEIGHT + sep + "L_" + STRUCTHASH + sep + "R_" + SIZE
				+ sep + "R_" + HEIGHT + sep + "R_" + STRUCTHASH + sep + TIME_TREES_PARSING + sep
				+ TIME_ALL_MATCHER_DIFF;

		for (Matcher matcher : allMatchers) {
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
			row += t.first.get(SIZE) + sep;
			row += t.first.get(HEIGHT) + sep;
			row += t.first.get(STRUCTHASH) + sep;
			row += t.second.get(SIZE) + sep;
			row += t.second.get(HEIGHT) + sep;
			row += t.second.get(STRUCTHASH) + sep;

			// Times:

			row += fileResult.getTimeParsing() + sep;
			row += fileResult.getTimeMatching() + sep;

			for (Matcher matcher : allMatchers) {
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

	public long getTimeOutSeconds() {
		return timeOutSeconds;
	}

	public void setTimeOutSeconds(long timeOutSeconds) {
		this.timeOutSeconds = timeOutSeconds;
	}

	public int getNrThreads() {
		return nrThreads;
	}

	public void setNrThreads(int nrThreads) {
		this.nrThreads = nrThreads;
	}

	public boolean isOverwriteResults() {
		return overwriteresults;
	}

	public void setOverwriteResults(boolean overrideResults) {
		this.overwriteresults = overrideResults;
	}

	public ITreeBuilder getTreeBuilder() {
		return treeBuilder;
	}

	public void setTreeBuilder(ITreeBuilder treeBuilder) {
		this.treeBuilder = treeBuilder;
	}

	public Map<String, Object> toGumtreePropertyToMap(GumtreeProperties properties) {
		Map<String, Object> propMap = new HashMap<>();

		for (ConfigurationOptions option : ConfigurationOptions.values()) {
			Object value = properties.get(option);
			if (value != null) {
				propMap.put(option.name(), value);

			}
		}

		return propMap;
	}

}
