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
package waffle.windows.auth;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import waffle.windows.auth.impl.WindowsAccountImpl;

/**
 * @author dblock[at]dblock[dot]org
 */
public class WindowsAccountTests {

    private Logger logger = LoggerFactory.getLogger(WindowsAccountTests.class);

    @Test
    public void testGetCurrentUsername() {
        String currentUsername = WindowsAccountImpl.getCurrentUsername();
        this.logger.info("Current username: {}", currentUsername);
        assertTrue(currentUsername.length() > 0);
    }

    @Test
    public void testGetCurrentAccount() {
        String currentUsername = WindowsAccountImpl.getCurrentUsername();
        IWindowsAccount account = new WindowsAccountImpl(currentUsername);
        assertTrue(account.getName().length() > 0);
        this.logger.info("Name: {}", account.getName());
        assertTrue(account.getDomain().length() > 0);
        this.logger.info("Domain: {}", account.getDomain());
        assertTrue(account.getFqn().length() > 0);
        this.logger.info("Fqn: {}", account.getFqn());
        assertTrue(account.getSidString().length() > 0);
        this.logger.info("Sid: {}", account.getSidString());
        // To avoid errors with machine naming being all upper-case, use test in this manner
        assertTrue(currentUsername.equalsIgnoreCase(account.getFqn()));
        assertTrue(currentUsername.endsWith("\\" + account.getName()));
        // To avoid errors with machine naming being all upper-case, use test in this manner
        assertTrue(currentUsername.toLowerCase().startsWith(account.getDomain().toLowerCase() + "\\"));
    }
}
