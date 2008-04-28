package edu.yale.its.tp.portlets.calendar.service.rss.xstream;

import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("channel")
public class RssChannel {
	
	private String title;
	private String link;
	private String description;
	
	@XStreamImplicit(itemFieldName="item")
	private List<RssItem> items;
	
	public RssChannel(String title, String link, String description, List<RssItem> items) {
		this.title = title;
		this.link = link;
		this.description = description;
		this.items = items;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<RssItem> getItems() {
		return items;
	}

	public void setItems(List<RssItem> items) {
		this.items = items;
	}

	
	
}
