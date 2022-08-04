package fr.gumtree.autotuning.diff;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.github.gumtreediff.actions.ChawatheScriptGenerator;
import com.github.gumtreediff.actions.SimplifiedChawatheScriptGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.actions.model.Insert;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.CompositeMatchers.CompositeMatcher;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumtreeProperties;
import com.github.gumtreediff.matchers.heuristic.gt.GreedyBottomUpMatcher;
import com.github.gumtreediff.matchers.heuristic.gt.GreedySubtreeMatcher;
import com.github.gumtreediff.tree.Tree;

import fr.gumtree.autotuning.gumtree.GTProxy;
import fr.gumtree.autotuning.treebuilder.ITreeBuilder;
import fr.gumtree.autotuning.treebuilder.JDTTreeBuilder;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;
import spoon.reflect.code.CtReturn;
import spoon.reflect.declaration.CtAnnotation;

public class BuilderTest {

	final File rootMegadiff = new File("./examples/");

	@Test
	public void testChangesSpoon_1_02f3fd() throws Exception {

		assertTrue(rootMegadiff.exists());

		GTProxy engine = new GTProxy();

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

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();
		System.out.println(actionsAll);
		assertEquals(1, actionsAll.size());
	}

	@Test
	public void testChangesJDT_1_02f3fd() throws Exception {

		assertTrue(rootMegadiff.exists());

		GTProxy engine = new GTProxy();

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

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		System.out.println(actionsAll);
		assertEquals(1, actionsAll.size());
	}

	@Test
	public void testChangesJDT_1_0a664d() throws Exception {

		assertTrue(rootMegadiff.exists());

		GTProxy engine = new GTProxy();

		String fl = rootMegadiff.getAbsolutePath()
				+ "/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_t.java";

		Tree tl = null;
		Tree tr = null;
		JDTTreeBuilder builder = new JDTTreeBuilder();
		tl = builder.build(new File(fl));
		tr = builder.build(new File(fr));

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		SimplifiedChawatheScriptGenerator edGenerator = new SimplifiedChawatheScriptGenerator();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		System.out.println(actionsAll);
		assertEquals(7, actionsAll.size());
	}

	@Test
	public void testChangesSpoon_1_0a664d() throws Exception {

		assertTrue(rootMegadiff.exists());

		GTProxy engine = new GTProxy();

		String fl = rootMegadiff.getAbsolutePath()
				+ "/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_t.java";

		Tree tl = null;
		Tree tr = null;
		SpoonTreeBuilder builder = new SpoonTreeBuilder();

		tl = builder.build(new File(fl));
		tr = builder.build(new File(fr));

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		SimplifiedChawatheScriptGenerator edGenerator = new SimplifiedChawatheScriptGenerator();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		System.out.println(actionsAll);
		assertEquals(1, actionsAll.size());

		assertTrue(actionsAll.get(0).getNode().getMetadata("spoon_object") instanceof CtReturn);

	}

	@Test
	public void testChangesSpoon_3_04f0e8() throws Exception {

		assertTrue(rootMegadiff.exists());

		GTProxy engine = new GTProxy();

		String fl = rootMegadiff.getAbsolutePath()
				+ "/3_04f0e8f7a3545cf877c10967396b06595d57c34a/JavaExtensions/3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/3_04f0e8f7a3545cf877c10967396b06595d57c34a/JavaExtensions/3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions_t.java";

		Tree tl = null;
		Tree tr = null;
		SpoonTreeBuilder builder = new SpoonTreeBuilder();

		tl = builder.build(new File(fl));
		tr = builder.build(new File(fr));

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();
		SimplifiedChawatheScriptGenerator edGenerator = new SimplifiedChawatheScriptGenerator();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		System.out.println(actionsAll);
		assertEquals(4, actionsAll.size());

	}

	@Test
	public void testChangesSpoon_3_04f0e8_CompleteMatcher_VARIABLE_TYPE() throws Exception {

		assertTrue(rootMegadiff.exists());

		GTProxy engine = new GTProxy();

		String fl = rootMegadiff.getAbsolutePath()
				+ "/3_04f0e8f7a3545cf877c10967396b06595d57c34a/JavaExtensions/3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/3_04f0e8f7a3545cf877c10967396b06595d57c34a/JavaExtensions/3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions_t.java";

		Tree tl = null;
		Tree tr = null;
		SpoonTreeBuilder builder = new SpoonTreeBuilder();

		tl = builder.build(new File(fl));
		tr = builder.build(new File(fr));

		CompositeMatcher matcher = new CompositeMatchers.ClassicGumtree();
		SimplifiedChawatheScriptGenerator edGenerator = new SimplifiedChawatheScriptGenerator();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		System.out.println(actionsAll);
		assertEquals(4, actionsAll.size());

	}

	@Test
	public void testChangesJDT_3_04f0e8_CompleteMatcher_VARIABLE_TYPE() throws Exception {

		assertTrue(rootMegadiff.exists());

		GTProxy engine = new GTProxy();

		String fl = rootMegadiff.getAbsolutePath()
				+ "/3_04f0e8f7a3545cf877c10967396b06595d57c34a/JavaExtensions/3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/3_04f0e8f7a3545cf877c10967396b06595d57c34a/JavaExtensions/3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions_t.java";

		Tree tl = null;
		Tree tr = null;
		JDTTreeBuilder builder = new JDTTreeBuilder();

		tl = builder.build(new File(fl));
		tr = builder.build(new File(fr));

		CompositeMatcher matcher = new CompositeMatchers.ClassicGumtree();
		SimplifiedChawatheScriptGenerator edGenerator = new SimplifiedChawatheScriptGenerator();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		System.out.println(actionsAll);
		assertEquals(4, actionsAll.size());

	}

	@Test
	public void testChangesSpoon_3_04f0e8_CompleteMatcherBis() throws Exception {

		assertTrue(rootMegadiff.exists());

		GTProxy engine = new GTProxy();

		String fl = rootMegadiff.getAbsolutePath()
				+ "/3_04f0e8f7a3545cf877c10967396b06595d57c34a/JavaExtensions2/3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/3_04f0e8f7a3545cf877c10967396b06595d57c34a/JavaExtensions2/3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions_t.java";

		Tree tl = null;
		Tree tr = null;
		SpoonTreeBuilder builder = new SpoonTreeBuilder();

		tl = builder.build(new File(fl));
		tr = builder.build(new File(fr));

		CompositeMatcher matcher = new CompositeMatchers.ClassicGumtree();
		SimplifiedChawatheScriptGenerator edGenerator = new SimplifiedChawatheScriptGenerator();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		System.out.println(actionsAll);
		assertEquals(2, actionsAll.size());

	}

	@Test
	public void testChangesSpoon_Simple1() throws Exception {

		assertTrue(rootMegadiff.exists());

		GTProxy engine = new GTProxy();

		String fl = "./examples/java_simple_case_1/T1_s.java";
		String fr = "./examples/java_simple_case_1/T1_t.java";

		Tree tl = null;
		Tree tr = null;
		SpoonTreeBuilder builder = new SpoonTreeBuilder();

		tl = builder.build(new File(fl));
		tr = builder.build(new File(fr));

		CompositeMatcher matcher = new CompositeMatchers.ClassicGumtree();
		SimplifiedChawatheScriptGenerator edGenerator = new SimplifiedChawatheScriptGenerator();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		System.out.println(actionsAll);
		assertEquals(1, actionsAll.size());

	}

	@Test
	public void testChangesSpoon_Simple_modifier2() throws Exception {

		assertTrue(rootMegadiff.exists());

		GTProxy engine = new GTProxy();

		String fl = "./examples/java_simple_case_modifier_2/T1_s.java";
		String fr = "./examples/java_simple_case_modifier_2/T1_t.java";

		Tree tl = null;
		Tree tr = null;
		SpoonTreeBuilder builder = new SpoonTreeBuilder();

		tl = builder.build(new File(fl));
		tr = builder.build(new File(fr));

		CompositeMatcher matcher = new CompositeMatchers.ClassicGumtree();
		SimplifiedChawatheScriptGenerator edGenerator = new SimplifiedChawatheScriptGenerator();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		System.out.println(actionsAll);
		assertEquals(2, actionsAll.size());

	}

	@Test
	public void testChangesJDT_1_02f3() throws Exception {

		assertTrue(rootMegadiff.exists());

		GTProxy engine = new GTProxy();

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

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		System.out.println(actionsAll);
		assertEquals(1, actionsAll.size());

		assertTrue(actionsAll.get(0).getNode().getType().name.equals("MarkerAnnotation"));

	}

	@Test
	public void testChangesSpoon_1_02f3() throws Exception {

		assertTrue(rootMegadiff.exists());

		GTProxy engine = new GTProxy();

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
		SimplifiedChawatheScriptGenerator edGenerator = new SimplifiedChawatheScriptGenerator();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		System.out.println(actionsAll);
		assertEquals(1, actionsAll.size());

		assertTrue(actionsAll.get(0).getNode().getMetadata("spoon_object") instanceof CtAnnotation);

	}

	@Test
	public void testDefaultClassicGT() throws Exception {

		CompositeMatchers.ClassicGumtree matcher = new CompositeMatchers.ClassicGumtree();

		assertTrue(matcher.matchers().get(0) instanceof GreedySubtreeMatcher);
		GreedySubtreeMatcher gms = (GreedySubtreeMatcher) matcher.matchers().get(0);
		assertEquals(1, gms.getMinPriority());

		assertTrue(matcher.matchers().get(1) instanceof GreedyBottomUpMatcher);
		GreedyBottomUpMatcher gbu = (GreedyBottomUpMatcher) matcher.matchers().get(1);
		assertEquals(1000, gbu.getSizeThreshold());

	}

	@Test
	public void testChangesSpoon_Simple_1_null_Modifiers() throws Exception {

		assertTrue(rootMegadiff.exists());

		GTProxy engine = new GTProxy();

		String fl = "./examples/simple_case_1/file1_s.java";
		String fr = "./examples/simple_case_1/file1_t.java";

		Tree tl = null;
		Tree tr = null;
		SpoonTreeBuilder builder = new SpoonTreeBuilder();

		tl = builder.build(new File(fl));
		tr = builder.build(new File(fr));

		CompositeMatcher matcher = new CompositeMatchers.ClassicGumtree();
		SimplifiedChawatheScriptGenerator edGenerator = new SimplifiedChawatheScriptGenerator();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();
		System.out.println("\nSPOON\n");
		System.out.println("\nLeft: " + tl.toTreeString());
		System.out.println("\nRight: " + tr.toTreeString());

		System.out.println(actionsAll);
		assertEquals(1, actionsAll.size());

		assertTrue(actionsAll.get(0) instanceof Insert);

	}

	@Test
	public void testChangesJDT_Simple_1_null_Modifiers() throws Exception {

		assertTrue(rootMegadiff.exists());

		GTProxy engine = new GTProxy();

		String fl = "./examples/simple_case_1/file1_s.java";
		String fr = "./examples/simple_case_1/file1_t.java";

		Tree tl = null;
		Tree tr = null;
		JDTTreeBuilder builder = new JDTTreeBuilder();

		tl = builder.build(new File(fl));
		tr = builder.build(new File(fr));

		System.out.println("\nJDT\n");
		System.out.println("\nLeft: " + tl.toTreeString());
		System.out.println("\nRight: " + tr.toTreeString());

		CompositeMatcher matcher = new CompositeMatchers.ClassicGumtree();
		SimplifiedChawatheScriptGenerator edGenerator = new SimplifiedChawatheScriptGenerator();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		System.out.println(actionsAll);
		assertEquals(1, actionsAll.size());

		assertTrue(actionsAll.get(0) instanceof Insert);

	}

	@Test
	public void testChangesSpoon_1_195a92e5ca85aab0fcf063ccd9391d814f05fbc8_AtmosphereResponse_s() throws Exception {

		assertTrue(rootMegadiff.exists());

		GTProxy engine = new GTProxy();

		String fl = rootMegadiff.getAbsolutePath()
				+ "/1_195a92e5ca85aab0fcf063ccd9391d814f05fbc8/AtmosphereResponse/1_195a92e5ca85aab0fcf063ccd9391d814f05fbc8_AtmosphereResponse_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/1_195a92e5ca85aab0fcf063ccd9391d814f05fbc8/AtmosphereResponse/1_195a92e5ca85aab0fcf063ccd9391d814f05fbc8_AtmosphereResponse_t.java";

		Tree tl = null;
		Tree tr = null;
		ITreeBuilder builder = new JDTTreeBuilder();// new SpoonTreeBuilder();

		tl = builder.build(new File(fl));
		tr = builder.build(new File(fr));

		CompositeMatcher matcher = new CompositeMatchers.ClassicGumtree();
		SimplifiedChawatheScriptGenerator edGenerator = new SimplifiedChawatheScriptGenerator();

		List<Action> actionsRoot = engine.computeDiff(tl, tr, matcher, edGenerator, new GumtreeProperties()).editScript
				.asList();

		System.out.println("nr Root " + actionsRoot.size());// assertEquals(4, actionsRoot.size());

		System.out.println("Root: " + actionsRoot);// assertEquals(4, actionsRoot.size());

		ChawatheScriptGenerator edGeneratorNormal = new ChawatheScriptGenerator();

		List<Action> actionsAll = engine.computeDiff(tl, tr, matcher, edGeneratorNormal,
				new GumtreeProperties()).editScript.asList();

		System.out.println("nr All " + actionsAll.size());

		System.out.println("All " + actionsAll);

		GumtreeProperties properies = new GumtreeProperties();
		properies = new GumtreeProperties();
		properies.put(ConfigurationOptions.bu_minsim, 0.1);// bu_minsim
		properies.put(ConfigurationOptions.bu_minsize, 1000);// bu_minsize
		properies.put(ConfigurationOptions.st_minprio, 1);

		actionsRoot = engine.computeDiff(tl, tr, matcher, edGenerator, properies).editScript.asList();

		System.out.println("Root " + actionsRoot.size());// assertEquals(4, actionsRoot.size());

		edGeneratorNormal = new ChawatheScriptGenerator();

		actionsAll = engine.computeDiff(tl, tr, matcher, edGeneratorNormal, properies).editScript.asList();

		System.out.println("All " + actionsAll.size());

		/// ----------

		properies = new GumtreeProperties();
		properies.put(ConfigurationOptions.bu_minsim, 1);// bu_minsim
		properies.put(ConfigurationOptions.bu_minsize, 1000);// bu_minsize
		properies.put(ConfigurationOptions.st_minprio, 1);

		actionsRoot = engine.computeDiff(tl, tr, matcher, edGenerator, properies).editScript.asList();

		System.out.println("Root " + actionsRoot.size());// assertEquals(4, actionsRoot.size());

		edGeneratorNormal = new ChawatheScriptGenerator();

		actionsAll = engine.computeDiff(tl, tr, matcher, edGeneratorNormal, properies).editScript.asList();

		System.out.println("All " + actionsAll.size());

		//// ClassicGumtree 11 5 7 4 0 0 0 0 12 3 0 0.2 500 2

		properies = new GumtreeProperties();
		properies.put(ConfigurationOptions.bu_minsim, 0.1);// bu_minsim
		properies.put(ConfigurationOptions.bu_minsize, 500);// bu_minsize
		properies.put(ConfigurationOptions.st_minprio, 2);

		actionsRoot = engine.computeDiff(tl, tr, matcher, edGenerator, properies).editScript.asList();

		System.out.println("Root " + actionsRoot.size());// assertEquals(4, actionsRoot.size());

		edGeneratorNormal = new ChawatheScriptGenerator();

		actionsAll = engine.computeDiff(tl, tr, matcher, edGeneratorNormal, properies).editScript.asList();

		System.out.println("All " + actionsAll.size());

		///

		properies = new GumtreeProperties();
		properies.put(ConfigurationOptions.bu_minsim, 1);// bu_minsim
		properies.put(ConfigurationOptions.bu_minsize, 400);// bu_minsize
		properies.put(ConfigurationOptions.st_minprio, 2);

		actionsRoot = engine.computeDiff(tl, tr, matcher, edGenerator, properies).editScript.asList();

		System.out.println("Root " + actionsRoot.size());// assertEquals(4, actionsRoot.size());

		edGeneratorNormal = new ChawatheScriptGenerator();

		actionsAll = engine.computeDiff(tl, tr, matcher, edGeneratorNormal, properies).editScript.asList();

		System.out.println("All " + actionsAll.size());

	}

}
