package com.orc.projectcollector.test;

import java.util.HashMap;
import java.util.List;

import com.orc.projectcollector.IProjectObserver;
import com.orc.projectcollector.ProjectDescription;

class SimpleProjectReceiver implements IProjectObserver {
	
	public SimpleProjectReceiver() {
		projects = new HashMap<String, ProjectDescription>();
	}
	
	public HashMap<String, ProjectDescription> projects;
	
	@Override
	public void receive(List<ProjectDescription> received) {
		for(ProjectDescription p : received) {
			if(!projects.containsKey(p.getSignature())) {
				projects.put(p.getSignature(), p);
			}
		}			
	}
	
}
