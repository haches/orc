import commands
import os
import datetime

prc = commands.getoutput('ps aux')
if prc.find('osproject_downloader.jar collect-project')<0:
	dow = datetime.datetime.today().weekday()
	if dow==0:
		os.system('java -jar osproject_downloader.jar collect-project --language c# --platform codeplex --repository --log-directory /tmp --host 5.9.93.15 --user root --password 83670327 --schema osprojects')
	elif dow==1:
		os.system('java -jar osproject_downloader.jar collect-project --language java --platform codeplex --repository --log-directory /tmp --host 5.9.93.15 --user root --password 83670327 --schema osprojects')
	elif dow==2:
		os.system('java -jar osproject_downloader.jar collect-project --language javascript --platform codeplex --repository --log-directory /tmp --host 5.9.93.15 --user root --password 83670327 --schema osprojects')
	elif dow==3:
		os.system('java -jar osproject_downloader.jar collect-project --language python --platform codeplex --repository --log-directory /tmp --host 5.9.93.15 --user root --password 83670327 --schema osprojects')
	elif dow==4:
		os.system('java -jar osproject_downloader.jar collect-project --language ruby --platform codeplex --repository --log-directory /tmp --host 5.9.93.15 --user root --password 83670327 --schema osprojects')
	elif dow==5:
		os.system('java -jar osproject_downloader.jar collect-project --language php --platform codeplex --repository --log-directory /tmp --host 5.9.93.15 --user root --password 83670327 --schema osprojects')
	elif dow==6:
		os.system('java -jar osproject_downloader.jar collect-project --language c++ --platform codeplex --repository --log-directory /tmp --host 5.9.93.15 --user root --password 83670327 --schema osprojects')
    	os.system('java -jar osproject_downloader.jar collect-project --language objective-c --platform codeplex --repository --log-directory /tmp --host 5.9.93.15 --user root --password 83670327 --schema osprojects')

