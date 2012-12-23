package com.orc.projectcollector;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.javautilities.date.DateUtil;
import com.orc.projectcollector.service.VersionControlNames;

public class BitbucketProjectDetailsCollector extends ProjectDetailsCollector {

	public BitbucketProjectDetailsCollector(Logger logger) {
		this.logger = logger; 
	}
	
	@Override
	protected void collect(ProjectDescription prj, Document doc) {
		if(doc==null) {
			return;
		}
		Elements readmes = doc.select("section#readme article.readme");
		if(readmes.size()==1) {
			prj.setDescription(readmes.get(0).text().trim());
		}
		for(Element e : doc.select("section#repo-stats dt")) {
			if(e.text().equals("Created")) {
				String time = e.nextElementSibling().select("time").text();
				prj.setCreatedDate(DateUtil.dateFromString(time));
			} else if(e.text().equals("Type")) {
				String versionControlType = e.nextElementSibling().text().toLowerCase();
				if(versionControlType.equals(VersionControlNames.git)) {
					String sourceLink = "https://bitbucket.org/" + prj.getOwner() + "/" + prj.getProjectName() + ".git";
					prj.setSourceLink(sourceLink);
					prj.setVersionControlType(VersionControlNames.git);
				} else if(versionControlType.equals(VersionControlNames.mercurial)) {
					String sourceLink = "https://bitbucket.org/" + prj.getOwner() + "/" + prj.getProjectName();
					prj.setSourceLink(sourceLink);
					prj.setVersionControlType(VersionControlNames.mercurial);
				}
			} else if(e.text().equals("Language")) {
				String language = e.nextElementSibling().text().toLowerCase();
				prj.setLanguage(language);
			}
		}		
	}

	@Override
	public String getPlatform() {
		// TODO Auto-generated method stub
		return null;
	}

}
