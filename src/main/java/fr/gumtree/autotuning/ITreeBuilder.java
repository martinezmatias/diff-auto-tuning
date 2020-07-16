package fr.gumtree.autotuning;

import java.io.File;

import com.github.gumtreediff.tree.ITree;

import fr.gumtree.autotuning.TuningEngine.ASTMODE;

public interface ITreeBuilder {

	public ITree build(File file) throws Exception;

	public ASTMODE modelType();

}
