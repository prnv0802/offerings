// This demo requires JDK 1.5 or later

import ipworksssh.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.TooManyListenersException;
import java.util.Vector;

public class sshtunnel {

	private JFrame jFrameMain = null;  //  @jve:decl-index=0:visual-constraint="10,10"

	private JPanel jContentPaneMain = null;

	private JMenuBar jMenuBarMain = null;

	private JLabel jLabelDescription = null;

	private JToolBar jToolBarMain = null;

	private JButton jButtonStart = null;

	private JButton jButtonRestart = null;

	private JButton jButtonStop = null;

	private JTabbedPane jTabbedPaneMain = null;

	private JPanel jPanelTunnel = null;  //  @jve:decl-index=0:visual-constraint="8,365"

	private JButton jButtonNew = null;

	private JPanel jPanelLog = null;  //  @jve:decl-index=0:visual-constraint="26,359"

	private JScrollPane jScrollPaneLog = null;

	private JTextArea jTextAreaLog = null;

	private JButton jButtonEdit = null;

	private JButton jButtonRemove = null;

	private JScrollPane jScrollPaneTunnel = null;

	private myreadonlytable myReadOnlyTableTunnels = null;

	private DefaultTableModel dtmTunnels = null;  //  @jve:decl-index=0:visual-constraint="15,361"

	private ArrayList<Sshtunnel> tunnels = new ArrayList<Sshtunnel>();  //  @jve:decl-index=0:

	private JPanel jPanelStatus = null;

	private JLabel jLabelStatus = null;

	private mydialog MyDialogTunnelConfiguration = null;  //  @jve:decl-index=0:visual-constraint="78,353"

	private SshtunnelEventListener sshtunnelEventListenerCommon = null;  //  @jve:decl-index=0:visual-constraint="426,418"

	/**
	 * This method shows a specified string in the status bar.
	 *
	 * @param s -
	 */
	private void status(String s)
	{
		jLabelStatus.setText(s);
	}
	/**
	 * This method initializes jToolBarMain
	 *
	 * @return javax.swing.JToolBar
	 */
	private JToolBar getJToolBarMain() {
		if (jToolBarMain == null) {
			jToolBarMain = new JToolBar();
			jToolBarMain.setBounds(new Rectangle(0, 0, 617, 25));
			jToolBarMain.setFloatable(false);
			jToolBarMain.setToolTipText("");
			jToolBarMain.add(getJButtonStart());
			jToolBarMain.add(getJButtonRestart());
			jToolBarMain.add(getJButtonStop());
		}
		return jToolBarMain;
	}

	/**
	 * This method initializes jButtonStart
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonStart() {
		if (jButtonStart == null) {
			jButtonStart = new JButton();
			jButtonStart.setText("Start");
			jButtonStart.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					status("Starting...");
					try {
						for (Sshtunnel t: tunnels) {
							t.setListening(true);
						}
					} catch (IPWorksSSHException e1) {
                                                showError("code="+e1.getCode()+"; msg=\""+e1.getMessage()+"\"","IPWorksSSHException");
						e1.printStackTrace();
					}
					status("Started.");
				}
			});
		}
		return jButtonStart;
	}

	/**
	 * This method executes the common operation for restarting.
	 * This method is called by:
	 *    - on clicking "Restart" button
	 *    - on closing Tunnel Config dialog with being checked "Auto Start"
	 *
	 */
	private void restart() {
		status("Re-starting...");
		try {
			for (Sshtunnel t: tunnels) {
				t.shutdown();
				t.setListening(true);
			}
		} catch (IPWorksSSHException ipwe) {
                        showError("code="+ipwe.getCode()+"; msg=\""+ipwe.getMessage()+"\"","IPWorksSSHException");
			ipwe.printStackTrace();
		}
		status("Started.");
	}

	/**
	 * This method initializes jButtonRestart
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonRestart() {
		if (jButtonRestart == null) {
			jButtonRestart = new JButton();
			jButtonRestart.setText("Restart");
			jButtonRestart.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					restart();
				}
			});
		}
		return jButtonRestart;
	}

	/**
	 * This method initializes jButtonStop
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonStop() {
		if (jButtonStop == null) {
			jButtonStop = new JButton();
			jButtonStop.setText("Stop");
			jButtonStop.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					status("Stopping...");
					try {
						for (Sshtunnel t: tunnels) {
							t.setListening(false);
							t.shutdown();
						}
						status("Stopped...");
					} catch (IPWorksSSHException ipwe) {
                                                showError("code="+ipwe.getCode()+"; msg=\""+ipwe.getMessage()+"\"","IPWorksSSHException");
						ipwe.printStackTrace();
					}
				}
			});
		}
		return jButtonStop;
	}

	/**
	 * This method initializes jTabbedPaneMain
	 *
	 * @return javax.swing.JTabbedPane
	 */
	private JTabbedPane getJTabbedPaneMain() {
		if (jTabbedPaneMain == null) {
			jTabbedPaneMain = new JTabbedPane();
			jTabbedPaneMain.setBounds(new Rectangle(0, 71, 609, 211));
			jTabbedPaneMain.addTab("Tunnels", getJPanelTunnel());
			jTabbedPaneMain.addTab("Log", getJPanelLog());
		}
		return jTabbedPaneMain;
	}

	/**
	 * This method initializes jPanelTunnel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelTunnel() {
		if (jPanelTunnel == null) {
			jPanelTunnel = new JPanel();
			jPanelTunnel.setLayout(null);
			jPanelTunnel.setSize(new Dimension(480, 193));
			jPanelTunnel.add(getJButtonNew(), null);
			jPanelTunnel.add(getJButtonEdit(), null);
			jPanelTunnel.add(getJButtonRemove(), null);
			jPanelTunnel.add(getJScrollPaneTunnel(), null);
		}
		return jPanelTunnel;
	}

	/**
	 * This method initializes jButtonNew
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonNew() {
		if (jButtonNew == null) {
			jButtonNew = new JButton();
			jButtonNew.setBounds(new Rectangle(518, 6, 80, 23));
			jButtonNew.setText("New ...");
			jButtonNew.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getMyDialogTunnelConfiguration().setVisible(true);
					if (MyDialogTunnelConfiguration.Result == mydialog.DialogResult.OK) {
						try {
							// add new tunnel
							Sshtunnel newtunnel = new Sshtunnel();
							// add event handler to newtunnel here
							newtunnel.addSshtunnelEventListener(getSshtunnelEventListenerCommon());
							// set host, port, etc.
							newtunnel.setLocalPort(Integer.valueOf(getMyDialogTunnelConfiguration().jTextFieldLocalPort.getText()));
							newtunnel.setSSHHost(getMyDialogTunnelConfiguration().jTextFieldSSHHost.getText());
							newtunnel.setSSHPort(Integer.valueOf(getMyDialogTunnelConfiguration().jTextFieldSSHPort.getText()));
							newtunnel.setSSHUser(getMyDialogTunnelConfiguration().jTextFieldSSHUser.getText());
							newtunnel.setSSHPassword(new String(getMyDialogTunnelConfiguration().jPasswordFieldSSHPassword.getPassword()));
							newtunnel.setSSHForwardHost(getMyDialogTunnelConfiguration().jTextFieldRemoteHost.getText());
							newtunnel.setSSHForwardPort(Integer.valueOf(getMyDialogTunnelConfiguration().jTextFieldRemotePort.getText()));
							tunnels.add(newtunnel);

							getDtmTunnels().addRow(new Object[] {
									getMyDialogTunnelConfiguration().jTextFieldTunnelName.getText(),
									String.valueOf(newtunnel.getLocalPort()),
									newtunnel.getSSHHost(),
									String.valueOf(newtunnel.getSSHPort()),
									newtunnel.getSSHForwardHost(),
									String.valueOf(newtunnel.getSSHForwardPort())
							});

							if (getMyDialogTunnelConfiguration().jCheckBoxAutoRestart.isSelected()) {
								restart();
							}
						} catch (IPWorksSSHException ipwe) {
                                                        showError("code="+ipwe.getCode()+"; msg=\""+ipwe.getMessage()+"\"","IPWorksSSHException");
							ipwe.printStackTrace();
						} catch (TooManyListenersException ex) {
                                                        showError(ex.getMessage(),"TooManyListenersException");
							ex.printStackTrace();
						}
					}
				}
			});
		}
		return jButtonNew;
	}

	/**
	 * This method initializes jPanelLog
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelLog() {
		if (jPanelLog == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.fill = GridBagConstraints.BOTH;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.weighty = 1.0;
			gridBagConstraints.gridx = 0;
			jPanelLog = new JPanel();
			jPanelLog.setLayout(new GridBagLayout());
			jPanelLog.setSize(new Dimension(519, 250));
			jPanelLog.add(getJScrollPaneLog(), gridBagConstraints);
		}
		return jPanelLog;
	}

	/**
	 * This method initializes jScrollPaneLog
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPaneLog() {
		if (jScrollPaneLog == null) {
			jScrollPaneLog = new JScrollPane();
			jScrollPaneLog.setViewportView(getJTextAreaLog());
		}
		return jScrollPaneLog;
	}

	/**
	 * This method initializes jTextAreaLog
	 *
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getJTextAreaLog() {
		if (jTextAreaLog == null) {
			jTextAreaLog = new JTextArea();
		}
		return jTextAreaLog;
	}

	/**
	 * This method initializes jButtonEdit
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonEdit() {
		if (jButtonEdit == null) {
			jButtonEdit = new JButton();
			jButtonEdit.setBounds(new Rectangle(518, 35, 80, 23));
			jButtonEdit.setText("Edit");
			jButtonEdit.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (getMyReadOnlyTableTunnels().getRowCount() > 0) {
						int index = getMyReadOnlyTableTunnels().getSelectedRow();
						mydialog config = new mydialog();
						Vector selectedRow = (Vector)getDtmTunnels().getDataVector().elementAt(index);
						config.jTextFieldTunnelName.setText((String)selectedRow.elementAt(0));
						config.jTextFieldLocalPort.setText(String.valueOf(selectedRow.elementAt(1)));
						config.jTextFieldSSHHost.setText((String)selectedRow.elementAt(2));
						config.jTextFieldSSHPort.setText(String.valueOf(selectedRow.elementAt(3)));
						config.jTextFieldRemoteHost.setText((String)selectedRow.elementAt(4));
						config.jTextFieldRemotePort.setText(String.valueOf(selectedRow.elementAt(5)));
						config.setVisible(true);
						if (config.Result == mydialog.DialogResult.OK) {
							Sshtunnel newtunnel = new Sshtunnel();
							try {
								newtunnel.shutdown();
								newtunnel.setLocalPort(Integer.valueOf(config.jTextFieldLocalPort.getText()));
								newtunnel.setSSHHost(config.jTextFieldSSHHost.getText());
                                                                newtunnel.setSSHPort(Integer.valueOf(config.jTextFieldSSHPort.getText()));
                                                                newtunnel.setSSHForwardHost(config.jTextFieldRemoteHost.getText());
                                                                newtunnel.setSSHForwardPort(Integer.valueOf(config.jTextFieldRemotePort.getText()));
							} catch (IPWorksSSHException ipwe) {
                                                                showError("code="+ipwe.getCode()+"; msg=\""+ipwe.getMessage()+"\"","IPWorksSSHException");
								ipwe.printStackTrace();
							}
							getDtmTunnels().removeRow(index);
							dtmTunnels.insertRow(index, new Object[]{
									config.jTextFieldTunnelName.getText()
									, String.valueOf(newtunnel.getLocalPort())
									, newtunnel.getSSHHost()
									, String.valueOf(newtunnel.getSSHPort())
									, newtunnel.getSSHForwardHost()
									, String.valueOf(newtunnel.getSSHForwardPort())
							});
						}
					}
				}
			});
		}
		return jButtonEdit;
	}

	/**
	 * This method initializes jButtonRemove
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonRemove() {
		if (jButtonRemove == null) {
			jButtonRemove = new JButton();
			jButtonRemove.setBounds(new Rectangle(518, 64, 80, 23));
			jButtonRemove.setText("Remove");
			jButtonRemove.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if (getMyReadOnlyTableTunnels().getRowCount() > 0) {
						int index = getMyReadOnlyTableTunnels().getSelectedRow();
						try {
							tunnels.get(index).setListening(false);
							tunnels.set(index, null);
							tunnels.remove(index);
							getDtmTunnels().removeRow(index);
						} catch (IPWorksSSHException ipwe) {
                                                        showError("code="+ipwe.getCode()+"; msg=\""+ipwe.getMessage()+"\"","IPWorksSSHException");
							ipwe.printStackTrace();
						}
					}
				}
			});
		}
		return jButtonRemove;
	}

	/**
	 * This method initializes jScrollPaneTunnel
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPaneTunnel() {
		if (jScrollPaneTunnel == null) {
			jScrollPaneTunnel = new JScrollPane();
			jScrollPaneTunnel.setBounds(new Rectangle(0, 0, 513, 184));
			jScrollPaneTunnel.setViewportView(getMyReadOnlyTableTunnels());
		}
		return jScrollPaneTunnel;
	}

	/**
	 * This method initializes myReadOnlyTableTunnels
	 *
	 * @return javax.swing.JTable
	 */
	private JTable getMyReadOnlyTableTunnels() {
		if (myReadOnlyTableTunnels == null) {
			myReadOnlyTableTunnels = new myreadonlytable(getDtmTunnels());
		}
		return myReadOnlyTableTunnels;
	}

	/**
	 * This method initializes dtmTunnels
	 *
	 * @return javax.swing.table.DefaultTableModel
	 */
	private DefaultTableModel getDtmTunnels() {
		if (dtmTunnels == null) {
			dtmTunnels = new DefaultTableModel(new String[] {"Name","Local Port","SSH Host","SSH Port","Remote Host","Remote Port"},0);
		}
		return dtmTunnels;
	}

	/**
	 * This method initializes jPanelStatus
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelStatus() {
		if (jPanelStatus == null) {
			jLabelStatus = new JLabel();
			jLabelStatus.setText("Stopped.");
			jPanelStatus = new JPanel();
			jPanelStatus.setLayout(new BoxLayout(getJPanelStatus(), BoxLayout.X_AXIS));
			jPanelStatus.setBounds(new Rectangle(0, 282, 610, 23));
			jPanelStatus.add(jLabelStatus, null);
		}
		return jPanelStatus;
	}
	/**
	 * This method initializes MyDialogTunnelConfiguration
	 *
	 * @return myDialog
	 */
	private mydialog getMyDialogTunnelConfiguration() {
		if (MyDialogTunnelConfiguration == null) {
			MyDialogTunnelConfiguration = new mydialog(getJFrameMain());
			MyDialogTunnelConfiguration.setModal(true);
		}
		return MyDialogTunnelConfiguration;
	}

	private void Log(String s) {
		s += "\r\n";
		getJTextAreaLog().append(s);
		jTextAreaLog.setCaretPosition(jTextAreaLog.getCaretPosition()+s.length());
//		System.out.print(s);
	}
	/**
	 * This method initializes sshtunnelEventListenerCommon which
	 * defines the common event actions for the SSH related events.
	 *
	 * @return ipworksssh.SshtunnelEventListener
	 */
	private SshtunnelEventListener getSshtunnelEventListenerCommon() {
		if (sshtunnelEventListenerCommon == null) {
			sshtunnelEventListenerCommon = new SshtunnelEventListener() {

				
				public void SSHKeyboardInteractive(SshtunnelSSHKeyboardInteractiveEvent e){}
				
				public void SSHServerAuthentication(SshtunnelSSHServerAuthenticationEvent e) {
					e.accept = true;
				    Log("SSHServerAuthentication: " + e.fingerprint);
				}

				public void SSHStatus(SshtunnelSSHStatusEvent e) {
					Log("SSHStatus: " + e.message);
				}

				public void connected(SshtunnelConnectedEvent e) {
					Log("Connection " + e.connectionId + " Connection: " + e.description);
				}

				public void connectionRequest(SshtunnelConnectionRequestEvent e) {
					Log("ConectionRequest: Address: " + e.address + " Port: " + String.valueOf(e.port));
				}

				public void disconnected(SshtunnelDisconnectedEvent e) {
					Log("Connection " + e.connectionId + " Disconnection: " + e.description);
				}

				public void error(SshtunnelErrorEvent e) {
					Log("Connection " + e.connectionId + " Error: " + String.valueOf(e.errorCode) + ", " + e.description);
				}

				public void dataIn(SshtunnelDataInEvent e) {
					/* do nothing */;
				}
				
				public void SSHCustomAuth(ipworksssh.SshtunnelSSHCustomAuthEvent e) {}
				
			};
		}
		return sshtunnelEventListenerCommon;
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				sshtunnel application = new sshtunnel();
				application.getJFrameMain().setVisible(true);
			}
		});
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
			jFrameMain.setBounds(new Rectangle(0, 0, 620, 337));
			jFrameMain.setContentPane(getJContentPaneMain());
			jFrameMain.setTitle("SSHTunnel Demo");
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
			jLabelDescription = new JLabel();
			jLabelDescription.setBounds(new Rectangle(6, 29, 604, 34));
			jLabelDescription.setText("<html><p>This demo shows how to use the SSHTunnel component to accept incoming plain text connections and tunnel them to a secure SSH host.  To use the demo, create a \"New\" tunnel below and click \"Start\".</p></html>");
			jContentPaneMain = new JPanel();
			jContentPaneMain.setLayout(null);
			jContentPaneMain.add(jLabelDescription, null);
			jContentPaneMain.add(getJToolBarMain(), null);
			jContentPaneMain.add(getJTabbedPaneMain(), null);
			jContentPaneMain.add(getJPanelStatus(), null);
		}
		return jContentPaneMain;
	}

        /**
         * This method shows an error information in the dialog
         *
         */
        private void showError(String msg, String title)
        {
                JOptionPane.showMessageDialog(getJFrameMain(),msg,title,JOptionPane.ERROR_MESSAGE);
        }

}









