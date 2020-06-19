package fr.gumtree.autotuning;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;

import fr.gumtree.refactoring.RefactorDatasetBuilder;

public class GithubConnectorTest {

	@Test
	public void testJSon() throws IOException, InterruptedException {

		RefactorDatasetBuilder refactorBuilder = new RefactorDatasetBuilder();
		// refactorBuilder.build(new
		// File("/Users/matias/Downloads/refactoring/AddParameter.json"), new
		// File("./out"));
		// refactorBuilder.build(new
		// File("/Users/matias/Downloads/refactoring/ChangeAttributeType.json"),
		// new File("./out"));
		// refactorBuilder.build(new
		// File("/Users/matias/Downloads/refactoring/ChangeParameterType.json"),
		// new File("./out"));
		refactorBuilder.build(new File("/Users/matias/Downloads/refactoring/ChangeReturnType.json"), new File("./out"));
		refactorBuilder.build(new File("/Users/matias/Downloads/refactoring/ChangeVariableType.json"),
				new File("./out"));
		refactorBuilder.build(new File("/Users/matias/Downloads/refactoring/RemoveParameter.json"), new File("./out"));
		refactorBuilder.build(new File("/Users/matias/Downloads/refactoring/RenameAttribute.json"), new File("./out"));
		refactorBuilder.build(new File("/Users/matias/Downloads/refactoring/RenameMethod.json"), new File("./out"));
		refactorBuilder.build(new File("/Users/matias/Downloads/refactoring/RenameParameter.json"), new File("./out"));
		refactorBuilder.build(new File("/Users/matias/Downloads/refactoring/RenameVariable.json"), new File("./out"));
	}

	@Test
	public void testConnection() throws IOException, InterruptedException {
		GitHub gitHub = GitHubBuilder.fromPropertyFile("github.properties").build();

		gitHub.checkApiUrlValidity();

		if (gitHub.isCredentialValid()) {
			System.out.println("Connected");
		} else {
			System.err.println("Not connected");
		}

		List<String> filesBefore = new ArrayList<>();
		List<String> filesCurrent = new ArrayList<>();

		Map<String, String> renamedFilesHint = new HashMap<>();

		RefactorDatasetBuilder r = new RefactorDatasetBuilder();
		r.check(gitHub, "", "Graylog2/graylog2-server", "2d98ae165ea43e9a1ac6a905d6094f077abb2e55", filesBefore,
				filesCurrent, renamedFilesHint);

		// System.out.println(filesBefore + " " + filesCurrent);

		Map<String, String> filesBeforeMap = new HashMap<>();
		Map<String, String> filesAfterMap = new HashMap<>();
		Map<String, String> filesRenameMap = new HashMap<>();
		r.populateWithGitHubAPI(gitHub, "Graylog2/graylog2-server", null, "2d98ae165ea43e9a1ac6a905d6094f077abb2e55",
				filesBeforeMap, filesAfterMap, filesRenameMap, new HashSet<>(), new HashSet<>());

		System.out.println(filesBeforeMap);
		System.out.println(filesAfterMap);
		System.out.println();

	}

}
