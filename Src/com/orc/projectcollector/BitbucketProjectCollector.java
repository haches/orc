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

public class BitbucketProjectCollector extends ProjectCollector {

	public BitbucketProjectCollector(String language, Logger logger) {
		this.language = language;
		this.logger = logger;
	}

	public BitbucketProjectCollector(String language, int maxThread, Logger logger) {
		this.language = language;
		setMaxThreads(maxThread);
		this.logger = logger;
	}
	
	@Override
	public String getPlatformName() {
		return PlatformNames.Bitbucket;
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
		for(Element e : doc.select("li.iterable-item > article.repo-summary")) {
			Elements links = e.select("h1 > a");
			if(links.size()>0) {
				String link = links.get(0).attr("href").trim().substring(1);
				int idx = link.indexOf('/');
				String owner = link.substring(0, idx);
				String prjName = link.substring(idx+1);				
				String description = "";
				Elements desps = e.select("p");
				if(desps.size()>0) {
					description = desps.get(0).text().trim();
				}
				int commits = 0;
				int follows = 0;
				int forks = 0;
				for(Element me : e.select("ul.repo-metadata > li > a")) {
					String text = me.text().trim();
					if(text.endsWith("commits")) {
						String[] ws = text.split(" ");
						commits = getNumber(ws[0]);
					} else if(text.endsWith("followers")) {
						String[] ws = text.split(" ");
						follows = getNumber(ws[0]);
					} else if(text.endsWith("forks")) {
						String[] ws = text.split(" ");
						forks = getNumber(ws[0]);
					}
				}
				if(commits >= getMinCommits()) {
					String name = owner + "/" + prjName;
					String homePage = "https://bitbucket.org/" + name + "/overview";
					ProjectDescription prj = new ProjectDescription(getPlatformName(), name, homePage, getLanguage());
					prj.setOwner(owner);
					prj.setProjectName(prjName);
					prj.setLiteralName(name);
					prj.setFollow(follows);
					prj.setFork(forks);
					prj.setDescription(description);
					prjs.add(prj);									
				}
			}
		}
		receiver.receive(prjs);
	}
	
	private int getNumber(String text) {
		int result = 0;
		if(text.endsWith("k")) {
			result = (int)(Double.valueOf(text.substring(0, text.length()-1)) * 1000.0);
		} else {
			result = Integer.valueOf(text);
		}
		return result;
	}
	
	/**
	 * Return the set of urls need to be scraped to get project information.
	 * @param lan The name of the programming langauge
	 * @return Set of retrieved urls.
	 */
	private HashSet<String> urls(String lan) {
		HashSet<String> result = new HashSet<String>();
		String encodedLan = lan;

		try {
			encodedLan = URLEncoder.encode(lan, "utf8");
		} catch (UnsupportedEncodingException e) {
			Logging.log(logger, Level.ERROR, e);
		}
		String firstUrl = "https://bitbucket.org/repo/all/followers?name=" + encodedLan;
		result.add(firstUrl);

		try {
			Document dom = Jsoup.connect(firstUrl).get();
			Elements header = dom.select("section#repo-list > h1");
			String text = header.get(0).text();
			String[] words = header.text().split(" ");
			int prjCount = Integer.valueOf(words[1]);
			int pageCount = prjCount / 10 + 1;
			
			for(int i=1; i<pageCount; i++) {
				result.add("https://bitbucket.org/repo/all/followers/" + String.valueOf(i+1) + "?name=" + encodedLan);
			}
		} catch (IOException e) {
			Logging.logError(logger, e);
		}		
		return result;
	}


}
