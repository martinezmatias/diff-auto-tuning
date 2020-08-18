
import sys
from src.processedDataConsumers.EngineHyperOptDAT import *
from src.commons.Datalocation import *
from src.processedDataConsumers.EngineGridSearchKfoldValidation import *
from src.processedDataConsumers.deprecated.plotPerformance import *
from src.rowDataConsumers.RQ0_Setup_ComputeFitnessDistanceOfConfiguationsFromRowData import *
from src.commons.Datalocation import *

plocation = sys.argv[1]

psuffix = sys.argv[2]

pkey = str(sys.argv[3])


computeFitness(plocation, suffix=psuffix, key = pkey)