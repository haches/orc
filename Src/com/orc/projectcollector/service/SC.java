package com.orc.projectcollector.service;

public class SC {
	
	/* Command names */
	static public String collectProject = "collect-project";
	static public String downloadProject = "download-project";
	static public String downloadManagement = "manage-download";
	
	/* Command option names */
	static public String languageOption = "language";
	static public String platformOption = "platform";
	static public String logDirectionOption = "log-directory";
	static public String threadOption = "thread";
	static public String repositoryOption = "repository";
	static public String helpOption = "help";
	static public String projectOption = "project"; 
	static public String outputOption = "output";
	static public String minCommitsOption = "min-revision";	
	
	static public String urlOption = "url";
	static public String createDateOption = "create-date";
	static public String folderOption = "folder";
	static public String projetNameOption ="project";
	static public String versionControlOption = "version-control";
	
	public static String parallelOpt = "parallel";
	public static String maxDownloadTimeOpt = "max-download-time";
	public static String updateFrequencyOpt = "update-frequency";	
	public static String maxProjectOpt = "max-project";
	public static String downloaderOption = "downloader-jar";	
		
	/* Command option variables */
	static public String helpVar = "help";
	static public String languageVar = "LANGUAGE";
	static public String platformVar = "PLATFORM";
	static public String repositoryVar = "REPOSITORY";
	static public String threadVar = "THREAD";	
	static public String fileVar = "FILE";
	static public String logDirVar = "LOG_DIR";
	static public String outputVar = "OUTPUT";
	static public String minCommitsVar = "MIN_COMMITS";
		
	static public String urlVar = "URL_VAR";
	static public String createDateVar = "CREATE_DATE";
	static public String folderVar = "FOLDER";
	static public String projectNameVar = "PROJECT_NAME";
	static public String versionControlVar = "VERSION_CONTROL";
	
	public static String parallelVal = "PARALLEL_JOBS";
	public static String maxDownloadTimeVal = "MAX_MINUTE";
	public static String updateFrequencyVal = "UPDATE_FREQUENCY";	
	public static String maxProjectVal = "MAX_PROJECT";
	public static String downloaderVal = "DOWNLOADER";
	
	/* Command descriptions */
	static public String collectProjectDescription = "Collect project information (such as name, source repository link) from multiple platforms, such as Github, CodePlex. This command goes to project hosting platforms and scrape the most recently updated projects from the specified hosting platforms. At a point of time, only part of the project can be detected and collected; so this command should be executed frequently to collect as many projects as possible. The collected data is printed to the standard output in JSON format. Each line is a JSON object representing a single project.\n";
	static public String downloadProjectDescription = "Download project source code or update project source code if the project has already been downloaded.";
	static public String downloadManagemengDescription = "Manage project downloading. For example, initialize new project to download, kill overtime downloading process.";
	
	/* Help messages */
	static public String collectRepositoryMessage = "Should the link to project repositories be collected? It takes more time to collect this information. Default: false.";
	static public String platformMessage = "The platforms from which projects are collected. Format: comma separated case-insensitive non-spacing platform names, for example: github,codeplex. Supported platforms: github, codeplex.";
	static public String helpMessage = "Show this help message.";
	static public String languageMessage = "Specify a comma-separated case-insensitive non-spacing programming languages. Only projects written in either of the specified languages are collected.\nExample: --language java,c#.\nNo default.";
	static public String threadMessage = "Specify the maximal number of threads that can be used to perform the collection.";
	static public String projectFileMessage = "Specify the path to the file storing project descriptions (in JSON format). Only these projects need to have their repository links retrieved.";
	static public String logDirectoryMessage = "Specify the directory for storing log files. If not specified, the system's temporary directory will be used.";
	static public String outputMessage = "Specify the file to store all outputs. If not specified, output will be printed to standard output.";
	static public String minCommitsMessage = "Specify the minimum number of commits of a project in order to have that project downloaded. Only have effect if the platform is Github. Default: 100.";
	
	static public String urlOptionMessage = "Specify the check-out url of the project.";
	static public String createDateOptionMessage = "Specify the creation date of the project. Format: YYYY-MM-DD.";
	static public String projectNameOptionMessage = "Specify the name of the project to be downloaded.";
	static public String folderOptionMessage = "Specify the base folder to store the project. The downloaded project is stored at FOLDER/PLATFORM/CREATE_DATE/PROJECT_NAME";
	static public String platformMessage2 = "The platforms from which projects are collected. Format: a single platform name. Supported platforms: github, codeplex.";
	static public String versionControlMessage = "Specify the version control system for the project. Supported values: git, svn, mercurial.";
	
	public static String parallelOptMessage = "The number of maximum downloading jobs at the same time. Default: 10";
	public static String maxDownloadTimeMessage = "the maximum number of minutes that a download job is allowed to take. If the downloading cannot be finished after MAX_MINUTE, abort the downloading job. Default: 60";
	public static String updateFrequencyMessage = "If a project has not be updated for UPDATE_FREQUENCY days, perform an update. Default: 30.";
	public static String maxProjectMessage = "The maximum number of projects to download or update before current command to exit. Default: 0. 0 means as long as there are some projects to download or update, do not exit.";
	public static String downloaderMessage = "The absolute path to the downloader jar file.";
	
}
