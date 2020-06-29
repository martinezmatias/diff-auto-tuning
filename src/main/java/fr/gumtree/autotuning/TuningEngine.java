package fr.gumtree.autotuning;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.github.gumtreediff.actions.model.Delete;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.actions.model.Move;
import com.github.gumtreediff.actions.model.TreeDelete;
import com.github.gumtreediff.actions.model.TreeInsert;
import com.github.gumtreediff.actions.model.Update;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.ConfigurableMatcher;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumTreeProperties;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.github.gumtreediff.utils.Pair;
import com.google.gson.JsonObject;

import gumtree.spoon.AstComparator;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.DiffImpl;
import gumtree.spoon.diff.operations.Operation;

/**
 * 
 * @author Matias Martinez
 *
 */
public class TuningEngine {
	private static final String NR_TREEDELETE = "NR_TREEDELETE";
	private static final String NR_TREEINSERT = "NR_TREEINSERT";
	private static final String NR_MOVE = "NR_MOVE";
	private static final String NR_UPDATE = "NR_UPDATE";
	private static final String NR_DELETE = "NR_DELETE";
	private static final String NR_INSERT = "NR_INSERT";
	private static final String CONFIGS = "r";
	private static final String MATCHER = "MATCHER";
	private static final String MATCHERS = "MATCHERS";
	private static final String TRIGHT = "TRIGHT";
	private static final String TLEFT = "TLEFT";
	private static final String CONFIG = "CONFIG";
	private static final String TIME = "TIME";
	private static final String NRROOTS = "NRROOTS";
	private static final String NRACTIONS = "NRACTIONS";
	private static final String STRUCTHASH = "STRUCTHASH";
	private static final String HEIGHT = "HEIGHT";
	public static final String SIZE = "SIZE";
	private static final String MEGADIFFSET = "MEGADIFFSET";
	private static final String COMMIT = "COMMIT";
	private static final String FILE = "FILE";
	private static final String TIMEOUT = "TIMEOUT";
	private AstComparator diff = new AstComparator();
	private SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();

	long timeOutSeconds = 60 * 5;

	enum PARALLEL_EXECUTION {
		MATCHER_LEVEL, PROPERTY_LEVEL, NONE
	}

	enum ASTMODE {
		GTSPOON, JDT
	};

	// TODO: we cannot use the same generator when we execute on parallel.
	private ChawatheScriptGenerator editScriptGenerator = new ChawatheScriptGenerator();

	Matcher[] matchers = new Matcher[] {
			// Simple
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
	private int nrThreads = 10;

	public void navigateMegaDiff(String out, File path, int[] subsets, int begin, int stop, ASTMODE astmodel,
			PARALLEL_EXECUTION parallel) throws IOException {
		this.navigateMegaDiff(out, path, subsets, begin, stop, astmodel, parallel, this.matchers);
	}

	/**
	 * Navigates megadiff datasets
	 * 
	 * @param path    path to megadiff root
	 * @param subsets subsets of megadiff to consider
	 * @param stop    max numbers of diff to analyze per subset
	 * @throws IOException
	 */
	public void navigateMegaDiff(String out, File path, int[] subsets, int begin, int stop, ASTMODE astmodel,
			PARALLEL_EXECUTION parallel, Matcher[] matchers) throws IOException {

		Map<String, Pair<Map, Map>> treeProperties = new HashMap<>();

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
					System.out.println("Skip " + commit.getName());
					continue;
				}

				if (nrCommit > stop) {
					System.out.println("Reach max " + nrCommit);
					break;
				}

				if (commit.list() == null)
					continue;

				for (File fileModif : commit.listFiles()) {
					long initdiff = (new Date()).getTime();
					if (".DS_Store".equals(fileModif.getName()))
						continue;

					String pathname = calculatePathName(fileModif, commit);

					File previousVersion = new File(pathname.trim() + "_s.java");
					File postVersion = new File(pathname.trim() + "_t.java");

					if (!previousVersion.exists() || !postVersion.exists()) {
						System.err.println("Missing file in diff " + pathname + " " + commit.getName());

						continue;
					}

					// System.out.println(nrCommit + " Analyzing " + previousVersion);
					String diffId = commit.getName() + "_" + fileModif.getName();

					System.out.println("\n---diff " + nrCommit + "/" + commits.size() + " id " + diffId);
					Map<String, Object> fileResult = analyzeDiff(diffId, previousVersion, postVersion, astmodel,
							parallel, treeProperties, matchers);
					fileResult.put(FILE, fileModif.getName());
					fileResult.put(COMMIT, commit.getName());
					fileResult.put(MEGADIFFSET, subset);

					File outResults = new File(out + File.separator + subset + File.separator + "nr_" + nrCommit
							+ "_id_" + diffId + "_" + astmodel.name() + ".csv");
					outResults.getParentFile().mkdirs();

					executionResultToCSV(outResults, fileResult);

					System.out.println("diff time " + ((new Date()).getTime() - initdiff) / 1000 + " sec");

				}
			}
		}
		System.out.println("Finished all diff from " + begin + " to " + stop);
		File treeFile = new File(
				out + "/tree_info_" + Arrays.toString(subsets).replace("[", "").replace("]", "").replace(",", "-") + "_"
						+ astmodel.name() + "_" + begin + "_" + stop + ".csv");
		treeInfoToCSV(treeFile, treeProperties);
		System.out.println("Saving tree file data " + treeFile.getAbsolutePath());

		long endTime = (new Date()).getTime();

		System.out.println("Time " + (endTime - initTime) / 1000);

	}

	public Map<String, Object> navigateSingleDiffMegaDiff(String out, File path, int subset, String commitId,
			ASTMODE astmodel, PARALLEL_EXECUTION parallel, Matcher[] matchers) throws IOException {

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

		Map<String, Object> fileResult = analyzeDiff(diffId, previousVersion, postVersion, astmodel, parallel,
				treeProperties, matchers);

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
	public Map<String, Object> analyzeDiff(String diffId, File previousVersion, File postVersion, ASTMODE model,
			PARALLEL_EXECUTION parallel, Map<String, Pair<Map, Map>> treeProperties, Matcher[] matchers) {
		try {
			ITree tl = null;
			ITree tr = null;
			if (ASTMODE.GTSPOON.equals(model)) {
				tl = scanner.getTree(diff.getCtType(previousVersion));
				tr = scanner.getTree(diff.getCtType(postVersion));
			} else if (ASTMODE.JDT.equals(model)) {
				String lc = new String(Files.readAllBytes(previousVersion.toPath()));
				tl = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

				String lr = new String(Files.readAllBytes(postVersion.toPath()));
				tr = new JdtTreeGenerator().generateFrom().string(lr).getRoot();

			} else {

			}

			treeProperties.put(diffId, new Pair<Map, Map>(extractTreeFeaturesMap(tl), extractTreeFeaturesMap(tr)));

			if (parallel.equals(PARALLEL_EXECUTION.MATCHER_LEVEL))
				return analyzeDiffByMatcherThread(tl, tr, parallel, matchers);
			else
				return analyzeDiff(tl, tr, parallel, matchers);

		} catch (Exception e) {
			e.printStackTrace();
			return new HashMap<>();
		}
	}

	public Map<String, Object> analyzeDiff(ITree tl, ITree tr, PARALLEL_EXECUTION parallel, Matcher[] matchers) {
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

			List<Future<Map<String, Object>>> result = executor.invokeAll(callables);

			executor.shutdown();

			List<Map<String, Object>> collectedResults = result.stream().map(e -> {
				try {
					return e.get();
				} catch (InterruptedException | ExecutionException e1) {

					e1.printStackTrace();
					return null;
				}
			}).collect(Collectors.toList());

			matcherResults.addAll(collectedResults);

		} catch (Throwable e) {
			e.printStackTrace();
		}

		return fileResult;
	}

	public Map<String, Object> computeFitnessFunction(ITree tl, ITree tr, Matcher matcher, boolean parallel) {
		long initMatcher = (new Date()).getTime();
		List<GumTreeProperties> combinations = null;

		Map<String, Object> result = new HashMap<>();

		result.put(MATCHER, matcher.getClass().getSimpleName());

		List<Object> alldiffresults = new ArrayList<>();

		result.put(CONFIGS, alldiffresults);

		if (matcher instanceof ConfigurableMatcher) {

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

			// System.out.println("Matcher " + matcher.getClass().getSimpleName() + "
			// options: " + domains.size()
			// + " Nr config: " + combinations.size());

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

				// if (iProperty % 10 == 0)
				// System.out.println(iProperty + "/" + combinations.size());

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
		System.out.println("Matcher " + matcher.getClass().getSimpleName() + " time "
				+ (((new Date()).getTime() - initMatcher) / 1000) + " Nr_config: " + combinations.size());
		return result;

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

	@Deprecated
	public List<Map<String, Object>> runInParallelScWait(int nrThreads, ITree tl, ITree tr, Matcher matcher,
			List<GumTreeProperties> combinations) throws Exception {

		ScheduledExecutorService executor = Executors.newScheduledThreadPool(nrThreads);

		List<Callable<Map<String, Object>>> callables = new ArrayList<>();

		Map<Future<Map<String, Object>>, Callable<Map<String, Object>>> futures = new HashMap();

		for (GumTreeProperties aGumTreeProperties : combinations) {
			// callables.add();

			DiffCallable callable = new DiffCallable(tl, tr, matcher, aGumTreeProperties);
			Future<Map<String, Object>> futureTask = executor.submit(callable);
			futures.put(futureTask, callable);
		}

		// List<Future<Map<String, Object>>> result = executor.invokeAll(callables, 30,
		// TimeUnit.SECONDS);

		// TOTAL
		executor.awaitTermination(1, TimeUnit.MINUTES);

		executor.shutdown();

		Collection<Future<Map<String, Object>>> result = futures.keySet();

		return result.stream().map(e -> {
			try {
				if (e.isDone() && !e.isCancelled())
					return e.get();
				else {
					// System.out.println("Cancell task");

					HashMap hashMap = new HashMap();
					hashMap.put(TIMEOUT, "true");

					return hashMap;
				}
			} catch (Exception e1) {

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
		Diff result = null;

		result = new DiffImpl(new TreeContext()// scanner.getTreeContext()
				, tl, tr, new ChawatheScriptGenerator(), matcher, aGumTreeProperties);

		List<Operation> actionsAll = result.getAllOperations();

		List<Operation> actionsRoot = result.getRootOperations();

		resultMap.put(NRACTIONS, actionsAll.size());
		resultMap.put(NRROOTS, actionsRoot.size());

		resultMap.put(NR_INSERT, actionsAll.stream().filter(e -> e.getAction() instanceof Insert).count());
		resultMap.put(NR_DELETE, actionsAll.stream().filter(e -> e.getAction() instanceof Delete).count());
		resultMap.put(NR_UPDATE, actionsAll.stream().filter(e -> e.getAction() instanceof Update).count());
		resultMap.put(NR_MOVE, actionsAll.stream().filter(e -> e.getAction() instanceof Move).count());
		resultMap.put(NR_TREEINSERT, actionsAll.stream().filter(e -> e.getAction() instanceof TreeInsert).count());
		resultMap.put(NR_TREEDELETE, actionsAll.stream().filter(e -> e.getAction() instanceof TreeDelete).count());

		long endSingleDiff = new Date().getTime();
		resultMap.put(TIME, (endSingleDiff - initSingleDiff));

		resultMap.put(CONFIG, aGumTreeProperties);

		return resultMap;

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
		return matchers;
	}

	public void setMatchers(Matcher[] matchers) {
		this.matchers = matchers;
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
		String row = "";
		boolean first = true;
		FileWriter fw = new FileWriter(out);
		for (Map<String, Object> map : matchers) {

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
					row += "1" + sep;

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
		System.out.println("Save file " + out.getAbsolutePath());

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

	private void treeInfoToCSV(File name, Map<String, Pair<Map, Map>> treeProperties) throws IOException {

		String sep = ",";
		String endline = "\n";
		String header = "DIFFID" + sep + "L_" + SIZE + sep + "L_" + HEIGHT + sep + "L_" + STRUCTHASH + sep + "R_" + SIZE
				+ sep + "R_" + HEIGHT + sep + "R_" + STRUCTHASH + endline;

		String row = "";

		for (String id : treeProperties.keySet()) {

			Pair<Map, Map> t = treeProperties.get(id);
			row += id + sep;
			row += t.first.get(SIZE) + sep;
			row += t.first.get(HEIGHT) + sep;
			row += t.first.get(STRUCTHASH) + sep;
			row += t.second.get(SIZE) + sep;
			row += t.second.get(HEIGHT) + sep;
			row += t.second.get(STRUCTHASH) + sep;
			row += endline;
		}

		FileWriter fw = new FileWriter(name);
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
