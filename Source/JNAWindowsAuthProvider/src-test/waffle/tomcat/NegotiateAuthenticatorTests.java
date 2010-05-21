/*
 * Copyright (c) Application Security Inc., 2010
 * All Rights Reserved
 * Eclipse Public License (EPLv1)
 * http://waffle.codeplex.com/license
 */
package waffle.tomcat;

import org.apache.catalina.Realm;

import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Sspi;
import com.sun.jna.platform.win32.Sspi.SecBufferDesc;

import waffle.tomcat.catalina.SimpleContext;
import waffle.tomcat.catalina.SimpleHttpRequest;
import waffle.tomcat.catalina.SimpleHttpResponse;
import waffle.tomcat.catalina.SimpleRealm;
import waffle.windows.auth.IWindowsCredentialsHandle;
import waffle.windows.auth.impl.WindowsCredentialsHandleImpl;
import waffle.windows.auth.impl.WindowsSecurityContextImpl;

import junit.framework.TestCase;

/**
 * Walle Tomcat Authenticator Tests
 * @author dblock[at]dblock[dot]org
 */
public class NegotiateAuthenticatorTests extends TestCase {

	NegotiateAuthenticator authenticator = null;
	
	@Override
	public void setUp() {
		authenticator = new NegotiateAuthenticator();
		SimpleContext ctx = new SimpleContext();
		Realm realm = new SimpleRealm();
		ctx.setRealm(realm);
		authenticator.setContainer(ctx);
		authenticator.start();
	}

	@Override
	public void tearDown() {
		authenticator.stop();
		authenticator = null;
	}
	
	public void testGetInfo() {
		assertTrue(authenticator.getInfo().length() > 0);		
	}

	public void testChallenge() {
		SimpleHttpRequest request = new SimpleHttpRequest();
		SimpleHttpResponse response = new SimpleHttpResponse();
		authenticator.authenticate(request, response, null);
		String[] wwwAuthenticates = response.getHeaderValues("WWW-Authenticate");
		assertEquals(2, wwwAuthenticates.length);
		assertEquals("Negotiate", wwwAuthenticates[0]);
		assertEquals("NTLM", wwwAuthenticates[1]);
		assertEquals("close", response.getHeader("Connection"));
		assertEquals(2, response.getHeaderNames().length);
		assertEquals(401, response.getStatus());
	}
	
	public void testNegotiate() {
		String securityPackage = "Negotiate";
		// client credentials handle
		IWindowsCredentialsHandle clientCredentials = WindowsCredentialsHandleImpl.getCurrent(securityPackage);
		clientCredentials.initialize();
		// initial client security context
		WindowsSecurityContextImpl clientContext = new WindowsSecurityContextImpl();
		clientContext.setPrincipalName(Advapi32Util.getUserName());
		clientContext.setCredentialsHandle(clientCredentials.getHandle());
		clientContext.setSecurityPackage(securityPackage);
		clientContext.initialize();
		// negotiate
		boolean authenticated = false;
        while(true)
        {
    		SimpleHttpRequest request = new SimpleHttpRequest();
    		String clientToken = Base64.encode(clientContext.getToken());
    		request.addHeader("Authorization", "NTLM " + clientToken);
    		
    		SimpleHttpResponse response = new SimpleHttpResponse();
    		authenticated = authenticator.authenticate(request, response, null);

    		if (authenticated) {
        		assertEquals(0, response.getHeaderNames().length);
    			break;
    		}
    		
    		assertTrue(response.getHeader("WWW-Authenticate").startsWith("NTLM "));
    		assertEquals("keep-alive", response.getHeader("Connection"));
    		assertEquals(2, response.getHeaderNames().length);
    		assertEquals(401, response.getStatus());
    		String continueToken = response.getHeader("WWW-Authenticate").substring(5);
    		byte[] continueTokenBytes = Base64.decode(continueToken);
    		assertTrue(continueTokenBytes.length > 0);
            SecBufferDesc continueTokenBuffer = new SecBufferDesc(Sspi.SECBUFFER_TOKEN, continueTokenBytes);
            clientContext.initialize(clientContext.getHandle(), continueTokenBuffer);
        }        
        assertTrue(authenticated);
	}
}
