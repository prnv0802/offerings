import inedi.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

public class certmanager extends JDialog {
  private Certmgr certmgr1 = new Certmgr();
  private GridBagLayout gblCertmgr = new GridBagLayout();
  private JLabel jlblStore = new JLabel();
  private JLabel jlblPassword = new JLabel();
  private JButton jbBrowse = new JButton();
  private JPasswordField jpfPassword = new JPasswordField();
  private JButton jbLoad = new JButton();
  private DefaultListModel dlmCerts = new DefaultListModel();
  private JList jlCerts = new JList();
  private JTextArea jtaCertInfo = new JTextArea();
  private JFileChooser jfcStoreChooser = new JFileChooser();
  private JLabel jlblStoreType = new JLabel();
  private JComboBox jcbStoreType = new JComboBox();
  private JTextField jtfCertStore = new JTextField();
  private JLabel jlblCerts = new JLabel();
  private JLabel jlblCertInfo = new JLabel();
  private JButton jbCancel = new JButton();
  private JButton jbOK = new JButton();

  private javax.swing.filechooser.FileFilter encryptFilter = new javax.swing.filechooser.FileFilter() {
      public String getDescription() { return "Certificates (*.cer; *.pem)"; }
      public boolean accept(File f) {
        return f.isDirectory() || f.getName().endsWith(".cer") || f.getName().endsWith(".pem");
      }
  };
  private javax.swing.filechooser.FileFilter signFilter = new javax.swing.filechooser.FileFilter() {
      public String getDescription() { return "Certificate Stores (*.jks; *.pfx)"; }
      public boolean accept(File f) {
        return f.isDirectory() || f.getName().endsWith(".jks") || f.getName().endsWith(".pfx");
      }
  };

  public certmanager(){
    try {
      jbInit();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setSize(new Dimension(580, 465));
    getContentPane().setLayout(gblCertmgr);
    this.setDefaultCloseOperation(HIDE_ON_CLOSE);
    this.setTitle("Certificate Manager");
    jfcStoreChooser.setCurrentDirectory(new File("."));
    jfcStoreChooser.setDialogTitle("Select Store");
    jfcStoreChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    jlblStore.setMaximumSize(new Dimension(80, 15));
    jlblStore.setMinimumSize(new Dimension(80, 15));
    jlblStore.setPreferredSize(new Dimension(80, 15));
    jlblStore.setText("Cert Store:");
    jlblPassword.setMaximumSize(new Dimension(80, 15));
    jlblPassword.setMinimumSize(new Dimension(80, 15));
    jlblPassword.setPreferredSize(new Dimension(80, 15));
    jlblPassword.setText("Password:");
    jbBrowse.setMaximumSize(new Dimension(80, 23));
    jbBrowse.setMinimumSize(new Dimension(80, 23));
    jbBrowse.setPreferredSize(new Dimension(80, 23));
    jbBrowse.setText("Browse");
    jbBrowse.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jbBrowse_actionPerformed(e);
      }
    });
    jpfPassword.setText("test");
    jbLoad.setMaximumSize(new Dimension(80, 23));
    jbLoad.setMinimumSize(new Dimension(80, 23));
    jbLoad.setPreferredSize(new Dimension(80, 23));
    jbLoad.setText("Load");
    jbLoad.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jbLoad_actionPerformed(e);
      }
    });
    jtaCertInfo.setMaximumSize(new Dimension(5, 5));
    jtaCertInfo.setMinimumSize(new Dimension(5, 5));
    jtaCertInfo.setPreferredSize(new Dimension(5, 5));
    jtaCertInfo.setEditable(false);
    jtaCertInfo.setText("");
    jtaCertInfo.setLineWrap(true);
    jtaCertInfo.setWrapStyleWord(true);
    jlCerts.setMaximumSize(new Dimension(10, 40));
    jlCerts.setMinimumSize(new Dimension(10, 40));
    jlCerts.setPreferredSize(new Dimension(5, 5));
    jlCerts.setModel(dlmCerts);
    jlCerts.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    jlCerts.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        jlCerts_mouseClicked(e);
      }
    });
    certmgr1.addCertmgrEventListener(new CertmgrEventListener() {
      public void certList(CertmgrCertListEvent e) {
        certmgr1_certList(e);
      }
      public void certChain(CertmgrCertChainEvent e) { }
      public void error(CertmgrErrorEvent e) { }
      public void keyList(CertmgrKeyListEvent e) { }
      public void storeList(CertmgrStoreListEvent e) { }
    });
    jlblStoreType.setMaximumSize(new Dimension(80, 15));
    jlblStoreType.setMinimumSize(new Dimension(80, 15));
    jlblStoreType.setPreferredSize(new Dimension(80, 15));
    jlblStoreType.setText("Store Type:");
    jcbStoreType.setMaximumSize(new Dimension(100, 19));
    jcbStoreType.setMinimumSize(new Dimension(100, 19));
    jcbStoreType.setPreferredSize(new Dimension(100, 19));
    jcbStoreType.addItem("JKS Store");
    jcbStoreType.addItem("PFX Store");
    jcbStoreType.setSelectedIndex(1);
    jtfCertStore.setText("RNIFSender.pfx");
    jlblCerts.setMaximumSize(new Dimension(80, 15));
    jlblCerts.setMinimumSize(new Dimension(80, 15));
    jlblCerts.setPreferredSize(new Dimension(80, 15));
    jlblCerts.setText("Certificates:");
    jlblCertInfo.setMaximumSize(new Dimension(120, 15));
    jlblCertInfo.setMinimumSize(new Dimension(120, 15));
    jlblCertInfo.setText("Certificate Attributes:");
    jbCancel.setMaximumSize(new Dimension(80, 23));
    jbCancel.setMinimumSize(new Dimension(80, 23));
    jbCancel.setPreferredSize(new Dimension(80, 23));
    jbCancel.setText("Cancel");
    jbCancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jbCancel_actionPerformed(e);
      }
    });
    jbOK.setText("OK");
    jbOK.setMaximumSize(new Dimension(80, 23));
    jbOK.setMinimumSize(new Dimension(80, 23));
    jbOK.setPreferredSize(new Dimension(80, 23));
    jbOK.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jbOK_actionPerformed(e);
      }
    });
    this.getContentPane().add(jlCerts, new GridBagConstraints(0, 4, 2, 2, 0.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 5, 5, 0), 0, 0));
    this.getContentPane().add(jtaCertInfo, new GridBagConstraints(2, 4, 2, 1, 0.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 5, 0, 5), 0, 0));
    this.getContentPane().add(jlblCertInfo, new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 2, 0), 0, 0));
    this.getContentPane().add(jlblCerts, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 2, 0), 0, 0));
    this.getContentPane().add(jbBrowse, new GridBagConstraints(3, 0, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 2, 5), 0, 0));
    this.getContentPane().add(jbLoad, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 10, 2, 5), 0, 0));
    this.getContentPane().add(jlblPassword, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 2, 0), 0, 0));
    this.getContentPane().add(jlblStore, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 2, 0), 0, 0));
    this.getContentPane().add(jlblStoreType, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 2, 0), 0, 0));
    this.getContentPane().add(jcbStoreType, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 2, 0), 0, 0));
    this.getContentPane().add(jpfPassword, new GridBagConstraints(1, 1, 2, 1, 1.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 5, 2, 0), 0, 0));
    this.getContentPane().add(jtfCertStore, new GridBagConstraints(1, 0, 2, 1, 1.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 2, 0), 0, 0));
    this.getContentPane().add(jbOK, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0,
        GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 10, 5, 0), 0, 0));
    this.getContentPane().add(jbCancel, new GridBagConstraints(3, 5, 1, 1, 0.0, 0.0,
        GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 10, 5, 5), 0, 0));
  }

  public void jbBrowse_actionPerformed(ActionEvent e) {
    if (jfcStoreChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
      jtfCertStore.setText(jfcStoreChooser.getSelectedFile().getAbsolutePath());
  }

  public void jbLoad_actionPerformed(ActionEvent e) {
    dlmCerts.clear();
    try {
      if (jcbStoreType.getSelectedItem().equals("JKS Store"))
        certmgr1.setCertStoreType(Certificate.cstUser);
      else
        certmgr1.setCertStoreType(Certificate.cstPFXFile);
      certmgr1.setCertStore(jtfCertStore.getText());
      certmgr1.setCertStorePassword(new String(jpfPassword.getPassword()));
      certmgr1.listStoreCertificates();
    } catch (InEDIException ex) {
      JOptionPane.showMessageDialog(this, "Error (" + ex.getCode() + "): " + ex.getMessage());
    }
  }

  public void certmgr1_certList(CertmgrCertListEvent e) {
    dlmCerts.addElement(e.certSubject);
  }

  public void jlCerts_mouseClicked(MouseEvent e) {
    if (jlCerts.getSelectedValue() == null)
      return;
    try {
      jtaCertInfo.setText("");
      certmgr1.setCert(new Certificate(certmgr1.getCertStoreType(),certmgr1.getCertStore(),certmgr1.getCertStorePassword(),jlCerts.getSelectedValue().toString()));
      jtaCertInfo.append("Issuer: " + certmgr1.getCert().getIssuer() + "\r\n");
      jtaCertInfo.append("Subject: " + certmgr1.getCert().getSubject() + "\r\n");
      jtaCertInfo.append("Public Key: " + certmgr1.getCert().getPublicKey() + "\r\n");
      if (certmgr1.getCert().getPrivateKeyAvailable())
        jtaCertInfo.append("You have a private key corresponding to this certificate");
    } catch (InEDIException ex) {
      JOptionPane.showMessageDialog(this, "Error (" + ex.getCode() + "): " + ex.getMessage());
    }
  }

  public Certmgr getCertmgr() {
    return certmgr1;
  }

  public static final int ACCEPTED = 0;
  public static final int CANCELED = 1;
  private int state = CANCELED;

  public int showDialog(boolean encrypting) {
    jfcStoreChooser.setFileFilter(encrypting ? encryptFilter : signFilter);
    show();
    return state;
  }

  public void jbOK_actionPerformed(ActionEvent e) {
    state = ACCEPTED;
    this.hide();
  }

  public void jbCancel_actionPerformed(ActionEvent e) {
    state = CANCELED;
    this.hide();
  }
}
