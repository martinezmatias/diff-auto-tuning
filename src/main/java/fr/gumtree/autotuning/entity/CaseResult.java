package fr.gumtree.autotuning.entity;

import java.util.HashMap;
import java.util.Map;

import com.github.gumtreediff.matchers.Matcher;

/**
 * Entity that stores the results of one case (file-pair)
 * 
 * @author Matias Martinez
 *
 */
public class CaseResult {

	Map<Matcher, MatcherResult> resultByMatcher = new HashMap<>();
	long timeParsing;
	long timeMatching;
	String fileName;
	String commit;
	// MEGADIFFSET
	String datasubset;
	Exception fromException;

	public Map<Matcher, MatcherResult> getResultByMatcher() {
		return resultByMatcher;
	}

	public void setResultByMatcher(Map<Matcher, MatcherResult> resultByMatcher) {
		this.resultByMatcher = resultByMatcher;
	}

	public long getTimeParsing() {
		return timeParsing;
	}

	public void setTimeParsing(long timeParsing) {
		this.timeParsing = timeParsing;
	}

	public long getTimeMatching() {
		return timeMatching;
	}

	public void setTimeMatching(long timeMatching) {
		this.timeMatching = timeMatching;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getCommit() {
		return commit;
	}

	public void setCommit(String commit) {
		this.commit = commit;
	}

	public String getDatasubset() {
		return datasubset;
	}

	public void setDatasubset(String datasubset) {
		this.datasubset = datasubset;
	}

	public Exception getFromException() {
		return fromException;
	}

	public void setFromException(Exception fromException) {
		this.fromException = fromException;
	}

	@Override
	public String toString() {
		return "CaseResult [Nr resultByMatcher=" + resultByMatcher.values().size() + ", fileName=" + fileName
				+ ", commit=" + commit + ", datasubset=" + datasubset + "]";
	}

}
