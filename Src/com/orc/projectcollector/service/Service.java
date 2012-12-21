package com.orc.projectcollector.service;

import java.util.LinkedList;
import java.util.List;

/**
 * Entry point class for all the facilities to collect project information
 * from different platforms.
 *
 */
public class Service {

	/**
	 * @param args 
	 */
	public static void main(String[] args) {
		Service s = new Service();
		boolean hasProcessed = false;
		for(PlatformCommand c : s.getAllCommands()) {
			if(c.isExectuable(args)) {
				hasProcessed = true;
				c.execute(args);
				break;
			}
		}
		
		if(!hasProcessed) {
			System.err.println("Use the following command lines to find more information:");
			for(PlatformCommand c : s.getAllCommands()) {
				System.err.println("\t" + c.getName() + " --help");
			}		
		}
	}
	
	/**
	 * @return The list of supported commands.
	 */
	private List<PlatformCommand> getAllCommands() {
		LinkedList<PlatformCommand> result = new LinkedList<PlatformCommand>();
		result.add(new CollectProjectCommand());
		result.add(new DownloadPRojectCommand());
		return result;
	}

}
