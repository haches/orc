package com.orc.projectcollector;

import java.util.List;

/**
 * Interface to receive retrieved projects
 */
public interface IProjectObserver {
	
	/**
	 * 
	 * @param projects List of projects that are retrieved
	 */
	public void receive(List<ProjectDescription> projects);
}
