====
    Licensed to Jasig under one or more contributor license
    agreements. See the NOTICE file distributed with this work
    for additional information regarding copyright ownership.
    Jasig licenses this file to you under the Apache License,
    Version 2.0 (the "License"); you may not use this file
    except in compliance with the License. You may obtain a
    copy of the License at:

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on
    an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied. See the License for the
    specific language governing permissions and limitations
    under the License.
====

-----------------------
Oracle Calendar Adapter
-----------------------

Why is this here instead of in src/main/java?

The Oracle Calendar adapter source code depends on proprietary Oracle libraries that we can't
redistribute.  However, licensees of the Oracle Calendar product should have access to these
resources.


------------------
Required resources
------------------

Required jars:
	oracle.calendar/soap
	oracle.calendar/searchlet
	oracle.xml/parser

You should be able to find all three resources within the $ORACLE_HOME directory on
your calendar server.


----------------------
Installing the adapter
----------------------

To use this adapter, first copy the Java file OracleICalAdapter.java 
into the org.jasig.portlet.calendar.adapter package.  You will also need to get the 
Oracle library jars, manually add them to your maven repository, and then reference them from
the project's pom.xml.  You will also need to add a bean definition for the adapter to
context/applicationContext.xml.  

Samples are provided below for both the pom.xml and applicationContext.xml additions.


Add the following to pom.xml:

    <!--  Oracle Calendar dependencies  -->
    <dependency>
        <groupId>oracle.calendar</groupId>
        <artifactId>soap</artifactId>
        <version>unknown</version>
    </dependency>
    <dependency>
        <groupId>oracle.calendar</groupId>
        <artifactId>searchlet</artifactId>
        <version>unknown</version>
    </dependency>
    <dependency>
        <groupId>oracle.xml</groupId>
        <artifactId>parser</artifactId>
        <version>2</version>
    </dependency>


Add the following to applicationContext.xml:

	<bean id="org.jasig.portlet.calendar.adapter.OracleICalAdapter" 
			class="org.jasig.portlet.calendar.adapter.OracleICalAdapter">
		<property name="cache" ref="calendarCache"/>
	</bean>

	Uncomment the Cached Credentials initialization service
	 <bean class="org.jasig.portlet.calendar.service.CachedCredentialsInitializationService">
	 </bean>
	
----------------------
Publishing a calendar
----------------------

When publishing a Oracle calendar, you need to specify 'url' Calendar parameter:

Example:
	url = https://calendar.myschool.edu/ocws-bin/ocas.fcgi
	