package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import com.github.gumtreediff.utils.Pair;

import fr.gumtree.autotuning.entity.ResponseBestParameter;
import fr.gumtree.autotuning.entity.ResponseGlobalBestParameter;
import fr.gumtree.autotuning.entity.ResponseLocalBestParameter;
import fr.gumtree.autotuning.experimentrunner.OfflineResultProcessor;
import fr.gumtree.autotuning.fitness.Fitness;
import fr.gumtree.autotuning.fitness.LengthEditScriptFitness;
import fr.gumtree.autotuning.gumtree.ASTMODE;
import fr.gumtree.autotuning.gumtree.ExecutionConfiguration.METRIC;
import fr.gumtree.autotuning.gumtree.ExecutionTPEConfiguration;
import fr.gumtree.autotuning.gumtree.ExecutionTPEConfiguration.HPOSearchType;
import fr.gumtree.autotuning.searchengines.TPEEngine;
import smile.validation.Bag;
import smile.validation.CrossValidation;

/**
 * 
 * @author Matias Martinez
 *
 */
public class OffLineResultProcessorTest {

	private static final String results_path = // "/Users/matias/develop/gt-tuning/results/resultsdatv3/";//
			"/Users/matias/develop/gt-tuning/git-dat-results/resultsv4/";

	final String defaultConfiguration = "ClassicGumtree-bu_minsim-0.5-bu_minsize-1000-st_minprio-1-st_priocalc-height";

	@Test
	public void testSmileFrameworkCrossValidation() throws IOException {
		int n = 13000;
		int k = 5;
		smile.validation.Bag[] cvresult = CrossValidation.of(n, k);

		assertEquals(k, cvresult.length);

		System.out.println(cvresult);
		for (int i = 0; i < cvresult.length; i++) {
			Bag bag = cvresult[i];
			System.out.println("sample (" + bag.samples.length + ")" + Arrays.toString(bag.samples));
			System.out.println("test (" + bag.oob.length + ")" + Arrays.toString(bag.oob));
		}

	}

	/// USED IN PAPER RQ new Local-Global RQ 1 and 3
	@Test
	public void testLocalGlobalBoth() throws Exception {

		int maxPerProject = 1000; //5000; R0 was this number
		int k = 10;
		METRIC metric = METRIC.MEDIAN;// METRIC.PERCENTILE75;
		OfflineResultProcessor processor = new OfflineResultProcessor("cross_validation_global_both_new");
		HPOSearchType search = HPOSearchType.TPE_OPTUNA;
		File fileResults = null;
			
		
		fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");
		processor.runCrossValidationExahustiveLocalGlobal(fileResults, maxPerProject, metric,
				"ExaJDT_" + maxPerProject + "_"+ search+ "_" , k, search, ASTMODE.JDT);

		fileResults = new File(results_path + "/outDAT2_SPOON_onlyresult/");
		processor.runCrossValidationExahustiveLocalGlobal(fileResults, maxPerProject, metric,
				"ExaSpoon_" + maxPerProject + "_"+ search+ "_", k, search, ASTMODE.GTSPOON);

	}



	/**
	 * This method should produce the RQ 2 e.g., comparison TPE Grid with budgets.
	 * But, it produces a heap exception. So, I run each case, one by one using
	 * testSeedSingleCrossValidationGlobalJDTTPE.
	 * 
	 * @throws Exception
	 */
	@Deprecated
	@Test
	public void testSeedCrossValidationGlobalJDTTPE() throws Exception {

		HPOSearchType search = HPOSearchType.TPE_HYPEROPT;

		OfflineResultProcessor processor = new OfflineResultProcessor("RQ2new_" + search + "_analysis_jdt_1000");
		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");
		int maxPerProject = 5000;
		METRIC metric = METRIC.MEDIAN;

		Integer[] totalLimits = new Integer[] { 1000 };

		List<File> collected = processor.retrievePairsToAnalyze(fileResults, maxPerProject, true);
		System.out.println("Collected " + collected.size());

		for (int totalLimit : totalLimits) {

			System.out.println("total Limit " + totalLimit);

			int k = 10;
			int nrseeds = 10;
			Integer[] allAttempts = new Integer[] { 10, 25, 50, 100 }; // { 10, 25, 50, 100, 2210 };

			for (int numberOfAttemptsTPE : allAttempts) {
				processor.runSeededCrossValidationExahustiveVsTPE(fileResults, collected, metric,
						"results_" + search.name() + "_seeds_" + nrseeds + "_maxProj_" + maxPerProject + "_evals_"
								+ numberOfAttemptsTPE + "_datasize_" + totalLimit + "_",
						numberOfAttemptsTPE, k, nrseeds, totalLimit, search);

				// processor
				// .runSeededCrossValidationExahustiveVsTPE(
				// fileResults, collected, metric, "ExaJDT_seeded" + maxPerProject + "_evals_"
				// + numberOfAttempts + "_datasize_" + totalLimit + "_",
				// numberOfAttempts, k, nrseeds, totalLimit, search);
			}
		}
	}

	/// USED IN PAPER for the new RQ2: TPE and Grid with multiples size of data
	@Test
	public void testSeedSingleCrossValidationGlobalJDTTPE() throws Exception {

		HPOSearchType search = HPOSearchType.TPE_HYPEROPT;

		System.out.println("Search " + search);

		OfflineResultProcessor processor = new OfflineResultProcessor("RQ2new_" + search + "_analysis_jdt_all");
		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");
		final int maxPerProject = 5000; // Do not change
		METRIC metric = METRIC.MEDIAN;
		// TO VARY
		int totalLimit = 100000000;
		// TO VARY
		int numberOfAttemptsTPE = 100;

		List<File> collected = processor.retrievePairsToAnalyze(fileResults, maxPerProject, true);
		System.out.println("Collected " + collected.size());

		System.out.println("total Limit " + totalLimit);

		final int k = 10; // Do not change
		final int nrseeds = 1; // only one as we try all //nrseeds = 10; // Do not change

		processor.runSeededCrossValidationExahustiveVsTPE(fileResults, collected, metric,
				"results_" + search.name() + "_seeds_" + nrseeds + "_maxProj_" + maxPerProject + "_evals_"
						+ numberOfAttemptsTPE + "_datasize_" + totalLimit + "_",
				numberOfAttemptsTPE, k, nrseeds, totalLimit, search);

	}

	enum Granulity {
		SPOON, JDT
	}

	/// MM 2023 ModifiedUSED IN PAPER for the new RQ2: TPE and Grid with multiples
	/// size of data
	@Test
	public void testSeedSingleCrossValidationGlobalJDTTPE2023() throws Exception {

		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");
		HPOSearchType search = HPOSearchType.GRID;

		System.out.println("Search " + search);

		METRIC metric = METRIC.MEDIAN;
		// TO VARY
		int totalLimit = 1000;
		final int maxPerProject = 1000;/// 5000; in the original was like that// Do not change
		// TO VARY
		int numberOfAttemptsTPE = 10;
		final int k = 10; // Was 10 Do not change
		final int nrseeds = 1; // only one as we try all //nrseeds = 10; // Do not change
		boolean cache = true;

		List<File> collected = OfflineResultProcessor.retrievePairsToAnalyze(fileResults, maxPerProject, true);
		System.out.println("Collected " + collected.size());

		int diffToConsider = collected.size() > totalLimit ? totalLimit : collected.size();

		// System.out.println("total Limit " + totalLimit);

		OfflineResultProcessor processor = new OfflineResultProcessor("RQ2new_" + search + "_" + "_jdt_" + metric.name()
				+ "_sizeds_" + diffToConsider + "_attemps_" + numberOfAttemptsTPE + "_nrseeds_" + nrseeds);

		processor.runSeededCrossValidationExahustiveVsOtherApproaches(fileResults, collected, metric, "eval",
				numberOfAttemptsTPE, k, nrseeds, totalLimit, search, cache, ASTMODE.JDT);

	}
	//NEW MM
	@Test
	public void testSeedSingleCrossValidationGlobalJDTTPE2023ONLINE() throws Exception {

		File fileResults = new File("/Users/matias/develop/gt-tuning/data-cvs-vintage/" );
		HPOSearchType search = HPOSearchType.TPE_HYPEROPT;//HPOSearchType.TPE_HYPEROPT;

		System.out.println("Search " + search);

		METRIC metric = METRIC.MEDIAN;
		ASTMODE astmode = ASTMODE.JDT;
		// TO VARY
		int totalLimit = 10;
		final int maxPerProject = 10;/// 5000; in the original was like that// Do not change
		// TO VARY
		int numberOfAttemptsTPE = 10;
		final int k = 5; // Was 10 Do not change
		final int nrseeds = 1; // only one as we try all //nrseeds = 10; // Do not change
		
		List<Pair<File, File>> collected = OfflineResultProcessor.retrievePairsToAnalyzePairs(fileResults, maxPerProject, true);
		System.out.println("Collected " + collected.size());

		int diffToConsider = collected.size() > totalLimit ? totalLimit : collected.size();

		// System.out.println("total Limit " + totalLimit);

		OfflineResultProcessor processor = new OfflineResultProcessor("RQ2new_" + search + "_" + "_jdt_" + metric.name()
				+ "_sizeds_" + diffToConsider + "_attemps_" + numberOfAttemptsTPE + "_nrseeds_" + nrseeds);

		processor.runSeededCrossValidationOnline(fileResults, collected, metric, "eval",
				numberOfAttemptsTPE, k, nrseeds, totalLimit, search,  astmode);

	}
	
	
	//NEW MM
		@Test
		public void testSizes2023ONLINE() throws Exception {

			File fileResults = new File("/Users/matias/develop/gt-tuning/data-cvs-vintage/" );
			HPOSearchType search = HPOSearchType.TPE_HYPEROPT;//HPOSearchType.TPE_HYPEROPT;

			System.out.println("Search " + search);

			METRIC metric = METRIC.MEDIAN;
			ASTMODE astmode = ASTMODE.GTSPOON;
			// TO VARY
			int totalLimit = 100000;
			final int maxPerProject = 1000;
			// TO VARY
			int numberOfAttemptsTPE = 10;
			final int k = 2; // Was 10 Do not change
			final int nrseeds = 1; // only one as we try all //nrseeds = 10; // Do not change
			
			List<Pair<File, File>> collected = OfflineResultProcessor.retrievePairsToAnalyzePairs(fileResults, maxPerProject, true);
			System.out.println("Collected " + collected.size());

			int diffToConsider = collected.size() > totalLimit ? totalLimit : collected.size();

			System.out.println("total Limit " + totalLimit);

			OfflineResultProcessor processor = new OfflineResultProcessor("RQtimes_" + search + "_" + astmode.toString() +"_" + metric.name()
					+ "_sizeds_" + diffToConsider + "_attemps_" + numberOfAttemptsTPE + "_nrseeds_" + nrseeds);

			processor.runTreeSizeCalculator(fileResults, collected, metric, "eval",
					numberOfAttemptsTPE, k, nrseeds, totalLimit, search,  astmode);

		}
		
	
		
		//NEW MM
		@Test
		public void testSizes2023ICSE24() throws Exception {

			File fileResults = new File("/Users/matias/develop/gt-tuning/data-cvs-vintage/" );
		

			METRIC metric = METRIC.MEDIAN;
			ASTMODE astmode = ASTMODE.JDT;
			// TO VARY
			int totalLimit = 1000;
			final int maxPerProject = 1000;
			// TO VARY
			
			final int k = 2; // Was 10 Do not change
			final int nrseeds = 1; // only one as we try all //nrseeds = 10; // Do not change
			
			List<Pair<File, File>> collected = OfflineResultProcessor.retrievePairsToAnalyzePairs(fileResults, maxPerProject, true);
			System.out.println("Collected " + collected.size());

			int diffToConsider = collected.size() > totalLimit ? totalLimit : collected.size();

			System.out.println("total Limit " + totalLimit);

			OfflineResultProcessor processor = new OfflineResultProcessor("RQICSE_"  + astmode.toString() +"_" + metric.name()
					+ "_sizeds_" + diffToConsider+ "_nrseeds_" + nrseeds);

			String defaultConfiguration = "ClassicGumtree-bu_minsim-0.5-bu_minsize-500-st_minprio-1-st_priocalc-height";
			processor.runTimesICSE2024(fileResults, collected, metric, "eval",k, nrseeds, totalLimit, astmode, defaultConfiguration);

		}
		
		@Test
		public void testtICSESimple() throws Exception {
			File fileResults = new File("/Users/matias/Downloads/datasets-main/defects4j" );

			METRIC metric = METRIC.MEDIAN;
			ASTMODE astmode = ASTMODE.JDT;
			// TO VARY
			int totalLimit = 100000000;
			final int maxPerProject = 1000000000;
			// TO VARY
			
			final int k = 2; // Was 10 Do not change
			final int nrseeds = 1; // only one as we try all //nrseeds = 10; // Do not change
			
			List<Pair<File, File>> collected = retrievePairsToAnalyzePairsD4J(fileResults);
			System.out.println("Collected " + collected.size());

			int diffToConsider = collected.size() > totalLimit ? totalLimit : collected.size();

			System.out.println("total Limit " + totalLimit);

			OfflineResultProcessor processor = new OfflineResultProcessor("RQICSE_"  + astmode.toString() +"_" + metric.name()
					+ "_sizeds_" + diffToConsider+ "_nrseeds_" + nrseeds);

			String defaultConfiguration = "ClassicGumtree-bu_minsim-0.5-bu_minsize-100-st_minprio-1-st_priocalc-height";
			processor.runTimesICSE2024Seed(fileResults, collected, metric, "eval",k, nrseeds, totalLimit, astmode, defaultConfiguration);
			
			
		}
		
		
		public static List<Pair<File, File>> retrievePairsToAnalyzePairsD4J(File rootFolder) throws IOException {

			// System.out.println("Inspecting " + rootFolder.getAbsolutePath());
			List<Pair<File, File>> collected = new ArrayList<Pair<File, File>>();

			
			File f = new File(rootFolder.getAbsoluteFile()+"/before/" );
					
					
			for (File project : f.listFiles()) {
				
				System.out.println("Proyect "+ project.getAbsolutePath());
				
				if (project.getName().equals(".DS_Store"))
					continue;
				
				for(File bugId : project.listFiles()) {
					
					if (bugId.getName().equals(".DS_Store"))
						continue;
					
					
					//System.out.println(bugId);
					
					File fbefore = bugId.listFiles()[0];
					System.out.println(fbefore);
					
					File fafer = new File(bugId.listFiles()[0].getAbsoluteFile().toString().replace("before", "after"));
					
					System.out.println(fafer);
					collected.add(new Pair(fbefore, fafer));
				}
				
			}	
			return collected;
			
		}
	
		
	
	@Test
	public void testSeedSingleCrossValidationGlobalJDTTPE2023ONLINELOCAL() throws Exception {

		File fileResults = new File("/Users/matias/develop/gt-tuning/data-cvs-vintage/" );
			
		
	//	runRQ4(fileResults,  HPOSearchType.TPE_HYPEROPT, ASTMODE.JDT);
	//	runRQ4(fileResults,  HPOSearchType.TPE_HYPEROPT, ASTMODE.GTSPOON);

	//	runRQ4(fileResults,  HPOSearchType.TPE_OPTUNA, ASTMODE.JDT);
		runRQ4(fileResults,  HPOSearchType.TPE_OPTUNA, ASTMODE.GTSPOON);
		
		
	}
	
	@Test
	public void testSeedSingleCrossValidationGlobalJDTTPE2023ONLINELOCALFineGrand() throws Exception {

		File fileResults = new File("/Users/matias/develop/gt-tuning/data-cvs-vintage/" );
			
		
	//	runRQ4(fileResults,  HPOSearchType.TPE_HYPEROPTFG, ASTMODE.JDT);
		runRQ4(fileResults,  HPOSearchType.TPE_HYPEROPT, ASTMODE.JDT);
	//	runRQ4(fileResults,  HPOSearchType.TPE_HYPEROPT, ASTMODE.GTSPOON);	
	//	runRQ4(fileResults,  HPOSearchType.TPE_HYPEROPT, ASTMODE.GTSPOON);
		
	}
	
	
	@Test
	public void testComparison() throws Exception {

		File fileResults = new File("/Users/matias/develop/gt-tuning/data-cvs-vintage/" );
			
		
	//	runRQ4(fileResults,  HPOSearchType.TPE_HYPEROPTFG, ASTMODE.JDT);
		runRQ5(fileResults,  HPOSearchType.TPE_HYPEROPT, ASTMODE.JDT);
	//	runRQ4(fileResults,  HPOSearchType.TPE_HYPEROPT, ASTMODE.GTSPOON);	
	//	runRQ4(fileResults,  HPOSearchType.TPE_HYPEROPT, ASTMODE.GTSPOON);
		
	}
	
	
	private void runRQ4(File fileResults, HPOSearchType search, ASTMODE astmode) throws IOException, Exception {
		System.out.println("Search " + search);

		METRIC metric = METRIC.MEDIAN;
	
		// TO VARY
		int totalLimit = 300;
		final int maxPerProject = 100;/// 5000; in the original was like that// Do not change
		// TO VARY
		int numberOfAttemptsTPE = 100;
		final int k = 2; 
		final int nrseeds = 1; // only one as we try all //nrseeds = 10; // Do not change
		
		List<Pair<File, File>> collected = OfflineResultProcessor.retrievePairsToAnalyzePairs(fileResults, maxPerProject, true);
		System.out.println("Collected " + collected.size());

		int diffToConsider = collected.size() > totalLimit ? totalLimit : collected.size();

		// System.out.println("total Limit " + totalLimit);

		OfflineResultProcessor processor = new OfflineResultProcessor("RQ4_" + search + "_" + astmode+ "_" + metric.name()
				+ "_sizeds_" + diffToConsider + "_attemps_" + numberOfAttemptsTPE + "_nrseeds_" + nrseeds);

		processor.runSeededCrossValidationLocalOnline(fileResults, collected, metric, "eval",
				numberOfAttemptsTPE, k, nrseeds, totalLimit, search,  astmode);
	}
	
	
	private void runRQ5(File fileResults, HPOSearchType search, ASTMODE astmode) throws IOException, Exception {
		System.out.println("Search " + search);

		METRIC metric = METRIC.MEDIAN;
	
		// TO VARY
		int totalLimit = 30000;
		final int maxPerProject = 10000;/// 5000; in the original was like that// Do not change
		// TO VARY
		int numberOfAttemptsTPE = 100;
		final int k = 2; 
		final int nrseeds = 1; // only one as we try all //nrseeds = 10; // Do not change
		
		List<Pair<File, File>> collected = OfflineResultProcessor.retrievePairsToAnalyzePairs(fileResults, maxPerProject, true);
		System.out.println("Collected " + collected.size());

		int diffToConsider = collected.size() > totalLimit ? totalLimit : collected.size();

		// System.out.println("total Limit " + totalLimit);

		OfflineResultProcessor processor = new OfflineResultProcessor("RQ_Comparison_" + search + "_" + astmode+ "_" + metric.name()
				+ "_sizeds_" + diffToConsider + "_attemps_" + numberOfAttemptsTPE + "_nrseeds_" + nrseeds);

		processor.runComparison(fileResults, collected, metric, "eval",
				numberOfAttemptsTPE, k, nrseeds, totalLimit, search,  astmode);
	}
	
	@Test
	public void testSeedSingleCrossValidationGlobalJDTTPE2023All() throws Exception {

		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");
		HPOSearchType[] searches = { HPOSearchType.TPE_HYPEROPT, HPOSearchType.TPE_OPTUNA,
				HPOSearchType.RANDOM_HYPEROPT, HPOSearchType.RANDOM_OPTUNA };
		
		METRIC[] metrics = { METRIC.MEDIAN, METRIC.PERCENTILE75 };
		boolean cache = true;
		for (METRIC metric : metrics) {
			for (int i = 0; i < searches.length; i++) {
				HPOSearchType search = searches[i];
				System.out.println("Search " + search);

				//METRIC metric = METRIC.MEDIAN;
				// TO VARY
				int totalLimit = 100000;
				final int maxPerProject = 1000;/// 5000; in the original was like that// Do not change
				// TO VARY
				int numberOfAttemptsTPE = 100;
				final int k = 10; // Was 10 Do not change
				final int nrseeds = 5; // only one as we try all //nrseeds = 10; // Do not change

				List<File> collected = OfflineResultProcessor.retrievePairsToAnalyze(fileResults, maxPerProject, true);
				System.out.println("Collected " + collected.size());

				int diffToConsider = collected.size() > totalLimit ? totalLimit : collected.size();

				// System.out.println("total Limit " + totalLimit);

				OfflineResultProcessor processor = new OfflineResultProcessor(
						"RQ2new_" + search + "_" + "_jdt_" + metric.name() + "_sizeds_" + diffToConsider + "_attemps_"
								+ numberOfAttemptsTPE + "_nrseeds_" + nrseeds);

				processor.runSeededCrossValidationExahustiveVsOtherApproaches(fileResults, collected, metric, "eval",
						numberOfAttemptsTPE, k, nrseeds, totalLimit, search, cache, ASTMODE.JDT);

			}
		}

	}
	
	@Test
	public void testSeedSingleCrossValidationGlobalSpoonTPE2023All() throws Exception {
		boolean cache = true;
		String granularity = "SPOON";
		File fileResults = new File(results_path + "/outDAT2_"+granularity
				+ "_onlyresult/");
		HPOSearchType[] searches = { HPOSearchType.TPE_HYPEROPT, HPOSearchType.TPE_OPTUNA,
				HPOSearchType.RANDOM_HYPEROPT, HPOSearchType.RANDOM_OPTUNA };
		
		METRIC[] metrics = { METRIC.MEDIAN, METRIC.PERCENTILE75 };
		int[] limits = {100 };//{100000, 1000, 100 };
		for (int totalLimit :limits ){
		for (METRIC metric : metrics) {
			for (int i = 0; i < searches.length; i++) {
				HPOSearchType search = searches[i];
				System.out.println("Search " + search);

				//METRIC metric = METRIC.MEDIAN;
				// TO VARY
				//int totalLimit = 100000;
				final int maxPerProject = 1000;/// 5000; in the original was like that// Do not change
				// TO VARY
				int numberOfAttemptsTPE = 100;
				final int k = 10; // Was 10 Do not change
				final int nrseeds = 5; // only one as we try all //nrseeds = 10; // Do not change

				List<File> collected = OfflineResultProcessor.retrievePairsToAnalyze(fileResults, maxPerProject, true);
				System.out.println("Collected " + collected.size());

				int diffToConsider = collected.size() > totalLimit ? totalLimit : collected.size();

				// System.out.println("total Limit " + totalLimit);

				OfflineResultProcessor processor = new OfflineResultProcessor(
						"RQ2new_" + search + "_" + granularity + "_" + metric.name() + "_sizeds_" + diffToConsider + "_attemps_"
								+ numberOfAttemptsTPE + "_nrseeds_" + nrseeds);

				processor.runSeededCrossValidationExahustiveVsOtherApproaches(fileResults, collected, metric, "eval",
						numberOfAttemptsTPE, k, nrseeds, totalLimit, search, cache, ASTMODE.GTSPOON);

			}
		}
		}
	}
	

	@Test
	public void testRetrieveTimes() throws Exception {

		OfflineResultProcessor processor = new OfflineResultProcessor("Times");
		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");
		final int maxPerProject = 5000; // Do not change

		List<File> collected = processor.retrievePairsToAnalyze(fileResults, maxPerProject, true);
		System.out.println("Size " + collected.size());

		FileWriter fw = new FileWriter(results_path + "/times_" + System.currentTimeMillis() + ".txt");

		for (int i = 0; i < collected.size(); i++) {
			System.out.println(collected.get(i).getAbsolutePath());
			File times = new File(collected.get(i).getParentFile().getAbsolutePath().replace(
					"/Users/matias/develop/gt-tuning/git-dat-results/resultsv4/outDAT2_JDT_onlyresult/",
					"/Users/matias/develop/gt-tuning/dat-row-data/outDAT2_JDT/") + File.separatorChar + "listTime.txt");

			if (times.exists()) {

				System.out.println("Exists" + times.getPath());

				// FileReader fr = new FileReader(times);

				String data = FileUtils.readFileToString(times, "UTF-8");
				fw.write(data);

			}

			// System.out.println(i);
			// break;
		}
		fw.close();

	}




	@Test
	public void testRandomTPEJDTGlobal() throws Exception {

		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");

		OfflineResultProcessor runner = new OfflineResultProcessor();
		int maxPerProject = 100;
		List<File> collected = runner.retrievePairsToAnalyze(fileResults, maxPerProject, true);
		OfflineResultProcessor processor = new OfflineResultProcessor();

		Path fileWithData = processor.createFileWithDataToAnalyze(collected);

		TPEEngine tpe = new TPEEngine();

		ExecutionTPEConfiguration configuration = new ExecutionTPEConfiguration(METRIC.MEAN, ASTMODE.JDT,
				new LengthEditScriptFitness(), HPOSearchType.TPE_HYPEROPT);
		configuration.setNumberOfAttempts(98);
		configuration.setSearchType(HPOSearchType.TPE_HYPEROPT);
		configuration.setRandomseed(12);
		LengthEditScriptFitness fitness = new LengthEditScriptFitness();
		ResponseBestParameter bestRandomTPE = tpe.computeBestGlobalCache(fileWithData.toFile(), fitness, configuration);

		assertEquals(149.17, bestRandomTPE.getMetricValue(), 0.1);
		System.out.println("Best TPE " + bestRandomTPE);
	}

	@Test
	public void testCollectFolders() throws IOException {

		File fileResults = new File(results_path + "/outDAT2_SPOON_onlyresult/");

		OfflineResultProcessor runner = new OfflineResultProcessor();
		List<File> collected = runner.retrievePairsToAnalyze(fileResults, 10);

		assertEquals(130, collected.size());
		assertEquals(10, collected.stream().filter(e -> e.getAbsolutePath().contains("git-struts")).count());
		assertEquals(10, collected.stream().filter(e -> e.getAbsolutePath().contains("git-tomcat")).count());
	}

	@Test
	@Deprecated // Use cross validation instead
	public void testExhaustiveGlobalSpoon() throws IOException {
		File fileResults = new File(results_path + "/outDAT2_SPOON_onlyresult/");
		Fitness fitnessFunction = new LengthEditScriptFitness();
		OfflineResultProcessor processor = new OfflineResultProcessor();

		boolean ignoreTimeout = true;
		ResponseGlobalBestParameter best = processor.summarizeBestGlobal(fileResults, fitnessFunction, METRIC.MEAN,
				ignoreTimeout);

		inspectResults(best);
		processor.analyzeBestWithGlobal(best);
	}

	@Test
	@Deprecated // Use cross validation instead
	public void testExhaustiveLocalSpoon() throws IOException {
		File fileResults = new File(results_path + "/outDAT2_SPOON_onlyresult/");
		OfflineResultProcessor processor = new OfflineResultProcessor();

		OfflineResultProcessor runner = new OfflineResultProcessor();

		ResponseLocalBestParameter best = runner.summarizeBestLocal(fileResults, METRIC.MEAN, defaultConfiguration);

		// inspectResults(best);
		processor.analyzeBestWithLocal(best);
	}

	private void inspectResults(ResponseGlobalBestParameter best) {
		System.out.println("Best " + best);

		boolean configDefaultIsAnalyzed = best.getMetricValueByConfiguration().keySet().contains(defaultConfiguration);
		System.out.println("Best is analyzed " + configDefaultIsAnalyzed + " best value: " + best.getMetricValue());
		assertTrue(configDefaultIsAnalyzed);

		boolean configDefaultIsBest = best.getAllBest().contains(defaultConfiguration);
		System.out.println("Best is default " + configDefaultIsBest + " value: "
				+ best.getMetricValueByConfiguration().get(defaultConfiguration));
	}

	@Test
	public void testExhaustiveGlobalJDT() throws IOException {
		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");

		OfflineResultProcessor runner = new OfflineResultProcessor();
		boolean ignoreTimeout = false;
		Fitness fitnessFunction = new LengthEditScriptFitness();

		ResponseGlobalBestParameter best = runner.summarizeBestGlobal(fileResults, fitnessFunction, METRIC.MEAN,
				ignoreTimeout);

		inspectResults(best);
		runner.analyzeBestWithGlobal(best);

	}

	@Test
	public void testExhaustiveLocalJDT() throws IOException {
		File fileResults = new File(results_path + "/outDAT2_JDT_onlyresult/");
		OfflineResultProcessor processor = new OfflineResultProcessor();

		int maxPerProject = 100;
		processor.analyzeLocal(fileResults, maxPerProject, METRIC.MEAN);
	}

}
