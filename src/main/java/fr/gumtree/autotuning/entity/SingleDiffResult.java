package fr.gumtree.autotuning.entity;

import java.util.HashMap;

import com.github.gumtreediff.actions.Diff;
import com.github.gumtreediff.matchers.GumtreeProperties;
import com.google.gson.JsonObject;

import fr.gumtree.autotuning.gumtree.GTProxy;
import fr.gumtree.autotuning.outils.Constants;

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

	public String retrievePlainConfiguration() {

		if (!this.containsKey(Constants.PLAIN_CONFIGURATION)) {

			GumtreeProperties gt = (GumtreeProperties) this.get(Constants.CONFIG);
			String plainProperty = GTProxy.plainProperties(new JsonObject(), this.get(Constants.MATCHER).toString(),
					gt);
			this.put(Constants.PLAIN_CONFIGURATION, plainProperty);
			return plainProperty;
		}

		return this.get(Constants.PLAIN_CONFIGURATION).toString();

	}

}
