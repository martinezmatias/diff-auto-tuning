
'''Given a dictionary, that counts the ocurrences of variables, returns a list'''
def plainDict(dicOcurrences = {}):
	aList = []
	for k in dicOcurrences.keys():
		nr = dicOcurrences[k]
		for i in range(1, nr + 1):
			aList.append(k)

	return aList

'''Increment the key the nr of units given by Value param'''
def incrementOne(dict, key, value = 1):
	if key not in dict:
		dict[key] = value
	else:
		dict[key] += value

'''fills the map with the index (value) of each column (key) according to the row passed as parameter'''
def columnsToMap(row, indexesOfColumns):
	if len(indexesOfColumns) == 0:
		columns = row.columns
		i = 1
		for c in columns:
			indexesOfColumns[c] = i
			i += 1