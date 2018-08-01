import os

if __name__ == "__main__":
	n_emu = 5
	template_avd = "/home/kemao/.android/avd/api19_0.avd"
	for i in range(1, n_emu):
		os.system("cp -r " + template_avd + " " + template_avd.split("_")[0] + "_" + str(i) + ".avd")

		with open(template_avd.split("_")[0] + "_" + str(i) + ".ini", "w") as ini_file:
			ini_file.write("avd.ini.encoding=UTF-8\n")
			ini_file.write("path=/home/kemao/.android/avd/api19_" + str(i) + ".avd\n")
			ini_file.write("path.rel=avd/api19_" + str(i) + ".avd\n")
			ini_file.write("target=android-19\n")
