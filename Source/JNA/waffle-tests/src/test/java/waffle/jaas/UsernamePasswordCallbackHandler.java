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
package waffle.jaas;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;

/**
 * @author dblock[at]dblock[dot]org
 */
public class UsernamePasswordCallbackHandler implements CallbackHandler {
    private final String username;
    private final String password;

    public UsernamePasswordCallbackHandler(final String newUsername, final String newPassword) {
        this.username = newUsername;
        this.password = newPassword;
    }

    @Override
    public void handle(final Callback[] cb) throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < cb.length; i++) {
            if (cb[i] instanceof NameCallback) {
                final NameCallback nc = (NameCallback) cb[i];
                nc.setName(this.username);
            } else if (cb[i] instanceof PasswordCallback) {
                final PasswordCallback pc = (PasswordCallback) cb[i];
                pc.setPassword(this.password.toCharArray());
            } else {
                throw new UnsupportedCallbackException(cb[i], "UsernamePasswordCallbackHandler");
            }
        }
    }
}
