package com.orc.projectcollector;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.orc.utilities.Logging;

public class GithubProjectCollector extends ProjectCollector {
	
	public GithubProjectCollector(String language, Logger logger) {
		this.language = language;
		this.logger = logger;
	}
	
	public GithubProjectCollector(String language, int maxThread, Logger logger) {
		this.language = language;
		setMaxThreads(maxThread);
		this.logger = logger;
	}

	@Override
	public void collect(IProjectObserver receiver) {
		String[] kinds = new String[] {"updated", "most_watched"};
		for(String k : kinds) {
			HashSet<String> urls = urls(language, k);			
			for(Document d : domsFromUrls(urls)) {				
				collectFromDocument(d, receiver);
			}
		}
	}	
	
	@Override
	public void collectFromDocument(Document doc, IProjectObserver receiver) {
		LinkedList<ProjectDescription> projects = new LinkedList<ProjectDescription>();
		String urlPrefix = "https://github.com";
		Elements rows = doc.select("table.repo tr");
		for(Element r : rows) {			
			Elements title = r.select("td.title a");
			if(title.size()>0) {
				String url = urlPrefix + title.get(0).attr("href");
				String name = title.get(0).text();
				ProjectDescription prj = new ProjectDescription(getPlatformName(), name, url, language);
				
				Element despRow = r.nextElementSibling();
				Elements despTds = despRow.select("td.desc");
				if(despTds.size()>0) {
					String description = despTds.get(0).text();
					prj.setDescription(description);
				}
				prj.setLiteralName(name);
				projects.add(prj);				
			}
		}
		receiver.receive(projects);
	}
		
	/**
	 * Return a set of urls that need to be scraped to get project information.
	 * @param lan The programming language in which the projects are written
	 * @param kind Kind of projects, possible values: "updated", "most_watched". 
	 * @return The set of urls
	 */
	private HashSet<String> urls(String lan, String kind) {
		HashSet<String> result = new HashSet<String>();
		String encodedLan = lan;
		try {
			encodedLan = URLEncoder.encode(lan, "utf8");
		} catch (UnsupportedEncodingException e) {
			Logging.logError(logger, e);
		}
		for(int i=1;i<=10;i++) {
			String url = "https://github.com/languages/" + encodedLan + "/" + kind;
			url = url + ((i<=1) ? "" : "?page=" + String.valueOf(i)); 				
			result.add(url);
		}
		return result;
	}

	@Override
	public String getPlatformName() {
		return PlatformNames.Github;
	}

}
