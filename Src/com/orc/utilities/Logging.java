package com.orc.utilities;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * Logging facilities
 *
 */
public class Logging {

	static public void logError(Logger logger, Exception e) {
		if(logger!=null) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage());
			sb.append(" ");
			logger.log(Level.ERROR, sb.toString(), e);			
		} else {
			e.printStackTrace();
		}
	}
	
	static public void log(Logger logger, Level level, Exception e) {
		if(logger!=null) {
			StringBuilder sb = new StringBuilder();
			sb.append(e.getMessage());
			sb.append(" ");
			logger.log(level, sb.toString(), e);			
		} else {
			e.printStackTrace();
		}
	}
	
	static public void log(Logger logger, Level level, String message, Exception e) {
		if(logger!=null) {
			StringBuilder sb = new StringBuilder();
			sb.append(message);
			sb.append(" ");
			sb.append(e.getMessage());
			sb.append("\n");
			logger.log(level, sb.toString(), e);			
		} else {
			e.printStackTrace();
		}
	}
	
	static public void logError(Logger logger, String message, Exception e) {
		if(logger!=null) {
			StringBuilder sb = new StringBuilder();
			sb.append(message);
			sb.append(" ");
			sb.append(e.getMessage());
			sb.append("\n");
			logger.log(Level.ERROR, sb.toString(), e);			
		} else {
			e.printStackTrace();
		}
	}	
}
