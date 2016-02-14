import ipworksssh.IPWorksSSHException;
import ipworksssh.Sshell;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.TooManyListenersException;

public class sshell {

	private JFrame jFrameSShellMain = null;  //  @jve:decl-index=0:visual-constraint="6,8"
	private JPanel jContentPaneSShellMain = null;
	private JPanel jPanelCredentials = null;
	private JLabel jLabelServer = null;
	private JLabel jLabelUser = null;
	private JLabel jLabelPassword = null;
	private JTextField jTextFieldServer = null;
	private JTextField jTextFieldUser = null;
	private JPasswordField jPasswordFieldPassword = null;
	private JButton jButtonConnect = null;
	private Sshell sshell1 = null;  //  @jve:decl-index=0:visual-constraint="549,11"
	private JTextPane jTextPaneNotifyMessage = null;
	private JScrollPane jScrollPaneOut = null;
	private JTextArea jTextAreaOut = null;
	/**
	 * This method initializes jFrameSShellMain
	 *
	 * @return javax.swing.JFrame
	 */
	private JFrame getJFrameSShellMain() {
		if (jFrameSShellMain == null) {
			jFrameSShellMain = new JFrame();
			jFrameSShellMain.setBounds(new Rectangle(0, 0, 536, 481));
			jFrameSShellMain.setTitle("SShell demo (Connect direct to SSH server)");
			jFrameSShellMain.setContentPane(getJContentPaneSShellMain());
                        jFrameSShellMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		return jFrameSShellMain;
	}

	/**
	 * This method initializes jContentPaneSShellMain
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPaneSShellMain() {
		if (jContentPaneSShellMain == null) {
			jContentPaneSShellMain = new JPanel();
			jContentPaneSShellMain.setLayout(null);
			jContentPaneSShellMain.add(getJPanelCredentials(), null);
			jContentPaneSShellMain.add(getJScrollPaneOut(), null);
		}
		return jContentPaneSShellMain;
	}

	/**
	 * This method initializes jPanelCredentials
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelCredentials() {
		if (jPanelCredentials == null) {
			jLabelPassword = new JLabel();
			jLabelPassword.setBounds(new Rectangle(8, 66, 64, 15));
			jLabelPassword.setText("Password:");
			jLabelPassword.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jLabelUser = new JLabel();
			jLabelUser.setBounds(new Rectangle(8, 44, 48, 15));
			jLabelUser.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jLabelUser.setText("User:");
			jLabelServer = new JLabel();
			jLabelServer.setBounds(new Rectangle(8, 22, 48, 15));
			jLabelServer.setText("Server:");
			jPanelCredentials = new JPanel();
			jPanelCredentials.setLayout(null);
			jPanelCredentials.setBounds(new Rectangle(8, 10, 512, 96));
			jPanelCredentials.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Credentials", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51, 51, 51)));
			jPanelCredentials.add(jLabelServer, null);
			jPanelCredentials.add(jLabelUser, null);
			jPanelCredentials.add(jLabelPassword, null);
			jPanelCredentials.add(getJTextFieldServer(), null);
			jPanelCredentials.add(getJTextFieldUser(), null);
			jPanelCredentials.add(getJPasswordFieldPassword(), null);
			jPanelCredentials.add(getJButtonConnect(), null);
		}
		return jPanelCredentials;
	}

	/**
	 * This method initializes jTextFieldServer
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldServer() {
		if (jTextFieldServer == null) {
			jTextFieldServer = new JTextField();
			jTextFieldServer.setBounds(new Rectangle(72, 22, 328, 19));
			jTextFieldServer.setText("<host>[:<port>]");
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
			jTextFieldUser.setBounds(new Rectangle(72, 44, 328, 19));
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
			jPasswordFieldPassword.setBounds(new Rectangle(72, 66, 328, 19));
		}
		return jPasswordFieldPassword;
	}

	/**
	 * This method initializes jButtonConnect
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonConnect() {
		if (jButtonConnect == null) {
			jButtonConnect = new JButton();
			jButtonConnect.setBounds(new Rectangle(405, 66, 99, 23));
			jButtonConnect.setText("Connect");
			jButtonConnect.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					try {
						if (!getSshell().isConnected()) {
							sshell1.setSSHUser(getJTextFieldUser().getText());
							sshell1.setSSHPassword(getJPasswordFieldPassword().getText());
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
								portnumber = Integer.parseInt(hostinfo[1]);
								break;
							default:
								;
							}
							getJTextAreaOut().append("Connecting to host \""+hostname+"\"; port: "+portnumber+"\r\n");
							sshell1.SSHLogon(hostname, portnumber);
							getJButtonConnect().setText("Disconnect");
						} else {
							sshell1.SSHLogoff();
							getJButtonConnect().setText("Connect");
						}
					} catch (IPWorksSSHException ipwe) {
						notifyMessage("Code: "+ipwe.getCode()+"; Message: \""+ipwe.getMessage()+"\""
								,"IPWorksSSHException");
					}
				}
			});
		}
                getJTextAreaOut().requestFocus();
		return jButtonConnect;
	}

	/**
	 * This method initializes sshell
	 *
	 * @return ipworksssh.Sshell
	 */
	private Sshell getSshell() {
		if (sshell1 == null) {
			sshell1 = new Sshell();
			try {
				sshell1.addSshellEventListener(new ipworksssh.SshellEventListener() {
					public void stdout(ipworksssh.SshellStdoutEvent e) {
						getJTextAreaOut().append(new String(e.text));
                                                jTextAreaOut.getCaret().setDot(jTextAreaOut.getText().length());
					}
					public void stderr(ipworksssh.SshellStderrEvent e) {
						getJTextAreaOut().append(new String(e.text));
                                                jTextAreaOut.getCaret().setDot(jTextAreaOut.getText().length());
					}
					public void connected(ipworksssh.SshellConnectedEvent e) {
					}
					public void connectionStatus(ipworksssh.SshellConnectionStatusEvent e) {
					}
					public void disconnected(ipworksssh.SshellDisconnectedEvent e) {
					}
					public void error(ipworksssh.SshellErrorEvent e) {
					}
					
					public void SSHCustomAuth(ipworksssh.SshellSSHCustomAuthEvent e){}
					
					public void SSHKeyboardInteractive(ipworksssh.SshellSSHKeyboardInteractiveEvent e) {
					}					
					public void SSHServerAuthentication(ipworksssh.SshellSSHServerAuthenticationEvent e) {
						e.accept = true;
					}
					public void SSHStatus(ipworksssh.SshellSSHStatusEvent e) {
					}
				});
			} catch (TooManyListenersException e) {
				notifyMessage("Message: \""+e.getMessage()+"\"","TooManyListenersException");
			}
		}
		return sshell1;
	}

	/**
	 * This method displays a simple dialog with given message and title
	 *
	 * @param msg - a message string to be shown
	 * @param title - dialog title string
	 */
	private void notifyMessage(String msg, String title) {
                JOptionPane.showMessageDialog(getJFrameSShellMain(),msg,title,JOptionPane.ERROR_MESSAGE);
	}


	/**
	 * This method initializes jScrollPaneOut
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPaneOut() {
		if (jScrollPaneOut == null) {
			jScrollPaneOut = new JScrollPane();
			jScrollPaneOut.setBounds(new Rectangle(8, 114, 511, 330));
			jScrollPaneOut.setViewportView(getJTextAreaOut());
			jScrollPaneOut.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
		}
		return jScrollPaneOut;
	}

	/**
	 * This method initializes jTextAreaOut
	 *
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getJTextAreaOut() {
		if (jTextAreaOut == null) {
			jTextAreaOut = new JTextArea();
			jTextAreaOut.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyTyped(java.awt.event.KeyEvent e) {
					try {
						getSshell().setStdin(Character.toString(e.getKeyChar()));
					} catch (IPWorksSSHException ipwe) {
						notifyMessage("Code: "+ipwe.getCode()+"; Message: \""+ipwe.getMessage()+"\"","IPWorksSSHException");
						ipwe.printStackTrace();
					}
					e.consume();
				}
			});
		}
		return jTextAreaOut;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				sshell application = new sshell ();
				application.getJFrameSShellMain().setVisible(true);
			}
		});
	}

}






