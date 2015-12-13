#!/usr/bin/env python
import os
import sys

from gzip2 import GzipFile

class FileReader(object):

    @staticmethod
    def open_file(filename, mode='rb'):
        """ open plain or compressed file
        @return file handler
        """
        parts = os.path.basename(filename).split('.')
        try:
            assert parts[-1] == 'gz'
            fh = GzipFile(mode=mode, filename = filename)
        except:
            fh = open(filename, mode)
        return fh

    @staticmethod
    def list_files(folder, regex_str=r'.', match=True):
        """ find all files under 'folder' with names matching 
        some reguler expression
        """
        assert os.path.isdir(folder)
        all_files_path = []
        for root, dirs, files in os.walk(folder):
            for filename in files:
                if match and re.match(regex_str, filename, re.IGNORECASE):
                    all_files_path.append(os.path.join(root, filename))
                elif not match and re.search(regex_str, filename, re.IGNORECASE):
                    all_files_path.append(os.path.join(root, filename))
        return all_files_path


if __name__ == '__main__':
    if len(sys.argv) < 2:
        print('Usage: unzip_tcp.py file.gz')
        sys.exit(-1)

    for line in FileReader.open_file(sys.argv[1]):
        print(line.strip('\r\n '))
