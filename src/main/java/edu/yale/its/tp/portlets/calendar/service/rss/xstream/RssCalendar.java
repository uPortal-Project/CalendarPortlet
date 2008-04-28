package edu.yale.its.tp.portlets.calendar.service.rss.xstream;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias("rss")
public class RssCalendar {
	
   	@XStreamAsAttribute
	private String version;
   	
   	private RssChannel channel;
   	
   	public RssCalendar(String version, RssChannel channel) {
   		this.version = version;
   		this.channel = channel;
   	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public RssChannel getChannel() {
		return channel;
	}

	public void setChannel(RssChannel channel) {
		this.channel = channel;
	}

   	
   	
}
