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
package org.jasig.portlet.calendar.adapter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.util.Set;
import javax.portlet.PortletRequest;
import net.fortuna.ical4j.model.component.VEvent;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.params.CoreConnectionPNames;
import org.jasig.portlet.calendar.CalendarConfiguration;
import org.jasig.portlet.calendar.caching.DefaultCacheKeyGeneratorImpl;
import org.jasig.portlet.calendar.caching.ICacheKeyGenerator;
import org.jasig.portlet.calendar.credentials.DefaultCredentialsExtractorImpl;
import org.jasig.portlet.calendar.credentials.ICredentialsExtractor;
import org.jasig.portlet.calendar.processor.ICalendarContentProcessorImpl;
import org.jasig.portlet.calendar.processor.IContentProcessor;
import org.jasig.portlet.calendar.url.DefaultUrlCreatorImpl;
import org.jasig.portlet.calendar.url.IUrlCreator;
import org.joda.time.Interval;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of {@link ICalendarAdapter} that uses Commons HttpClient for retrieving {@link
 * CalendarEventSet}s.
 *
 * <p>This bean requires an EhCache {@link Cache} be provided. This bean also depends on instances
 * of 3 different interfaces (default implementation listed in parenthesis):
 *
 * <ul>
 *   <li>{@link IUrlCreator} (default configuration: {@link DefaultUrlCreatorImpl})
 *   <li>{@link ICredentialsExtractor} (default: {@link DefaultCredentialsExtractorImpl})
 *   <li>{@link IContentProcessor} (default: {@link ICalendarContentProcessorImpl})
 * </ul>
 *
 * By specifying alternate implementations for these interfaces, multiple instances of this class
 * can be configured to consume {@link CalendarEventSet}s from a variety of different end points,
 * for example an RSS feed behind basic auth, a CalendarKey implementation behind a shared secret,
 * or behind CAS.
 *
 * @author Nicholas Blair, nblair@doit.wisc.edu
 */
public final class ConfigurableHttpCalendarAdapter<T> extends AbstractCalendarAdapter
    implements ICalendarAdapter {

  protected final Log log = LogFactory.getLog(this.getClass());

  private Cache cache;
  private IUrlCreator urlCreator = new DefaultUrlCreatorImpl();
  private ICredentialsExtractor credentialsExtractor = new DefaultCredentialsExtractorImpl();
  private IContentProcessor contentProcessor = new ICalendarContentProcessorImpl();
  private ICacheKeyGenerator cacheKeyGenerator = new DefaultCacheKeyGeneratorImpl();
  private String cacheKeyPrefix = "default";

  /** @param cache the cache to set */
  @Required
  public void setCache(Cache cache) {
    this.cache = cache;
  }

  /** @param urlCreator the urlCreator to set */
  public void setUrlCreator(IUrlCreator urlCreator) {
    this.urlCreator = urlCreator;
  }

  /** @param credentialsExtractor the credentialsExtractor to set */
  public void setCredentialsExtractor(ICredentialsExtractor credentialsExtractor) {
    this.credentialsExtractor = credentialsExtractor;
  }

  /** @param contentProcessor the contentProcessor to set */
  public void setContentProcessor(IContentProcessor contentProcessor) {
    this.contentProcessor = contentProcessor;
  }

  /** @param cacheKeyPrefix the cacheKeyPrefix to set */
  public void setCacheKeyPrefix(String cacheKeyPrefix) {
    this.cacheKeyPrefix = cacheKeyPrefix;
  }

  public void setCacheKeyGenerator(ICacheKeyGenerator cacheKeyGenerator) {
    this.cacheKeyGenerator = cacheKeyGenerator;
  }

  /**
   * Workflow for this implementation:
   *
   * <ol>
   *   <li>consult the configured {@link IUrlCreator} for the url to request
   *   <li>consult the cache to see if the fetch via HTTP is necessary (if not return the cached
   *       events)
   *   <li>if the fetch is necessary, consult the {@link ICredentialsExtractor} for necessary {@link
   *       Credentials}
   *   <li>Invoke retrieveCalendarHttp
   *   <li>Pass the returned {@link InputStream} into the configured {@link IContentProcessor}
   *   <li>Return the {@link CalendarEventSet}s
   * </ol>
   *
   * (non-Javadoc)
   *
   * @see
   *     org.jasig.portlet.calendar.adapter.ICalendarAdapter#getEvents(org.jasig.portlet.calendar.CalendarConfiguration,
   *     org.joda.time.Interval, javax.portlet.PortletRequest)
   */
  public CalendarEventSet getEvents(
      CalendarConfiguration calendarConfiguration, Interval interval, PortletRequest request)
      throws CalendarException {

    /*
     *  Some HTTP iCal providers, such as Google, don't allow you to specify
     *  the interval in the RESTful call so you get the whole calendar. To
     *  avoid receiving the entire calendar every time you need a specific
     *  interval, break up the caching into two stages.
     *
     *  Stage 1 caches the entire calendar (or partial if the REST call supports intervals).
     *
     *  Stage 2 filters the cached calendar down to the requested interval and
     *  caches the calendar events for that interval.
     */

    // Stage 1: Try to get the cached calendar.
    String url = this.urlCreator.constructUrl(calendarConfiguration, interval, request);
    log.debug("generated url: " + url);

    String intermediateCacheKey =
        cacheKeyGenerator.getKey(
            calendarConfiguration, interval, request, cacheKeyPrefix.concat(".").concat(url));

    T calendar;
    Element cachedCalendar = this.cache.get(intermediateCacheKey);
    if (cachedCalendar == null) {
      Credentials credentials = credentialsExtractor.getCredentials(request);
      // read in the data
      InputStream stream = retrieveCalendarHttp(url, credentials);
      // run the stream through the processor

      try {
        calendar = (T) contentProcessor.getIntermediateCalendar(interval, stream);
      } catch (CalendarException e) {
        log.error(
            "Calendar parsing exception: "
                + e.getCause().getMessage()
                + " from calendar at "
                + url);
        throw e;
      }

      // save the VEvents to the cache
      cachedCalendar = new Element(intermediateCacheKey, calendar);
      this.cache.put(cachedCalendar);
      if (log.isDebugEnabled()) {
        log.debug("Storing calendar cache, key:" + intermediateCacheKey);
      }
    } else {
      if (log.isDebugEnabled()) {
        log.debug("Retrieving calendar from cache, key:" + intermediateCacheKey);
      }
      calendar = (T) cachedCalendar.getObjectValue();
    }

    // The cache key for retrieving a calendar over HTTP may not include
    // the interval, so we need to add the current interval to the existing
    // cache key.  This might result in the interval being contained in the
    // key twice, but that won't hurt anything.
    String processorCacheKey = getIntervalSpecificCacheKey(intermediateCacheKey, interval);

    // Stage 2: Get the calendar event set for the requested interval from cache
    // or generate it from the calendar from stage 1.
    CalendarEventSet eventSet;
    Element cachedElement = this.cache.get(processorCacheKey);
    if (cachedElement == null) {
      Set<VEvent> events = contentProcessor.getEvents(interval, calendar);
      log.debug("contentProcessor found " + events.size() + " events");

      // Save the calendar event set to the cache.  Calculate how long
      // this event set should survive.  We don't want this event set to
      // survive beyond the expiration of the calendar from stage 1 or you
      // have the potential of having two sets of events with different
      // overlapping intervals displaying different data, assuming getting
      // the calendar in stage 1 returns the whole calendar and not just
      // the portion of the calendar within the desired interval.
      //
      // For instance this inconsistency in calendar event sets can happen
      // when you get the calendar and display a week, then
      // near the expiration of the calendar from stage 1 get events for a
      // month that contains the week.  If you then display the week again
      // after the calendar (stage 1) has expired, you could get a changed
      // calendar and generate different calendar events than what you'd see
      // in the month view until the stage-2-month calendar event set expires
      // and builds a calendar event set based on the same data as the week
      // was generated with.
      int timeToLiveInSeconds = -1;
      long currentTime = System.currentTimeMillis();
      if (cachedCalendar.getExpirationTime() > currentTime) {
        long timeToLiveInMilliseconds = cachedCalendar.getExpirationTime() - currentTime;
        timeToLiveInSeconds = (int) timeToLiveInMilliseconds / 1000;
      }
      eventSet =
          insertCalendarEventSetIntoCache(
              this.cache,
              processorCacheKey,
              events,
              timeToLiveInSeconds > 0 ? timeToLiveInSeconds : -1);
    } else {
      if (log.isDebugEnabled()) {
        log.debug("Retrieving calendar event set from cache, key:" + processorCacheKey);
      }
      eventSet = (CalendarEventSet) cachedElement.getObjectValue();
    }

    return eventSet;
  }

  protected String getIntervalSpecificCacheKey(String baseKey, Interval interval) {
    StringBuffer buf = new StringBuffer();
    buf.append(baseKey);
    buf.append(interval.toString());
    return buf.toString();
  }

  /**
   * Uses Commons HttpClient to retrieve the specified url (optionally with the provided {@link
   * Credentials}. The response body is returned as an {@link InputStream}.
   *
   * @param url URL of the calendar to be retrieved
   * @param credentials {@link Credentials} to use with the request, if necessary (null is ok if
   *     credentials not required)
   * @return the body of the http response as a stream
   * @throws CalendarException wraps all potential {@link Exception} types
   */
  protected InputStream retrieveCalendarHttp(String url, Credentials credentials)
      throws CalendarException {

    final int TIMEOUT_MS = 3 * 1000;
    final HttpClient client = new HttpClient();

    //fixing where broken https links never connect. just keeps retrying.
    client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT_MS );
    client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT_MS);
    client.getParams().setParameter("http.connection-manager.timeout", new Long(TIMEOUT_MS));

    if (null != credentials) {
      client.getState().setCredentials(AuthScope.ANY, credentials);
    }
    GetMethod get = null;

    if(url.contains("webcal://")){
      return tryWebcalHttps(get,url,client);
    }else{
      return tryHttp(get,url,client);
    }
  }

  protected InputStream tryHttp(GetMethod get, String url, HttpClient client) throws CalendarException{
      try {
          if (log.isDebugEnabled()) {
              log.debug("Retrieving calendar " + url);
          }

          get = new GetMethod(url);
          final int rc = client.executeMethod(get);
          if (rc == HttpStatus.SC_OK) {
                // return the response body
                return getCalendarResponse(get);
          } else {
              log.warn("HttpStatus for " + url + ":  " + rc);
              throw new CalendarException(
                      "Non-successful status code retrieving url '" + url + "';  status code: " + rc);
          }
      } catch(ConnectTimeoutException e) {
          log.warn("Error fetching iCalendar feed for "+ url, e);
          throw new CalendarException("Error fetching iCalendar feed for "+ url, e);
      } catch(SocketTimeoutException e){
          log.warn("Error fetching iCalendar feed for "+ url, e);
          throw new CalendarException("Error fetching iCalendar feed for "+ url, e);
      } catch (HttpException e) {
          log.warn("Error fetching iCalendar feed for "+ url, e);
          throw new CalendarException("Error fetching iCalendar feed for "+ url, e);
      } catch (IOException e) {
          log.warn("Error fetching iCalendar feed for "+ url, e);
          throw new CalendarException("Error fetching iCalendar feed for "+ url, e);
      } finally {
          if (get != null) {
              get.releaseConnection();
          }
      }
  }

    protected InputStream tryWebcalHttps(GetMethod get, String url, HttpClient client) throws CalendarException{
        try {

            if (log.isDebugEnabled()) {
                log.debug("Retrieving calendar " + url);
            }

            get = new GetMethod(url.replace("webcal://","https://"));
            final int rc = client.executeMethod(get);
            if (rc == HttpStatus.SC_OK) {
                // return the response body
                return getCalendarResponse(get);
            } else {
                log.warn("HttpStatus for https replacement of " + url + ":  " + rc+". Trying http.");
                get.releaseConnection();

                return tryWebcalHttp(get, url, client);
            }
        } catch(ConnectTimeoutException e) {
            log.warn("Timed out trying to connect to https replacement of "+ url +". Trying http.");
            get.releaseConnection();
            return tryWebcalHttp(get, url, client);
        } catch(SocketTimeoutException e){
            log.warn("Timed out between fetches to http replacement of "+ url +". Trying http.");
            get.releaseConnection();
            return tryWebcalHttp(get, url, client);
        }catch (HttpException e) {
            log.warn("Error fetching iCalendar feed for "+ url, e);
            throw new CalendarException("Error fetching iCalendar feed for "+ url, e);
        } catch (IOException e) {
            log.warn("Error fetching iCalendar feed", e);
            throw new CalendarException("Error fetching iCalendar feed for "+ url, e);
        } finally {
            if (get != null) {
                get.releaseConnection();
            }
        }
    }



    protected InputStream tryWebcalHttp(GetMethod get, String url, HttpClient client) throws CalendarException{
        try{
            get = new GetMethod(url.replace("webcal://","http://"));
            final int rc1 = client.executeMethod(get);
            if (rc1 == HttpStatus.SC_OK) {
                return getCalendarResponse(get);
            } else {
                log.warn("HttpStatus for http replacement of " + url + ":  " + rc1+". Link appears dead, throwing error.");
                throw new CalendarException(
                        "Non-successful status code retrieving url '" + url + "';  status code: " + rc1);
            }
        }catch(ConnectTimeoutException ee){
            log.warn("Timed out trying to connect to http replacement of "+ url +". Link appears dead, throwing error.", ee);
            throw new CalendarException("Error fetching iCalendar feed for "+ url, ee);
        }catch(SocketTimeoutException ee){
            log.warn("Timed out between fetches to http replacement of "+ url +". Link appears dead, throwing error.", ee);
            throw new CalendarException("Error fetching iCalendar feed for "+ url, ee);
        }catch (HttpException ee) {
            log.warn("Error fetching iCalendar feed", ee);
            throw new CalendarException("Error fetching iCalendar feed for "+ url, ee);
        } catch (IOException ee) {
            log.warn("Error fetching iCalendar feed", ee);
            throw new CalendarException("Error fetching iCalendar feed for "+ url, ee);
        } finally {
            get.releaseConnection();
        }
    }

    protected InputStream getCalendarResponse(GetMethod get) throws IOException{
        log.debug("request completed successfully");
        final InputStream responseBody = get.getResponseBodyAsStream();

        /*
         * CAP-207:  Some HTTP endpoints seem to provide XML documents
         * that begin with a "Byte Order Mark" which causes a parsing
         * failure.  This BOMInputStream strips the BOM is present.
         */
        final BOMInputStream bomIn = new BOMInputStream(responseBody);

        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        IOUtils.copyLarge(bomIn, buffer);
        return new ByteArrayInputStream(buffer.toByteArray());
    }
}
