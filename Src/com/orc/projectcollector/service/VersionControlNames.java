package com.orc.projectcollector.service;

public class VersionControlNames {

	public static String git = "git";
	public static String subversion = "subversion";
	public static String mercurial = "mercurial";
	
	public static String gitCmd = "git";
	public static String svnCmd = "svn";
	public static String mercurialCmd = "hg";
	
	public static String gitFolder = ".git";
	public static String svnFolder = ".svn";
	public static String mercurialFolder = ".hg";
	
	/**
	 * Return the command from a version control type
	 * @param versionControlType
	 * @return
	 */
	public static String getCommandName(String versionControlType) {
		if(versionControlType.equals(git)) {
			return gitCmd;
		} else if(versionControlType.equals(subversion)) {
			return svnCmd;
		} else if(versionControlType.equals(mercurial)) {
			return mercurialCmd;
		}
		return "";
	}
	
	public static String getFolder(String versionControlType) {
		if(versionControlType.equals(git)) {
			return gitFolder;
		} else if(versionControlType.equals(subversion)) {
			return svnFolder;
		} else if(versionControlType.equals(mercurial)) {
			return mercurialFolder;
		}
		return "";		
	}

}
