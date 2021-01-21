package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

import org.junit.Test;

import com.github.gumtreediff.actions.ChawatheScriptGenerator;
import com.github.gumtreediff.actions.model.Action;
import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumTreeProperties;
import com.github.gumtreediff.tree.ITree;

import gumtree.spoon.AstComparator;
import gumtree.spoon.builder.SpoonGumTreeBuilder;

public class DiffTest {

	private SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();
	private AstComparator diff = new AstComparator();

	final File rootMegadiff = new File("./examples/");

	@Test
	public void testChangeInAnnotationSpoonGranularity() throws Exception {

		assertTrue(rootMegadiff.exists());

		TuningEngine engine = new TuningEngine();

		String fl = rootMegadiff.getAbsolutePath()
				+ "/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_t.java";

		ITree tl = null;
		ITree tr = null;

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
		ITree tl = null;
		ITree tr = null;

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
		ITree tl = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

		lc = new String(Files.readAllBytes(new File(fr).toPath()));
		ITree tr = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

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

}
