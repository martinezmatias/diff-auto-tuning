package fr.gumtree.autotuning.gumtree;

import java.util.HashMap;
import java.util.Map;

import com.github.gumtreediff.matchers.ConfigurationOptions;

import fr.gumtree.autotuning.domain.CategoricalParameterDomain;
import fr.gumtree.autotuning.domain.DoubleParameterDomain;
import fr.gumtree.autotuning.domain.IntParameterDomain;
import fr.gumtree.autotuning.domain.ParameterDomain;

public class ParametersResolvers {

	public static Map<ConfigurationOptions, ParameterDomain<?>> parametersDomain = new HashMap<>();

	static {

		parametersDomain.put(ConfigurationOptions.bu_minsize,
				new IntParameterDomain(ConfigurationOptions.bu_minsize.name(), Integer.class, 1000, 100, 2000, 100));

		parametersDomain.put(ConfigurationOptions.bu_minsim,
				new DoubleParameterDomain(ConfigurationOptions.bu_minsim.name(), Double.class, 0.5, 0.1, 1.0, 0.1));

		parametersDomain.put(ConfigurationOptions.st_minprio,
				new IntParameterDomain(ConfigurationOptions.st_minprio.name(), Integer.class, 2, 1, 5, 1));

		parametersDomain.put(ConfigurationOptions.st_priocalc, new CategoricalParameterDomain(
				ConfigurationOptions.st_priocalc.name(), String.class, "height", new String[] { "size", "height" }));

		parametersDomain.put(ConfigurationOptions.xy_minsim,
				new DoubleParameterDomain(ConfigurationOptions.xy_minsim.name(), Double.class, 0.5, 0.1, 1.0, 0.1));

		parametersDomain.put(ConfigurationOptions.cd_labsim,
				new DoubleParameterDomain(ConfigurationOptions.cd_labsim.name(), Double.class, 0.5, 0.1, 1.0, 0.2));

		parametersDomain.put(ConfigurationOptions.cd_structsim1,
				new DoubleParameterDomain(ConfigurationOptions.cd_structsim1.name(), Double.class, 0.6, 0.2, 1.0, 0.2));

		parametersDomain.put(ConfigurationOptions.cd_structsim2,
				new DoubleParameterDomain(ConfigurationOptions.cd_structsim2.name(), Double.class, 0.4, 0.2, 1.0, 0.2));

		parametersDomain.put(ConfigurationOptions.cd_maxleaves,
				new IntParameterDomain(ConfigurationOptions.cd_maxleaves.name(), Integer.class, 4, 2, 6, 2));

	}

}
