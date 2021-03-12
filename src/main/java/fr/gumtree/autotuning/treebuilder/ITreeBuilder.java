package fr.gumtree.autotuning.treebuilder;

import java.io.File;

import com.github.gumtreediff.tree.Tree;

import fr.gumtree.autotuning.gumtree.ASTMODE;

public interface ITreeBuilder {

	public Tree build(File file) throws Exception;

	public ASTMODE modelType();

}
