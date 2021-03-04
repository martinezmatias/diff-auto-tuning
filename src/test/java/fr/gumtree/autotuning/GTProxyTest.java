package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.github.gumtreediff.actions.Diff;
import com.github.gumtreediff.tree.Tree;
import com.google.gson.JsonObject;

import fr.gumtree.autotuning.server.ServerLauncher;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class GTProxyTest {

	ServerLauncher launcher;

	@Before
	public void setup() throws IOException {
		System.out.println("Starting server");
		launcher = new ServerLauncher();
		launcher.start();
	}

	@After
	public void down() throws IllegalAccessException {
		System.out.println("Shut down server");
		launcher.stop();
		System.out.println("End");
	}

	@Test
	public void testProxy() throws Exception {
		GTProxy proxy = new GTProxy();

		File fs = new File(
				"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_s.java");
		File ft = new File(
				"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_t.java");

		ITreeBuilder treebuilder = null;

		treebuilder = new SpoonTreeBuilder();

		Tree tl = treebuilder.build((fs));

		Tree tr = treebuilder.build((ft));

		Diff d1 = proxy.run(tl, tr, "SimpleGumtree-st_priocalc-height-st_minprio-5");

		assertTrue(d1.editScript.asList().size() > 0);

		Diff d2 = proxy.run(tl, tr, "ChangeDistiller-cd_maxleaves-6-cd_labsim-0.3-cd_structsim1-0.4-cd_structsim2-1.0");

		assertTrue(d2.editScript.asList().size() > 0);

		Diff d3 = proxy.run(tl, tr,
				"CompleteGumtreeMatcher-st_priocalc-size-bu_minsim-1.0-st_minprio-2-bu_minsize-100");

		assertTrue(d3.editScript.asList().size() > 0);

		//
		Diff d4 = proxy.run(tl, tr, "XyMatcher-st_priocalc-size-st_minprio-1-xy_minsim-0.1");

		assertTrue(d4.editScript.asList().size() > 0);
		//

		Diff d5 = proxy.run(tl, tr, "ClassicGumtree-st_priocalc-size-bu_minsim-0.1-st_minprio-1-bu_minsize-900");

		assertTrue(d5.editScript.asList().size() > 0);

	}

	@Test
	public void testRequestCreateSingleDiff() throws IOException, InterruptedException {

		File fs = new File(
				"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_s.java");
		File ft = new File(
				"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_t.java");

		JsonObject responseJSon = this.launcher.initSimple(fs, ft);

		assertEquals("created", responseJSon.get("status").getAsString());

		String param = "SimpleGumtree-st_priocalc-height-st_minprio-5";
		JsonObject convertedObject = this.launcher.call(param);
		// assertEquals(57, convertedObject.get("actions").getAsInt());

		param = "SimpleGumtree-st_priocalc-size-st_minprio-1";
		convertedObject = this.launcher.call(param);
		assertEquals(1,
				convertedObject.get("actions").getAsJsonArray().get(0).getAsJsonObject().get("nractions").getAsInt());

		param = "SimpleGumtree-st_priocalc-size-st_minprio-3";
		convertedObject = this.launcher.call(param);
		assertEquals(9,
				convertedObject.get("actions").getAsJsonArray().get(0).getAsJsonObject().get("nractions").getAsInt());

		param = "XyMatcher-st_priocalc-size-st_minprio-4-xy_minsim-1.0";
		convertedObject = this.launcher.call(param);
		assertEquals(19,
				convertedObject.get("actions").getAsJsonArray().get(0).getAsJsonObject().get("nractions").getAsInt());

		param = "ClassicGumtree-st_priocalc-size-bu_minsim-0.3-st_minprio-4-bu_minsize-1100";
		convertedObject = this.launcher.call(param);
		assertEquals(1,
				convertedObject.get("actions").getAsJsonArray().get(0).getAsJsonObject().get("nractions").getAsInt());

	}

	@Test
	public void testRequestCreateMultiple() throws IOException, InterruptedException {

		File fs = new File("./examples/input_multiple.txt");

		JsonObject jsonResponse = this.launcher.initMultiple(fs);

		assertEquals("created", jsonResponse.get("status").getAsString());

		assertEquals(1, jsonResponse.get("pairs").getAsInt());

		assertEquals("multiplecreate", jsonResponse.get("operation").getAsString());

		String param = "SimpleGumtree-st_priocalc-height-st_minprio-5";
		JsonObject convertedObject = this.launcher.callMultiple(param);
		// assertEquals(57, convertedObject.get("actions").getAsInt());

		param = "SimpleGumtree-st_priocalc-size-st_minprio-1";
		convertedObject = this.launcher.callMultiple(param);
		System.out.println("response " + convertedObject.toString());
		assertEquals(1,
				convertedObject.get("actions").getAsJsonArray().get(0).getAsJsonObject().get("nractions").getAsInt());

		param = "SimpleGumtree-st_priocalc-size-st_minprio-3";
		convertedObject = this.launcher.callMultiple(param);
		System.out.println("response " + convertedObject.toString());
		assertEquals(9,
				convertedObject.get("actions").getAsJsonArray().get(0).getAsJsonObject().get("nractions").getAsInt());

		param = "XyMatcher-st_priocalc-size-st_minprio-4-xy_minsim-1.0";
		convertedObject = this.launcher.callMultiple(param);
		System.out.println("response " + convertedObject.toString());
		assertEquals(19,
				convertedObject.get("actions").getAsJsonArray().get(0).getAsJsonObject().get("nractions").getAsInt());

		param = "ClassicGumtree-st_priocalc-size-bu_minsim-0.3-st_minprio-4-bu_minsize-1100";
		convertedObject = this.launcher.callMultiple(param);
		System.out.println("response " + convertedObject.toString());
		assertEquals(1,
				convertedObject.get("actions").getAsJsonArray().get(0).getAsJsonObject().get("nractions").getAsInt());

	}

}
