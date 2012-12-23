package com.orc.projectcollector;

import java.util.HashSet;

import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.javautilities.logging.LogUtil;
import com.orc.projectcollector.service.VersionControlNames;

public class GoogleCodeProjectDetailsCollector extends ProjectDetailsCollector {

	public GoogleCodeProjectDetailsCollector(Logger logger) {
		this.logger = logger; 
	}
	@Override
	protected void collect(ProjectDescription prj, Document doc) {
		if(doc==null) {
			return;
		}
		// Check if this is an open-source project.
		boolean hasSource = false;
		String sourcePageUrl = "";
		for(Element e : doc.select("div#mt > a")) {
			if(e.text().equals("Source")) {
				sourcePageUrl = "http://code.google.com" + e.attr("href").trim();
				hasSource = true;
			}
		}
		if(!hasSource) {
			return;
		}
		
		String[] sourceInfo = getSourceInfo(prj.getProjectName(), sourcePageUrl);
		if(sourceInfo==null) {
			return;
		}

		for(Element e : doc.select("div#maincol table td#wikicontent")) {
			prj.setDescription(e.text());
		}
		for(Element e : doc.select("span#star_count")) {
			int star = Integer.valueOf(e.text().trim());
			prj.setStar(star);
		}
		
		boolean foundLicense = false;
		for(Element e: doc.select("tr.pscontent td.pscolumnl ul.pslist li")) {
			if(foundLicense) {
				for(Element lnk : e.select("a")) {
					prj.setLicense(lnk.text().trim());
					break;
				}
				break;
			} else {
				if(e.text().equals("Code license")) {				
					foundLicense = true;
				}				
			}
		}
		
		HashSet<String> labels = new HashSet<String>();
		for(Element e: doc.select("tr.pscontent td.pscolumnl ul.pslist li")) {
			for(Element span : e.select("span#project_labels")) {
				for(Element lnk : span.select("a")) {
					labels.add(lnk.text().trim());
				}
			}		
		}
		prj.setLabels(labels);		
		prj.setVersionControlType(sourceInfo[0]);
		prj.setSourceLink(sourceInfo[1]);
	}
	
	/**
	 * Return code code information
	 * @param prjName Name of the project
	 * @param sourcePageUrl The URL of the source page
	 * @return The first element is version control type, for example, subversion. The second element is the link to checkout source.
	 */
	private String[] getSourceInfo(String prjName, String sourcePageUrl) {
		String[] result = null;
		try{
			Document dom = Jsoup.connect(sourcePageUrl).get();
			if(dom!=null) {
				for(Element e : dom.select("tt#checkoutcmd")) {
					String cmd = e.text().trim();
					String[] ws = cmd.split(" ");
					if(ws[0].equals(VersionControlNames.svnCmd)) {
						result = new String[] { VersionControlNames.subversion, ws[2] };						
					} else if(ws[0].equals(VersionControlNames.gitCmd)) {
						result = new String[] { VersionControlNames.git, ws[2] };
					} else if(ws[0].equals(VersionControlNames.mercurialCmd)) {
						result = new String[] { VersionControlNames.mercurial, ws[2] };
					} else {
						System.out.println("Bad: " + cmd);
					}
				}				
			}
		}catch(Exception e) {
			LogUtil.logError(logger, e);
		}
		return result;
	}

	@Override
	public String getPlatform() {
		return PlatformNames.Googlecode;
	}


}
