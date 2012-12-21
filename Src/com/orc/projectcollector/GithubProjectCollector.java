package com.orc.projectcollector;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;

import org.apache.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.javautilities.database.DbUtil;
import com.javautilities.date.DateUtil;
import com.javautilities.logging.LogUtil;
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
	public String getPlatformName() {
		return PlatformNames.Github;
	}

	@Override
	public void collect(IProjectObserver receiver) {
		String select = 
				"SELECT p.name, u.name as 'owner', COUNT(c.id) as 'commits', p.created_at, p.url, p.description " +
				"FROM projects p, commits c, users u " +
				"WHERE p.id = c.project_id AND language = '" + getLanguage() + "' AND u.id = p.owner_id " +
				"GROUP BY p.id " +
				"ORDER BY COUNT(c.id) DESC";
		Connection githubCon = DbUtil.getConnection("83.212.96.118", "github", "utf-8", "github", "github", 3306);
		ResultSet rs = DbUtil.resultFromQuery(githubCon, select, 100*1000, logger);
		int minCommits = getMinCommits();
		LinkedList<ProjectDescription> projects = new LinkedList<ProjectDescription>();
		try{
			while(rs.next()) {
				int commits = rs.getInt(3);
				if(commits < minCommits) {
					break;
				}
				String pName = rs.getString(1);
				String pOwner = rs.getString(2);
				Calendar pDate = Calendar.getInstance();
				pDate.setTime(rs.getDate(4));				
				String pUrl = rs.getString(5);
				String pDescription = rs.getString(6);				
				if(pOwner==null) {
					pOwner = "";
				}
				if(pDescription==null) {
					pDescription = "";
				}
				String u = pUrl.substring(29, pUrl.length());
				int idx = u.indexOf('/');
				pOwner = u.substring(0, idx);
				String homePage = "https://github.com/" + pOwner + "/" + pName;
				ProjectDescription prj = new ProjectDescription(getPlatformName(), pName, homePage, getLanguage());
				prj.setSourceLink("https://github.com/" + pOwner + "/" + pName + ".git");
				prj.setDescription(pDescription);
				prj.setCreatedDate(pDate);
				prj.setOwner(pOwner);
				prj.setName(pOwner + "/" + pName);
				projects.add(prj);
				if(projects.size()>10000) {
					receiver.receive(projects);
					projects = new LinkedList<ProjectDescription>();
				}										
			}
			if(projects.size()>0) {
				receiver.receive(projects);
			}
		}catch(Exception e) {
			LogUtil.logError(logger, e);
		}
		
	}

	@Override
	public void collectFromDocument(Document doc, IProjectObserver receiver) {
		// Do nothing.		
	}


}
