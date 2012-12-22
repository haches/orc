package com.orc.projectcollector.service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map.Entry;

import com.javautilities.database.DbUtil;
import com.javautilities.database.manager.CommitMode;
import com.javautilities.database.manager.DataManager;
import com.javautilities.database.manager.DataManagerUtility;
import com.javautilities.database.manager.DataTable;
import com.javautilities.date.DateUtil;
import com.javautilities.jsap.commandline.OptionFactory;
import com.javautilities.logging.LogUtil;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;

/**
 * Command to manage project downloading
 * @author jasonw
 *
 */
public class ProjectDownloadManagementCommand extends PlatformCommand {

	@Override
	public String getName() {
		return SC.downloadManagement;
	}

	@Override
	public String getDescription() {
		return SC.downloadManagemengDescription;
	}
	
	/**
	 * Database connection
	 */
	private Connection con;
	
	private HashMap<Process, Calendar> downloaders;

	@Override
	public void execute(String[] args) {
		JSAPResult config = parseCommandLine(args);
		setupLogger(config);
		if(config.success() && !config.getBoolean(SC.helpVar)) {
			con = getConnection(config);
			downloaders = new HashMap<Process, Calendar>();

			int parallelJobs = config.getInt(SC.parallelVal);
			int maxDownloadTime = config.getInt(SC.maxDownloadTimeVal);
			String folder = config.getString(SC.folderVar);
			int updateFrequency = config.getInt(SC.updateFrequencyVal);
			int maxProjects = config.getInt(SC.maxProjectVal);
			String language = config.getString(SC.languageVar);
			String downloader = config.getString(SC.downloaderVal);
			performDownloading(parallelJobs, maxDownloadTime, updateFrequency, folder, language, maxProjects, downloader);
		}
	}
	
	private void performDownloading(int maxDownloaders, int maxDownloadTime, int updateFrequency, String folder, String language, int maxProjects, String downloader) {
		int downloadedProjects = 0;
		while(true) {
			// Launch more downloading processor if necessary.
			if(maxDownloaders > downloaders.size()) {
				if(maxProjects==0 || maxProjects > downloadedProjects) {
					LinkedList<Process> processes = getDownloaderProcesses(maxDownloaders - downloaders.size(), updateFrequency, folder, language, downloader);
					downloadedProjects += processes.size();
					for(Process p : processes) {
						Calendar now = Calendar.getInstance();
						downloaders.put(p, now);
					}					
				}
				if(downloaders.size()==0) {
					break;
				}
			}
			
			// Sleep for a while.
			try {
				Thread.sleep(1000 * 5);
			} catch (InterruptedException e) {
				LogUtil.logError(logger, e);
			}
			
			LinkedList<Process> toBeRemoved = new LinkedList<Process>(); 
			for(Entry<Process, Calendar> p : downloaders.entrySet()) {
				Process prc = p.getKey();
				Calendar startTime = p.getValue();
				Calendar now = Calendar.getInstance();
				int diff = (int)(now.getTime().getTime() - startTime.getTime().getTime()) / 1000 / 60;
				boolean terminated = true;
				try{
					int exitValue = prc.exitValue();
					terminated = true;
				}catch(IllegalThreadStateException e) {
					//Process has not terminated yet. 
					terminated = false;
				}
				if(terminated) {
					try {
						prc.waitFor();
					} catch (InterruptedException e) {
						LogUtil.logError(logger, e);
					}
					toBeRemoved.add(prc);
				} else {
					if(diff > maxDownloaders) {
						System.out.println("Terminating.");
						prc.destroy();
						try {
							prc.waitFor();
						} catch (InterruptedException e) {
							LogUtil.logError(logger, e);
						}
						toBeRemoved.add(prc);
					}					
				}				
			}
			for(Process p : toBeRemoved) {
				downloaders.remove(p);
			}			
			
			SimpleDateFormat df = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
			String nowStr = df.format(new Date());
			System.out.println(nowStr + ": " + String.valueOf(downloadedProjects));
		}
		System.out.println("Done");
	}
	
	/**
	 * Start project downloading processes
	 * @param count The number of processes to start.
	 * @param updateFrequency Update-frequency of the projects
	 * @param folder The folder to store downloaded projects
	 * @param language The programming language.
	 * @param downloader The path to the downloader jar file.
	 * @return The list of downloading processes that have been started.
	 */
	private LinkedList<Process> getDownloaderProcesses(int count, int updateFrequency, String folder, String language, String downloader) {
		LinkedList<Process> result = new LinkedList<Process>();
		String now = DateUtil.getNowDate();
		String query = 
			"SELECT platform, name, created_date, version_control, source_link FROM projects WHERE " + 
			"language = '" + language + "' AND NOT source_link IS NULL AND " + "version_control='git' AND " +  
	        "(source_updated_date IS NULL OR DATEDIFF(source_updated_date, '" + now + "') > " + String.valueOf(updateFrequency) + ") LIMIT " + String.valueOf(count);
		
		
		ResultSet rs = DbUtil.resultFromQuery(con, query, logger);
		LinkedList<ProcessBuilder> pBuilders = new LinkedList<ProcessBuilder>();

		DataManager dm = DataManager.getInstance();
		DataTable tbl = DataManagerUtility.newDataTable("osprojects", "projects", 
			new String[] {
				"platform",
				"name",
				"source_updated_date"
			}, 
			CommitMode.Update, con, logger);
		try {
			while(rs.next()) {
				String platform = rs.getString(1);
				String project = rs.getString(2);
				String createDate = DateUtil.dashStringFromDate(DateUtil.dateToCalendar((rs.getDate(3))));				
				String versionControl = rs.getString(4);
				String sourceLink = rs.getString(5);
				ProcessBuilder pb = getProcessBuilder(folder, platform, project, createDate, versionControl, sourceLink, downloader);
				pBuilders.add(pb);
				
				String[] row = new String[] {
					platform,
					project,
					now
				};
				tbl.addRow(row);
			}
			if(tbl.rowCount()>0) {
				dm.AddTableAndCommit(tbl, con, logger);	
			}
			for(ProcessBuilder b : pBuilders) {
				try {
					Process prc = b.start();
					result.add(prc);
				} catch (IOException e) {
					LogUtil.logError(logger, e);
				}
			}
		} catch (SQLException e) {
			LogUtil.logError(logger, e);
		}
				
		return result;
	}
	
	private ProcessBuilder getProcessBuilder(String folder, String platform, String project, String createDate, String versionControl, String sourceLink, String downloader) {
		String cmd = 
			"java -jar " + downloader + " download-project --platform " + platform + 
			" --project " + project + 
			" --url " + sourceLink + " --create-date " + createDate + 
			" --folder " + folder + " --version-control " + versionControl;
		
		String[] command = getCommand(cmd); 	
        ProcessBuilder pb = new ProcessBuilder(command);
        pb.directory(new File(folder));		        
		return pb;
	}
	
	@Override
	protected JSAP setupParser() {
		JSAP parser = new JSAP();
		FlaggedOption parallelOpt = OptionFactory.flaggedIntOption(SC.parallelOpt, SC.parallelVal, 10, SC.parallelOptMessage, true);
		FlaggedOption maxDownloadTimeOpt = OptionFactory.flaggedIntOption(SC.maxDownloadTimeOpt, SC.maxDownloadTimeVal, 60, SC.maxDownloadTimeMessage, true);
		FlaggedOption locationOpt = OptionFactory.flaggedOption(SC.folderOption, SC.folderVar, SC.folderOptionMessage, true);
		FlaggedOption updateFrequencyOpt = OptionFactory.flaggedIntOption(SC.updateFrequencyOpt, SC.updateFrequencyVal, 30, SC.updateFrequencyMessage, true);
		FlaggedOption languageOpt = OptionFactory.flaggedOption(SC.languageOption, SC.languageVar, "C#", SC.languageMessage, true);
		FlaggedOption maxProjectOpt = OptionFactory.flaggedIntOption(SC.maxProjectOpt, SC.maxProjectVal, 0, SC.maxProjectMessage, true);
		FlaggedOption downloaderOpt = OptionFactory.flaggedOption(SC.downloaderOption, SC.downloaderVal, SC.downloaderMessage, true);
		
		FlaggedOption logDirFlag =
				new FlaggedOption(SC.logDirVar)
				.setLongFlag(SC.logDirectionOption)
				.setStringParser(JSAP.STRING_PARSER);
			logDirFlag.setHelp(SC.logDirectoryMessage);
		
		try{
			parser.registerParameter(parallelOpt);
			parser.registerParameter(maxDownloadTimeOpt);
			parser.registerParameter(locationOpt);
			parser.registerParameter(updateFrequencyOpt);
			parser.registerParameter(logDirFlag);
			parser.registerParameter(languageOpt);
			parser.registerParameter(maxProjectOpt);
			parser.registerParameter(downloaderOpt);
			OptionFactory.registerDatabaseOptions(parser);
			OptionFactory.registerHelpOption(parser);
		}catch(Exception e) {
			LogUtil.logError(logger, e);
		}		
		return parser;
	}


}