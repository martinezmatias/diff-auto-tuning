package fr.gumtree.autotuning.entity;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.github.gumtreediff.actions.Diff;
import com.github.gumtreediff.matchers.GumtreeProperties;

import fr.gumtree.autotuning.gumtree.GTProxy;
import fr.gumtree.autotuning.outils.Constants;

/**
 * 
 * @author Matias Martinez
 *
 */
public class SingleDiffResult extends HashMap<String, Object> {

	private static final String PLAIN_CONFIG_REDUCED = Constants.PLAIN_CONFIGURATION + "_h";

	/**
	 * The diff related to this result
	 */
	Diff diff;

	static List<String> propertiesToIgnoreInHash;
	static {

		propertiesToIgnoreInHash = Arrays.asList("bu_minsize");

	}

	public SingleDiffResult(String matcher) {
		this.put(Constants.MATCHER, matcher);
	}

	private static final long serialVersionUID = 1L;

	public Diff getDiff() {
		return diff;
	}

	public void setDiff(Diff actions) {
		this.diff = actions;
	}

	public String retrievePlainConfiguration() {

		if (!this.containsKey(Constants.PLAIN_CONFIGURATION)) {

			GumtreeProperties gt = (GumtreeProperties) this.get(Constants.CONFIG);

			Object matcher = this.get(Constants.MATCHER);

			String plainProperty = GTProxy.plainProperties(matcher.toString(), gt);

			this.put(Constants.PLAIN_CONFIGURATION, plainProperty);

			// let's compute the key without the bu

			String plainPropertyHash = computeReduced(matcher, gt);

			this.put(PLAIN_CONFIG_REDUCED, plainPropertyHash);

			return plainProperty;

		}

		return this.get(Constants.PLAIN_CONFIGURATION).toString();

	}

	public String retrievePlainConfigurationReduced() {

		if (!this.containsKey(PLAIN_CONFIG_REDUCED)) {

			GumtreeProperties gt = (GumtreeProperties) this.get(Constants.CONFIG);

			Object matcher = this.get(Constants.MATCHER);

			String plainProperty = GTProxy.plainProperties(matcher.toString(), gt);

			this.put(Constants.PLAIN_CONFIGURATION, plainProperty);

			// let's compute the key without the bu

			String plainPropertyHash = computeReduced(matcher, gt);

			this.put(PLAIN_CONFIG_REDUCED, plainPropertyHash);

			return plainPropertyHash;

		}

		return this.get(PLAIN_CONFIG_REDUCED).toString();

	}

	public static String computeReduced(Object matcher, GumtreeProperties gt) {
		String plainPropertyHash = GTProxy.plainProperties(matcher.toString(), propertiesToIgnoreInHash, gt);
		return plainPropertyHash;
	}

}
