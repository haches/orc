package com.orc.projectcollector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.orc.utilities.Logging;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


public class GoogleCodeProjectCollector extends ProjectCollector {
	
	public GoogleCodeProjectCollector(String language, Logger logger) {
		this.language = language;
		this.logger = logger;
	}

	public GoogleCodeProjectCollector(String language, int maxThread, Logger logger) {
		this.language = language;
		setMaxThreads(maxThread);
		this.logger = logger;
	}

	@Override
	public void collect(IProjectObserver receiver) {
		HashSet<String> urls = urls(language);
		for(Document d : domsFromUrls(urls)) {
			collectFromDocument(d, receiver);
		}	
	}

	@Override
	public void collectFromDocument(Document doc, IProjectObserver receiver) {
		if(doc==null) {
			return;
		}
		LinkedList<ProjectDescription> prjs = new LinkedList<ProjectDescription>();
		for(Element e : doc.select("div#serp table > tbody > tr")) {
			Elements tds = e.select("td");
			if(tds.size()>=2) {
				Elements links = tds.get(1).select("a");
				if(links.size()>0) {
					String href = links.get(0).attr("href");
					String prjName = href.substring(3, href.length()-1);
					String homePage = "http://code.google.com/p/" + prjName;
					String name = prjName;
					ProjectDescription prj = new ProjectDescription(getPlatformName(), name, homePage, getLanguage());
					prjs.add(prj);
				}
			}
		}
		receiver.receive(prjs);
	}

	@Override
	public String getPlatformName() {
		return PlatformNames.Googlecode;
	}
	
	/**
	 * Return the set of urls need to be scraped to get project information.
	 * @param lan The name of the programming language
	 * @return Set of retrieved urls.
	 */
	private HashSet<String> urls(String lan) {
		HashSet<String> result = new HashSet<String>();
		String encodedLan = "label:" + lan;

		try {
			encodedLan = URLEncoder.encode(lan, "utf8");
		} catch (UnsupportedEncodingException e) {
			Logging.log(logger, Level.ERROR, e);
		}
		String firstUrl = "http://code.google.com/hosting/search?q=" + encodedLan;
		result.add(firstUrl);
				
		try {
			Document dom = Jsoup.connect(firstUrl).get();
			int prjCount = 0;
			for(Element e : dom.select("table.mainhdr td")) {
				if(e.textNodes().size()>0) {
					String txt = e.textNodes().get(0).text().trim();
					if(txt.startsWith("Results")) {
						String[] ws = txt.split(" ");
						prjCount = Integer.valueOf(ws[ws.length-1]);
						break;
					}
				}				
			}
			int pageCount = prjCount / 10 + 1;
			
			for(int i=1; i<pageCount; i++) {
				result.add("http://code.google.com/hosting/search?q=" + encodedLan + "&start=" + String.valueOf(i*10));
			}
		} catch (IOException e) {
			Logging.logError(logger, e);
		}		
		
		return result;
	}	

}
