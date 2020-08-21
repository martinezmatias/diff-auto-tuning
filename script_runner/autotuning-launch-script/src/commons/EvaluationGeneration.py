import os

martinJDT=[
'26_08971b145eb2e3bd314b8ba833da2b34f213f99a_DispatchBParser',
'4_01d8ec68011387017898b90695b84caa0508597c_DoubleMatrix',
'4_01d8ec68011387017898b90695b84caa0508597c_FloatMatrix',
'22_235f25d1f42e401ada75c753c4644748b2bf1a79_XMLMessages',
'8_0e0143af62348006a08f000c863bbd4a3e8ee19d_SipStackImpl',
'9_04349a03c81e7d12670abd20f088be0e6a693761_ValangParserTokenManager',
'3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions',
'8_0c500246640d65ce8940c83704539e2de43a1bba_CoPIGrantCountQueryRunner',
'16_2147fe5aa47f7520756d139534f86e0e934e2e6d_DateTimeFormatters',
'26_327fb79aa10b01b6cc53bbc31ce244583a8e1119_NativeString',
'40_74e7382ef413900f9d650a59a6dde7c5eebcefc6_Inventory',
'32_1efdf68089db048859f659ebf5656da8fad592d9_Long',
'32_9e13cc8ad982ba191400889bb8e5248a1933d45e_Long',
'11_140ffc5c5eebfdfda20246cb2af6c957f5c7137c_HomeHelper',
'33_6e79dbbb40cfd509f5eb73eb245f081566601a0e_R',
'36_dd9b3d812674c61274ae5b8eb3bdd8b2ec778b82_CompilerConfiguration',
'33_24784d74a8a7298fe5fcff3375b214742db26b45_R',
'10_142367b24be1f77592c1b9799448d3e975d31570_DateTimeFormatters',
'31_10a51e3b6822814227d4317c8b05e8dad891b089_R',
'34_1bb9b571f1251877394f1f5200e4be45abeb30a5_IContentTypeManagerTest',
'34_9719a19b96ff79309402a8dacf833364c171f4ad_IContentTypeManagerTest',
'10_142367b24be1f77592c1b9799448d3e975d31570_DateTimeFormatterBuilder',
'28_1c6bae6d5ea58cc8dcdff9f97c58372e9ec6be9c_JXTable',
'20_0e473e9c92defefffa5e300ffe2ad033904f872f_KeyboardTextsSet',
'20_14bc2e89efed136a8f351e6e407be3ec2d7a3308_KeyboardTextsSet',
'14_175f5cfbbcbce775b4a8b008f5bebe33477f75b2_TransformedImageDisplay',
'15_15aeae14a79ca02d9fc4c4a9c155b17210dd69e9_MonkeySourceRandom',
'16_1dcb42802a910007281b243c73dc491f63b0caa3_FieldProgramParser',
'14_1bd9420e2131e2cf02b1860de1e33b0ba9546703_Interpreter',
'16_05b75ea2e087f5e7615089b2828f1d72589d2fb2_BSuggPathBasedtxtFn',

]
def navigate(rootResults, model):
	pairfiles = (os.listdir(rootResults))
	print("Pair_ID;Best Configuration;#ActionBest;Default Configuration;#ActionDefault;Text DIFF;ACTIONS BEST (TextualDiff);ACTIONS Default (TextualDiff);Is Best better than Default? (Yes/No/no clear difference); Comment;Good Example?;")
	for pairfile in pairfiles:
		if ".DS_Store" == pairfile:
			continue

		bestDiff = None
		defaultDiff = None
		bestText = None
		defaultText = None
		generatedFiles = (os.listdir(os.path.join(rootResults,pairfile)))

		actionsBestJSON = None
		actionsDefaultJSON = None

		if pairfile not in martinJDT:
			continue

		for iFile in generatedFiles:

			if ".DS_Store" == iFile:
				continue

			if "textDiffView_default" in iFile  and not "textDiffView_default_noparam.html" in iFile:
				defaultText = iFile

			if "textDiffView_best_" in iFile:
				bestText = iFile

			if "vanillaDiffView_best" in iFile:
				bestDiff = iFile

			if "vanillaDiffView_default" in iFile and  not "vanillaDiffView_default_noparam" in iFile :
				defaultDiff = iFile

			if "treeDiffSerialFormat_best_" in iFile:
				actionsBestJSON = iFile

			if "treeDiffSerialFormat_default" in iFile and not "treeDiffSerialFormat_default_noparam" in iFile:
				actionsDefaultJSON = iFile

		print("{};{};{};{};{};{};{};{}".format(pairfile,createLink(bestDiff, pairfile, model,"vanillaDiffView_best_"  ),createLink2(actionsBestJSON,pairfile,  model, "Actions"), createLink(defaultDiff,pairfile,  model, "vanillaDiffView_default_"), createLink2(actionsDefaultJSON,pairfile,  model, "Actions"),createLink("diff.txt",pairfile,  model, ""),  createLink(bestText, pairfile, model, "textDiffView_best_"), createLink(defaultText, pairfile, model, "textDiffView_default_")))

def createLink(text,file,  model, key):
	return "=HYPERLINK(CONCAT(CONCAT(\"{}\",\"{}\"),\"/{}\"),\"{}\")".format(model,file,text,text.replace(key,""))

def createLink2(text,file,  model, key):
	return "=HYPERLINK(CONCAT(CONCAT(\"{}\",\"{}\"),\"/{}\"),\"{}\")".format(model,file,text,key)

#=HYPERLINK(CONCAT(CONCAT("https://",CONFIG!A1),"/vanillaDiffView_best_ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1900@GT_STM_MH@1.html"),"vanillaDiffView_best_ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1900@GT_STM_MH@1.html")
#=HYPERLINK(CONCAT(CONCAT("https://",CONFIG!A1),"/vanillaDiffView_best_ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1900@GT_STM_MH@1.html"),"vanillaDiffView_best_ClassicGumtree@GT_BUM_SMT@0.1@GT_BUM_SZT@1900@GT_STM_MH@1.html")
#navigate("/Users/matias/develop/gt-tuning/git-appendix/evaluation/jeanremy/casesSpoon", "https://uphf.github.io/dat/evaluation/jeanremy/casesSpoon/")
#navigate("/Users/matias/develop/gt-tuning/git-appendix/evaluation/jeanremy/casesJDT", "https://uphf.github.io/dat/evaluation/jeanremy/casesJDT/")
#navigate("/Users/matias/develop/gt-tuning/git-appendix/evaluation/martin/casesSpoon", "https://uphf.github.io/dat/evaluation/martin/casesSpoon/")
navigate("/Users/matias/develop/gt-tuning/git-appendix/evaluation/martin/casesJDT", "https://uphf.github.io/dat/evaluation/martin/casesJDT/")
#navigate("/Users/matias/develop/gt-tuning/git-appendix/evaluation/matias/casesSpoon", "https://uphf.github.io/dat/evaluation/matias/casesSpoon/")
#navigate("/Users/matias/develop/gt-tuning/git-appendix/evaluation/matias/casesJDT", "https://uphf.github.io/dat/evaluation/matias/casesJDT/")
