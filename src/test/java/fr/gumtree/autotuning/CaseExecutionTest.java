package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.junit.Test;

import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumtreeProperties;

import fr.gumtree.autotuning.TuningEngine.PARALLEL_EXECUTION;
import fr.gumtree.autotuning.entity.CaseResult;
import fr.gumtree.autotuning.entity.MatcherResult;
import fr.gumtree.autotuning.entity.SingleDiffResult;

public class CaseExecutionTest {
	final File rootMegadiff = new File("./examples/megadiff-sample");

	@Test
	public void testNavigate_SingleDiff_1_831e3b() throws IOException {

		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();

		String commitId = "831e3b0420e70f7c2695cb248dd8b488b1fd84b7";

		boolean parallel = false;

		int megadiff_id = 1;

		CaseResult result = reader.navigateSingleDiffMegaDiff("./out/", rootMegadiff, megadiff_id, commitId,
				PARALLEL_EXECUTION.PROPERTY_LEVEL);

		assertNotNull(result);
		System.out.println(result);
		assertTrue(result.getResultByMatcher().keySet().size() > 0);

	}

	/// Diff in one annotation
	@Test
	public void testNavigate_SingleDiff_1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e() throws IOException {

		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();

		String commitId = "02f3fd442349d4e7fdfc9c31a82bb1638db8495e";

		boolean parallel = false;

		int megadiff_id = 1;

		CaseResult result = reader.navigateSingleDiffMegaDiff("./out/", rootMegadiff, megadiff_id, commitId,
				PARALLEL_EXECUTION.NONE);

		assertNotNull(result);
		System.out.println(result);

		Integer nrActions = (Integer) result.getResultByMatcher().values().stream().findFirst().get()
				.getAlldiffresults().get(0).get(TuningEngine.NRACTIONS);
		assertTrue(nrActions > 0);

	}

	@Test
	public void testNavigate_SingleDiff_1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_convertion() throws IOException {

		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();

		String commitId = "02f3fd442349d4e7fdfc9c31a82bb1638db8495e";

		int megadiff_id = 1;

		CaseResult result = reader.navigateSingleDiffMegaDiff("./out/", rootMegadiff, megadiff_id, commitId,
				PARALLEL_EXECUTION.NONE);

		assertNotNull(result);
		System.out.println(result);

		SingleDiffResult singleDiffResult = result.getResultByMatcher().values().stream().findFirst().get()
				.getAlldiffresults().get(0);

		Integer nrActions = (Integer) singleDiffResult.get(TuningEngine.NRACTIONS);
		assertTrue(nrActions > 0);

		Map<String, Object> properties = reader
				.toGumtreePropertyToMap((GumtreeProperties) singleDiffResult.get(TuningEngine.CONFIG));
		assertNotNull(properties);
		assertTrue(properties.size() > 0);

		System.out.println(properties);

		MatcherResult resultMatcherSimple = result.getResultByMatcher().values().stream()
				.filter(e -> e.getMatcherName().equals("SimpleGumtree")).findFirst().get();

		assertTrue(resultMatcherSimple.getAlldiffresults().size() > 0);

		SingleDiffResult resultSimpleDiff = resultMatcherSimple.getAlldiffresults().get(0);

		Map<String, Object> propertiesSimple = reader
				.toGumtreePropertyToMap((GumtreeProperties) resultSimpleDiff.get(TuningEngine.CONFIG));

		System.out.println(propertiesSimple);
		assertEquals(2, propertiesSimple.keySet().size());

		assertTrue(propertiesSimple.keySet().contains(ConfigurationOptions.st_priocalc.name()));

		assertTrue(propertiesSimple.keySet().contains(ConfigurationOptions.st_minprio.name()));
	}

	@Test
	public void testNavigate_SingleDiff_1_() throws IOException {

		assertTrue(rootMegadiff.exists());

		TuningEngine reader = new TuningEngine();

		String commitId = "0a664d752c4b0e5a7fb6f06d005181a0c9dc2905";

		int megadiff_id = 1;

		CaseResult result = reader.navigateSingleDiffMegaDiff("./out/", rootMegadiff, megadiff_id, commitId,
				PARALLEL_EXECUTION.NONE);

		assertNotNull(result);
		System.out.println(result);

		assertTrue(result.getResultByMatcher().keySet().size() > 0);

	}
}
