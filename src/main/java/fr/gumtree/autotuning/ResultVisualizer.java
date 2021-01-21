package fr.gumtree.autotuning;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import org.rendersnake.HtmlCanvas;
import org.rendersnake.Renderable;

import com.github.difflib.DiffUtils;
import com.github.difflib.UnifiedDiffUtils;
import com.github.difflib.algorithm.DiffException;
import com.github.difflib.patch.Patch;
import com.github.gumtreediff.actions.ChawatheScriptGenerator;
import com.github.gumtreediff.actions.Diff;
import com.github.gumtreediff.actions.EditScript;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.client.diff.webdiff.MergelyDiffView2;
import com.github.gumtreediff.client.diff.webdiff.TextDiffView;
import com.github.gumtreediff.client.diff.webdiff.VanillaDiffHtmlBuilder;
import com.github.gumtreediff.client.diff.webdiff.VanillaDiffView;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.CompositeMatchers.ClassicGumtree;
import com.github.gumtreediff.matchers.CompositeMatchers.CompleteGumtreeMatcher;
import com.github.gumtreediff.matchers.CompositeMatchers.CompositeMatcher;
import com.github.gumtreediff.matchers.CompositeMatchers.SimpleGumtree;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumTreeProperties;
import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.gumtree.autotuning.treebuilder.JDTTreeBuilder;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;
import fr.gumtree.treediff.jdt.TreeDiffFormatBuilder;

public class ResultVisualizer {

	public boolean saveVisualizationRow(String row, String pathMegadiff, String pathOut, boolean isJDTModel)
			throws Exception {

		System.out.println("*****\nRow to analyze " + row);
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
		// System.out.println(fl);

		File fileLeftt = new File(fl);
		File fileRight = new File(fr);

		if (!fileLeftt.exists() || !fileRight.exists()) {
			System.out.println("Error " + fileLeftt.getAbsolutePath() + " does not exist");
			return false;
		}
		String lcontent = new String(Files.readAllBytes(fileLeftt.toPath()));

		String rcontent = new String(Files.readAllBytes(fileRight.toPath()));

		ITreeBuilder builder = null;
		if (isJDTModel) {
			builder = new JDTTreeBuilder();
		} else {
			builder = new SpoonTreeBuilder();
		}
		ITree tl = builder.build(fileLeftt);
		ITree tr = builder.build(fileRight);

		File fout = new File(pathOut);

		String bestConfig = split[6];
		System.out.println("--Running for best: " + bestConfig);
		GumTreeProperties propertiesBest = createProperties(bestConfig);
		Matcher matcherBest = createMatcher(bestConfig);
		System.out.println(propertiesBest.getProperties());
		String nameBest = "best_" + bestConfig;

		saveVisualization(fileLeftt, tl, fileRight, tr, propertiesBest, matcherBest, fout, diffId, nameBest);
		System.out.println("End best");

		String defaultConfig = split[7];
		GumTreeProperties propertiesDefault = createProperties(defaultConfig);
		Matcher matcherDefault = createMatcher(defaultConfig);

		System.out.println("--Running for default: " + defaultConfig);
		System.out.println(propertiesDefault.getProperties());
		String nameDefault = "default_" + defaultConfig;
		saveVisualization(fileLeftt, tl, fileRight, tr, propertiesDefault, matcherDefault, fout, diffId, nameDefault);
		System.out.println("End default");

		System.out.println("--Running for nonparameters ");
		String nameDefaultNoparam = "default_noparam";
		saveVisualization(fileLeftt, tl, fileRight, tr, new GumTreeProperties(), matcherDefault, fout, diffId,
				nameDefaultNoparam);
		System.out.println("End default");

		File fileLoriginal = new File(
				fout.getAbsoluteFile() + File.separator + diffId + File.separator + filename + "_left.java");
		FileWriter fwleft = new FileWriter(fileLoriginal);
		fwleft.write(lcontent);
		fwleft.close();

		File fileRoriginal = new File(
				fout.getAbsoluteFile() + File.separator + diffId + File.separator + filename + "_right.java");
		FileWriter fwright = new FileWriter(fileRoriginal);
		fwright.write(rcontent);
		fwright.close();

		///
		// exportMergely(diffId, fout, fileLoriginal, fileRoriginal);

		exportUnifiedDiff(diffId, fout, fileLoriginal, fileRoriginal);

		return true;
	}

	public void exportUnifiedDiff(String diffId, File fout, File fileLoriginal, File fileRoriginal)
			throws IOException, DiffException {
		// build simple lists of the lines of the two testfiles
		List<String> original = Files.readAllLines(fileLoriginal.toPath());
		List<String> revised = Files.readAllLines(fileRoriginal.toPath());

		// compute the patch: this is the diffutils part
		Patch<String> patch2 = DiffUtils.diff(original, revised);

		// generating unified diff format
		List<String> unifiedDiff = UnifiedDiffUtils.generateUnifiedDiff(fileLoriginal.getName(),
				fileRoriginal.getName(), original, patch2, 3);

		File parentDiff = new File(fout.getAbsoluteFile() + File.separator + diffId + File.separator);

		File diffFile = new File(parentDiff.getAbsolutePath() + File.separator + "diff.txt");

		parentDiff.mkdirs();

		FileWriter fw = new FileWriter(diffFile);
		for (String line : unifiedDiff) {
			fw.write(line);
			fw.write("\n");
		}
		fw.close();
		System.out.println("save at " + diffFile.getAbsolutePath());

		// System.out.println(patch2);

		// simple output the computed patch to console
		// for (AbstractDelta<String> delta : patch2.getDeltas()) {
		// System.out.println(delta);
		// }
	}

	public void exportMergely(String diffId, File fout, File fileLoriginal, File fileRoriginal) throws IOException {
		Renderable view2 = new MergelyDiffView2(0, fileLoriginal.getName(), fileRoriginal.getName());
		HtmlCanvas html = new HtmlCanvas();
		view2.renderOn(html);

		String html2 = html.toHtml();
		html2 = html2.replace("/dist/", "../../libs/dist/");

		File parentDiff = new File(fout.getAbsoluteFile() + File.separator + diffId + File.separator);

		File htmlFile = new File(parentDiff.getAbsolutePath() + File.separator + "simple.html");
		parentDiff.mkdirs();

		FileWriter fw = new FileWriter(htmlFile);
		fw.write(html2);
		fw.close();
		System.out.println("save at " + htmlFile.getAbsolutePath());
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

		MappingStore mappings = matcher.match(tl, tr);
		CompositeMatcher cm = (CompositeMatcher) matcher;
		cm.configure(properies);

		EditScript editScript = new ChawatheScriptGenerator().computeActions(mappings);

		List<Action> actionsAll = editScript.asList();

		TreeContext leftContext = new TreeContext();
		leftContext.setRoot(tl);
		TreeContext rightContext = new TreeContext();
		rightContext.setRoot(tr);

		Diff diffgtt = new Diff(leftContext, rightContext, mappings, editScript);

		File parentDiff = new File(fout.getAbsoluteFile() + File.separator + diffId + File.separator);
		parentDiff.mkdirs();

		saveDiffInfo(parentDiff, actionsAll, configId);

		VanillaDiffHtmlBuilder builder = new VanillaDiffHtmlBuilder(fileLeftt, fileRight,
				(com.github.gumtreediff.actions.Diff) diffgtt);
		builder.produce();

		//

		// VanillaDiffView
		Renderable view = new VanillaDiffView(fileLeftt, fileRight, (com.github.gumtreediff.actions.Diff) diffgtt);
		saveRenderable(parentDiff, diffId, configId, view, "vanillaDiffView");

		// Text
		Renderable viewText = new TextDiffView(fileLeftt, fileRight, (com.github.gumtreediff.actions.Diff) diffgtt);
		saveRenderable(parentDiff, diffId, configId, viewText, "textDiffView");

		// Unified
		saveUnified(configId, diffgtt, parentDiff);

	}

	public void saveDiffInfo(File parentDiff, List<Action> actionsAll, String configId) throws IOException {
		System.out.println("Size all ops " + actionsAll.size());

		JsonObject jsonunif = new JsonObject();
		jsonunif.addProperty("nrAllActions", actionsAll.size());

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		String json = gson.toJson(jsonunif);

		File uniflFile = new File(parentDiff.getAbsolutePath() + File.separator + "infoActions_" + configId + ".json");
		FileWriter fw = new FileWriter(uniflFile);
		fw.write(json);
		fw.close();

	}

	public void saveUnified(String configId, com.github.gumtreediff.actions.Diff diffgtt, File parentDiff)
			throws IOException {
		TreeDiffFormatBuilder unifiedrep = new TreeDiffFormatBuilder();
		JsonElement jsonunif = unifiedrep.build(diffgtt);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		String json = gson.toJson(jsonunif);

		File uniflFile = new File(
				parentDiff.getAbsolutePath() + File.separator + "treeDiffSerialFormat_" + configId + ".json");
		FileWriter fw = new FileWriter(uniflFile);
		fw.write(json);
		fw.close();
	}

	public File saveRenderable(File parentDiff, String diffId, String configId, Renderable view, String name)
			throws IOException {
		HtmlCanvas html = new HtmlCanvas();
		view.renderOn(html);

		String html2 = html.toHtml();
		// html2 = html2.replace("/dist/vanilla.css", "../dist/vanilla.css");
		html2 = html2.replace("/dist/", "../../libs/dist/");
		// html2 = html2.replace("/monaco/", "../monaco/");

		File htmlFile = new File(parentDiff.getAbsolutePath() + File.separator + name + "_" + configId + ".html");

		FileWriter fw = new FileWriter(htmlFile);
		fw.write(html2);
		fw.close();
		System.out.println("save at " + htmlFile.getAbsolutePath());
		return parentDiff;
	}
}
