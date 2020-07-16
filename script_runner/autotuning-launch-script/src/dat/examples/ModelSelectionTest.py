## every estimator exposes a score method that can judge the quality of the fit (or the prediction) on new data. Bigger is better.
# https://scikit-learn.org/stable/tutorial/statistical_inference/model_selection.html

from sklearn import datasets, svm
X_digits, y_digits = datasets.load_digits(return_X_y=True)
svc = svm.SVC(C=1, kernel='linear')
resultFit  = svc.fit(X_digits[:-100], y_digits[:-100]).score(X_digits[-100:], y_digits[-100:])
print(resultFit)

## Manual Division

import numpy as np
X_folds = np.array_split(X_digits, 3)
y_folds = np.array_split(y_digits, 3)
scores = list()
for k in range(3):
     # We use 'list' to copy, in order to 'pop' later on
     X_train = list(X_folds)
     X_test = X_train.pop(k)
     X_train = np.concatenate(X_train)
     y_train = list(y_folds)
     y_test = y_train.pop(k)
     y_train = np.concatenate(y_train)
     scores.append(svc.fit(X_train, y_train).score(X_test, y_test))
print(scores)


## Nested cross-validation
### https://scikit-learn.org/stable/tutorial/statistical_inference/model_selection.html#grid-search
print("Nested cross-validation")
from sklearn.model_selection import KFold, cross_val_score
k_fold = KFold(n_splits=5)

for train, test in k_fold.split(X_digits):
	score = svc.fit(X_digits[train], y_digits[train]).score(X_digits[test], y_digits[test])
	print(score)