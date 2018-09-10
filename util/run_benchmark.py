import matplotlib
# Force matplotlib to not use any Xwindows backend.
from deap import creator, base
from evaluation import eval_suite
from evaluation import gen_individual_with_coverage, gen_individual
from evaluation import get_sequence
from evaluation import get_sequence_with_fitness

from application import static_analyser
from application.prepare_apk_parallel import get_package_name, push_apk_and_string_xml
from devices.device_manager import DeviceManager

matplotlib.use('Agg')

import argparse
import os
import subprocess as sub
import time

import logger
import settings
from devices import adb


def run_benchmark(app_path):
    folder_name = os.path.basename(app_path)
    result_dir = settings.WORKING_DIR + "benchmark-dir"
    adb.adb_logs_dir = result_dir

    # set up auxiliar result dir
    os.system("rm -rf " + result_dir + "/*" + logger.redirect_string())
    result_code = os.system("mkdir -p " + result_dir)
    if result_code != 0: raise Exception("Unable to create result dir")
    os.system("mkdir -p " + result_dir + "/intermediate")
    os.system("mkdir -p " + result_dir + "/coverage")
    os.system("mkdir -p " + result_dir + "/crashes")

    # kill all emulators running
    device_manager = DeviceManager()
    device_manager.shutdown_emulators()
    assert len(device_manager.get_devices()) == 0

    # we will store times in a dictionary
    times = []

    os.chdir(app_path)

    creator.create("FitnessCovLen", base.Fitness, weights=(10.0, -0.5, 1000.0))
    creator.create("Individual", list, fitness=creator.FitnessCovLen)
    settings.SEQUENCE_LENGTH_MIN = settings.SEQUENCE_LENGTH_MAX

    # - Reiniciar ADB server
    start_time = time.time()
    restart_adb_server()
    times.append(("Reinicar ADB server", time.time() - start_time))

    # - Bootear emulador
    if settings.USE_EMULATORS:
        start_time = time.time()
        boot_emulator(device_manager)
        times.append(("Bootear emulador", time.time() - start_time))

    # - Get devices
    start_time = time.time()
    device = device_manager.get_devices()[0]
    times.append(("Obtener emuladores/dispositivos disponibles", time.time() - start_time))

    # - Reinicar un emulador
    if settings.USE_EMULATORS:
        start_time = time.time()
        reboot_device(device_manager, device)
        times.append(("Reiniciar emulador", time.time() - start_time))

    # - Reinicar un emulador
    if settings.USE_REAL_DEVICES:
        start_time = time.time()
        reboot_device(device_manager, device)
        times.append(("Reiniciar dispositivo", time.time() - start_time))

    # - Instalar Motifcore
    start_time = time.time()
    device_manager.prepare_motifcore()
    times.append(("Instalar Motifcore", time.time() - start_time))

    # - Borrar archivos SD card
    start_time = time.time()
    device_manager.clean_sdcard()
    times.append(("Limpiar SD card", time.time() - start_time))

    # - Instrumentar la app
    start_time = time.time()
    instrument_apk(folder_name, result_dir)
    times.append(("Instrumentar app", time.time() - start_time))

    # - Instalar app
    start_time = time.time()
    package_name = prepare_apk(device, app_path, result_dir)
    times.append(("Extraer cadenas e Instalar apk", time.time() - start_time))

    # - Check battery
    start_time = time.time()
    device_manager.log_devices_battery("init", result_dir)
    times.append(("Comprobar y loggear estado bateria emuladores", time.time() - start_time))

    # - Correr monkey
    start_time = time.time()
    run_monkey(device, package_name)
    times.append(("Correr monkey", time.time() - start_time))

    # - pm clear
    start_time = time.time()
    adb.shell_command(device, "pm clear " + package_name)
    times.append(("Limpiar datos app", time.time() - start_time))

    # - Crear un caso de test sin fitness
    start_time = time.time()
    _ = get_sequence(True, device, result_dir, package_name, 0, set())
    times.append(("Generar caso de test (sin fitness)", time.time() - start_time))

    # - Crear un test suite sin fitness
    start_time = time.time()
    individual, _ = gen_individual(True, device, result_dir, package_name)
    times.append(("Generar individuo (sin fitness)", time.time() - start_time))

    # - Crear un caso de test con fitness
    start_time = time.time()
    _ = get_sequence_with_fitness(True, device, result_dir, package_name, 0, 0, 0, set())
    times.append(("Generar caso de test (con fitness)", time.time() - start_time))

    # - Crear un test suite con fitness
    start_time = time.time()
    individual_with_fitness, _ = gen_individual_with_coverage(True, device, result_dir, app_path, package_name, 0, 0)
    times.append(("Generar individuo (con fitness)", time.time() - start_time))

    # - Intentar prender bluetooth
    start_time = time.time()
    device.set_bluetooth_state(True)
    times.append(("Intentar prender bluetooth", time.time() - start_time))

    # - Intentar prender WiFi
    start_time = time.time()
    device.set_wifi_state(True)
    times.append(("Intentar prender WiFi", time.time() - start_time))

    # - Intentar prender location services
    start_time = time.time()
    device.set_location_state(True)
    times.append(("Intentar prender location services", time.time() - start_time))

    # - Ejecutar un caso de test
    # - Enviar broadcast de cobertura
    # - Obtener archivo de cobertura
    start_time = time.time()
    eval_suite(True, individual, device, result_dir, app_path, package_name, 0, 0)
    times.append(("Ejecutar individuo obteniendo fitness", time.time() - start_time))

    # - Desinstalar app
    start_time = time.time()
    adb.uninstall(device, package_name)
    times.append(("Desinstalar app", time.time() - start_time))

    return times


def restart_adb_server():
    os.system(adb.adb_cmd_prefix + " kill-server" + logger.redirect_string())
    os.system(adb.adb_cmd_prefix + " devices" + logger.redirect_string())


def boot_emulator(device_manager):
    device_name = settings.AVD_SERIES + "_0"

    emulator_cmd = "export QEMU_AUDIO_DRV=none && $ANDROID_HOME/emulator/emulator"
    # logs = " > " + device_name + ".log 2>" + device_name + ".err"
    logs = " >/dev/null 2>/dev/null"

    flags = " -wipe-data -no-window -no-boot-anim -writable-system -verbose -debug all"
    sub.Popen(emulator_cmd + ' -avd ' + device_name + flags + logs, stdout=sub.PIPE, stderr=sub.PIPE, shell=True)

    while len(device_manager.get_devices(refresh=True)) < 1:
        time.sleep(3)


def reboot_device(device_manager, device):
    output, errors, result_code = adb.adb_command(device, "reboot")
    if result_code != 0:
        logger.log_progress("\nUnable to reboot device: " + device.name)
        logger.log_progress("\nPlease, turn it off and on manually.")
        raise Exception("Unable to reboot device: " + device.name)

    time.sleep(5)
    while len(device_manager.get_devices(refresh=True)) < 1:
        time.sleep(3)


def instrument_apk(folder_name, result_dir):
    result_code = os.system("ant clean emma debug 2>&1 >" + result_dir + "/build.log")
    if result_code != 0: raise Exception("Unable run ant clean emma debug")

    result_code = os.system("cp bin/coverage.em " + result_dir + "/" + logger.redirect_string())
    if result_code != 0: raise Exception("Unable to copy coverage.em file")

    p = sub.Popen("ls bin/*-debug.apk", stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    apk_path, errors = p.communicate()
    apk_path = apk_path.rstrip('\n')

    p = sub.Popen(
        settings.AAPT + " d xmltree " + apk_path + " AndroidManifest.xml | grep package | awk 'BEGIN {FS=\"\\\"\"}{print $2}'",
        stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    package_name, errors = p.communicate()
    package_name = package_name.rstrip('\n')

    return apk_path, package_name


def prepare_apk(device, instrumented_app_dir, result_dir):
    package_name, apk_path = get_package_name(instrumented_app_dir)

    # static analysis
    if settings.ENABLE_STRING_SEEDING:
        output_dir = instrumented_app_dir + "/bin"
        static_analyser.decode_apk(apk_path, output_dir)

    decoded_dir = instrumented_app_dir + "/bin/" + apk_path.split("/")[-1].split(".apk")[0]

    result = push_apk_and_string_xml(device, decoded_dir, package_name, apk_path)
    if not result[0]:
        raise Exception("Unable to install app")
    else:
        return package_name

def run_monkey(device, package_name):
    monkey_cmd = adb.adb_cmd_prefix + " -s " + device.name + \
                 " shell monkey -p " + package_name + \
                 " -v --throttle 200 --ignore-crashes --ignore-security-exceptions --ignore-timeouts " + \
                 str(settings.SEQUENCE_LENGTH_MAX) + " 2>&1 >/dev/null"
    os.system(monkey_cmd)


def get_subject_paths(subjects_directory):
    p = sub.Popen("ls -d " + subjects_directory + "*/", stdout=sub.PIPE, stderr=sub.PIPE, shell=True)
    output, errors = p.communicate()
    app_paths = []
    for line in output.strip().split('\n'):
        if "hydrate" not in line:  # hydrate app doesn't compile yet, so don't bother
            app_paths.append(line.rstrip('/'))  # remove trailing forward slash
    return app_paths


if __name__ == "__main__":
    # run this script from the root folder as:
    # python run_benchmark.py

    # parse args
    parser = argparse.ArgumentParser(description='Run Evolutiz experiment with different strategies.')
    parser.add_argument('-d', '--subjects', dest='subjects_directory', default='$PWD/monkey/subjects/',
                        help='Directory where subjects are located')

    args = parser.parse_args()
    app_path = get_subject_paths(args.subjects_directory)[0]

    # run Evolutiz exp
    logger.prepare()
    logger.clear_progress()
    benchmark_repetitions = 5
    logger.log_progress("Running benchmark: ")

    times_by_repetition = []
    all_times = []
    for i in range(0, benchmark_repetitions):
        logger.log_progress("\rRunning benchmark: " + str(i + 1) + "/" + str(benchmark_repetitions))
        benchmark = run_benchmark(app_path)
        times_by_repetition.append(benchmark)
        all_times.extend(benchmark)

    logger.clear_progress()
    logger.log_progress("\nBenchmark results\n")
    for item in times_by_repetition[0]:
        concept, _ = item
        times_for_concept = [item[1] for item in all_times if item[0] == concept]
        avg_time = sum(times_for_concept) / float(benchmark_repetitions)
        logger.log_progress(concept + " -> " + str(avg_time) + "\n")

    logger.restore()
