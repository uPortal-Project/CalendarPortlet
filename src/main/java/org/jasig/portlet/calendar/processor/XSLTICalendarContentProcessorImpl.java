/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portlet.calendar.processor;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import net.fortuna.ical4j.model.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portlet.calendar.adapter.CalendarException;
import org.joda.time.Interval;

/**
 * This {@link IContentProcessor} implementation uses XSLT to transform an XML
 * stream into iCal. The iCal is then extracted into {@link CalendarEventSet}s.
 *
 * @author Anthony Colebourne
 * @version $Header: XSLTICalendarContentProcessorImpl.java Exp $
 */
public class XSLTICalendarContentProcessorImpl extends ICalendarContentProcessorImpl {

    protected final Log log = LogFactory.getLog(this.getClass());

    public XSLTICalendarContentProcessorImpl() {
        super();
    }

    public XSLTICalendarContentProcessorImpl(String xslFile) {
        super();
        this.xslFile = xslFile;
    }

    @Override
    public Calendar getIntermediateCalendar(Interval interval, InputStream in) {
        InputStream ical = transformToICal(in);
        return super.getIntermediateCalendar(interval, ical);
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
