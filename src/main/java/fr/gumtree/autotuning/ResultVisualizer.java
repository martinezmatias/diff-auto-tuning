package fr.gumtree.autotuning;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.rendersnake.HtmlCanvas;

import com.github.gumtreediff.actions.ChawatheScriptGenerator;
import com.github.gumtreediff.client.diff.webdiff.VanillaDiffHtmlBuilder;
import com.github.gumtreediff.client.diff.webdiff.VanillaDiffView;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.CompositeMatchers.ClassicGumtree;
import com.github.gumtreediff.matchers.CompositeMatchers.CompleteGumtreeMatcher;
import com.github.gumtreediff.matchers.CompositeMatchers.SimpleGumtree;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumTreeProperties;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import fr.gumtree.treediff.jdt.TreeDiffGumTreeBuilder;
import gumtree.spoon.diff.DiffImpl;
import gumtree.spoon.diff.operations.Operation;

public class ResultVisualizer {

	public void saveVisualizationRow(String row, String pathMegadiff, String pathOut) throws IOException {
		String[] split = row.split(",");
		String id = split[0];
		String[] idSplitted = id.split("_");

		String groupId = idSplitted[3];
		String hash = idSplitted[4];
		String filename = idSplitted[5];

		File rootMegadiff = new File(pathMegadiff);

		String diffId = groupId + "_" + hash + "_" + filename + File.separator;

		String patch = File.separator + groupId + File.separator + groupId + "_" + hash + File.separator + filename
				+ File.separator + groupId + "_" + hash + "_" + filename;

		String fl = rootMegadiff.getAbsolutePath() + patch + "_s.java";
		String fr = rootMegadiff.getAbsolutePath() + patch + "_t.java";
		System.out.println(fl);

		File fileLeftt = new File(fl);
		File fileRight = new File(fr);

		if (!fileLeftt.exists() || !fileRight.exists()) {
			System.out.println("Error " + fileLeftt.getAbsolutePath() + " does not exist");
			return;
		}
		String lcontent = new String(Files.readAllBytes(fileLeftt.toPath()));
		ITree tl = new JdtTreeGenerator().generateFrom().string(lcontent).getRoot();

		String rcontent = new String(Files.readAllBytes(fileRight.toPath()));
		ITree tr = new JdtTreeGenerator().generateFrom().string(rcontent).getRoot();

		File fout = new File(pathOut);
		String bestConfig = split[6];

		GumTreeProperties propertiesBest = createProperties(bestConfig);
		Matcher matcherBest = createMatcher(bestConfig);

		String nameBest = "best_" + bestConfig;
		saveVisualization(fileLeftt, tl, fileRight, tr, propertiesBest, matcherBest, fout, diffId, nameBest);
		System.out.println("End best");

		String defaultConfig = split[7];
		GumTreeProperties propertiesDefault = createProperties(defaultConfig);
		Matcher matcherDefault = createMatcher(defaultConfig);

		String nameDefault = "default_" + defaultConfig;
		saveVisualization(fileLeftt, tl, fileRight, tr, propertiesDefault, matcherDefault, fout, diffId, nameDefault);
		System.out.println("End default");

		String nameDefaultNoparam = "default_noparam";
		saveVisualization(fileLeftt, tl, fileRight, tr, new GumTreeProperties(), matcherDefault, fout, diffId,
				nameDefaultNoparam);
		System.out.println("End default");

		FileWriter fwleft = new FileWriter(
				new File(fout.getAbsoluteFile() + File.separator + diffId + File.separator + filename + "_left.java"));
		fwleft.write(lcontent);
		fwleft.close();

		FileWriter fwright = new FileWriter(
				new File(fout.getAbsoluteFile() + File.separator + diffId + File.separator + filename + "_right.java"));
		fwright.write(rcontent);
		fwright.close();
	}

	private Matcher createMatcher(String bestConfig) {
		String props = bestConfig.split("@")[0];

		if (props.toLowerCase().equals(SimpleGumtree.class.getSimpleName().toLowerCase()))
			return new CompositeMatchers.SimpleGumtree();

		if (props.toLowerCase().equals(CompleteGumtreeMatcher.class.getSimpleName().toLowerCase()))
			return new CompositeMatchers.CompleteGumtreeMatcher();

		if (props.toLowerCase().equals(ClassicGumtree.class.getSimpleName().toLowerCase()))
			return new CompositeMatchers.ClassicGumtree();

		return null;
	}

	private GumTreeProperties createProperties(String bestConfig) {
		String[] props = bestConfig.split("@");
		GumTreeProperties properies = new GumTreeProperties();
		for (int i = 1; i < props.length; i = i + 2) {
			properies.put(ConfigurationOptions.valueOf(props[i]), props[i + 1]);
		}
		System.out.println(properies);
		return properies;
	}

	public void saveVisualization(File fileLeftt, ITree tl, File fileRight, ITree tr, GumTreeProperties properies,
			Matcher matcher, File fout, String diffId, String configId) throws IOException {
		DiffImpl result = null;
		result = new DiffImpl(new TreeContext(), tl, tr, new ChawatheScriptGenerator(), matcher, properies);

		List<Operation> actionsAll = result.getAllOperations();

		System.out.println("Size  " + actionsAll.size());
		System.out.println("All " + actionsAll);

		System.out.println("Visualization:");
		com.github.gumtreediff.actions.Diff diffgtt = result.getNativeDiff();

		VanillaDiffHtmlBuilder builder = new VanillaDiffHtmlBuilder(fileLeftt, fileRight,
				(com.github.gumtreediff.actions.Diff) diffgtt);
		builder.produce();

		VanillaDiffView view = new VanillaDiffView(fileLeftt, fileRight, (com.github.gumtreediff.actions.Diff) diffgtt);
		HtmlCanvas html = new HtmlCanvas();
		view.renderOn(html);

		String html2 = html.toHtml();
		html2 = html2.replace("/dist/vanilla.css", "../dist/vanilla.css");

		File parentDiff = new File(fout.getAbsoluteFile() + File.separator + diffId + File.separator);

		File htmlFile = new File(parentDiff.getAbsolutePath() + File.separator + "visualization_" + configId + ".html");
		parentDiff.mkdirs();

		FileWriter fw = new FileWriter(htmlFile);
		fw.write(html2);
		fw.close();
		System.out.println("save at " + htmlFile.getAbsolutePath());

		TreeDiffGumTreeBuilder unifiedrep = new TreeDiffGumTreeBuilder();
		JsonElement jsonunif = unifiedrep.build(diffgtt);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		String json = gson.toJson(jsonunif);

		File uniflFile = new File(parentDiff.getAbsolutePath() + File.separator + "unified_" + configId + ".json");
		fw = new FileWriter(uniflFile);
		fw.write(json);
		fw.close();

	}
}
