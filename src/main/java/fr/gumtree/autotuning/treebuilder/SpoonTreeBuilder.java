package fr.gumtree.autotuning.treebuilder;

import java.io.File;

import com.github.gumtreediff.tree.ITree;

import fr.gumtree.autotuning.ITreeBuilder;
import fr.gumtree.autotuning.TuningEngine.ASTMODE;
import gumtree.spoon.AstComparator;
import gumtree.spoon.builder.SpoonGumTreeBuilder;

public class SpoonTreeBuilder implements ITreeBuilder {

	private AstComparator diff = new AstComparator();
	private SpoonGumTreeBuilder scanner = new SpoonGumTreeBuilder();

	@Override
	public ITree build(File file) throws Exception {

		return scanner.getTree(diff.getCtType(file));
	}

	@Override
	public ASTMODE modelType() {
		return ASTMODE.GTSPOON;
	}

}
