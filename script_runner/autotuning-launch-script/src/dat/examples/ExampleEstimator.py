import numpy as np
from sklearn.base import BaseEstimator, RegressorMixin, ClassifierMixin
from sklearn.utils.validation import check_X_y, check_array, check_is_fitted
from sklearn.utils.multiclass import unique_labels
from sklearn.metrics import euclidean_distances

##http://danielhnyk.cz/creating-your-own-estimator-scikit-learn/

class ExampleEstimator(BaseEstimator, RegressorMixin): #ClassifierMixin

	 def __init__OLD(self, algo = None, ):

		 #self.best_ = []
		 self.algo = algo

	 def __init__(self, algo = None):

		 # print("Initializing classifier:\n")
		 import inspect
		 args, _, _, values = inspect.getargvalues(inspect.currentframe())
		 values.pop("self")

		 for arg, val in values.items():
			 setattr(self, arg, val)
			 # print("{} = {}".format(arg,val)


	 def fit(self, X, y):
		 print("fit {}".format(X))

		 #self.best_ = 1

		 return self

	 def predict(self, X):

		 print("Predict {}".format(X))
		 # Check is fit had been called
		 #check_is_fitted(self)

		 # Input validation
		# X = check_array(X)
		 #self.best_ = 1
		 factor = 0.1
		 if self.algo is "GT":
		 	factor = 0.4
		 if self.algo is "XY":
			 factor = 0.5
			 return [0.99 for i in range(0, len(X))]
		 if self.algo is "CD":
				 factor = 0.11
		 print("Algorithm {}".format(self.algo))
		 res = [(i + 0.1) * factor for i in range(0, len(X))]
		 print(res)
		# closest = np.argmin(euclidean_distances(X, self.X_), axis=1)
		 return res #self.y_[closest]

	 def score(self, X, y=None):
		 # counts number of values bigger than mean
		 print("score {} X {} Y {}".format(self.algo,X, y))
		 return (np.mean(self.predict(X)))


	 def z(self, xi, max):
		 ## we ignore min because it's zero
		 return 1 - (xi / max)