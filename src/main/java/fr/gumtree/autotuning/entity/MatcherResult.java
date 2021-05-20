package fr.gumtree.autotuning.entity;

import java.util.List;

import com.github.gumtreediff.matchers.Matcher;

import fr.gumtree.autotuning.outils.Constants;

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

	public MatcherResult(String matcherName, Matcher matcher) {
		super();
		this.matcherName = matcherName;
		this.matcher = matcher;
	}

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
		for (SingleDiffResult singleDiffResult : alldiffresults) {
			singleDiffResult.put(Constants.MATCHER, this.matcherName);
		}
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
