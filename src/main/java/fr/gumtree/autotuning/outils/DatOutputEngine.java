package fr.gumtree.autotuning.outils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import fr.gumtree.autotuning.searchengines.MapList;
import fr.gumtree.autotuning.searchengines.ResultByConfig;
import fr.gumtree.treediff.jdt.TreeDiffFormatBuilder;

/**
 * 
 * @author Matias Martinez
 *
 */
public class DatOutputEngine {

	private static final String PREFIX_TREEDIFF_FORMAT = "treeDiffSerialFormat_";

	private static final String PREFIX_FILE_SUMMARY_JSON = "equivalents_";

	private static final String RESULTS_JSON = "result_size_per_config_";

	private MapList<String, String> equivalent = new MapList<String, String>();

	private boolean zipped = true;

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

		String key = filename.replace("/", "_") + "_c_" + plainProperties;

		TreeDiffFormatBuilder unifiedrep = new TreeDiffFormatBuilder();
		JsonElement jsonunif = unifiedrep.build(new JsonObject(), new JsonObject(), diffgtt, new JsonObject());

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		String json = gson.toJson(jsonunif);

		String hashJson = toHexString(getSHA(json));

		if (hashes.containsKey(hashJson)) {

			// System.out.println("Existing Json");
			equivalent.add(hashes.get(hashJson), key);

		} else {
			// System.out.println("New Json");
			hashes.put(hashJson, key);
			equivalent.add(key, key);

			File uniflFile = new File(
					parentDiff.getAbsolutePath() + File.separator + this.id + PREFIX_TREEDIFF_FORMAT + key + ".json");
			if (zipped) {
				FileOutputStream fos = new FileOutputStream(parentDiff.getAbsolutePath() + File.separator + this.id
						+ PREFIX_TREEDIFF_FORMAT + key + ".zip");
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
		File uniflFile = new File(
				parentDiff.getAbsolutePath() + File.separator + this.id + PREFIX_TREEDIFF_FORMAT + key + ".json");
		if (zipped) {
			FileOutputStream fos = new FileOutputStream(
					parentDiff.getAbsolutePath() + File.separator + this.id + PREFIX_TREEDIFF_FORMAT + key + ".zip");
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

		for (String xRquivalen : this.equivalent.keySet()) {

			File uniflFile = new File(parentDiff.getAbsolutePath() + File.separator + this.id + PREFIX_FILE_SUMMARY_JSON
					+ xRquivalen + ".txt");

			List<String> equiv = this.equivalent.get(xRquivalen);
			String content = "";
			for (String string : equiv) {
				content += (string);
				content += ("\n");
			}
			if (zipped) {
				FileOutputStream fos = new FileOutputStream(parentDiff.getAbsolutePath() + File.separator + this.id
						+ PREFIX_FILE_SUMMARY_JSON + xRquivalen + ".zip");
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

	public void save(File parentDiff, ResultByConfig result) throws Exception {

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonArray allc = new JsonArray();
		for (String key : result.keySet()) {

			JsonObject resultConfig = new JsonObject();
			resultConfig.addProperty("prop", key);
			JsonArray arry = new JsonArray();
			for (Integer i : result.get(key))
				arry.add(i);

			resultConfig.add("p", arry);
			allc.add(resultConfig);
		}

		String content = gson.toJson(allc);

		File uniflFile = new File(parentDiff.getAbsolutePath() + File.separator + this.id + RESULTS_JSON + ".json");

		if (zipped) {
			FileOutputStream fos = new FileOutputStream(
					parentDiff.getAbsolutePath() + File.separator + this.id + RESULTS_JSON + ".zip");
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

	public static void saveUnifiedComplete(String configId, com.github.gumtreediff.actions.Diff diffgtt,
			File parentDiff) throws IOException {
		TreeDiffFormatBuilder unifiedrep = new TreeDiffFormatBuilder();
		JsonElement jsonunif = unifiedrep.build(diffgtt);

		Gson gson = new GsonBuilder().setPrettyPrinting().create();

		String json = gson.toJson(jsonunif);

		File uniflFile = new File(
				parentDiff.getAbsolutePath() + File.separator + PREFIX_TREEDIFF_FORMAT + configId + ".json");
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

}
