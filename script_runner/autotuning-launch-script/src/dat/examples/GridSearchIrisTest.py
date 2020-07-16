from sklearn import svm, datasets
from sklearn.model_selection import GridSearchCV
iris = datasets.load_iris()
parameters = {'kernel':('linear', 'rbf'), 'C':[1, 10]}
svc = svm.SVC()
clf = GridSearchCV(svc, parameters)
clf.fit(iris.data, iris.target)
result =  clf.cv_results_
print(result)

print("Best: ")
print(clf.best_params_)

print("Param: ")
print(clf.cv_results_['params'])