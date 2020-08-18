package fr.gumtree.autotuning;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.github.gumtreediff.gen.jdt.JdtTreeGenerator;
import com.github.gumtreediff.matchers.CompositeMatchers;
import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumTreeProperties;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.tree.ITree;

public class VisualDiffTest {

	@Test
	public void testDiffForTestJDT() throws IOException {
		/**
		 * ./gumtree webdiff
		 * /Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded/1/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_s.java
		 * /Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded/1/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_t.java
		 * --port 4568
		 */
		File rootMegadiff = new File(
				"/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded");
		assertTrue(rootMegadiff.exists());
		String diffId = "1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService";
		String fl = rootMegadiff.getAbsolutePath()
				+ "/1/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_s.java";
		String fr = rootMegadiff.getAbsolutePath()
				+ "/1/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905/FlowControlService/1_0a664d752c4b0e5a7fb6f06d005181a0c9dc2905_FlowControlService_t.java";
		System.out.println(fl);

		File fileLeftt = new File(fl);
		String lc = new String(Files.readAllBytes(fileLeftt.toPath()));
		ITree tl = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

		File fileRight = new File(fr);
		lc = new String(Files.readAllBytes(fileRight.toPath()));
		ITree tr = new JdtTreeGenerator().generateFrom().string(lc).getRoot();

		GumTreeProperties properies = new GumTreeProperties();

		properies.getProperties().clear();
		properies.put(ConfigurationOptions.GT_BUM_SMT, 0.7);
		properies.put(ConfigurationOptions.GT_BUM_SZT, 1900);
		properies.put(ConfigurationOptions.GT_STM_MH, 2);

		Matcher matcher = new CompositeMatchers.ClassicGumtree();
		File fout = new File("/Users/matias/Downloads/");
		String name = "default";
		ResultVisualizer rv = new ResultVisualizer();
		rv.saveVisualization(fileLeftt, tl, fileRight, tr, properies, matcher, fout, diffId, name);
		System.out.println("End");

	}

	@Test
	public void testDiffForTestJDT2() throws IOException {

		String row = "nr_563_id_1_0649f1cc087cefe312edb4dc21dc7e01ebb2b401_Compile_JDT.csv,ClassicGumtree_0.1_1500_1,8,ClassicGumtree_0.5_1000_2,102,94,ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1500@GT_STM_MH@1,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@2";
		// "nr_796_id_3_054edb17739fc164d8f56aef4dbb7ff9eaf9e88c_AvailableServiceBindingController_JDT.csv,ClassicGumtree_0.1_1400_1,8,ClassicGumtree_0.5_1000_2,94,86,ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1400@GT_STM_MH@1,ClassicGumtree@GT_BUM_SMT@0.5@GT_BUM_SZT@1000@GT_STM_MH@2";
		String pathMegadiff = "/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded";
		String pathOut = "/Users/matias/Downloads/";
		ResultVisualizer rv = new ResultVisualizer();
		rv.saveVisualizationRow(row, pathMegadiff, pathOut);

	}

	@Test
	public void testAll() {
		String pathMegadiff = "/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded";
		String pathOut = "/Users/matias/Downloads/visualizationsTop/";
		String pathResults = "/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/src/rowDataConsumers/revisionstoanalyze_10.csv";

		read(pathResults, pathMegadiff, pathOut);

	}

	public void read(String pathResults, String pathMegadiff, String pathOut) {
		BufferedReader reader;
		ResultVisualizer rv = new ResultVisualizer();

		try {
			reader = new BufferedReader(new FileReader(pathResults));
			String line = reader.readLine();
			System.out.println("Ignore the first one: " + line);
			line = reader.readLine();

			// we store the hash of the lines and the distance
			Map<String, Integer> linesAndDifferences = new HashMap<>();

			while (line != null) {
				// read next line

				if (line.trim().isEmpty())
					continue;

				// get the distance
				String distance = line.split(",")[5].replace(".0", "");

				linesAndDifferences.put(line, new Integer(distance));

				line = reader.readLine();

			}
			reader.close();

			List<String> lines = new ArrayList(linesAndDifferences.keySet());

			Collections.sort(lines, new Comparator<String>() {

				@Override
				public int compare(String o1, String o2) {

					return Integer.compare(linesAndDifferences.get(o2), linesAndDifferences.get(o1));
				}

			});

			System.out.println("total configs " + lines.size());

			int i = 0;
			int limit = 50;
			for (String iline : lines) {

				System.out.println(i + "/" + limit);
				rv.saveVisualizationRow(iline, pathMegadiff, pathOut);

				i++;

				if (i > limit)
					break;

			}
			System.out.println("Results");
			for (int j = 0; j < limit; j++) {

				System.out.println(lines.get(j) + ", " + linesAndDifferences.get(lines.get(j)));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
