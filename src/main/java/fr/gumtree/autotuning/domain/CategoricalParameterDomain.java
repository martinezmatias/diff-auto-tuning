package fr.gumtree.autotuning.domain;

import com.github.gumtreediff.matchers.ConfigurationOptions;

import fr.gumtree.autotuning.ParameterDomain;

/**
 * 
 * @author Matias Martinez
 *
 */
public class CategoricalParameterDomain extends ParameterDomain<String> {

	public CategoricalParameterDomain(ConfigurationOptions id, Class type, String defaultValue, String[] interval) {
		super(id, type, defaultValue, interval);
	}

}
