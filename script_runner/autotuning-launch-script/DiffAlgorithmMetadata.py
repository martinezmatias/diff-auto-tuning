
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
	"SimpleGumtree":"SimpleGumtree_0.4_2",
"ClassicGumtree":"ClassicGumtree_0.5_1000_2"	,
"CompleteGumtreeMatcher":"CompleteGumtreeMatcher_0.5_1000_2",
"ChangeDistiller": "ChangeDistiller_0.5_4_0.6_0.4",
"XyMatcher": "XyMatcher_2_0.5"
			}