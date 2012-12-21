package com.orc.projectcollector;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;

public class GithubProjectDetailsCollector extends ProjectDetailsCollector {

	public GithubProjectDetailsCollector(Logger logger) {
		this.logger = logger;
	}
	
	@Override
	public void collect(ProjectDescription prj) {
		//Do nothing.
	}	
	
	@Override
	public void collect(ProjectDescription prj, Document doc) {
		//Do nothing.
	}
	
	@Override
	public void collectFromFile(ProjectDescription prj, String path) {
		//Do nothing.
	}	

	@Override
	public String getPlatform() {
		return PlatformNames.Github;
	}	

}
