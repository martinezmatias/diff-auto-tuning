package fr.uphf.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.github.gumtreediff.actions.ChawatheScriptGenerator;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.ConfigurableMatcher;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumTreeProperties;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.google.gson.JsonArray;
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
public class Engine {
	private AstComparator diff = new AstComparator();
	private SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();
	private ChawatheScriptGenerator editScriptGenerator = new ChawatheScriptGenerator();

	Matcher[] matchers = new Matcher[] {
			// Simple
			new CompositeMatchers.SimpleGumtree(),
			//
			new CompositeMatchers.ClassicGumtree(),
			//
			new CompositeMatchers.ChangeDistiller(),

			new CompositeMatchers.CompleteGumtreeMatcher() };

	public void navigate(File path, int[] subsets, int stop) {
		int nrPairFiles = 0;
		long initTime = (new Date()).getTime();
		for (int subset : subsets) {

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
						// log.debug("Missing file in diff " + pathname + " " + childFile.getName());
						System.err.println("Missing file in diff " + pathname + " " + commit.getName());

						continue;
					}
					nrPairFiles++;
					System.out.println(nrPairFiles + " Analyzing " + previousVersion);

					JsonObject treeFeatures = new JsonObject();
					treeFeatures.addProperty("f", fileModif.getName());
					treeFeatures.addProperty("c", commit.getName());
					treeFeatures.addProperty("d", subset);
					try {
						// TODO: Also with JDT

						ITree tl = scanner.getTree(diff.getCtType(previousVersion));
						ITree tr = scanner.getTree(diff.getCtType(postVersion));

						JsonObject thFeaturesL = extracted(tl);
						JsonObject thFeaturesR = extracted(tr);

						treeFeatures.add("tl", thFeaturesL);
						treeFeatures.add("tr", thFeaturesR);

						JsonArray matchersJSon = new JsonArray();
						treeFeatures.add("mt", matchersJSon);

						////
						for (Matcher matcher : matchers) {

							JsonObject resultJson = computeFitnessFunction(tl, tr, matcher);
							matchersJSon.add(resultJson);
						}

					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		long endTime = (new Date()).getTime();

		System.out.println("Time " + (endTime - initTime) / 1000);

	}

	public JsonObject extracted(ITree tl) {
		JsonObject thFeatures = new JsonObject();
		thFeatures.addProperty("s", tl.getMetrics().size);
		thFeatures.addProperty("h", tl.getMetrics().height);
		thFeatures.addProperty("hs", tl.getMetrics().structureHash);
		return thFeatures;
	}

	public JsonObject computeFitnessFunction(ITree tl, ITree tr, Matcher matcher) {

		List<GumTreeProperties> combinations = null;

		JsonObject propertiesJSON = new JsonObject();

		propertiesJSON.addProperty("m", matcher.getClass().getSimpleName());

		JsonArray allDiffResulFromMatcher = new JsonArray();

		propertiesJSON.add("r", allDiffResulFromMatcher);

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

		int i = 0;
		boolean paralel = true;
		combinations = combinations.subList(0, (combinations.size() > 50) ? 50 : combinations.size());
		if (!paralel) {
			for (GumTreeProperties aGumTreeProperties : combinations) {

				i++;
				JsonObject resDiff = runDiff(tl, tr, matcher, aGumTreeProperties);

				allDiffResulFromMatcher.add(resDiff);

				if (i % 10 == 0)
					System.out.println(i + "/" + combinations.size());

			}
		} else {
			// parallel
			try {
				parallell(10, tl, tr, matcher, combinations);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return propertiesJSON;

	}

	public class DiffCallable implements Callable<JsonObject> {
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
		public JsonObject call() throws Exception {

			return runDiff(tl, tr, matcher, aGumTreeProperties);
		}

	}

	public void parallell(int nrThreads, ITree tl, ITree tr, Matcher matcher, List<GumTreeProperties> combinations)
			throws InterruptedException {

		ExecutorService executor = Executors.newFixedThreadPool(nrThreads);
		List<Future> futures = new ArrayList<>();
		List<Callable<JsonObject>> callables = new ArrayList<>();

		for (GumTreeProperties aGumTreeProperties : combinations) {

			// Future<JsonObject> f = executor.submit
			callables.add(new DiffCallable(tl, tr, matcher, aGumTreeProperties));
			// futures.add(f);
		}

		System.out.println("#nr callables " + callables.size());
		List<Future<JsonObject>> result = executor.invokeAll(callables);

		// executor.awaitTermination(10, TimeUnit.MINUTES);

		// while (!executor.isTerminated()) {
		// }

		executor.shutdown();

		System.out.println("Finished all threads");
	}

	public JsonObject runDiff(ITree tl, ITree tr, Matcher matcher, GumTreeProperties aGumTreeProperties) {
		long initSingleDiff = new Date().getTime();
		// System.out.println("running");
		Diff result = new DiffImpl(scanner.getTreeContext(), tl, tr, editScriptGenerator, matcher, aGumTreeProperties);

		List<Operation> actionsAll = result.getAllOperations();

		List<Operation> actionsRoot = result.getRootOperations();

		long endSingleDiff = new Date().getTime();

		JsonObject resultJson = new JsonObject();
		resultJson.addProperty("na", actionsAll.size());
		resultJson.addProperty("nr", actionsRoot.size());

		resultJson.addProperty("t", (endSingleDiff - initSingleDiff));
		JsonObject propertiesJSON = new JsonObject();
		resultJson.add("p", propertiesJSON);

		for (String prop : aGumTreeProperties.getProperties().keySet()) {
			propertiesJSON.addProperty(prop, aGumTreeProperties.getProperties().get(prop).toString());
		}
		return propertiesJSON;

	}

	public List<GumTreeProperties> computeCartesianProduct(List<ParameterDomain> domains) {
		if (domains.size() < 2)
			throw new IllegalArgumentException(
					"Can't have a product of fewer than two sets (got " + domains.size() + ")");

		return _cartesianProduct(0, domains);
	}

	private List<GumTreeProperties> _cartesianProduct(int index, List<ParameterDomain> domains) {

		List<GumTreeProperties> ret = new ArrayList<>();

		if (index == domains.size()) {
			ret.add(new GumTreeProperties());
		} else {

			ParameterDomain domainOfParameters = domains.get(index);
			for (Object valueFromDomain : domainOfParameters.computeInterval()) {
				List<GumTreeProperties> configurationFromOthersDomains = _cartesianProduct(index + 1, domains);
				for (GumTreeProperties configFromOthers : configurationFromOthersDomains) {

					configFromOthers.put(domainOfParameters.getId(), valueFromDomain);
					// configFromOthers.add(obj);
					ret.add(configFromOthers);
				}
			}
		}
		return ret;
	}

	protected String calculatePathName(File fileModif, File parentFile) {
		return
		// The folder with the file name
		fileModif.getAbsolutePath() + File.separator
		// check if add the revision name in the file name
				+ (parentFile.getName() + "_")
				// File name
				+ fileModif.getName();
	}

	public Matcher[] getMatchers() {
		return matchers;
	}

	public void setMatchers(Matcher[] matchers) {
		this.matchers = matchers;
	}

}
