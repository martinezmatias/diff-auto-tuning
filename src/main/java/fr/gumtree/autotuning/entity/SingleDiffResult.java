package fr.gumtree.autotuning.entity;

import java.util.HashMap;
import java.util.List;

import com.github.gumtreediff.actions.model.Action;

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

	List<Action> actions;

	public List<Action> getActions() {
		return actions;
	}

	public void setActions(List<Action> actions) {
		this.actions = actions;
	}

}
