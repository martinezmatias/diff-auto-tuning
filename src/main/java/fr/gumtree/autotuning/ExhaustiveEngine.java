package fr.gumtree.autotuning;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
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
import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.ConfigurableMatcher;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumtreeProperties;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.Tree;
import com.github.gumtreediff.utils.Pair;
import com.google.gson.JsonObject;

import fr.gumtree.autotuning.entity.CaseResult;
import fr.gumtree.autotuning.entity.MatcherResult;
import fr.gumtree.autotuning.entity.SingleDiffResult;
import fr.gumtree.autotuning.treebuilder.ITreeBuilder;
import fr.gumtree.treediff.jdt.TreeDiffFormatBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ExhaustiveEngine {

	GTProxy gumtreeproxy = new GTProxy();

//	private ITreeBuilder treeBuilder = new SpoonTreeBuilder();

	long timeOutSeconds = 60 * 60; // 60 min

	public enum PARALLEL_EXECUTION {
		MATCHER_LEVEL, PROPERTY_LEVEL, NONE
	}

	public enum ASTMODE {
		GTSPOON, JDT
	};

	// TODO: we cannot use the same generator when we execute on parallel.
	private ChawatheScriptGenerator editScriptGenerator = new ChawatheScriptGenerator();

	public Matcher[] allMatchers = new Matcher[] {
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

	public CaseResult runSingleOnPairOfFiles(ITreeBuilder treeBuilder, String out, int subset,
			PARALLEL_EXECUTION parallel, File previousVersion, File postVersion, String diffId) throws IOException {
		Map<String, Pair<Map, Map>> treeProperties = new HashMap<>();

		long initTime = (new Date()).getTime();

		CaseResult fileResult = analyzeCase(treeBuilder, diffId, previousVersion, postVersion, parallel, treeProperties,
				allMatchers);

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

				SingleDiffResult resDiff = gumtreeproxy.runDiff(tl, tr, matcher, aGumtreeProperties);

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

			return gumtreeproxy.runDiff(tl, tr,
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

	public JsonObject extractTreeFeatures(Tree tl) {
		JsonObject thFeatures = new JsonObject();
		thFeatures.addProperty(Constants.SIZE, tl.getMetrics().size);
		thFeatures.addProperty(Constants.HEIGHT, tl.getMetrics().height);
		thFeatures.addProperty(Constants.STRUCTHASH, tl.getMetrics().structureHash);
		return thFeatures;
	}

	public Map<String, Object> extractTreeFeaturesMap(Tree tl) {
		Map<String, Object> fileresult = new HashMap<>();
		fileresult.put(Constants.SIZE, tl.getMetrics().size);
		fileresult.put(Constants.HEIGHT, tl.getMetrics().height);
		fileresult.put(Constants.STRUCTHASH, tl.getMetrics().structureHash);
		return fileresult;
	}

	public void metadataToCSV(File nameFile, Map<String, Pair<Map, Map>> treeProperties, CaseResult fileResult)
			throws IOException {

		String sep = ",";
		String endline = "\n";
		String header = "DIFFID" + sep + "L_" + Constants.SIZE + sep + "L_" + Constants.HEIGHT + sep + "L_"
				+ Constants.STRUCTHASH + sep + "R_" + Constants.SIZE + sep + "R_" + Constants.HEIGHT + sep + "R_"
				+ Constants.STRUCTHASH + sep + Constants.TIME_TREES_PARSING + sep + Constants.TIME_ALL_MATCHER_DIFF;

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
			row += t.first.get(Constants.SIZE) + sep;
			row += t.first.get(Constants.HEIGHT) + sep;
			row += t.first.get(Constants.STRUCTHASH) + sep;
			row += t.second.get(Constants.SIZE) + sep;
			row += t.second.get(Constants.HEIGHT) + sep;
			row += t.second.get(Constants.STRUCTHASH) + sep;

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

}
