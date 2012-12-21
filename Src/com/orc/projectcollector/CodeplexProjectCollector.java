package com.orc.projectcollector;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.orc.utilities.Logging;

public class CodeplexProjectCollector extends ProjectCollector {
	
	public CodeplexProjectCollector(String language, Logger logger) {
		this.language = language;
		this.logger = logger;
	}

	public CodeplexProjectCollector(String language, int maxThread, Logger logger) {
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
		Elements rows = doc.select("div#search_directory_row");
		LinkedList<ProjectDescription> prjs = new LinkedList<ProjectDescription>();
		for(int i=0; i<rows.size();i++) {
			Element row = rows.get(i);
			
			Elements as = row.select("h3 > a");
			Element a = as.get(0);
			String literalName = a.text();
			
			String link = a.attr("href");
			int idx = link.indexOf(".codeplex.com");
			
			String name = link.substring(7, idx);
			
			String description = "";
			Elements desps = row.select("div.clear + p");
			if(desps.size()>0) {
				description = desps.get(0).text();
			}

			// Collect labels and metrics
			int pageViews = 0;
			int downloads = 0;
			int contributors = 0;
			Calendar createdDate = Calendar.getInstance();
			
			HashSet<String> labels = new HashSet<String>();
			Elements paras = row.select("p.search_info");
			for(Element p : paras) {
				if(p.text().startsWith("Tags:")) {
					// Collect labels.
					Elements links = p.select("a");
					for(Element l : links) {
						labels.add(l.text());
					}
				} else if(p.text().contains("Page Views:")) {
					// Collect metrics
					int j = 0;
					
					for(Element e : p.select("span")) {
						if(j==0) {
							pageViews = Integer.valueOf(e.text());							
						} else if(j==1) {
							downloads = Integer.valueOf(e.text());
						} else if(j==2) {
							if(e.hasAttr("localtimeticks")) {
								long ticks = Integer.valueOf(e.attr("localtimeticks"));
								Calendar c = Calendar.getInstance();
								c.setTimeInMillis(ticks*1000);
								createdDate = c;						
							}
						}
						j++;
					}
				}
				
			}
					
			// Collect license.
			String license = "";
			for(Element e : row.select("a")) {
				if(e.attr("href").endsWith("license")) {
					license = e.text();
					break;
				} else if(e.attr("href").endsWith("team/view")) {
					contributors = Integer.valueOf(e.text());
				}
			}
			
			ProjectDescription prj = new ProjectDescription(getPlatformName(), name, link, language);
			prj.setLiteralName(literalName);
			prj.setDescription(description);
			prj.setLabels(labels);
			prj.setLicense(license);
			prj.setPageViews(pageViews);
			prj.setDownloads(downloads);
			prj.setContributors(contributors);
			prj.setCreatedDate(createdDate);
			prjs.add(prj);						
		}
		receiver.receive(prjs);
	}

	@Override
	public String getPlatformName() {
		return PlatformNames.CodePlex; 
	}
	
	/**
	 * Return a set of urls that need to be scraped to get project information.
	 * @param lan The programming language in which the projects are written 
	 * @return The set of urls
	 */
	private HashSet<String> urls(String lan) {
		HashSet<String> result = new HashSet<String>();
		String encodedLan = lan;
		try {
			encodedLan = URLEncoder.encode(lan, "utf8");
		} catch (UnsupportedEncodingException e) {
			Logging.log(logger, Level.ERROR, e);
		}
		String firstUrl = "http://www.codeplex.com/site/search?query=" + encodedLan + "&sortBy=Relevance&licenses=|&refinedSearch=true&size=100&page=0";
		result.add(firstUrl);
		
		try {
			Document dom = Jsoup.connect(firstUrl).get();
			Elements footer = dom.select("table#search_directory + ul#leftColumnWidth_pagination");
			Elements spans = footer.select("li:eq(0) > span");
			int prjCount = 0;
			int pageCount = 0;
			if(spans.size()==3) {
				prjCount = Integer.valueOf(spans.get(2).text());
				pageCount = prjCount / 100 + 1;
				for(int i=1; i<=pageCount; i++) {
					result.add("http://www.codeplex.com/site/search?query=" + encodedLan + "&sortBy=Relevance&licenses=|&refinedSearch=true&size=100&page=" + String.valueOf(i));
				}
			}
		} catch (IOException e) {
			Logging.logError(logger, e);
		}
		return result;
	}
	
}
