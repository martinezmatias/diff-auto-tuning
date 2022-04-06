package fr.gumtree.autotuning.gumtree;

import java.util.HashMap;
import java.util.Map;

import com.github.gumtreediff.matchers.ConfigurationOptions;

import fr.gumtree.autotuning.domain.CategoricalParameterDomain;
import fr.gumtree.autotuning.domain.DoubleParameterDomain;
import fr.gumtree.autotuning.domain.IntParameterDomain;
import fr.gumtree.autotuning.domain.ParameterDomain;

public class ParametersResolvers {

	protected Map<ConfigurationOptions, ParameterDomain<?>> parametersDomain = new HashMap<>();

	public static ParametersResolvers defaultDomain = new ParametersResolvers();

	static {

		defaultDomain.parametersDomain.put(ConfigurationOptions.bu_minsize,
				new IntParameterDomain(ConfigurationOptions.bu_minsize.name(), Integer.class, 1000, 100, 2000, 100));

		defaultDomain.parametersDomain.put(ConfigurationOptions.bu_minsim,
				new DoubleParameterDomain(ConfigurationOptions.bu_minsim.name(), Double.class, 0.5, 0.1, 1.0, 0.1));

		defaultDomain.parametersDomain.put(ConfigurationOptions.st_minprio,
				new IntParameterDomain(ConfigurationOptions.st_minprio.name(), Integer.class, 2, 1, 5, 1));

		defaultDomain.parametersDomain.put(ConfigurationOptions.st_priocalc, new CategoricalParameterDomain(
				ConfigurationOptions.st_priocalc.name(), String.class, "height", new String[] { "size", "height" }));

	}

	public Map<ConfigurationOptions, ParameterDomain<?>> getParametersDomain() {
		return parametersDomain;
	}

}
