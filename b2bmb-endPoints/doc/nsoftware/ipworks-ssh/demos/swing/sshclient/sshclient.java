import ipworksssh.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.TooManyListenersException;

public class sshclient {

	private JFrame jFrameMain = null;

	private JPanel jContentPaneMain = null;

	private JLabel jLabelDemoDescription = null;

	private JLabel jLabelSSHServer = null;

	private JLabel jLabelSSHPort = null;

	private JLabel jLabelUser = null;

	private JLabel jLabelPassword = null;

	private JTextField jTextFieldSSHServer = null;

	private JTextField jTextFieldSSHPort = null;

	private JTextField jTextFieldUser = null;

	private JPasswordField jPasswordFieldPassword = null;

	private JButton jButtonConnect = null;

	private JScrollPane jScrollPaneResponse = null;

	private JTextArea jTextAreaResponse = null;

	private Sshclient sshclient1 = null; //  @jve:decl-index=0:visual-constraint="562,20"

	private String channelId = "0"; //  @jve:decl-index=0:

	/**
	 * This method initializes jTextFieldSSHServer
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldSSHServer() {
		if (jTextFieldSSHServer == null) {
			jTextFieldSSHServer = new JTextField();
			jTextFieldSSHServer.setBounds(new Rectangle(76, 40, 176, 19));
		}
		return jTextFieldSSHServer;
	}

	/**
	 * This method initializes jTextFieldSSHPort
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldSSHPort() {
		if (jTextFieldSSHPort == null) {
			jTextFieldSSHPort = new JTextField();
			jTextFieldSSHPort.setBounds(new Rectangle(328, 40, 176, 19));
		}
		return jTextFieldSSHPort;
	}

	/**
	 * This method initializes jTextFieldUser
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldUser() {
		if (jTextFieldUser == null) {
			jTextFieldUser = new JTextField();
			jTextFieldUser.setBounds(new Rectangle(76, 66, 176, 19));
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
			jPasswordFieldPassword.setBounds(new Rectangle(328, 64, 176
				, 19));
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
                        jButtonConnect.setBounds(new Rectangle(404, 94, 100, 24));
			jButtonConnect.setText("Connect");
			jButtonConnect.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (getJButtonConnect().getText().equals("Connect")) {
						try {
							getJTextAreaResponse().requestFocus();

							getSshclient().setSSHUser(getJTextFieldUser().getText());
							sshclient1.setSSHPassword(new String(getJPasswordFieldPassword().getPassword()));
							sshclient1.SSHLogon(getJTextFieldSSHServer().getText(), Integer.valueOf(getJTextFieldSSHPort().getText()).intValue());

							channelId = sshclient1.openChannel("session");
							sshclient1.openTerminal(channelId, "vt100", 80, 24, false, "\0");
							sshclient1.startService(channelId, "shell", "");

                                                        jButtonConnect.setText("Disconnect");
						}
						catch (IPWorksSSHException ipwe) {
                                                        showError("code="+ipwe.getCode()+"; msg=\""+ipwe.getMessage()+"\"","IPWorksSSHException");
							ipwe.printStackTrace();
						}
					} else {
                                                try {
                                                        sshclient1.SSHLogoff();
                                                        jButtonConnect.setText("Connect");
                                                } catch (IPWorksSSHException ipwe) {
                                                        showError("code="+ipwe.getCode()+"; msg=\""+ipwe.getMessage()+"\"","IPWorksSSHException");
                                                        ipwe.printStackTrace();
                                                }

                                        }
				}
			});
		}
		return jButtonConnect;
	}

	/**
	 * This method initializes jScrollPaneResponse
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPaneResponse() {
		if (jScrollPaneResponse == null) {
			jScrollPaneResponse = new JScrollPane();
			jScrollPaneResponse.setBounds(new Rectangle(8, 132, 496, 356));
			jScrollPaneResponse.setViewportView(getJTextAreaResponse());
		}
		return jScrollPaneResponse;
	}

	/**
	 * This method initializes jTextAreaResponse
	 *
	 * @return javax.swing.JTextArea
	 */
	protected JTextArea getJTextAreaResponse() {
		if (jTextAreaResponse == null) {
			jTextAreaResponse = new JTextArea();
			jTextAreaResponse.setWrapStyleWord(true);
			jTextAreaResponse.addKeyListener(new java.awt.event.KeyAdapter() {
				public void keyTyped(java.awt.event.KeyEvent e) {
					e.consume();
					try {
						getSshclient().getChannels().item(channelId).setDataToSend(new String(new char[] {e.getKeyChar()}).getBytes("iso-8859-1"));
					}
					catch (IPWorksSSHException ipwe) {
                                                showError("code="+ipwe.getCode()+"; msg=\""+ipwe.getMessage()+"\"","IPWorksSSHException");
						ipwe.printStackTrace();
					}
					catch (UnsupportedEncodingException e1) {
                                                showError("msg=\""+e1.getMessage()+"\"","UnsupportedEncodingException");
						e1.printStackTrace();
					}
					getJTextAreaResponse().getCaret().setVisible(true);

				}
			});
		}
		return jTextAreaResponse;
	}

	/**
	 * This method initializes sshclient
	 *
	 * @return ipworksssh.Sshclient
	 */
	private Sshclient getSshclient() {
		if (sshclient1 == null) {
			sshclient1 = new Sshclient();
			try {
				sshclient1.addSshclientEventListener(new ipworksssh.DefaultSshclientEventListener() {
					public void SSHStatus(ipworksssh.SshclientSSHStatusEvent e) {
						getJTextAreaResponse().append("SSHStatus: " + e.message + "\r\n");
						jTextAreaResponse.getCaret().setDot(jTextAreaResponse.getText().length());
					}
					
					public void SSHChannelOpened(ipworksssh.SshclientSSHChannelOpenedEvent e) {
						getJTextAreaResponse().append("SSHChannelOpened: " + e.channelId + "\r\n");
						jTextAreaResponse.getCaret().setDot(jTextAreaResponse.getText().length());
					}						
					
					public void SSHChannelClosed(ipworksssh.SshclientSSHChannelClosedEvent e) {
						getJTextAreaResponse().append("SSHChannelClosed: " + e.channelId + "\r\n");
						jTextAreaResponse.getCaret().setDot(jTextAreaResponse.getText().length());
					}						

					public void SSHChannelData(ipworksssh.SshclientSSHChannelDataEvent e) {
						getJTextAreaResponse().append(new String(e.channelData));
						jTextAreaResponse.getCaret().setDot(jTextAreaResponse.getText().length());
					}
					
					public void SSHChannelEOF(ipworksssh.SshclientSSHChannelEOFEvent e){}

					public void SSHChannelRequest(ipworksssh.SshclientSSHChannelRequestEvent e) {
						getJTextAreaResponse().append("SSHChannelRequest: " + e.channelId + ", " + new String(e.requestType) + "\r\n");
						jTextAreaResponse.getCaret().setDot(jTextAreaResponse.getText().length());
					}
					
					public void SSHChannelRequested(ipworksssh.SshclientSSHChannelRequestedEvent e) {
						getJTextAreaResponse().append("SSHChannelRequested: " + e.channelId + ", " + new String(e.requestType) + "\r\n");
						jTextAreaResponse.getCaret().setDot(jTextAreaResponse.getText().length());
					}					
					
					public void SSHCustomAuth(ipworksssh.SshclientSSHCustomAuthEvent e){}
					
					public void SSHKeyboardInteractive(ipworksssh.SshclientSSHKeyboardInteractiveEvent e) {
						getJTextAreaResponse().append("SSHKeyboardInteractive event fired.\r\n");
						jTextAreaResponse.getCaret().setDot(jTextAreaResponse.getText().length());						
					}
					public void SSHServerAuthentication(ipworksssh.SshclientSSHServerAuthenticationEvent e) {
						e.accept = true;
						getJTextAreaResponse().append("SSHServerAuthentication: " + e.fingerprint + "\r\n");
						jTextAreaResponse.getCaret().setDot(jTextAreaResponse.getText().length());
					}

					public void connected(SshclientConnectedEvent e) {
						getJTextAreaResponse().append("Connected: " + e.description + "\r\n");
						jTextAreaResponse.getCaret().setDot(jTextAreaResponse.getText().length());
					}

					public void connectionStatus(SshclientConnectionStatusEvent e) {
						getJTextAreaResponse().append("ConnectionStatus: " + e.connectionEvent + ", " + e.statusCode + ", " + e.description + "\r\n");
						jTextAreaResponse.getCaret().setDot(jTextAreaResponse.getText().length());
					}

					public void disconnected(SshclientDisconnectedEvent e) {
						getJTextAreaResponse().append("Disconnected: " + e.description + "\r\n");
						jTextAreaResponse.getCaret().setDot(jTextAreaResponse.getText().length());
					}

					public void error(SshclientErrorEvent e) {
						getJTextAreaResponse().append("Error: " + e.errorCode + ", " + e.description + "\r\n");
						jTextAreaResponse.getCaret().setDot(jTextAreaResponse.getText().length());
					}
				});
			}
			catch (TooManyListenersException e) {
                                showError("msg=\""+e.getMessage()+"\"","TooManyListenersException");
				e.printStackTrace();
			}
		}
		return sshclient1;
	}

	/**
	 * This method initializes jFrameMain
	 *
	 * @return javax.swing.JFrame
	 */
	private JFrame getJFrameMain() {
		if (jFrameMain == null) {
			jFrameMain = new JFrame();
			jFrameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			jFrameMain.setBounds(new Rectangle(0, 0, 520, 523));
			jFrameMain.setContentPane(getJContentPaneMain());
			jFrameMain.setTitle("SSHClient Demo");
                        jFrameMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		return jFrameMain;
	}

	/**
	 * This method initializes jContentPaneMain
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPaneMain() {
		if (jContentPaneMain == null) {
			jLabelPassword = new JLabel();
			jLabelPassword.setBounds(new Rectangle(259, 66, 72, 16));
			jLabelPassword.setText("Password:");
			jLabelUser = new JLabel();
			jLabelUser.setBounds(new Rectangle(6, 66, 72, 16));
			jLabelUser.setText("User:");
			jLabelSSHPort = new JLabel();
			jLabelSSHPort.setBounds(new Rectangle(259, 40, 176, 19));
			jLabelSSHPort.setDisplayedMnemonic(KeyEvent.VK_UNDEFINED);
			jLabelSSHPort.setText("SSH Port:");
			jLabelSSHServer = new JLabel();
			jLabelSSHServer.setBounds(new Rectangle(6, 40, 72, 16));
			jLabelSSHServer.setText("SSH Server:");
			jLabelDemoDescription = new JLabel();
			jLabelDemoDescription.setBounds(new Rectangle(9, 0, 493, 32));
			jLabelDemoDescription.setText("<html><p>This demo shows how to use the SSHClient component to connect to and execute commands on a remote SSH host.</p></html>");
			jContentPaneMain = new JPanel();
			jContentPaneMain.setLayout(null);
			jContentPaneMain.add(jLabelDemoDescription, null);
			jContentPaneMain.add(jLabelSSHServer, null);
			jContentPaneMain.add(jLabelSSHPort, null);
			jContentPaneMain.add(jLabelUser, null);
			jContentPaneMain.add(jLabelPassword, null);
			jContentPaneMain.add(getJTextFieldSSHServer(), null);
			jContentPaneMain.add(getJTextFieldSSHPort(), null);
			jContentPaneMain.add(getJTextFieldUser(), null);
			jContentPaneMain.add(getJPasswordFieldPassword(), null);
			jContentPaneMain.add(getJButtonConnect(), null);
			jContentPaneMain.add(getJScrollPaneResponse(), null);

			// Focus control code
			final Component[] focusOrder = new Component[] {
				getJTextFieldSSHServer(),
				getJTextFieldSSHPort(),
				getJTextFieldUser(),
				getJPasswordFieldPassword(),
				getJButtonConnect()
			};
			FocusTraversalPolicy policy = new FocusTraversalPolicy() {
				java.util.List list = Arrays.asList(focusOrder);
				public Component getFirstComponent(Container focusCycleRoot) {
					return focusOrder[0];
				}

				public Component getLastComponent(Container focusCycleRoot) {
					return focusOrder[focusOrder.length - 1];
				}

				public Component getComponentAfter(Container fcr, Component cmp) {
					int index = list.indexOf(cmp);
					return focusOrder[ (index + 1) % focusOrder.length];
				}

				public Component getComponentBefore(Container fcr, Component cmp) {
					int index = list.indexOf(cmp);
					return focusOrder[ (index - 1 + focusOrder.length) % focusOrder.length];
				}

				public Component getDefaultComponent(Container focusCycleRoot) {
					return focusOrder[0];
				}
			};
			jContentPaneMain.setFocusTraversalPolicy(policy);
		}
		return jContentPaneMain;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				sshclient application = new sshclient();
				application.getJFrameMain().setVisible(true);
			}
		});
	}

        /**
         *
         */
        private void showError(String msg, String title) {
                JOptionPane.showMessageDialog(getJFrameMain(),msg,title,JOptionPane.ERROR_MESSAGE);
        }
}






