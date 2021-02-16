package fr.gumtree.autotuning;

import java.io.File;

import com.github.gumtreediff.actions.ChawatheScriptGenerator;
import com.github.gumtreediff.actions.Diff;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.ConfigurableMatcher;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumtreeProperties;
import com.github.gumtreediff.tree.Tree;
import com.google.gson.JsonObject;

import fr.gumtree.treediff.jdt.TreeDiffFormatBuilder;

public class GTProxy {

	protected TuningEngine engine = new TuningEngine();

	TreeDiffFormatBuilder builder = new TreeDiffFormatBuilder(false, false);

	public Diff run(Tree tleft, Tree tright, String params) {
		return run(tleft, tright, params, null);
	}

	public Diff run(Tree tleft, Tree tright, String params, File out) {
		String[] paramSplit = split(params);

		GumtreeProperties properties = parseProperty(paramSplit);

		String algoName = paramSplit[0];

		ConfigurableMatcher cmatcher = getMatcher(algoName);

		cmatcher.configure(properties);

		System.out.println(properties);

		ChawatheScriptGenerator edGenerator = new ChawatheScriptGenerator();

		Diff diff = engine.computeDiff(tleft, tright, cmatcher, edGenerator, properties);

		if (out != null) {
			JsonObject jso = new JsonObject();
			engine.save(builder, out, jso, diff, properties, cmatcher.getClass().getSimpleName(), "single");
			System.out.println("Saved");
		}

		return diff;
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

			System.out.println(si + " " + siv);

			properties.tryConfigure(ConfigurationOptions.valueOf(si), siv);
		}
		return properties;
	}

	private ConfigurableMatcher getMatcher(String algoName) {

		if (algoName.equals("SimpleGumtree"))
			return new CompositeMatchers.SimpleGumtree();
		//
		if (algoName.equals("ClassicGumtree"))
			return new CompositeMatchers.ClassicGumtree();
		//
		if (algoName.equals("CompleteGumtreeMatcher"))
			return new CompositeMatchers.CompleteGumtreeMatcher();

		if (algoName.equals("ChangeDistiller"))
			return new CompositeMatchers.ChangeDistiller();
		if (algoName.equals("XyMatcher"))
			return new CompositeMatchers.XyMatcher();

		return null;
	}

	public TuningEngine getEngine() {
		return engine;
	}

	public void setEngine(TuningEngine engine) {
		this.engine = engine;
	}

}
