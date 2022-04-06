package fr.gumtree.autotuning.exhaustive;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.Test;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.experimentrunner.StructuredFolderfRunner;

public class SummarizationResultTest {

	@Test
	public void testGlobal() throws IOException {

		File fileResults = new File("./examples/execution_sample1");
		assertTrue(fileResults.exists());

		assertTrue(fileResults.list().length > 0);

		StructuredFolderfRunner runner = new StructuredFolderfRunner();

		ResponseBestParameter best = runner.summarizeBestGlobal(fileResults);

		// assertTrue(results.size() > 1000);

		// assertEquals(2, results.values().stream().findAny().get().size());

		assertTrue(best.getAllBest().size() > 1);

		assertEquals(2, best.getNumberOfEvaluatedPairs());

	}

	@Test
	public void testLocal() throws IOException {

		File fileResults = new File("./examples/execution_sample1");
		assertTrue(fileResults.exists());

		assertTrue(fileResults.list().length > 0);

		StructuredFolderfRunner runner = new StructuredFolderfRunner();

		ResponseBestParameter best = runner.summarizeBestLocal(fileResults);

		// assertTrue(results.size() > 1000);

		// assertEquals(2, results.values().stream().findAny().get().size());

		assertTrue(best.getAllBest().size() > 1);

		assertEquals(2, best.getNumberOfEvaluatedPairs());

		System.out.println(best);

	}

}
