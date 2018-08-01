import matplotlib.pyplot as plt
import pickle

import settings


def extract_axis(array, axis):
	ret = []
	for elem in array:
		ret.append(elem[axis])
	return ret


def plot(logbook, axis, apk_dir):
	gen = logbook.select("gen")
	fit_avg = extract_axis(logbook.select("avg"), axis)
	fit_min = extract_axis(logbook.select("min"), axis)
	fit_max = extract_axis(logbook.select("max"), axis)

	fig, ax1 = plt.subplots()
	line1 = ax1.plot(gen, fit_avg, "b-", label="Avg Fitness")
	line2 = ax1.plot(gen, fit_min, "r-", label="Min Fitness")
	line3 = ax1.plot(gen, fit_max, "g-", label="Max Fitness")
	ax1.set_xlabel("Generation")
	ax1.set_ylabel("Obj.-" + str(axis), color="b")
	for tl in ax1.get_yticklabels():
		tl.set_color("b")

	lns = line1 + line2 + line3
	labs = [l.get_label() for l in lns]
	leg = ax1.legend(lns, labs, loc="upper right", frameon=False)
	leg.get_frame().set_alpha(0.5)
	# plt.show()
	fig = plt.gcf()
	fig.set_size_inches(18.5, 10.5)
	fig.savefig(apk_dir + '/intermediate/obj_' + str(axis) + '.pdf', dpi=300)


if __name__ == "__main__":
	logbook = pickle.load(open(settings.WORKING_DIR + "intermediate/logbook.pickle"))
	plot(logbook, 0)
