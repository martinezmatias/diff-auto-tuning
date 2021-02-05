package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumtreeProperties;

import fr.gumtree.autotuning.domain.CategoricalParameterDomain;
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

		IntParameterDomain intdomSz = new IntParameterDomain(ConfigurationOptions.bu_minsize.name(), Integer.class,
				1000, 100, 2000, 100);

		Integer[] intervalInt = intdomSz.computeInterval();

		System.out.println(Arrays.toString(intervalInt));
		assertEquals(20, intervalInt.length);

		DoubleParameterDomain doubleDomainSMT = new DoubleParameterDomain(ConfigurationOptions.bu_minsim.name(),
				Double.class, 0.5, 0.1, 1.0, 0.1);

		Double[] intervalD = doubleDomainSMT.computeInterval();
		System.out.println(Arrays.toString(intervalD));
		assertEquals(10, intervalD.length);

		List<ParameterDomain> domains = new ArrayList<>();

		IntParameterDomain intdomMH = new IntParameterDomain(ConfigurationOptions.st_minprio.name(), Integer.class, 2,
				1, 5, 1);

		Integer[] intervalIntMH = intdomMH.computeInterval();

		System.out.println(Arrays.toString(intervalIntMH));
		assertEquals(5, intervalIntMH.length);

		// Add all domains
		domains.add(intdomSz);
		domains.add(doubleDomainSMT);
		domains.add(intdomMH);

		// Cartesian

		TuningEngine engine = new TuningEngine();

		List<GumtreeProperties> combinations = engine.computeCartesianProduct(domains);

		int expectedCombinations = intervalInt.length * intervalD.length * intervalIntMH.length;

		assertEquals(20 * 10 * 5, expectedCombinations);

		assertEquals(expectedCombinations, combinations.size());

		System.out.println("Combinations " + combinations.size());
		int i = 0;
		for (GumtreeProperties GumtreeProperties : combinations) {
			System.out.println("Combination " + ++i + ": " + GumtreeProperties.get(ConfigurationOptions.bu_minsim) + " "
					+ GumtreeProperties.get(ConfigurationOptions.bu_minsize) + " "
					+ GumtreeProperties.get(ConfigurationOptions.st_minprio));

		}

		IntParameterDomain intAnother = new IntParameterDomain(ConfigurationOptions.st_minprio.name(), Integer.class, 2,
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

	@Test
	public void testCategoricalCombinationTest() {
		ParameterDomain<String> categoricalDomain = new CategoricalParameterDomain(
				ConfigurationOptions.cd_labsim.toString(), String.class, "one", new String[] { "one", "two", "many" });

		assertEquals(3, categoricalDomain.interval.length);
		assertEquals("one", categoricalDomain.interval[0]);
		assertEquals("two", categoricalDomain.interval[1]);
		assertEquals("many", categoricalDomain.interval[2]);
		String[] result = categoricalDomain.computeInterval();

		assertEquals(3, result.length);
		assertEquals("one", result[0]);
		assertEquals("two", result[1]);
		assertEquals("many", result[2]);

		DoubleParameterDomain doubleDomainSMT = new DoubleParameterDomain(ConfigurationOptions.bu_minsim.toString(),
				Double.class, 0.5, 0.1, 1.0, 0.1);

		Double[] intervalD = doubleDomainSMT.computeInterval();
		assertEquals(10, intervalD.length);

		List<ParameterDomain> domains = new ArrayList<>();

		domains.add(doubleDomainSMT);
		domains.add(categoricalDomain);

		TuningEngine engine = new TuningEngine();
		List<GumtreeProperties> combinations = engine.computeCartesianProduct(domains);

		assertEquals(30, combinations.size());
		// First element
		assertEquals("one", combinations.get(0).get(ConfigurationOptions.cd_labsim));
		assertEquals(0.1, combinations.get(0).get(ConfigurationOptions.bu_minsim));

		// Second element
		assertEquals("two", combinations.get(1).get(ConfigurationOptions.cd_labsim));
		assertEquals(0.1, combinations.get(1).get(ConfigurationOptions.bu_minsim));

		//
		assertEquals(0.2, combinations.get(4).get(ConfigurationOptions.bu_minsim));

		// last one
		assertEquals("many", combinations.get(29).get(ConfigurationOptions.cd_labsim));
		assertEquals(1d, (double) combinations.get(29).get(ConfigurationOptions.bu_minsim), 0.01);

	}
}
