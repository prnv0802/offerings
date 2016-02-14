import javax.swing.*;
import java.awt.*;


public class mydialog extends JDialog {
	public static enum DialogResult {OK, Cancel};

	public mydialog() throws HeadlessException {
		initialize();
	}

	public mydialog(Frame owner) throws HeadlessException {
		super(owner);
		initialize();
	}

	public mydialog(Dialog owner) throws HeadlessException {
		super(owner);
		initialize();
	}

	public mydialog(Frame owner, boolean modal) throws HeadlessException {
		super(owner, modal);
		initialize();
	}

	public mydialog(Frame owner, String title) throws HeadlessException {
		super(owner, title);
		initialize();
	}

	public mydialog(Dialog owner, boolean modal) throws HeadlessException {
		super(owner, modal);
		initialize();
	}

	public mydialog(Dialog owner, String title) throws HeadlessException {
		super(owner, title);
		initialize();
	}

	public mydialog(Frame owner, String title, boolean modal)
			throws HeadlessException {
		super(owner, title, modal);
		initialize();
	}

	public mydialog(Dialog owner, String title, boolean modal)
			throws HeadlessException {
		super(owner, title, modal);
		initialize();
	}

	public mydialog(Frame owner, String title, boolean modal,
			GraphicsConfiguration gc) {
		super(owner, title, modal, gc);
		initialize();
	}

	public mydialog(Dialog owner, String title, boolean modal,
			GraphicsConfiguration gc) throws HeadlessException {
		super(owner, title, modal, gc);
		initialize();
	}

	public DialogResult Result;
	public JPanel jContentPaneTunnelConfiguration = null;
	private JLabel jLabelTunnelName = null;
	private JLabel jLabelLocalPort = null;
	private JLabel jLabelSSHHost = null;
	private JLabel jLabelSSHPort = null;
	private JLabel jLabelRemoteHost = null;
	private JLabel jLabelRemotePort = null;
	public JTextField jTextFieldTunnelName = null;
	public JTextField jTextFieldLocalPort = null;
	public JTextField jTextFieldSSHHost = null;
	public JTextField jTextFieldSSHPort = null;
	public JTextField jTextFieldRemoteHost = null;
	public JTextField jTextFieldRemotePort = null;
	public JCheckBox jCheckBoxAutoRestart = null;
	public JButton jButtonOK = null;
	public JButton jButtonCancel = null;
	private JLabel jLabelSSHUser = null;
	private JLabel jLabelSSHPassword = null;
	public JTextField jTextFieldSSHUser = null;
	public JPasswordField jPasswordFieldSSHPassword = null;
	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
			this.setMinimumSize(new Dimension(298, 357));
			this.setMaximumSize(new Dimension(298, 357));
			this.setBounds(new Rectangle(0, 0, 298, 357));
			this.setPreferredSize(new Dimension(298, 357));
			this.setResizable(false);
			this.setModal(true);
			this.setTitle("Tunnel Configuration");
			this.setContentPane(getJContentPaneTunnelConfiguration());
	}

	/**
	 * This method initializes jContentPaneTunnelConfiguration
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPaneTunnelConfiguration() {
		if (jContentPaneTunnelConfiguration == null) {
			jLabelSSHPassword = new JLabel();
			jLabelSSHPassword.setBounds(new Rectangle(8, 158, 92, 16));
			jLabelSSHPassword.setText("SSH Password:");
			jLabelSSHUser = new JLabel();
			jLabelSSHUser.setBounds(new Rectangle(8, 134, 61, 16));
			jLabelSSHUser.setText("SSH User:");
			jLabelRemotePort = new JLabel();
			jLabelRemotePort.setBounds(new Rectangle(8, 229, 78, 12));
			jLabelRemotePort.setText("Remote Port:");
			jLabelRemoteHost = new JLabel();
			jLabelRemoteHost.setBounds(new Rectangle(8, 205, 78, 12));
			jLabelRemoteHost.setText("Remote Host:");
			jLabelSSHPort = new JLabel();
			jLabelSSHPort.setBounds(new Rectangle(8, 110, 57, 12));
			jLabelSSHPort.setText("SSH Port:");
			jLabelSSHHost = new JLabel();
			jLabelSSHHost.setBounds(new Rectangle(8, 86, 57, 12));
			jLabelSSHHost.setText("SSH Host:");
			jLabelLocalPort = new JLabel();
			jLabelLocalPort.setBounds(new Rectangle(8, 42, 202, 12));
			jLabelLocalPort.setText("Local Port (incoming connections):");
			jLabelTunnelName = new JLabel();
			jLabelTunnelName.setBounds(new Rectangle(8, 18, 78, 12));
			jLabelTunnelName.setText("Tunnel Name:");
			jContentPaneTunnelConfiguration = new JPanel();
			jContentPaneTunnelConfiguration.setLayout(null);
			jContentPaneTunnelConfiguration.add(jLabelTunnelName, null);
			jContentPaneTunnelConfiguration.add(jLabelLocalPort, null);
			jContentPaneTunnelConfiguration.add(jLabelSSHHost, null);
			jContentPaneTunnelConfiguration.add(jLabelSSHPort, null);
			jContentPaneTunnelConfiguration.add(jLabelRemoteHost, null);
			jContentPaneTunnelConfiguration.add(jLabelRemotePort, null);
			jContentPaneTunnelConfiguration.add(getJTextFieldTunnelName(), null);
			jContentPaneTunnelConfiguration.add(getJTextFieldLocalPort(), null);
			jContentPaneTunnelConfiguration.add(getJTextFieldSSHHost(), null);
			jContentPaneTunnelConfiguration.add(getJTextFieldSSHPort(), null);
			jContentPaneTunnelConfiguration.add(getJTextFieldRemoteHost(), null);
			jContentPaneTunnelConfiguration.add(getJTextFieldRemotePort(), null);
			jContentPaneTunnelConfiguration.add(getJCheckBoxAutoRestart(), null);
			jContentPaneTunnelConfiguration.add(getJButtonOK(), null);
			jContentPaneTunnelConfiguration.add(getJButtonCancel(), null);
			jContentPaneTunnelConfiguration.add(jLabelSSHUser, null);
			jContentPaneTunnelConfiguration.add(jLabelSSHPassword, null);
			jContentPaneTunnelConfiguration.add(getJTextFieldSSHUser(), null);
			jContentPaneTunnelConfiguration.add(getJPasswordFieldSSHPassword(), null);
		}
		return jContentPaneTunnelConfiguration;
	}

	/**
	 * This method initializes jTextFieldTunnelName
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldTunnelName() {
		if (jTextFieldTunnelName == null) {
			jTextFieldTunnelName = new JTextField();
			jTextFieldTunnelName.setBounds(new Rectangle(115, 15, 167, 19));
		}
		return jTextFieldTunnelName;
	}

	/**
	 * This method initializes jTextFieldLocalPort
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldLocalPort() {
		if (jTextFieldLocalPort == null) {
			jTextFieldLocalPort = new JTextField();
			jTextFieldLocalPort.setBounds(new Rectangle(215, 39, 67, 19));
		}
		return jTextFieldLocalPort;
	}

	/**
	 * This method initializes jTextFieldSSHHost
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldSSHHost() {
		if (jTextFieldSSHHost == null) {
			jTextFieldSSHHost = new JTextField();
			jTextFieldSSHHost.setBounds(new Rectangle(115, 83, 167, 19));
		}
		return jTextFieldSSHHost;
	}

	/**
	 * This method initializes jTextFieldSSHPort
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldSSHPort() {
		if (jTextFieldSSHPort == null) {
			jTextFieldSSHPort = new JTextField();
			jTextFieldSSHPort.setBounds(new Rectangle(215, 107, 67, 19));
		}
		return jTextFieldSSHPort;
	}

	/**
	 * This method initializes jTextFieldRemoteHost
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldRemoteHost() {
		if (jTextFieldRemoteHost == null) {
			jTextFieldRemoteHost = new JTextField();
			jTextFieldRemoteHost.setBounds(new Rectangle(115, 202, 167, 19));
		}
		return jTextFieldRemoteHost;
	}

	/**
	 * This method initializes jTextFieldRemotePort
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldRemotePort() {
		if (jTextFieldRemotePort == null) {
			jTextFieldRemotePort = new JTextField();
			jTextFieldRemotePort.setBounds(new Rectangle(215, 226, 67, 19));
		}
		return jTextFieldRemotePort;
	}

	/**
	 * This method initializes jCheckBoxAutoRestart
	 *
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxAutoRestart() {
		if (jCheckBoxAutoRestart == null) {
			jCheckBoxAutoRestart = new JCheckBox();
			jCheckBoxAutoRestart.setBounds(new Rectangle(5, 269, 278, 16));
			jCheckBoxAutoRestart.setText("Auto restart all tunnels after adding this one.");
		}
		return jCheckBoxAutoRestart;
	}

	/**
	 * This method initializes jButtonOK
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonOK() {
		if (jButtonOK == null) {
			jButtonOK = new JButton();
			jButtonOK.setBounds(new Rectangle(123, 300, 75, 21));
			jButtonOK.setText("OK");
			jButtonOK.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Result = DialogResult.OK;
					setVisible(false);
				}
			});
		}
		return jButtonOK;
	}

	/**
	 * This method initializes jButtonCancel
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonCancel() {
		if (jButtonCancel == null) {
			jButtonCancel = new JButton();
			jButtonCancel.setBounds(new Rectangle(207, 300, 75, 21));
			jButtonCancel.setText("Cancel");
			jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					Result = DialogResult.Cancel;
					setVisible(false);
				}
			});
		}
		return jButtonCancel;
	}

	/**
	 * This method initializes jTextFieldSSHUser
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldSSHUser() {
		if (jTextFieldSSHUser == null) {
			jTextFieldSSHUser = new JTextField();
			jTextFieldSSHUser.setBounds(new Rectangle(115, 131, 167, 19));
		}
		return jTextFieldSSHUser;
	}

	/**
	 * This method initializes jPasswordFieldSSHPassword
	 *
	 * @return javax.swing.JPasswordField
	 */
	private JPasswordField getJPasswordFieldSSHPassword() {
		if (jPasswordFieldSSHPassword == null) {
			jPasswordFieldSSHPassword = new JPasswordField();
			jPasswordFieldSSHPassword.setBounds(new Rectangle(115, 155, 167, 19));
		}
		return jPasswordFieldSSHPassword;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
