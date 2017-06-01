package gov.nih.nci.hpc.web.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;

public class HpcBrowserEntry {
	@JsonView(Views.Public.class)
	private String name;

	@JsonView(Views.Public.class)
	private String id;

	@JsonView(Views.Public.class)
	private String fullPath;

	@JsonView(Views.Public.class)
	private String selectedNodePath;

	@JsonView(Views.Public.class)
	private String selectedNodeId;

	@JsonView(Views.Public.class)
	private List<HpcBrowserEntry> children;

	@JsonView(Views.Public.class)
	private boolean isCollection;

	@JsonView(Views.Public.class)
	private boolean populated;

	@JsonView(Views.Public.class)
	private boolean partial;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSelectedNodeId() {
		return selectedNodeId;
	}

	public void setSelectedNodeId(String selectedNodeId) {
		this.selectedNodeId = selectedNodeId;
	}

	public boolean isPopulated() {
		return populated;
	}

	public void setPopulated(boolean populated) {
		this.populated = populated;
	}

	public String getSelectedNodePath() {
		return selectedNodePath;
	}

	public void setSelectedNodePath(String selectedNodePath) {
		this.selectedNodePath = selectedNodePath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<HpcBrowserEntry> getChildren() {
		if (children == null)
			children = new ArrayList<HpcBrowserEntry>();
		return children;
	}

	public void setChildren(List<HpcBrowserEntry> children) {
		this.children = children;
	}

	public boolean isCollection() {
		return isCollection;
	}

	public void setCollection(boolean isCollection) {
		this.isCollection = isCollection;
	}

	public String getFullPath() {
		return fullPath;
	}

	public void setFullPath(String fullPath) {
		this.fullPath = fullPath;
	}

	public boolean isPartial() {
		return partial;
	}

	public void setPartial(boolean partial) {
		this.partial = partial;
	}
}
