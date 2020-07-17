from hyperopt import tpe, hp, fmin
import numpy as np
#https://github.com/hyperopt/hyperopt/blob/master/tutorial/02.MultipleParameterTutorial.ipynb
def f(params):
    x1, x2 = params['algorithm'], params['x2']
    if x1 == 'SimpleGumtree':
        return -1 * x2
    if x1 == 'ClassicGumtree':
        return 2 * x2
    if x1 == 'CompleteGumtreeMatcher':
        return -3 * x2
    if x1 == 'ChangeDistiller':
        return 4 * x2
    if x1 == 'XyMatcher':
        return 6 * x2
search_space = {
    'algorithm': hp.choice('algorithm', ["SimpleGumtree", "ClassicGumtree", "CompleteGumtreeMatcher","ChangeDistiller","XyMatcher"]),
    'x2': hp.randint('x2', -5, 5)
}
print(search_space)

best = fmin(
    fn=f,
    space=search_space,
    algo=tpe.suggest,
    max_evals=100
)
print(best)