package fr.gumtree.autotuning.gumtree;

import com.github.gumtreediff.matchers.GumtreeProperties;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.Tree;

import fr.gumtree.autotuning.entity.SingleDiffResult;

public class DummyDiffProxy implements DiffProxy {

	@Override
	public SingleDiffResult runDiff(Tree tl, Tree tr, Matcher matcher, GumtreeProperties aGumtreeProperties) {
		SingleDiffResult resultDiff = new SingleDiffResult(matcher.getClass().getSimpleName());
		return null;
	}

}
