package com.orc.projectcollector.test;

import java.io.File;


import junit.framework.TestCase;
import org.junit.Test;

import com.orc.projectcollector.GithubProjectDetailsCollector;
import com.orc.projectcollector.PlatformNames;
import com.orc.projectcollector.ProjectDescription;

/**
 * Tests for GithubProjectDetailsCollector
 * Some tests need Internet to run.
 *
 */
public class GithubProjectDetailsCollectorTest extends TestCase {
	
	@Test	
	public void testCollectFromFile() {
		java.net.URL f = this.getClass().getResource("html/mono_github_homepage.htm");
		
		GithubProjectDetailsCollector c = new GithubProjectDetailsCollector(null);
		File html = new File(f.getPath());
		ProjectDescription prj = new ProjectDescription(PlatformNames.Github, "mono", "https://github.com/mono/mono", "csharp");
		c.collectFromFile(prj, html.getPath());
		
		assertEquals("mono", prj.getName());
		assertEquals("git://github.com/mono/mono.git", prj.getSourceLink());
		assertEquals("git", prj.getVersionControlType());
		assertEquals(1156, prj.getStar());
		assertEquals(412, prj.getFork());	
	}	
	
	@Test
	public void testCollectFromUrl() {
		GithubProjectDetailsCollector c = new GithubProjectDetailsCollector(null);
		ProjectDescription prj = new ProjectDescription(PlatformNames.Github, "mono", "https://github.com/mono/mono", "csharp");
		c.collect(prj);
		
		assertEquals("mono", prj.getName());
		assertEquals("git://github.com/mono/mono.git", prj.getSourceLink());		
	}	
}
