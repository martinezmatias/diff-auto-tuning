package fr.gumtree.autotuning;

import org.junit.Test;

public class MainTest {

	public void testCD() {
		// 1_00b0ec3bcfd8437269d19ad865f3c530bf8b954b
		// change in comment but changedistiller produce lot of changes
	}

	@Test
	public void testMain1() {
		String[] command = new String[] {
				"-path=/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded", //
				"-out=./out/", //
				"-stop=1", //
				"-subset=1", "-paralleltype=MATCHER_LEVEL" };
		Main.main(command);
	}

	@Test
	public void testMain2() {
		String[] command = new String[] {
				"-path=/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded", //
				"-out=./out/", //
				"-stop=1", //
				"-astmodel=JDT", //
				"-subset=1", "-paralleltype=MATCHER_LEVEL" };
		Main.main(command);
	}

	@Test
	public void testMain2_propertyLevel() {
		String[] command = new String[] {
				"-path=/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded", //
				"-out=./out/", //
				"-stop=1", //
				"-astmodel=JDT", //
				"-subset=1", "-paralleltype=PROPERTY_LEVEL" };
		Main.main(command);
	}

	@Test
	public void testMain3() {
		String[] command = new String[] {
				"-path=/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded", //
				"-out=./out/", //
				"-stop=1", //
				"-subset=1", "-timeout=10" };
		Main.main(command);
	}

	@Test
	public void testMain4_matcher() {
		String[] command = new String[] {
				"-path=/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded", //
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
		String[] command = new String[] {
				"-path=/Users/matias/develop/sketch-repair/git-sketch4repair/datasets/megadiff-expanded", //
				"-out=./outxysimpletest/", //
				"-stop=100000000", //
				"-begin=15151", "-subset=2",
				// "-timeout=10", //
				"-matchers", "XyMatcher", //
				"-matchers", "simpleGumtree", "-overwriteresults=false" };
		Main.main(command);
	}
}
