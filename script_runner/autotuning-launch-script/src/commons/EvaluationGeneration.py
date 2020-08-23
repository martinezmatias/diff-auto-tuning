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

martinSpoon=[
'1_16c59fbd6408fc407a393351aff75a873a7add56_CommandLineRunner',
'1_22a9d9b39597cfe4ca8e77275aa54ef56d4e56a8_TwoPaneSelectionPanel',
'1_196112702a87a78214f80516ef691d21c9e38c34_CommandLineRunner',
'4_065c3b8e8b2191a3f3230ce0d92de4cf8af5285b_LookUpTable',
'6_036a9db5fe2d9a057fabc810013836a1c82e2d4b_Node',
'7_00e3cf2cc304476d7b856ebb8eb58d386a82face_SQLitePlugin',
'10_0f1e0480a113bdd91859f45ea0ebd152814da9c3_Util',
'10_08e547745fc6e423eb71a8da88639003d0d5d027_DBConnection',
'11_0782cf32c73c9df2f8fb91c80f8973525d167b65_BOPCrafting',
'12_127c77864449b011f58e439f3f8d99096dd72446_ParserImpl',
'13_13a388247255f822536cd1e06c90bf41907b8879_CubePuzzle',
'15_22577bd30a63d86fbfb69c510886e0e92fa35c4a_BlacklistDialogue',
'16_1af106f251b4a6ce7fa8632d67abe44034d487e1_SpoutClient',
'17_209ffc88961a36ed0ec878becdf9871297989a56_MiscTest',
'19_039e06fdce5d43fc220a887e59555c64911c5286_GridDatasetInfo',
'20_1a0c8b25b6f8a51506845818671013fddd62c0a2_Main',
'20_16f0fdfd59aa4dc8799ab670a98110b8ce129ff9_TestSamplesLoader',
'22_08358b39668c24f824d0eac8d021cf652f933a27_EncodingMap',
'24_24be1e5763437bf5aea09e71aadb7a80160f1794_Serializer',
'24_123f62e20471b57b85f9d86af2e00eb3650aef55_DBSSql',
'26_07dab009ba690ebba019fbfaa1be784187b3d568_TechEditWizardData',
'26_40d3f145b833c2dd25f02e6ea543f926e5ac38bd_ReplicaDbV1',
'28_3cd5c04e548142ef3dc562194a469ed1dc7c4ae2_CalendarApplication',
'28_4c581249602c2ffba538ee55cd31c6458ef13432_ActivityChooserModel',
'29_1f1dc0af2c01f2a2b6cf4907ecb21b4e05982e2b_SSLSocketTest',
'29_543d0eb5dae5148c8148ca6abad8c806f7b4f4ad_SSLSocketTest',
'29_54195c894ffefcea20ce06fa90559afef0b20c16_PersonHasAdvisorRelationshipGenerator',
'31_3daa10c4d2392697e6c3827ce2e70af9257086ae_MasterInfo',
'31_596d05e3983e25546f3d4d4d9f974f77157c7d5e_Simulator',
'38_fbe9017ef3b18f4096cc2ee3d1df5a4707483597_BuildpathsBlock',

]

jrJDT=[
'2_03b1dec4d20cee110b68cf8325f28f4403468317_FTPClient',
'3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions',
'3_09e29cf8903af739223e76bd81cdd1cc99847aa1_SimpleDateFormat',
'4_0a3726b153cfca74af11cec28c46279d46019e13_BaseDataChip',
'4_01d8ec68011387017898b90695b84caa0508597c_DoubleMatrix',
'4_01d8ec68011387017898b90695b84caa0508597c_FloatMatrix',
'5_0c7c39cce1c6f052cd2afa4fc1ad99de70b37dd6_Formatter',
'6_025b151897ce68bb6571a56facea4296936c458d_CreateSAML2HostedProviderTemplate',
'8_0c500246640d65ce8940c83704539e2de43a1bba_CoPIGrantCountQueryRunner',
'8_0e0143af62348006a08f000c863bbd4a3e8ee19d_SipStackImpl',
'8_01ec8332c1ff2c5c13df58df7f96c891b5f73169_SimpleDateFormat',
'8_050623494a4ba889b8d5f86fa83d5e8fedc05e50_PropertyConfigurator',
'9_04349a03c81e7d12670abd20f088be0e6a693761_ValangParserTokenManager',
'10_12aebda5c29a97d3a7a4d4d513aaa60d397d5515_ClientResources',
'11_0a33a79a61928e124ce79e3aa25267d7e85d675a_DateTimeFormat',
'11_140ffc5c5eebfdfda20246cb2af6c957f5c7137c_HomeHelper',
'11_0965fe2c1f1d9870d2066d506ec5371b767f9b65_ClickingButtons',
'16_0151b0430a725a556dee417c5e8a5da347f71b3e_IResource',
'16_2147fe5aa47f7520756d139534f86e0e934e2e6d_DateTimeFormatters',
'20_1b862fb2c74e974d7ec30f26a34403a1f29af873_Vector2f',
'22_235f25d1f42e401ada75c753c4644748b2bf1a79_XMLMessages',
'22_12089f30b8c2c15d7453f4467b4a3e91cee85e9d_Mockito',
'24_0e56f33fc781fc3a3dc61d4b9ecb07755f0d2dfa_FormAction',
'26_327fb79aa10b01b6cc53bbc31ce244583a8e1119_NativeString',
'26_08971b145eb2e3bd314b8ba833da2b34f213f99a_DispatchBParser',
'32_1efdf68089db048859f659ebf5656da8fad592d9_Long',
'32_9e13cc8ad982ba191400889bb8e5248a1933d45e_Long',
'32_35c90dff5f4d6b5f3d3fffd8a3871e22596f00fa_CssParser',
'40_74e7382ef413900f9d650a59a6dde7c5eebcefc6_Inventory',

]

jrSpoon=['3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions',
'4_007922de19cb8ea011e4e8431e3ae9c1d4d23807_Translations',
'7_0f69892a76e0703470b1d26be92d4ca3134cfba8_JdbcTest',
'10_0f1e0480a113bdd91859f45ea0ebd152814da9c3_Util',
'10_08e547745fc6e423eb71a8da88639003d0d5d027_DBConnection',
'14_11a8554fed91758fbbdd56a30f855d8571c97d55_CajitaRewriterTest',
'15_15aeae14a79ca02d9fc4c4a9c155b17210dd69e9_MonkeySourceRandom',
'15_22577bd30a63d86fbfb69c510886e0e92fa35c4a_BlacklistDialogue',
'16_1af106f251b4a6ce7fa8632d67abe44034d487e1_SpoutClient',
'17_209ffc88961a36ed0ec878becdf9871297989a56_MiscTest',
'20_0e473e9c92defefffa5e300ffe2ad033904f872f_KeyboardTextsSet',
'20_1a0c8b25b6f8a51506845818671013fddd62c0a2_Main',
'20_16f0fdfd59aa4dc8799ab670a98110b8ce129ff9_TestSamplesLoader',
'22_04eeed38d6e3268123c55ac3bf63e475f3ff74a2_MythtvProvider',
'24_123f62e20471b57b85f9d86af2e00eb3650aef55_DBSSql',
'26_40d3f145b833c2dd25f02e6ea543f926e5ac38bd_ReplicaDbV1',
'26_122e4b5f21209dd4bc3e89693c9f622882d38e7d_CompleteEvaluator',
'26_122e4b5f21209dd4bc3e89693c9f622882d38e7d_ExperimentalEvaluator',
'26_327fb79aa10b01b6cc53bbc31ce244583a8e1119_NativeString',
'28_3cd5c04e548142ef3dc562194a469ed1dc7c4ae2_CalendarApplication',
'29_1f1dc0af2c01f2a2b6cf4907ecb21b4e05982e2b_SSLSocketTest',
'29_543d0eb5dae5148c8148ca6abad8c806f7b4f4ad_SSLSocketTest',
'29_54195c894ffefcea20ce06fa90559afef0b20c16_PersonHasAdvisorRelationshipGenerator',
'31_3daa10c4d2392697e6c3827ce2e70af9257086ae_MasterInfo',
'32_35c90dff5f4d6b5f3d3fffd8a3871e22596f00fa_CssParser',
'33_1ae71bbde59783c78219b9a8eb219717f1e4e06c_ItemLogs',
'33_f1ef13926c5f2e3b2460d9f60dddfa3d6f961c9b_ItemLogs',
'36_7e38a4dfff404ab0adc0223a36ec2dd7c4f488d5_TabPaneTest',
'38_fbe9017ef3b18f4096cc2ee3d1df5a4707483597_BuildpathsBlock',
'40_b7d76e52660dc85c8de51be2d9e045d2527a75ab_Material',
]

matiasSpoon = [
'1_16c59fbd6408fc407a393351aff75a873a7add56_CommandLineRunner',
'1_22a9d9b39597cfe4ca8e77275aa54ef56d4e56a8_TwoPaneSelectionPanel',
'1_196112702a87a78214f80516ef691d21c9e38c34_CommandLineRunner',
'3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions',
'4_065c3b8e8b2191a3f3230ce0d92de4cf8af5285b_LookUpTable',
'4_007922de19cb8ea011e4e8431e3ae9c1d4d23807_Translations',
'6_036a9db5fe2d9a057fabc810013836a1c82e2d4b_Node',
'6_0149605d610fb4e993495393b7ca159e94cddea2_DefaultPassConfig',
'7_00e3cf2cc304476d7b856ebb8eb58d386a82face_SQLitePlugin',
'7_0f69892a76e0703470b1d26be92d4ca3134cfba8_JdbcTest',
'10_0f1e0480a113bdd91859f45ea0ebd152814da9c3_Util',
'10_08e547745fc6e423eb71a8da88639003d0d5d027_DBConnection',
'11_0782cf32c73c9df2f8fb91c80f8973525d167b65_BOPCrafting',
'12_127c77864449b011f58e439f3f8d99096dd72446_ParserImpl',
'13_13a388247255f822536cd1e06c90bf41907b8879_CubePuzzle',
'14_11a8554fed91758fbbdd56a30f855d8571c97d55_CajitaRewriterTest',
'15_15aeae14a79ca02d9fc4c4a9c155b17210dd69e9_MonkeySourceRandom',
'15_22577bd30a63d86fbfb69c510886e0e92fa35c4a_BlacklistDialogue',
'16_1af106f251b4a6ce7fa8632d67abe44034d487e1_SpoutClient',
'16_069a7499c3600c12e92a9cb8dc9292759715b67e_LayerServiceImpl',
'17_209ffc88961a36ed0ec878becdf9871297989a56_MiscTest',
'17_10964b90244f8e1047fb0881d4975805c507cd54_MachineController',
'19_039e06fdce5d43fc220a887e59555c64911c5286_GridDatasetInfo',
'20_0e473e9c92defefffa5e300ffe2ad033904f872f_KeyboardTextsSet',
'20_1a0c8b25b6f8a51506845818671013fddd62c0a2_Main',
'20_16f0fdfd59aa4dc8799ab670a98110b8ce129ff9_TestSamplesLoader',
'22_04eeed38d6e3268123c55ac3bf63e475f3ff74a2_MythtvProvider',
'22_08358b39668c24f824d0eac8d021cf652f933a27_EncodingMap',
'24_24be1e5763437bf5aea09e71aadb7a80160f1794_Serializer',
'24_123f62e20471b57b85f9d86af2e00eb3650aef55_DBSSql',
'26_07dab009ba690ebba019fbfaa1be784187b3d568_TechEditWizardData',
'26_40d3f145b833c2dd25f02e6ea543f926e5ac38bd_ReplicaDbV1',
'26_122e4b5f21209dd4bc3e89693c9f622882d38e7d_CompleteEvaluator',
'26_122e4b5f21209dd4bc3e89693c9f622882d38e7d_ExperimentalEvaluator',
'26_327fb79aa10b01b6cc53bbc31ce244583a8e1119_NativeString',
'28_3cd5c04e548142ef3dc562194a469ed1dc7c4ae2_CalendarApplication',
'28_4c581249602c2ffba538ee55cd31c6458ef13432_ActivityChooserModel',
'28_2476509a3aeb479cf86c5ed6b68af8d0a785e83b_AbstractHTTPChannel',
'29_1f1dc0af2c01f2a2b6cf4907ecb21b4e05982e2b_SSLSocketTest',
'29_543d0eb5dae5148c8148ca6abad8c806f7b4f4ad_SSLSocketTest',
'29_54195c894ffefcea20ce06fa90559afef0b20c16_PersonHasAdvisorRelationshipGenerator',
'31_3daa10c4d2392697e6c3827ce2e70af9257086ae_MasterInfo',
'31_596d05e3983e25546f3d4d4d9f974f77157c7d5e_Simulator',
'32_35c90dff5f4d6b5f3d3fffd8a3871e22596f00fa_CssParser',
'33_1ae71bbde59783c78219b9a8eb219717f1e4e06c_ItemLogs',
'33_f1ef13926c5f2e3b2460d9f60dddfa3d6f961c9b_ItemLogs',
'36_7e38a4dfff404ab0adc0223a36ec2dd7c4f488d5_TabPaneTest',
'38_fbe9017ef3b18f4096cc2ee3d1df5a4707483597_BuildpathsBlock',
'40_b7d76e52660dc85c8de51be2d9e045d2527a75ab_Material',

]

matiasJDT = [
'40_74e7382ef413900f9d650a59a6dde7c5eebcefc6_Inventory',	
'1_0007d191fec7fe2d6a0c4e87594cb286a553f92c_ASTInspector',	
'2_03b1dec4d20cee110b68cf8325f28f4403468317_FTPClient',	
'3_04f0e8f7a3545cf877c10967396b06595d57c34a_JavaExtensions',	
'3_09e29cf8903af739223e76bd81cdd1cc99847aa1_SimpleDateFormat',	
'4_0a3726b153cfca74af11cec28c46279d46019e13_BaseDataChip',	
'4_01d8ec68011387017898b90695b84caa0508597c_DoubleMatrix',	
'4_01d8ec68011387017898b90695b84caa0508597c_FloatMatrix',	
'4_007922de19cb8ea011e4e8431e3ae9c1d4d23807_Translations',	
'5_0c7c39cce1c6f052cd2afa4fc1ad99de70b37dd6_Formatter',	
'6_025b151897ce68bb6571a56facea4296936c458d_CreateSAML2HostedProviderTemplate',	
'8_0c500246640d65ce8940c83704539e2de43a1bba_CoPIGrantCountQueryRunner',	
'8_0e0143af62348006a08f000c863bbd4a3e8ee19d_SipStackImpl',	
'8_01ec8332c1ff2c5c13df58df7f96c891b5f73169_SimpleDateFormat',	
'8_050623494a4ba889b8d5f86fa83d5e8fedc05e50_PropertyConfigurator',	
'9_04349a03c81e7d12670abd20f088be0e6a693761_ValangParserTokenManager',	
'10_12aebda5c29a97d3a7a4d4d513aaa60d397d5515_ClientResources',	
'10_142367b24be1f77592c1b9799448d3e975d31570_DateTimeFormatterBuilder',	
'10_142367b24be1f77592c1b9799448d3e975d31570_DateTimeFormatters',	
'11_0a33a79a61928e124ce79e3aa25267d7e85d675a_DateTimeFormat',	
'11_140ffc5c5eebfdfda20246cb2af6c957f5c7137c_HomeHelper',	
'11_0965fe2c1f1d9870d2066d506ec5371b767f9b65_ClickingButtons',	
'14_1bd9420e2131e2cf02b1860de1e33b0ba9546703_Interpreter',	
'14_11a8554fed91758fbbdd56a30f855d8571c97d55_CajitaRewriterTest',	
'14_175f5cfbbcbce775b4a8b008f5bebe33477f75b2_TransformedImageDisplay',	
'15_15aeae14a79ca02d9fc4c4a9c155b17210dd69e9_MonkeySourceRandom',	
'16_1dcb42802a910007281b243c73dc491f63b0caa3_FieldProgramParser',	
'16_05b75ea2e087f5e7615089b2828f1d72589d2fb2_BSuggPathBasedtxtFn',	
'16_0151b0430a725a556dee417c5e8a5da347f71b3e_IResource',	
'16_2147fe5aa47f7520756d139534f86e0e934e2e6d_DateTimeFormatters',	
'20_0e473e9c92defefffa5e300ffe2ad033904f872f_KeyboardTextsSet',	
'20_1b862fb2c74e974d7ec30f26a34403a1f29af873_Vector2f',	
'20_14bc2e89efed136a8f351e6e407be3ec2d7a3308_KeyboardTextsSet',	
'22_235f25d1f42e401ada75c753c4644748b2bf1a79_XMLMessages',	
'22_12089f30b8c2c15d7453f4467b4a3e91cee85e9d_Mockito',	
'24_0e56f33fc781fc3a3dc61d4b9ecb07755f0d2dfa_FormAction',	
'26_122e4b5f21209dd4bc3e89693c9f622882d38e7d_CompleteEvaluator',	
'26_122e4b5f21209dd4bc3e89693c9f622882d38e7d_ExperimentalEvaluator',	
'26_327fb79aa10b01b6cc53bbc31ce244583a8e1119_NativeString',	
'26_08971b145eb2e3bd314b8ba833da2b34f213f99a_DispatchBParser',	
'28_1c6bae6d5ea58cc8dcdff9f97c58372e9ec6be9c_JXTable',	
'31_10a51e3b6822814227d4317c8b05e8dad891b089_R',	
'32_1efdf68089db048859f659ebf5656da8fad592d9_Long',	
'32_9e13cc8ad982ba191400889bb8e5248a1933d45e_Long',	
'32_35c90dff5f4d6b5f3d3fffd8a3871e22596f00fa_CssParser',	
'33_6e79dbbb40cfd509f5eb73eb245f081566601a0e_R',	
'33_24784d74a8a7298fe5fcff3375b214742db26b45_R',	
'34_1bb9b571f1251877394f1f5200e4be45abeb30a5_IContentTypeManagerTest',	
'34_49e3db969e4926736e5a9f9e782a478ac57cbc95_HelpMapping',	
'34_9719a19b96ff79309402a8dacf833364c171f4ad_IContentTypeManagerTest',	
'36_dd9b3d812674c61274ae5b8eb3bdd8b2ec778b82_CompilerConfiguration',	

]

def navigate(rootResults, model, selectedPairs):
	pairfiles = (os.listdir(rootResults))
	print("Pair_ID;Best Configuration;#ActionBest;Default Configuration;#ActionDefault;Text DIFF;ACTIONS BEST (TextualDiff);ACTIONS Default (TextualDiff);Is Best better than Default? (Yes/No/no clear difference); Comment;Good Example?;Problems in Visualization?;")
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

		if pairfile not in selectedPairs:
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
#navigate("/Users/matias/develop/gt-tuning/git-appendix/evaluation/casesSpoon", "https://uphf.github.io/dat/evaluation/casesSpoon/", selectedPairs=jrSpoon)
#navigate("/Users/matias/develop/gt-tuning/git-appendix/evaluation/casesJDT", "https://uphf.github.io/dat/evaluation/casesJDT/", selectedPairs=jrJDT)
#navigate("/Users/matias/develop/gt-tuning/git-appendix/evaluation/casesSpoon", "https://uphf.github.io/dat/evaluation/casesSpoon/", selectedPairs=martinSpoon)
#navigate("/Users/matias/develop/gt-tuning/git-appendix/evaluation/casesJDT", "https://uphf.github.io/dat/evaluation/casesJDT/", selectedPairs=martinJDT)
#navigate("/Users/matias/develop/gt-tuning/git-appendix/evaluation/casesSpoon", "https://uphf.github.io/dat/evaluation/casesSpoon/", selectedPairs=matiasSpoon)
#navigate("/Users/matias/develop/gt-tuning/git-appendix/evaluation/casesJDT", "https://uphf.github.io/dat/evaluation/casesJDT/", selectedPairs=matiasJDT)
