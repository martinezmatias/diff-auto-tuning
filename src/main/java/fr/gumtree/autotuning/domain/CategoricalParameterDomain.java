package fr.gumtree.autotuning.domain;

import fr.gumtree.autotuning.ParameterDomain;

/**
 * 
 * @author Matias Martinez
 *
 */
public class CategoricalParameterDomain extends ParameterDomain<String> {

	public CategoricalParameterDomain(String id, Class type, String defaultValue, String[] interval) {
		super(id, type, defaultValue, interval);
	}

}
