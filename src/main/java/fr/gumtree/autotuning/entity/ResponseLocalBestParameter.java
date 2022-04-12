package fr.gumtree.autotuning.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ResponseLocalBestParameter extends ResponseBestParameter {

	// For each file, results of best vs another default
	// store best length and default (or another)

	// store the most frequent

	private Map<String, Integer> countBestByConfigurations = new HashMap<String, Integer>();

	public Map<String, Integer> getCountBestByConfigurations() {
		return countBestByConfigurations;
	}

	public void setCountBestByConfigurations(Map<String, Integer> countBestByConfigurations) {
		this.countBestByConfigurations = countBestByConfigurations;
	}

}
