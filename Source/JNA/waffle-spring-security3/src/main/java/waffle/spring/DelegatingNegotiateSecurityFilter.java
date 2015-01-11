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
/**
 * 
 */
package waffle.spring;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

/**
 * 
 * 
 * <p>
 * Supports optional injection of spring security entities, allowing Waffle to act as an interface towards an identity
 * provider(the AD).
 * </p>
 * 
 * <i>Below mentioned entities are verified to be set before invoked, inherited entities are not.</i>
 * 
 * <ul>
 * <li>
 * The <code>AuthenticationManager</code> allows for the service provider to authorize the principal.</li>
 * 
 * <li>
 * The <code>authenticationSuccessHandler</code> allows for the service provider to further populate the
 * {@link org.springframework.security.core.Authentication Authentication} object.</li>
 * 
 * <li>
 * The <code>AuthenticationFailureHandler</code> is called if the AuthenticationManager throws an
 * {@link org.springframework.security.core.AuthenticationException AuthenticationException}.</li>
 * 
 * <li>
 * The <code>AccessDeniedHandler</code> is called if the AuthenticationManager throws an
 * {@link org.springframework.security.access.AccessDeniedException AccessDeniedException}.</li>
 * </ul>
 * Example configuration:
 * 
 * <pre>
 * {@code
 * <bean id="waffleNegotiateSecurityFilter"
 * 		class="waffle.spring.DelegatingNegotiateSecurityFilter"
 * 		scope="tenant">
 * 		<property name="allowGuestLogin" value="false" />
 * 		<property name="Provider" ref="waffleSecurityFilterProviderCollection" />
 * 		<property name="authenticationManager" ref="authenticationManager" />
 * 		<property name="authenticationSuccessHandler" ref="authenticationSuccessHandler" />
 * 		<property name="authenticationFailureHandler" ref="authenticationFailureHandler" />
 * 		<property name="accessDeniedHandler" ref="accessDeniedHandler" />
 * 		<property name="defaultGrantedAuthority">
 * 			<null />
 * 		</property>
 * 	</bean>
 * </code>
 * }
 * </pre>
 */
public class DelegatingNegotiateSecurityFilter extends NegotiateSecurityFilter {
    private static final Logger          LOGGER = LoggerFactory.getLogger(NegotiateSecurityFilter.class);

    private AuthenticationManager        authenticationManager;
    private AuthenticationSuccessHandler authenticationSuccessHandler;
    private AuthenticationFailureHandler authenticationFailureHandler;
    private AccessDeniedHandler          accessDeniedHandler;

    /**
     * @return the accessDeniedHandler
     */
    public AccessDeniedHandler getAccessDeniedHandler() {
        return accessDeniedHandler;
    }

    /**
     * @param accessDeniedHandler
     *            the accessDeniedHandler to set
     */
    public void setAccessDeniedHandler(final AccessDeniedHandler accessDeniedHandler) {
        this.accessDeniedHandler = accessDeniedHandler;
    }

    /**
     * @return the authenticationFailureHandler
     */
    public AuthenticationFailureHandler getAuthenticationFailureHandler() {
        return authenticationFailureHandler;
    }

    /**
     * @param authenticationFailureHandler
     *            the authenticationFailureHandler to set
     */
    public void setAuthenticationFailureHandler(final AuthenticationFailureHandler authenticationFailureHandler) {
        this.authenticationFailureHandler = authenticationFailureHandler;
    }

    public DelegatingNegotiateSecurityFilter() {
        super();
        LOGGER.debug("[waffle.spring.NegotiateSecurityFilter] loaded");
    }

    @Override
    protected boolean setAuthentication(final HttpServletRequest request, final HttpServletResponse response,
            final Authentication authentication) {
        try {
            if (authenticationManager != null) {
                logger.debug("Delegating to custom authenticationmanager");
                final Authentication customAuthentication = authenticationManager.authenticate(authentication);
                SecurityContextHolder.getContext().setAuthentication(customAuthentication);
            }
            if (authenticationSuccessHandler != null) {
                try {
                    authenticationSuccessHandler.onAuthenticationSuccess(request, response, authentication);
                } catch (final IOException e) {
                    logger.warn("Error calling authenticationSuccessHandler: " + e.getMessage());
                    return false;
                } catch (final ServletException e) {
                    logger.warn("Error calling authenticationSuccessHandler: " + e.getMessage());
                    return false;
                }
            }
        } catch (final AuthenticationException e) {

            logger.warn("Error authenticating user in custom authenticationmanager: " + e.getMessage());
            sendAuthenticationFailed(request, response, e);
            return false;
        } catch (final AccessDeniedException e) {
            logger.warn("Error authorizing user in custom authenticationmanager: " + e.getMessage());
            sendAccessDenied(request, response, e);
            return false;
        }
        return true;
    }

    @Override
    public void afterPropertiesSet() throws ServletException {
        super.afterPropertiesSet();

        if (this.getProvider() == null) {
            throw new ServletException("Missing NegotiateSecurityFilter.Provider");
        }
    }

    /**
     * Forward to authenticationFailureHandler.
     * 
     * @param response
     *            HTTP Response
     * @param close
     *            Close connection.
     */
    private void sendAuthenticationFailed(final HttpServletRequest request, final HttpServletResponse response,
            final AuthenticationException ae) {
        if (authenticationFailureHandler != null) {
            try {
                authenticationFailureHandler.onAuthenticationFailure(request, response, ae);
                return;
            } catch (final IOException e) {
                LOGGER.warn("IOException invoking authenticationFailureHandler: " + e.getMessage());
            } catch (final ServletException e) {
                LOGGER.warn("ServletException invoking authenticationFailureHandler: " + e.getMessage());
            }
        }
        super.sendUnauthorized(response, true);
    }

    /**
     * Forward to accessDeniedHandler.
     * 
     * @param response
     *            HTTP Response
     * @param close
     *            Close connection.
     */
    private void sendAccessDenied(final HttpServletRequest request, final HttpServletResponse response,
            final AccessDeniedException ae) {
        if (accessDeniedHandler != null) {
            try {
                accessDeniedHandler.handle(request, response, ae);
                return;
            } catch (final IOException e) {
                LOGGER.warn("IOException invoking accessDeniedHandler: " + e.getMessage());
            } catch (final ServletException e) {
                LOGGER.warn("ServletException invoking accessDeniedHandler: " + e.getMessage());
            }
        }
        // fallback
        sendUnauthorized(response, true);
    }

    /**
     * @return the authenticationSuccessHandler
     */
    public AuthenticationSuccessHandler getAuthenticationSuccessHandler() {
        return authenticationSuccessHandler;
    }

    /**
     * @param authenticationSuccessHandler
     *            the authenticationSuccessHandler to set
     */
    public void setAuthenticationSuccessHandler(final AuthenticationSuccessHandler authenticationSuccessHandler) {
        this.authenticationSuccessHandler = authenticationSuccessHandler;
    }

    /**
     * @return the authenticationManager
     */
    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }

    /**
     * @param authenticationManager
     *            the authenticationManager to set
     */
    public void setAuthenticationManager(final AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

}
