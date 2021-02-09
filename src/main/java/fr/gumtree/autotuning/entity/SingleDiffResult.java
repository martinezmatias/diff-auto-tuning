package fr.gumtree.autotuning.entity;

import java.util.HashMap;

import com.github.gumtreediff.actions.Diff;

/**
 * 
 * @author Matias Martinez
 *
 */
public class SingleDiffResult extends HashMap<String, Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Diff diff;

	public Diff getDiff() {
		return diff;
	}

	public void setDiff(Diff actions) {
		this.diff = actions;
	}

}
