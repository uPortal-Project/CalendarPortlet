package edu.yale.its.tp.portlets.calendar.processor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Set;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.fortuna.ical4j.model.Period;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import edu.yale.its.tp.portlets.calendar.CalendarEvent;
import edu.yale.its.tp.portlets.calendar.adapter.CalendarException;

/**
 * This {@link IContentProcessor} implementation uses XSLT to transform an XML
 * stream into iCal. The iCal is then extracted into {@link CalendarEvent}s.
 * 
 * @author Anthony Colebourne
 * @version $Header: XSLTICalendarContentProcessorImpl.java Exp $
 */
public class XSLTICalendarContentProcessorImpl extends ICalendarContentProcessorImpl {
	private Log log = LogFactory.getLog(this.getClass());

	public XSLTICalendarContentProcessorImpl() {
		super();
	}

	public XSLTICalendarContentProcessorImpl(String xslFile) {
		super();
		this.xslFile = xslFile;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.yale.its.tp.portlets.calendar.adapter.ContentProcessor#getEvents(
	 * java.lang.Long, net.fortuna.ical4j.model.Period, java.io.InputStream)
	 */
	public Set<CalendarEvent> getEvents(Long calendarId, Period period, InputStream in) {
		// do xslt
		InputStream ical = transformToICal(in);
	        
		return super.getEvents(calendarId, period, ical);
	}

	protected final InputStream transformToICal(InputStream in) throws CalendarException {

		StreamSource xmlSource = new StreamSource(in);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		
		try {
			log.debug("Stylesheet is "+xslFile);
			
			InputStream xsl = this.getClass().getClassLoader().getResourceAsStream(xslFile);
			
			Transformer tx = TransformerFactory.newInstance().newTransformer(new StreamSource(xsl));
			tx.transform(xmlSource,new StreamResult(out));

			log.debug(out.toString());
			
			InputStream result = new ByteArrayInputStream(out.toByteArray());
			
			return result;
		}
		catch(TransformerConfigurationException tce) {
			log.error("Failed to configure transformer",tce);
			throw new CalendarException("Failed to configure transformer",tce);
		}
		catch(TransformerException txe) {
			throw new CalendarException("Failed transformation",txe);
		}
	}

	private String xslFile;
	public void setXslFile(String xslFile) {
		this.xslFile = xslFile;
	}
}
