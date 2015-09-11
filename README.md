# CalendarPortlet

TBD fill this in.

## Migrating to v2.3.0

If using Exchange Web Services, the configuration.properties file has minor in order to support EWS Impersonation.  Several *Context.xml files are also updated in case you have overridden them in the uPortal overlays.

configuration.properties changes:
* ntlm.domain is renamed exchangeWs.ntlm.domain.  Also the meaning has changed a bit.  Refer to the comments in the file.
