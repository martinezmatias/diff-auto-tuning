##https://github.com/hyperopt/hyperopt/blob/master/tutorial/Partial-sampling%20in%20hyperopt.ipynb

from hyperopt import hp, fmin, rand
space = hp.choice('a', [-1, hp.uniform('b', 0, 1)])
best = fmin(fn=lambda x: x, space=space, algo=rand.suggest, max_evals=100)
print(best)

