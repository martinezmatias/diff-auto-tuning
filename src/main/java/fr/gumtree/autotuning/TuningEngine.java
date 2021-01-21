package fr.gumtree.autotuning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.github.gumtreediff.actions.ChawatheScriptGenerator;
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
import com.github.gumtreediff.matchers.GumTreeProperties;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.utils.Pair;
import com.google.gson.JsonObject;

import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;

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

	Map<String, List<GumTreeProperties>> cacheCombinations = new HashMap<String, List<GumTreeProperties>>();

	public TuningEngine() {
		super();

		initCacheCombinationProperties();
	}

	public void initCacheCombinationProperties() {
		this.cacheCombinations.clear();
		for (Matcher matcher : allMatchers) {
			List<GumTreeProperties> allCombinations = computesCombinations(matcher);
			this.cacheCombinations.put(matcher.getClass().getCanonicalName(), allCombinations);

		}
	}

	public void navigateMegaDiff(String out, File path, int[] subsets, int begin, int stop, PARALLEL_EXECUTION parallel)
			throws IOException {
		this.navigateMegaDiff(out, path, subsets, begin, stop, parallel, this.allMatchers);
	}

	public void navigateMegaDiff(String out, File path, int[] subsets, int begin, int stop, PARALLEL_EXECUTION parallel,
			String[] matchersString) throws Exception {

		if (matchersString == null || matchersString.length == 0) {
			System.out.println("Using default matchers " + Arrays.toString(this.allMatchers));
			this.navigateMegaDiff(out, path, subsets, begin, stop, parallel, this.allMatchers);
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

			this.navigateMegaDiff(out, path, subsets, begin, stop, parallel, newMatchers);
		}
	}

	/**
	 * Navigates megadiff datasets
	 * 
	 * @param path    path to megadiff root
	 * @param subsets subsets of megadiff to consider
	 * @param stop    max numbers of diff to analyze per subset
	 * @throws IOException
	 */
	public void navigateMegaDiff(String out, File path, int[] subsets, int begin, int stop, PARALLEL_EXECUTION parallel,
			Matcher[] matchers) throws IOException {
		this.initCacheCombinationProperties();

		System.out.println("Execution mode " + parallel);
		// Map<String, Pair<Map, Map>> treeProperties = new HashMap<>();

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
					Map<String, Object> fileResult = analyzeDiff(diffId, previousVersion, postVersion, parallel,
							treeProperties, matchers);

					// This time includes the creation of tree
					long timediff = (new Date()).getTime() - initdiff;
					System.out.println("diff time " + timediff / 1000 + " sec, " + timediff + " milliseconds");

					fileResult.put(FILE, fileModif.getName());
					fileResult.put(COMMIT, commit.getName());
					fileResult.put(MEGADIFFSET, subset);

					// Saving in files
					outResults.getParentFile().mkdirs();

					executionResultToCSV(outResults, fileResult);

					File treeFile = new File(out + File.separator + subset + File.separator + "metaInfo_nr_" + nrCommit
							+ "_id_" + diffId + "_" + this.treeBuilder.modelType().name() + ".csv");
					metadataToCSV(treeFile, treeProperties, fileResult);
				}
			}
		}
		System.out.println("Finished all diff from index " + begin + " to " + stop);

		long endTime = (new Date()).getTime();

		System.out.println("TOTAL Time " + ((endTime - initTime) / 1000) + " secs");

	}

	public Map<String, Object> navigateSingleDiffMegaDiff(String out, File path, int subset, String commitId,
			PARALLEL_EXECUTION parallel) throws IOException {

		Map<String, Pair<Map, Map>> treeProperties = new HashMap<>();

		long initTime = (new Date()).getTime();

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

		Map<String, Object> fileResult = analyzeDiff(diffId, previousVersion, postVersion, parallel, treeProperties,
				allMatchers);

		fileResult.put(FILE, fileModif.getName());
		fileResult.put(COMMIT, commit.getName());
		fileResult.put(MEGADIFFSET, subset);

		File outResults = new File(out + diffId + ".csv");

		executionResultToCSV(outResults, fileResult);

		long endTime = (new Date()).getTime();

		System.out.println("Time " + (endTime - initTime) / 1000);

		return fileResult;

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
	public Map<String, Object> analyzeDiff(String diffId, File previousVersion, File postVersion,
			PARALLEL_EXECUTION parallel, Map<String, Pair<Map, Map>> treeProperties, Matcher[] matchers) {
		try {
			ITree tl = this.treeBuilder.build(previousVersion);
			ITree tr = this.treeBuilder.build(postVersion);
			long init = (new Date()).getTime();

			long endTree = (new Date()).getTime();
			treeProperties.put(diffId, new Pair<Map, Map>(extractTreeFeaturesMap(tl), extractTreeFeaturesMap(tr)));

			long endFeatures = (new Date()).getTime();
			Map<String, Object> result = null;
			if (parallel.equals(PARALLEL_EXECUTION.MATCHER_LEVEL))
				result = analyzeDiffByMatcherThread(tl, tr, parallel, matchers);
			else
				result = analyzeDiffByProperty(tl, tr, parallel, matchers);

			long endMatching = (new Date()).getTime();

			long timeParsing = endTree - init;
			long timeFeaturing = endFeatures - endTree;
			long timeMatching = endMatching - endFeatures;
			System.out.println("Time tree " + timeParsing + ", time features " + timeFeaturing + ", time matching "
					+ timeMatching);

			if (result != null) {
				result.put(TIME_TREES_PARSING, timeParsing);
				result.put(TIME_ALL_MATCHER_DIFF, timeMatching);
			}

			return result;
		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<>();
		}
	}

	public Map<String, Object> analyzeDiffByProperty(ITree tl, ITree tr, PARALLEL_EXECUTION parallel,
			Matcher[] matchers) {
		List<Map<String, Object>> matcherResults = new ArrayList<>();

		Map<String, Object> fileResult = new HashMap<>();

		fileResult.put(MATCHERS, matcherResults);

		for (Matcher matcher : matchers) {
			try {

				Map<String, Object> resultJson = computeFitnessFunction(tl, tr, matcher,
						PARALLEL_EXECUTION.PROPERTY_LEVEL.equals(parallel) && this.nrThreads > 1);

				matcherResults.add(resultJson);
			} catch (Exception e) {
				System.err.println("Problems with matcher " + matcher.getClass().getSimpleName());
				e.printStackTrace();
			}
		}
		return fileResult;
	}

	public Map<String, Object> analyzeDiffByMatcherThread(ITree tl, ITree tr, PARALLEL_EXECUTION parallel,
			Matcher[] matchers) {
		List<Map<String, Object>> matcherResults = new ArrayList<>();

		Map<String, Object> fileResult = new HashMap<>();

		fileResult.put(MATCHERS, matcherResults);

		ExecutorService executor = Executors.newFixedThreadPool(matchers.length);
		List<Callable<Map<String, Object>>> callables = new ArrayList<>();

		try {

			for (Matcher matcher : matchers) {

				callables.add(new MatcherCallable(tl, tr, matcher));
			}

			List<Future<Map<String, Object>>> result = executor.invokeAll(callables, this.timeOutSeconds,
					TimeUnit.SECONDS);

			executor.shutdown();

			List<Map<String, Object>> collectedResults = result.stream().map(e -> {
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

			matcherResults.addAll(collectedResults);

		} catch (

		Throwable e) {
			e.printStackTrace();
		}

		return fileResult;
	}

	enum ERROR_TYPE {
		TIMEOUT, EXCEPTION
	}

	public Map<String, Object> returnEmptyResult(Matcher[] matchers, List<Future<Map<String, Object>>> result,
			Future<Map<String, Object>> e, ERROR_TYPE errortype) {
		Map<String, Object> resultFromCancelled = new HashMap<>();

		int indexFuture = result.indexOf(e);
		if (indexFuture >= 0) {

			Matcher matcher = matchers[indexFuture];
			System.out.println("Timeout for " + matcher.getClass().getSimpleName());

			resultFromCancelled.put(MATCHER, matcher.getClass().getSimpleName());

			List<Object> alldiffresults = new ArrayList<>();

			resultFromCancelled.put(CONFIGS, alldiffresults);

			List<GumTreeProperties> combinations = getPropertiesCombinations(matcher);

			for (GumTreeProperties gumTreeProperties : combinations) {

				Map notFinishedConfig = new HashMap();
				notFinishedConfig.put(TIMEOUT, errortype.ordinal() + 1);
				notFinishedConfig.put(CONFIG, gumTreeProperties);
				alldiffresults.add(notFinishedConfig);

			}
		}
		return resultFromCancelled;
	}

	public Map<String, Object> computeFitnessFunction(ITree tl, ITree tr, Matcher matcher, boolean parallel) {
		long initMatcher = (new Date()).getTime();
		List<GumTreeProperties> combinations = null;

		Map<String, Object> result = new HashMap<>();

		String matcherName = matcher.getClass().getSimpleName();

		result.put(MATCHER, matcherName);

		List<Object> alldiffresults = new ArrayList<>();

		result.put(CONFIGS, alldiffresults);

		if (matcher instanceof ConfigurableMatcher) {

			combinations = getPropertiesCombinations(matcher);

		} else {
			// No properties
			combinations = new ArrayList<GumTreeProperties>();
			GumTreeProperties properies = new GumTreeProperties();
			combinations.add(properies);

		}

		int iProperty = 0;

		if (!parallel) {
			for (GumTreeProperties aGumTreeProperties : combinations) {

				iProperty++;
				Map<String, Object> resDiff = runDiff(tl, tr, matcher, aGumTreeProperties);

				alldiffresults.add(resDiff);

			}
		} else {
			// parallel
			try {
				List<Map<String, Object>> results = runInParallelSc(nrThreads, tl, tr, matcher, combinations,
						this.timeOutSeconds);
				for (Map<String, Object> iResult : results) {

					alldiffresults.add(iResult);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		//

		long timeAllConfigs = ((new Date()).getTime() - initMatcher);
		result.put(TIME_MATCHER_ALL_CONFIGS, timeAllConfigs);
		System.out.println("End execution Matcher " + matcherName + ", time " + timeAllConfigs
				+ " milliseconds, Nr_config: " + combinations.size());
		return result;

	}

	public List<GumTreeProperties> getPropertiesCombinations(Matcher matcher) {
		return this.cacheCombinations.get(matcher.getClass().getCanonicalName());
	}

	public List<GumTreeProperties> computesCombinations(Matcher matcher) {
		List<GumTreeProperties> combinations;
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

	public class MatcherCallable implements Callable<Map<String, Object>> {
		ITree tl;
		ITree tr;
		Matcher matcher;

		public MatcherCallable(ITree tl, ITree tr, Matcher matcher) {
			this.tl = tl;
			this.tr = tr;
			this.matcher = matcher;
		}

		@Override
		public Map<String, Object> call() throws Exception {

			return computeFitnessFunction(tl, tr, matcher, false);
		}
	}

	public class DiffCallable implements Callable<Map<String, Object>> {
		ITree tl;
		ITree tr;
		Matcher matcher;
		GumTreeProperties aGumTreeProperties;

		public DiffCallable(ITree tl, ITree tr, Matcher matcher, GumTreeProperties aGumTreeProperties) {
			this.tl = tl;
			this.tr = tr;
			this.matcher = matcher;
			this.aGumTreeProperties = aGumTreeProperties;
		}

		@Override
		public Map<String, Object> call() throws Exception {

			return runDiff(tl, tr,
					// TODO: Workaround: we cannot used the same instance of a matcher to match in
					// parallel two diffs
					matcher.getClass().newInstance()
					// matcher
					// matcher
					, aGumTreeProperties);
		}

	}

	public List<Map<String, Object>> runInParallel(int nrThreads, ITree tl, ITree tr, Matcher matcher,
			List<GumTreeProperties> combinations) throws Exception {

		ExecutorService executor = Executors.newFixedThreadPool(nrThreads);
		List<Callable<Map<String, Object>>> callables = new ArrayList<>();

		for (GumTreeProperties aGumTreeProperties : combinations) {
			callables.add(new DiffCallable(tl, tr, matcher, aGumTreeProperties));
		}

		List<Future<Map<String, Object>>> result = executor.invokeAll(callables);

		executor.shutdown();

		return result.stream().map(e -> {
			try {
				return e.get();
			} catch (InterruptedException | ExecutionException e1) {

				e1.printStackTrace();
				return null;
			}
		}).collect(Collectors.toList());
	}

	public List<Map<String, Object>> runInParallelSc(int nrThreads, ITree tl, ITree tr, Matcher matcher,
			List<GumTreeProperties> combinations, long timeoutSeconds) throws Exception {

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(nrThreads);

		List<DiffCallable> callables = new ArrayList<>();

		for (GumTreeProperties aGumTreeProperties : combinations) {
			callables.add(new DiffCallable(tl, tr, matcher, aGumTreeProperties));
		}

		List<Future<Map<String, Object>>> result = executor.invokeAll(callables, timeoutSeconds, TimeUnit.SECONDS);

		executor.shutdown();

		return result.stream().map(e -> {
			try {
				if (e.isDone() && !e.isCancelled())
					return e.get();
				else {

					HashMap notFinishedConfig = new HashMap();
					notFinishedConfig.put(TIMEOUT, "true");

					int indexFuture = result.indexOf(e);
					if (indexFuture >= 0) {
						// Store the properties of the future not finished
						DiffCallable icallable = callables.get(indexFuture);
						notFinishedConfig.put(CONFIG, icallable.aGumTreeProperties);

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
	 * Computes the diff given a matcher.
	 * 
	 * @param tl
	 * @param tr
	 * @param matcher
	 * @param aGumTreeProperties
	 * @return
	 */
	public Map<String, Object> runDiff(ITree tl, ITree tr, Matcher matcher, GumTreeProperties aGumTreeProperties) {
		long initSingleDiff = new Date().getTime();
		Map<String, Object> resultMap = new HashMap<>();

		// Calling directly to GT.core
		List<Action> actionsAll = computeDiff(tl, tr, matcher, aGumTreeProperties);

		resultMap.put(NRACTIONS, actionsAll.size());

		resultMap.put(NR_INSERT, actionsAll.stream().filter(e -> e instanceof Insert).count());
		resultMap.put(NR_DELETE, actionsAll.stream().filter(e -> e instanceof Delete).count());
		resultMap.put(NR_UPDATE, actionsAll.stream().filter(e -> e instanceof Update).count());
		resultMap.put(NR_MOVE, actionsAll.stream().filter(e -> e instanceof Move).count());
		resultMap.put(NR_TREEINSERT, actionsAll.stream().filter(e -> e instanceof TreeInsert).count());
		resultMap.put(NR_TREEDELETE, actionsAll.stream().filter(e -> e instanceof TreeDelete).count());

		long endSingleDiff = new Date().getTime();
		resultMap.put(TIME, (endSingleDiff - initSingleDiff));

		resultMap.put(CONFIG, aGumTreeProperties);

		return resultMap;

	}

	public List<Action> computeDiff(ITree tl, ITree tr, Matcher matcher, GumTreeProperties properies) {

		return computeDiff(tl, tr, matcher, new ChawatheScriptGenerator(), properies);
	}

	public List<Action> computeDiff(ITree tl, ITree tr, Matcher matcher, EditScriptGenerator edGenerator,
			GumTreeProperties properies) {

		CompositeMatcher cm = (CompositeMatcher) matcher;
		cm.configure(properies);

		MappingStore mappings = matcher.match(tl, tr);

		EditScript actions = edGenerator.computeActions(mappings);

		List<Action> actionsAll = actions.asList();
		return actionsAll;
	}

	public List<GumTreeProperties> computeCartesianProduct(List<ParameterDomain> domains) {

		return cartesianProduct(0, domains);
	}

	private List<GumTreeProperties> cartesianProduct(int index, List<ParameterDomain> domains) {

		List<GumTreeProperties> ret = new ArrayList<>();

		if (index == domains.size()) {
			ret.add(new GumTreeProperties());
		} else {

			ParameterDomain domainOfParameters = domains.get(index);
			for (Object valueFromDomain : domainOfParameters.computeInterval()) {
				List<GumTreeProperties> configurationFromOthersDomains = cartesianProduct(index + 1, domains);
				for (GumTreeProperties configFromOthers : configurationFromOthersDomains) {

					configFromOthers.put(domainOfParameters.getId(), valueFromDomain);
					ret.add(configFromOthers);
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
	private void executionResultToCSV(File out, Map<String, Object> fileresult) throws IOException {

		String sep = ",";
		String endline = "\n";
		String header = "";

		List<Map<String, Object>> matchers = (List<Map<String, Object>>) fileresult.get(MATCHERS);

		if (matchers == null) {
			System.err.println("Problems when saving results: No matchers for identifier " + out.getName());
			return;
		}

		String row = "";
		boolean first = true;
		FileWriter fw = new FileWriter(out);
		for (Map<String, Object> map : matchers) {

			if (map == null || !map.containsKey(MATCHER) || map.get(MATCHER) == null) {
				System.out.println("No matcher in results ");
				continue;
			}

			String xmatcher = map.get(MATCHER).toString();

			List<Map<String, Object>> configs = (List<Map<String, Object>>) map.get(CONFIGS);
			for (Map<String, Object> config : configs) {
				// re-init the row

				if (config == null)
					continue;

				GumTreeProperties gtp = (config.containsKey(CONFIG)) ? (GumTreeProperties) config.get(CONFIG)
						: new GumTreeProperties();
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

					row += gtp.getProperties().keySet().size() + sep;
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

	public JsonObject extractTreeFeatures(ITree tl) {
		JsonObject thFeatures = new JsonObject();
		thFeatures.addProperty(SIZE, tl.getMetrics().size);
		thFeatures.addProperty(HEIGHT, tl.getMetrics().height);
		thFeatures.addProperty(STRUCTHASH, tl.getMetrics().structureHash);
		return thFeatures;
	}

	public Map<String, Object> extractTreeFeaturesMap(ITree tl) {
		Map<String, Object> fileresult = new HashMap<>();
		fileresult.put(SIZE, tl.getMetrics().size);
		fileresult.put(HEIGHT, tl.getMetrics().height);
		fileresult.put(STRUCTHASH, tl.getMetrics().structureHash);
		return fileresult;
	}

	private void metadataToCSV(File nameFile, Map<String, Pair<Map, Map>> treeProperties,
			Map<String, Object> fileResult) throws IOException {

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
		List<Map<String, Object>> matchersInfo = (List<Map<String, Object>>) fileResult.get(MATCHERS);

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

			row += fileResult.get(TIME_TREES_PARSING) + sep;
			row += fileResult.get(TIME_ALL_MATCHER_DIFF) + sep;

			for (Matcher matcher : allMatchers) {
				Optional<Map<String, Object>> findFirst = matchersInfo.stream()
						.filter(e -> e.get(MATCHER).equals(matcher.getClass().getSimpleName())).findFirst();
				if (findFirst.isPresent()) {
					Map<String, Object> pM = findFirst.get();

					row += pM.get(TIME_MATCHER_ALL_CONFIGS) + sep;
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
}
