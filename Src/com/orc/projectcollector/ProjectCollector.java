package com.orc.projectcollector;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import com.orc.utilities.DownloadUtilities;
import com.orc.utilities.Logging;


abstract public class ProjectCollector {
	
	protected Logger logger;
	
	public String getLanguage() {
		return language;
	}

	/**
	 * Name of the programming language.
	 * Projects written in this language are to be collected
	 */
	String language;
	
	/**
	 * The maximum number of thread used for parallel downloading
	 */
	private int maxThreads = 20;
	
	/**
	 * The minimum number of commits a project should have in order for it to be downloaded
	 */
	private int minCommits;
		
	public int getMaxThreads() {
		return maxThreads;
	}

	public void setMaxThreads(int maxThreads) {
		this.maxThreads = maxThreads;
	}
	
	public int getMinCommits() {
		return minCommits;
	}

	public void setMinCommits(int minCommits) {
		this.minCommits = minCommits;
	}

	/**
	 * Name of the platform (such as Github)
	 * @return
	 */
	abstract public String getPlatformName();
	
	/**
	 * Collect projects and send those projects to the observer o.
	 * Since the collection may take a long time in some platform, the collected projects
	 * should be sent to the receiver often: typically, after a page of projects are collected, send them
	 * to the receiver. 
	 * Implementation wise: this method should figure out which pages to download, and download those pages, parse
	 * the downloaded pages into Document objects and call collectFromDocument to extract project information.
	 * @param receiver Project observer which gets notified when new projects are collected.
	 */
	abstract public void collect(IProjectObserver receiver);
		
	/**
	 * Parse the HTML document doc (which represents the project listing page from a platform) to extract project information, and notify receiver with the extracted project information.
	 * @param doc The DOM structure of a HTML page
	 * @param receiver Project observer which gets notified when new projects are collected.
	 */
	abstract public void collectFromDocument(Document doc, IProjectObserver receiver);
	
	/**
	 * Download html pages for urls and parse the downloaded pages into DOMs.
	 * @param urls The set of urls to download
	 * @return List of DOMs parsed from pages which are successfully downloaded.
	 */
	protected List<Document> domsFromUrls(HashSet<String> urls) {
		HashMap<String, Document> doms = DownloadUtilities.urlDocuments(urls, getMaxThreads(), logger);
		LinkedList<Document> result = new LinkedList<Document>();
		for(Entry<String, Document> p : doms.entrySet()) {
			Document dom = p.getValue();
			if(dom!=null) {
				result.add(dom);
			}			
		}		
		return result;
	}	
	
	/**
	 * Collect project information from a file given by path.
	 * @param path The absoluate path to the file storing a html page.
	 * @param receiver The receiver used to notify collected project information.
	 */
	public void collectFromFile(String path, IProjectObserver receiver) {
		try {
			Document doc = Jsoup.parse(new File(path), "UTF-8", "");
			collectFromDocument(doc, receiver);
		} catch (IOException e) {
			Logging.logError(logger, e);
		}		
	}	
	
	/**
	 * Get the signature of a project from its name
	 * @param projectName Name of the project
	 * @return Signature of the project, which is: platform_name.project_name, such as github.prjName.
	 */
	public String getProjectSignature(String projectName) {
		return getPlatformName() + "." + projectName;
	}	
}
