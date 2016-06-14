# CalendarPortlet

This is a [Sponsored Portlet][] in the Apereo uPortal project.

See also [documentation in the external wiki][CalendarPortlet in Confluence].

## Migrating to v2.3.0

If using Exchange Web Services, the configuration.properties file has minor in order to support EWS Impersonation.  Several *Context.xml files are also updated in case you have overridden them in the uPortal overlays.

configuration.properties changes:
* ntlm.domain is renamed exchangeWs.ntlm.domain.  Also the meaning has changed a bit.  Refer to the comments in the file.

[Sponsored Portlet]: https://wiki.jasig.org/display/PLT/Jasig+Sponsored+Portlets
[CalendarPortlet in Confluence]: https://wiki.jasig.org/display/PLT/Calendar+Portlet
