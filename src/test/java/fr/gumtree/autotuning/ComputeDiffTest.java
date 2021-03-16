package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.Test;

import com.github.gumtreediff.actions.ChawatheScriptGenerator;
import com.github.gumtreediff.actions.EditScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.CompositeMatchers.SimpleGumtree;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumtreeProperties;
import com.github.gumtreediff.tree.Tree;

import fr.gumtree.autotuning.entity.SingleDiffResult;
import fr.gumtree.autotuning.gumtree.GTProxy;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;

public class ComputeDiffTest {
	SpoonTreeBuilder builder = new SpoonTreeBuilder();
	final File rootMegadiff = new File("./examples/");

	@Test
	public void testChangeInAnnotationSpoonGranularity() throws Exception {

		assertTrue(rootMegadiff.exists());

		GTProxy engine = new GTProxy();

		String fl = rootMegadiff.getAbsolutePath()
				+ "/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_t.java";

		Tree tl = null;
		Tree tr = null;

		tl = builder.build(new File(fl));
		tr = builder.build(new File(fr));

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		ChawatheScriptGenerator edGenerator = new ChawatheScriptGenerator();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();
		assertEquals(1, actionsAll.size());

		// XY has variability
		GumtreeProperties properies = new GumtreeProperties();

		properies.put(ConfigurationOptions.st_minprio, 1);// .st_minprio
		properies.put(ConfigurationOptions.xy_minsim, 0.2);// xy_minsim

		actionsAll = engine.computeDiff(tl, tr, new CompositeMatchers.XyMatcher(), edGenerator, properies).editScript
				.asList();

		assertEquals(1, actionsAll.size());

		properies.put(ConfigurationOptions.st_minprio, 3);// st_minprio
		properies.put(ConfigurationOptions.xy_minsim, 0.2);// xy_minsim

		actionsAll = engine.computeDiff(tl, tr, new CompositeMatchers.XyMatcher(), edGenerator, properies).editScript
				.asList();

		assertEquals(55, actionsAll.size());

		//
		properies.put(ConfigurationOptions.st_minprio, 2);// st_minprio
		properies.put(ConfigurationOptions.xy_minsim, 0.6);// xy_minsim

		actionsAll = engine.computeDiff(tl, tr, new CompositeMatchers.XyMatcher(), edGenerator, properies).editScript
				.asList();

		assertEquals(19, actionsAll.size());

		// Now Classic
		properies = new GumtreeProperties();
		properies.put(ConfigurationOptions.bu_minsim, 0.1);// bu_minsim
		properies.put(ConfigurationOptions.bu_minsize, 100);// bu_minsize
		properies.put(ConfigurationOptions.st_minprio, 1); // st_minprio

		actionsAll = engine.computeDiff(tl, tr, new CompositeMatchers.ClassicGumtree(), edGenerator,
				properies).editScript.asList();

		assertEquals(1, actionsAll.size());

		//
		properies = new GumtreeProperties();
		properies.put(ConfigurationOptions.bu_minsim, 0.2);// bu_minsim
		properies.put(ConfigurationOptions.bu_minsize, 600);// bu_minsize
		properies.put(ConfigurationOptions.st_minprio, 5);

		actionsAll = engine.computeDiff(tl, tr, new CompositeMatchers.ClassicGumtree(), edGenerator,
				properies).editScript.asList();

		assertEquals(1, actionsAll.size());

		/// Forcing to fail
		properies = new GumtreeProperties();
		properies.put(ConfigurationOptions.bu_minsim, 0);
		properies.put(ConfigurationOptions.bu_minsize, 0);
		properies.put(ConfigurationOptions.st_minprio, 299990); // fake value

		actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, properies).editScript.asList();

		assertTrue(actionsAll.size() != 1);

	}

	@Test
	public void testDiffForTest() throws Exception {
		// merge_gtJDT_5_CDJDT_4/1/nr_911_id_1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_JDT.csv

		assertTrue(rootMegadiff.exists());

		String fl = rootMegadiff.getAbsolutePath()
				+ "/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_t.java";
		System.out.println(fl);
		Tree tl = null;
		Tree tr = null;

		tl = builder.build(new File(fl));
		tr = builder.build(new File(fr));

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		ChawatheScriptGenerator edGenerator = new ChawatheScriptGenerator();

		GTProxy engine = new GTProxy();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();
		assertEquals(1, actionsAll.size());
	}

	@Test
	public void testDiffForTestJDT() throws Exception {
		/**
		 * ./gumtree webdiff
		 * /Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded/1/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_s.java
		 * /Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded/1/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_t.java
		 * --port 4568
		 */

		assertTrue(rootMegadiff.exists());

		String fl = rootMegadiff.getAbsolutePath()
				+ "/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_t.java";
		System.out.println(fl);

		String lc = new String(Files.readAllBytes(new File(fl).toPath()));
		Tree tl = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

		lc = new String(Files.readAllBytes(new File(fr).toPath()));
		Tree tr = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

		GTProxy engine = new GTProxy();

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		ChawatheScriptGenerator edGenerator = new ChawatheScriptGenerator();
		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		System.out.println("Size  " + actionsAll.size());
		System.out.println("All " + actionsAll);

		assertEquals(63, actionsAll.size());

		System.out.println(actionsAll);

		System.out.println("After configuring");
		GumtreeProperties properies = new GumtreeProperties();
		//
		properies = new GumtreeProperties();
		properies.put(ConfigurationOptions.bu_minsim, 0.5);
		properies.put(ConfigurationOptions.bu_minsize, 1000);
		properies.put(ConfigurationOptions.st_minprio, 2);

		actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, properies).editScript.asList();

		System.out.println(actionsAll.size());
		System.out.println(actionsAll);

		assertEquals(63, actionsAll.size());

		properies = new GumtreeProperties();
		properies.put(ConfigurationOptions.bu_minsim, 1);
		properies.put(ConfigurationOptions.bu_minsize, 900);
		properies.put(ConfigurationOptions.st_minprio, 2);

		actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, properies).editScript.asList();

		// System.out.println("\n" + properies.getProperties());
		System.out.println("Size  " + actionsAll.size());
		System.out.println("All " + actionsAll);

		assertEquals(118, actionsAll.size());

		int previousSize = actionsAll.size();

		properies = new GumtreeProperties();
		properies.put(ConfigurationOptions.bu_minsim, 0.7);
		properies.put(ConfigurationOptions.bu_minsize, 1900);
		properies.put(ConfigurationOptions.st_minprio, 2);

		actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, properies).editScript.asList();

		// System.out.println("\n" + properies.getProperties());
		System.out.println("Size  " + actionsAll.size());
		System.out.println("All " + actionsAll);

		assertEquals(1, actionsAll.size());

		//
		assertTrue(actionsAll.size() < previousSize);

		// properies.getProperties().clear();
		properies.put(ConfigurationOptions.bu_minsim, 0.1);
		properies.put(ConfigurationOptions.bu_minsize, 100);
		properies.put(ConfigurationOptions.st_minprio, 3);

		actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, properies).editScript.asList();

		// System.out.println("\n" + properies.getProperties());
		System.out.println("Size  " + actionsAll.size());
		System.out.println("All " + actionsAll);

		assertEquals(147, actionsAll.size());
		// Default 2.1.1
		properies = new GumtreeProperties();
		properies.put(ConfigurationOptions.bu_minsim, 0.5);
		properies.put(ConfigurationOptions.bu_minsize, 1000);
		properies.put(ConfigurationOptions.st_minprio, 1);

		actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, properies).editScript.asList();

		System.out.println(actionsAll.size());
		System.out.println(actionsAll);

		assertEquals(7, actionsAll.size());

	}

	/**
	 * Case that simply adds a method invocation
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDiffForTestJDT_1_0007d191() throws Exception {

		assertTrue(rootMegadiff.exists());

		String fl = rootMegadiff.getAbsolutePath()
				+ "/1_0007d191fec7fe2d6a0c4e87594cb286a553f92c/ASTInspector/1_0007d191fec7fe2d6a0c4e87594cb286a553f92c_ASTInspector_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/1_0007d191fec7fe2d6a0c4e87594cb286a553f92c/ASTInspector/1_0007d191fec7fe2d6a0c4e87594cb286a553f92c_ASTInspector_t.java";
		System.out.println(fl);

		String lc = new String(Files.readAllBytes(new File(fl).toPath()));
		Tree tl = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

		lc = new String(Files.readAllBytes(new File(fr).toPath()));
		Tree tr = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

		GTProxy engine = new GTProxy();

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		ChawatheScriptGenerator edGenerator = new ChawatheScriptGenerator();
		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		EditScriptGenerator edGeneratorSimplified = new SimplifiedChawatheScriptGenerator();

		System.out.println("Size  " + actionsAll.size());
		System.out.println("All " + actionsAll);

		assertEquals(886, actionsAll.size());

		// Now simplified
		actionsAll = engine.computeDiff(tl, tr, matcher, edGeneratorSimplified, new GumtreeProperties()).editScript
				.asList();
		assertEquals(543, actionsAll.size());

		System.out.println(actionsAll);

		System.out.println("After configuring");
		GumtreeProperties properies = new GumtreeProperties();
		// vanillaDiffView_best_SimpleGumtree@bu_minsim_SBUP@0.1@st_minprio@1.html
		properies = new GumtreeProperties();
		properies.put(ConfigurationOptions.st_minprio, 1);

		actionsAll = engine.computeDiff(tl, tr, new CompositeMatchers.SimpleGumtree(), edGenerator,
				properies).editScript.asList();

		System.out.println(actionsAll.size());
		System.out.println(actionsAll);

		assertEquals(8, actionsAll.size());

		List<Action> actionsSimplified = engine.computeDiff(tl, tr, new CompositeMatchers.SimpleGumtree(),
				edGeneratorSimplified, properies).editScript.asList();

		assertEquals(1, actionsSimplified.size());

	}

	/**
	 * Updates a comment
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDiffForTestJDT_2_03() throws Exception {

		assertTrue(rootMegadiff.exists());

		String fl = rootMegadiff.getAbsolutePath()
				+ "/2_03b1dec4d20cee110b68cf8325f28f4403468317/FTPClient/2_03b1dec4d20cee110b68cf8325f28f4403468317_FTPClient_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/2_03b1dec4d20cee110b68cf8325f28f4403468317/FTPClient/2_03b1dec4d20cee110b68cf8325f28f4403468317_FTPClient_t.java";
		System.out.println(fl);

		String lc = new String(Files.readAllBytes(new File(fl).toPath()));
		Tree tl = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

		lc = new String(Files.readAllBytes(new File(fr).toPath()));
		Tree tr = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

		GTProxy engine = new GTProxy();

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		ChawatheScriptGenerator edGenerator = new ChawatheScriptGenerator();
		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		EditScriptGenerator edGeneratorSimplified = new SimplifiedChawatheScriptGenerator();

		System.out.println("Size  " + actionsAll.size());
		System.out.println("All " + actionsAll);

		assertTrue(actionsAll.size() > 100);

		// Now simplified
		actionsAll = engine.computeDiff(tl, tr, matcher, edGeneratorSimplified, new GumtreeProperties()).editScript
				.asList();
		assertTrue(actionsAll.size() > 1);

		System.out.println(actionsAll);

		System.out.println("After configuring");

		GumtreeProperties properies = new GumtreeProperties();
		// vanillaDiffView_best_SimpleGumtree@bu_minsim_SBUP@0.1@st_minprio@1.html
		properies = new GumtreeProperties();
		properies.put(ConfigurationOptions.st_minprio, 1);

		actionsAll = engine.computeDiff(tl, tr, new CompositeMatchers.SimpleGumtree(), edGenerator,
				properies).editScript.asList();

		System.out.println(actionsAll.size());
		System.out.println(actionsAll);

		assertEquals(1, actionsAll.size());

		List<Action> actionsSimplified = engine.computeDiff(tl, tr, new CompositeMatchers.SimpleGumtree(),
				edGeneratorSimplified, properies).editScript.asList();

		assertEquals(1, actionsSimplified.size());

	}

	/**
	 * Insertion of Import and update method invocation
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDiffForTestJDT_3_04f() throws Exception {

		assertTrue(rootMegadiff.exists());

		String fl = rootMegadiff.getAbsolutePath()
				+ "/3_04f0e8f7a3545cf877c10967396b06595d57c34a/JavaExtensions/3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/3_04f0e8f7a3545cf877c10967396b06595d57c34a/JavaExtensions/3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions_t.java";
		System.out.println(fl);

		String lc = new String(Files.readAllBytes(new File(fl).toPath()));
		Tree tl = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

		lc = new String(Files.readAllBytes(new File(fr).toPath()));
		Tree tr = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

		GTProxy engine = new GTProxy();

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		ChawatheScriptGenerator edGenerator = new ChawatheScriptGenerator();
		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		EditScriptGenerator edGeneratorSimplified = new SimplifiedChawatheScriptGenerator();

		System.out.println("Size  " + actionsAll.size());
		System.out.println("All " + actionsAll);

		// assertTrue(actionsAll.size() > 100);

		// Now simplified
		actionsAll = engine.computeDiff(tl, tr, matcher, edGeneratorSimplified, new GumtreeProperties()).editScript
				.asList();
		assertTrue(actionsAll.size() > 2);

		System.out.println(actionsAll);

		System.out.println("After configuring");

		GumtreeProperties properies = new GumtreeProperties();
		// vanillaDiffView_best_SimpleGumtree@bu_minsim_SBUP@0.1@st_minprio@1.html
		properies = new GumtreeProperties();
		properies.put(ConfigurationOptions.st_minprio, 1);

		actionsAll = engine.computeDiff(tl, tr, new CompositeMatchers.SimpleGumtree(), edGenerator,
				properies).editScript.asList();

		System.out.println(actionsAll.size());
		System.out.println(actionsAll);

		// assertEquals(1, actionsAll.size());

		List<Action> actionsSimplified = engine.computeDiff(tl, tr, new CompositeMatchers.SimpleGumtree(),
				edGeneratorSimplified, properies).editScript.asList();

		assertEquals(3, actionsSimplified.size());

	}

	/**
	 * Insertion of Import and update method invocation
	 * 
	 * @throws Exception
	 */
	@Test
	public void testDiffForTestJDT_java_example() throws Exception {

		assertTrue(rootMegadiff.exists());

		String fl = "./examples/java_simple_case_1/T1_s.java";
		String fr = "./examples/java_simple_case_1/T1_t.java";
		System.out.println(fl);

		String lc = new String(Files.readAllBytes(new File(fl).toPath()));
		Tree tl = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

		lc = new String(Files.readAllBytes(new File(fr).toPath()));
		Tree tr = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

		GTProxy engine = new GTProxy();

		// CompositeMatchers.ClassicGumtree matcher = new
		// CompositeMatchers.ClassicGumtree();
		SimpleGumtree matcher = new SimpleGumtree();
		ChawatheScriptGenerator edGenerator = new ChawatheScriptGenerator();
		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		EditScriptGenerator edGeneratorSimplified = new SimplifiedChawatheScriptGenerator();

		System.out.println("Size  " + actionsAll.size());
		System.out.println("All " + actionsAll);

		// assertTrue(actionsAll.size() > 100);

		GumtreeProperties properties = new GumtreeProperties();

		properties = new GumtreeProperties();
		// Using min = 1, the imports and package declaration are mapped.
		properties.tryConfigure(ConfigurationOptions.st_minprio, 1);

		// Now simplified
		actionsAll = engine.computeDiff(tl, tr, matcher, edGeneratorSimplified, properties).editScript.asList();
		assertEquals(1, actionsAll.size());

		System.out.println(actionsAll);

		System.out.println("After configuring");

	}

	@Test
	public void testComplete_Matcher_1_203910661b7277() throws Exception {
		GTProxy engine = new GTProxy();

		// --0/0
		// nr actions: 1
		// time: 1291
		// config: {st_priocalc=height, bu_minsim=0.5, st_minprio=2, bu_minsize=2000}

		File fs = new File(
				"./examples/megadiff-sample/1/1_203910661b72775d1a983bf98c25ddde2d2898b9/Producto/1_203910661b72775d1a983bf98c25ddde2d2898b9_Producto_s.java");
		File ft = new File(
				"./examples/megadiff-sample/1/1_203910661b72775d1a983bf98c25ddde2d2898b9/Producto/1_203910661b72775d1a983bf98c25ddde2d2898b9_Producto_t.java");

		Tree tl = null;
		Tree tr = null;
		SpoonTreeBuilder builder = new SpoonTreeBuilder();
		tl = builder.build(fs);
		tr = builder.build(ft);

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();

		GumtreeProperties properies = new GumtreeProperties();

		// {st_priocalc=height, bu_minsim=0.5, st_minprio=2, bu_minsize=2000}
		properies.put(ConfigurationOptions.st_priocalc, "height");// .st_minprio
		properies.put(ConfigurationOptions.bu_minsim, 0.5);
		properies.put(ConfigurationOptions.st_minprio, 2);
		properies.put(ConfigurationOptions.bu_minsize, 2000);

		SingleDiffResult result = engine.runDiff(tl, tr, matcher, properies);
		System.out.println(result);

		assertEquals(1, result.get("NRACTIONS"));

		properies.put(ConfigurationOptions.st_priocalc, "height");// .st_minprio
		properies.put(ConfigurationOptions.bu_minsim, 0.1);
		properies.put(ConfigurationOptions.st_minprio, 3);
		properies.put(ConfigurationOptions.bu_minsize, 100);

		result = engine.runDiff(tl, tr, matcher, properies);
		System.out.println(result);

		assertTrue((int) result.get("NRACTIONS") > 300);

		properies.put(ConfigurationOptions.st_priocalc, "height");// .st_minprio
		properies.put(ConfigurationOptions.bu_minsim, 0.8);
		properies.put(ConfigurationOptions.st_minprio, 4);
		properies.put(ConfigurationOptions.bu_minsize, 100);

		result = engine.runDiff(tl, tr, matcher, properies);
		System.out.println(result);

		assertTrue((int) result.get("NRACTIONS") > 600);

		properies.put(ConfigurationOptions.st_priocalc, "height");// .st_minprio
		properies.put(ConfigurationOptions.bu_minsim, 0.9);
		properies.put(ConfigurationOptions.st_minprio, 2);
		properies.put(ConfigurationOptions.bu_minsize, 600);

		result = engine.runDiff(tl, tr, matcher, properies);
		System.out.println(result);

		assertTrue((int) result.get("NRACTIONS") > 20);

		properies.put(ConfigurationOptions.st_priocalc, "height");// .st_minprio
		properies.put(ConfigurationOptions.bu_minsim, 0.1);
		properies.put(ConfigurationOptions.st_minprio, 1);
		properies.put(ConfigurationOptions.bu_minsize, 100);

		result = engine.runDiff(tl, tr, matcher, properies);
		System.out.println(result);

		// assertTrue((int) result.get("NRACTIONS") > 70);

		properies.put(ConfigurationOptions.st_priocalc, "height");// .st_minprio
		properies.put(ConfigurationOptions.bu_minsim, 1);
		properies.put(ConfigurationOptions.st_minprio, 1);
		properies.put(ConfigurationOptions.bu_minsize, 1800);

		result = engine.runDiff(tl, tr, matcher, properies);
		System.out.println(result);

		assertTrue((int) result.get("NRACTIONS") == 1);

	}

	@Test
	public void testComplete_Matcher_Int2ObjectOpenHashMap() throws Exception {
		GTProxy engine = new GTProxy();

		File fs = new File(
				"./examples/megadiff-sample/1/1_203910661b72775d1a983bf98c25ddde2d2898b9/Producto/1_203910661b72775d1a983bf98c25ddde2d2898b9_Producto_s.java");
		File ft = new File(
				"./examples/megadiff-sample/1/1_203910661b72775d1a983bf98c25ddde2d2898b9/Producto/1_203910661b72775d1a983bf98c25ddde2d2898b9_Producto_t.java");

		Tree tl = null;
		Tree tr = null;
		SpoonTreeBuilder builder = new SpoonTreeBuilder();
		tl = builder.build(fs);
		tr = builder.build(ft);

		CompositeMatchers.CompleteGumtreeMatcher matcher = new CompositeMatchers.CompleteGumtreeMatcher();

		GumtreeProperties properies = new GumtreeProperties();

		// CompleteGumtreeMatcher_{st_priocalc=height, bu_minsim=0.2, st_minprio=3,
		// bu_minsize=1000}
		properies.put(ConfigurationOptions.st_priocalc, "height");
		properies.put(ConfigurationOptions.bu_minsim, 0.2);
		properies.put(ConfigurationOptions.st_minprio, 3);
		properies.put(ConfigurationOptions.bu_minsize, 1000);

		SingleDiffResult result = engine.runDiff(tl, tr, matcher, properies);
		System.out.println(result);

		assertEquals(1, result.get("NRACTIONS"));

	}

}
