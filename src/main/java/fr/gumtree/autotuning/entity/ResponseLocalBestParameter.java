package fr.gumtree.autotuning.entity;

/**
 * 
 * @author Matias Martinez
 *
 */
public class ResponseLocalBestParameter extends ResponseBestParameter {

	// For each file, results of best vs another default
	// store best length and default (or another)

	// store the most frequent

	public String targetConfig;

	public String getTargetConfig() {
		return targetConfig;
	}

	public void setTargetConfig(String targetConfig) {
		this.targetConfig = targetConfig;
	}

}
