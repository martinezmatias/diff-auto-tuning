package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.List;

import org.junit.Test;

import com.github.gumtreediff.actions.ChawatheScriptGenerator;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumTreeProperties;
import com.github.gumtreediff.tree.ITree;
import com.github.gumtreediff.tree.TreeContext;

import gumtree.spoon.AstComparator;
import gumtree.spoon.builder.SpoonGumTreeBuilder;
import gumtree.spoon.diff.Diff;
import gumtree.spoon.diff.DiffImpl;
import gumtree.spoon.diff.operations.Operation;

public class DiffTest {

	private SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();
	private AstComparator diff = new AstComparator();

	@Test
	public void testChangeInAnnotation() throws Exception {

		File rootMegadiff = new File(
				"/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded");
		assertTrue(rootMegadiff.exists());

		String fl = "/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded/1/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_s.java";
		String fr = "/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded/1/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_t.java";

		ITree tl = null;
		ITree tr = null;

		tl = scanner.getTree(diff.getCtType(new File(fl)));
		tr = scanner.getTree(diff.getCtType(new File(fr)));

		Diff result = null;

		result = new DiffImpl(new TreeContext()// scanner.getTreeContext()
				, tl, tr); // new ChawatheScriptGenerator(), new CompositeMatchers.XyMatcher(), new
							// GumTreeProperties());

		List<Operation> actionsAll = result.getAllOperations();

		assertEquals(1, actionsAll.size());

		// XY has variability
		GumTreeProperties properies = new GumTreeProperties();

		properies.put(ConfigurationOptions.GT_STM_MH, 1);
		properies.put(ConfigurationOptions.GT_XYM_SIM, 0.2);

		result = new DiffImpl(new TreeContext()// scanner.getTreeContext()
				, tl, tr, new ChawatheScriptGenerator(), new CompositeMatchers.XyMatcher(), properies);

		actionsAll = result.getAllOperations();

		assertEquals(1, actionsAll.size());

		properies.put(ConfigurationOptions.GT_STM_MH, 3);
		properies.put(ConfigurationOptions.GT_XYM_SIM, 0.2);

		result = new DiffImpl(new TreeContext()// scanner.getTreeContext()
				, tl, tr, new ChawatheScriptGenerator(), new CompositeMatchers.XyMatcher(), properies);

		actionsAll = result.getAllOperations();

		assertEquals(55, actionsAll.size());

		//
		properies.put(ConfigurationOptions.GT_STM_MH, 2);
		properies.put(ConfigurationOptions.GT_XYM_SIM, 0.6);

		result = new DiffImpl(new TreeContext()// scanner.getTreeContext()
				, tl, tr, new ChawatheScriptGenerator(), new CompositeMatchers.XyMatcher(), properies);

		actionsAll = result.getAllOperations();

		assertEquals(19, actionsAll.size());

		// Now Classic
		properies.getProperties().clear();
		properies.put(ConfigurationOptions.GT_BUM_SMT, 0.1);
		properies.put(ConfigurationOptions.GT_BUM_SZT, 100);
		properies.put(ConfigurationOptions.GT_STM_MH, 1);
		result = new DiffImpl(new TreeContext(), tl, tr, new ChawatheScriptGenerator(),
				new CompositeMatchers.ClassicGumtree(), properies);

		actionsAll = result.getAllOperations();

		assertEquals(1, actionsAll.size());

		//
		properies.getProperties().clear();
		properies.put(ConfigurationOptions.GT_BUM_SMT, 0.2);
		properies.put(ConfigurationOptions.GT_BUM_SZT, 600);
		properies.put(ConfigurationOptions.GT_STM_MH, 5);
		result = new DiffImpl(new TreeContext(), tl, tr, new ChawatheScriptGenerator(),
				new CompositeMatchers.ClassicGumtree(), properies);

		actionsAll = result.getAllOperations();

		assertEquals(1, actionsAll.size());

		/// Forcing to fail
		properies.getProperties().clear();
		properies.put(ConfigurationOptions.GT_BUM_SMT, 0);
		properies.put(ConfigurationOptions.GT_BUM_SZT, 0);
		properies.put(ConfigurationOptions.GT_STM_MH, 299990); // fake value
		result = new DiffImpl(new TreeContext(), tl, tr, new ChawatheScriptGenerator(),
				new CompositeMatchers.ClassicGumtree(), properies);

		actionsAll = result.getAllOperations();

		assertNotEquals(1, actionsAll.size());

	}

}
