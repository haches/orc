package com.orc.projectcollector.service;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.javautilities.jsap.commandline.OptionFactory;
import com.javautilities.logging.LogUtil;
import com.martiansoftware.jsap.FlaggedOption;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;
import com.martiansoftware.jsap.Switch;
import com.mysql.jdbc.log.LogUtils;
import com.orc.projectcollector.PlatformNames;
import com.orc.utilities.Logging;

public class DownloadPRojectCommand extends PlatformCommand {

	@Override
	public String getName() {
		return SC.downloadProject;
	}

	@Override
	public String getDescription() {
		return SC.downloadProjectDescription;
	}

	@Override
	public void execute(String[] args) {
		JSAPResult config = parseCommandLine(args);		
		if(config.success() && !config.getBoolean(SC.helpVar)) {
			String projectName = config.getString(SC.projectNameVar);
			String platform = config.getString(SC.platformVar);
			String url = config.getString(SC.urlVar);
			String baseFolder = config.getString(SC.folderVar);
			String createDate = config.getString(SC.createDateVar);
			String versionControl = config.getString(SC.versionControlVar);
			String folder = getProjectCheckoutFolder(platform, projectName, createDate, baseFolder);
			File f = new File(folder);
			boolean isNew = false;
			if(!f.exists()) {				
				isNew = true;
				try {
					FileUtils.forceMkdir(f);
				} catch (IOException e) {
					LogUtil.logError(logger, e);
				}
			}
			
			String cmd = getCheckoutCommand(versionControl, url, isNew);			
			try {
				String os = System.getProperty("os.name").toLowerCase();
				String[] command = null; 
				if(os.indexOf("win")>=0) {
					command = new String[] {"CMD", "/C", cmd};
				} else {					
				}
				
		        ProcessBuilder probuilder = new ProcessBuilder(command);
		        probuilder.directory(new File(folder));		        
		        Process process = probuilder.start();
		        process.waitFor();
			} catch (IOException e) {
				e.printStackTrace();
				LogUtil.logError(logger, e);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}		
	}
	
	private String getCheckoutCommand(String versionControl, String url, boolean isNew) {
		StringBuilder sb = new StringBuilder();
		if(isNew) {
			if(versionControl.equals("git")) {
				sb.append("git clone ");
				sb.append(url);
				sb.append(" .");
			} else {
				
			}
		} else {
			if(versionControl.equals("git")) {
				sb.append("git pull");
			}
		}
		return sb.toString();
	}
	
	/**
	 * Construct the directory to store checked out project files.
	 * @param platform
	 * @param projectName
	 * @param createDate
	 * @param baseFolder
	 * @return
	 */
	private String getProjectCheckoutFolder(String platform, String projectName, String createDate, String baseFolder) {
		StringBuilder folder = new StringBuilder();
		folder.append(baseFolder);
		folder.append(File.separatorChar);
		folder.append(platform);
		folder.append(File.separatorChar);
		folder.append(createDate);
		folder.append(File.separatorChar);
		if(platform.equals(PlatformNames.Github)) {
			int idx = projectName.indexOf('/');
			folder.append(projectName.substring(0, idx));
			folder.append(File.separatorChar);
			folder.append(projectName.substring(idx+1));
		} else {
			folder.append(projectName);
		}
		return folder.toString();
	}
	
	/**
	 * Logger
	 */
	private Logger logger;	

	@Override
	protected JSAP setupParser() {
		JSAP parser = new JSAP();
		
		FlaggedOption platformOpt = OptionFactory.flaggedOption(SC.platformOption, SC.platformVar, SC.platformMessage2, true);
		FlaggedOption projectNameOpt = OptionFactory.flaggedOption(SC.projetNameOption, SC.projectNameVar, SC.projectNameOptionMessage, true);
		FlaggedOption urlOpt = OptionFactory.flaggedOption(SC.urlOption, SC.urlVar, SC.urlOptionMessage, true);
		FlaggedOption createDateOpt = OptionFactory.flaggedOption(SC.createDateOption, SC.createDateVar, SC.createDateOptionMessage, true);
		FlaggedOption folderOption = OptionFactory.flaggedOption(SC.folderOption, SC.folderVar, SC.folderOptionMessage, true);
		FlaggedOption versionControlOption = OptionFactory.flaggedOption(SC.versionControlOption, SC.versionControlVar, SC.versionControlMessage, true);
		
		FlaggedOption logDirFlag =
				new FlaggedOption(SC.logDirVar)
				.setLongFlag(SC.logDirectionOption)
				.setStringParser(JSAP.STRING_PARSER);
			logDirFlag.setHelp(SC.logDirectoryMessage);
		
		try{
			parser.registerParameter(platformOpt);
			parser.registerParameter(projectNameOpt);
			parser.registerParameter(versionControlOption);
			parser.registerParameter(urlOpt);
			parser.registerParameter(createDateOpt);
			parser.registerParameter(folderOption);
			parser.registerParameter(logDirFlag);
			OptionFactory.registerHelpOption(parser);
		}catch(Exception e) {
			LogUtil.logError(logger, e);
		}
		return parser;
		
	}

}
