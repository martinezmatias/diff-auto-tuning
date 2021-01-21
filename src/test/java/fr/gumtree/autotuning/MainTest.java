package fr.gumtree.autotuning;

import java.io.File;

import org.junit.Test;

public class MainTest {

	final File rootMegadiff = new File("./examples/megadiff-sample/");

	@Test
	public void testMain1() {
		String[] command = new String[] { "-path=" + rootMegadiff.getAbsolutePath(), //
				"-out=./out/", //
				"-stop=1", //
				"-subset=1", "-paralleltype=MATCHER_LEVEL" };
		Main.main(command);
	}

	@Test
	public void testMain2() {
		String[] command = new String[] { "-path=" + rootMegadiff.getAbsolutePath(), //
				"-out=./out/", //
				"-stop=1", //
				"-astmodel=JDT", //
				"-subset=1", "-paralleltype=MATCHER_LEVEL" };
		Main.main(command);
	}

	@Test
	public void testMain2_propertyLevel() {
		String[] command = new String[] { "-path=" + rootMegadiff.getAbsolutePath(), //
				"-out=./out/", //
				"-stop=1", //
				"-astmodel=JDT", //
				"-subset=1", "-paralleltype=PROPERTY_LEVEL" };
		Main.main(command);
	}

	@Test
	public void testMain3() {
		String[] command = new String[] { "-path=" + rootMegadiff.getAbsolutePath(), //
				"-out=./out/", //
				"-stop=1", //
				"-astmodel=JDT", //
				"-subset=1", "-timeout=10", "-overwriteresults=true" };
		Main.main(command);
	}

	@Test
	public void testMain4_matcher() {
		String[] command = new String[] { "-path=" + rootMegadiff.getAbsolutePath(), //
				"-out=./out/", //
				"-stop=1", //
				"-subset=1", "-timeout=10", //
				"-matchers=XyMatcher", //
				"-matchers=blablabla", //
				"-matchers=simpleGumtree", "-overwriteresults=true", "-paralleltype=MATCHER_LEVEL" };
		Main.main(command);
	}

	@Test
	public void testMain5_matcher() {
		String[] command = new String[] { "-path=" + rootMegadiff.getAbsolutePath(), //
				"-out=./outxysimpletest/", //
				"-stop=100000000", //
				"-begin=15151", "-subset=2",
				// "-timeout=10", //
				"-matchers", "XyMatcher", //
				"-matchers", "simpleGumtree", "-overwriteresults=false" };
		Main.main(command);
	}

	@Test
	public void testMain6_matcher() {
		String[] command = new String[] { "-path=" + rootMegadiff.getAbsolutePath(), //
				"-out=./outxysimpletest/", //
				"-stop=1", //
				// "-begin=1",
				"-subset=2",
				// "-timeout=10", //
				"-matchers", "ChangeDistiller", //
				"-overwriteresults=true",
				//
				"-paralleltype=PROPERTY_LEVEL" };
		Main.main(command);
	}

}
