package fr.gumtree.autotuning.treebuilder;

import java.io.File;
import java.nio.file.Files;

import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.tree.Tree;

import fr.gumtree.autotuning.ExhaustiveEngine.ASTMODE;

public class JDTTreeBuilder implements ITreeBuilder {

	@Override
	public Tree build(File file) throws Exception {
		String lc = new String(Files.readAllBytes(file.toPath()));
		return new JdtTreeGenerator().generateFrom().string(lc).getRoot();

	}

	@Override
	public ASTMODE modelType() {
		return ASTMODE.JDT;
	}

}
