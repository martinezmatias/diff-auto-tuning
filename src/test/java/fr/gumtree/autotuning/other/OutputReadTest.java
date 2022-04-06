package fr.gumtree.autotuning.other;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import org.junit.Test;

import fr.gumtree.autotuning.outils.DatOutputEngine;
import fr.gumtree.autotuning.searchengines.ResultByConfig;

public class OutputReadTest {

	@Test
	public void test() throws Exception {

		DatOutputEngine output = new DatOutputEngine("");

		String file = "./examples/equivalents_0123bbbe6d03f9030bbe62d3df302002fce8df01_Address_s.java_c_ClassicGumtree-bu_minsim-0.1-bu_minsize-100-st_minprio-1-st_priocalc-size_size_62.zip";
		// String out = output.readZip(file);

//		System.out.println(out);

		String out = output.unzipFolder(new File(file).toPath());

		assertNotNull(out);

		assertFalse(out.isEmpty());

		///
		String file2 = "./examples/result_size_per_config_.zip";

		ResultByConfig cr = new ResultByConfig();

		output.readZipAndAdd(cr, new File(file2));

		assertFalse(cr.isEmpty());

		System.out.println(cr);

	}

}
