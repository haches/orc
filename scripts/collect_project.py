import commands
import os
import datetime

prc = commands.getoutput('ps aux')
if prc.find('osproject_downloader.jar collect-project')<0:
	dow = datetime.datetime.today().weekday()
	schedule = [['c#', 'csharp'], ['java'], ['javascript'], ['python'], ['ruby'], ['php', 'objective-c'], ['c', 'c++']]
	platforms = ['codeplex', 'github', 'bitbucket', 'googlecode']
	
	for lan in schedule[dow]:
		for plf in platforms:
			os.system('java -jar osproject_downloader.jar collect-project --language ' + lan + ' --platform ' + plf + ' --repository --log-directory /tmp --host 5.9.93.15 --user root --password 83670327 --schema osprojects')

