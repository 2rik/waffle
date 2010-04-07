package waffle.windows.auth;

public interface IWindowsIdentity {

	/**
	 * 
	 * @return
	 */
	public String getSidString();

	/**
	 * 
	 * @return
	 */
	public byte[] getSid();

	/**
	 * 
	 * @return
	 */
	public String getFqn();

	/**
	 * 
	 * @return
	 */
	public IWindowsAccount[] getGroups();
}
