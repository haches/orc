package com.orc.projectcollector;

import org.jsoup.nodes.Document;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;


public class SourceforgeProjectCollector extends ProjectCollector {
	
	public SourceforgeProjectCollector(String language) {
		this.language = language;
	}

	@Override
	public void collect(IProjectObserver receiver) {
		throw new NotImplementedException();
	}

	@Override
	public void collectFromDocument(Document doc, IProjectObserver receiver) {
		throw new NotImplementedException();
	}

	@Override
	public String getPlatformName() {
		// TODO Auto-generated method stub
		return null;
	}

}
