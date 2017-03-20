# CalendarPortlet

[![Build Status](https://travis-ci.org/Jasig/CalendarPortlet.svg?branch=master)](https://travis-ci.org/Jasig/CalendarPortlet)

This is a [Sponsored Portlet][] in the Apereo uPortal project.

## Configuration

See also [documentation in the external wiki][CalendarPortlet in Confluence].  Earlier documentation may be found there, and is still relevant.  We are in the process of moving documentation here.

### Using Encrypted Property Values

You may optionally provide sensitive configuration items -- such as database passwords -- in encrypted format.  Use the [Jasypt CLI Tools](http://www.jasypt.org/cli.html) to encrypt the sensitive value, then include it in a `.properties` file like this:

```
hibernate.connection.password=ENC(9ffpQXJi/EPih9o+Xshm5g==)
```

Specify the encryption key using the `UP_JASYPT_KEY` environment variable.

## Migrating to v2.3.0

If using Exchange Web Services, the `configuration.properties` file has minor in order to support EWS Impersonation.  Several `*Context.xml` files are also updated in case you have overridden them in the uPortal overlays.

`configuration.properties` changes:
* `ntlm.domain` is renamed `exchangeWs.ntlm.domain`.  Also the meaning has changed a bit.  Refer to the comments in the file.

[Sponsored Portlet]: https://wiki.jasig.org/display/PLT/Jasig+Sponsored+Portlets
[CalendarPortlet in Confluence]: https://wiki.jasig.org/display/PLT/Calendar+Portlet
