package com.orc.projectcollector;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class GithubProjectDetailsCollector extends ProjectDetailsCollector {

	public GithubProjectDetailsCollector(Logger logger) {
		this.logger = logger;
	}
	
	@Override
	public void collect(ProjectDescription prj, Document doc) {			
		// Collect project description.
		String description = "";
		Elements desc = doc.select(".repository-description");
		if(desc.size()> 0) {
			description = desc.first().text();
			if(description.endsWith("— Read more")) {
				description = description.substring(0, description.length() - "— Read more".length()).trim();
			}			
		}
		
		// Collect project url.
		Elements links = doc.select(".public_clone_url > a");
		String link = "";
		if(links.size()> 0) {
			link = links.first().attr("href");
		}
		
		// Collect project star.
		int star = 0;
		Elements stars = doc.select("a.btn-star + a");		
		if(stars.size()>0) {
			String starCount = stars.get(0).text().replace(",", "");
			star = Integer.valueOf(starCount);			
		}
		
		// Collect project forks.
		int fork = 0;
		Elements forks = doc.select("a.btn-fork + a");
		if(forks.size()>0) {
			String forkCount = forks.get(0).text().replace(",", "");
			fork = Integer.valueOf(forkCount);		
		}
		
		prj.setStar(star);
		prj.setFork(fork);
		prj.setVersionControlType(VersionControlNames.Git);
		prj.setSourceLink(link);
	}

	@Override
	public String getPlatform() {
		return PlatformNames.Github;
	}	

}
