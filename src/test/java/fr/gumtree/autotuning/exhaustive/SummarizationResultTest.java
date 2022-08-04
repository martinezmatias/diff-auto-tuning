package fr.gumtree.autotuning.exhaustive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.experimentrunner.OfflineResultProcessor;
import fr.gumtree.autotuning.fitness.LengthEditScriptFitness;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.gumtree.ParametersResolvers;

public class SummarizationResultTest {

	@Test
	public void testGlobal() throws IOException {

		File fileResults = new File("./examples/execution_sample1");
		assertTrue(fileResults.exists());

		assertTrue(fileResults.list().length > 0);

		OfflineResultProcessor runner = new OfflineResultProcessor();
		LengthEditScriptFitness fitness = new LengthEditScriptFitness();
		ResponseBestParameter best = runner.summarizeBestGlobal(fileResults, fitness, METRIC.MEAN, false);

		assertTrue(best.getAllBest().size() > 1);

		assertEquals(2, best.getNumberOfEvaluatedPairs());

	}

	@Test
	public void testLocal() throws IOException {

		File fileResults = new File("./examples/execution_sample1");
		assertTrue(fileResults.exists());

		assertTrue(fileResults.list().length > 0);

		OfflineResultProcessor runner = new OfflineResultProcessor();
		LengthEditScriptFitness fitness = new LengthEditScriptFitness();
		ResponseBestParameter best = runner.summarizeBestLocal(fileResults, METRIC.MEAN,
				ParametersResolvers.defaultConfiguration);

		assertTrue(best.getAllBest().size() > 1);

		assertEquals(2, best.getNumberOfEvaluatedPairs());

		System.out.println(best);

	}

}
