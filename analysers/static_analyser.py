# Copyright (c) 2016-present, Ke Mao. All rights reserved.
#
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions
# are met:
#
#     * Redistributions of source code must retain the above copyright
#       notice, this list of conditions and the following disclaimer.
#
#     * Redistributions in binary form must reproduce the above
#       copyright notice, this list of conditions and the following
#       disclaimer in the documentation and/or other materials provided
#       with the distribution.
#
#     * The names of the contributors may not be used to endorse or
#       promote products derived from this software without specific
#       prior written permission.
#
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
# "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
# LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
# A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
# OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
# SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
# LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
# DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
# THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
# (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.


import os
import settings


def decode_apk(input_apk, output_dir):
	os.chdir(output_dir)
	os.system("java -jar " + settings.WORKING_DIR + "lib/apktool.jar d -f " + input_apk)


def upload_string_xml(device, decoded_dir, package_name):
	string_xml_path = decoded_dir + "/res/values/strings.xml"
	if settings.ENABLE_STRING_SEEDING is False or os.path.exists(string_xml_path) is False:
		# if not exist, upload dummy strings.xml
		string_xml_path = settings.WORKING_DIR + "resources/dummy_strings.xml"
	os.system("/usr/local/android-sdk/platform-tools/adb -s " + device + " shell rm /mnt/sdcard/" + package_name + "_strings.xml")
	os.system("/usr/local/android-sdk/platform-tools/adb -s " + device + " push " + string_xml_path + " /mnt/sdcard/" + package_name + "_strings.xml")

