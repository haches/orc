package com.orc.projectcollector.test;

import java.io.File;
import java.util.HashSet;
import org.junit.Test;

import com.orc.projectcollector.CodeplexProjectCollector;

import junit.framework.TestCase;

/**
 * Tests for CodeplexProjectCollector
 * Some tests need Internet to run.
 *
 */
public class CodeplexProjectCollectorTest extends TestCase {

	@Test
	public void testCollectFromFile() {
		CodeplexProjectCollector c = new CodeplexProjectCollector("C#", null);
		SimpleProjectReceiver r = new SimpleProjectReceiver();
		java.net.URL f = this.getClass().getResource("html/codeplex_csharp.htm");
		File html = new File(f.getPath());
		c.collectFromFile(html.getPath(), r);
		HashSet<String> labels = new HashSet<String>();
		labels.add(".NET");
		labels.add("compiler");
		labels.add("cool");
		labels.add("operating system");
		labels.add("os");
		labels.add("Cosmos");
		labels.add("C#");
						
		String cosmosSig = c.getProjectSignature("cosmos");
		assertEquals(25, r.projects.size());
		assertEquals(true, r.projects.containsKey(cosmosSig));
		assertEquals("A complete operating system written in C#.", r.projects.get(cosmosSig).getDescription());
		assertEquals("Cosmos (C# Open Source Managed Operating System)", r.projects.get(cosmosSig).getLiteralName());		
		assertEquals("http://cosmos.codeplex.com/", r.projects.get(cosmosSig).getHomepage());
		assertTrue(labels.equals(r.projects.get(cosmosSig).getLabels()));
		assertEquals(true, r.projects.containsKey(c.getProjectSignature("sharpcompress")));			
	}
	
	@Test
	public void testCollectFromWeb() {
		CodeplexProjectCollector c = new CodeplexProjectCollector("C#", null);
		SimpleProjectReceiver r = new SimpleProjectReceiver();
		c.collect(r);
		assertTrue(r.projects.size() > 0);
	}
}
