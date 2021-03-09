package fr.gumtree.autotuning.domain;

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
