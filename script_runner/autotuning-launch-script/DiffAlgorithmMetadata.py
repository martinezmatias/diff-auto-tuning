
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