package com.orc.projectcollector;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.orc.utilities.DownloadUtilities;
import com.orc.utilities.Logging;

/**
 * Class to scrape project details, such as source code link from a project's homepage
 *
 */
abstract public class ProjectDetailsCollector {
	
	protected Logger logger;
		
	/**
	 * Collect project detailed information, such as source code repository location
	 * for project prj.
	 * @param prj The project whose detailed information is to be collected.
	 */
	public void collect(ProjectDescription prj) {		
		Document doc = DownloadUtilities.urlDocument(prj.getHomepage(), logger).get(prj.getHomepage());
		if(doc!=null) {
			collect(prj, doc);
		}			
	}
		
	/**
	 * Collect project detailed information, such as source code repository location from a file
	 * param prj The project whose detailed information is to be collected.
	 * @param path The absolute path to the file storing the detailed information. 
	 */
	public void collectFromFile(ProjectDescription prj, String path) {
		File input = new File(path);
		Document doc;
		try {
			doc = Jsoup.parse(input, "UTF-8", "");
			collect(prj, doc);
		} catch (IOException e) {
			Logging.logError(logger, e);
		}		
	}
			
	/**
	 * Collect project detailed information from the HTML DOM doc.
	 * @param prj The project whose information is to be collected
	 * @param doc The DOM of the HTML containing detailed information
	 */
	abstract protected void collect(ProjectDescription prj, Document doc);
	
	/**
	 * Get the platform such as Github of current collector.
	 * @return The name of the platform.
	 */
	abstract public String getPlatform();	
	
}