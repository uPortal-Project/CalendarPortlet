/*
 * Created on Feb 5, 2008
 *
 * Copyright(c) Yale University, Feb 5, 2008.  All rights reserved.
 * (See licensing and redistribution disclosures at end of this file.)
 * 
 */
package edu.yale.its.tp.portlets.calendar;

import java.util.Comparator;

import net.fortuna.ical4j.model.component.VEvent;

/**
 * VEventStartComparator compares to VEvents and orders them
 * by starting date.  For events that start at the time, whichever
 * event ends first will be considered "first".
 * 
 * @author Jen Bourey
 */
public class VEventStartComparator implements Comparator<VEvent> {

	/*
	 * (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(VEvent event1, VEvent event2) {
		
		if (event1.getStartDate().getDate().before(event2.getStartDate().getDate()))
			return -1;
		else if (event1.getStartDate().getDate().after(event2.getStartDate().getDate()))
			return 1;
		else if (event1.getStartDate().getDate().equals(event2.getStartDate().getDate())) {
			if (event1.getEndDate() == null && event2.getEndDate() == null)
				return 0;
			else if (event1.getEndDate() == null)
				return -1;
			else if (event2.getEndDate() == null)
				return 1;
			if (event1.getEndDate().getDate().before(event2.getEndDate().getDate()))
				return -1;
			else if (event1.getEndDate().getDate().before(event2.getEndDate().getDate()))
				return 1;
		}

		int comp = 0;
		
		if (event1.getSummary() != null && event2.getSummary() != null) {
			comp = event1.getSummary().getValue().compareTo(event2.getSummary().getValue());
			if (comp != 0)
				return comp;
		}
		if (event1.getName() != null && event2.getName() != null) {
			comp = event1.getName().compareTo(event2.getName());
			if (comp != 0)
				return comp;
		}
		if (event1.getDescription() != null && event2.getDescription() != null) {
			comp = event1.getDescription().getValue().compareTo(event2.getDescription().getValue());
			if (comp != 0)
				return comp;
		}
		return 0;
	}

}


/*
 * VEventStartComparator.java
 * 
 * Copyright (c) Feb 5, 2008 Yale University. All rights reserved.
 * 
 * THIS SOFTWARE IS PROVIDED "AS IS," AND ANY EXPRESS OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE, ARE EXPRESSLY DISCLAIMED. IN NO EVENT SHALL
 * YALE UNIVERSITY OR ITS EMPLOYEES BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED, THE COSTS OF PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED IN ADVANCE OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Redistribution and use of this software in source or binary forms, with or
 * without modification, are permitted, provided that the following conditions
 * are met.
 * 
 * 1. Any redistribution must include the above copyright notice and disclaimer
 * and this list of conditions in any related documentation and, if feasible, in
 * the redistributed software.
 * 
 * 2. Any redistribution must include the acknowledgment, "This product includes
 * software developed by Yale University," in any related documentation and, if
 * feasible, in the redistributed software.
 * 
 * 3. The names "Yale" and "Yale University" must not be used to endorse or
 * promote products derived from this software.
 */