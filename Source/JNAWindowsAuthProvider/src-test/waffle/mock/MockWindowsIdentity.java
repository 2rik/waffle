/*
 * Copyright (c) Application Security Inc., 2010
 * All Rights Reserved
 * Eclipse Public License (EPLv1)
 * http://waffle.codeplex.com/license
 */
package waffle.mock;

import java.util.ArrayList;
import java.util.List;

import waffle.windows.auth.IWindowsAccount;
import waffle.windows.auth.IWindowsIdentity;
import waffle.windows.auth.IWindowsImpersonationContext;

/**
 * @author dblock[at]dblock[dot]org
 */
public class MockWindowsIdentity implements IWindowsIdentity {

	private String _fqn;
	private List<String> _groups;
	private boolean _guest = false;
	
	public MockWindowsIdentity(String fqn, List<String> groups) {
		_fqn = fqn;
		_groups = groups;
	}
	
	@Override
	public String getFqn() {
		return _fqn;
	}

	@Override
	public IWindowsAccount[] getGroups() {
		List<MockWindowsAccount> groups = new ArrayList<MockWindowsAccount>();
		for(String group : _groups) {
			groups.add(new MockWindowsAccount(group));
		}
		return groups.toArray(new IWindowsAccount[0]);
	}

	@Override
	public byte[] getSid() {
		return null;
	}

	@Override
	public String getSidString() {
		return "S-" + _fqn.hashCode();
	}
	
	@Override
	public void dispose() {
		
	}
	
	@Override
	public boolean isGuest() {
		return _guest;
	}

	@Override
	public IWindowsImpersonationContext impersonate() {
		return new MockWindowsImpersonationContext();
	}
}
