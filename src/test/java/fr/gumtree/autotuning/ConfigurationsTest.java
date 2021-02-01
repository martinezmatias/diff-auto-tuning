package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumTreeProperties;

import fr.gumtree.autotuning.domain.DoubleParameterDomain;
import fr.gumtree.autotuning.domain.IntParameterDomain;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ConfigurationsTest {

	@Test
	public void testDomain() {

		IntParameterDomain intdomSz = new IntParameterDomain(ConfigurationOptions.bu_minsize, Integer.class, 1000, 100,
				2000, 100);

		Integer[] intervalInt = intdomSz.computeInterval();

		System.out.println(Arrays.toString(intervalInt));
		assertEquals(20, intervalInt.length);

		DoubleParameterDomain doubleDomainSMT = new DoubleParameterDomain(ConfigurationOptions.bu_minsim, Double.class,
				0.5, 0.1, 1.0, 0.1);

		Double[] intervalD = doubleDomainSMT.computeInterval();
		System.out.println(Arrays.toString(intervalD));
		assertEquals(10, intervalD.length);

		List<ParameterDomain> domains = new ArrayList<>();

		IntParameterDomain intdomMH = new IntParameterDomain(ConfigurationOptions.st_minprio, Integer.class, 2, 1, 5,
				1);

		Integer[] intervalIntMH = intdomMH.computeInterval();

		System.out.println(Arrays.toString(intervalIntMH));
		assertEquals(5, intervalIntMH.length);

		// Add all domains
		domains.add(intdomSz);
		domains.add(doubleDomainSMT);
		domains.add(intdomMH);

		// Cartesian

		TuningEngine engine = new TuningEngine();

		List<GumTreeProperties> combinations = engine.computeCartesianProduct(domains);

		int expectedCombinations = intervalInt.length * intervalD.length * intervalIntMH.length;

		assertEquals(20 * 10 * 5, expectedCombinations);

		assertEquals(expectedCombinations, combinations.size());

		System.out.println("Combinations " + combinations.size());
		int i = 0;
		for (GumTreeProperties gumTreeProperties : combinations) {
			System.out.println("Combination " + ++i + ": " + gumTreeProperties.get(ConfigurationOptions.bu_minsim) + " "
					+ gumTreeProperties.get(ConfigurationOptions.bu_minsize) + " "
					+ gumTreeProperties.get(ConfigurationOptions.st_minprio));

		}

		IntParameterDomain intAnother = new IntParameterDomain(ConfigurationOptions.st_minprio, Integer.class, 2,
				new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8 });

		assertEquals(8, intAnother.computeInterval().length);

	}

	@Test
	public void testCategoricalTest() {
		ParameterDomain<String> option = (ParameterDomain<String>) ParametersResolvers.parametersDomain
				.get(ConfigurationOptions.st_priocalc);

		assertEquals(2, option.interval.length);
		assertEquals("size", option.interval[0]);
		assertEquals("height", option.interval[1]);
		String[] result = option.computeInterval();

		assertEquals(2, result.length);
		assertEquals("size", result[0]);
		assertEquals("height", result[1]);
	}
}
