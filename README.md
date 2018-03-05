# CalendarPortlet

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jasig.portlet/CalendarPortlet/badge.svg)](https://maven-badges.herokuapp.com/maven-central/org.jasig.portlet/CalendarPortlet)
[![Linux Build Status](https://travis-ci.org/Jasig/CalendarPortlet.svg?branch=master)](https://travis-ci.org/Jasig/CalendarPortlet)
[![Windows Build status](https://ci.appveyor.com/api/projects/status/d8e32yt07o12mg23/branch/master?svg=true)](https://ci.appveyor.com/project/ChristianMurphy/calendarportlet/branch/master)

This is a [Sponsored Portlet][] in the Apereo uPortal project.

## Configuration

See also [documentation in the external wiki][calendarportlet in confluence]. Earlier documentation may be found there, and is still relevant. We are in the process of moving documentation here.

### Using Encrypted Property Values

You may optionally provide sensitive configuration items -- such as database passwords -- in encrypted format. Use the [Jasypt CLI Tools](http://www.jasypt.org/cli.html) to encrypt the sensitive value, then include it in a `.properties` file like this:

```
hibernate.connection.password=ENC(9ffpQXJi/EPih9o+Xshm5g==)
```

Specify the encryption key using the `UP_JASYPT_KEY` environment variable.

## Migrating to v2.3.0

If using Exchange Web Services, the `configuration.properties` file has minor in order to support EWS Impersonation. Several `*Context.xml` files are also updated in case you have overridden them in the uPortal overlays.

`configuration.properties` changes:

* `ntlm.domain` is renamed `exchangeWs.ntlm.domain`. Also the meaning has changed a bit. Refer to the comments in the file.

[sponsored portlet]: https://wiki.jasig.org/display/PLT/Jasig+Sponsored+Portlets
[calendarportlet in confluence]: https://wiki.jasig.org/display/PLT/Calendar+Portlet

## Dependencies

These dependencies are expected to be loaded by overall uPortal:

* [Font Awesome][] 4, last tested with [Font Awesome 4.7.0][]
* [Bootstrap][] 3, last tested with [Bootstrap 3.3.7][]

[font awesome]: http://fontawesome.io/
[font awesome 4.7.0]: https://github.com/FortAwesome/Font-Awesome/releases/tag/v4.7.0
[bootstrap]: https://getbootstrap.com
[bootstrap 3.3.7]: https://getbootstrap.com/docs/3.3/
