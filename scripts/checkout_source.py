import commands
import os
import datetime

prc = commands.getoutput('ps aux')
if prc.find('osproject_downloader.jar manage-download')<0:
	os.system('java -jar osproject_downloader.jar manage-download --language c# --host 5.9.93.15  --user root --password 83670327 --schema osprojects --folder /home/jasonw/osprjs --max-download-time 60 --max-project 3000 --downloader-jar /home/jasonw/projects/orc/scripts/osproject_downloader.jar')


