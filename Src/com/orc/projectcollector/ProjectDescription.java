package com.orc.projectcollector;

import java.util.HashSet;

import com.google.gson.Gson;

/**
 * Description of a project hosted in some platform
 */
public class ProjectDescription {
	
	public ProjectDescription(String platform, String name, String link, String language) {
		setPlatform(platform);
		setName(name);
		setHomepage(link);
		setLiteralName("");
		setDescription("");	
		setLabels(new HashSet<String>());
		setVersionControlType("");
		setSourceLink("");
		setSignature(platform + "." + name);
		setLanguage(language);
	}
		
	@Override
	public int hashCode() {
		return signature.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		boolean result = false;
		if(obj instanceof ProjectDescription) {
			ProjectDescription pd = (ProjectDescription)obj;
			result = getSignature().equals(pd.getSignature());
		}
		return result;
	}	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("platform:" + platform + "\n");
		sb.append("name:" + name + "\n");
		sb.append("literal name:" + literalName + "\n");
		sb.append("homepage:" + homepage + "\n");
		sb.append("version control:" + versionControlType + "\n");
		sb.append("repository:" + sourceLink + "\n");
		sb.append("labels:");
		int i=0;
		for(String s : labels) {
			if(i>0) {
				sb.append(", ");
			}
			sb.append(s);
			i++;
		}
		sb.append("follows:" + String.valueOf(follow) + "\n");
		sb.append("stars:" + String.valueOf(star) + "\n");
		sb.append("forks:" + String.valueOf(fork) + "\n");
		sb.append("description:\n");
		sb.append(description);		
		return sb.toString();		
	}
	
	/**
	 * @return Return a JSON representation of current project description
	 */
	public String toJson() {
		Gson gson = new Gson();
		return gson.toJson(this);
	}
	
	public HashSet<String> getLabels() {
		return labels;
	}
	
	public String getLabelsAsString() {
		StringBuilder sb = new StringBuilder();
		if(labels!=null) {
			for(String lbl : labels) {
				if(sb.length()>0) {
					sb.append(", ");
				}
				sb.append(lbl);
			}
		}
		return sb.toString();
	}

	public void setLabels(HashSet<String> labels) {
		this.labels = labels;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public String getPlatform() {
		return platform;
	}

	public void setPlatform(String platform) {
		this.platform = platform;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLiteralName() {
		return literalName;
	}

	public void setLiteralName(String literalName) {
		this.literalName = literalName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description.replace('\r', ' ').replace('\n', ' ');
	}

	public String getHomepage() {
		return homepage;
	}

	public void setHomepage(String link) {
		this.homepage = link;
	}

	public String getSourceLink() {
		return sourceLink;
	}

	public void setSourceLink(String sourceLink) {
		this.sourceLink = sourceLink;
	}

	public int getStar() {
		return star;
	}

	public void setStar(int star) {
		this.star = star;
	}

	public int getFork() {
		return fork;
	}

	public void setFork(int fork) {
		this.fork = fork;
	}

	public int getFollow() {
		return follow;
	}

	public void setFollow(int follow) {
		this.follow = follow;
	}

	public String getVersionControlType() {
		return versionControlType;
	}

	public void setVersionControlType(String versionControlType) {
		this.versionControlType = versionControlType;
	}
	
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}	

	/**
	 * Name of the project, this is the name of the project repository.
	 */
	private String name;
	
	/**
	 * Literal name of the project, sometimes it is the same as name, sometimes, it is a phrase which is easy to read.
	 * If there is no literal name for a project in some platform, this attribute is set to name.
	 */
	private String literalName;

	/**
	 * Description of the project.
	 * Empty string if no description is available.
	 */
	private String description;
	
	/**
	 * The URL to the project's homepage
	 */
	private String homepage;
	
	/**
	 * Platform (such as Github) from which the project belongs.
	 */
	private String platform;
	
	/**
	 * Signature of current project.
	 */
	private String signature;
	
	/**
	 * Labels of current project
	 */
	private HashSet<String> labels;
	
	/**
	 * Source code check-out link
	 */
	private String sourceLink;
		
	/**
	 * Stars of the project, available only from some platforms.
	 * Stars is an indication of a project's popularity.
	 */
	private int star;
	
	/**
	 * Number of forks of the project, only available from some platforms.
	 * Forks is a indication of a project's popularity.
	 */
	private int fork;
	
	/**
	 * Number of follows of the project, only available from some platforms.
	 * Follows is a indication of a project's popularity.
	 */
	private int follow;
	
	/**
	 * Version control type, such as git, subversion and mercurial.
	 */
	private String versionControlType;	
	
	/**
	 * Programming language
	 */
	private String language;

}
