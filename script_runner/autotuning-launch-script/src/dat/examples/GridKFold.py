import sklearn
import numpy as np

k = np.arange(20)+1
parameters = {'n_neighbors': k}
knn = sklearn.neighbors.KNeighborsClassifier()
clf = sklearn.grid_search.GridSearchCV(knn, parameters, cv=10)
all_scores = []
all_k = []
all_d = [1,2,3,4,5,6,7,8,9,10]
kFolds = sklearn.cross_validation.KFold(X.shape[0], n_folds=10)

for d in all_d:
    svd = sklearn.decomposition.TruncatedSVD(n_components=d)
    scores = []
    for train_index, test_index in kFolds:
        train_data, test_data = X[train_index], X[test_index]
        train_labels, test_labels = Y[train_index], Y[test_index]
        data_mean = np.mean(train_data, axis=0)
        train_data_centered = train_data - data_mean
        test_data_centered = test_data - data_mean
        X_d = svd.fit_transform(train_data_centered)
        X_d_test = svd.transform(test_data_centered)
        clf.fit(X_d, train_labels)
        scores.append(sklearn.metrics.accuracy_score(test_labels, clf.predict(X_d_test)))

    all_scores.append(scores)
    all_k.append(clf.best_params_['n_neighbors'])