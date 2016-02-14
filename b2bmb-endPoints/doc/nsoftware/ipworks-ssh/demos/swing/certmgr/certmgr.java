import ipworksssh.Certificate;
import ipworksssh.Certmgr;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.TooManyListenersException;

public class certmgr {

	private JFrame jFrameMain = null;

	private JPanel jContentPaneMain = null;

	private JLabel jLabelStorePassword = null;

	private JComboBox jComboBoxStoreType = null;

	private JLabel jLabelCertificatesInStore = null;

	private JButton jButtonBrowse = null;

	private JLabel jLabelCertificateStorePath = null;

	private JTextField jTextFieldStoreFile = null;

	private JLabel jLabelStoreType = null;

	private Certmgr certmgr1 = null;  //  @jve:decl-index=0:visual-constraint="473,38"

	private JList jListCertificates = null;

	private JLabel jLabelCertificateAttributes = null;

	private JButton jButtonLoad = null;

	private JScrollPane jScrollPaneCertificateAttributes = null;

	private JTextArea jTextAreaCertificateAttributes = null;

	private DefaultListModel defaultListModelCertificates = null;  //  @jve:decl-index=0:visual-constraint="481,84"

	private JFileChooser jFileChooserCertificateStore = null;  //  @jve:decl-index=0:visual-constraint="15,438"

	private JTextField jTextFieldPassword = null;

	/**
	 * This method initializes jComboBoxStoreType
	 *
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getJComboBoxStoreType() {
		if (jComboBoxStoreType == null) {
			jComboBoxStoreType = new JComboBox(new String[] {"JKS Store", "PFX Store"});
			jComboBoxStoreType.setBounds(new Rectangle(209, 80, 133, 21));
		}
		return jComboBoxStoreType;
	}

	/**
	 * This method initializes jButtonBrowse
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonBrowse() {
		if (jButtonBrowse == null) {
			jButtonBrowse = new JButton();
			jButtonBrowse.setBounds(new Rectangle(347, 32, 89, 21));
			jButtonBrowse.setText("Browse");
			jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					getJFileChooserCertificateStore().setCurrentDirectory(new File(getJTextFieldStoreFile().getText()));
					jFileChooserCertificateStore.setFileSelectionMode(JFileChooser.FILES_ONLY);
					jFileChooserCertificateStore.setDialogTitle("Select Store");
					if (jFileChooserCertificateStore.showOpenDialog(getJFrameMain()) == JFileChooser.APPROVE_OPTION) {
						getJTextFieldStoreFile().setText(jFileChooserCertificateStore.getSelectedFile().toString());
					}
				}
			});
		}
		return jButtonBrowse;
	}

	/**
	 * This method initializes jTextFieldStoreFile
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldStoreFile() {
		if (jTextFieldStoreFile == null) {
			jTextFieldStoreFile = new JTextField();
			jTextFieldStoreFile.setBounds(new Rectangle(9, 32, 332, 21));
			jTextFieldStoreFile.setText("myidentities.jks");
		}
		return jTextFieldStoreFile;
	}

	/**
	 * This method initializes certmgr
	 *
	 * @return ipworksssh.Certmgr
	 */
	private Certmgr getCertmgr() {
		if (certmgr1 == null) {
			certmgr1 = new Certmgr();
			try {
				certmgr1.addCertmgrEventListener(new ipworksssh.CertmgrEventListener() {
					public void certList(ipworksssh.CertmgrCertListEvent e) {
						getDefaultListModelCertificates().addElement(e.certSubject);
					}
					public void certChain(ipworksssh.CertmgrCertChainEvent e) {
					}
					public void error(ipworksssh.CertmgrErrorEvent e) {
					}
					public void keyList(ipworksssh.CertmgrKeyListEvent e) {
					}
					public void storeList(ipworksssh.CertmgrStoreListEvent e) {
					}
				});
			} catch (TooManyListenersException e) {
				e.printStackTrace();
			}
		}
		return certmgr1;
	}

	/**
	 * This method initializes jListCertificates
	 *
	 * @return javax.swing.JList
	 */
	private JList getJListCertificates() {
		if (jListCertificates == null) {
			jListCertificates = new JList(getDefaultListModelCertificates());
			jListCertificates.setBounds(new Rectangle(8, 142, 174, 235));
			jListCertificates.addMouseListener(new java.awt.event.MouseAdapter() {
				public void mouseClicked(java.awt.event.MouseEvent e) {
					try {
						getJTextAreaCertificateAttributes().setText("");
                                                // get the specified certificate from the store file
                                                if ( jComboBoxStoreType.getSelectedItem().toString().equals("JKS Store") ) {
                                                        // JKS
                                                        getCertmgr().setCert(new ipworksssh.Certificate(
                                                                Certificate.cstJKSFile,
                                                                getJTextFieldStoreFile().getText(),
                                                                getJTextFieldPassword().getText(),
                                                                getJListCertificates().getSelectedValue().toString()
                                                                ));
                                                } else {
                                                        // PFX
                                                        getCertmgr().setCert(new ipworksssh.Certificate(
                                                                Certificate.cstPFXFile,
                                                                getJTextFieldStoreFile().getText(),
                                                                getJTextFieldPassword().getText(),
                                                                getJListCertificates().getSelectedValue().toString()
                                                                ));
                                                }
						jTextAreaCertificateAttributes.append("Issuer: " + certmgr1.getCert().getIssuer() + "\r\n");
						jTextAreaCertificateAttributes.append("Subject: " + certmgr1.getCert().getSubject() + "\r\n");
						jTextAreaCertificateAttributes.append("Public Key: " + certmgr1.getCert().getPublicKey() + "\r\n");
                                                if (certmgr1.getCert().getPrivateKeyAvailable()) {
                                                        jTextAreaCertificateAttributes.append("You have a private key corresponding to this certificate");
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			});
		}
		return jListCertificates;
	}

	/**
	 * This method updates entries in the Certificates List
	 *
	 */
	private void updateEntries(){
		getDefaultListModelCertificates().clear();
		try {
			if(getJComboBoxStoreType().getSelectedItem().equals("JKS Store")) {
				getCertmgr().setCertStoreType(Certmgr.cstJKSFile);
			} else {
				getCertmgr().setCertStoreType(Certmgr.cstPFXFile);
			}
			getCertmgr().setCertStorePassword(getJTextFieldPassword().getText());
			getCertmgr().setCertStore(getJTextFieldStoreFile().getText());
			getCertmgr().listStoreCertificates();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}

	/**
	 * This method initializes jButtonLoad
	 *
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonLoad() {
		if (jButtonLoad == null) {
			jButtonLoad = new JButton();
			jButtonLoad.setBounds(new Rectangle(348, 80, 89, 21));
			jButtonLoad.setText("Load");
			jButtonLoad.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					updateEntries();
				}
			});
		}
		return jButtonLoad;
	}

	/**
	 * This method initializes jScrollPaneCertificateAttributes
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPaneCertificateAttributes() {
		if (jScrollPaneCertificateAttributes == null) {
			jScrollPaneCertificateAttributes = new JScrollPane();
			jScrollPaneCertificateAttributes.setBounds(new Rectangle(195, 143, 240, 235));
			jScrollPaneCertificateAttributes.setViewportView(getJTextAreaCertificateAttributes());
		}
		return jScrollPaneCertificateAttributes;
	}

	/**
	 * This method initializes jTextAreaCertificateAttributes
	 *
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getJTextAreaCertificateAttributes() {
		if (jTextAreaCertificateAttributes == null) {
			jTextAreaCertificateAttributes = new JTextArea();
			jTextAreaCertificateAttributes.setLineWrap(true);
		}
		return jTextAreaCertificateAttributes;
	}

	/**
	 * This method initializes defaultListModelCertificates
	 *
	 * @return javax.swing.DefaultListModel
	 */
	private DefaultListModel getDefaultListModelCertificates() {
		if (defaultListModelCertificates == null) {
			defaultListModelCertificates = new DefaultListModel();
		}
		return defaultListModelCertificates;
	}

	/**
	 * This method initializes jFileChooserCertificateStore
	 *
	 * @return javax.swing.JFileChooser
	 */
	private JFileChooser getJFileChooserCertificateStore() {
		if (jFileChooserCertificateStore == null) {
			jFileChooserCertificateStore = new JFileChooser();
			jFileChooserCertificateStore.setSize(new Dimension(444, 308));
		}
		return jFileChooserCertificateStore;
	}

	/**
	 * This method initializes jTextFieldPassword
	 *
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldPassword() {
		if (jTextFieldPassword == null) {
			jTextFieldPassword = new JTextField();
			jTextFieldPassword.setBounds(new Rectangle(11, 80, 189, 21));
			jTextFieldPassword.setText("password");
		}
		return jTextFieldPassword;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				certmgr application = new certmgr();
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
			jFrameMain.setSize(453, 416);
			jFrameMain.setContentPane(getJContentPaneMain());
			jFrameMain.setTitle("CertMgr demo");
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
			jLabelCertificateAttributes = new JLabel();
			jLabelCertificateAttributes.setBounds(new Rectangle(195, 118, 176, 20));
			jLabelCertificateAttributes.setText("Certificate attributes:");
			jLabelStoreType = new JLabel();
			jLabelStoreType.setBounds(new Rectangle(209, 64, 111, 15));
			jLabelStoreType.setText("Store type:");
			jLabelCertificateStorePath = new JLabel();
			jLabelCertificateStorePath.setBounds(new Rectangle(9, 15, 185, 12));
			jLabelCertificateStorePath.setText("Certificate Store path:");
			jLabelCertificatesInStore = new JLabel();
			jLabelCertificatesInStore.setBounds(new Rectangle(8, 120, 169, 20));
			jLabelCertificatesInStore.setText("Certificates in Store:");
			jLabelStorePassword = new JLabel();
			jLabelStorePassword.setBounds(new Rectangle(11, 61, 119, 13));
			jLabelStorePassword.setText("Store password:");
			jContentPaneMain = new JPanel();
			jContentPaneMain.setLayout(null);
			jContentPaneMain.add(jLabelStorePassword, null);
			jContentPaneMain.add(getJComboBoxStoreType(), null);
			jContentPaneMain.add(jLabelCertificatesInStore, null);
			jContentPaneMain.add(getJButtonBrowse(), null);
			jContentPaneMain.add(jLabelCertificateStorePath, null);
			jContentPaneMain.add(getJTextFieldStoreFile(), null);
			jContentPaneMain.add(jLabelStoreType, null);
			jContentPaneMain.add(getJListCertificates(), null);
			jContentPaneMain.add(jLabelCertificateAttributes, null);
			jContentPaneMain.add(getJButtonLoad(), null);
			jContentPaneMain.add(getJScrollPaneCertificateAttributes(), null);
			jContentPaneMain.add(getJTextFieldPassword(), null);
		}
		return jContentPaneMain;
	}

}



