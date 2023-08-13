package fr.gumtree.autotuning.gumtree;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.gumtree.autotuning.entity.SingleDiffResult;
import fr.gumtree.autotuning.outils.Constants;
import fr.gumtree.treediff.jdt.TreeDiffFormatBuilder;

public class GTProxy implements DiffProxy {

//	protected ExhaustiveEngine engine = new ExhaustiveEngine();

	TreeDiffFormatBuilder builder = new TreeDiffFormatBuilder(false, false);

	public Diff run(Tree tleft, Tree tright, String params) {
		return run(tleft, tright, params, null);
	}

	public Diff run(Tree tleft, Tree tright, String params, File out) {
		try {
			String[] paramSplit = split(params);

			GumtreeProperties properties = parseProperty(paramSplit);

			String algoName = paramSplit[0];

			ConfigurableMatcher cmatcher = getMatcher(algoName);

			cmatcher.configure(properties);

			//System.out.println("Calling: "+ properties);

			ChawatheScriptGenerator edGenerator = new ChawatheScriptGenerator();

			return run(tleft, tright, properties, cmatcher, edGenerator, out);
		} catch (Exception e) {
			System.err.println("Error computing diff");
			e.printStackTrace();
			return null;
		}
	}
	ExecutorService executor = Executors.newSingleThreadScheduledExecutor();
	
	public Diff runT(Tree tleft, Tree tright, String params, File out) {

		Callable<Diff> task = new Callable<Diff>() {
			public Diff call() throws InterruptedException {
				return run(tleft,  tright,  params,  out);
			}
		};
		
	
		Future<Diff> future = executor.submit(task);
		try {
			Diff resDiff = future.get(1, TimeUnit.SECONDS);
			
			return resDiff;
		} catch (TimeoutException ex) {
			System.out.println("Timeout");
			
			return null;


		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		return null;
	}

	
	public Diff run(Tree tleft, Tree tright, GumtreeProperties properties, ConfigurableMatcher cmatcher,
			ChawatheScriptGenerator edGenerator, File out) {

		try {

			Diff diff = computeDiff(tleft, tright, cmatcher, edGenerator, properties);

			if (out != null) {
				JsonObject jso = new JsonObject();
				save(builder, out, jso, diff, properties, cmatcher.getClass().getSimpleName(), "single");
				System.out.println("Saved");
			}

			return diff;
		} catch (Exception e) {
			System.err.println("Error computing diff");
			e.printStackTrace();
			return null;
		}
	}

	public String[] split(String params) {
		String sep = "-";

		String[] paramSplit = params.split(sep);
		return paramSplit;
	}

	public GumtreeProperties parseProperty(String paramSplit) {
		return parseProperty(split(paramSplit));
	}

	public GumtreeProperties parseProperty(String[] paramSplit) {
		GumtreeProperties properties = new GumtreeProperties();
		for (int i = 1; i < paramSplit.length; i = i + 2) {
			String si = paramSplit[i];
			String siv = paramSplit[i + 1];

			// System.out.println(si + " " + siv);

			properties.tryConfigure(ConfigurationOptions.valueOf(si), siv);
		}
		// System.out.println("Fininsh config - ");
		return properties;
	}

	private ConfigurableMatcher getMatcher(String algoName) {

		if (algoName.equals("SimpleGumtree"))
			return new CompositeMatchers.SimpleGumtree();

		if (algoName.equals("ClassicGumtree"))
			return new CompositeMatchers.ClassicGumtree();

		if (algoName.equals("HybridGumtree"))
			return new CompositeMatchers.HybridGumtree();

		if (algoName.equals("ChangeDistiller"))
			return new CompositeMatchers.ChangeDistiller();

		if (algoName.equals("XyMatcher"))
			return new CompositeMatchers.XyMatcher();

		return null;
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
		try {
			SingleDiffResult resultDiff = new SingleDiffResult(matcher.getClass().getSimpleName());

			// Calling directly to GT.core
			Diff diff = computeDiff(tl, tr, matcher, aGumtreeProperties);
			long endSingleDiff = new Date().getTime();

			if (diff != null) {
				List<Action> actionsAll = diff.editScript.asList();

				resultDiff.put(Constants.NRACTIONS, actionsAll.size());
				resultDiff.put(Constants.NR_INSERT, actionsAll.stream().filter(e -> e instanceof Insert).count());
				resultDiff.put(Constants.NR_DELETE, actionsAll.stream().filter(e -> e instanceof Delete).count());
				resultDiff.put(Constants.NR_UPDATE, actionsAll.stream().filter(e -> e instanceof Update).count());
				resultDiff.put(Constants.NR_MOVE, actionsAll.stream().filter(e -> e instanceof Move).count());
				resultDiff.put(Constants.NR_TREEINSERT,
						actionsAll.stream().filter(e -> e instanceof TreeInsert).count());
				resultDiff.put(Constants.NR_TREEDELETE,
						actionsAll.stream().filter(e -> e instanceof TreeDelete).count());
			}
			resultDiff.put(Constants.TIME, (endSingleDiff - initSingleDiff));

			resultDiff.put(Constants.CONFIG, aGumtreeProperties);

			resultDiff.setDiff(diff);

			return resultDiff;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}


	public Diff computeDiff(Tree tl, Tree tr, Matcher matcher, GumtreeProperties properies) {

		return computeDiff(tl, tr, matcher, new ChawatheScriptGenerator(), properies);
	}

	public Diff computeDiff(Tree tl, Tree tr, Matcher matcher, EditScriptGenerator edGenerator,
			GumtreeProperties properies) {
		try {

			CompositeMatcher cm = (CompositeMatcher) matcher;
			cm.configure(properies);

			MappingStore mappings = matcher.match(tl, tr);

			EditScript actions = edGenerator.computeActions(mappings);

			Diff diff = new Diff(null, null, mappings, actions);

			return diff;
			// return null;
		} catch (Exception e) {
			System.err.println(
					"Problems computing diff " + matcher.getClass().getSimpleName() + "_" + properies.toString());
			e.printStackTrace();
			return null;
		}
	}

	public void save(TreeDiffFormatBuilder builder, File outResults, JsonObject jso, Diff diff, GumtreeProperties gttp,
			String maattcher) {

		save(builder, outResults, jso, diff, gttp, maattcher, "exhaustive");
	}

	public void save(TreeDiffFormatBuilder builder, File outResults, JsonObject jso, Diff diff, GumtreeProperties gttp,
			String maattcher, String key) {

		String fileKey = plainProperties(maattcher, gttp);

		plainProperties(jso, maattcher, gttp);

		System.out.println(fileKey);

		JsonElement js = builder.build(null, null, diff, jso);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		String json = gson.toJson(js);

		try {
			FileWriter fwriter = new FileWriter(new File(outResults + File.separator + key + "_" + fileKey + ".json"));

			fwriter.write(json);
			fwriter.flush();
			fwriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String plainProperties(String matcher, GumtreeProperties gttp) {
		return plainProperties(matcher, new ArrayList(), gttp);
	}

	public static String plainProperties(String matcher, List<String> toIgnore, GumtreeProperties gttp) {
		String fileKey = "";

		Map<String, Object> propertiesMap = toGumtreePropertyToMap(gttp);

		List<String> sorted = new ArrayList<>(propertiesMap.keySet());

		Collections.sort(sorted);

		for (String pKey : sorted) {

			if (toIgnore.contains(pKey))
				continue;

			String value = propertiesMap.get(pKey).toString();

			String separator = "-";
			if (!fileKey.isEmpty())
				fileKey += separator;
			else {
				fileKey += matcher + separator;
			}
			fileKey += pKey + separator + value;

		}
		return fileKey;
	}

	public static void plainProperties(JsonObject jso, String maattcher, GumtreeProperties gttp) {

		Map<String, Object> propertiesMap = toGumtreePropertyToMap(gttp);

		List<String> sorted = new ArrayList<>(propertiesMap.keySet());

		Collections.sort(sorted);

		for (String pKey : sorted) {

			String value = propertiesMap.get(pKey).toString();
			jso.addProperty(pKey, value);

		}

	}

	public static Map<String, Object> toGumtreePropertyToMap(GumtreeProperties properties) {
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
