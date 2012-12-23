package com.orc.projectcollector.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.log4j.Level;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.javautilities.database.manager.CommitMode;
import com.javautilities.database.manager.DataManager;
import com.javautilities.database.manager.DataManagerUtility;
import com.javautilities.database.manager.DataTable;
import com.javautilities.date.DateUtil;
import com.javautilities.jsap.commandline.OptionFactory;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPException;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.orc.projectcollector.BitbucketProjectCollector;
import com.orc.projectcollector.BitbucketProjectDetailsCollector;
import com.orc.projectcollector.CodeplexProjectCollector;
import com.orc.projectcollector.CodeplexProjectDetailsCollector;
import com.orc.projectcollector.GithubProjectCollector;
import com.orc.projectcollector.GithubProjectDetailsCollector;
import com.orc.projectcollector.GoogleCodeProjectCollector;
import com.orc.projectcollector.GoogleCodeProjectDetailsCollector;
import com.orc.projectcollector.IProjectObserver;
import com.orc.projectcollector.PlatformNames;
import com.orc.projectcollector.ProjectCollector;
import com.orc.projectcollector.ProjectDescription;
import com.orc.projectcollector.ProjectDetailsCollector;
import com.orc.utilities.Logging;

public class CollectProjectCommand extends PlatformCommand implements IProjectObserver{
			
	public CollectProjectCommand() {
	}
	
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
		} else if(platform.equals(PlatformNames.Bitbucket)) {
			return new BitbucketProjectDetailsCollector(logger);
		} else if(platform.equals(PlatformNames.Googlecode)) {
			return new GoogleCodeProjectDetailsCollector(logger);
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
	
	private Connection con;
				
	@Override
	public void execute(String[] args) {
		assert(isExectuable(args));
		JSAPResult config = parseCommandLine(args);		
		if(config.success() && !config.getBoolean(SC.helpVar)) {			
			try {
				// Setup logger.
				setupLogger(config);
				
				// Setup database.
				if(config.contains(OptionFactory.schemaVar)) {
					con = getConnection(config);
				}
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
			c.setMinCommits(config.getInt(SC.minCommitsVar));
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
					LinkedList<ProjectDescription> ps = new LinkedList<ProjectDescription>(); 
					for(Future<ProjectDescription> f : results) {
						ProjectDescription prjDesp = f.get();
						if(prjDesp!=null) {
							ps.add(prjDesp);													
						}
					}
					storeProject(ps);
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
		
	private void storeProject(LinkedList<ProjectDescription> projects) {
		if(con!=null) {
			DataManager dm = DataManager.getInstance();
			DataTable tbl = DataManagerUtility.newDataTable("osprojects", "projects", 
				new String[] {
					"platform",
					"name",
					"project_name",
					"owner",
					"language",
					"description",
					"version_control",
					"source_link",
					"homepage",
					"follows",
					"star",
					"fork",
					"labels",
					"created_date",
					"license",
					"page_views",
					"downloads",
					"contributors",
					"timestamp"
				}, 
				CommitMode.InsertOrElseUpdate, con, logger);
			String now = DateUtil.getNowDate();
			
			for(ProjectDescription p : projects) {
				if(p.getSourceLink()==null || p.getSourceLink().length()==0) {
					continue;
				}
				String[] row = {
						p.getPlatform(),
						p.getName(),
						p.getProjectName(),
						p.getOwner(),
						p.getLanguage(),
						p.getDescription(),
						p.getVersionControlType(),
						p.getSourceLink(),
						p.getHomepage(),
						String.valueOf(p.getFollow()),
						String.valueOf(p.getStar()),
						String.valueOf(p.getFork()),
						p.getLabelsAsString(),
						DateUtil.dashStringFromDate(p.getCreatedDate()),
						p.getLicense(),
						String.valueOf(p.getPageViews()),
						String.valueOf(p.getDownloads()),
						String.valueOf(p.getContributors()),
						now
					};
				tbl.addRow(row);
			}
			dm.AddTableAndCommit(tbl, con, logger);			
		}
		
		for(ProjectDescription p : projects) {
			if(p.getSourceLink().length()==0) {
				continue;
			}
			storeProject(p);
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
			//System.out.println(p.getPlatform() + ": " + p.getName());
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
			if(platforms.contains(PlatformNames.Bitbucket)) {
				BitbucketProjectCollector bCol = new BitbucketProjectCollector(lan, threadCount, logger);
				result.add(bCol);
			}
			if(platforms.contains(PlatformNames.Googlecode)) {
				GoogleCodeProjectCollector gCol = new GoogleCodeProjectCollector(lan, threadCount, logger);
				result.add(gCol);
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
		
		FlaggedOption minCommitsOpt = OptionFactory.flaggedIntOption(SC.minCommitsOption, SC.minCommitsVar, 2, SC.minCommitsMessage, true);
				
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
			parser.registerParameter(minCommitsOpt);
			OptionFactory.registerDatabaseOptions(parser);			
		} catch (JSAPException e) {
			Logging.log(logger, Level.ERROR, e);
		}
		return parser;
	}

}
