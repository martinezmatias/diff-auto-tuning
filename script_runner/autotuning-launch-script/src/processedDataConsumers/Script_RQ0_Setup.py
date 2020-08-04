import unittest
from src.rowDataConsumers.ResultsCountPairsAnalyzed import *
from src.commons.DatasetMerger import *
class MyTestCase(unittest.TestCase):



	def _test_countAnalyzed(self):
		countFilePairsAnalyzed("../../results/outCDJDT_4/")
		countFilePairsAnalyzed("../../results/outCD_5/")
		countFilePairsAnalyzed("../../results/outgtJDT_5/")
		countFilePairsAnalyzed("../../results/outgt6/")


	def _test_mergedatasetsSpoon(self):
		merge(location1="/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/outgt6/",
			location2="/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/outCD_5",
			destination="/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/merge_gt6_cd_5")

	def _test_mergedatasetsJDT(self):
		merge(location1="/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/outgtJDT_5/",
			location2="/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/outCDJDT_4",
			destination="/Users/matias/develop/gt-tuning/git-code-gpgt/script_runner/autotuning-launch-script/results/merge_gtJDT_5_CDJDT_4")

if __name__ == '__main__':
	unittest.main()
