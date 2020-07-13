from  hyperopt  import  hp,  tpe,  fmin,  Trials,  STATUS_OK
 from  sklearn  import  datasets 
from  sklearn.neighbors  import  KNeighborsClassifier 
from  sklearn.svm  import  SVC
 from  sklearn.linear_model  import  LogisticRegression
 from  sklearn.ensemble.forest  import  RandomForestClassifier   

from  sklearn.preprocessing  import  scale,  normalize 

from  sklearn.model_selection  import  cross_val_score 

 import  numpy  as  np 
import  pandas  as  pd 
import  matplotlib.pyplot  as  plt

models  =  { 
      'logistic_regression'  :  LogisticRegression, 
      'rf'  :  RandomForestClassifier, 
      'knn'  :  KNeighborsClassifier,   'svc'  :  SVC 
}


 def  search_space(model):   

        model  =  model.lower() 
        space  =  {} 

        if  model  ==  'knn':   space  =  {   'n_neighbors':  hp.choice('n_neighbors',  range(1,100)),
         'scale':  hp.choice('scale',  [0,  1]), 
        'normalize':  hp.choice('normalize',  [0,  1]),   


        }   

        elif  model  ==  'svc': 
                  space  =  { 
                          'C':  hp.uniform('C',  0,  20), 
                          'kernel':  hp.choice('kernel',  ['linear',  'sigmoid',  'poly',  'rbf']), 
                          'gamma':  hp.uniform('gamma',  0,  20), 
                          'scale':  hp.choice('scale',  [0,  1]), 
                          'normalize':  hp.choice('normalize',  [0,  1]), 
                  }   

        elif  model  ==  'logistic_regression': 
                  space  =  { 
                          'warm_start'  :  hp.choice('warm_start',  [True,  False]), 
                          'fit_intercept'  :  hp.choice('fit_intercept',  [True,  False]),
                          'tol'  :  hp.uniform('tol',  0.00001,  0.0001), 
                          'C'  :  hp.uniform('C',  0.05,  3), 
                          'solver'  :  hp.choice('solver',  ['newton-cg',  'lbfgs',  'liblinear']), 
                          'max_iter'  :  hp.choice('max_iter',  range(100,1000)), 
                          'scale':  hp.choice('scale',  [0,  1]), 
                          'normalize':  hp.choice('normalize',  [0,  1]), 
                          'multi_class'  :  'auto', 
                          'class_weight'  :  'balanced' 
            }   

    elif  model  ==  'rf': 
              space  =  {   'max_depth':  hp.choice('max_depth',  range(1,20)), 
                      'max_features':  hp.choice('max_features',  range(1,3)), 
                      'n_estimators':  hp.choice('n_estimators',  range(10,50)), 
                      'criterion':  hp.choice('criterion',  ["gini",  "entropy"]), 
          }   

    space['model']  =  model   

    return  space

def  get_acc_status(clf,X_,y): 
    acc  =  cross_val_score(clf,  X_,  y,  cv=5).mean() 
    return  {'loss':  -acc,  'status':  STATUS_OK}

def  obj_fnc(params)  :   
      model  =  params.get('model').lower() 
      X_  =  scale_normalize(params,X[:])   
      del  params['model'] 
      clf  =  models[model](**params) 
      return(get_acc_status(clf,X_,y))

hypopt_trials  =  Trials() 

 best_params  =  fmin(obj_fnc,  search_space(model),  algo=tpe.suggest,
					max_evals=1000,  trials=hypopt_trials) 

 print(best_params) 
print(hypopt_trials.best_trial['result']['loss'])

with  open('dominostats.json',  'w')  as  f: 
      f.write(json.dumps({"Algo":  model,  "Accuracy":
hypopt_trials.best_trial['result']['loss'],"Best  params"  :  best_params}))