package fr.gumtree.autotuning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.Test;

import com.github.gumtreediff.actions.Diff;
import com.github.gumtreediff.tree.Tree;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import fr.gumtree.autotuning.TuningEngine.ASTMODE;
import fr.gumtree.autotuning.treebuilder.SpoonTreeBuilder;

public class GTProxyTest {

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
	public void testRequestCreate() throws IOException, InterruptedException {

		File fs = new File(
				"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_s.java");
		File ft = new File(
				"./examples/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e/Version/1_02f3fd442349d4e7fdfc9c31a82bb1638db8495e_Version_t.java");

		HttpClient client = HttpClient.newHttpClient();
		URI create = URI.create("http://localhost:8001/test?action=load&model=" + ASTMODE.GTSPOON + "&left="
				+ fs.getAbsolutePath() + "&right=" + ft.getAbsolutePath());

		System.out.println(create);

		HttpRequest request = HttpRequest.newBuilder().uri(create).build();
		HttpResponse<String> d = client.send(request, BodyHandlers.ofString());

		String res = d.body();

		System.out.println("-->" + res);

		assertEquals("created", res);
	}

	@Test
	public void testRequestRun() throws IOException, InterruptedException {

		String param = "SimpleGumtree-st_priocalc-height-st_minprio-5";
		JsonObject convertedObject = call(param);
		assertEquals(57, convertedObject.get("actions").getAsInt());

		param = "SimpleGumtree-st_priocalc-size-st_minprio-1";
		convertedObject = call(param);
		assertEquals(1, convertedObject.get("actions").getAsInt());

		param = "SimpleGumtree-st_priocalc-size-st_minprio-3";
		convertedObject = call(param);
		assertEquals(9, convertedObject.get("actions").getAsInt());

		param = "XyMatcher-st_priocalc-size-st_minprio-4-xy_minsim-1.0";
		convertedObject = call(param);
		assertEquals(19, convertedObject.get("actions").getAsInt());

		param = "ClassicGumtree-st_priocalc-size-bu_minsim-0.3-st_minprio-4-bu_minsize-1100";
		convertedObject = call(param);
		assertEquals(1, convertedObject.get("actions").getAsInt());

	}

	public JsonObject call(String param) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newHttpClient();

		URI create = URI.create("http://localhost:8001/test?action=run&parameters=" + param);

		System.out.println(create);

		HttpRequest request = HttpRequest.newBuilder().uri(create).build();
		HttpResponse<String> d = client.send(request, BodyHandlers.ofString());

		String res = d.body();

		JsonObject convertedObject = new Gson().fromJson(res, JsonObject.class);

		System.out.println("-->" + res);
		System.out.println(convertedObject);
		return convertedObject;
	}

}
