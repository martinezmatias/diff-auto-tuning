package fr.gumtree.autotuning.entity;

import java.util.List;

import com.github.gumtreediff.matchers.Matcher;

/**
 * Entity that stores the results of a matcher
 * 
 * @author Matias Martinez
 *
 */
public class MatcherResult {

	String matcherName;

	Matcher matcher;

	List<SingleDiffResult> alldiffresults;

	long timeAllConfigs;

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String getMatcherName() {
		return matcherName;
	}

	public void setMatcherName(String matcherName) {
		this.matcherName = matcherName;
	}

	public List<SingleDiffResult> getAlldiffresults() {
		return alldiffresults;
	}

	public void setAlldiffresults(List<SingleDiffResult> alldiffresults) {
		this.alldiffresults = alldiffresults;
	}

	public long getTimeAllConfigs() {
		return timeAllConfigs;
	}

	public void setTimeAllConfigs(long timeAllConfigs) {
		this.timeAllConfigs = timeAllConfigs;
	}

	public Matcher getMatcher() {
		return matcher;
	}

	public void setMatcher(Matcher matcher) {
		this.matcher = matcher;
	}

}
