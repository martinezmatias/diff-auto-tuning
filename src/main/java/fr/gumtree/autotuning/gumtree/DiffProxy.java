package fr.gumtree.autotuning.gumtree;

import com.github.gumtreediff.matchers.GumtreeProperties;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.Tree;

import fr.gumtree.autotuning.entity.SingleDiffResult;

/**
 * Interface for running an algorithm
 * 
 * @author Matias Martinez
 *
 */
public interface DiffProxy {

	public SingleDiffResult runDiff(Tree tl, Tree tr, Matcher matcher, GumtreeProperties aGumtreeProperties);
}
