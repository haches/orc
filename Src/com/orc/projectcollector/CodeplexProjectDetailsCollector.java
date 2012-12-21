package com.orc.projectcollector;

import java.util.Calendar;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.Gson;
import com.orc.utilities.DownloadUtilities;

public class CodeplexProjectDetailsCollector extends ProjectDetailsCollector {

	public CodeplexProjectDetailsCollector(Logger logger) {
		this.logger = logger; 
	}
	
	private String getFollowUrl(String prjName) {
		String result = "http://" + prjName + ".codeplex.com/site/api/projects/" + prjName + "/followProject";
		return result;
	}
	
	class FollowInfo {
		public int TotalFollowers;
		public boolean IsFollowing;
	}
	
	@Override
	public void collect(ProjectDescription prj, Document doc) {
		
		// Get the number of followers of the project.
		int follows = 0;
		String followUrl = getFollowUrl(prj.getName());		
		String followContent =  DownloadUtilities.contentFromUrl(followUrl, "utf8", logger);		
		if(followContent!=null) {
			Gson gson = new Gson();
			FollowInfo f = gson.fromJson(followContent, FollowInfo.class);
			follows = f.TotalFollowers;
		}
		
		// Get created time.
		Elements dates = doc.select("div#current_rating span.smartDate");
		if(dates.size() > 0) {
			Element date = dates.get(0);
			String dateStr = date.attr("title");
			long ticks = Integer.valueOf(date.attr("localtimeticks"));
			Calendar c = Calendar.getInstance();
			c.setTimeInMillis(ticks*1000);
			prj.setCreatedDate(c);
		}
		
		// Get project name.
		Elements nameLinks = doc.select("a#sourceTab");
		String name = "";
		String sourcePage = "";
		if(nameLinks.size() > 0) {			
			sourcePage = nameLinks.get(0).attr("href"); 
			name = sourcePage.substring(7, sourcePage.indexOf(".codeplex.com/"));
		}
		
		if(sourcePage.length() > 0) {
			Document sourceDom = DownloadUtilities.urlDocument(sourcePage, logger).get(sourcePage);;
			String vcType = "";
			String vcLink = "";
			
			// Check for subversion repository.
			if(sourceDom!=null) {
				Elements divs = sourceDom.select("input#connecttext2");		
				for(Element d : divs) {
					String text = d.val();
					if(text.endsWith("svn")) {
						vcType = VersionControlNames.Subversion;
						vcLink = text;
						break;
					}
				}
			}
			
			// Check for mercurial repository.
			if(sourceDom!=null && vcLink.length()==0) {
				Elements divs = sourceDom.select("input#clonetext1");
				for(Element d : divs) {
					String text = d.val();
					if(text.startsWith("https://hg")) {
						vcType = VersionControlNames.Mercurial;
						vcLink = text;
						break;
					}
				}
			}
			
			// Check for git repository.
			if(sourceDom!=null && vcLink.length()==0) {
				Elements divs = sourceDom.select("input#clonetext1");
				for(Element d : divs) {
					String text = d.val();
					if(text.startsWith("https://git")) {
						vcType = VersionControlNames.Git;
						vcLink = text;
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
	}

	@Override
	public String getPlatform() {
		return PlatformNames.CodePlex;
	}

}
