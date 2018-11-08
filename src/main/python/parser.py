#!/usr/bin/python
# **coding=utf-8***

import getopt
import sys
import xml.etree.ElementTree as ET
import shutil

import logging  # 引入logging模块
logging.basicConfig(level=logging.INFO)  # 设置日志级别

VERBOSE = False
NAMESPACE = "http://maven.apache.org/POM/4.0.0"
NAMESPACE_MAP = {"project": NAMESPACE}
SNAPSHOT_SUFFIX = "-SNAPSHOT"
ARTIFACTORY_URL = "http://artifactory.intra.xiaojukeji.com"


def usage():
    print('''
    this is help message: until now this function can only handle pom 4.0.0
    -h --help show help info
    -i --input the input file or directory
    -a --action
    -v --verbose show verbose information
    ''')
    pass


def gen_req_url(groupid, artifactid, version):
    pass


def get_pom_module_version(inputfile):
    root = ET.parse(inputfile)
    version = root.find("./project:version", NAMESPACE_MAP).text
    groupid = root.find("./project:groupId", NAMESPACE_MAP).text
    artifactid = root.find("./project:artifactId", NAMESPACE_MAP).text
    return {"version": version, "artifactId": artifactid, "groupId": groupid}
    pass


def remove_snapshot_pom_module_version(inputfiles=[]):
    for inputfile in inputfiles:
        root = ET.parse(inputfile)
        version = root.find("./project:version", NAMESPACE_MAP).text
        if version.endswith(SNAPSHOT_SUFFIX):
            new_version = version[0:version.rfind(SNAPSHOT_SUFFIX)]
            root.find("./project:version", NAMESPACE_MAP).text = new_version
            #logging.info("rewrite file{} version from {} to {}", inputfile, version, new_version)
            root.write(inputfile, default_namespace=NAMESPACE)
    pass


def get_pom_module_versions(inputfiles=[]):
    fileInfo = {}
    for fileName in inputfiles:
        fileInfo.setdefault(fileName, get_pom_module_version(fileName))
    return fileInfo
    pass


def do_backup_files(inputfiles=[], suffix=".bak"):
    global VERBOSE
    for filename in inputfiles:
        backup_file = filename + suffix
        if VERBOSE:
            print("backup file % to file %s", filename, backup_file)
        shutil.copy(filename, backup_file)
    pass


def do_restore_files(inputfiles=[], suffix=".bak"):
    pass


def test_if_has_release(file_info={}):
    version = file_info.get("version")
    version = version[0:version.rfind('-SNAPSHOT')]
    artifactId  = file_info.get("artifactId")
    groupId = file_info.get("groupId")
    request_url = ARTIFACTORY_URL \
                  + groupId.replace(".", "/") \
                  + '/' + artifactId + "/" \
                  + version
    # TODO : request if it has been released
    if VERBOSE:
        print request_url
    return False
    pass


def get_file_who_is_snapshot(file_info_map={}):
    res = []
    for (file_name, info) in file_info_map.items():
        if info.get("version", "").endswith(SNAPSHOT_SUFFIX):
            res.append(file_name)
    return res
    pass


def get_file_who_has_no_release(files_with_snapshots=[], file_info_map={}):
    res = []
    for file_name in files_with_snapshots:
        if file_name not in file_info_map: continue
        file_info = file_info_map.get(file_name)
        if not test_if_has_release(file_info):
            res.append(file_name)
    return res
    pass


def check_and_rename(inputfiles= []):
    # 1. get versions
    file_info_map = get_pom_module_versions(inputfiles)
    # logging.info("file_info_map {}", file_info_map)
    print("file_info_map", file_info_map)
    # 2. filter snapshots
    files_with_snapshots = get_file_who_is_snapshot(file_info_map)
    # logging.info("files_with_snapshots {}", files_with_snapshots)
    print("files_with_snapshots", files_with_snapshots)
    # 3. check if has release in artifactory
    files_with_snapshots_with_no_release = get_file_who_has_no_release(files_with_snapshots, file_info_map)
    # logging.info("files_with_snapshots_with_no_release {}", files_with_snapshots_with_no_release)
    print("files_with_snapshots_with_no_release", files_with_snapshots_with_no_release)
    # 4. backup
    do_backup_files(files_with_snapshots_with_no_release)
    # 5. remove snapshot
    remove_snapshot_pom_module_version(files_with_snapshots_with_no_release)
    pass


def main():
    try:
        opts, args = getopt.getopt(sys.argv[1:], "hi:a:v", ["help", "input=", "action=", "verbose"])
    except getopt.GetoptError as err:
        print(err)
        usage()
        sys.exit(2)

    inputfiles = []
    action = ""
    global VERBOSE
    for o, a in opts:
        if o in ("-v", "--verbose"):
            VERBOSE = True
        elif o in ("-h", "--help"):
            usage()
            sys.exit()
        elif o in ("-i", "--input"):
            inputfiles += a.split(",")
        elif o in ("-a", "--action"):
            action = a
        else:
            usage()
    check_and_rename(inputfiles)
    # print(inputfiles, action, VERBOSE)
    # print(get_pom_module_versions(inputfiles))
    # do_backup_files(inputfiles, ".bak")


if __name__ == "__main__":
    main()
