package com.orc.projectcollector;

/**
 * Constants for project hosting platform names
 *
 */
public class PlatformNames {
	static public String Github = "github";
	static public String CodePlex = "codeplex";
	
	/**
	 * A string with all platforms separated by comma.
	 * @return
	 */
	static public String getAllPlatformsAsString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Github);
		sb.append(',');
		sb.append(CodePlex);
		return sb.toString();
		
	}
}
