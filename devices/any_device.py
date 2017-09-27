import emulator
import real_device
import settings

def get_devices():
    devices = []
    if settings.USE_REAL_DEVICES:
        devices.extend(real_device.get_devices())
    if settings.USE_EMULATORS:
        devices.extend(emulator.get_devices())
    return devices;

def boot_devices():
    if settings.USE_EMULATORS:
        emulator.boot_devices()

def clean_sdcard():
    if settings.USE_EMULATORS:
        emulator.clean_sdcard()

def pack_and_deploy_aut():
	# instrument the app under test
	if settings.USE_REAL_DEVICES:
		real_device.pack_and_deploy_aut()
	if settings.USE_EMULATORS:
		emulator.pack_and_deploy_aut()

def prepare_motifcore():
	if settings.USE_REAL_DEVICES:
		real_device.prepare_motifcore()
	if settings.USE_EMULATORS:
		emulator.prepare_motifcore()

