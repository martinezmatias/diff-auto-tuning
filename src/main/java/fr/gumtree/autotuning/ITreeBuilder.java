package fr.gumtree.autotuning;

import java.io.File;

import com.github.gumtreediff.tree.Tree;

import fr.gumtree.autotuning.TuningEngine.ASTMODE;

public interface ITreeBuilder {

	public Tree build(File file) throws Exception;

	public ASTMODE modelType();

}
