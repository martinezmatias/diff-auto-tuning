from rpy2.robjects.packages import importr
from rpy2.robjects import r, pandas2ri
pandas2ri.activate()
import pandas as pd

sk = importr('ScottKnottESD')
data = pd.read_csv("data.csv")
r_sk = sk.sk_esd(data)
ranking = pd.DataFrame({'columns':r_sk[2], 'rank':list(r_sk[1])}) # long format
ranking = pd.DataFrame([list(r_sk[1])], columns=r_sk[2]) # wide format