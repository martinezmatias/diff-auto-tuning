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
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumTreeProperties;
import com.github.gumtreediff.tree.Tree;

import fr.gumtree.autotuning.treebuilder.JDTTreeBuilder;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;

public class CasesTest {

//	private SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();
	// private AstComparator diff = new AstComparator();

	final File rootMegadiff = new File("./examples/");

	@Test
	public void testChangesSpoon() throws Exception {

		assertTrue(rootMegadiff.exists());

		TuningEngine engine = new TuningEngine();

		String fl = rootMegadiff.getAbsolutePath()
				+ "/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_t.java";

		Tree tl = null;
		Tree tr = null;
		SpoonTreeBuilder builder = new SpoonTreeBuilder();
		tl = builder.build(new File(fl));
		tr = builder.build(new File(fr));

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		ChawatheScriptGenerator edGenerator = new ChawatheScriptGenerator();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumTreeProperties());
		System.out.println(actionsAll);
		assertEquals(1, actionsAll.size());
	}

	@Test
	public void testChangesJDT() throws Exception {

		assertTrue(rootMegadiff.exists());

		TuningEngine engine = new TuningEngine();

		String fl = rootMegadiff.getAbsolutePath()
				+ "/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_t.java";

		Tree tl = null;
		Tree tr = null;
		JDTTreeBuilder builder = new JDTTreeBuilder();
		tl = builder.build(new File(fl));
		tr = builder.build(new File(fr));

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		SimplifiedChawatheScriptGenerator edGenerator = new SimplifiedChawatheScriptGenerator();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumTreeProperties());

		System.out.println(actionsAll);
		assertEquals(1, actionsAll.size());
	}

	@Test
	public void testChangeInAnnotationSpoonGranularity() throws Exception {

		assertTrue(rootMegadiff.exists());

		TuningEngine engine = new TuningEngine();

		String fl = rootMegadiff.getAbsolutePath()
				+ "/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_t.java";

		Tree tl = null;
		Tree tr = null;

		tl = scanner.getTree(diff.getCtType(new File(fl)));
		tr = scanner.getTree(diff.getCtType(new File(fr)));

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		ChawatheScriptGenerator edGenerator = new ChawatheScriptGenerator();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumTreeProperties());
		assertEquals(1, actionsAll.size());

		// XY has variability
		GumTreeProperties properies = new GumTreeProperties();

		properies.put(ConfigurationOptions.GT_STM_MH, 1);
		properies.put(ConfigurationOptions.GT_XYM_SIM, 0.2);

		actionsAll = engine.computeDiff(tl, tr, new CompositeMatchers.XyMatcher(), edGenerator, properies);

		assertEquals(1, actionsAll.size());

		properies.put(ConfigurationOptions.GT_STM_MH, 3);
		properies.put(ConfigurationOptions.GT_XYM_SIM, 0.2);

		actionsAll = engine.computeDiff(tl, tr, new CompositeMatchers.XyMatcher(), edGenerator, properies);

		assertEquals(55, actionsAll.size());

		//
		properies.put(ConfigurationOptions.GT_STM_MH, 2);
		properies.put(ConfigurationOptions.GT_XYM_SIM, 0.6);

		actionsAll = engine.computeDiff(tl, tr, new CompositeMatchers.XyMatcher(), edGenerator, properies);

		assertEquals(19, actionsAll.size());

		// Now Classic
		properies.getProperties().clear();
		properies.put(ConfigurationOptions.GT_BUM_SMT, 0.1);
		properies.put(ConfigurationOptions.GT_BUM_SZT, 100);
		properies.put(ConfigurationOptions.GT_STM_MH, 1);

		actionsAll = engine.computeDiff(tl, tr, new CompositeMatchers.ClassicGumtree(), edGenerator, properies);

		assertEquals(1, actionsAll.size());

		//
		properies.getProperties().clear();
		properies.put(ConfigurationOptions.GT_BUM_SMT, 0.2);
		properies.put(ConfigurationOptions.GT_BUM_SZT, 600);
		properies.put(ConfigurationOptions.GT_STM_MH, 5);

		actionsAll = engine.computeDiff(tl, tr, new CompositeMatchers.ClassicGumtree(), edGenerator, properies);

		assertEquals(1, actionsAll.size());

		/// Forcing to fail
		properies.getProperties().clear();
		properies.put(ConfigurationOptions.GT_BUM_SMT, 0);
		properies.put(ConfigurationOptions.GT_BUM_SZT, 0);
		properies.put(ConfigurationOptions.GT_STM_MH, 299990); // fake value

		actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, properies);

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

		tl = scanner.getTree(diff.getCtType(new File(fl)));
		tr = scanner.getTree(diff.getCtType(new File(fr)));

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		ChawatheScriptGenerator edGenerator = new ChawatheScriptGenerator();

		TuningEngine engine = new TuningEngine();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumTreeProperties());
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

		TuningEngine engine = new TuningEngine();

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		ChawatheScriptGenerator edGenerator = new ChawatheScriptGenerator();
		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumTreeProperties());

		System.out.println("Size  " + actionsAll.size());
		System.out.println("All " + actionsAll);

		assertEquals(63, actionsAll.size());

		System.out.println(actionsAll);

		System.out.println("After configuring");
		GumTreeProperties properies = new GumTreeProperties();
		//
		properies.getProperties().clear();
		properies.put(ConfigurationOptions.GT_BUM_SMT, 0.5);
		properies.put(ConfigurationOptions.GT_BUM_SZT, 1000);
		properies.put(ConfigurationOptions.GT_STM_MH, 2);

		actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, properies);

		System.out.println(actionsAll.size());
		System.out.println(actionsAll);

		assertEquals(63, actionsAll.size());

		properies.getProperties().clear();
		properies.put(ConfigurationOptions.GT_BUM_SMT, 1);
		properies.put(ConfigurationOptions.GT_BUM_SZT, 900);
		properies.put(ConfigurationOptions.GT_STM_MH, 2);

		actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, properies);

		System.out.println("\n" + properies.getProperties());
		System.out.println("Size  " + actionsAll.size());
		System.out.println("All " + actionsAll);

		assertEquals(118, actionsAll.size());

		int previousSize = actionsAll.size();

		properies.getProperties().clear();
		properies.put(ConfigurationOptions.GT_BUM_SMT, 0.7);
		properies.put(ConfigurationOptions.GT_BUM_SZT, 1900);
		properies.put(ConfigurationOptions.GT_STM_MH, 2);

		actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, properies);

		System.out.println("\n" + properies.getProperties());
		System.out.println("Size  " + actionsAll.size());
		System.out.println("All " + actionsAll);

		assertEquals(1, actionsAll.size());

		//
		assertTrue(actionsAll.size() < previousSize);

		properies.getProperties().clear();
		properies.put(ConfigurationOptions.GT_BUM_SMT, 0.1);
		properies.put(ConfigurationOptions.GT_BUM_SZT, 100);
		properies.put(ConfigurationOptions.GT_STM_MH, 3);

		actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, properies);

		System.out.println("\n" + properies.getProperties());
		System.out.println("Size  " + actionsAll.size());
		System.out.println("All " + actionsAll);

		assertEquals(147, actionsAll.size());
		// Default 2.1.1
		properies.getProperties().clear();
		properies.put(ConfigurationOptions.GT_BUM_SMT, 0.5);
		properies.put(ConfigurationOptions.GT_BUM_SZT, 1000);
		properies.put(ConfigurationOptions.GT_STM_MH, 1);

		actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, properies);

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

		TuningEngine engine = new TuningEngine();

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		ChawatheScriptGenerator edGenerator = new ChawatheScriptGenerator();
		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumTreeProperties());

		EditScriptGenerator edGeneratorSimplified = new SimplifiedChawatheScriptGenerator();

		System.out.println("Size  " + actionsAll.size());
		System.out.println("All " + actionsAll);

		assertEquals(886, actionsAll.size());

		// Now simplified
		actionsAll = engine.computeDiff(tl, tr, matcher, edGeneratorSimplified, new GumTreeProperties());
		assertEquals(543, actionsAll.size());

		System.out.println(actionsAll);

		System.out.println("After configuring");
		GumTreeProperties properies = new GumTreeProperties();
		// vanillaDiffView_best_SimpleGumtree@GT_BUM_SMT_SBUP@0.1@GT_STM_MH@1.html
		properies.getProperties().clear();
		properies.put(ConfigurationOptions.GT_BUM_SMT_SBUP, 0.1);
		properies.put(ConfigurationOptions.GT_STM_MH, 1);

		actionsAll = engine.computeDiff(tl, tr, new CompositeMatchers.SimpleGumtree(), edGenerator, properies);

		System.out.println(actionsAll.size());
		System.out.println(actionsAll);

		assertEquals(8, actionsAll.size());

		List<Action> actionsSimplified = engine.computeDiff(tl, tr, new CompositeMatchers.SimpleGumtree(),
				edGeneratorSimplified, properies);

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

		TuningEngine engine = new TuningEngine();

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		ChawatheScriptGenerator edGenerator = new ChawatheScriptGenerator();
		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumTreeProperties());

		EditScriptGenerator edGeneratorSimplified = new SimplifiedChawatheScriptGenerator();

		System.out.println("Size  " + actionsAll.size());
		System.out.println("All " + actionsAll);

		assertTrue(actionsAll.size() > 100);

		// Now simplified
		actionsAll = engine.computeDiff(tl, tr, matcher, edGeneratorSimplified, new GumTreeProperties());
		assertTrue(actionsAll.size() > 1);

		System.out.println(actionsAll);

		System.out.println("After configuring");

		GumTreeProperties properies = new GumTreeProperties();
		// vanillaDiffView_best_SimpleGumtree@GT_BUM_SMT_SBUP@0.1@GT_STM_MH@1.html
		properies.getProperties().clear();
		properies.put(ConfigurationOptions.GT_BUM_SMT_SBUP, 0.1);
		properies.put(ConfigurationOptions.GT_STM_MH, 1);

		actionsAll = engine.computeDiff(tl, tr, new CompositeMatchers.SimpleGumtree(), edGenerator, properies);

		System.out.println(actionsAll.size());
		System.out.println(actionsAll);

		assertEquals(1, actionsAll.size());

		List<Action> actionsSimplified = engine.computeDiff(tl, tr, new CompositeMatchers.SimpleGumtree(),
				edGeneratorSimplified, properies);

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

		TuningEngine engine = new TuningEngine();

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		ChawatheScriptGenerator edGenerator = new ChawatheScriptGenerator();
		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumTreeProperties());

		EditScriptGenerator edGeneratorSimplified = new SimplifiedChawatheScriptGenerator();

		System.out.println("Size  " + actionsAll.size());
		System.out.println("All " + actionsAll);

		// assertTrue(actionsAll.size() > 100);

		// Now simplified
		actionsAll = engine.computeDiff(tl, tr, matcher, edGeneratorSimplified, new GumTreeProperties());
		assertTrue(actionsAll.size() > 2);

		System.out.println(actionsAll);

		System.out.println("After configuring");

		GumTreeProperties properies = new GumTreeProperties();
		// vanillaDiffView_best_SimpleGumtree@GT_BUM_SMT_SBUP@0.1@GT_STM_MH@1.html
		properies.getProperties().clear();
		properies.put(ConfigurationOptions.GT_BUM_SMT_SBUP, 0.1);
		properies.put(ConfigurationOptions.GT_STM_MH, 1);

		actionsAll = engine.computeDiff(tl, tr, new CompositeMatchers.SimpleGumtree(), edGenerator, properies);

		System.out.println(actionsAll.size());
		System.out.println(actionsAll);

		// assertEquals(1, actionsAll.size());

		List<Action> actionsSimplified = engine.computeDiff(tl, tr, new CompositeMatchers.SimpleGumtree(),
				edGeneratorSimplified, properies);

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
		String fr = "./examples/java_simple_case_1/T1_s.java";
		System.out.println(fl);

		String lc = new String(Files.readAllBytes(new File(fl).toPath()));
		Tree tl = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

		lc = new String(Files.readAllBytes(new File(fr).toPath()));
		Tree tr = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

		TuningEngine engine = new TuningEngine();

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		ChawatheScriptGenerator edGenerator = new ChawatheScriptGenerator();
		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumTreeProperties());

		EditScriptGenerator edGeneratorSimplified = new SimplifiedChawatheScriptGenerator();

		System.out.println("Size  " + actionsAll.size());
		System.out.println("All " + actionsAll);

		// assertTrue(actionsAll.size() > 100);

		// Now simplified
		actionsAll = engine.computeDiff(tl, tr, matcher, edGeneratorSimplified, new GumTreeProperties());
		assertEquals(1, actionsAll.size());

		System.out.println(actionsAll);

		System.out.println("After configuring");

	}

}
