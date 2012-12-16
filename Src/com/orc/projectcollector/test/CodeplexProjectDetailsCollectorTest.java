package com.orc.projectcollector.test;

import java.io.File;

import org.junit.Test;

import com.orc.projectcollector.CodeplexProjectDetailsCollector;
import com.orc.projectcollector.PlatformNames;
import com.orc.projectcollector.ProjectDescription;
import com.orc.projectcollector.VersionControlNames;

import junit.framework.TestCase;

/**
 * Tests for CodeplexProjectDetailsCollector
 * Some tests need Internet to run.
 *
 */
public class CodeplexProjectDetailsCollectorTest extends TestCase {

	@Test	
	public void testCollectGitProject() {
		CodeplexProjectDetailsCollector c = new CodeplexProjectDetailsCollector(null);
		ProjectDescription prj = new ProjectDescription(PlatformNames.CodePlex, "inputsimulator", "https://github.com/cstrahan/inputsimulator", "csharp");
		java.net.URL f = this.getClass().getResource("html/codeplex_inputsimulator_homepage.htm");
		File html = new File(f.getPath());
		c.collectFromFile(prj, html.getPath());		
		
		assertEquals(VersionControlNames.Git, prj.getVersionControlType());
		assertEquals("https://git01.codeplex.com/inputsimulator", prj.getSourceLink());		
	}	
	
	@Test
	public void testCollectMercurialProject() {
		CodeplexProjectDetailsCollector c = new CodeplexProjectDetailsCollector(null);
		ProjectDescription prj = new ProjectDescription(PlatformNames.CodePlex, "ajaxcontroltoolkit", "http://ajaxcontroltoolkit.codeplex.com/", "csharp");
		java.net.URL f = this.getClass().getResource("html/codeplex_ajaxcontroltoolkit_homepage.htm");
		File html = new File(f.getPath());
		c.collectFromFile(prj, html.getPath());		
		
		assertEquals(VersionControlNames.Mercurial, prj.getVersionControlType());
		assertEquals("https://hg.codeplex.com/ajaxcontroltoolkit", prj.getSourceLink());		
	}
	
	public void testCollectSubversionProject() {
		CodeplexProjectDetailsCollector c = new CodeplexProjectDetailsCollector(null);
		ProjectDescription prj = new ProjectDescription(PlatformNames.CodePlex, "cosmos", "http://cosmos.codeplex.com/", "csharp");
		java.net.URL f = this.getClass().getResource("html/codeplex_cosmos_homepage.htm");
		File html = new File(f.getPath());
		c.collectFromFile(prj, html.getPath());		
		
		assertEquals(VersionControlNames.Subversion, prj.getVersionControlType());
		assertEquals("https://IL2CPU.svn.codeplex.com/svn", prj.getSourceLink());		
		
	}

}
