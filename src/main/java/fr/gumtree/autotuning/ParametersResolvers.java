package fr.gumtree.autotuning;

import java.util.HashMap;
import java.util.Map;

import com.github.gumtreediff.matchers.ConfigurationOptions;

public class ParametersResolvers {

	public static Map<ConfigurationOptions, ParameterDomain<?>> parametersDomain = new HashMap<>();

	static {

		parametersDomain.put(ConfigurationOptions.GT_BUM_SZT,
				new IntParameterDomain(ConfigurationOptions.GT_BUM_SZT, Integer.class, 1000, 100, 2000, 100));

		parametersDomain.put(ConfigurationOptions.GT_BUM_SMT,
				new DoubleParameterDomain(ConfigurationOptions.GT_BUM_SMT, Double.class, 0.5, 0.1, 1.0, 0.1));

		parametersDomain.put(ConfigurationOptions.GT_STM_MH,
				new IntParameterDomain(ConfigurationOptions.GT_STM_MH, Integer.class, 2, 1, 5, 1));

		parametersDomain.put(ConfigurationOptions.GT_BUM_SMT_SBUP,
				new DoubleParameterDomain(ConfigurationOptions.GT_BUM_SMT_SBUP, Double.class, 0.4, 0.1, 1.0, 0.2));

		// CD
		// CD TD
		parametersDomain.put(ConfigurationOptions.GT_CD_LSIM,
				new DoubleParameterDomain(ConfigurationOptions.GT_CD_LSIM, Double.class, 0.5, 0.1, 1.0, 0.2));

		// CD BP
		parametersDomain.put(ConfigurationOptions.GT_CD_SSIM1,
				new DoubleParameterDomain(ConfigurationOptions.GT_CD_SSIM1, Double.class, 0.6, 0.2, 1.0, 0.2));

		parametersDomain.put(ConfigurationOptions.GT_CD_SSIM2,
				new DoubleParameterDomain(ConfigurationOptions.GT_CD_SSIM2, Double.class, 0.4, 0.1, 1.0, 0.2));
		//
		parametersDomain.put(ConfigurationOptions.GT_CD_ML,
				new IntParameterDomain(ConfigurationOptions.GT_CD_ML, Integer.class, 4, 2, 6, 2));

	}

}
