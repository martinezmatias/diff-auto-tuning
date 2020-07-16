
#https://www.kaggle.com/questions-and-answers/30560

## https://scikit-learn.org/stable/modules/grid_search.html#grid-search
##
#A search consists of:

#an estimator (regressor or classifier such as sklearn.svm.SVC());

#a parameter space;

#a method for searching or sampling candidates;
import numpy as np
import matplotlib.pyplot as plt

from sklearn import datasets
from sklearn.linear_model import LassoCV
from sklearn.linear_model import Lasso
from sklearn.model_selection import KFold, train_test_split
from sklearn.model_selection import GridSearchCV
from src.dat.examples.ExampleEstimator import *


#X_train, X_test, y_train, y_test = train_test_split(
#    X, y, test_size=0.5, random_state=0)

def run():
	X = ["a1", "a2", "a4", "a3"]
	Y = [1.0, 1.0, 1.0, 1.0]
	tuned_parameters = [{'algo': ["NULL", "GT", "CD", "XY"]}]
	n_folds = 2

	clf = GridSearchCV(estimator=ExampleEstimator(), param_grid=tuned_parameters, cv=n_folds, refit=True)
	clf.fit(X, Y)
	k_fold = KFold(3)

	print(clf.cv_results_)
	print("Best: ")
	print(clf.best_params_)

	print("Param: ")
	print(clf.cv_results_['params'])

	print("Grid scores on development set:")
	print()
	means = clf.cv_results_['mean_test_score']
	stds = clf.cv_results_['std_test_score']
	for mean, std, params in zip(means, stds, clf.cv_results_['params']):
		print("%0.3f (+/-%0.03f) for %r"
			  % (mean, std * 2, params))
	print()

	for k, (train, test) in enumerate(k_fold.split(X)):
		#lasso_cv.fit(X[train], y[train])
		pass
		#print("[fold {0}] alpha: {1:.5f}, score: {2:.5f}".
		#	  format(k, lasso_cv.alpha_, lasso_cv.score(X[test], y[test])))
	print()


#a cross-validation scheme; and

#a score function.


## https://scikit-learn.org/stable/auto_examples/model_selection/plot_grid_search_digits.html#sphx-glr-auto-examples-model-selection-plot-grid-search-digits-py

def fitness(configuration, listOfDiff):
	#evaluate the configuration in all diff from List

	# return the mean?
	return



run()

