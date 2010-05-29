/*
 * Copyright (c) Application Security Inc., 2010
 * All Rights Reserved
 * Eclipse Public License (EPLv1)
 * http://waffle.codeplex.com/license
 */
package waffle.tomcat;

import java.security.Principal;

import javax.servlet.http.HttpServletResponse;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.deploy.LoginConfig;
import org.apache.commons.logging.LogFactory;

import waffle.windows.auth.IWindowsIdentity;
import waffle.windows.auth.IWindowsSecurityContext;

/**
 * A Tomcat Negotiate (NTLM, Kerberos) Authenticator.
 * @author dblock[at]dblock[dot]org
 */
public class NegotiateAuthenticator extends WaffleAuthenticatorBase {

    public NegotiateAuthenticator() {
    	super();
    	_log = LogFactory.getLog(NegotiateAuthenticator.class);
    	_info = "waffle.tomcat.NegotiateAuthenticator/1.0";
    	_log.debug("[waffle.tomcat.NegotiateAuthenticator] loaded");
    }
	
	@Override
	public void start() {
		_log.info("[waffle.tomcat.NegotiateAuthenticator] started");		
	}
	
	@Override
	public void stop() {
		_log.info("[waffle.tomcat.NegotiateAuthenticator] stopped");		
	}

	@Override
	protected boolean authenticate(Request request, Response response, LoginConfig loginConfig) {
		
		Principal principal = request.getUserPrincipal();
		AuthorizationHeader authorizationHeader = new AuthorizationHeader(request);
		boolean ntlmPost = authorizationHeader.isNtlmType1PostAuthorizationHeader();

		_log.debug(request.getMethod() + " " + request.getRequestURI() + ", contentlength: " + request.getContentLength());
		_log.debug("authorization: " + authorizationHeader.toString() + ", ntlm post: " + ntlmPost);
		
		if (principal != null && ! ntlmPost) {
			// user already authenticated
			_log.debug("previously authenticated user: " + principal.getName());
			return true;
		}
			
		// authenticate user
		if (! authorizationHeader.isNull()) {
			
			String securityPackage = authorizationHeader.getSecurityPackage();			
			// maintain a connection-based session for NTLM tokens
			String connectionId = Integer.toString(request.getRemotePort());
			
			_log.debug("security package: " + securityPackage + ", connection id: " + connectionId);
			
			if (ntlmPost) {
				// type 1 NTLM authentication message received
				_auth.resetSecurityToken(connectionId);
			}
			
			// log the user in using the token
			IWindowsSecurityContext securityContext = null;
			
			try {
				byte[] tokenBuffer = authorizationHeader.getTokenBytes();
				_log.debug("token buffer: " + tokenBuffer.length + " byte(s)");
				securityContext = _auth.acceptSecurityToken(connectionId, tokenBuffer, securityPackage);
				_log.debug("continue required: " + securityContext.getContinue());

				byte[] continueTokenBytes = securityContext.getToken();
				if (continueTokenBytes != null) {
					String continueToken = new String(Base64.encode(continueTokenBytes));
					_log.debug("continue token: " + continueToken);
					response.addHeader("WWW-Authenticate", securityPackage + " " + continueToken);
				}
				
    			if (securityContext.getContinue() || ntlmPost) {
    				response.setHeader("Connection", "keep-alive");
    				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    				response.flushBuffer();
    				return false;
    			}
    			
			} catch (Exception e) {
				_log.warn("error logging in user: " + e.getMessage());
				sendUnauthorized(response);
				return false;
			}
			
			// realm: fail if no realm is configured
			if(context == null || context.getRealm() == null) {
				_log.warn("missing context/realm");
				sendError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE);
				return false;
			}

			// create and register the user principal with the session
			IWindowsIdentity windowsIdentity = securityContext.getIdentity();
			
			try {
				_log.debug("logged in user: " + windowsIdentity.getFqn() + 
						" (" + windowsIdentity.getSidString() + ")");
				
				WindowsPrincipal windowsPrincipal = new WindowsPrincipal(
						windowsIdentity, context.getRealm(), _principalFormat, _roleFormat);
				
				_log.debug("roles: " + windowsPrincipal.getRolesString());
				
				principal = windowsPrincipal;
				register(request, response, principal, securityPackage, principal.getName(), null);
				_log.info("successfully logged in user: " + principal.getName());
				
			} finally {
				windowsIdentity.dispose();
			}
			
			return true;
		}
		
		_log.debug("authorization required");
		sendUnauthorized(response);
		return false;
	}	
}
