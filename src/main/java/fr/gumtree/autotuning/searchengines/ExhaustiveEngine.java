package fr.gumtree.autotuning.searchengines;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
import fr.gumtree.autotuning.gumtree.DiffProxy;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.gumtree.GTProxy;
import fr.gumtree.autotuning.gumtree.ParametersResolvers;
import fr.gumtree.autotuning.outils.Constants;
import fr.gumtree.autotuning.outils.DatOutputEngine;
import fr.gumtree.autotuning.treebuilder.ITreeBuilder;
import fr.gumtree.autotuning.treebuilder.JDTTreeBuilder;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ExhaustiveEngine implements OptimizationMethod {

	public static final String SUMMARY_CASES = "summary_cases_";

	public enum PARALLEL_EXECUTION {
		MATCHER_LEVEL, PROPERTY_LEVEL, NONE
	}

	DiffProxy gumtreeproxy = null;

	public ExhaustiveEngine(DiffProxy gumtreeproxy) {
		this.gumtreeproxy = gumtreeproxy;
	}

	public Matcher[] allMatchers = new Matcher[] { new CompositeMatchers.SimpleGumtree(),
			new CompositeMatchers.ClassicGumtree(), new CompositeMatchers.HybridGumtree(),

	};

	Map<String, List<GumtreeProperties>> cacheCombinations = new HashMap<String, List<GumtreeProperties>>();

	public ExhaustiveEngine() {
		super();
		this.gumtreeproxy = new GTProxy();
	}

	public void initCacheCombinationProperties(ParametersResolvers domain) {
		this.cacheCombinations.clear();
		for (Matcher matcher : allMatchers) {
			List<GumtreeProperties> allCombinations = computesCombinations(matcher, domain);
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
			ExecutionConfiguration configuration, Map<String, Pair<Map, Map>> treeProperties, Matcher[] matchers) {

		try {

			System.out.println("Starting experiment: " + configuration);

			Tree tl = treeBuilder.build(previousVersion);
			Tree tr = treeBuilder.build(postVersion);
			long init = (new Date()).getTime();

			long endTree = (new Date()).getTime();
			// TODO: remove from here
			treeProperties.put(diffId, new Pair<Map, Map>(extractTreeFeaturesMap(tl), extractTreeFeaturesMap(tr)));

			long endFeatures = (new Date()).getTime();
			CaseResult result = null;
			if (configuration.getParalelisationMode().equals(PARALLEL_EXECUTION.MATCHER_LEVEL))
				result = analyzeDiffByMatcherThread(tl, tr, configuration, matchers);
			else if (configuration.getParalelisationMode().equals(PARALLEL_EXECUTION.PROPERTY_LEVEL))
				result = analyzeDiffByPropertyParallel(tl, tr, configuration, matchers);

			else if (configuration.getParalelisationMode().equals(PARALLEL_EXECUTION.NONE))
				result = analyzeDiffByPropertySerial(tl, tr, matchers);
			else {
				throw new IllegalAccessError(
						"Option not recognised " + configuration.getParalelisationMode().toString());
			}

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

			if (result != null) {

				File parent = new File(
						configuration.getDirDiffTreeSerialOutput().getAbsolutePath() + File.separator + diffId);
				parent.mkdirs();
				File outResults = new File(parent.getAbsoluteFile() + File.separator + SUMMARY_CASES + diffId + "_"
						+ treeBuilder.modelType().name() + ".csv");

				DatOutputEngine.executionResultToCSV(outResults, result);
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
	private CaseResult analyzeDiffByPropertyParallel(Tree tl, Tree tr, ExecutionConfiguration configuration,
			Matcher[] matchers) {

		CaseResult resultsForCase = new CaseResult();

		for (Matcher matcher : matchers) {
			try {

				MatcherResult resultFromMatcher = runSingleMatcherMultipleParameters(tl, tr, matcher, configuration);

				resultsForCase.getResultByMatcher().put(matcher, resultFromMatcher);
			} catch (Exception e) {
				System.err.println("Problems with matcher " + matcher.getClass().getSimpleName());
				e.printStackTrace();
			}
		}
		return resultsForCase;
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
	private CaseResult analyzeDiffByPropertySerial(Tree tl, Tree tr, Matcher[] matchers) {

		CaseResult resultsForCase = new CaseResult();

		for (Matcher matcher : matchers) {
			try {

				MatcherResult resultFromMatcher = runSingleMatcherSerial(tl, tr, matcher);

				resultsForCase.getResultByMatcher().put(matcher, resultFromMatcher);
			} catch (Exception e) {
				System.err.println("Problems with matcher " + matcher.getClass().getSimpleName());
				e.printStackTrace();
			}
		}
		return resultsForCase;
	}

	protected MatcherResult runSingleMatcherSerial(Tree tl, Tree tr, Matcher matcher) {
		long initMatcher = (new Date()).getTime();
		List<GumtreeProperties> combinations = null;

		String matcherName = getNameOfMatcher(matcher);

		MatcherResult result = new MatcherResult(matcherName, matcher);

		ParametersResolvers domain = ParametersResolvers.defaultDomain;
		combinations = getConfigurations(matcher, domain);

		List<SingleDiffResult> alldiffresults = runSingleMatcherSerial(tl, tr, matcher, combinations);

		result.setAlldiffresults(alldiffresults);

		long timeAllConfigs = ((new Date()).getTime() - initMatcher);
		result.setTimeAllConfigs(timeAllConfigs);
		System.out.println("End execution Matcher " + matcherName + ", time " + timeAllConfigs
				+ " milliseconds, Nr_config: " + combinations.size());
		return result;

	}

	private String getNameOfMatcher(Matcher matcher) {
		return matcher.getClass().getSimpleName();
	}

	public List<SingleDiffResult> runSingleMatcherSerial(Tree tl, Tree tr, Matcher matcher,
			List<GumtreeProperties> combinations) {
		List<SingleDiffResult> alldiffresults = new ArrayList<>();

		int i = 0;
		for (GumtreeProperties aGumtreeProperties : combinations) {
			// GTProxy gumtreeproxy = new GTProxy();
			SingleDiffResult resDiff = gumtreeproxy.runDiff(tl, tr, matcher, aGumtreeProperties);

			if (resDiff != null) {
				i = printResult(getNameOfMatcher(matcher), combinations.size(), i, resDiff);
				alldiffresults.add(resDiff);
			}
		}
		return alldiffresults;
	}

	private CaseResult analyzeDiffByMatcherThread(Tree tl, Tree tr, ExecutionConfiguration configuration,
			Matcher[] matchers) {

		CaseResult fileResult = new CaseResult();

		ExecutorService executor = Executors.newFixedThreadPool(matchers.length);
		List<Callable<MatcherResult>> callables = new ArrayList<>();

		try {

			for (Matcher matcher : matchers) {

				callables.add(new MatcherCallable(tl, tr, matcher, configuration));
			}

			List<Future<MatcherResult>> result = executor.invokeAll(callables, configuration.getTimeOut(),
					configuration.getTimeUnit());

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
		MatcherResult resultFromCancelled = new MatcherResult("none", null);

		int indexFuture = result.indexOf(e);
		if (indexFuture >= 0) {

			Matcher matcher = matchers[indexFuture];
			System.out.println("Timeout for " + matcher.getClass().getSimpleName());

			resultFromCancelled.setMatcherName(matcher.getClass().getSimpleName());

			List<SingleDiffResult> alldiffresults = new ArrayList<>();

			resultFromCancelled.setAlldiffresults(alldiffresults);

			ParametersResolvers domain = ParametersResolvers.defaultDomain;
			List<GumtreeProperties> combinations = getPropertiesCombinations(matcher, domain);

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
	 * Gets all combinations of properties and run eachs on parallel.
	 * 
	 * @param tl
	 * @param tr
	 * @param matcher
	 * @param parallel
	 * @return
	 */
	protected MatcherResult runSingleMatcherMultipleParameters(Tree tl, Tree tr, Matcher matcher,
			ExecutionConfiguration configuration) {
		long initMatcher = (new Date()).getTime();
		List<GumtreeProperties> combinations = null;

		String matcherName = matcher.getClass().getSimpleName();

		MatcherResult result = new MatcherResult(matcherName, matcher);

		List<SingleDiffResult> alldiffresults = new ArrayList<>();

		ParametersResolvers domain = ParametersResolvers.defaultDomain;
		combinations = getConfigurations(matcher, domain);

		try {
			List<SingleDiffResult> results = runSingleMatcherMultipleParameters(tl, tr, matcher, combinations,
					configuration.getTimeOut(), configuration.getTimeUnit(), configuration.getNumberOfThreads());
			for (SingleDiffResult iResult : results) {

				alldiffresults.add(iResult);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		result.setAlldiffresults(alldiffresults);

		long timeAllConfigs = ((new Date()).getTime() - initMatcher);
		result.setTimeAllConfigs(timeAllConfigs);
		System.out.println("End execution Matcher " + matcherName + ", time " + timeAllConfigs
				+ " milliseconds, Nr_config: " + combinations.size());
		return result;

	}

	public List<GumtreeProperties> getConfigurations(Matcher matcher, ParametersResolvers domain) {
		List<GumtreeProperties> combinations;
		if (matcher instanceof ConfigurableMatcher) {

			combinations = getPropertiesCombinations(matcher, domain);

		} else {
			// The matcher does not allow customization
			combinations = new ArrayList<GumtreeProperties>();
			GumtreeProperties properies = new GumtreeProperties();
			combinations.add(properies);

		}
		return combinations;
	}

	public int printResult(String matcherName, int size, int i, SingleDiffResult resDiff) {

		if (resDiff != null) {
			System.out.println("--" + (i++) + "/" + size);
			// System.out.println("nr actions: " + resDiff.getDiff().editScript.size());
			System.out.println("nr actions: " + resDiff.get(Constants.NRACTIONS));
			System.out.println("time: " + resDiff.get(Constants.TIME));
			System.out.println("config: " + matcherName + " " + resDiff.get(Constants.CONFIG));

			System.out.println("---");
		}
		return i;
	}

	public List<GumtreeProperties> getPropertiesCombinations(Matcher matcher, ParametersResolvers domain) {

		if (this.cacheCombinations == null || this.cacheCombinations.isEmpty()) {
			this.initCacheCombinationProperties(domain);
		}
		return this.cacheCombinations.get(matcher.getClass().getCanonicalName());
	}

	public List<GumtreeProperties> computesCombinations(Matcher matcher, ParametersResolvers domain) {
		List<GumtreeProperties> combinations;
		ConfigurableMatcher configurableMatcher = (ConfigurableMatcher) matcher;

		// We collect the options of the matcher
		Set<ConfigurationOptions> options = configurableMatcher.getApplicableOptions();

		List<ParameterDomain> domains = new ArrayList<>();

		// We collect the domains
		for (ConfigurationOptions option : options) {

			ParameterDomain<?> paramOption = domain.getParametersDomain().get(option);
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
		ExecutionConfiguration configuration;

		public MatcherCallable(Tree tl, Tree tr, Matcher matcher, ExecutionConfiguration configuration) {
			this.tl = tl;
			this.tr = tr;
			this.matcher = matcher;
			this.configuration = configuration;
		}

		@Override
		public MatcherResult call() throws Exception {

			// TODO: the initial v2 executed in paralell, why
			// return runSingleMatcherMultipleParameters(tl, tr, matcher,
			// this.configuration);

			long initMatcher = (new Date()).getTime();
			List<GumtreeProperties> combinations = null;

			String matcherName = matcher.getClass().getSimpleName();

			MatcherResult result = new MatcherResult(matcherName, matcher);

			List<SingleDiffResult> alldiffresults = new ArrayList<>();

			ParametersResolvers domain = ParametersResolvers.defaultDomain;
			combinations = getConfigurations(matcher, domain);

			// Sequence
			for (GumtreeProperties aGumtreeProperties : combinations) {

				SingleDiffResult resDiff = gumtreeproxy.runDiff(tl, tr, matcher, aGumtreeProperties);

				alldiffresults.add(resDiff);

			}

			result.setAlldiffresults(alldiffresults);

			long timeAllConfigs = ((new Date()).getTime() - initMatcher);
			result.setTimeAllConfigs(timeAllConfigs);
			System.out.println("End Sequential execution Matcher " + matcherName + ", time " + timeAllConfigs
					+ " milliseconds, Nr_config: " + combinations.size());
			return result;

		}
	}

	public class DiffCallable implements Callable<SingleDiffResult> {
		Tree tl;
		Tree tr;
		Matcher matcher;
		GumtreeProperties aGumtreeProperties;
		int idConfig;
		int totalConfig;

		public DiffCallable(int idConfing, int totalConfig, Tree tl, Tree tr, Matcher matcher,
				GumtreeProperties aGumtreeProperties) {
			this.idConfig = idConfing;
			this.totalConfig = totalConfig;
			this.tl = tl;
			this.tr = tr;
			this.matcher = matcher;
			this.aGumtreeProperties = aGumtreeProperties;
		}

		@Override
		public SingleDiffResult call() throws Exception {

			System.out.println("Launch " + aGumtreeProperties.toString());

			// GTProxy gumtreeproxy = new GTProxy();
			SingleDiffResult result = gumtreeproxy.runDiff(tl, tr,
					// TODO: Workaround: we cannot used the same instance of a matcher to match in
					// parallel two diffs
					matcher.getClass().newInstance()
					// matcher
					// matcher
					, aGumtreeProperties);

			// printResult(matcher.getClass().getSimpleName(), this.totalConfig,
			// this.idConfig, result);

			return result;
		}

	}

	/**
	 * Executes a list of configuration from a Matcher in parallel.
	 * 
	 * @param tl
	 * @param tr
	 * @param matcher
	 * @param combinations
	 * @param timeoutSeconds
	 * @param nrThreads
	 * 
	 * @return
	 * @throws Exception
	 */
	public List<SingleDiffResult> runSingleMatcherMultipleParameters(Tree tl, Tree tr, Matcher matcher,
			List<GumtreeProperties> combinations, long timeoutSeconds, TimeUnit unit, int nrThreads) throws Exception {

		System.out.println("nrThreads " + nrThreads);
		ScheduledExecutorService executor = Executors.newScheduledThreadPool(nrThreads);

		List<DiffCallable> callables = new ArrayList<>();
		int i = 0;
		for (GumtreeProperties aGumtreeProperties : combinations) {
			callables.add(new DiffCallable(i++, combinations.size(), tl, tr, matcher, aGumtreeProperties));
		}

		List<Future<SingleDiffResult>> result = executor.invokeAll(callables, timeoutSeconds, unit);

		executor.shutdown();

		return result.stream().map(e -> {
			try {
				if (e.isDone() && !e.isCancelled()) {
					SingleDiffResult singleDiffResult = e.get();
					System.out.println("Finishing thread: " + singleDiffResult.retrievePlainConfiguration());
					return singleDiffResult;
				} else {

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

	@Override
	public ResponseBestParameter computeBestGlobal(File dataFilePairs, ExecutionConfiguration configuration)
			throws Exception {

		Map<String, Pair<Map, Map>> treeCharacteristics = new HashMap<String, Pair<Map, Map>>();
		DatOutputEngine saver = new DatOutputEngine(getDiffId(dataFilePairs));

		// We select the parser
		ASTMODE astmode = configuration.getAstmode();
		ITreeBuilder treebuilder = null;
		if (ASTMODE.GTSPOON.equals(astmode)) {
			treebuilder = new SpoonTreeBuilder();
		} else if (ASTMODE.JDT.equals(astmode)) {
			treebuilder = new JDTTreeBuilder();
		} else {
			System.err.println("Mode not configured " + astmode);
		}

		BufferedReader reader;
		ResultByConfig results = new ResultByConfig();

		try {
			// We read the file with the all pairs
			reader = new BufferedReader(new FileReader(dataFilePairs));
			String line = reader.readLine();

			while (line != null) {
				System.out.println(line);

				System.out.println("Line " + line);

				String[] sp = line.split(" ");

				// Compute all diffs
				String filenameLeft = sp[0];
				String filenameRight = sp[1];
				File previousVersion = new File(filenameLeft);
				File postVersion = new File(filenameRight);

				String id = previousVersion.getName().replace("_s.java", "").replace(".java", "");

				CaseResult caseResult = this.analyzeCase(treebuilder, id, previousVersion, postVersion, configuration,
						treeCharacteristics, this.allMatchers);

				// Navegate over the cases.
				for (MatcherResult mresult : caseResult.getResultByMatcher().values()) {

					for (SingleDiffResult diffResult : mresult.getAlldiffresults()) {
						int isize = (int) diffResult.get(Constants.NRACTIONS);

						// maybe to replace by a toString
						String plainProperties = diffResult.retrievePlainConfiguration();

						results.add(plainProperties, isize);

						if (configuration.isSaveScript()) {

							saver.saveUnifiedNotDuplicated(getDiffId(previousVersion), plainProperties,
									diffResult.getDiff(), configuration.getDirDiffTreeSerialOutput());

						}

					}

				}

				// Next line
				line = reader.readLine();

			}
			reader.close();

			saver.saveRelations(configuration.getDirDiffTreeSerialOutput());

			saver.saveSummarization(configuration.getDirDiffTreeSerialOutput(), results);

			ResponseBestParameter bestResult = summarizeResultsForGlobal(results, configuration.getMetric());

			return bestResult;

		} catch (Exception e) {

			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Returns the summarization of the results
	 * 
	 * @param configuration
	 * @param results
	 * @return
	 */
	public ResponseBestParameter summarizeResultsForGlobal(ResultByConfig results, METRIC metric) {
		// Now to summarize
		ResponseBestParameter bestResult = new ResponseBestParameter();

		// let's compute the median of each conf
		Map<String, Double> medianByConfiguration = new HashMap<>();

		Double minMedian = Double.MAX_VALUE;
		int nrEvaluations = 0;

		int i = 0;
		for (String aConfigresult : results.keySet()) {

			DescriptiveStatistics stats = new DescriptiveStatistics();

			List<Integer> allSizesOfConfigs = results.get(aConfigresult);
			if (allSizesOfConfigs.size() > nrEvaluations)
				nrEvaluations = allSizesOfConfigs.size();

			for (Integer aSize : allSizesOfConfigs) {
				stats.addValue(aSize);
			}
			double median = 0;
			if (metric.equals(METRIC.MEDIAN))
				median = stats.getPercentile(50);

			else if (metric.equals(METRIC.MEAN))
				median = stats.getMean();

			medianByConfiguration.put(aConfigresult, median);

			if (median < minMedian) {
				minMedian = median;
			}

			System.out.println(++i + " " + aConfigresult + " " + median + ": " + allSizesOfConfigs);
		}
		final Double minValuemedian = minMedian;
		// Choose the config with best median

		System.out.println("Min " + metric.toString() + ": " + +minValuemedian);

		List<String> bests = medianByConfiguration.keySet().stream()
				.filter(e -> minValuemedian.equals(medianByConfiguration.get(e))).collect(Collectors.toList());

		System.out.println("Total best configs " + bests.size() + " / " + results.keySet().size());
		//
		System.out.println("Bests (" + bests.size() + ") : " + bests);

		bestResult.setNumberOfEvaluatedPairs(nrEvaluations);
		bestResult.setMedian(minMedian);
		bestResult.setBest(bests);
		return bestResult;
	}

	public ResponseBestParameter summarizeResultsForLocal(List<ResultByConfig> allLocalResults, METRIC metric) {

		Map<String, Integer> freqBest = new HashMap<String, Integer>();

		for (ResultByConfig resultByConfig : allLocalResults) {
			int min = Integer.MAX_VALUE;
			List<String> currentMinConfigs = new ArrayList<>();

			// For a results (local, i.e. one pair) we find the configs with shortest ed
			for (String aCondif : resultByConfig.keySet()) {

				int sizeConfig = resultByConfig.get(aCondif).get(0);

				if (sizeConfig <= min) {

					// it's a new min, we remove others
					if (sizeConfig < min) {
						currentMinConfigs.clear();
					}

					currentMinConfigs.add(aCondif);
					min = sizeConfig;

				}
			}

			// We increment the best counter with the best from the result

			for (String minConfig : currentMinConfigs) {

				int count = freqBest.containsKey(minConfig) ? freqBest.get(minConfig) : 0;
				freqBest.put(minConfig, count + 1);

			}

		}

		// Find the best local according with the number of times is the best

		ResponseBestParameter bestResult = new ResponseBestParameter();

		List<String> bestMinConfig = new ArrayList<>();
		int min = Integer.MAX_VALUE;
		for (String aConfig : freqBest.keySet()) {

			int sizeConfig = freqBest.get(aConfig);

			if (sizeConfig <= min) {

				// it's a new min, we remove others
				if (sizeConfig < min) {
					bestMinConfig.clear();
				}

				bestMinConfig.add(aConfig);
				min = sizeConfig;

			}

		}

		bestResult.setBest(bestMinConfig);
		bestResult.setNumberOfEvaluatedPairs(allLocalResults.size());

		return bestResult;

	}

	@Override
	public ResponseBestParameter computeBestLocal(File left, File right, ExecutionConfiguration configuration)
			throws Exception {

		ASTMODE astmode = configuration.getAstmode();

		ITreeBuilder treebuilder = null;
		if (ASTMODE.GTSPOON.equals(astmode)) {
			treebuilder = new SpoonTreeBuilder();
		} else if (ASTMODE.JDT.equals(astmode)) {
			treebuilder = new JDTTreeBuilder();
		} else {
			System.err.println("Mode not configured " + astmode);
		}

		ResponseBestParameter bestResult = computeBestLocal(treebuilder, left, right, configuration);

		return bestResult;
	}

	public ResponseBestParameter computeBestLocal(ITreeBuilder treebuilder, File left, File right,
			ExecutionConfiguration configuration) throws IOException, NoSuchAlgorithmException, Exception {
		return computeBestLocal(treebuilder, getDiffId(left), left, right, configuration);
	}

	public ResponseBestParameter computeBestLocal(ITreeBuilder treebuilder, String diffId, File left, File right,
			ExecutionConfiguration configuration) throws IOException, NoSuchAlgorithmException, Exception {
		Map<String, Pair<Map, Map>> treeProperties = new HashMap<String, Pair<Map, Map>>();

		String outDiffId = configuration.getDirDiffTreeSerialOutput().getAbsolutePath() + File.separator + diffId;

		if (new File(outDiffId).exists()) {
			System.out.println("Existing " + outDiffId);
			return null;
		}

		DatOutputEngine saver = new DatOutputEngine(diffId);

		CaseResult caseResult = this.analyzeCase(treebuilder, diffId, left, right, configuration, treeProperties,
				this.allMatchers);

		int min = Integer.MAX_VALUE;
		// List with all the configs that produce the min
		List<Pair<String, GumtreeProperties>> minDiff = new ArrayList<>();

		ResultByConfig results = new ResultByConfig();

		for (MatcherResult mresult : caseResult.getResultByMatcher().values()) {

			for (SingleDiffResult diffResult : mresult.getAlldiffresults()) {

				if (diffResult == null || diffResult.get(Constants.NRACTIONS) == null) {
					continue;
				}

				int isize = (int) diffResult.get(Constants.NRACTIONS);

				GumtreeProperties gt = (GumtreeProperties) diffResult.get(Constants.CONFIG);
				String plainProperty = diffResult.retrievePlainConfiguration();
				results.add(plainProperty, isize);

				if (isize <= min) {

					// We discard the others in case is strictly less
					if (isize < min) {
						minDiff.clear();
					}
					min = isize;

					minDiff.add(new Pair<>(mresult.getMatcherName(), gt));

				}

				if (configuration.isSaveScript()) {
					saver.saveUnifiedNotDuplicated(getDiffId(left), plainProperty, diffResult.getDiff(),
							configuration.getDirDiffTreeSerialOutput());

				}

			}

		}
		ResponseBestParameter bestResult = new ResponseBestParameter();
		bestResult.setMedian(min);

		for (Pair<String, GumtreeProperties> pair : minDiff) {
			String oneBest = GTProxy.plainProperties(new JsonObject(), pair.first, pair.second);
			bestResult.getAllBest().add(oneBest);
		}

		saver.saveSummarization(configuration.getDirDiffTreeSerialOutput(), results);
		saver.saveRelations(configuration.getDirDiffTreeSerialOutput());
		return bestResult;
	}

	public String getDiffId(File left) {
		return left.getName().split("\\.")[0].replace("_s", "").replace("_t", "");
	}

}
