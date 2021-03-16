package fr.gumtree.autotuning.searchengines;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.ConfigurableMatcher;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumtreeProperties;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.utils.Pair;
import com.google.gson.JsonObject;

import fr.gumtree.autotuning.domain.ParameterDomain;
import fr.gumtree.autotuning.entity.CaseResult;
import fr.gumtree.autotuning.entity.MatcherResult;
import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.entity.SingleDiffResult;
import fr.gumtree.autotuning.gumtree.ASTMODE;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration;
import fr.gumtree.autotuning.gumtree.GTProxy;
import fr.gumtree.autotuning.gumtree.ParametersResolvers;
import fr.gumtree.autotuning.outils.Constants;
import fr.gumtree.autotuning.treebuilder.ITreeBuilder;
import fr.gumtree.autotuning.treebuilder.JDTTreeBuilder;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ExhaustiveEngine implements SearchMethod {

	long timeOutSeconds = 60 * 60; // 60 min

	public enum PARALLEL_EXECUTION {
		MATCHER_LEVEL, PROPERTY_LEVEL, NONE
	}

	// TODO: we cannot use the same generator when we execute on parallel.
//	private ChawatheScriptGenerator editScriptGenerator = new ChawatheScriptGenerator();

	public Matcher[] allMatchers = new Matcher[] {
			//
			new CompositeMatchers.SimpleGumtree(),
			//
			new CompositeMatchers.ClassicGumtree(),
			//
			new CompositeMatchers.CompleteGumtreeMatcher(),
			//
			// new CompositeMatchers.ChangeDistiller(),
			//
			new CompositeMatchers.XyMatcher(),

	};
	/**
	 * Indicates the number of threads used in the property parallelization.
	 */
	private int nrThreads = 10;

	Map<String, List<GumtreeProperties>> cacheCombinations = new HashMap<String, List<GumtreeProperties>>();

	public ExhaustiveEngine() {
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
	public CaseResult analyzeCase(ITreeBuilder treeBuilder, String diffId, File previousVersion, File postVersion,
			PARALLEL_EXECUTION parallel, Map<String, Pair<Map, Map>> treeProperties, Matcher[] matchers) {
		try {
			Tree tl = treeBuilder.build(previousVersion);
			Tree tr = treeBuilder.build(postVersion);
			long init = (new Date()).getTime();

			long endTree = (new Date()).getTime();
			// TODO: remove from here
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
	private CaseResult analyzeDiffByPropertyParallel(Tree tl, Tree tr, PARALLEL_EXECUTION parallel,
			Matcher[] matchers) {

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

	private CaseResult analyzeDiffByMatcherThread(Tree tl, Tree tr, PARALLEL_EXECUTION parallel, Matcher[] matchers) {
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

	protected MatcherResult returnEmptyResult(Matcher[] matchers, List<Future<MatcherResult>> result,
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
				notFinishedConfig.put(Constants.TIMEOUT, errortype.ordinal() + 1);
				notFinishedConfig.put(Constants.CONFIG, GumtreeProperties);

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
	protected MatcherResult runSingleMatcherMultipleConfigurations(Tree tl, Tree tr, Matcher matcher,
			boolean parallel) {
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
			int i = 0;
			for (GumtreeProperties aGumtreeProperties : combinations) {

				// System.out.println(aGumtreeProperties);
				GTProxy gumtreeproxy = new GTProxy();
				SingleDiffResult resDiff = gumtreeproxy.runDiff(tl, tr, matcher, aGumtreeProperties);

				if (resDiff != null) {
					i = printResult(matcherName, combinations.size(), i, resDiff);
					alldiffresults.add(resDiff);
				}
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

	public int printResult(String matcherName, int size, int i, SingleDiffResult resDiff) {
		System.out.println("--" + (i++) + "/" + size);
		// System.out.println("nr actions: " + resDiff.getDiff().editScript.size());
		System.out.println("nr actions: " + resDiff.get(Constants.NRACTIONS));
		System.out.println("time: " + resDiff.get(Constants.TIME));
		System.out.println("config: " + matcherName + " " + resDiff.get(Constants.CONFIG));

		System.out.println("---");
		return i;
	}

	public List<GumtreeProperties> getPropertiesCombinations(Matcher matcher) {

		if (this.cacheCombinations == null || this.cacheCombinations.isEmpty()) {
			this.initCacheCombinationProperties();
		}
		return this.cacheCombinations.get(matcher.getClass().getCanonicalName());
	}

	protected List<GumtreeProperties> computesCombinations(Matcher matcher) {
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
			GTProxy gumtreeproxy = new GTProxy();
			SingleDiffResult result = gumtreeproxy.runDiff(tl, tr,
					// TODO: Workaround: we cannot used the same instance of a matcher to match in
					// parallel two diffs
					matcher.getClass().newInstance()
					// matcher
					// matcher
					, aGumtreeProperties);

			printResult(matcher.getClass().getSimpleName(), 0, 0, result);

			return result;
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
	protected List<SingleDiffResult> runInParallelMultipleConfigurations(int nrThreads, Tree tl, Tree tr,
			Matcher matcher, List<GumtreeProperties> combinations, long timeoutSeconds) throws Exception {

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
					notFinishedConfig.put(Constants.TIMEOUT, "true");

					int indexFuture = result.indexOf(e);
					if (indexFuture >= 0) {
						// Store the properties of the future not finished
						DiffCallable icallable = callables.get(indexFuture);
						notFinishedConfig.put(Constants.CONFIG, icallable.aGumtreeProperties);

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

	public Matcher[] getMatchers() {
		return allMatchers;
	}

	public void setMatchers(Matcher[] matchers) {
		this.allMatchers = matchers;
	}

	/**
	 * TODO: to remove??
	 * 
	 * @param tl
	 * @return
	 */
	protected JsonObject extractTreeFeatures(Tree tl) {
		JsonObject thFeatures = new JsonObject();
		thFeatures.addProperty(Constants.SIZE, tl.getMetrics().size);
		thFeatures.addProperty(Constants.HEIGHT, tl.getMetrics().height);
		thFeatures.addProperty(Constants.STRUCTHASH, tl.getMetrics().structureHash);
		return thFeatures;
	}

	protected Map<String, Object> extractTreeFeaturesMap(Tree tl) {
		Map<String, Object> fileresult = new HashMap<>();
		fileresult.put(Constants.SIZE, tl.getMetrics().size);
		fileresult.put(Constants.HEIGHT, tl.getMetrics().height);
		fileresult.put(Constants.STRUCTHASH, tl.getMetrics().structureHash);
		return fileresult;
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

	@Override
	public ResponseBestParameter computeBestGlobal(File dataFilePairs) throws Exception {

		return computeBestGlobal(dataFilePairs, ASTMODE.GTSPOON, new ExecutionConfiguration());
	}

	@Override
	public ResponseBestParameter computeBestGlobal(File dataFilePairs, ASTMODE astmode,
			ExecutionConfiguration configuration) throws Exception {
		PARALLEL_EXECUTION parallel = PARALLEL_EXECUTION.PROPERTY_LEVEL;
		Map<String, Pair<Map, Map>> treeCharacteristics = new HashMap<String, Pair<Map, Map>>();

		// We select the parser
		ITreeBuilder treebuilder = null;
		if (ASTMODE.GTSPOON.equals(astmode)) {
			treebuilder = new SpoonTreeBuilder();
		} else if (ASTMODE.JDT.equals(astmode)) {
			treebuilder = new JDTTreeBuilder();
		} else {
			System.err.println("Mode not configured " + astmode);
		}

		BufferedReader reader;
		MapList<String, Integer> results = new MapList<>();

		try {
			// We read the file with the all pairs
			reader = new BufferedReader(new FileReader(dataFilePairs));
			String line = reader.readLine();

			while (line != null) {
				System.out.println(line);

				System.out.println("Line " + line);

				String[] sp = line.split(" ");

				// Compute all diffs
				CaseResult caseResult = this.analyzeCase(treebuilder, sp[0], new File(sp[0]), new File(sp[1]), parallel,
						treeCharacteristics, this.allMatchers);

				// Navegate over the cases.
				for (MatcherResult mresult : caseResult.getResultByMatcher().values()) {

					for (SingleDiffResult diffResult : mresult.getAlldiffresults()) {
						int isize = (int) diffResult.get(Constants.NRACTIONS);
						GumtreeProperties gt = (GumtreeProperties) diffResult.get(Constants.CONFIG);

						// maybe to replace by a toString
						String oneBest = GTProxy.plainProperties(new JsonObject(), mresult.getMatcherName(), gt);
						results.add(oneBest, isize);

					}

				}

				// Next line
				line = reader.readLine();

			}
			reader.close();

			// Now to summarize
			ResponseBestParameter bestResult = new ResponseBestParameter();

			// let's compute the median of each conf
			Map<String, Double> medianByConfiguration = new HashMap<>();

			for (String aConfigresult : results.keySet()) {

				DescriptiveStatistics stats = new DescriptiveStatistics();

				List<Integer> allSizesOfConfigs = results.get(aConfigresult);
				for (Integer aSize : allSizesOfConfigs) {
					stats.addValue(aSize);
				}
				double median = stats.getPercentile(50);
				medianByConfiguration.put(aConfigresult, median);

			}
			// Choose the config with best median

			List<String> best = medianByConfiguration.keySet().stream()
					.sorted((e1, e2) -> medianByConfiguration.get(e1).compareTo(medianByConfiguration.get(e2)))
					.collect(Collectors.toList());

			bestResult.setMedian(medianByConfiguration.get(best.get(0)));
			bestResult.setBest(best.get(0));

		} catch (Exception e) {

			e.printStackTrace();
		}

		return null;
	}

	@Override
	public ResponseBestParameter computeBestLocal(File left, File right) throws Exception {
		return computeBestLocal(left, right, ASTMODE.GTSPOON, new ExecutionConfiguration());

	}

	@Override
	public ResponseBestParameter computeBestLocal(File left, File right, ASTMODE astmode,
			ExecutionConfiguration configuration) throws Exception {

		Map<String, Pair<Map, Map>> treeProperties = new HashMap<String, Pair<Map, Map>>();

		PARALLEL_EXECUTION parallel = PARALLEL_EXECUTION.MATCHER_LEVEL;

		ITreeBuilder treebuilder = null;
		if (ASTMODE.GTSPOON.equals(astmode)) {
			treebuilder = new SpoonTreeBuilder();
		} else if (ASTMODE.JDT.equals(astmode)) {
			treebuilder = new JDTTreeBuilder();
		} else {
			System.err.println("Mode not configured " + astmode);
		}

		CaseResult caseResult = this.analyzeCase(treebuilder, left.getName(), left, right, parallel, treeProperties,
				this.allMatchers);

		int min = Integer.MAX_VALUE;
		List<Pair<String, GumtreeProperties>> minDiff = new ArrayList<>();

		for (MatcherResult mresult : caseResult.getResultByMatcher().values()) {

			for (SingleDiffResult diffResult : mresult.getAlldiffresults()) {

				if (diffResult == null || diffResult.get(Constants.NRACTIONS) == null) {
					continue;
				}

				int isize = (int) diffResult.get(Constants.NRACTIONS);
				if (isize <= min) {

					// We discard the others in case is strictly less
					if (isize < min) {
						minDiff.clear();
					}
					min = isize;

					GumtreeProperties gt = (GumtreeProperties) diffResult.get(Constants.CONFIG);
					minDiff.add(new Pair<>(mresult.getMatcherName(), gt));

				}

			}

		}
		ResponseBestParameter bestResult = new ResponseBestParameter();
		bestResult.setMedian(min);

		for (Pair<String, GumtreeProperties> pair : minDiff) {
			String oneBest = GTProxy.plainProperties(new JsonObject(), pair.first, pair.second);
			bestResult.getAllBest().add(oneBest);
		}

		return bestResult;
	}

}
