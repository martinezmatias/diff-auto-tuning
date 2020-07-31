import matplotlib.pyplot as plt
import numpy as np

def plotImprovements(improvements, defaults, key):
	#data_a = [[1,2,5], [5,7,2,2,5], [7,2,5]]
	#data_b = [[6,4,2], [1,2,5,3,2], [2,3,5,1]]

	ticks = ['GumTree', 'ChangeDistiller', 'Xy']

	def set_box_color(bp, color):
		plt.setp(bp['boxes'], color=color)
		plt.setp(bp['whiskers'], color=color)
		plt.setp(bp['caps'], color=color)
		plt.setp(bp['medians'], color=color)

	plt.figure()

	bpl = plt.boxplot(defaults, positions=np.array(range(len(defaults)))*2.0-0.4, sym='', widths=0.6)
	bpr = plt.boxplot(improvements, positions=np.array(range(len(improvements)))*2.0+0.4, sym='', widths=0.6)
	set_box_color(bpl, '#D7191C') # colors are from http://colorbrewer2.org/
	set_box_color(bpr, '#2C7BB6')

	# draw temporary red and blue lines and use them to create a legend
	plt.plot([], c='#D7191C', label='Default (Left)')
	plt.plot([], c='#2C7BB6', label='Optimized (Right)')
	plt.legend()

	plt.xticks(range(0, len(ticks) * 2, 2), ticks)
	plt.xlim(-2, len(ticks)*2)
	plt.ylim(0, 1)
	plt.tight_layout()
	plt.savefig('boxcomparePerformance_{}.pdf'.format(key))
