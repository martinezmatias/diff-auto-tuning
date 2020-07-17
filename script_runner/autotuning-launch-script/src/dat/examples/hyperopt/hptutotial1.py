#https://github.com/hyperopt/hyperopt/blob/master/tutorial/01.BasicTutorial.ipynb
from hyperopt import tpe, hp, fmin
objective = lambda x: (x-3)**2 + 2
import matplotlib.pyplot as plt
import numpy as np

x = np.linspace(-10, 10, 100)
y = objective(x)

fig = plt.figure()
plt.plot(x, y)
plt.show()

space = hp.uniform('x', -10, 10)
print("space {}".format(space))

# Now we can search through the search space $x$ and find the value of $x$ that can optimize the objective function. HyperOpt performs it using fmin.
best = fmin(
    fn=objective, # Objective Function to optimize
    space=space, # Hyperparameter's Search Space
    algo=tpe.suggest, # Optimization algorithm
    max_evals=1000 # Number of optimization attempts
)
print(best)


#hp.randint(label, upper) or hp.randint(label, low, high)
#hp.uniform(label, low, high)
#hp.loguniform(label, low, high)
#hp.normal(label, mu, sigma)
#hp.lognormal(label, mu, sigma)
#hp.quniform(label, low, high, q)
#hp.qloguniform(label, low, high, q)
#hp.qnormal(label, mu, sigma, q)
#hp.qlognormal(label, mu, sigma, q)
#hp.choice(label, list)