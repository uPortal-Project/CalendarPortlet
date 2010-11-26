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

------------------------
Blackboard Vista Adapter
------------------------

Why is this here instead of in src/main/java?

The Blackboard Vista adapter source code depends on proprietary Blackboard libraries that we can't
redistribute.  However, licensees of the Blackboard Vista product should have access to these
resources.


------------------
Required resources
------------------

Required jars:
	Blackboard Vista SDK client
	Techtrader Bytecode

These resources are available for download at http://www.edugarage.com/display/BBDN/downloads.


----------------------
Installing the adapter
----------------------

To use this adapter, first copy the Java file BlackboardVistaICalAdapter.java 
into the org.jasig.portlet.calendar.adapter package.  You will also need to get the 
Blackboard library jars, manually add them to your maven repository, and then reference them from
the project's pom.xml.  You will also need to add a bean definition for the adapter to
context/applicationContext.xml.  

Samples are provided below for both the pom.xml and applicationContext.xml additions.


Add the following to pom.xml:

    <!--  Blackboard dependencies -->
    <dependency>
        <groupId>blackboard</groupId>
        <artifactId>vista-sdk-client</artifactId>
        <version>4.2.0</version>
        <scope>compile</scope>
    </dependency>
    <dependency>
        <groupId>axis</groupId>
        <artifactId>axis</artifactId>
        <version>1.0</version>
        <scope>compile</scope>
    </dependency>
    <dependency>
        <groupId>techtrader</groupId>
        <artifactId>bytecode</artifactId>
        <version>unknown</version>
    </dependency>
    <dependency>
        <groupId>wsdl4j</groupId>
        <artifactId>wsdl4j</artifactId>
        <version>1.6.1</version>
    </dependency>


Add the following to applicationContext.xml:

	<bean id="org.jasig.portlet.calendar.adapter.BlackboardVistaICalAdapter" 
			class="org.jasig.portlet.calendar.adapter.BlackboardVistaICalAdapter">
		<property name="cache" ref="calendarCache"/>
	</bean>
	
Uncomment the Cached Credentials initialization service
	 <bean class="org.jasig.portlet.calendar.service.CachedCredentialsInitializationService">
	 </bean>
	
----------------------
Publishing a calendar
----------------------

When publishing a Blackboard calendar, you need to specify 'url' and 'glcid' Calendar parameters:

Example:
	url = https://blackboard.myschool.edu/webct/axis/
	glcid = URN:X-WEBCT-VISTA-V1:00000000-0000-0000-0000-000000000000
