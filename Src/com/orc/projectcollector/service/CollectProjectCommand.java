package com.orc.projectcollector.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.orc.projectcollector.CodeplexProjectCollector;
import com.orc.projectcollector.CodeplexProjectDetailsCollector;
import com.orc.projectcollector.GithubProjectCollector;
import com.orc.projectcollector.GithubProjectDetailsCollector;
import com.orc.projectcollector.IProjectObserver;
import com.orc.projectcollector.PlatformNames;
import com.orc.projectcollector.ProjectCollector;
import com.orc.projectcollector.ProjectDescription;
import com.orc.projectcollector.ProjectDetailsCollector;
import com.orc.utilities.Logging;

public class CollectProjectCommand extends PlatformCommand implements IProjectObserver{
		
	/**
	 * If the log-directory option is specified, the returned file will be in that directory;
	 * otherwise, use system temporary directory.
	 * @return Full path of log file. 
	 */
	static String logFilePath(String logDir) {
		String directory;
		String logPrefix = "collect_project_";
		if(logDir==null) {
			directory = System.getProperty("java.io.tmpdir");
		} else {
			directory = logDir;
		}

		Calendar now = Calendar.getInstance();
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy_MMM_dd_HH_mm_ss"); 
		String dateNow = formatter.format(now.getTime());
		return directory + File.separator + logPrefix + "_" + dateNow + ".log";
	}
	
	public CollectProjectCommand() {
	}
	
	/**
	 * Logger
	 */
	private Logger logger;
	
	/**
	 * Create specific project detailed information collector for given platform 
	 * @param platform The platform, such as github, codeplex
	 * @return The created collector
	 */
	private ProjectDetailsCollector getDetailsCollector(String platform) {
		if(platform.equals(PlatformNames.Github)) {
			return new GithubProjectDetailsCollector(logger);
		} else if(platform.equals(PlatformNames.CodePlex)) {
			return new CodeplexProjectDetailsCollector(logger);
		}
		return null;
	}
			
	@Override
	public String getName() {
		return SC.collectProject;
	}
	
	@Override
	public String getDescription() {
		return SC.collectProjectDescription;
	}
	
	/**
	 * Writer used to output result.
	 * If null, print result to standard output.
	 */
	private BufferedWriter outputWriter;
		
	@Override
	public void execute(String[] args) {
		assert(isExectuable(args));
		JSAPResult config = parseCommandLine(args);		
		if(config.success() && !config.getBoolean(SC.helpVar)) {			
			try {
				// Setup logger.
				String logFile = logFilePath(config.contains(SC.logDirVar) ? config.getString(SC.logDirVar) : null);
				logger = Logger.getLogger("project-collector");				
				org.apache.log4j.FileAppender appender = new FileAppender(new SimpleLayout(), logFile);
				logger.addAppender(appender);
				
				try{
					if(config.contains(SC.outputVar)) {
						outputWriter = new BufferedWriter(new FileWriter(new File(config.getString(SC.outputVar)), false));
					}
									
					// Collect project.
					collectedProjects = new LinkedList<ProjectDescription>();
					if(config.contains(SC.fileVar)) {				
						collectProjects(config.getString(SC.fileVar));
					} else {		
						collectProjects(config);
					}
					
					// Get repository links for collected projects. 
					if(config.getBoolean(SC.repositoryVar)) {
						collectProjectDetails(config, collectedProjects);
					}										
				}catch(Exception e) {
					Logging.logError(logger, e);
				}finally {
					if(outputWriter!=null) {
						outputWriter.flush();
						outputWriter.close();
					}
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Load project descriptions from file
	 * @param path Path to the file storing project descriptions.
	 */
	private void collectProjects(String path) {
		Gson gson = new Gson();
		try{
			FileInputStream fstream = new FileInputStream(path);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
	
			String line;
			while ((line = br.readLine()) != null)   {
			  try{
				  ProjectDescription prj = gson.fromJson(line, ProjectDescription.class);
				  if(prj!=null) {
					  collectedProjects.add(prj);
				  }				  
			  }catch(JsonSyntaxException e) {
				  Logging.log(logger, Level.ERROR, line, e);
			  }			  
			  catch(Exception e) {
				  Logging.log(logger, Level.ERROR, e);
			  }
			}	
			in.close();		
		}catch(Exception e) {
			Logging.log(logger, Level.ERROR, "abcd", e);
		}	
	}
	
	/**
	 * Collect projects (not including repository links) and store
	 * results in collectedProjects.
	 * @param config The parsed command line options.
	 */
	private void collectProjects(JSAPResult config) {
		for(ProjectCollector c : getProjectCollectors(config)) {
			c.collect(this);
		}		
	}
	
	/**
	 * Collect project details (including repository links) for projects
	 * @param config Parsed command line options
	 * @param projects List of projects whose details are to be collected.
	 */
	private void collectProjectDetails(JSAPResult config, List<ProjectDescription> projects) {		
		int threadCount = config.getInt(SC.threadVar);
		HashMap<String, List<ProjectDescription>> prjs = projectsByPlatform(projects);		
		HashSet<String> remove = new HashSet<String>();
		while(prjs.size()>0) {
			remove.clear();
			LinkedList<Callable<ProjectDescription>> tasks = new LinkedList<Callable<ProjectDescription>>();
			
			boolean batchOk = false;
			while(!batchOk) {
				for(Entry<String, List<ProjectDescription>> e : prjs.entrySet()) {
					List<ProjectDescription> ps = e.getValue();				
					if(ps.size()>0) {
						final ProjectDescription prj = ps.get(0);
						tasks.add(new Callable<ProjectDescription>() {
							@Override
							public ProjectDescription call() throws Exception {
								ProjectDetailsCollector c = getDetailsCollector(prj.getPlatform());
								c.collect(prj);
								return prj;
							}					
						});
						ps.remove(0);
					}
					if(ps.size()==0) {
						remove.add(e.getKey());
					}
					if(tasks.size()==threadCount) {
						batchOk = true;
						break;
					}				
				}
				for(String platform : remove) {
					prjs.remove(platform);
				}					
				if(prjs.size()==0) {
					batchOk = true;
				}
			}
			
			if(tasks.size()>0) {				
				ExecutorService s = null;
				try{
					s = Executors.newFixedThreadPool(tasks.size());
					List<Future<ProjectDescription>> results = s.invokeAll(tasks);
					for(Future<ProjectDescription> f : results) {
						ProjectDescription prjDesp = f.get();
						if(prjDesp!=null) {
							storeProject(prjDesp);							
						}
					}
				}catch(Exception e) {
					Logging.log(logger, Level.ERROR, e);
				}finally {
					if(s!=null) {
						s.shutdownNow();
					}
				}
			}
		}
	}
	
	/**
	 * Store project p in some media.
	 * @param p The project to store.
	 */
	private void storeProject(ProjectDescription p) {
		if(outputWriter!=null) {
			try{
				outputWriter.append(p.toJson());
				outputWriter.append("\n");		
			}catch(Exception e) {
				Logging.logError(logger, e);
			}
		} else {
			System.out.println(p.toJson());
		}
	}
	
	/**
	 * Group projects by their platforms
	 * @param projects 
	 * @return A hash-table whose keys are platform names, values are projects of the corresponding platforms.
	 */
	private HashMap<String, List<ProjectDescription>> projectsByPlatform(List<ProjectDescription> projects) {
		HashMap<String, List<ProjectDescription>> result = new HashMap<String, List<ProjectDescription>>();
		for(ProjectDescription p : projects) {
			String platform = p.getPlatform();
			List<ProjectDescription> list = null;
			if(result.containsKey(platform)) {
				list = result.get(platform);
			} else {
				list = new LinkedList<ProjectDescription>();
				result.put(platform, list);
			}
			list.add(p);
		}
		return result;
	}
	
	/**
	 * The list of project collected so far.
	 */
	private List<ProjectDescription> collectedProjects;
	
	@Override
	public void receive(List<ProjectDescription> projects) {
		collectedProjects.addAll(projects);
	}
	
	/**
	 * Setup the list of project collectors given configuration config from command line options.
	 * @param config Result from command line option parsing
	 * @return The list of project collectors
	 */
	private List<ProjectCollector> getProjectCollectors(JSAPResult config) {		
		LinkedList<ProjectCollector> result = new LinkedList<ProjectCollector>();
		int threadCount = config.getInt(SC.threadVar);
		HashSet<String> languages = getElementsFromString(config.getString(SC.languageVar), false);
		HashSet<String> platforms = getElementsFromString(config.getString(SC.platformVar), true);
		for(String lan : languages) {
			if(platforms.contains(PlatformNames.Github)) {
				GithubProjectCollector gCol = new GithubProjectCollector(lan, threadCount, logger);			
				result.add(gCol);				
			}			
			if(platforms.contains(PlatformNames.CodePlex)) {
				CodeplexProjectCollector cCol = new CodeplexProjectCollector(lan, threadCount, logger);			
				result.add(cCol);				
			}
		}
		return result;
	}
	
	/**
	 * @param lang String containing comma-separated non-spacing elements.
	 * @param toLower If true, all individual elements are turned to lower case.
	 * @return A set of individual elements. 
	 */
	private HashSet<String> getElementsFromString(String content, boolean toLower) {
		HashSet<String> result = new HashSet<String>();
		for(String e : content.split(",")) {
			if(e.length() > 0) {
				result.add(e);
			}
		}
		return result;
	}
	
	@Override
	protected JSAP setupParser() {		
		FlaggedOption platformFlag = 
			new FlaggedOption(SC.platformVar)
			.setLongFlag(SC.platformOption)
			.setDefault(PlatformNames.getAllPlatformsAsString())
			.setRequired(false)
			.setStringParser(JSAP.STRING_PARSER);
		platformFlag.setHelp(SC.platformMessage);

		FlaggedOption langNames =
			new FlaggedOption(SC.languageVar)
			.setLongFlag(SC.languageOption)
			.setStringParser(JSAP.STRING_PARSER)
			.setRequired(true);
		langNames.setHelp(SC.languageMessage);
		
		Switch repoSwitch = 
			new Switch(SC.repositoryVar)
			.setLongFlag(SC.repositoryOption);		
		repoSwitch.setHelp(SC.collectRepositoryMessage);
		
		FlaggedOption projectFlag = 
			new FlaggedOption(SC.fileVar)
			.setLongFlag(SC.projectOption)
			.setRequired(false)
			.setStringParser(JSAP.STRING_PARSER);
		projectFlag.setHelp(SC.projectFileMessage);
		
		FlaggedOption threadFlag = 
			new FlaggedOption(SC.threadVar)
			.setLongFlag(SC.threadOption)
			.setRequired(true)
			.setDefault("20")
			.setStringParser(JSAP.INTEGER_PARSER);
		threadFlag.setHelp(SC.threadMessage);
		
		FlaggedOption logDirFlag =
			new FlaggedOption(SC.logDirVar)
			.setLongFlag(SC.logDirectionOption)
			.setStringParser(JSAP.STRING_PARSER);
		logDirFlag.setHelp(SC.logDirectoryMessage);
						
		Switch helpSwitch =
			new Switch(SC.helpVar)
			.setLongFlag(SC.helpOption);
		helpSwitch.setHelp(SC.helpMessage);
		
		FlaggedOption outputFlag =
			new FlaggedOption(SC.outputVar)
			.setLongFlag(SC.outputOption)
			.setRequired(false);
		outputFlag.setHelp(SC.outputMessage);
				
		JSAP parser = new JSAP();
		try {
			parser.registerParameter(repoSwitch);
			parser.registerParameter(platformFlag);
			parser.registerParameter(langNames);
			parser.registerParameter(projectFlag);
			parser.registerParameter(outputFlag);
			parser.registerParameter(logDirFlag);
			parser.registerParameter(threadFlag);
			parser.registerParameter(helpSwitch);
			
		} catch (JSAPException e) {
			Logging.log(logger, Level.ERROR, e);
		}
		return parser;
	}

}