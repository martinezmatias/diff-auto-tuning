package fr.uphf.reader;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.stream.Collectors;

import com.github.gumtreediff.actions.ChawatheScriptGenerator;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.ConfigurableMatcher;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumTreeProperties;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.utils.Pair;
import com.google.gson.JsonObject;

import fr.uphf.ParameterDomain;
import fr.uphf.ParametersResolvers;
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
	private AstComparator diff = new AstComparator();
	private SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();

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

	};

	/**
	 * Navigates megadiff datasets
	 * 
	 * @param path    path to megadiff root
	 * @param subsets subsets of megadiff to consider
	 * @param stop    max numbers of diff to analyze per subset
	 * @throws IOException
	 */
	public void navigateMegaDiff(File path, int[] subsets, int stop, boolean parallel) throws IOException {

		Map<String, Pair<Map, Map>> treeProperties = new HashMap<>();

		long initTime = (new Date()).getTime();
		for (int subset : subsets) {

			int nrPairFiles = 0;
			File pathSubset = new File(path.getAbsoluteFile() + File.separator + subset + File.separator);

			for (File commit : pathSubset.listFiles()) {

				if (stop <= nrPairFiles) {
					System.out.println("Reach max " + nrPairFiles);
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

					nrPairFiles++;
					System.out.println(nrPairFiles + " Analyzing " + previousVersion);
					String diffId = commit.getName() + "_" + fileModif.getName();

					Map<String, Object> fileResult = new HashMap<>();
					fileResult.put(FILE, fileModif.getName());
					fileResult.put(COMMIT, commit.getName());
					fileResult.put(MEGADIFFSET, subset);

					analyzeDiff(diffId, previousVersion, postVersion, parallel, treeProperties, fileResult,
							this.matchers);

					File outResults = new File("./out/" + diffId + ".csv");

					executionResultToCSV(outResults, fileResult);

				}
			}
		}
		treeInfoToCSV(new File("./out/tree_info_" + Arrays.toString(subsets) + ".csv"), treeProperties);

		long endTime = (new Date()).getTime();

		System.out.println("Time " + (endTime - initTime) / 1000);

	}

	/**
	 * 
	 * @param diffId
	 * @param previousVersion
	 * @param postVersion
	 * @param parallel
	 * @param treeProperties
	 * @param fileResult
	 */
	public void analyzeDiff(String diffId, File previousVersion, File postVersion, boolean parallel,
			Map<String, Pair<Map, Map>> treeProperties, Map<String, Object> fileResult, Matcher[] matchers) {
		try {
			// TODO: Also with JDT

			ITree tl = scanner.getTree(diff.getCtType(previousVersion));
			ITree tr = scanner.getTree(diff.getCtType(postVersion));

			treeProperties.put(diffId, new Pair<Map, Map>(extractTreeFeaturesMap(tl), extractTreeFeaturesMap(tr)));

			List<Map<String, Object>> matcherResults = new ArrayList<>();

			fileResult.put(MATCHERS, matcherResults);

			for (Matcher matcher : matchers) {
				try {
					Map<String, Object> resultJson = computeFitnessFunction(tl, tr, matcher, parallel);

					matcherResults.add(resultJson);
				} catch (Exception e) {
					System.err.println("Problems with matcher " + matcher.getClass().getSimpleName());
					e.printStackTrace();
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public Map<String, Object> computeFitnessFunction(ITree tl, ITree tr, Matcher matcher, boolean parallel) {

		List<GumTreeProperties> combinations = null;

		Map<String, Object> result = new HashMap<>();

		// propertiesJSON.addProperty(MATCHER, matcher.getClass().getSimpleName());
		result.put(MATCHER, matcher.getClass().getSimpleName());

		List<Object> alldiffresults = new ArrayList<>();

		// propertiesJSON.add(CONFIGS, allDiffResulFromMatcher);
		result.put(CONFIGS, alldiffresults);

		if (matcher instanceof ConfigurableMatcher) {

			ConfigurableMatcher configurableMatcher = (ConfigurableMatcher) matcher;

			// We collect the options of the matcher
			Set<ConfigurationOptions> options = configurableMatcher.getApplicableOptions();

			List<ParameterDomain> domains = new ArrayList<>();

			// We collect the domains
			for (ConfigurationOptions option : options) {

				System.out.println("Option " + option);
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

			System.out.println("Matcher " + matcher.getClass().getSimpleName() + " options: " + domains.size()
					+ " Nr config: " + combinations.size());

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

				if (iProperty % 10 == 0)
					System.out.println(iProperty + "/" + combinations.size());

			}
		} else {
			// parallel
			try {
				List<Map<String, Object>> results = runInParallel(10, tl, tr, matcher, combinations);
				for (Map<String, Object> iResult : results) {

					alldiffresults.add(iResult);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result;

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

		Diff result = new DiffImpl(scanner.getTreeContext(), tl, tr, new ChawatheScriptGenerator(), matcher,
				aGumTreeProperties);

		List<Operation> actionsAll = result.getAllOperations();

		List<Operation> actionsRoot = result.getRootOperations();

		long endSingleDiff = new Date().getTime();

		Map<String, Object> resultMap = new HashMap<>();

		resultMap.put(NRACTIONS, actionsAll.size());
		resultMap.put(NRROOTS, actionsRoot.size());

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
	 * @throws IOException
	 */
	private void executionResultToCSV(File out, Map<String, Object> fileresult) throws IOException {

		String sep = ",";
		String endline = "\n";
		String header = "";

		List<Map<String, Object>> matchers = (List<Map<String, Object>>) fileresult.get(MATCHERS);
		String row = "";
		boolean first = true;
		for (Map<String, Object> map : matchers) {

			String xmatcher = map.get(MATCHER).toString();

			List<Map<String, Object>> configs = (List<Map<String, Object>>) map.get(CONFIGS);
			for (Map<String, Object> config : configs) {
				row += xmatcher + sep;

				row += config.get(NRACTIONS) + sep;

				row += config.get(NRROOTS) + sep;

				row += config.get(TIME) + sep;

				GumTreeProperties gtp = (GumTreeProperties) config.get(CONFIG);

				row += gtp.getProperties().keySet().size() + sep;

				if (first) {
					header += MATCHER + sep;
					header += NRACTIONS + sep;
					header += NRROOTS + sep;
					header += TIME + sep;
					header += "NROPTIONS" + sep;

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
				}

				row += endline;
			}

		}

		String all = header;
		all += row;

		FileWriter fw = new FileWriter(out);
		fw.write(all);
		fw.flush();
		fw.close();

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
}
