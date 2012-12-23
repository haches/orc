package com.orc.projectcollector;

/**
 * Constants for project hosting platform names
 *
 */
public class PlatformNames {
	static public String Github = "github";
	static public String CodePlex = "codeplex";
	static public String Bitbucket = "bitbucket";
	
	/**
	 * A string with all platforms separated by comma.
	 * @return
	 */
	static public String getAllPlatformsAsString() {
		StringBuilder sb = new StringBuilder();
		sb.append(Github);
		sb.append(',');
		sb.append(CodePlex);
		sb.append(',');
		sb.append(Bitbucket);
		return sb.toString();
		
	}
}
