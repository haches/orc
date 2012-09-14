package com.orc.projectcollector.service;

public class SC {
	
	/* Command names */
	static public String collectProject = "collect-project";
	
	/* Command option names */
	static public String languageOption = "language";
	static public String platformOption = "platform";
	static public String logDirectionOption = "log-directory";
	static public String threadOption = "thread";
	static public String repositoryOption = "repository";
	static public String helpOption = "help";
	static public String projectOption = "project"; 
	static public String outputOption = "output";
	
	/* Command option variables */
	static public String helpVar = "help";
	static public String languageVar = "language";
	static public String platformVar = "platform";
	static public String repositoryVar = "repository";
	static public String threadVar = "thread";	
	static public String fileVar = "file";
	static public String logDirVar = "logDir";
	static public String outputVar = "output";
	
	/* Command descriptions */
	static public String collectProjectDescription = "Collect project information (such as name, source repository link) from multiple platforms, such as Github, CodePlex. This command goes to project hosting platforms and scrape the most recently updated projects from the specified hosting platforms. At a point of time, only part of the project can be detected and collected; so this command should be executed frequently to collect as many projects as possible. The collected data is printed to the standard output in JSON format. Each line is a JSON object representing a single project.\n";	
	
	/* Help messages */
	static public String collectRepositoryMessage = "Should the link to project repositories be collected? It takes more time to collect this information. Default: false.";
	static public String platformMessage = "The platforms from which projects are collected. Format: comma separated case-insensitive non-spacing platform names, for example: github,codeplex. Supported platforms: github, codeplex.";
	static public String helpMessage = "Show this help message.";
	static public String languageMessage = "Specify a comma-separated case-insensitive non-spacing programming languages. Only projects written in either of the specified languages are collected.\nExample: --language java,c#.\nNo default.";
	static public String threadMessage = "Specify the maximal number of threads that can be used to perform the collection.";
	static public String projectFileMessage = "Specify the path to the file storing project descriptions (in JSON format). Only these projects need to have their repository links retrieved.";
	static public String logDirectoryMessage = "Specify the directory for storing log files. If not specified, the system's temporary directory will be used.";
	static public String outputMessage = "Specify the file to store all outputs. If not specified, output will be printed to standard output.";
}
