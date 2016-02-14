import inedi.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;


public class oftpserver extends JFrame implements OftpserverEventListener, ActionListener, ChangeListener, ItemListener {

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
    public void run() {
    oftpserver frame = new oftpserver();
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);}});
  }


  private static final int MAIN_FORM_WIDTH          = 750;
  private static final int MAIN_FORM_HEIGHT         = 550;
  private static final int COMPONENT_GAP            = 5;

  private Oftpserver            m_Server;
  private Certificate           m_SSLCert;
  private SimpleDateFormat      m_DateFormat    = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
  private Vector                m_Profiles      = new Vector();
  private DefaultComboBoxModel  m_ClientIds     = new DefaultComboBoxModel();
  private DefaultListModel      m_SendFiles     = new DefaultListModel();

  private JLabel        lblCertInfo         = new JLabel();
  private JTextField    txtServerId         = new JTextField();
  private JTextField    txtServerPassword   = new JTextField();
  private JTextField    txtServerPort       = new JTextField();
  private JButton       btnStartServer      = new JButton("Start");
  private JButton       btnSelectCert       = new JButton("Select Certificate");
  private JCheckBox     chkUseSSL           = new JCheckBox("Use SSL", false);
  private JCheckBox     chkReqSSLClientAuth = new JCheckBox("Require SSL Client Authentication", false);
  private JTextArea     txtAreaLog          = new JTextArea();
  private JOptionPane   msgBox              = new JOptionPane();
  private JCertSelector certSelctor         = new JCertSelector(this);

  private JComboBox     cbxClientId         = new JComboBox();
  private JButton       btnSaveProfile      = new JButton("Save Profile");
  private JButton       btnNewProfile       = new JButton("New Profile");
  private JButton       btnDeleteProfile    = new JButton("Delete Profile");
  private JTextField    txtClientPassword   = new JTextField();
  private JTextField    txtIncomingFolder   = new JTextField();
  private JTextField    txtOutgoingFolder   = new JTextField();
  private JButton       btnInFolderSelect   = new JButton("Browse");
  private JButton       btnOutFolderSelect  = new JButton("Browse");
  private JTextField    txtClientCertFile   = new JTextField();
  private JButton       btnClientCertSelect = new JButton("Browse");
  private JComboBox     cbxOutgoingSecurity = new JComboBox(new Object[]{"None", "Encrypted", "Signed", "Signed And Encrypted"});
  private JCheckBox     chkCompress         = new JCheckBox("Compress Outgoing Files");
  private JCheckBox     chkReqSignedReceipt = new JCheckBox("Request Signed Receipt");
  private JButton       btnRefresh          = new JButton("Refresh");
  private JButton       btnCreateTestFiles  = new JButton("Create Test Files");
  private JList         lstQueuedFiles      = new JList();
  private JLabel        lblSavedAt          = new JLabel();



  public oftpserver(){
    super("OFTPServer Demo");
    initOftpserver();
    initComponents();
    initListeners();
    initProfileDirectory();
    initProfiles();
  }
  private void initOftpserver() {
    try {
      m_Server = new Oftpserver();
      m_Server.setServerSSIDCode("SERVERSSID");
      m_Server.setServerPassword("PASSWORD");
      m_Server.setServerSFIDCode("SERVERSSID");
      m_Server.setLocalPort(3305);
      m_Server.setLocalHost("localhost");

      m_Server.addOftpserverEventListener(this);
    } catch (Exception ex) {}
  }
  private void initComponents() {
    JTabbedPane tabMain = new JTabbedPane();
    JPanel panelServer = new JPanel();
    JPanel panelClients = new JPanel();
    tabMain.setTabPlacement(JTabbedPane.TOP);
    tabMain.addTab("Server", null, panelServer);
    tabMain.addTab("Clients", null, panelClients);

    // Server
    JGroupBox gbServerSetting = new JGroupBox("Server Settings");
    JPanel panelCertSettings = new JPanel();
    borderLayoutTCB(
        panelCertSettings,
        createMultiLineLable("If a certificate is specified here, it must have a private key. It will be used for SSL support, as well as Signing and Decryption operations. Note that only clients supporting Version 2.0 will be able to connect if SSL is enabled."),
        layoutTC(layoutLC(btnSelectCert, lblCertInfo), new JLabel()),
        layoutLC(chkUseSSL, chkReqSSLClientAuth)
    );
    chkUseSSL.setEnabled(false);
    chkReqSSLClientAuth.setEnabled(false);
    borderLayoutLC(gbServerSetting,
                   gridLayout(4, 2, new Component[]{
                       new Label("Server Id:"), txtServerId,
                       new Label("Server Password:"), txtServerPassword,
                       new Label("Server Port:"), txtServerPort,
                       new Label(), btnStartServer}),
                   panelCertSettings);


    JGroupBox gbLog = new JGroupBox("Log");
    borderLayoutFill(gbLog, createScrollPane(txtAreaLog));
    borderLayoutTC(panelServer, gbServerSetting, gbLog);
    borderLayoutTC(getContentPane(),
                   createMultiLineLable("The OFTPServer demo shows how to receive and send files from/to an OFTP client. The server supports various settings,including OFTP 2 specific features such as SSL and Signing/Encryption support."),
                   tabMain);

    // Clients
    JGroupBox gpClientProfile = new JGroupBox("Client Profile");
    JGroupBox gpProfileDetails = new JGroupBox("Profile Details");
    gpClientProfile.setSize(100, 80);
    borderLayoutTC(panelClients, gpClientProfile, gpProfileDetails);

    JPanel panelClientId = new JPanel();
    borderLayoutCR(panelClientId, cbxClientId, new JLabel("Note that client profiles are loaded from the \"profiles\" sub-directory of the demo."));
    borderLayoutLC(gpClientProfile,
                   gridLayout(2, 1, new Component[]{new JLabel("Client Id:")}),
                   gridLayout(2, 1, new Component[]{panelClientId, layoutLC(gridLayout(1, 3, new Component[]{btnSaveProfile, btnNewProfile, btnDeleteProfile}), lblSavedAt)}));

    JPanel panelTemp = new JPanel();
    JPanel panelTemp1 = gridLayout(2, 2, new Component[]{new JLabel("Client Password:"), txtClientPassword, new JLabel(), new JLabel()});
    panelTemp1.setSize(120, 60);
    JPanel panelTemp4 = new JPanel();
    borderLayoutLCR(panelTemp4,
                    gridLayout(2, 1, new Component[]{new JLabel("Incoming Directory:"), new JLabel("Outgoing Directory:")}),
                    gridLayout(2, 1, new Component[]{txtIncomingFolder, txtOutgoingFolder}),
                    gridLayout(2, 1, new Component[]{btnInFolderSelect, btnOutFolderSelect}));
    borderLayoutLC(panelTemp, panelTemp1, panelTemp4);
    JPanel panelTemp2 = new JPanel();
    JPanel panelTemp5 = layoutLC(
            gridLayout(2, 1, new Component[]{new JLabel("Certificate File:"), new JLabel("Outgoing File Security:")}),
            gridLayout(2, 1, new Component[]{layoutCR(txtClientCertFile, btnClientCertSelect), gridLayout(1, 3, new Component[]{cbxOutgoingSecurity, chkCompress, chkReqSignedReceipt})}));

    borderLayoutTCB(panelTemp2,
                    panelTemp,
                    createMultiLineLable("If a client certificate file is specified, it will be used for SSL client authentication (if checked on the Server tab) as well as Signature Verification and Encryption operations. Note that only clients supporting Version 2.0 of OFTP will be able to support SSL, Signing, Encryption, and Compression operations."),
                    layoutTC(panelTemp5, gridLayout(1, 6, new Component[]{new JLabel("Queued Files:"), new JLabel(), new JLabel(), btnRefresh, btnCreateTestFiles})));

    borderLayoutTC(gpProfileDetails, panelTemp2, createScrollPane(lstQueuedFiles));

    lstQueuedFiles.setModel(m_SendFiles);
    cbxClientId.setModel(m_ClientIds);

    txtServerId.setText(m_Server.getServerSSIDCode());
    txtServerPassword.setText(m_Server.getServerPassword());
    txtServerPort.setText("" + m_Server.getLocalPort());

    setSize(MAIN_FORM_WIDTH, MAIN_FORM_HEIGHT);
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);

  }
  private void initListeners() {
    btnSelectCert.addActionListener(this);
    btnStartServer.addActionListener(this);

    chkUseSSL.addChangeListener(this);
    chkReqSSLClientAuth.addChangeListener(this);

    btnSaveProfile.addActionListener(this);
    btnNewProfile.addActionListener(this);
    btnDeleteProfile.addActionListener(this);
    btnInFolderSelect.addActionListener(this);
    btnOutFolderSelect.addActionListener(this);
    btnRefresh.addActionListener(this);
    btnCreateTestFiles.addActionListener(this);
    btnClientCertSelect.addActionListener(this);

    cbxClientId.addItemListener(this);
  }
  private void initProfileDirectory() {
    try {
      File file0 = new File(OFTPProfile.DEFAULT_PROFILE_ROOT);
      File file1 = new File(OFTPProfile.DEFAULT_PROFILE_DIRECTORY);
      File file2 = new File(OFTPProfile.DEFAULT_PROFILE_INCOMING);
      File file3 = new File(OFTPProfile.DEFAULT_PROFILE_OUTGOING);
      if (!(file0.exists() && file0.isDirectory())) file0.mkdirs();
      if (!(file1.exists() && file1.isDirectory())) file1.mkdirs();
      if (!(file2.exists() && file1.isDirectory())) file2.mkdirs();
      if (!(file3.exists() && file1.isDirectory())) file3.mkdirs();
    } catch (Exception ex) {
      showError(ex);
    }
  }
  private void initProfiles() {
    File file0 = new File(OFTPProfile.DEFAULT_PROFILE_ROOT);
    m_Profiles.clear();
    m_ClientIds.removeAllElements();

    Vector profiles = new Vector();
    File[] files = file0.listFiles();
    for (int i = 0; i < files.length; i++) {
      if (files[i].isFile()) {
        String temp = files[i].getAbsolutePath();
        if (temp.endsWith(".profile")) {
          profiles.add(temp);
        }
      }
    }

    try {
      if (profiles.size() == 0) {
        newProfile(OFTPProfile.DEFAULT_PROFILE_NAME);
      } else {
        for (int i = 0; i < profiles.size(); i++) loadProfileFromFile((String) profiles.get(i));
        cbxClientId.setSelectedIndex(0);
        updateControl((OFTPProfile)m_Profiles.get(0));
      }
    } catch (Exception ex) {
      showError(ex);
    }

  }


  private JTextArea   createMultiLineLable(String text) {
    JTextArea txtAreaDes = new JTextArea();
    txtAreaDes.setText(text);
    txtAreaDes.setEnabled(false);
    txtAreaDes.setDisabledTextColor(txtAreaDes.getForeground());
    txtAreaDes.setLineWrap(true);
    txtAreaDes.setWrapStyleWord(true);
    txtAreaDes.setBackground(this.getBackground());
    return txtAreaDes;
  }
  private JScrollPane createScrollPane(Component component) {
    return new JScrollPane(component, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
  }

  private static void borderLayoutTC(Container container, Component top, Component center) {
    BorderLayout layout = new BorderLayout(COMPONENT_GAP, COMPONENT_GAP);
    container.setLayout(layout);
    container.add(top, BorderLayout.NORTH);
    container.add(center, BorderLayout.CENTER);
  }
  private static void borderLayoutCB(Container container, Component center, Component bottom) {
    BorderLayout layout = new BorderLayout(COMPONENT_GAP, COMPONENT_GAP);
    container.setLayout(layout);
    container.add(center, BorderLayout.CENTER);
    container.add(bottom,  BorderLayout.SOUTH);
  }
  private static void borderLayoutTCB(Container container, Component top, Component center, Component bottom) {
    BorderLayout layout = new BorderLayout(COMPONENT_GAP, COMPONENT_GAP);
    container.setLayout(layout);
    container.add(top, BorderLayout.NORTH);
    container.add(center, BorderLayout.CENTER);
    container.add(bottom, BorderLayout.SOUTH);
  }
  private static void borderLayoutLC(Container container, Component left, Component center) {
    BorderLayout layout = new BorderLayout(COMPONENT_GAP * 4, COMPONENT_GAP * 4);
    container.setLayout(layout);
    container.add(left, BorderLayout.WEST);
    container.add(center,  BorderLayout.CENTER);
  }
  private static void borderLayoutLCR(Container container, Component left, Component center, Component right) {
    BorderLayout layout = new BorderLayout(COMPONENT_GAP * 4, COMPONENT_GAP * 4);
    container.setLayout(layout);
    container.add(left, BorderLayout.WEST);
    container.add(center,  BorderLayout.CENTER);
    container.add(right,  BorderLayout.EAST);
  }
  private static void borderLayoutCR(Container container, Component center, Component right) {
    BorderLayout layout = new BorderLayout(COMPONENT_GAP * 4, COMPONENT_GAP * 4);
    container.setLayout(layout);
    container.add(center,  BorderLayout.CENTER);
    container.add(right,  BorderLayout.EAST);
  }
  private static void borderLayoutFill(Container container, Component fill) {
    BorderLayout layout = new BorderLayout(COMPONENT_GAP, COMPONENT_GAP);
    container.setLayout(layout);
    container.add(fill,  BorderLayout.CENTER);
  }
  private static JPanel gridLayout(int rowCount, int columnCount, Component[] components) {
    JPanel panel = new JPanel();
    GridLayout layout = new GridLayout(rowCount, columnCount);
    layout.setHgap(COMPONENT_GAP);
    layout.setVgap(COMPONENT_GAP);
    panel.setLayout(layout);
    for (int i = 0; i < components.length; i++) {
      panel.add(components[i]);
    }
    return panel;
  }
  private static JPanel layoutCR(Component center, Component right) {
    JPanel panel = new JPanel();
    BorderLayout layout = new BorderLayout(COMPONENT_GAP * 4, COMPONENT_GAP * 4);
    panel.setLayout(layout);
    panel.add(center, BorderLayout.CENTER);
    panel.add(right, BorderLayout.EAST);
    return panel;
  }
  private static JPanel layoutLC(Component left, Component center) {
    JPanel panel = new JPanel();
    BorderLayout layout = new BorderLayout(COMPONENT_GAP * 4, COMPONENT_GAP * 4);
    panel.setLayout(layout);
    panel.add(left, BorderLayout.WEST);
    panel.add(center,  BorderLayout.CENTER);
    return panel;
  }
  private static JPanel layoutTC(Component top, Component center) {
    JPanel panel = new JPanel();
    BorderLayout layout = new BorderLayout(COMPONENT_GAP, COMPONENT_GAP);
    panel.setLayout(layout);
    panel.add(top, BorderLayout.NORTH);
    panel.add(center,  BorderLayout.CENTER);
    return panel;
  }


  private void log(String log) {
    txtAreaLog.append("[" + m_DateFormat.format(new Date()) + "] " + log + "\r\n");
  }
  private void showError(Exception ex) {
    msgBox.setMessageType(JOptionPane.YES_OPTION);
    msgBox.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
  }
  private void showWarning(String msg) {
    msgBox.setMessageType(JOptionPane.YES_OPTION);
    msgBox.showMessageDialog(this, msg, "Warning", JOptionPane.WARNING_MESSAGE);
  }
  private void upateOftpserver() throws InEDIException{
    m_Server.setServerSSIDCode(txtServerId.getText());
    m_Server.setServerPassword(txtServerPassword.getText());
    m_Server.setServerSFIDCode(txtServerId.getText());
    m_Server.setLocalPort(Integer.parseInt(txtServerPort.getText()));
  }
  private OFTPConnection getConnection(String connectionId) {
    return (OFTPConnection)m_Server.getConnections().get(connectionId);
  }


  private String selectFolder() {
    JFileChooser folderChooser = new JFileChooser();
    folderChooser.setCurrentDirectory(new File(OFTPProfile.DEFAULT_PROFILE_ROOT));
    folderChooser.setDialogTitle("Select directory");
    folderChooser.setFileFilter(new FileFilter() {
      public boolean accept(File f) {
        return f.isDirectory();
      }
      public String getDescription() {
        return "Select directory";
      }
    });
    folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    folderChooser.setAcceptAllFileFilterUsed(false);
    if (folderChooser.showDialog(this, "OK") == JFileChooser.APPROVE_OPTION) {
      return folderChooser.getSelectedFile().getAbsolutePath();
    } else {
      return null;
    }
  }
  private void refreshQueuedFiles() {
    m_SendFiles.clear();
    File path = new File(txtOutgoingFolder.getText());
    if (path.isDirectory() && path.exists()) {
      File[] files = path.listFiles();
      for (int i = 0; i < files.length; i++) {
        if (files[i].isFile()) m_SendFiles.addElement(files[i].getAbsolutePath());
      }
    }
  }
  private void newProfile() throws IOException {
    String input = JOptionPane.showInputDialog("Please input the client Id for the new profile.");
    if (input != null && input.length() > 0) {
      if (indexOfProfile(input) >= 0) {
        showWarning("The profile \"" + input + "\" has exist.");
        return;
      }
      newProfile(input);
    }
  }
  private void deleteProfile() {
    String profileName = getCurrentProfile();
    if (profileName.length() == 0) {
        showWarning("Please select a valid profile name.");
        return;
    }
    String msg = "Are you sure you wish to delete the profile for \"" + profileName + "\" ?";
    if (JOptionPane.showConfirmDialog(this, msg, "Delete Confirmation", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
      String profileFullName = getProfileFullName(profileName);
      File file = new File(profileFullName);
      if (file.exists()) file.delete();

      int index = cbxClientId.getSelectedIndex();
      m_Profiles.removeElementAt(index);
      m_ClientIds.removeElementAt(index);

    }
  }
  private void saveProfile() throws IOException {
    String profileName = getCurrentProfile();
    if (profileName.length() == 0) {
        showWarning("Please select a valid profile name.");
        return;
    }

    String profileFullName = getProfileFullName(profileName);
    writeProfileToFile(profileFullName);
    loadProfileFromFile(profileFullName);
    lblSavedAt.setText("Saved at " + m_DateFormat.format(new Date()));
  }

  private void writeFile(String content, String fileFullName) throws IOException {
    FileOutputStream out = null;
    try {
      out = new FileOutputStream(fileFullName);
      out.write(content.getBytes());
    } finally {
      if (out != null) out.close();
    }
  }
  private Hashtable readFile(String fileFullName) throws IOException {
    File file = new File(fileFullName);
    BufferedReader reader = null;
    Hashtable result = new Hashtable();
    try {
      reader = new BufferedReader(new FileReader(file));
      String text = null;
      while ((text = reader.readLine()) != null) {
        String[] temp = text.split(";");
        if (temp.length >= 2) {
          result.put(temp[0], temp[1]);
        }
      }
    } finally {
      if (reader != null) reader.close();
    }
    return result;
  }
  private String getCurrentPath() {
    String currentPath = new File(".").getAbsolutePath();
    if (currentPath.endsWith(".")) currentPath = currentPath.substring(0, currentPath.length() - 1);
    return currentPath;
  }
  private String getProfileFullName(String profileName) {
    return pathCombine(getCurrentPath(), OFTPProfile.DEFAULT_PROFILE_ROOT + profileName + ".profile");
  }
  private void loadProfileFromFile(String fileFullName) throws IOException {
    Hashtable properties = readFile(fileFullName);
    String shortName = getProfileNameWithoutExt(fileFullName);
    OFTPProfile profile = getProfile(shortName);
    boolean profileExist = profile != null;

    if (!profileExist) profile = new OFTPProfile();
    profile.IncomingDir = (String)properties.get("IncomingDir");
    profile.OutgoingDir = (String)properties.get("OutgoingDir");
    profile.Password = (String)properties.get("Password");
    profile.CertificateFile = (String)properties.get("CertificateFile");

    profile.Compress = "true".equalsIgnoreCase((String)properties.get("Compress"));
    profile.SignedReceipt = "true".equalsIgnoreCase((String)properties.get("SignedReceipt"));
    profile.FileSecurity = Integer.parseInt((String)properties.get("FileSecurity"));

    if (!profileExist) {
      m_Profiles.addElement(profile);
      m_ClientIds.addElement(shortName);
    }
  }
  private void updateControl(OFTPProfile profile) {
    txtClientPassword.setText(profile.Password);
    txtIncomingFolder.setText(profile.IncomingDir);
    txtOutgoingFolder.setText(profile.OutgoingDir);
    txtClientCertFile.setText(profile.CertificateFile);
    chkCompress.setSelected(profile.Compress);
    chkReqSignedReceipt.setSelected(profile.SignedReceipt);
    cbxOutgoingSecurity.setSelectedIndex(profile.FileSecurity);
    refreshQueuedFiles();
  }
  private void writeProfileToFile(String profileFullName) throws IOException {
    File file = new File(profileFullName);
    if (file.exists()) file.delete();
    file.createNewFile();

    File incomingFolder = new File(txtIncomingFolder.getText());
    File outgoingFolder = new File(txtOutgoingFolder.getText());
    if (txtIncomingFolder.getText().length() > 0 && !incomingFolder.exists()) incomingFolder.mkdirs();
    if (txtOutgoingFolder.getText().length() > 0 && !outgoingFolder.exists()) outgoingFolder.mkdirs();

    String profileContents = "";
    profileContents += "Password;" + txtClientPassword.getText() + "\r\n";
    profileContents += "IncomingDir;" + txtIncomingFolder.getText() + "\r\n";
    profileContents += "OutgoingDir;" + txtOutgoingFolder.getText() + "\r\n";
    profileContents += "CertificateFile;" + txtClientCertFile.getText() + "\r\n";
    profileContents += "Compress;" + (chkCompress.isSelected() ? "true" : "false") + "\r\n";
    profileContents += "SignedReceipt;" + (chkReqSignedReceipt.isSelected() ? "true" : "false") + "\r\n";
    profileContents += "FileSecurity;" + cbxOutgoingSecurity.getSelectedIndex() + "\r\n";

    writeFile(profileContents, profileFullName);
  }
  private void newProfile(String profileName) throws IOException {
    String profileFullName = getProfileFullName(profileName);
    OFTPProfile profile = new OFTPProfile();
    profile.IncomingDir = profile.IncomingDir.replaceFirst(OFTPProfile.DEFAULT_PROFILE_NAME, profileName);
    profile.OutgoingDir = profile.OutgoingDir.replaceFirst(OFTPProfile.DEFAULT_PROFILE_NAME, profileName);
    updateControl(profile);
    writeProfileToFile(profileFullName);
    loadProfileFromFile(profileFullName);
    cbxClientId.setSelectedIndex(indexOfProfile(profileName));
  }
  private int  indexOfProfile(String profileName) {
    for (int i = 0; i < m_ClientIds.getSize(); i++) {
      if (((String)m_ClientIds.getElementAt(i)).equalsIgnoreCase(profileName)) {
        return i;
      }
    }
    return -1;
  }
  private String pathCombine(String path, String file) {
    path = path.trim();
    if (path.endsWith("\\") || path.endsWith("/")) {
      path = path.substring(0, path.length() - 1);
    }
    return  path + File.separator + file;
  }
  private OFTPProfile getProfile(String profileName) {
    int index = indexOfProfile(profileName);
    return index >= 0 ? (OFTPProfile)m_Profiles.get(index) : null;
  }
  private String getProfileNameWithoutExt(String profileName) {
    int startIndex = profileName.lastIndexOf("/");
    if (startIndex < 0) startIndex = 0;
    int startIndex1 = profileName.lastIndexOf("\\");
    if (startIndex1 < 0) startIndex1 = 0;

    int start = Math.max(startIndex, startIndex1);
    int end = profileName.lastIndexOf(".");
    if (end > start) return profileName.substring(start + 1, end);
    else return profileName.substring(start + 1);

  }
  private String getCurrentProfile() {
    if (cbxClientId.getSelectedIndex() < 0) return "";
    else return this.cbxClientId.getSelectedItem().toString();
  }

  public void acceptConnection(OftpserverAcceptConnectionEvent e) {
    /* This event fires when the client initially connects. Within this event you can validate the client's
       * credentials by inspecting the ClientSSIDCode and ClientPassword parameter
       * and reject the connection if needed */

    try {
      OFTPConnection connection = getConnection(e.connectionId);
      log("Client [" + e.clientSSIDCode + "] connected from " + connection.getRemoteHost());

      //We must set the DownloadDirectory for incoming files here
      //as the client may start sending files immediately after this event completes.
      OFTPProfile tempProfile = getProfile(e.clientSSIDCode);
      if (tempProfile != null) {
        connection.setDownloadDirectory(tempProfile.IncomingDir);

        //If a client certificate is specified, load this here.
        if (tempProfile.CertificateFile != null && tempProfile.CertificateFile.length() > 0)
        {
          connection.setRecipientCertStoreType(Certificate.cstPublicKeyFile);
          connection.setRecipientCertStore(tempProfile.CertificateFile);
          connection.setRecipientCertSubject("*"); //The special value of * picks the first (and in this case only) certificate in the store
        }

        //Reset the flag indicating whether files are sent or not
        tempProfile.SendingFiles = false;
        e.accept = true;
        log("Client [" + e.clientSSIDCode + "] connected from " + connection.getRemoteHost() + " is accepted.");
      } else {
        log("Client [" + e.clientSSIDCode + "] connected from " + connection.getRemoteHost() + " is refused.");
      }

    } catch (Exception ex) {
      showError(ex);
    }
  }
  public void acceptFile(OftpserverAcceptFileEvent e) {
    /* This event fires whenever the client sends a file to the server.
       * By default it will be downloaded to the DownloadDirectory value specified
       * in the OFTPServer.Connections[x].DownloadDirectory field
       * Within this event you may choose to reject the file or override the location of the file */

      //Overwrite the file if it already exists in this demo.
      e.overwrite = true;
  }
  public void connected(OftpserverConnectedEvent e) {
  }
  public void connectionRequest(OftpserverConnectionRequestEvent e) {
  }
  public void disconnected(OftpserverDisconnectedEvent e) {
    log("Client [" + getConnection(e.connectionId).getSSIDCode() + "] disconnected.");
  }
  public void endTransfer(OftpserverEndTransferEvent e) {
    if (e.direction == 0) { //The client is sending a file to the server
      log("Successfully received file \"" + e.virtualFileName + "\" from client \"" + getConnection(e.connectionId).getSSIDCode() + "\"");
    } else {
      log("Successfully sent file \"" + e.virtualFileName + "\" to client \"" + getConnection(e.connectionId).getSSIDCode() + "\"");
    }
  }
  public void error(OftpserverErrorEvent e) {
  }
  public void PITrail(OftpserverPITrailEvent e) {
    //log(e.direction + " " + e.commandDescription);
  }
  public void readyToSend(OftpserverReadyToSendEvent e) {
    /* When this event fires, it means that the connected client is now in a state where it can receive files.
       * We will initiate sending any queued files to the client at this time by spawning a new thread
       * and calling the SendFile method for each of the files to be sent
       * */

      //Only if we're not already sending do we want to start sending files.
      String clientSSIDCode = getConnection(e.connectionId).getSSIDCode();

      OFTPProfile tempProfile = getProfile(clientSSIDCode);
      if (!(tempProfile.SendingFiles))
      {
        tempProfile.SendingFiles = true;
        Thread worker = new Thread(new FileSender(e.connectionId, tempProfile));
        worker.start();
      }
  }

  public void SSLClientAuthentication(OftpserverSSLClientAuthenticationEvent e) {
    /* If SSLAuthenticateClients is set to true, a SSL client certificate will
       * be required when a client connects.
       * Use this event to accept or reject the certficate that is presented.
       * Note that at this point in the connection we do not know the SSID of the client so we
       * must iterate over all acceptable certificates for all profiles and if we find a match,
       * let the connection proceed.
       * */

	  try{
		  for (int i = 0; i < m_ClientIds.getSize(); i++) {
			  OFTPProfile tempProfile = (OFTPProfile) m_Profiles.get(i);
			  if (tempProfile.CertificateFile.length() > 0) {
				  Certificate tempCertficate = new Certificate(tempProfile.CertificateFile);
				  boolean isEqual = true;
				  byte[] cert = tempCertficate.getEncoded();
				  if (cert.length == e.certEncoded.length) {// There is a match, accept the connection
					  for (int j = 0; j < cert.length; j++) {
						  if (cert[j] != e.certEncoded[j]) {
							  isEqual = false;
							  break;
						  }
					  }

				  } else {
					  isEqual = false;
				  }
				  if (isEqual) {
					  e.accept = true;
					  break;
				  }
			  }
		  }
	  }
	  catch(Exception ex)
	  {
		  showError(ex); 
	  }
  }
  public void SSLStatus(OftpserverSSLStatusEvent e) {
    log("SSL Status [" + e.connectionId + "]: " + e.message);
  }
  public void startTransfer(OftpserverStartTransferEvent e) {
    if (e.direction == 0) {//The client is sending a file to the server
      log("Receiving file \"" + e.virtualFileName + "\" from client \"" + getConnection(e.connectionId).getSSIDCode() + "\"");
    } else {
      log("Sending file \"" + e.virtualFileName + "\" to client \"" + getConnection(e.connectionId).getSSIDCode() + "\"");
    }
  }
  public void transfer(OftpserverTransferEvent e) {
  }
  public void certificateReceived(OftpserverCertificateReceivedEvent e){
  }
  public void endResponse(OftpserverEndResponseEvent e){
  }

  public void actionPerformed(ActionEvent e) {
    try {
      if (e.getSource() == btnStartServer) {
        if (btnStartServer.getText().equals("Start")) {
          upateOftpserver();
          m_Server.setListening(true);
          log("Server Has Started.");
          btnStartServer.setText("Stop");
        } else {
          m_Server.shutdown();
          log("Server Has Shutdown.");
          btnStartServer.setText("Start");
        }
      } else if (e.getSource() == btnSelectCert) {
        certSelctor.setVisible(true);

        m_SSLCert = certSelctor.getCertificate();
        boolean hasCert = (m_SSLCert != null);
        chkUseSSL.setEnabled(hasCert);
        chkReqSSLClientAuth.setEnabled(hasCert);
        lblCertInfo.setText(hasCert ? "Subject: " +m_SSLCert.getSubject() : "");
        if (!hasCert) {
          chkUseSSL.setSelected(false);
          chkReqSSLClientAuth.setSelected(false);
        }

        // This certificate is also used for Secure Authentication, Signing, and Decryption operations.
        // This could be a separate certificate, but for simplicity this demo uses the same certificate
        m_Server.setCertificate(m_SSLCert);
      } else if (e.getSource() == btnSaveProfile) {
        saveProfile();
      } else if (e.getSource() == btnNewProfile) {
        newProfile();
      }else if (e.getSource() == btnDeleteProfile) {
        deleteProfile();
      }else if (e.getSource() == btnInFolderSelect) {
        String folder = selectFolder();
        if (folder != null) txtIncomingFolder.setText(folder);
      } else if (e.getSource() == btnOutFolderSelect) {
        String folder = selectFolder();
        if (folder != null) txtOutgoingFolder.setText(folder);
      } else if (e.getSource() == btnClientCertSelect) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File("."));
        fileChooser.setDialogTitle("Select public certificate");
        fileChooser.setFileFilter(new FileFilter() {
          public boolean accept(File f) {
            return f.getName().endsWith(".cer") || f.getName().endsWith(".der") || f.isDirectory();
          }
          public String getDescription() {
            return "Certificate Files (*.cer, *.der)";
          }
        });
        if (fileChooser.showDialog(this, "OK") == JFileChooser.APPROVE_OPTION) {
          txtClientCertFile.setText(fileChooser.getSelectedFile().getAbsolutePath());
        }
      } else if (e.getSource() == btnRefresh) {
        refreshQueuedFiles();
      } else if (e.getSource() == btnCreateTestFiles) {
        for (int i = 0; i < 3; i++) {
          writeFile("This is some test data.", pathCombine(txtOutgoingFolder.getText(), "test" + i + ".txt"));
        }
        refreshQueuedFiles();
      }
    } catch (Exception ex) {
      showError(ex);
    }
  }
  public void stateChanged(ChangeEvent e) {
    try {
      if (e.getSource() == chkUseSSL) {
        m_Server.setUseSSL(chkUseSSL.isSelected());
        m_Server.setSSLCert(m_SSLCert);
        txtServerPort.setText(chkUseSSL.isSelected() ? "6619" : "3305");
      } else if (e.getSource() == chkReqSSLClientAuth) {
        m_Server.setSSLAuthenticateClients(chkReqSSLClientAuth.isSelected());
      }
    } catch (Exception ex) {
      showError(ex);
    }
  }
  public void itemStateChanged(ItemEvent e) {
    if (cbxClientId.getSelectedIndex() >= 0) {
      updateControl(getProfile(getCurrentProfile()));
    } else {
      updateControl(new OFTPProfile());
    }
  }

  protected void processWindowEvent(WindowEvent e) {
    if (e.getID() == WindowEvent.WINDOW_CLOSING) {
      try {
        m_Server.shutdown();
      } catch (Exception ex){}
    }
    super.processWindowEvent(e);
  }

  private class FileSender implements Runnable {
    private String      m_ConnectionId;
    private OFTPProfile m_Profile;
    public FileSender(String connectionId, OFTPProfile profile) {
      m_ConnectionId = connectionId;
      m_Profile = profile;

    }

    public void run() {
      log("Begin run");
      File outFolder = new File(m_Profile.OutgoingDir);
      File[] files = outFolder.listFiles(new java.io.FileFilter() {
        public boolean accept(File pathname) {
          return pathname.isFile();
        }
      });

      OFTPConnection conn = (OFTPConnection)m_Server.getConnections().get(m_ConnectionId);
      try {
        conn.setCompress(m_Profile.Compress);
        conn.setSignedReceipt(m_Profile.SignedReceipt);
        conn.setVirtualFileSecurityLevel(m_Profile.FileSecurity);
      } catch (Exception ex) {
        log("Failed to set connection(" + conn.getSSIDCode() + ") property. (" + ex.getMessage() + ")");
      }

      for (int i = 0; i < files.length; i++) {
        String virtualFileName = files[i].getName();
        try {
          m_Server.sendFile(m_ConnectionId, conn.getSSIDCode(), files[i].getAbsolutePath(), virtualFileName);
        } catch (InEDIException ex) {
          log("Error Sending File \"" + virtualFileName + "\" to client \"" + conn.getSSIDCode() + "\": " + ex.getMessage());
        }
      }

      try {
        log("Ending session with \"" + conn.getSSIDCode() + "\".");
        m_Server.logoff(m_ConnectionId);
      } catch (Exception ex) {
        log("Error ending session with \"" + conn.getSSIDCode() + "\": " + ex.getMessage());
      }
    }

  }
  private class OFTPProfile {
    public String IncomingDir;
    public String OutgoingDir;
    public String Password;
    public String CertificateFile;
    public Boolean SendingFiles;
    public Boolean Compress;
    public Boolean SignedReceipt;
    public int FileSecurity;

    public static final String DEFAULT_PROFILE_NAME       = "CLIENTSSID";
    public static final String DEFAULT_PROFILE_ROOT       = ".\\profiles\\";
    public static final String DEFAULT_PROFILE_DIRECTORY  = DEFAULT_PROFILE_ROOT + DEFAULT_PROFILE_NAME;
    public static final String DEFAULT_PROFILE_PASSWORD   = "PASSWORD";
    public static final String DEFAULT_PROFILE_INCOMING   = DEFAULT_PROFILE_DIRECTORY + "\\Incoming";
    public static final String DEFAULT_PROFILE_OUTGOING   = DEFAULT_PROFILE_DIRECTORY + "\\Outgoing";

    public OFTPProfile() {
      IncomingDir     = DEFAULT_PROFILE_INCOMING;
      OutgoingDir     = DEFAULT_PROFILE_OUTGOING;
      Password        = DEFAULT_PROFILE_PASSWORD;
      CertificateFile = "";
      SendingFiles    = false;
      Compress        = false;
      SignedReceipt   = false;
      FileSecurity    = 0;
    }
  }
  private class JGroupBox extends JPanel {
    private TitledBorder m_TitledBorder;
    public JGroupBox()
    {
      super();
      m_TitledBorder = BorderFactory.createTitledBorder("GroupBox");
      this.setBorder(m_TitledBorder);
    }
    public JGroupBox(String title)
    {
      super();
      m_TitledBorder = BorderFactory.createTitledBorder(title);
      this.setBorder(m_TitledBorder);
    }
    public String getTitle() {
      return m_TitledBorder.getTitle();
    }
    public void setTitle(String title) {
      m_TitledBorder.setTitle(title);
    }
  }
  private class JCertSelector extends JDialog implements ActionListener, ListSelectionListener {
    private JTextField        txtFile             = new JTextField(".\\oftpserver.pfx");
    private JPasswordField    passPassword        = new JPasswordField("test");
    private JButton           btnFileSelector     = new JButton("Browse");
    private JButton           btnLoad             = new JButton("Load");
    private JGroupBox         gpLoadFromFile      = new JGroupBox("Load a certificate from file");
    private JGroupBox         gpCerts             = new JGroupBox("Select a Certificate");
    private JList             lstCerts            = new JList();
    private JButton           btnOk               = new JButton("OK");
    private JButton           btnCancel           = new JButton("Cancel");
    private JLabel            lblNotify           = new JLabel();

    private Certmgr           m_Certmgr           = new Certmgr();
    private DefaultListModel  m_CertsModel        = new DefaultListModel();

    public JCertSelector(JFrame owner) {
      super (owner, "Please select a private key certificate", true);
      
      initCertmgr();
      initComponents();
    }
    private void initComponents() {
      setSize(450, 550);
      lstCerts.setModel(m_CertsModel);
      btnOk.setEnabled(false);

      // Load from file group
      borderLayoutLCR(
          gpLoadFromFile,
          gridLayout(2, 1, new Component[]{new JLabel("File:"), new JLabel("Password:")}),
          gridLayout(2, 1, new Component[]{txtFile, passPassword}),
          gridLayout(2, 1, new Component[]{btnFileSelector, btnLoad}));
      gpLoadFromFile.setBounds(0, 0, 100, 80);

      borderLayoutCB(gpCerts, createScrollPane(lstCerts), lblNotify);

      borderLayoutTCB(
          getContentPane(),
          gpLoadFromFile,
          gpCerts,
          gridLayout(1, 5, new Component[]{new JLabel(), new JLabel(), new JLabel(), btnCancel, btnOk})
      );

      btnFileSelector.addActionListener(this);
      btnLoad.addActionListener(this);
      btnCancel.addActionListener(this);
      btnOk.addActionListener(this);
      lstCerts.addListSelectionListener(this);
    }
    private void initCertmgr() {

      try {
        m_Certmgr.addCertmgrEventListener(new CertmgrEventListener() {
          public void certChain(CertmgrCertChainEvent e) {
          }
          public void certList(CertmgrCertListEvent e) {
            m_CertsModel.addElement(e.certSubject);
          }
          public void error(CertmgrErrorEvent e) {
          }
          public void keyList(CertmgrKeyListEvent e) {
          }
          public void storeList(CertmgrStoreListEvent e) {
          }
        });
      } catch (Exception ex) {
        showError(ex);
      }
    }
    
    public Certificate getCertificate() {
      return m_Certmgr.getCert();
    }
    public void actionPerformed(ActionEvent e) {
      try {
        if (e.getSource() == btnFileSelector) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setCurrentDirectory(new File("."));
            fileChooser.setDialogTitle("Select private certificate");
            fileChooser.setFileFilter(new FileFilter() {
              public boolean accept(File f) {
                return f.getName().endsWith(".pfx") || f.getName().endsWith(".p12") || f.isDirectory();
              }
              public String getDescription() {
                return "PKCS12 files (*.pfx, *.p12)";
              }
            });
            if (fileChooser.showDialog(this, "OK") == JFileChooser.APPROVE_OPTION) {
              txtFile.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        } else if (e.getSource() == btnLoad) {
          if (txtFile.getText().length() == 0) {
            showWarning("You must select a certificate first.");
            return;
          }
          m_Certmgr.setCertStoreType(Certmgr.cstPFXFile);
          m_Certmgr.setCertStore(txtFile.getText());
          m_Certmgr.setCertStorePassword(new String(passPassword.getPassword()));
          m_CertsModel.clear();
          m_Certmgr.listStoreCertificates();
          if (m_CertsModel.getSize() > 0) {
            lstCerts.setSelectedIndex(0);
          }
        } else if (e.getSource() == btnCancel) {
          setVisible(false);
        } else if (e.getSource() == btnOk) {
          setVisible(false);
        }
      } catch (Exception ex) {
        showError(ex);
      }
    }
    public void valueChanged(ListSelectionEvent e) {

      try {
        if (e.getSource() == lstCerts) {
          if (m_CertsModel.getSize() == 0) {
            lblNotify.setText("");
            m_Certmgr.setCert(null);
            btnOk.setEnabled(true);
          } else {
            m_Certmgr.setCert(new Certificate(
                m_Certmgr.getCertStoreType(),
                m_Certmgr.getCertStore(),
                m_Certmgr.getCertStorePassword(),
                m_CertsModel.get(e.getFirstIndex()).toString()));

            if (!m_Certmgr.getCert().getPrivateKeyAvailable()) {
              lblNotify.setText("The selected certificate has no private key associated with it.");
              btnOk.setEnabled(false);
            } else {
              lblNotify.setText("");
              btnOk.setEnabled(true);
            }
          }
        }
      } catch (Exception ex) {
        showError(ex);
      }
    }
  }
}









