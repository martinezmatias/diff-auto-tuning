
CHANGE_DISTILLER = "ChangeDistiller"

CompleteGT = "CompleteGumtreeMatcher"

ClasicGT = "ClassicGumtree"

SimpleGT = "SimpleGumtree"

XyMatcher = "XyMatcher"


LIST_DIFF_ALGO = [SimpleGT,ClasicGT, CompleteGT, CHANGE_DISTILLER,
				  XyMatcher]

propertiesPerMatcher = {}
propertiesPerMatcher["SimpleGumtree"] = ["GT_BUM_SMT_SBUP", "GT_STM_MH"]
propertiesPerMatcher["ClassicGumtree"] = ["GT_BUM_SMT", "GT_BUM_SZT", "GT_STM_MH"]
propertiesPerMatcher["CompleteGumtreeMatcher"] = ["GT_BUM_SMT", "GT_BUM_SZT", "GT_STM_MH"]
propertiesPerMatcher["ChangeDistiller"] = ["GT_CD_LSIM", "GT_CD_ML","GT_CD_SSIM1",  "GT_CD_SSIM2"]
propertiesPerMatcher["XyMatcher"] = ["GT_STM_MH", "GT_XYM_SIM"]

defaultConfigurations = {
	#"SimpleGumtree":"SimpleGumtree_0.4_2", ##as the threshold does not count, we use 0.5, which we have computed it
"SimpleGumtree":"SimpleGumtree_0.5_2",
"ClassicGumtree":"ClassicGumtree_0.5_1000_1"	, ## "CompleteGumtreeMatcher_0.5_1000_2", GT2 uses H = 1 , GT 3 uses 2
"CompleteGumtreeMatcher": "CompleteGumtreeMatcher_0.5_1000_1",#"CompleteGumtreeMatcher_0.5_1000_2",
"ChangeDistiller": "ChangeDistiller_0.5_4_0.6_0.4",
"XyMatcher": "XyMatcher_2_0.5"
			}


'''Returns a key for the configuration. It's the combination of the values for each property
The CSV does not have a key: it has columns where the concrete values or null.
Thus, this method creates the key according to the metadata
'''
def getConfigurationKeyFromCSV(row, indexesOfPropertiesOnTable):

		matcherName = row.MATCHER
		key = matcherName
		## for each property related to the matcher
		for property in propertiesPerMatcher[matcherName]:
			# get the position of that property in the
			index = indexesOfPropertiesOnTable[property]
			#Get the value on that index
			valueOfProperty = row[index]

			key+="_"+"{:.1f}".format((valueOfProperty)).rstrip('0').rstrip('.')

		return key

def createCompleteKey(configId = "", algo = None):
	#print("\nconfig {}".format(configId))
	properties = propertiesPerMatcher[algo]

	#print("\nconfig {} properties {}" .format(configId, properties))
	values = configId.split("_")
	r="{}".format(algo)
	for i in range(0, len(properties)):
		r+="@{}@{}".format(properties[i],values[i+1])

	return r