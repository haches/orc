package com.orc.projectcollector.service;

import java.util.Arrays;

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
}
