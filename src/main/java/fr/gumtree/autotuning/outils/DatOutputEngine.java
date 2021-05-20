package fr.gumtree.autotuning.outils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.github.gumtreediff.matchers.ConfigurationOptions;
import com.github.gumtreediff.matchers.GumtreeProperties;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.utils.Pair;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.gumtree.autotuning.entity.CaseResult;
import fr.gumtree.autotuning.entity.MatcherResult;
import fr.gumtree.autotuning.entity.SingleDiffResult;
import fr.gumtree.autotuning.searchengines.MapList;
import fr.gumtree.autotuning.searchengines.ResultByConfig;
import fr.gumtree.treediff.jdt.TreeDiffFormatBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class DatOutputEngine {

	public static final String ED_SIZE = "v";

	public static final String CONFIGURATION = "c";

	private static final String PREFIX_TREEDIFF_FORMAT = "treeDiffSerialFormat_";

	private static final String PREFIX_FILE_SUMMARY_JSON = "equivalents_";

	private static final String RESULTS_JSON = "result_size_per_config_";

	private MapList<String, String> equivalent = new MapList<String, String>();

	private static boolean zipped = true;

	private String id;

	// Hash - First file with same
	private Map<String, String> hashes = new HashMap<>();

	public DatOutputEngine(String name) {
		this.id = name;
	}

	// TODO add config on last parameter
	public void saveUnifiedNotDuplicated(String filename, String plainProperties,
			com.github.gumtreediff.actions.Diff diffgtt, File parentDiff) throws IOException, NoSuchAlgorithmException {

		filename = new File(filename).getName();

		String key = filename.replace("/", "_") + "_c_" + plainProperties + "_edsize_" + diffgtt.editScript.size();

		TreeDiffFormatBuilder unifiedrep = new TreeDiffFormatBuilder();
		JsonElement jsonunif = unifiedrep.build(new JsonObject(), new JsonObject(), diffgtt, new JsonObject());

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		String json = gson.toJson(jsonunif);

		String hashJson = toHexString(getSHA(json));

		File caseFolder = new File(
				parentDiff.getAbsolutePath() + File.separator + this.id + File.separator + PREFIX_TREEDIFF_FORMAT);
		caseFolder.mkdirs();

		if (hashes.containsKey(hashJson)) {

			// System.out.println("Existing Json");
			equivalent.add(hashes.get(hashJson), key);

		} else {
			// System.out.println("New Json");
			hashes.put(hashJson, key);
			equivalent.add(key, key);

			File uniflFile = new File(caseFolder + File.separator + PREFIX_TREEDIFF_FORMAT + key + ".json");
			if (zipped) {
				FileOutputStream fos = new FileOutputStream(
						caseFolder + File.separator + PREFIX_TREEDIFF_FORMAT + key + ".zip");
				ZipOutputStream zipOut = new ZipOutputStream(fos);
				ZipEntry zipEntry = new ZipEntry(uniflFile.getName());
				zipOut.putNextEntry(zipEntry);
				zipOut.write(json.getBytes());
				zipOut.close();
			} else {

				FileWriter fw = new FileWriter(uniflFile);
				fw.write(json);
				fw.close();

			}

		}
	}

	public void saveUnified(String filename, String plainProperties, com.github.gumtreediff.actions.Diff diffgtt,
			File parentDiff) throws IOException, NoSuchAlgorithmException {

		filename = new File(filename).getName();

		String key = filename.replace("/", "_") + "_c_" + plainProperties;

		TreeDiffFormatBuilder unifiedrep = new TreeDiffFormatBuilder();
		JsonElement jsonunif = unifiedrep.build(new JsonObject(), new JsonObject(), diffgtt, new JsonObject());

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		String json = gson.toJson(jsonunif);

		File caseFolder = new File(
				parentDiff.getAbsolutePath() + File.separator + this.id + File.separator + PREFIX_TREEDIFF_FORMAT);
		caseFolder.mkdirs();

		File uniflFile = new File(caseFolder + File.separator + PREFIX_TREEDIFF_FORMAT + key + ".json");
		if (zipped) {
			FileOutputStream fos = new FileOutputStream(
					caseFolder + File.separator + PREFIX_TREEDIFF_FORMAT + key + ".zip");
			ZipOutputStream zipOut = new ZipOutputStream(fos);
			ZipEntry zipEntry = new ZipEntry(uniflFile.getName());
			zipOut.putNextEntry(zipEntry);
			zipOut.write(json.getBytes());
			zipOut.close();
		} else {

			FileWriter fw = new FileWriter(uniflFile);
			fw.write(json);
			fw.close();
		}

	}

	public void saveRelations(File parentDiff) throws IOException, NoSuchAlgorithmException {

		File caseFolder = new File(
				parentDiff.getAbsolutePath() + File.separator + this.id + File.separator + PREFIX_FILE_SUMMARY_JSON);
		caseFolder.mkdirs();

		for (String xRquivalen : this.equivalent.keySet()) {

			File uniflFile = new File(caseFolder + File.separator + PREFIX_FILE_SUMMARY_JSON + xRquivalen + ".txt");

			List<String> equiv = this.equivalent.get(xRquivalen);
			String content = "";
			for (String string : equiv) {
				content += (string);
				content += ("\n");
			}
			if (zipped) {
				FileOutputStream fos = new FileOutputStream(
						caseFolder + File.separator + PREFIX_FILE_SUMMARY_JSON + xRquivalen + ".zip");
				ZipOutputStream zipOut = new ZipOutputStream(fos);
				ZipEntry zipEntry = new ZipEntry(uniflFile.getName());
				zipOut.putNextEntry(zipEntry);
				zipOut.write(content.getBytes());
				zipOut.close();
			} else {
				FileWriter fw = new FileWriter(uniflFile);
				fw.write(content);
				fw.close();
			}
		}

	}

	public void saveSummarization(File parentDiff, ResultByConfig result) throws Exception {

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonArray allc = new JsonArray();
		for (String key : result.keySet()) {

			JsonObject resultConfig = new JsonObject();
			resultConfig.addProperty(CONFIGURATION, key);
			JsonArray arry = new JsonArray();
			for (Integer i : result.get(key))
				arry.add(i);

			resultConfig.add(ED_SIZE, arry);
			allc.add(resultConfig);
		}

		String content = gson.toJson(allc);

		File caseFolder = new File(parentDiff.getAbsolutePath() + File.separator + this.id);
		caseFolder.mkdirs();

		File uniflFile = new File(caseFolder + File.separator + RESULTS_JSON + this.id + ".json");

		if (zipped) {
			FileOutputStream fos = new FileOutputStream(caseFolder + File.separator + RESULTS_JSON + this.id + ".zip");
			ZipOutputStream zipOut = new ZipOutputStream(fos);
			ZipEntry zipEntry = new ZipEntry(uniflFile.getName());
			zipOut.putNextEntry(zipEntry);
			zipOut.write(content.getBytes());
			zipOut.close();
		} else {
			FileWriter fw = new FileWriter(uniflFile);
			fw.write(content);
			fw.close();
		}

	}

	public void saveUnifiedComplete(String configId, com.github.gumtreediff.actions.Diff diffgtt, File parentDiff)
			throws IOException {
		TreeDiffFormatBuilder unifiedrep = new TreeDiffFormatBuilder();
		JsonElement jsonunif = unifiedrep.build(diffgtt);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		String json = gson.toJson(jsonunif);

		File caseFolder = new File(
				parentDiff.getAbsolutePath() + File.separator + this.id + File.separator + PREFIX_TREEDIFF_FORMAT);
		caseFolder.mkdirs();

		File uniflFile = new File(caseFolder + File.separator + PREFIX_TREEDIFF_FORMAT + configId + ".json");
		FileWriter fw = new FileWriter(uniflFile);
		fw.write(json);
		fw.close();
	}

	public static byte[] getSHA(String input) throws NoSuchAlgorithmException {
		// Static getInstance method is called with hashing SHA
		MessageDigest md = MessageDigest.getInstance("SHA-256");

		return md.digest(input.getBytes(StandardCharsets.UTF_8));
	}

	public static String toHexString(byte[] hash) {
		// Convert byte array into signum representation
		BigInteger number = new BigInteger(1, hash);

		// Convert message digest into hex value
		StringBuilder hexString = new StringBuilder(number.toString(16));

		// Pad with leading zeros
		while (hexString.length() < 32) {
			hexString.insert(0, '0');
		}

		return hexString.toString();
	}

	public String unzipFolder(Path source) throws IOException {
		String out = "";
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(source.toFile()))) {
			ZipEntry zipEntry = zis.getNextEntry();

			while (zipEntry != null) {

				StringBuffer d = new StringBuffer();
				int len;
				byte[] buffer = new byte[1024];
				while ((len = zis.read(buffer)) > 0) {
					d.append(new String(buffer), 0, len);
				}

				out = d.toString();

				zipEntry = zis.getNextEntry();

			}
			zis.closeEntry();

		}
		System.out.println(out);
		return out;
	}

	// protect zip slip attack
	public static Path zipSlipProtect(ZipEntry zipEntry, Path targetDir) throws IOException {

		// test zip slip vulnerability
		// Path targetDirResolved = targetDir.resolve("../../" + zipEntry.getName());

		Path targetDirResolved = targetDir.resolve(zipEntry.getName());

		// make sure normalized file still has targetDir as its prefix
		// else throws exception
		Path normalizePath = targetDirResolved.normalize();
		if (!normalizePath.startsWith(targetDir)) {
			throw new IOException("Bad zip entry: " + zipEntry.getName());
		}

		return normalizePath;
	}

	public void readAndAdd(ResultByConfig results, File zip) throws IOException {

		String outJson = unzipFolder(zip.toPath());

		JsonElement jsonElement = new JsonParser().parse(outJson);

		readResultByConfig(results, jsonElement);

	}

	public void readResultByConfig(ResultByConfig results, JsonElement result) {

		JsonArray arry = result.getAsJsonArray();

		for (JsonElement config : arry) {

			String key = config.getAsJsonObject().get(DatOutputEngine.CONFIGURATION).getAsString();
			JsonArray values = config.getAsJsonObject().get(DatOutputEngine.ED_SIZE).getAsJsonArray();

			for (JsonElement v : values) {
				results.add(key, v.getAsInt());
			}
		}

	}

	/**
	 * Store the data in a csv file
	 * 
	 * @param out
	 * @param fileresult
	 * @param astmodel
	 * @throws IOException
	 */
	public static void executionResultToCSV(File out, CaseResult fileresult) throws IOException {

		String sep = ",";
		String endline = "\n";
		String header = "";

		Collection<MatcherResult> matchers = fileresult.getResultByMatcher().values();

		if (matchers == null) {
			System.err.println("Problems when saving results: No matchers for identifier " + out.getName());
			return;
		}

		String row = "";
		boolean first = true;

		StringBuffer bufferS = new StringBuffer();

		for (MatcherResult map : matchers) {

			if (map == null || map.getMatcher() == null) {
				System.out.println("No matcher in results ");
				continue;
			}

			String xmatcher = map.getMatcherName().toString();

			List<SingleDiffResult> configs = (List<SingleDiffResult>) map.getAlldiffresults();
			for (SingleDiffResult config : configs) {
				// re-init the row

				if (config == null)
					continue;

				GumtreeProperties gtp = (config.containsKey(Constants.CONFIG))
						? (GumtreeProperties) config.get(Constants.CONFIG)
						: new GumtreeProperties();

				row = config.retrievePlainConfiguration() + sep;
				row += xmatcher + sep;

				if (config.get(Constants.TIMEOUT) != null) {

					row += "" + sep;

					row += "" + sep;

					//
					row += "" + sep;
					row += "" + sep;
					row += "" + sep;
					row += "" + sep;
					row += "" + sep;
					row += "" + sep;
					//
					row += "" + sep;

					row += "" + sep;

					// TIMEout
					row += config.get(Constants.TIMEOUT) + sep;

				} else {

					row += config.get(Constants.NRACTIONS) + sep;

					row += config.get(Constants.NRROOTS) + sep;

					//
					row += config.get(Constants.NR_INSERT) + sep;
					row += config.get(Constants.NR_DELETE) + sep;
					row += config.get(Constants.NR_UPDATE) + sep;
					row += config.get(Constants.NR_MOVE) + sep;
					row += config.get(Constants.NR_TREEINSERT) + sep;
					row += config.get(Constants.NR_TREEDELETE) + sep;

					//
					row += config.get(Constants.TIME) + sep;

					row += 0 + sep;// gtp.getProperties().keySet().size()
					// TIMEout
					row += "0" + sep;
				}
				if (first) {
					header += Constants.PLAIN_CONFIGURATION + sep;
					header += Constants.MATCHER + sep;
					header += Constants.NRACTIONS + sep;
					header += Constants.NRROOTS + sep;

					header += Constants.NR_INSERT + sep;
					header += Constants.NR_DELETE + sep;
					header += Constants.NR_UPDATE + sep;
					header += Constants.NR_MOVE + sep;
					header += Constants.NR_TREEINSERT + sep;
					header += Constants.NR_TREEDELETE + sep;

					header += Constants.TIME + sep;
					header += "NROPTIONS" + sep;
					header += Constants.TIMEOUT + sep;

				}

				for (ConfigurationOptions confOption : ConfigurationOptions.values()) {
					if (first) {
						header += confOption.name() + sep;

					}
					row += ((gtp.get(confOption) != null) ? gtp.get(confOption) : "") + sep;

				}

				if (first) {
					header += endline;
					first = false;
					// fw.write(header);
					bufferS.append(header);
				}

				row += endline;
				// fw.write(row);
				// fw.flush();
				bufferS.append(row);

			}

		}

		//

		if (zipped) {
			File fzipper = new File(out.getAbsolutePath().replace(".csv", ".zip"));
			FileOutputStream fos = new FileOutputStream(fzipper);
			ZipOutputStream zipOut = new ZipOutputStream(fos);
			ZipEntry zipEntry = new ZipEntry(out.getName());
			zipOut.putNextEntry(zipEntry);
			zipOut.write(bufferS.toString().getBytes());
			zipOut.close();
		} else {
			FileWriter fw = new FileWriter(out);
			fw.close();

		}

		//

		System.out.println("Saved file " + out.getAbsolutePath());

	}

	public static void metadataToCSV(File nameFile, Map<String, Pair<Map, Map>> treeProperties, CaseResult fileResult,
			Matcher[] matchers) throws IOException {

		String sep = ",";
		String endline = "\n";
		String header = "DIFFID" + sep + "L_" + Constants.SIZE + sep + "L_" + Constants.HEIGHT + sep + "L_"
				+ Constants.STRUCTHASH + sep + "R_" + Constants.SIZE + sep + "R_" + Constants.HEIGHT + sep + "R_"
				+ Constants.STRUCTHASH + sep + Constants.TIME_TREES_PARSING + sep + Constants.TIME_ALL_MATCHER_DIFF;

		for (Matcher matcher : matchers) {
			header += (sep + matcher.getClass().getSimpleName());
		}

		header += endline;

		String row = "";
		Collection<MatcherResult> matchersInfo = fileResult.getResultByMatcher().values();

		if (matchersInfo == null) {
			System.err.println("Problems when saving results: No matchers for identifier " + nameFile.getName());
			return;
		}

		for (String id : treeProperties.keySet()) {

			Pair<Map, Map> t = treeProperties.get(id);
			row += id + sep;
			row += t.first.get(Constants.SIZE) + sep;
			row += t.first.get(Constants.HEIGHT) + sep;
			row += t.first.get(Constants.STRUCTHASH) + sep;
			row += t.second.get(Constants.SIZE) + sep;
			row += t.second.get(Constants.HEIGHT) + sep;
			row += t.second.get(Constants.STRUCTHASH) + sep;

			// Times:

			row += fileResult.getTimeParsing() + sep;
			row += fileResult.getTimeMatching() + sep;

			for (Matcher matcher : matchers) {
				Optional<MatcherResult> findFirst = matchersInfo.stream()
						.filter(e -> e.getMatcherName().equals(matcher.getClass().getSimpleName())).findFirst();
				if (findFirst.isPresent()) {
					MatcherResult pM = findFirst.get();

					row += pM.getTimeAllConfigs() + sep;
				} else {
					row += "" + sep;
				}
			}
			row += endline;
		}

		FileWriter fw = new FileWriter(nameFile);
		fw.write(header + row);
		fw.close();

	}

}
