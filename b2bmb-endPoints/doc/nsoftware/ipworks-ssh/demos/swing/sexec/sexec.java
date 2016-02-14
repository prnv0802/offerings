import ipworksssh.IPWorksSSHException;
import ipworksssh.Sexec;

import javax.swing.*;
import java.awt.*;
import java.util.TooManyListenersException;

public class sexec {

	private JFrame jFrame = null;

	private JPanel jContentPane = null;

	private JLabel jLabelDescription = null;

	private JLabel jLabelDescription2 = null;

	private JLabel jLabelServer = null;

	private JLabel jLabelUser = null;

	private JLabel jLabel = null;

	private JLabel jLabelSSHCommand = null;

	private JLabel jLabelResponse = null;

	private JTextField jTextFieldServer = null;

	private JTextField jTextFieldUser = null;

	private JPasswordField jPasswordFieldPassword = null;

	private JTextField jTextFieldCommand = null;

	private JButton jButtonExecute = null;

	private JScrollPane jScrollPaneResponse = null;

	private JTextArea jTextAreaResponse = null;

	private Sexec sexec1 = null;  //  @jve:decl-index=0:visual-constraint="540,8"

	private JDialog jDialogMessageBox = null;  //  @jve:decl-index=0:visual-constraint="12,420"

	private JPanel jContentPaneMessageBox = null;

	private JTextPane jTextPaneNotifyMessage = null;

	private JButton jButtonNotifyOK = null;

	/**
	 * This method displays a simple dialog with given message and title
	 *
	 * @param msg - a message string to be shown
	 * @param title - dialog title string
	 */
	private void notifyMessage(String msg, String title) {
                JOptionPane.showMessageDialog(jFrame,msg,title,JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * This method initializes jTextFieldServer
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldServer() {
		if (jTextFieldServer == null) {
			jTextFieldServer = new JTextField();
			jTextFieldServer.setBounds(new Rectangle(77, 37, 196, 19));
			jTextFieldServer.setText("<hostname>[:<portnumber>]");
		}
		return jTextFieldServer;
	}

	/**
	 * This method initializes jTextFieldUser
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldUser() {
		if (jTextFieldUser == null) {
			jTextFieldUser = new JTextField();
			jTextFieldUser.setBounds(new Rectangle(350, 37, 146, 19));
		}
		return jTextFieldUser;
	}

	/**
	 * This method initializes jPasswordFieldPassword
	 *
	 * @return javax.swing.JPasswordField
	 */
	private JPasswordField getJPasswordFieldPassword() {
		if (jPasswordFieldPassword == null) {
			jPasswordFieldPassword = new JPasswordField();
			jPasswordFieldPassword.setBounds(new Rectangle(350, 59, 146, 19));
		}
		return jPasswordFieldPassword;
	}

	/**
	 * This method initializes jTextFieldCommand
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldCommand() {
		if (jTextFieldCommand == null) {
			jTextFieldCommand = new JTextField();
			jTextFieldCommand.setBounds(new Rectangle(8, 103, 406, 19));
			jTextFieldCommand.setText("ls -la");
		}
		return jTextFieldCommand;
	}

	/**
	 * This method initializes jButtonExecute
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonExecute() {
		if (jButtonExecute == null) {
			jButtonExecute = new JButton();
			jButtonExecute.setBounds(new Rectangle(416, 103, 80, 23));
			jButtonExecute.setText("Execute");
			jButtonExecute.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getJTextAreaResponse().setText(null);
					if (!getSexec().isConnected()) {
						String[] hostinfo = getJTextFieldServer().getText().split(":");
						String hostname = "";
						int portnumber = 22;
						switch (hostinfo.length) {
						case 1:
							hostname = hostinfo[0];
							portnumber = 22;
							break;
						case 2:
							hostname = hostinfo[0];
							portnumber = Integer.valueOf(hostinfo[1]).intValue();
							break;
						default:
							;
						}

						try {
							sexec1.setTimeout(60);
							sexec1.setSSHUser(getJTextFieldUser().getText());
							sexec1.setSSHPassword(new String(getJPasswordFieldPassword().getPassword()));
							sexec1.SSHLogon(hostname, portnumber);
						} catch (IPWorksSSHException ipwe) {
							ipwe.printStackTrace();
							notifyMessage("Code: "+ipwe.getCode()+"; Message: \""+ipwe.getMessage()+"\"","IPWorksSSHException");
						}
					}
					try {
                                                sexec1.execute(getJTextFieldCommand().getText());
					} catch (IPWorksSSHException ipwe) {
						ipwe.printStackTrace();
						notifyMessage("Code: "+ipwe.getCode()+"; Message: \""+ipwe.getMessage()+"\"","IPWorksSSHException");
					}

				}
			});
		}
		return jButtonExecute;
	}

	/**
	 * This method initializes jScrollPaneResponse
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPaneResponse() {
		if (jScrollPaneResponse == null) {
			jScrollPaneResponse = new JScrollPane();
			jScrollPaneResponse.setBounds(new Rectangle(8, 155, 488, 198));
			jScrollPaneResponse.setViewportView(getJTextAreaResponse());
		}
		return jScrollPaneResponse;
	}

	/**
	 * This method initializes jTextAreaResponse
	 *
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getJTextAreaResponse() {
		if (jTextAreaResponse == null) {
			jTextAreaResponse = new JTextArea();
		}
		return jTextAreaResponse;
	}

	/**
	 * This method initializes sexec
	 *
	 * @return ipworksssh.Sexec
	 */
	private Sexec getSexec() {
		if (sexec1 == null) {
			sexec1 = new Sexec();
			try {
				sexec1.addSexecEventListener(new ipworksssh.SexecEventListener() {
					public void stdout(ipworksssh.SexecStdoutEvent e) {
						getJTextAreaResponse().append(new String(e.text));
					}
					public void connected(ipworksssh.SexecConnectedEvent e) {
					}
					public void connectionStatus(ipworksssh.SexecConnectionStatusEvent e) {
					}
					public void disconnected(ipworksssh.SexecDisconnectedEvent e) {
					}
					public void error(ipworksssh.SexecErrorEvent e) {
					}
					public void SSHCustomAuth(ipworksssh.SexecSSHCustomAuthEvent e){}
					public void SSHKeyboardInteractive(ipworksssh.SexecSSHKeyboardInteractiveEvent e) {
					}
					public void SSHServerAuthentication(ipworksssh.SexecSSHServerAuthenticationEvent e) {
						e.accept = true;
					}
					public void SSHStatus(ipworksssh.SexecSSHStatusEvent e) {
					}
					public void stderr(ipworksssh.SexecStderrEvent e) {
					}
				});
			} catch (TooManyListenersException e) {
				e.printStackTrace();
			}
		}
		return sexec1;
	}

	/**
	 * This method initializes jDialogMessageBox
	 *
	 * @return javax.swing.JDialog
	 */
	private JDialog getJDialogMessageBox() {
		if (jDialogMessageBox == null) {
			jDialogMessageBox = new JDialog(getJFrame());
			jDialogMessageBox.setSize(new Dimension(483, 186));
			jDialogMessageBox.setTitle("Notify Message");
			jDialogMessageBox.setContentPane(getJContentPaneMessageBox());
		}
		return jDialogMessageBox;
	}

	/**
	 * This method initializes jContentPaneMessageBox
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPaneMessageBox() {
		if (jContentPaneMessageBox == null) {
			jContentPaneMessageBox = new JPanel();
			jContentPaneMessageBox.setLayout(null);
			jContentPaneMessageBox.add(getJTextPaneNotifyMessage(), null);
			jContentPaneMessageBox.add(getJButtonNotifyOK(), null);
		}
		return jContentPaneMessageBox;
	}

	/**
	 * This method initializes jTextPaneNotifyMessage
	 *
	 * @return javax.swing.JTextPane
	 */
	private JTextPane getJTextPaneNotifyMessage() {
		if (jTextPaneNotifyMessage == null) {
			jTextPaneNotifyMessage = new JTextPane();
			jTextPaneNotifyMessage.setBounds(new Rectangle(6, 14, 461, 68));
			jTextPaneNotifyMessage.setBackground(new Color(238, 238, 238));
		}
		return jTextPaneNotifyMessage;
	}

	/**
	 * This method initializes jButtonNotifyOK
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonNotifyOK() {
		if (jButtonNotifyOK == null) {
			jButtonNotifyOK = new JButton();
			jButtonNotifyOK.setBounds(new Rectangle(204, 107, 66, 31));
			jButtonNotifyOK.setText("OK");
			jButtonNotifyOK.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getJDialogMessageBox().setVisible(false);
				}
			});
		}
		return jButtonNotifyOK;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				sexec application = new sexec();
				application.getJFrame().setVisible(true);
			}
		});
	}

	/**
	 * This method initializes jFrame
	 *
	 * @return javax.swing.JFrame
	 */
	private JFrame getJFrame() {
		if (jFrame == null) {
			jFrame = new JFrame();
			jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jFrame.setBounds(new Rectangle(0, 0, 512, 392));
			jFrame.setContentPane(getJContentPane());
			jFrame.setTitle("SExec demo application");
		}
		return jFrame;
	}

	/**
	 * This method initializes jContentPane
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jLabelResponse = new JLabel();
			jLabelResponse.setBounds(new Rectangle(8, 140, 64, 15));
			jLabelResponse.setText("Response:");
			jLabelSSHCommand = new JLabel();
			jLabelSSHCommand.setBounds(new Rectangle(8, 89, 120, 14));
			jLabelSSHCommand.setText("SSH Command:");
			jLabel = new JLabel();
			jLabel.setBounds(new Rectangle(286, 59, 62, 15));
			jLabel.setText("Password:");
			jLabelUser = new JLabel();
			jLabelUser.setBounds(new Rectangle(286, 37, 38, 15));
			jLabelUser.setText("User:");
			jLabelServer = new JLabel();
			jLabelServer.setBounds(new Rectangle(8, 37, 72, 15));
			jLabelServer.setText("SSH Server:");
			jLabelDescription2 = new JLabel();
			jLabelDescription2.setBounds(new Rectangle(15, 12, 471, 22));
			jLabelDescription2.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabelDescription2.setText("securely execute commands on a server.");
			jLabelDescription2.setHorizontalAlignment(SwingConstants.CENTER);
			jLabelDescription = new JLabel();
			jLabelDescription.setBounds(new Rectangle(15, 1, 471, 19));
			jLabelDescription.setHorizontalAlignment(SwingConstants.CENTER);
			jLabelDescription.setHorizontalTextPosition(SwingConstants.CENTER);
			jLabelDescription.setText("This demo shows how to use the SExec component to remotely and");
			jContentPane = new JPanel();
			jContentPane.setLayout(null);
			jContentPane.add(jLabelDescription, null);
			jContentPane.add(jLabelDescription2, null);
			jContentPane.add(jLabelServer, null);
			jContentPane.add(jLabelUser, null);
			jContentPane.add(jLabel, null);
			jContentPane.add(jLabelSSHCommand, null);
			jContentPane.add(jLabelResponse, null);
			jContentPane.add(getJTextFieldServer(), null);
			jContentPane.add(getJTextFieldUser(), null);
			jContentPane.add(getJPasswordFieldPassword(), null);
			jContentPane.add(getJTextFieldCommand(), null);
			jContentPane.add(getJButtonExecute(), null);
			jContentPane.add(getJScrollPaneResponse(), null);
		}
		return jContentPane;
	}
}






