package com.orc.projectcollector;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.orc.utilities.DownloadUtilities;

public class CodeplexProjectDetailsCollector extends ProjectDetailsCollector {

	public CodeplexProjectDetailsCollector(Logger logger) {
		this.logger = logger; 
	}
	
	@Override
	public void collect(ProjectDescription prj, Document doc) {
		
		// Get the number of followers of the project. 
		int follows = 0;
		Elements followLinks = doc.select("div#favoriteProjectContainer > p.subtab_right > a");
		if(followLinks.size()>0) {
			String s = followLinks.get(0).text();
			s = s.substring(0, s.indexOf(' ')).trim();
			follows = Integer.valueOf(s);			
		}
		
		// Get project name.
		Elements nameLinks = doc.select("a#ctl00_ctl00_MasterContent_Tabs_sourceTab");
		String name = "";
		String sourcePage = "";
		if(nameLinks.size() > 0) {			
			sourcePage = nameLinks.get(0).attr("href"); 
			name = sourcePage.substring(7, sourcePage.indexOf(".codeplex.com/"));
		}
		
		Document sourceDom = DownloadUtilities.urlDocument(sourcePage, logger).get(sourcePage);;
		String vcType = "";
		String vcLink = "";
		
		// Check for subversion repository.
		if(sourceDom!=null) {
			Elements divs = sourceDom.select("div#SourceConnectInfoPanel div.modal_info div");		
			for(Element d : divs) {
				String text = d.text();
				if(text.contains("Subversion URL:")) {
					vcType = VersionControlNames.Subversion;
					vcLink = d.select("b").text();
					break;
				}
			}
		}
		
		// Check for mercurial repository.
		if(sourceDom!=null && vcLink.length()==0) {
			Elements divs = sourceDom.select("div#MercurialInfoPanel div.modal_info div");
			for(Element d : divs) {
				String text = d.text();
				if(text.contains("Clone URL:")) {
					vcType = VersionControlNames.Mercurial;
					vcLink = d.select("b").text();
					break;
				}
			}
		}
		
		// Check for git repository.
		if(sourceDom!=null && vcLink.length()==0) {
			Elements divs = sourceDom.select("div#GitInfoPanel div.modal_info div");
			for(Element d : divs) {
				String text = d.text();
				if(text.contains("Clone URL:")) {
					vcType = VersionControlNames.Git;
					vcLink = d.select("b").text();
					break;
				}
			}
		}
		
		if(vcLink.length()>0 && name.length() > 0) {
			prj.setVersionControlType(vcType);
			prj.setSourceLink(vcLink);
			prj.setFollow(follows);
		}
	}

	@Override
	public String getPlatform() {
		return PlatformNames.CodePlex;
	}

}
