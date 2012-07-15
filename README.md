WAFFLE - Windows Authentication Framework
=========================================

![waffle](https://github.com/dblock/waffle/raw/1.5/waffle.jpg)

WAFFLE is a native Windows Authentication Framework consisting of two C# and Java libraries that perform functions related to Windows authentication, supporting Negotiate, NTLM and Kerberos. Waffle also includes libraries that enable drop-in Windows Single Sign On for popular Java web servers, when running on Windows. While Waffle makes it ridiculously easy to do Windows Authentication in Java, on Windows, Waffle does not work on *nix.

Unlike many other implementations Waffle on Windows does not require any server-side Kerberos keytab setup, it's a drop-in solution. You can see it in action in [this slightly blurry video](http://www.youtube.com/watch?v=LmTwbOh0hBU) produced for [TeamShatter.com](http://www.teamshatter.com/topics/general/team-shatter-exclusive/securing-java-applications-with-smart-cards-and-single-sign-on/). 

Essentials
----------

* [Download Version 1.5](https://github.com/downloads/dblock/waffle/Waffle.1.5.zip)
* [Need Help? Google Group](http://groups.google.com/group/waffle-users)
* [Frequently Asked Questions](https://github.com/dblock/waffle/wiki/Frequently-Asked-Questions)
* [Troubleshooting Negotiate](https://github.com/dblock/waffle/wiki/Troubleshooting-Negotiate)
* [Older Versions on CodePlex](http://waffle.codeplex.com/).

Documentation
-------------

There're several semi-independent parts to Waffle. Choose the appropriate documentation.

* Simple native interfaces in C# and Java to do all things Windows authentication. Useful if you're building a custom client that requires Windows authentication. See [Getting Started with WAFFLE API](https://github.com/dblock/waffle/blob/1.5/Docs/GettingStartedWithWaffleAPI.md)
* A generic Servlet Negotiate (NTLM and Kerberos) Security Filter that can be used with many web servers, including Tomcat, Jetty and WebSphere. See [documentation](https://github.com/dblock/waffle/blob/1.5/Docs/ServletSingleSignOnSecurityFilter.md).
* A Tomcat Negotiate (NTLM and Kerberos) Authenticator Valve, built for the Tomcat Web Container. See [documentation](https://github.com/dblock/waffle/blob/1.5/Docs/TomcatMixedSingleSignOnAndFormAuthenticatorValve.md).
* A Tomcat Single Sign-On + Form Authentication Mixed Valve, built for the Tomcat Web Container and allowing users to choose whether to do form authentication (a username and password sent to the server from a form) or Windows SSO (NTLM or Kerberos). See [documentation](https://github.com/dblock/waffle/blob/1.5/Docs/TomcatMixedSingleSignOnAndFormAuthenticatorValve.md).
* A Spring-Security Negotiate (NTLM and Kerberos) Filter. See [documentation](https://github.com/dblock/waffle/blob/1.5/Docs/SpringSecuritySingleSignOnFilter.md).
* A Spring-Security Windows Authentication Manager. See [documentation](https://github.com/dblock/waffle/blob/1.5/Docs/SpringSecurityAuthenticationProvider.md).
* A JAAS Login Module, useful when extending a custom Java client that already implements JAAS to support Windows SSO. See  [documentation](https://github.com/dblock/waffle/blob/1.5/Docs/TomcatWindowsLoginJAASAuthenticator.md).

Waffle was created and is sponsored by [Application Security Inc.](http://www.appsecinc.com/). For a long story, read the [Project History](https://github.com/dblock/waffle/blob/1.5/HISTORY.md). Also, feel free to use [this PowerPoint presentation](http://www.slideshare.net/dblockdotorg/waffle-at-nycjavasig) from NYJavaSIG to talk about Waffle.

Features
--------

* Account lookup locally and in Active Directory via Win32 API with zero configuration.
* Enumerating Active Directory domains and domain information.
* Returns computer domain / workgroup join information.
* Supports logon for local and domain users returning consistent fully qualified names, identity (SIDs), local and domain groups, including nested.
* Supports all functions required for implementing server-side single-signon with Negotiate and NTLM and various implementations for Java web servers.
* Supports Windows Identity impersonation.
* Includes a Windows Installer Merge Module for distribution of C# binaries.

Related and Similar Products
----------------------------

* [Quest Vintella Single-Sign-On](http://www.quest.com/single-sign-on-for-java/) (Commercial)
* [IOPlex Jespa](http://www.ioplex.com/) (Commercial)
* [Josso](http://www.josso.org/confluence/display/JOSSO1/JOSSO+-+Java+Open+Single+Sign-On+Project+Home)
* [Tomcat SPNEGO](http://tomcatspnego.codeplex.com/)

License and Copyright
---------------------

Copyright (c) [Application Security Inc.](http://www.appsecinc.com), 2010-2012 and Contributors. 

This project is licensed under the [Eclipse Public License](https://github.com/dblock/waffle/blob/1.5/LICENSE).

Project maintained by [Daniel Doubrovkine](https://github.com/dblock).
