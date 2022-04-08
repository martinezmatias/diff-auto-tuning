package fr.gumtree.autotuning;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.experimentrunner.StructuredFolderfRunner;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.searchengines.ResultByConfig;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ResultProcessorTest {

	final String defaultConfiguration = "ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height";

	@Test
	public void testGlobalSpoon() throws IOException {
		File fileResults = new File("/Users/matias/develop/gt-tuning/results/resultsdatv2/outDAT2_SPOON_onlyresult/");

		StructuredFolderfRunner runner = new StructuredFolderfRunner();

		boolean ignoreTimeout = true;
		ResponseBestParameter best = runner.summarizeBestGlobal(fileResults, METRIC.MEAN, ignoreTimeout);

		inspectResults(best);
		analyzeBest(best);
	}

	@Test
	public void testGlobalJDT() throws IOException {
		File fileResults = new File("/Users/matias/develop/gt-tuning/results/resultsdatv2/outDAT2_JDT_onlyresult/");

		StructuredFolderfRunner runner = new StructuredFolderfRunner();
		boolean ignoreTimeout = false;

		ResponseBestParameter best = runner.summarizeBestGlobal(fileResults, METRIC.MEAN, ignoreTimeout);

		inspectResults(best);
		analyzeBest(best);

	}

	public void analyzeBest(ResponseBestParameter best) {

		ResultByConfig values = best.getValuesPerConfig();

		List<Double> perBest = new ArrayList<>();

		int casesImprovement = 0;
		int casesWorst = 0;

		for (String oneBestConfig : best.getAllBest()) {
			List<Integer> valuesOneBest = values.get(oneBestConfig);
			List<Integer> valuesDefault = values.get(defaultConfiguration);

			List<Integer> resultsComparison = new ArrayList<>();

			if (valuesDefault.size() == valuesOneBest.size()) {

				for (int i = 0; i < valuesDefault.size(); i++) {
					Integer iVD = valuesDefault.get(i);
					Integer iVB = valuesOneBest.get(i);

					int diff = iVB - iVD;
					resultsComparison.add(diff);
				}

				long equalsB = resultsComparison.stream().filter(e -> e == 0).count();
				long betterBest = resultsComparison.stream().filter(e -> e < 0).count();
				long worstBest = resultsComparison.stream().filter(e -> e > 0).count();

				perBest.add(((double) betterBest / (double) valuesOneBest.size()));

				if (betterBest > worstBest)
					casesImprovement++;

				if (betterBest < worstBest)
					casesWorst++;

			} else {
				System.err.println("Error different size");
			}
		}
		System.out.println(perBest);

		System.out.println("Cases improvement " + casesImprovement);
		System.out.println("Cases worst " + casesWorst);
	}

	private void inspectResults(ResponseBestParameter best) {
		System.out.println("Best " + best);

		boolean configDefaultIsAnalyzed = best.getAllConfigs().contains(defaultConfiguration);
		System.out.println("Best is analyzed " + configDefaultIsAnalyzed + " best value: " + best.getMetricValue());
		assertTrue(configDefaultIsAnalyzed);

		boolean configDefaultIsBest = best.getAllBest().contains(defaultConfiguration);
		System.out.println("Best is default " + configDefaultIsBest + " value: "
				+ best.getMetricValueByConfiguration().get(defaultConfiguration));
	}

	public static void main(String[] args) throws IOException {
		ResultProcessor p = new ResultProcessor();
		p.process();

	}

}
