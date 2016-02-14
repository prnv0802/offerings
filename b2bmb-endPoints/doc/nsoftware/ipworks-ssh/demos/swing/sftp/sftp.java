import ipworksssh.IPWorksSSHException;
import ipworksssh.Sftp;

import javax.swing.*;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.TooManyListenersException;
import java.util.Vector;

public class sftp {

    private JFrame jFrameSftpMain = null; //  @jve:decl-index=0:visual-constraint="10,10"

    private JPanel jPanelSftp = null;

    private JPanel jPanelLocalHost = null;

    private JPanel jPanelRemoteHost = null;

    private JLabel jLabelRemoteHost = null;

    private JPanel jPanelLocalToRemote = null;

    private JPanel jPanelRemoteToLocal = null;

    private JButton jButtonUpload = null;

    private JPanel jPanelOperationButtons = null;

    private JButton jButtonConnect = null;

    private JButton jButtonCancel = null;

    private JButton jButtonExit = null;

    private JButton jButtonDownload = null;

    private JLabel jLabel1 = null;

    private JPanel jPanelLocalHostBorder1 = null;

    private JPanel jPanelLocalHostBorder = null;

    private JComboBox jComboBoxRemoteHost = null;

    private JScrollPane jScrollPaneRemoteHost = null;

    private JTable jTableRemoteHost = null;

    private JPanel jPanelRemoteHostLabelBackground = null;

    private JComboBox jComboBoxLocalHost = null;

    private JScrollPane jScrollPaneLocalHost = null;

    private JTable jTableLocalHost = null;

    private JPanel jPanelLocalHostLabelBackground = null;

    private JFrame jFrameSftpLogin = null; //  @jve:decl-index=0:visual-constraint="12,483"

    private JPanel jContentPane = null;

    private JLabel jLabelServerInformation = null;

    private JPanel jPanelServerInformationGroupBox = null;

    private JButton jButtonLogin = null;

    private JButton jButtonCancel2 = null;

    private JPanel jPanelServerInformationLabelBackground = null;

    private JLabel jLabelHostName = null;

    private JLabel jLabelUser = null;

    private JLabel jLabelAuthenticationType = null;

    private JLabel jLabelKeyFile = null;

    private JLabel jLabelPassword = null;

    private JTextField jTextFieldHostName = null;

    private JTextField jTextFieldUser = null;

    private JComboBox jComboBoxAuthenticationType = null;

    private JTextField jTextFieldKeyFile = null;

    private JButton jButtonBrowse = null;

    private JPasswordField jPasswordFieldPassword = null;

    private String currentDir = null;
    private DefaultTableModel tmLocalHost = new DefaultTableModel(new String[] {"File Name", "Size", "Date"}, 0);
    private DefaultTableModel tmRemoteHost = new DefaultTableModel(new String[] {"File Name", "Size", "Date"}, 0); //  @jve:decl-index=0:visual-constraint="592,54"

    /**
     * This class is just for read-only, non-editable JTable.
     * Use it in place of Jtable(TableModel) constructor.
     *
     * @author /n software inc.
     *
     */
    private class myReadOnlyTable  extends JTable {
        private static final long serialVersionUID = 1L;
        public myReadOnlyTable(TableModel tm) {
            super(tm);
        }

        public boolean isCellEditable(int row, int col) {
            return false;
        }
    };

    /**
     * This method shows the outline (code and message string) for the
     * given IPWorksSSHExcption.
     *
     * @param ipwe - IPWorksSSHException
     */
    private void notifyIPWorksError(IPWorksSSHException ipwe) {
        String msg = "code = " + ipwe.getCode() + "; msg = \"" + ipwe.getMessage() + "\"";
        JOptionPane.showMessageDialog(getJFrameSftpMain(),msg,"IPWorksSSHException",JOptionPane.ERROR_MESSAGE);
    }

    /**
     * This method initializes jPanelSftp
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelSftp() {
        if (jPanelSftp == null) {
            jPanelSftp = new JPanel();
            jPanelSftp.setLayout(null);
            jPanelSftp.add(getJPanelLocalHost(), null);
            jPanelSftp.add(getJPanelRemoteHost(), null);
            jPanelSftp.add(getJPanelLocalToRemote(), null);
            jPanelSftp.add(getJPanelRemoteToLocal(), null);
            jPanelSftp.add(getJPanelOperationButtons(), null);
            jPanelSftp.add(getJScrollPaneTransactionLog(), null);
        }
        return jPanelSftp;
    }

    /**
     * This method initializes jPanelLocalHost
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelLocalHost() {
        if (jPanelLocalHost == null) {
            jLabel1 = new JLabel();
            jLabel1.setBounds(new Rectangle(18, -3, 102, 21));
            jLabel1.setText("Local Host");
            jPanelLocalHost = new JPanel();
            jPanelLocalHost.setLayout(null);
            jPanelLocalHost.setBounds(new Rectangle(14, 14, 246, 216));
            jPanelLocalHost.add(jLabel1, null);
            jPanelLocalHost.add(getJPanelLocalHostBorder(), null);
        }
        return jPanelLocalHost;
    }

    /**
     * This method initializes jPanelRemoteHost
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelRemoteHost() {
        if (jPanelRemoteHost == null) {
            jLabelRemoteHost = new JLabel();
            jLabelRemoteHost.setBounds(new Rectangle(18, -3, 102, 21));
            jLabelRemoteHost.setText("Remote Host");
            jPanelRemoteHost = new JPanel();
            jPanelRemoteHost.setLayout(null);
            jPanelRemoteHost.setBounds(new Rectangle(317, 14, 246, 216));
            jPanelRemoteHost.add(jLabelRemoteHost, null);
            jPanelRemoteHost.add(getJPanelLocalHostBorder1(), null);
        }
        return jPanelRemoteHost;
    }

    /**
     * This method initializes jPanelLocalToRemote
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelLocalToRemote() {
        if (jPanelLocalToRemote == null) {
            jPanelLocalToRemote = new JPanel();
            jPanelLocalToRemote.setLayout(new FlowLayout());
            jPanelLocalToRemote.setBounds(new Rectangle(260, 89, 57, 47));
            jPanelLocalToRemote.add(getJButtonUpload(), null);
        }
        return jPanelLocalToRemote;
    }

    /**
     * This method initializes jPanelRemoteToLocal
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelRemoteToLocal() {
        if (jPanelRemoteToLocal == null) {
            GridLayout gridLayout2 = new GridLayout();
            gridLayout2.setRows(1);
            GridLayout gridLayout = new GridLayout();
            gridLayout.setRows(1);
            jPanelRemoteToLocal = new JPanel();
            jPanelRemoteToLocal.setLayout(null);
            jPanelRemoteToLocal.setBounds(new Rectangle(260, 139, 57, 47));
            jPanelRemoteToLocal.add(getJButtonDownload(), null);
        }
        return jPanelRemoteToLocal;
    }

    /**
     * This method initializes jButtonUpload
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonUpload() {
        if (jButtonUpload == null) {
            jButtonUpload = new JButton();
            jButtonUpload.setText("=>");
            jButtonUpload.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (getJTableLocalHost().getSelectedRowCount() > 0) {
                        Vector selectedRow = (Vector) tmLocalHost.getDataVector().elementAt(getJTableLocalHost().getSelectedRow());
                        String fileName = (String) selectedRow.elementAt(0);
                        try {
                            getSftp1().setLocalFile(currentDir + File.separator + fileName);
                            getSftp1().setRemoteFile(fileName);
                            getSftp1().upload();
                        }
                        catch (IPWorksSSHException ipwe) {
                            notifyIPWorksError(ipwe);
                        }
                        Remote_Dir_Refresh();
                    }
                }
            });
        }
        return jButtonUpload;
    }

    /**
     * This method initializes jPanelOperationButtons
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelOperationButtons() {
        if (jPanelOperationButtons == null) {
            jPanelOperationButtons = new JPanel();
            jPanelOperationButtons.setLayout(null);
            jPanelOperationButtons.setBounds(new Rectangle(15, 387, 338, 37));
            jPanelOperationButtons.add(getJButtonConnect(), null);
            jPanelOperationButtons.add(getJButtonCancel(), null);
            jPanelOperationButtons.add(getJButtonExit(), null);
        }
        return jPanelOperationButtons;
    }

    /**
     * This method initializes jButtonConnect
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonConnect() {
        if (jButtonConnect == null) {
            jButtonConnect = new JButton();
            jButtonConnect.setBounds(new Rectangle(2, 2, 100, 30));
            jButtonConnect.setText("Connect");
            jButtonConnect.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (jButtonConnect.getText().equals("Connect")) {
                        getJFrameSftpLogin().setVisible(true);
                    }
                    else {
                        try {
                            transactionLog("getting logged off...\r\n");
                            sftp1.SSHLogoff();
                            jButtonConnect.setText("Connect");
                            transactionLog("logged off.\r\n");
                        }
                        catch (ipworksssh.IPWorksSSHException ipwe) {
                            notifyIPWorksError(ipwe);
                        }
                    }
                }
            });
        }
        return jButtonConnect;
    }

    /**
     * This method initializes jButtonCancel in Sftp Main Window.
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonCancel() {
        if (jButtonCancel == null) {
            jButtonCancel = new JButton();
            jButtonCancel.setBounds(new Rectangle(106, 2, 100, 30));
            jButtonCancel.setText("Cancel");
            jButtonCancel.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        getSftp1().interrupt();
                    }
                    catch (IPWorksSSHException ipwe) {
                        notifyIPWorksError(ipwe);
                    }
                }
            });
        }
        return jButtonCancel;
    }

    /**
     * This method initializes jButtonExit
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonExit() {
        if (jButtonExit == null) {
            jButtonExit = new JButton();
            jButtonExit.setBounds(new Rectangle(210, 2, 100, 30));
            jButtonExit.setText("Exit");
            jButtonExit.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    getJFrameSftpMain().dispose();
                }
            });
        }
        return jButtonExit;
    }

    /**
     * This method initializes jButtonDownload
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonDownload() {
        if (jButtonDownload == null) {
            jButtonDownload = new JButton();
            jButtonDownload.setText("<=");
            jButtonDownload.setBounds(new Rectangle(5, 5, 48, 26));
            jButtonDownload.addActionListener(new java.awt.event.ActionListener() {
                /**
                 * Mouseclick
                 */
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (getJTableRemoteHost().getSelectedRowCount() > 0) {
                        Vector selectedRow = (Vector) tmRemoteHost.getDataVector().elementAt(getJTableRemoteHost().getSelectedRow());
                        String fileName = (String) selectedRow.elementAt(0);
                        try {
                            getSftp1().setLocalFile(currentDir + File.separator + fileName);
                            getSftp1().setRemoteFile(fileName);
                            getSftp1().download();
                            getSftp1().setRemoteFile("");
                        }
                        catch (IPWorksSSHException ipwe) {
                            notifyIPWorksError(ipwe);
                        }
                        Local_Dir_Refresh();
                    }
                }
            });
        }
        return jButtonDownload;
    }

    /**
     * This method initializes jPanelLocalHostBorder1
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelLocalHostBorder1() {
        if (jPanelLocalHostBorder1 == null) {
            jPanelLocalHostBorder1 = new JPanel();
            jPanelLocalHostBorder1.setLayout(null);
            jPanelLocalHostBorder1.setBounds(new Rectangle(3, 7, 240, 206));
            jPanelLocalHostBorder1.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            jPanelLocalHostBorder1.add(getJComboBoxRemoteHost(), null);
            jPanelLocalHostBorder1.add(getJScrollPaneRemoteHost(), null);
            jPanelLocalHostBorder1.add(getJPanelRemoteHostLabelBackground(), null);
        }
        return jPanelLocalHostBorder1;
    }

    /**
     * This method initializes jPanelLocalHostBorder
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelLocalHostBorder() {
        if (jPanelLocalHostBorder == null) {
            jPanelLocalHostBorder = new JPanel();
            jPanelLocalHostBorder.setLayout(null);
            jPanelLocalHostBorder.setBounds(new Rectangle(3, 7, 240, 206));
            jPanelLocalHostBorder.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            jPanelLocalHostBorder.add(getJComboBoxLocalHost(), null);
            jPanelLocalHostBorder.add(getJScrollPaneLocalHost(), null);
            jPanelLocalHostBorder.add(getJPanelLocalHostLabelBackground(), null);
        }
        return jPanelLocalHostBorder;
    }

    /**
     * This method initializes jComboBoxRemoteHost
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getJComboBoxRemoteHost() {
        if (jComboBoxRemoteHost == null) {
            jComboBoxRemoteHost = new JComboBox();
            jComboBoxRemoteHost.setBounds(new Rectangle(3, 14, 234, 29));
        }
        return jComboBoxRemoteHost;
    }

    /**
     * This method initializes jScrollPaneRemoteHost
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPaneRemoteHost() {
        if (jScrollPaneRemoteHost == null) {
            jScrollPaneRemoteHost = new JScrollPane();
            jScrollPaneRemoteHost.setBounds(new Rectangle(3, 48, 234, 155));
            jScrollPaneRemoteHost.setViewportView(getJTableRemoteHost());
        }
        return jScrollPaneRemoteHost;
    }

    /**
     * This method initializes jTableRemoteHost
     *
     * @return javax.swing.JTable
     */
    private JTable getJTableRemoteHost() {
        if (jTableRemoteHost == null) {
            jTableRemoteHost = new myReadOnlyTable(tmRemoteHost);
            jTableRemoteHost.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() > 1) { // detect doubleclick
                        Vector selectedRow = (Vector) tmRemoteHost.getDataVector().elementAt(getJTableRemoteHost().getSelectedRow());
                        String dirName = (String) (selectedRow.elementAt(0));
                        String sizeInfo = (String) (selectedRow.elementAt(1));
                        if (sizeInfo.equals("<dir>")) {
                            try {
                                sftp1.setRemotePath(dirName); //
                                Remote_Dir_Refresh();
                            }
                            catch (IPWorksSSHException ipwe) {
                                notifyIPWorksError(ipwe);
                            }
                        }
                    }
                }
            });
        }
        return jTableRemoteHost;
    }

    /**
     * This method initializes jPanelRemoteHostLabelBackground
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelRemoteHostLabelBackground() {
        if (jPanelRemoteHostLabelBackground == null) {
            jPanelRemoteHostLabelBackground = new JPanel();
            jPanelRemoteHostLabelBackground.setLayout(new GridBagLayout());
            jPanelRemoteHostLabelBackground.setBounds(new Rectangle(9, -13, 90, 24));
            jPanelRemoteHostLabelBackground.setBackground(jPanelSftp.getBackground());
        }
        return jPanelRemoteHostLabelBackground;
    }

    /**
     * This method initializes jComboBoxLocalHost
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getJComboBoxLocalHost() {
        if (jComboBoxLocalHost == null) {
            jComboBoxLocalHost = new JComboBox();
            jComboBoxLocalHost.setBounds(new Rectangle(3, 14, 234, 29));
        }
        return jComboBoxLocalHost;
    }

    /**
     * This method initializes jScrollPaneLocalHost
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPaneLocalHost() {
        if (jScrollPaneLocalHost == null) {
            jScrollPaneLocalHost = new JScrollPane();
            jScrollPaneLocalHost.setBounds(new Rectangle(3, 48, 234, 155));
            jScrollPaneLocalHost.setViewportView(getJTableLocalHost());
        }
        return jScrollPaneLocalHost;
    }

    /**
     * This method initializes jTableLocalHost
     *
     * @return javax.swing.JTable
     */
    private JTable getJTableLocalHost() {
        if (jTableLocalHost == null) {
            jTableLocalHost = new myReadOnlyTable(tmLocalHost);
            jTableLocalHost.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() > 1) { // detect doubleclick
                        Vector selectedRow = (Vector) tmLocalHost.getDataVector().elementAt(getJTableLocalHost().getSelectedRow());
                        String dirName = (String) selectedRow.elementAt(0);
                        File dir = new File(currentDir + File.separator + dirName);
                        if (dir.exists() && dir.isDirectory()) {
                            try {
                                currentDir = dir.getCanonicalPath();
                            }
                            catch (java.io.IOException ioe) {
                                ;
                            }
                            tmLocalHost.setRowCount(0); // clear table
                            Local_Dir_Refresh();
                        }
                    }

                }
            });
        }
        return jTableLocalHost;
    }

    /**
     * This method initializes jPanelLocalHostLabelBackground
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelLocalHostLabelBackground() {
        if (jPanelLocalHostLabelBackground == null) {
            jPanelLocalHostLabelBackground = new JPanel();
            jPanelLocalHostLabelBackground.setLayout(new GridBagLayout());
            jPanelLocalHostLabelBackground.setBounds(new Rectangle(9, -13, 74, 24));
        }
        return jPanelLocalHostLabelBackground;
    }

    private boolean SftpLogin_blnLogin;

    /**
     * This method initializes jFrameSftpLogin
     *
     * @return javax.swing.JFrame
     */
    private JFrame getJFrameSftpLogin() {
        if (jFrameSftpLogin == null) {
            jFrameSftpLogin = new JFrame("Sftp Login");
            jFrameSftpLogin.setBounds(new Rectangle(0, 0, 456, 240));
            jFrameSftpLogin.setContentPane(getJContentPane());
            jFrameSftpLogin.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            jFrameSftpLogin.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowClosing(java.awt.event.WindowEvent e) {
                    jFrameSftpLogin.setVisible(false);
                    if (SftpLogin_blnLogin) {
                        try {
                            if ( sftp1 != null ) {
                                if ( sftp1.isConnected() ) {
                                    sftp1.SSHLogoff();
                                }
                                sftp1 = null; // make sure to clear previous connection data.
                            }
                            getSftp1().setSSHUser(getJTextFieldUser().getText());

                            if (getJComboBoxAuthenticationType().getSelectedIndex() == 0) {
                                // Password Authentication
                                sftp1.setSSHAuthMode(sftp1.amPassword);
                                sftp1.setSSHPassword(getJPasswordFieldPassword().getText());
                            }
                            else {
                                // Public Key Authentication
                                sftp1.setSSHAuthMode(sftp1.amPublicKey);
                                String certstore = getJTextFieldKeyFile().getText();
                                String certpasswd = getJPasswordFieldPassword().getText();
                                String certsubj = "*";
                                if (getJComboBoxAuthenticationType().getSelectedIndex() == 1) {
                                    // PEM
                                    sftp1.setSSHCert(new ipworksssh.Certificate(ipworksssh.Certificate.cstPEMKeyFile, certstore, certpasswd, certsubj));
                                }
                                else {
                                    // PFX
                                    sftp1.setSSHCert(new ipworksssh.Certificate(ipworksssh.Certificate.cstPFXFile, certstore, certpasswd, certsubj));
                                }
                            }
                            getJButtonConnect().setText("Disconnect");
                            String[] hostinfo = getJTextFieldHostName().getText().split(":");
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
                            transactionLog("Connecting to host \"" + hostname + "\"; port: " + portnumber + "\r\n");
                            sftp1.SSHLogon(hostname, portnumber);

                            Remote_Dir_Refresh();
                        }
                        catch (ipworksssh.IPWorksSSHException ipwe) {
                            notifyIPWorksError(ipwe);
                        }
                        catch (NumberFormatException ex) {
                            transactionLog("Invalid port number.");
                            JOptionPane.showMessageDialog(getJFrameSftpMain(),"Invalid port number.","Error",JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            });
        }
        return jFrameSftpLogin;
    }

    /**
     * This method initializes jContentPane
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJContentPane() {
        if (jContentPane == null) {
            jLabelServerInformation = new JLabel();
            jLabelServerInformation.setBounds(new Rectangle(23, -1, 126, 26));
            jLabelServerInformation.setText("Server Information");
            jContentPane = new JPanel();
            jContentPane.setLayout(null);
            jContentPane.add(jLabelServerInformation, null);
            jContentPane.add(getJPanelServerInformationGroupBox(), null);
            jContentPane.add(getJButtonLogin(), null);
            jContentPane.add(getJButtonCancel2(), null);
        }
        return jContentPane;
    }

    /**
     * This method initializes jPanelServerInformationGroupBox
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelServerInformationGroupBox() {
        if (jPanelServerInformationGroupBox == null) {
            jLabelPassword = new JLabel();
            jLabelPassword.setBounds(new Rectangle(9, 152, 73, 26));
            jLabelPassword.setText("Password");
            jLabelKeyFile = new JLabel();
            jLabelKeyFile.setBounds(new Rectangle(9, 119, 73, 24));
            jLabelKeyFile.setText("Key File");
            jLabelAuthenticationType = new JLabel();
            jLabelAuthenticationType.setBounds(new Rectangle(8, 85, 127, 25));
            jLabelAuthenticationType.setText("Authentication Type:");
            jLabelUser = new JLabel();
            jLabelUser.setBounds(new Rectangle(9, 51, 75, 24));
            jLabelUser.setText("User");
            jLabelHostName = new JLabel();
            jLabelHostName.setBounds(new Rectangle(9, 18, 75, 22));
            jLabelHostName.setText("Host Name");
            jPanelServerInformationGroupBox = new JPanel();
            jPanelServerInformationGroupBox.setLayout(null);
            jPanelServerInformationGroupBox.setBounds(new Rectangle(9, 13, 345, 190));
            jPanelServerInformationGroupBox.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
            jPanelServerInformationGroupBox.add(getJPanelServerInformationLabelBackground(), null);
            jPanelServerInformationGroupBox.add(jLabelHostName, null);
            jPanelServerInformationGroupBox.add(jLabelUser, null);
            jPanelServerInformationGroupBox.add(jLabelAuthenticationType, null);
            jPanelServerInformationGroupBox.add(jLabelKeyFile, null);
            jPanelServerInformationGroupBox.add(jLabelPassword, null);
            jPanelServerInformationGroupBox.add(getJTextFieldHostName(), null);
            jPanelServerInformationGroupBox.add(getJTextFieldUser(), null);
            jPanelServerInformationGroupBox.add(getJComboBoxAuthenticationType(), null);
            jPanelServerInformationGroupBox.add(getJTextFieldKeyFile(), null);
            jPanelServerInformationGroupBox.add(getJButtonBrowse(), null);
            jPanelServerInformationGroupBox.add(getJPasswordFieldPassword(), null);
        }
        return jPanelServerInformationGroupBox;
    }

    /**
     * This method initializes jButtonLogin
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonLogin() {
        if (jButtonLogin == null) {
            jButtonLogin = new JButton();
            jButtonLogin.setBounds(new Rectangle(362, 16, 77, 29));
            jButtonLogin.setText("Login");
            jButtonLogin.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    SftpLogin_blnLogin = true;

                    // Close jFrameSftpLogin
                    WindowEvent we = new WindowEvent(getJFrameSftpLogin(), WindowEvent.WINDOW_CLOSING);
                    getJFrameSftpLogin().dispatchEvent(we);
                    jFrameSftpLogin = null;
                }
            });
        }
        return jButtonLogin;
    }

    /**
     * This method initializes jButtonCancel2 in the SftpLogin window.
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonCancel2() {
        if (jButtonCancel2 == null) {
            jButtonCancel2 = new JButton();
            jButtonCancel2.setBounds(new Rectangle(361, 61, 78, 29));
            jButtonCancel2.setText("Cancel");
            jButtonCancel2.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    SftpLogin_blnLogin = false;
                    getJFrameSftpLogin().dispose();
                    jFrameSftpLogin = null;
                }
            });
        }
        return jButtonCancel2;
    }

    /**
     * This method initializes jPanelServerInformationLabelBackground
     *
     * @return javax.swing.JPanel
     */
    private JPanel getJPanelServerInformationLabelBackground() {
        if (jPanelServerInformationLabelBackground == null) {
            jPanelServerInformationLabelBackground = new JPanel();
            jPanelServerInformationLabelBackground.setLayout(new GridBagLayout());
            jPanelServerInformationLabelBackground.setBounds(new Rectangle(8, -10, 121, 21));
        }
        return jPanelServerInformationLabelBackground;
    }

    /**
     * This method initializes jTextFieldHostName
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextFieldHostName() {
        if (jTextFieldHostName == null) {
            jTextFieldHostName = new JTextField();
            jTextFieldHostName.setBounds(new Rectangle(80, 16, 255, 22));
            jTextFieldHostName.setText("<host>[:<port>]");
        }
        return jTextFieldHostName;
    }

    /**
     * This method initializes jTextFieldUser
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextFieldUser() {
        if (jTextFieldUser == null) {
            jTextFieldUser = new JTextField();
            jTextFieldUser.setBounds(new Rectangle(80, 51, 256, 22));
        }
        return jTextFieldUser;
    }

    /**
     * This method initializes jComboBoxAuthenticationType
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getJComboBoxAuthenticationType() {
        if (jComboBoxAuthenticationType == null) {
            jComboBoxAuthenticationType = new JComboBox(new String[] {"Password", "Public key authentication (PEM)", "Public key authentication (PFX)"});
            jComboBoxAuthenticationType.setBounds(new Rectangle(129, 86, 205, 20));
            jComboBoxAuthenticationType.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    boolean enable = false;
                    switch (getJComboBoxAuthenticationType().getSelectedIndex()) {
                        case 0: // password auth
                            enable = false;
                            break;
                        case 1:
                        case 2:
                            enable = true;
                            break;
                    }
                    getJTextFieldKeyFile().setEnabled(enable);
                    getJButtonBrowse().setEnabled(enable);
                }
            });
        }
        return jComboBoxAuthenticationType;
    }

    /**
     * This method initializes jTextFieldKeyFile
     *
     * @return javax.swing.JTextField
     */
    private JTextField getJTextFieldKeyFile() {
        if (jTextFieldKeyFile == null) {
            jTextFieldKeyFile = new JTextField();
            jTextFieldKeyFile.setBounds(new Rectangle(82, 120, 155, 20));
            jTextFieldKeyFile.setEnabled(false);
        }
        return jTextFieldKeyFile;
    }

    /**
     * This method initializes jButtonBrowse
     *
     * @return javax.swing.JButton
     */
    private JButton getJButtonBrowse() {
        if (jButtonBrowse == null) {
            jButtonBrowse = new JButton("Browse...");
            jButtonBrowse.setBounds(new Rectangle(243, 119, 91, 24));
            jButtonBrowse.setEnabled(false);
            jButtonBrowse.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        JFileChooser ofd = new JFileChooser(".." + File.separator + "..");
                        ofd.showOpenDialog(getJFrameSftpLogin());
                        getJTextFieldKeyFile().setText(ofd.getSelectedFile().getCanonicalPath());
                    }
                    catch (IOException ex) {
                        ;
                    }
                }
            });
        }
        return jButtonBrowse;
    }

    /**
     * This method initializes jPasswordFieldPassword
     *
     * @return javax.swing.JPasswordField
     */
    private JPasswordField getJPasswordFieldPassword() {
        if (jPasswordFieldPassword == null) {
            jPasswordFieldPassword = new JPasswordField();
            jPasswordFieldPassword.setBounds(new Rectangle(81, 155, 254, 23));
        }
        return jPasswordFieldPassword;
    }

    /**
     * This method initializes sftp1
     *
     * @return ipworksssh.Sftp
     */
    private Sftp getSftp1() {
        if (sftp1 == null) {
            sftp1 = new Sftp();
            try {
                sftp1.addSftpEventListener(new ipworksssh.SftpEventListener() {
                    public void dirList(ipworksssh.SftpDirListEvent e) {
                        if (e.isDir) {
                            tmRemoteHost.addRow(new String[] {e.fileName, "<dir>", ""});
                        }
                        else {
                            tmRemoteHost.addRow(new Object[] {e.fileName, String.valueOf(e.fileSize), e.fileTime});
                        }
                    }

                    public void connected(ipworksssh.SftpConnectedEvent e) {
                    }

                    public void connectionStatus(ipworksssh.SftpConnectionStatusEvent e) {
                    }

                    public void disconnected(ipworksssh.SftpDisconnectedEvent e) {
                    }

                    public void endTransfer(ipworksssh.SftpEndTransferEvent e) {
                    }

                    public void error(ipworksssh.SftpErrorEvent e) {
                    }
                    
                    public void SSHCustomAuth(ipworksssh.SftpSSHCustomAuthEvent e){}
                    
                    public void SSHKeyboardInteractive(ipworksssh.SftpSSHKeyboardInteractiveEvent e) {}
                    
                    public void SSHServerAuthentication(ipworksssh.SftpSSHServerAuthenticationEvent e) {
                        e.accept = true;
                    }

                    public void SSHStatus(ipworksssh.SftpSSHStatusEvent e) {
                        transactionLog(e.message + "\r\n");
                    }

                    public void startTransfer(ipworksssh.SftpStartTransferEvent e) {
                    }

                    public void transfer(ipworksssh.SftpTransferEvent e) {
                    }
                });
            }
            catch (TooManyListenersException ex) {
                ;
            }
        }
        return sftp1;
    }

    /**
     * This method initializes jScrollPaneTransactionLog
     *
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getJScrollPaneTransactionLog() {
        if (jScrollPaneTransactionLog == null) {
            jScrollPaneTransactionLog = new JScrollPane();
            jScrollPaneTransactionLog.setBounds(new Rectangle(15, 245, 546, 135));
            jScrollPaneTransactionLog.setViewportView(getJTextAreaTransactionLog());
        }
        return jScrollPaneTransactionLog;
    }

    /**
     * This method initializes jTextAreaTransactionLog
     *
     * @return javax.swing.JTextArea
     */
    private JTextArea getJTextAreaTransactionLog() {
        if (jTextAreaTransactionLog == null) {
            jTextAreaTransactionLog = new JTextArea();
            jTextAreaTransactionLog.setEditable(false);
        }
        return jTextAreaTransactionLog;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                sftp application = new sftp();
                application.getJFrameSftpMain().setVisible(true);
            }
        });
    }

    /**
     * This method returns a path to local SFTP home for current user.
     * @return java.lang.String
     */
    private String getDefaultLocalFtpHome() {
        return System.getProperty("user.home");
    }

    /**
     * This method initializes jFrameSftpMain
     *
     * @return javax.swing.JFrame
     */
    private JFrame getJFrameSftpMain() {
        if (jFrameSftpMain == null) {
            jFrameSftpMain = new JFrame();
            jFrameSftpMain.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jFrameSftpMain.setBounds(new Rectangle(0, 0, 585, 456));
            jFrameSftpMain.setContentPane(getJPanelSftp());
            jFrameSftpMain.setTitle("Sftp Demo");
            jFrameSftpMain.addWindowListener(new java.awt.event.WindowAdapter() {
                public void windowOpened(java.awt.event.WindowEvent e) {
                    // OnLoad
                    currentDir = getDefaultLocalFtpHome();
                    Local_Dir_Refresh();
                }
            });
        }
        return jFrameSftpMain;
    }

    private Sftp sftp1 = null; //  @jve:decl-index=0:visual-constraint="618,103"

    private JScrollPane jScrollPaneTransactionLog = null;

    private JTextArea jTextAreaTransactionLog = null;

    /**
     * This method returns all directories found at specified path
     *
     * @param path	a path to search directories
     * @return java.io.File[]
     */
    private File[] getDirs(String path) {
        File[] tmp = (new File(path)).listFiles();
        Vector ret = new Vector();

        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].isDirectory()) {
                ret.add(tmp[i]);
            }
        }
        File[] dirs = new File[ret.size()];
        for (int i = 0; i < dirs.length; i++) {
            dirs[i] = (File) ret.elementAt(i);
        }
        return dirs;
    }

    /**
     * This method returns all files found at specified path
     *
     * @param path a path to search files
     * @return java.io.File[]
     */
    private File[] getFiles(String path) {
        File[] tmp = (new File(path)).listFiles();
        Vector ret = new Vector();

        for (int i = 0; i < tmp.length; i++) {
            if (tmp[i].isFile()) {
                ret.add(tmp[i]);
            }
        }
        File[] files = new File[ret.size()];
        for (int i = 0; i < files.length; i++) {
            files[i] = (File) ret.elementAt(i);
        }
        return files;
    }

    /**
     * This method refreshes the table for local file/dir
     *
     */
    private void Local_Dir_Refresh() {
        //
        Update_ComboBox(getJComboBoxLocalHost(), currentDir);
        // refresh local directory list
        tmLocalHost.setRowCount(0); // clear all data rows

        tmLocalHost.addRow(new String[] {"..", "<dir>", ""});

        // salvage directory entries in the current directory
        File[] dirs = getDirs(currentDir);
        for (int i = 0; i < dirs.length; i++) {
            tmLocalHost.addRow(new Object[] {dirs[i].getName(), "<dir>", new Date(dirs[i].lastModified()).toLocaleString()});
        }
        // salvage file entries in the current directory
        File[] files = getFiles(currentDir);
        for (int i = 0; i < files.length; i++) {
            tmLocalHost.addRow(new Object[] {files[i].getName(),
                               new Long(files[i].length()),
                               new Date(files[i].lastModified()).toLocaleString()});
        }
    }

    /**
     * This method refreshes the table for remote file/dir
     *
     */
    private void Remote_Dir_Refresh() {
        Update_ComboBox(getJComboBoxRemoteHost(), sftp1.getRemotePath());
        // refresh remote directory list
        tmRemoteHost.setRowCount(0); // clear all data rows

        try {
            sftp1.setRemoteFile("");
            sftp1.listDirectory();
        }
        catch (ipworksssh.IPWorksSSHException ipwe) {
            notifyIPWorksError(ipwe);
        }
    }

    /**
     * This method updates a list of specified combobox
     *
     * @param cb	a combobox
     * @param s		a data to be placed at the top of the item list
     */
    private void Update_ComboBox(JComboBox cb, String s) {
        //
        cb.removeItem(s);
        cb.insertItemAt(s, 0);
        cb.setSelectedIndex(0);
    }

    /**
     * This method appends a message into jTextAreaTransactionLog
     *
     * @param
     */
    private void transactionLog(String msg) {
        getJTextAreaTransactionLog().append(msg);
        jTextAreaTransactionLog.setCaretPosition(jTextAreaTransactionLog.getText().length());

    }
}






