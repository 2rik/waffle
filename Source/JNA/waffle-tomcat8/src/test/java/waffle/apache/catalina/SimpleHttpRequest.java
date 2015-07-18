/**
 * Waffle (https://github.com/dblock/waffle)
 *
 * Copyright (c) 2010 - 2015 Application Security, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Application Security, Inc.
 */
package waffle.apache.catalina;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.apache.catalina.connector.Request;
import org.mockito.Mockito;

/**
 * Simple HTTP Request.
 * 
 * @author dblock[at]dblock[dot]org
 */
public class SimpleHttpRequest extends Request {

    private static int remotePortS;

    public synchronized static int nextRemotePort() {
        return ++SimpleHttpRequest.remotePortS;
    }

    public synchronized static void resetRemotePort() {
        SimpleHttpRequest.remotePortS = 0;
    }

    private String                    requestURI;
    private String                    queryString;
    private String                    remoteUser;
    private String                    method     = "GET";
    private final Map<String, String> headers    = new HashMap<>();
    private final Map<String, String> parameters = new HashMap<>();
    private byte[]                    content;

    private SimpleHttpSession         httpSession;

    private Principal                 principal;

    public SimpleHttpRequest() {
        super();
        this.httpSession = Mockito.mock(SimpleHttpSession.class, Mockito.CALLS_REAL_METHODS);
        this.httpSession.setAttributes(new HashMap<String, Object>());
        this.remotePort = SimpleHttpRequest.nextRemotePort();
    }

    public void addHeader(final String headerName, final String headerValue) {
        this.headers.put(headerName, headerValue);
    }

    public void addParameter(final String parameterName, final String parameterValue) {
        this.parameters.put(parameterName, parameterValue);
    }

    @Override
    public int getContentLength() {
        return this.content == null ? -1 : this.content.length;
    }

    @Override
    public String getHeader(final String headerName) {
        return this.headers.get(headerName);
    }

    @Override
    public String getMethod() {
        return this.method;
    }

    @Override
    public String getParameter(final String parameterName) {
        return this.parameters.get(parameterName);
    }

    @Override
    public String getQueryString() {
        return this.queryString;
    }

    @Override
    public String getRemoteAddr() {
        return this.remoteAddr;
    }

    @Override
    public String getRemoteHost() {
        return this.remoteHost;
    }

    @Override
    public int getRemotePort() {
        return this.remotePort;
    }

    @Override
    public String getRemoteUser() {
        return this.remoteUser;
    }

    @Override
    public String getRequestURI() {
        return this.requestURI;
    }

    @Override
    public HttpSession getSession() {
        return this.httpSession;
    }

    @Override
    public HttpSession getSession(final boolean create) {
        if (this.httpSession == null && create) {
            this.httpSession = Mockito.mock(SimpleHttpSession.class, Mockito.CALLS_REAL_METHODS);
            this.httpSession.setAttributes(new HashMap<String, Object>());
        }
        return this.httpSession;
    }

    @Override
    public Principal getUserPrincipal() {
        return this.principal;
    }

    public void setContentLength(final int length) {
        this.content = new byte[length];
    }

    public void setMethod(final String value) {
        this.method = value;
    }

    public void setQueryString(final String queryValue) {
        this.queryString = queryValue;
        if (this.queryString != null) {
            for (final String eachParameter : this.queryString.split("[&]")) {
                final String[] pair = eachParameter.split("=");
                final String value = pair.length == 2 ? pair[1] : "";
                this.addParameter(pair[0], value);
            }
        }
    }

    @Override
    public void setRemoteAddr(final String value) {
        this.remoteAddr = value;
    }

    @Override
    public void setRemoteHost(final String value) {
        this.remoteHost = value;
    }

    public void setRemoteUser(final String value) {
        this.remoteUser = value;
    }

    public void setRequestURI(final String value) {
        this.requestURI = value;
    }

    @Override
    public void setUserPrincipal(final Principal value) {
        this.principal = value;
    }
}
