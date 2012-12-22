package com.orc.projectcollector.service;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Calendar;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import com.javautilities.database.DbUtil;
import com.javautilities.jsap.commandline.OptionFactory;
import com.martiansoftware.jsap.JSAP;
import com.martiansoftware.jsap.JSAPResult;

abstract public class PlatformCommand {
	
	/**
	 * Get the name of the current command
	 * @return Name of current command
	 */
	abstract public String getName();
	
	/**
	 * Get the description of current command
	 * @return
	 */
	abstract public String getDescription();
	
	/**
	 * Given command line argument args, is current command executable?
	 * Each command has a action name (given by the getName method), which is the first command line argument.
	 * Executability can be decided by testing if the first element in args equals to  the action name. 
	 * @param args Command line arguments, with their order preserved.
	 * @return true if executable; false otherwise.
	 */
	public boolean isExectuable(String[] args) {
		return args.length > 0 && args[0].equals(getName());
	}
		
	/**
	 * Execute current command with command line arguments args
	 * @param args Command line arguments.
	 */
	abstract public void execute(String[] args);
	
	/**
	 * Setup command line parser.
	 * @return The command line option parser
	 */
	abstract protected JSAP setupParser();
		
	/**
	 * Print the parsing error and the help message
	 * @param parser Command line parser
	 * @param config The parsing result
	 */
	protected void printErrorAndHelp(JSAP parser, JSAPResult config) {
		if(!config.getBoolean(SC.helpOption)) {	
	        for (java.util.Iterator errs = config.getErrorMessageIterator(); errs.hasNext();) {
	        	System.err.println("Error: " + errs.next());
	        }					        				
		}
		System.err.println("");
	    System.err.println("Usage:\n\t" + getName() + " " + parser.getUsage());
        System.err.println("\nDescription:");
        System.err.println(getDescription());
        System.err.println("\nOptions:");
        System.err.println(parser.getHelp());		
	}
	
	/**
	 * Parse command line arguments args.
	 * If there is a parsing error or the help option is present, display some help message.
	 * @param args The command line arguments.
	 * @return The parsed result.
	 */
	protected JSAPResult parseCommandLine(String[] args) {
		String[] newArgs = Arrays.copyOfRange(args, 1, args.length);
		JSAP p = setupParser();		
		JSAPResult config = p.parse(newArgs);
		if(!config.success() || config.getBoolean(SC.helpVar)) {
			printErrorAndHelp(p, config);
		}
		return config;
	}	
	
	
	/**
	 * Logger
	 */
	protected Logger logger;	
	
	protected void setupLogger(JSAPResult c) {
		String logFile = logFilePath(c.contains(SC.logDirVar) ? c.getString(SC.logDirVar) : null);
		logger = Logger.getLogger("project-collector");				
		Appender appender;
		try {
			if(c.contains(SC.logDirVar)) {
				appender = new FileAppender(new SimpleLayout(), logFile);
			} else {
				appender = new ConsoleAppender(new SimpleLayout());
			}			
			
			logger.addAppender(appender);
		} catch (IOException e) {
			e.printStackTrace();
		}				
	}
	
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
	
	/**
	 * Get database connection from command line arguments.
	 * @param config
	 * @return
	 */
	protected Connection getConnection(JSAPResult config) {
		Connection result = DbUtil.getConnection(
				config.getString(OptionFactory.hostVar), 
				config.getString(OptionFactory.schemaVar), 
				"UTF-8", 
				config.getString(OptionFactory.userVar),
				config.getString(OptionFactory.passwordVar),
				config.getInt(OptionFactory.portVar));
		return result;
	}		
	
	/**
	 * Get operating system dependent command line.
	 * @param cmd
	 * @return
	 */
	protected String[] getCommand(String cmd) {
		String os = System.getProperty("os.name").toLowerCase();
		String[] command = null; 
		if(os.indexOf("win")>=0) {
			command = new String[] {"CMD", "/C", cmd};
		} else {
			command = new String[] {"/bin/bash", "-c", cmd};
		}
		return command;
	}
}
