/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jasig.portlet.calendar.mvc.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.portlet.Event;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import org.apache.commons.lang.StringUtils;
import org.jasig.portal.search.PortletUrl;
import org.jasig.portal.search.PortletUrlParameter;
import org.jasig.portal.search.SearchConstants;
import org.jasig.portal.search.SearchRequest;
import org.jasig.portal.search.SearchResult;
import org.jasig.portal.search.SearchResults;
import org.jasig.portlet.calendar.mvc.CalendarDisplayEvent;
import org.jasig.portlet.calendar.mvc.CalendarHelper;
import org.jasig.portlet.calendar.util.DateUtil;
import org.joda.time.DateMidnight;
import org.joda.time.Interval;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.portlet.bind.annotation.EventMapping;

/** @author Chris Waymire (chris@waymire.net) */
@Controller
@RequestMapping("VIEW")
public class SearchContentController {
  private static final DateTimeFormatter fmt_date = DateTimeFormat.forPattern("MMMM d, yyyy");
  private static final DateTimeFormatter fmt_time = DateTimeFormat.forPattern("m:h a");

  @Autowired(required = true)
  private CalendarHelper helper;

  @EventMapping(SearchConstants.SEARCH_REQUEST_QNAME_STRING)
  public void searchContent(EventRequest request, EventResponse response) {
    final Event event = request.getEvent();
    final SearchRequest searchQuery = (SearchRequest) event.getValue();
    final String[] searchTerms = searchQuery.getSearchTerms().split(" ");
    List<String> errors = new ArrayList<String>();
    DateMidnight start = new DateMidnight();
    Interval interval = DateUtil.getInterval(start, 180);
    Set<CalendarDisplayEvent> events = helper.getEventList(errors, interval, request);

    final SearchResults searchResults = new SearchResults();
    searchResults.setQueryId(searchQuery.getQueryId());
    searchResults.setWindowId(request.getWindowID());

    for (CalendarDisplayEvent e : events) {
      for (String term : searchTerms) {
        if (eventContainsTerm(e, term)) {
          final PortletUrl url = createPortletUrl(e);
          final SearchResult searchResult = new SearchResult();
          final String summary = createSearchResultSummary(e);
          searchResult.setTitle(e.getSummary());
          searchResult.setSummary(summary);
          searchResult.setPortletUrl(url);
          searchResult.getType().add("Calendar");
          searchResults.getSearchResult().add(searchResult);
        }
      }
    }
    if (!searchResults.getSearchResult().isEmpty()) {
      response.setEvent(SearchConstants.SEARCH_RESULTS_QNAME, searchResults);
    }
  }

  private boolean eventContainsTerm(final CalendarDisplayEvent event, final String term) {
    final String summary =
        StringUtils.isEmpty(event.getSummary()) ? "" : event.getSummary().toUpperCase();
    final String description =
        StringUtils.isEmpty(event.getDescription()) ? "" : event.getDescription().toUpperCase();
    final String check = StringUtils.isEmpty(term) ? "" : term.toUpperCase();
    return (!StringUtils.isEmpty(check)
        && (summary.contains(check) || description.contains(check)));
  }

  private PortletUrl createPortletUrl(CalendarDisplayEvent event) {
    final DateMidnight midnight = new DateMidnight(event.getDayStart());
    final Interval interval = DateUtil.getInterval(midnight, 1);
    final PortletUrl url = new PortletUrl();
    final PortletUrlParameter param = new PortletUrlParameter();
    param.setName("interval");
    param.getValue().add(interval.toString());
    url.getParam().add(param);
    return url;
  }

  private String createSearchResultSummary(CalendarDisplayEvent event) {
    StringBuilder summary = new StringBuilder(fmt_date.print(event.getDayStart()));
    if (!event.getDateStartTime().equals(event.getDateEndTime())) {
      summary
          .append(" ")
          .append(event.getDateStartTime())
          .append(" - ")
          .append(event.getDateEndTime());
    }
    return summary.toString();
  }
}
