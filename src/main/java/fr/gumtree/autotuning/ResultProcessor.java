package fr.gumtree.autotuning;

import java.io.File;
import java.io.IOException;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.experimentrunner.StructuredFolderfRunner;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ResultProcessor {

	public void process() throws IOException {

		File fileResults = new File("/Users/matias/develop/gt-tuning/results/resultsdatv2/outDAT2_SPOON_onlyresult/");

		StructuredFolderfRunner runner = new StructuredFolderfRunner();

		ResponseBestParameter best = runner.summarizeBestGlobal(fileResults);

		System.out.println("Best " + best);

	}

	public static void main(String[] args) throws IOException {
		ResultProcessor p = new ResultProcessor();
		p.process();

	}

}
