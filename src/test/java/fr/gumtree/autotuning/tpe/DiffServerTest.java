package fr.gumtree.autotuning.tpe;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.gson.JsonObject;

import fr.gumtree.autotuning.server.DiffServerLauncher;

/**
 * 
 * @author Matias Martinez
 *
 */
public class DiffServerTest {

	DiffServerLauncher launcher;

	@Before
	public void setup() throws IOException {
		System.out.println("Starting server");
		launcher = new DiffServerLauncher();
		launcher.start();
	}

	@After
	public void down() throws IllegalAccessException {
		System.out.println("Shut down server");
		launcher.stop();
		System.out.println("End");
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
		assertEquals(7,
				convertedObject.get("actions").getAsJsonArray().get(0).getAsJsonObject().get("nractions").getAsInt());

		param = "XyMatcher-st_priocalc-size-st_minprio-4-xy_minsim-1.0";
		convertedObject = this.launcher.call(param);
		assertEquals(27, // 19,
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
		assertEquals(7,
				convertedObject.get("actions").getAsJsonArray().get(0).getAsJsonObject().get("nractions").getAsInt());

		param = "XyMatcher-st_priocalc-size-st_minprio-4-xy_minsim-1.0";
		convertedObject = this.launcher.callMultiple(param);
		System.out.println("response " + convertedObject.toString());
		assertEquals(27, // 19,
				convertedObject.get("actions").getAsJsonArray().get(0).getAsJsonObject().get("nractions").getAsInt());

		param = "ClassicGumtree-st_priocalc-size-bu_minsim-0.3-st_minprio-4-bu_minsize-1100";
		convertedObject = this.launcher.callMultiple(param);
		System.out.println("response " + convertedObject.toString());
		assertEquals(1,
				convertedObject.get("actions").getAsJsonArray().get(0).getAsJsonObject().get("nractions").getAsInt());

	}
}
