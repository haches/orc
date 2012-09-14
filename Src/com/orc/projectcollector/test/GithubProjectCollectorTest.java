package com.orc.projectcollector.test;

import org.junit.Test;
import java.io.File;
import junit.framework.TestCase;

import com.orc.projectcollector.GithubProjectCollector;

/**
 * Tests for Github project collector.
 * Some tests need Internet connection to run.
 */
public class GithubProjectCollectorTest extends TestCase {
	
	@Test
	public void testCollectFromFile() {
		GithubProjectCollector c = new GithubProjectCollector("C#", null);
		SimpleProjectReceiver r = new SimpleProjectReceiver();
		java.net.URL f = this.getClass().getResource("html/github_csharp_updated.htm");
		File html = new File(f.getPath());
		c.collectFromFile(html.getPath(), r);		
		
		String sig1 = c.getProjectSignature("VVVV.Nodes.Mapping.Database");
		assertEquals(20, r.projects.size());		
		assertEquals(true, r.projects.containsKey(sig1));		
		assertEquals("https://github.com/elliotwoods/VVVV.Nodes.Mapping.Database", r.projects.get(sig1).getHomepage());
		assertEquals("", r.projects.get(sig1).getDescription());
		assertEquals("VVVV.Nodes.Mapping.Database", r.projects.get(sig1).getLiteralName());
		
		String sig2 = c.getProjectSignature("btw-samples");
		assertEquals(true, r.projects.containsKey(sig2));
		assertEquals("Official C# Reference Samples for Being The Worst podcast", r.projects.get(sig2).getDescription());
		assertEquals("btw-samples", r.projects.get(sig2).getLiteralName());

		String sig3 = c.getProjectSignature("testttt");
		assertEquals(true, r.projects.containsKey(sig3));		
		assertEquals("https://github.com/phiree/testttt", r.projects.get(sig3).getHomepage());
		assertEquals("testttt", r.projects.get(sig3).getLiteralName());
	}	
	
	@Test
	public void testCollectFromWeb() {
		GithubProjectCollector c = new GithubProjectCollector("C#", null);
		SimpleProjectReceiver r = new SimpleProjectReceiver();
		c.collect(r);
		
		assertEquals(true, r.projects.size() > 0);		
	}	
	
}
