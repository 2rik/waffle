/**
 * Waffle (https://github.com/dblock/waffle)
 *
 * Copyright (c) 2010 - 2014 Application Security, Inc.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Application Security, Inc.
 */
package waffle.servlet.spi;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.io.BaseEncoding;

import waffle.util.AuthorizationHeader;
import waffle.util.NtlmServletRequest;
import waffle.windows.auth.IWindowsAuthProvider;
import waffle.windows.auth.IWindowsIdentity;
import waffle.windows.auth.IWindowsSecurityContext;

/**
 * A negotiate security filter provider.
 * 
 * @author dblock[at]dblock[dot]org
 */
public class NegotiateSecurityFilterProvider implements SecurityFilterProvider {

    private static final Logger  LOGGER           = LoggerFactory.getLogger(NegotiateSecurityFilterProvider.class);

    private static final String  WWW_AUTHENTICATE = "WWW-Authenticate";

    private static final String  PROTOCOLS        = "protocols";
    private static final String  NEGOTIATE        = "Negotiate";
    private static final String  NTLM             = "NTLM";

    private List<String>         protocols        = new ArrayList<String>();
    private IWindowsAuthProvider auth;

    public NegotiateSecurityFilterProvider(final IWindowsAuthProvider auth) {
        this.auth = auth;
        this.protocols.add(NEGOTIATE);
        this.protocols.add(NTLM);
    }

    public List<String> getProtocols() {
        return this.protocols;
    }

    public void setProtocols(final List<String> values) {
        this.protocols = values;
    }

    @Override
    public void sendUnauthorized(final HttpServletResponse response) {
        final Iterator<String> protocolsIterator = this.protocols.iterator();
        while (protocolsIterator.hasNext()) {
            response.addHeader(WWW_AUTHENTICATE, protocolsIterator.next());
        }
    }

    @Override
    public boolean isPrincipalException(final HttpServletRequest request) {
        final AuthorizationHeader authorizationHeader = new AuthorizationHeader(request);
        final boolean ntlmPost = authorizationHeader.isNtlmType1PostAuthorizationHeader();
        LOGGER.debug("authorization: {}, ntlm post: {}", authorizationHeader, Boolean.valueOf(ntlmPost));
        return ntlmPost;
    }

    @Override
    public IWindowsIdentity doFilter(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException {

        final AuthorizationHeader authorizationHeader = new AuthorizationHeader(request);
        final boolean ntlmPost = authorizationHeader.isNtlmType1PostAuthorizationHeader();

        // maintain a connection-based session for NTLM tokens
        final String connectionId = NtlmServletRequest.getConnectionId(request);
        final String securityPackage = authorizationHeader.getSecurityPackage();
        LOGGER.debug("security package: {}, connection id: {}", securityPackage, connectionId);

        if (ntlmPost) {
            // type 2 NTLM authentication message received
            this.auth.resetSecurityToken(connectionId);
        }

        final byte[] tokenBuffer = authorizationHeader.getTokenBytes();
        LOGGER.debug("token buffer: {} byte(s)", Integer.valueOf(tokenBuffer.length));
        final IWindowsSecurityContext securityContext = this.auth.acceptSecurityToken(connectionId, tokenBuffer,
                securityPackage);

        final byte[] continueTokenBytes = securityContext.getToken();
        if (continueTokenBytes != null && continueTokenBytes.length > 0) {
            String continueToken = BaseEncoding.base64().encode(continueTokenBytes);
            LOGGER.debug("continue token: {}", continueToken);
            response.addHeader(WWW_AUTHENTICATE, securityPackage + " " + continueToken);
        }

        LOGGER.debug("continue required: {}", Boolean.valueOf(securityContext.isContinue()));
        if (securityContext.isContinue() || ntlmPost) {
            response.setHeader("Connection", "keep-alive");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.flushBuffer();
            return null;
        }

        final IWindowsIdentity identity = securityContext.getIdentity();
        securityContext.dispose();
        return identity;
    }

    @Override
    public boolean isSecurityPackageSupported(final String securityPackage) {
        for (String protocol : this.protocols) {
            if (protocol.equalsIgnoreCase(securityPackage)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void initParameter(final String parameterName, final String parameterValue) {
        if (parameterName.equals(PROTOCOLS)) {
            this.protocols = new ArrayList<String>();
            String[] protocolNames = parameterValue.split("\\s+");
            for (String protocolName : protocolNames) {
                protocolName = protocolName.trim();
                if (protocolName.length() > 0) {
                    LOGGER.debug("init protocol: {}", protocolName);
                    if (protocolName.equals(NEGOTIATE) || protocolName.equals(NTLM)) {
                        this.protocols.add(protocolName);
                    } else {
                        LOGGER.error("unsupported protocol: {}", protocolName);
                        throw new RuntimeException("Unsupported protocol: " + protocolName);
                    }
                }
            }
        } else {
            throw new InvalidParameterException(parameterName);
        }
    }
}
