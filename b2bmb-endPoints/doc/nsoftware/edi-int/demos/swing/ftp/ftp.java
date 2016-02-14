import inedi.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Vector;

public class ftp extends JFrame {
  private static final long serialVersionUID = 2679018617704062326L;

  //The control
  Ftps ftps1 = new Ftps();

  Vector remoteFiles = new Vector();
  Vector localFiles = new Vector();
  int fileSize;
  boolean fileTransfer = false;
  boolean connected = false;

  //the various forms
  ftploginform lf = new ftploginform() {
    private static final long serialVersionUID = -6005621848147294112L;
    void ok(){login(true);}
    void cancel(){login(false);}
  };
  ftpprogressbar ftppb;

  //two ftppanels with functions overwritten to correspond to local or remote site
  ftppanel panelLocal = new ftppanel() {
    private static final long serialVersionUID = 1L;
    public void mkDir() {mkDirLocal();}
    public void chgDir() {chgDirLocal();}
    public void delete(String fileName) {deleteLocal(fileName);}
    public void rename(String fileName) {renameLocal(fileName);}
    public void refresh() {refreshLocal();}
    public void getFile(String fileName) {getFileLocal(fileName);}
  };
  ftppanel panelRemote = new ftppanel() {
    private static final long serialVersionUID = 1L;
    public void mkDir() {mkDirRemote();}
    public void chgDir() {chgDirRemote();}
    public void delete(String fileName) {deleteRemote(fileName);}
    public void rename(String fileName) {renameRemote(fileName);}
    public void refresh() {refreshRemote();}
    public void getFile(String fileName) {getFileRemote(fileName);}
  };

  JPanel contentPane;
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JButton get = new JButton();
  JButton send = new JButton();
  JScrollPane piPane = new JScrollPane();
  JTextArea pitrail = new JTextArea();
  Border border1;
  JPanel buttons = new JPanel();
  GridBagLayout gridBagLayout2 = new GridBagLayout();
  JRadioButton ascii = new JRadioButton();
  JRadioButton binary = new JRadioButton();
  JRadioButton auto = new JRadioButton();
  ButtonGroup transferMode = new ButtonGroup();
  JPanel jPanel1 = new JPanel();
  JButton connect = new JButton();
  JButton cancel = new JButton();
  JButton exit = new JButton();
  GridBagLayout gridBagLayout3 = new GridBagLayout();
  boolean packFrame = false;

  /**Construct the frame*/
  public ftp(){
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try  {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
    //Validate frames that have preset sizes
    //Pack frames that have useful preferred size info, e.g. from their layout
    if (packFrame) {
      pack();
    } else  {
      validate();
    }
  }

  /**Component initialization*/
  private void jbInit() throws Exception  {
    //setIconImage(Toolkit.getDefaultToolkit().createImage(Frame1.class.getResource("[Your Icon]")));
    contentPane = (JPanel) this.getContentPane();
    border1 = BorderFactory.createEmptyBorder(5,5,5,5);
    contentPane.setLayout(gridBagLayout1);
    this.setSize(new Dimension(700, 444));
    this.setTitle("Secure Ftp Demo");
    panelLocal.setBorder(BorderFactory.createCompoundBorder(
        new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(151, 145, 140))," Local System "),
        BorderFactory.createEmptyBorder(2,2,2,2)));
    panelRemote.setBorder(BorderFactory.createCompoundBorder(
        new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(151, 145, 140))," Remote System "),
        BorderFactory.createEmptyBorder(2,2,2,2)));
    buttons.setBorder(BorderFactory.createCompoundBorder(
        new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(151, 145, 140))," Transfer Mode "),
        BorderFactory.createEmptyBorder(0,0,0,0)));
    get.setFont(new java.awt.Font("Monospaced", 0, 12));
    get.setMaximumSize(new Dimension(40, 25));
    get.setMinimumSize(new Dimension(40, 25));
    get.setPreferredSize(new Dimension(40, 25));
    get.setMargin(new Insets(1, 1, 1, 1));
    get.setText(" <- ");
    get.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        get_actionPerformed(e);
      }
    });
    send.setFont(new java.awt.Font("Monospaced", 0, 12));
    send.setMaximumSize(new Dimension(40, 25));
    send.setMinimumSize(new Dimension(40, 25));
    send.setPreferredSize(new Dimension(40, 25));
    send.setMargin(new Insets(1, 1, 1, 1));
    send.setText(" -> ");
    send.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        send_actionPerformed(e);
      }
    });
    contentPane.setBorder(border1);
    buttons.setLayout(gridBagLayout2);
    ascii.setPreferredSize(new Dimension(60, 25));
    ascii.setMinimumSize(new Dimension(60, 25));
    ascii.setText("ASCII");
    ascii.setHorizontalAlignment(SwingConstants.CENTER);
    ascii.setMaximumSize(new Dimension(60, 25));
    ascii.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ascii_actionPerformed(e);
      }
    });
    binary.setPreferredSize(new Dimension(60, 25));
    binary.setMinimumSize(new Dimension(60, 25));
    binary.setText("Binary");
    binary.setHorizontalAlignment(SwingConstants.CENTER);
    binary.setMaximumSize(new Dimension(60, 25));
    binary.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        binary_actionPerformed(e);
      }
    });
    auto.setPreferredSize(new Dimension(60, 25));
    auto.setMinimumSize(new Dimension(60, 25));
    auto.setSelected(true);
    auto.setText("Auto");
    auto.setHorizontalAlignment(SwingConstants.CENTER);
    auto.setMaximumSize(new Dimension(60, 25));
    auto.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        auto_actionPerformed(e);
      }
    });
    piPane.setMaximumSize(new Dimension(32767, 100));
    piPane.setMinimumSize(new Dimension(24, 100));
    piPane.setPreferredSize(new Dimension(4, 100));
    pitrail.setEditable(false);
    connect.setMaximumSize(new Dimension(80, 23));
    connect.setMinimumSize(new Dimension(80, 23));
    connect.setPreferredSize(new Dimension(80, 23));
    connect.setMargin(new Insets(1, 1, 1, 1));
    connect.setText("Connect");
    connect.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        connect_actionPerformed(e);
      }
    });
    cancel.setMaximumSize(new Dimension(80, 23));
    cancel.setMinimumSize(new Dimension(80, 23));
    cancel.setPreferredSize(new Dimension(80, 23));
    cancel.setMargin(new Insets(1, 1, 1, 1));
    cancel.setText("Cancel");
    cancel.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancel_actionPerformed(e);
      }
    });
    exit.setMaximumSize(new Dimension(80, 23));
    exit.setMinimumSize(new Dimension(80, 23));
    exit.setPreferredSize(new Dimension(80, 23));
    exit.setMargin(new Insets(1, 1, 1, 1));
    exit.setText("Exit");
    exit.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        exit_actionPerformed(e);
      }
    });
    jPanel1.setLayout(gridBagLayout3);
    ftps1.addFtpsEventListener(new inedi.FtpsEventListener() {
      public void SSLStatus(FtpsSSLStatusEvent e) {
        System.out.println(e.message);
      }
      public void SSLServerAuthentication(FtpsSSLServerAuthenticationEvent e) {
        ftps1_SSLServerAuthentication(e);
      }
      public void PITrail(FtpsPITrailEvent e) {
        ftps1_PITrail(e);
      }
      public void dirList(FtpsDirListEvent e) {
        ftps1_dirList(e);
      }
      public void endTransfer(FtpsEndTransferEvent e) {
        ftps1_endTransfer(e);
      }
      public void error(FtpsErrorEvent e) {
      }
      public void startTransfer(FtpsStartTransferEvent e) {
        ftps1_startTransfer(e);
      }
      public void transfer(FtpsTransferEvent e) {
        ftps1_transfer(e);
      }
      public void connectionStatus(FtpsConnectionStatusEvent e) {
      }
    });
    contentPane.add(panelLocal, new GridBagConstraints(0, 0, 2, 2, 1.0, 1.0
            ,GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    contentPane.add(panelRemote, new GridBagConstraints(3, 0, 1, 2, 1.0, 1.0
            ,GridBagConstraints.EAST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    contentPane.add(buttons, new GridBagConstraints(0, 2, 4, 1, 0.0, 0.0
            ,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(2, 2, 2, 0), 0, 0));
    buttons.add(ascii, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 2, 0, 2), 0, 0));
    buttons.add(binary, new GridBagConstraints(2, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 2, 0, 2), 0, 0));
    buttons.add(auto, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 2, 0, 2), 0, 0));
    contentPane.add(get, new GridBagConstraints(2, 0, 1, 1, 0.0, 1.0
            ,GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(0, 2, 2, 2), 0, 0));
    contentPane.add(send, new GridBagConstraints(2, 1, 1, 1, 0.0, 1.0
            ,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(2, 2, 0, 2), 0, 0));
    contentPane.add(piPane, new GridBagConstraints(0, 3, 4, 1, 1.0, 0.0
            ,GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(2, 2, 2, 0), 0, 0));
    contentPane.add(jPanel1, new GridBagConstraints(1, 4, 3, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 5, 2, 2), 0, 0));
    jPanel1.add(connect, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
    jPanel1.add(cancel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
    jPanel1.add(exit, new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 10, 0, 0), 0, 0));
    piPane.getViewport().add(pitrail, null);
    transferMode.add(ascii);
    transferMode.add(binary);
    transferMode.add(auto);

    //set the file seperators
    //for local files, it's whatever is defined by the local system
    panelLocal.setSeperator(File.separatorChar);
    //for the remote system (the host), Ftps's standard is a front-slash
    panelRemote.setSeperator('/');
    panelRemote.setEnabled(false);
    //set the local directory
    panelLocal.setDirectory(System.getProperties().getProperty("user.dir"));
    refreshLocal();
  }

  /**Main method*/
  public static void main(String[] args) {
	new ftp().setVisible(true);
  }

  /**Overridden so we can exit when window is closed*/
  protected void processWindowEvent(WindowEvent e) {
    super.processWindowEvent(e);
    if (e.getID() == WindowEvent.WINDOW_CLOSING)
      die();
  }
  private void die() {
    lf.setVisible(false);
    this.setVisible(false);
    lf.dispose();
    dispose();
    System.exit(0);
  }

  //======================= File Transfer Operations =======================
  //Set the transfer mode
  void auto_actionPerformed(ActionEvent e) {
    try{
      ftps1.setTransferMode(Ftps.tmDefault);
    } catch (InEDIException ipwe) {
      JOptionPane.showMessageDialog(null, ipwe.getMessage());
    }
  }
  void binary_actionPerformed(ActionEvent e) {
    try{
      ftps1.setTransferMode(Ftps.tmBinary);
    } catch (InEDIException ipwe) {
      JOptionPane.showMessageDialog(null, ipwe.getMessage());
    }
  }
  void ascii_actionPerformed(ActionEvent e) {
    try{
      ftps1.setTransferMode(Ftps.tmASCII);
    } catch (InEDIException ipwe) {
      JOptionPane.showMessageDialog(null, ipwe.getMessage());
    }
  }

  //Download File
  void get_actionPerformed(ActionEvent e) {get(panelRemote.getSelected());}
  private void get(String fileName) {
    if (fileName.startsWith("<DIR>"))
      pitrail.append("Cannot download directories.\n");
    else  {
      int index = panelRemote.getSelectedIndex();
      if (index >= 0) {
        try{
          fileSize = (int)((FtpsEntry) remoteFiles.get(index)).myEvent.fileSize;
          ftps1.setRemoteFile(fileName);
          ftps1.setLocalFile(panelLocal.getDir() + fileName);
          fileTransfer = true;
          ftps1.download();
          ftps1.setRemoteFile("");
          refreshLocal();
        } catch (InEDIException ipwe) {
          JOptionPane.showMessageDialog(null, ipwe.getMessage());
        }
      }
    }
  }

  //Upload File
  void send_actionPerformed(ActionEvent e) {send(panelLocal.getSelected());}
  private void send(String fileName) {
    if (fileName.startsWith("<DIR>"))
      pitrail.append("Cannot upload directories.\n");
    else{
      try{
        File upload = new File(panelLocal.getDir() + fileName);
        fileSize = (int) upload.length();
        ftps1.setLocalFile(panelLocal.getDir() + fileName);
        ftps1.setRemoteFile(fileName);
        fileTransfer = true;
        ftps1.upload();
        ftps1.setRemoteFile("");
        refreshRemote();
      } catch (InEDIException ipwe) {
        JOptionPane.showMessageDialog(null, ipwe.getMessage());
      }
    }
  }

  //======================= Local Operations =======================
  //Make a local directory
  void mkDirLocal() {
    String retVal = JOptionPane.showInputDialog(this, "Enter new directory:");
    if (retVal != null) {
      File newDir = new File(panelLocal.getDir() + retVal);
      if (newDir.exists())
        JOptionPane.showMessageDialog(null, "File already exists.");
      else
        if(!newDir.mkdir())
          JOptionPane.showMessageDialog(null, "Could not create directory.");
      refreshLocal();
    }
  }

  //Change local directory
  void chgDirLocal() {
    String retVal = JOptionPane.showInputDialog(this, "Enter new directory:");
    if (retVal != null) {
      File newDir = new File(panelLocal.getDir() + retVal);
      if (!newDir.exists())
        JOptionPane.showMessageDialog(null, "Directory does not exist.");
      else {
        try {
          panelLocal.setDirectory(newDir.getCanonicalPath());
          refreshLocal();
        } catch (java.io.IOException ioe) {
          JOptionPane.showMessageDialog(null, "Could not not access directory.");
        }
      }
    }
  }

  //Delete local file
  void deleteLocal(String fileName) {
    //This code is highly dangerous (deletes -indiscriminately-)
    if (fileName.startsWith("<DIR>"))
      fileName = fileName.substring(6);
    File delFile = new File(panelLocal.getDir() + fileName);
    if (!delFile.delete())
      JOptionPane.showMessageDialog(null, "Cannot delete file.");
    refreshLocal();
  }

  //Rename local file
  void renameLocal(String fileName) {
    String retVal = JOptionPane.showInputDialog(this, "Enter new file name:");
    if (retVal != null) {
      File newFile = new File(panelLocal.getDir() + retVal);
      if (newFile.exists())
        JOptionPane.showMessageDialog(null, "Filename is already in use.");
      else {
        File oldFile = new File(panelLocal.getDir() + panelLocal.getSelected());
        if(!oldFile.renameTo(newFile))
          JOptionPane.showMessageDialog(null, "Could not rename file.");
      }
      refreshLocal();
    }
  }

  //Refresh local directory listing
  void refreshLocal() {
    File dir = new File(panelLocal.getDir());
    if (dir.exists() && dir.isDirectory()) {
      //reset the list
      localFiles.removeAllElements();
      panelLocal.setList(localFiles);

      //add an object for the current and parent directories
      localFiles.add("<DIR> .");
      localFiles.add("<DIR> ..");

      //get all files in the current directory and add them
      //to the list with "<DIR> " in front of directory names
      File[] fileList = dir.listFiles();
      for (int i = 0; i < fileList.length; i++) {
        if (fileList[i].isDirectory())
          localFiles.add("<DIR> "+ fileList[i].getName());
        else localFiles.add(fileList[i].getName());
      }
      //set the list data
      panelLocal.setList(localFiles);
    }
    else{
      //There is enough error checking that this should never happen... but...
      JOptionPane.showMessageDialog(null, "The local path is not a directory.");
    }
  }

  //Get local file information
  void getFileLocal(String fileName) {
    //if there's no selection index, fileName is null, so ignore it
    if (fileName != null) {
      //if the file is a directory, change to that directory
      if (fileName.startsWith("<DIR>")) {
        File dir = new File(panelLocal.getDir() + fileName.substring(6));
        if (dir.exists() && dir.isDirectory())
          try{
            panelLocal.setDirectory(dir.getCanonicalPath());
            refreshLocal();
          } catch (java.io.IOException ioe) {
            JOptionPane.showMessageDialog(null, "Could not access directory.");
          }
        else JOptionPane.showMessageDialog(null, "Target is not a directory.");
      }
      //otherwise, upload the file
      else send(fileName);
    }
  }

  //======================= Remote Operations =======================
  //Make a remote directory
  void mkDirRemote() {
    String retVal = JOptionPane.showInputDialog(this, "Enter new directory:");
    if (retVal != null) {
      try{
        ftps1.makeDirectory(retVal);
        refreshRemote();
      } catch (InEDIException ipwe) {
        JOptionPane.showMessageDialog(null, ipwe.getMessage());
      }
    }
  }

  //Change remote directory
  void chgDirRemote() {
    String retVal = JOptionPane.showInputDialog(this, "Enter new directory:");
    if (retVal != null) {
      try{
        ftps1.setRemotePath(retVal);
        panelRemote.setDirectory(ftps1.getRemotePath());
        refreshRemote();
      } catch (InEDIException ipwe) {
        JOptionPane.showMessageDialog(null, ipwe.getMessage());
      }
    }
  }

  //Delete remote file
  void deleteRemote(String fileName) {
    try{
      if (fileName.startsWith("<DIR>")) fileName = fileName.substring(6);
      ftps1.deleteFile(fileName);
      ftps1.setRemoteFile("");
      refreshRemote();
    } catch (InEDIException ipwe) {
      JOptionPane.showMessageDialog(null, ipwe.getMessage());
    }
  }

  //Rename remote file
  void renameRemote(String fileName) {
    String retVal = JOptionPane.showInputDialog(this, "Enter new file name:");
    if (retVal != null) {
      try{
        ftps1.setRemoteFile(panelRemote.getSelected());
        ftps1.renameFile(retVal);
        refreshRemote();
      } catch (InEDIException ipwe) {
        JOptionPane.showMessageDialog(null, ipwe.getMessage());
      }
    }
  }

  //Refresh remote directory listing
  void refreshRemote() {
    try  {
      remoteFiles.removeAllElements();
      panelRemote.setList(remoteFiles);
      ftps1.setRemoteFile("");
      ftps1.listDirectoryLong();
      panelRemote.setList(remoteFiles);
    } catch (InEDIException ipwe) {
      JOptionPane.showMessageDialog(null, ipwe.getMessage());
    }
  }

  //Get remote file information
  void getFileRemote(String fileName) {
    //if there's no selection index, fileName is null, so ignore it
    if (fileName != null) {
      //if the file is a directory, change to that directory
      if (fileName.startsWith("<DIR>"))
        try  {
          ftps1.setRemotePath(fileName.substring(6));
          panelRemote.setDirectory(ftps1.getRemotePath());
          refreshRemote();
        } catch (InEDIException ipwe) {
          JOptionPane.showMessageDialog(null, ipwe.getMessage());
        }
      //otherwise, download the file
      else get(fileName);
    }
  }

  //======================= Connection and Cancellation Handling =======================
  //Connection events
  void connect_actionPerformed(ActionEvent e) {
    //if you're not already connected
    if (!connected) {
      //resize and position lf so that it is centered and visible
      Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
      Dimension frameSize = lf.getSize();
      if (frameSize.height > screenSize.height)
        frameSize.height = screenSize.height;
      if (frameSize.width > screenSize.width)
        frameSize.width = screenSize.width;

      //disable actions in this window and set lf visible
      this.setEnabled(false);
      lf.setVisible(true);
    }
    else  {
      //if you are connected, disconnect
      try{
        ftps1.logoff();
        connected = false;
      } catch (InEDIException ipwe) {
      }
      connect.setText("Connect");
    }
  }
  void login(boolean logon) {
    //set lf invisible and reenable actions in this window
    this.setEnabled(true);
    lf.setVisible(false);

    //if you're actually logging in...
    if (logon) {
      try{
        //set the control's values and logon
        ftps1.setTimeout(10);
        ftps1.logoff();
        ftps1.setRemoteHost(lf.hostTextField.getText());
        ftps1.setUser(lf.userTextField.getText());
        ftps1.setPassword(lf.passwordPasswordField.getText());
        // set SSLStartMode
        String item = (String) lf.sslmodeComboBox.getSelectedItem();
        int defaultPortNumber = 0;
        if ( item.equals(lf.strExplicit ))
        {
          ftps1.setSSLStartMode(ftps1.sslExplicit);
          defaultPortNumber = 21;
        }
        else if ( item.equals(lf.strImplicit) )
        {
          ftps1.setSSLStartMode(ftps1.sslImplicit);
          defaultPortNumber = 990;
        }
        else
        {
          ftps1.setSSLStartMode(ftps1.sslNone);
          defaultPortNumber = 21;
        }
        // set port number
        if ( lf.portTextField.getText().length() > 0 )
        {
          ftps1.setRemotePort(Integer.valueOf(lf.portTextField.getText()).intValue());
        }
        else
        {
          lf.portTextField.setText(Integer.toString(defaultPortNumber));
          ftps1.setRemotePort(defaultPortNumber);
        }

        ftps1.logon();
        connect.setText("Disconnect");
        connected = true;

        //get the directory and it's files
        panelRemote.setEnabled(true);
        panelRemote.setDirectory(ftps1.getRemotePath());
        refreshRemote();
      } catch (InEDIException ipwe) {
        //if there's a problem, show it
        JOptionPane.showMessageDialog(null, ipwe.getMessage());
      }
    }
  }

  //Cancel actions
  void cancel_actionPerformed(ActionEvent e) {}

  //Exit the client
  void exit_actionPerformed(ActionEvent e) {
    die();
  }

  //======================= Ftps Control Operations =======================
  /* Dir list (we only use long listings in this demo)
   * To differentiate between a normal a long list, use ftps1.getAction() and
   * Ftps.ftpListDirectory and Ftps.ftpListDirectoryLong.
   * Filenames are stored in 'dirEntry' under normal listing.
   */
  void ftps1_dirList(FtpsDirListEvent e) {
    remoteFiles.add(new FtpsEntry(e));
  }

  /* Use this function to begin file transfer handling */
  void ftps1_startTransfer(FtpsStartTransferEvent e) {
    if (fileTransfer) {
      ftppb = new ftpprogressbar() {
        private static final long serialVersionUID = 1227514076979403903L;
      };
      ftppb.setMax(fileSize);
      this.setEnabled(false);
      ftppb.setVisible(true);
    }
  }
  void ftppbDie() {
    try{
      ftps1.abort();
    } catch (InEDIException ipwe) {
    }
    this.setEnabled(true);
    ftppb.setVisible(false);
  }

  /* Keep track of messages coming in through the protocol interface (PI) */
  void ftps1_PITrail(FtpsPITrailEvent e) {
    System.out.println(e.message);
    pitrail.append(e.message + "\n");
    pitrail.setCaretPosition(pitrail.getText().length());
  }

  /* Use this function to keep track of transfer information
   * such as bytes transfered.
   */
  void ftps1_transfer(FtpsTransferEvent e) {
    if (fileTransfer)
      ftppb.setProgress((int)(100 * ((long)e.bytesTransferred / fileSize)));
  }

  /* Use this functon to clean up after file transfers */
  void ftps1_endTransfer(FtpsEndTransferEvent e) {
    if (fileTransfer) {
      this.setEnabled(true);
      ftppb.setVisible(false);
      fileTransfer = false;
    }
  }

  class FtpsEntry {
    FtpsDirListEvent myEvent;
    FtpsEntry(FtpsDirListEvent anEvent){myEvent = anEvent;}
    public String toString(){return (myEvent.isDir ? "<DIR> " : "") + myEvent.fileName;}
  }

  void ftps1_SSLServerAuthentication(FtpsSSLServerAuthenticationEvent e) {
    e.accept = JOptionPane.showConfirmDialog(this,
    e.status + "\n\n" + e.certIssuer + "\n" + e.certSubject + "\n\n" + "Do you accept it?",
    "Security information", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION;
  }
}






