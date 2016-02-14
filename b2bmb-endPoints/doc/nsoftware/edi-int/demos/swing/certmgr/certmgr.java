import inedi.*;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

public class certmgr extends JFrame {

  private static final long serialVersionUID = 8584385477975253115L;
  Certmgr certmgr1 = new Certmgr();
  JLabel jLabel4 = new JLabel();
  JTextField textFieldStoreFile = new JTextField();
  JLabel jLabel2 = new JLabel();
  JButton jbBrowse = new JButton();
  JLabel jLabel1 = new JLabel();
  JComboBox cbStoreType = new JComboBox();
  JTextField passwordField = new JTextField();
  JLabel jLabel3 = new JLabel();
  DefaultListModel dlmCerts = new DefaultListModel();
  JList jbCerts = new JList(dlmCerts);
  JLabel jLabel5 = new JLabel();
  JButton bLoad = new JButton();
  TitledBorder titledBorder1;
  JTextArea tpCertInfo = new JTextArea();
  //JButton jButton1 = new JButton();

  public certmgr(){
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
  }
  public static void main(String[] args) {
    (new certmgr()).setVisible(true);
  }
  private void jbInit() throws Exception {
    titledBorder1 = new TitledBorder("");
    this.setLocale(java.util.Locale.getDefault());
    this.setResizable(true);
    this.setSize(new Dimension(453, 416));
    this.setTitle("certmgr demo");
    jLabel3.setBounds(new Rectangle(11, 61, 119, 13));
    jLabel3.setText("Store password:");
    passwordField.setBounds(new Rectangle(11, 80, 189, 21));
    passwordField.setText("password");
    passwordField.setAlignmentX((float) 0.0);
    cbStoreType.setBackground(Color.white);
    cbStoreType.setAlignmentY((float) 0.5);
    cbStoreType.setAutoscrolls(false);
    cbStoreType.setDebugGraphicsOptions(0);
    cbStoreType.setDoubleBuffered(false);
    cbStoreType.setActionCommand("comboBoxChanged");
    cbStoreType.setEditable(false);
    cbStoreType.setSelectedIndex(-1);
    cbStoreType.setBounds(new Rectangle(209, 80, 133, 21));
    jLabel1.setBounds(new Rectangle(8, 120, 169, 20));
    jLabel1.setText("Certificates in Store:");
    jbBrowse.setText("Browse");
    jbBrowse.addActionListener(new certmgr_jbBrowse_actionAdapter(this));
    jbBrowse.setBounds(new Rectangle(347, 32, 89, 21));
    jLabel2.setBounds(new Rectangle(9, 15, 185, 12));
    jLabel2.setText("Certificate Store path:");
    textFieldStoreFile.setBounds(new Rectangle(9, 32, 332, 21));
    textFieldStoreFile.setText("myidentities.jks");
    textFieldStoreFile.setAlignmentX((float) 0.0);
    jLabel4.setBounds(new Rectangle(209, 64, 111, 15));
    jLabel4.setText("Store type:");
    this.addWindowListener(new certmgr_this_windowAdapter(this));
    this.getContentPane().setLayout(null);
    certmgr1.addCertmgrEventListener(new certMgr_certmgr1_certmgrEventAdapter(this));
    jbCerts.setToolTipText("");
    jbCerts.setVerifyInputWhenFocusTarget(true);
    jbCerts.setSelectionForeground(Color.white);
    jbCerts.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    jbCerts.setBounds(new Rectangle(8, 142, 174, 235));
    jbCerts.addMouseListener(new certmgr_jbCerts_mouseAdapter(this));
    jLabel5.setRequestFocusEnabled(true);
    jLabel5.setText("Certificate attributes:");
    jLabel5.setBounds(new Rectangle(195, 118, 176, 20));
    bLoad.setBounds(new Rectangle(348, 80, 89, 21));
    bLoad.setActionCommand("Load Store");
    bLoad.setText("Load");
    bLoad.addActionListener(new certmgr_bLoad_actionAdapter(this));
    tpCertInfo.setText("");
    tpCertInfo.setLineWrap(true);
    tpCertInfo.setBounds(new Rectangle(195, 143, 240, 235));
    this.getContentPane().add(jLabel2, null);
    this.getContentPane().add(textFieldStoreFile, null);
    this.getContentPane().add(jLabel1, null);
    this.getContentPane().add(jbCerts, null);
    this.getContentPane().add(jLabel3, null);
    this.getContentPane().add(passwordField, null);
    this.getContentPane().add(cbStoreType, null);
    this.getContentPane().add(jLabel4, null);
    this.getContentPane().add(jLabel5, null);
    this.getContentPane().add(jbBrowse, null);
    this.getContentPane().add(bLoad, null);
    this.getContentPane().add(tpCertInfo, null);
  }

  void jbBrowse_actionPerformed(ActionEvent e) {
      JFileChooser jfc = new JFileChooser(textFieldStoreFile.getText());
      jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
      jfc.setDialogTitle("Select Store");
      if (jfc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        textFieldStoreFile.setText(jfc.getSelectedFile().toString());
      }
  }

  void certmgr1_certList(CertmgrCertListEvent e) {
    dlmCerts.addElement(e.certSubject);
  }

  void this_windowActivated(WindowEvent e) {

  }
  void updateEntries(){
    dlmCerts.clear();
    try
    {
      if(cbStoreType.getSelectedItem().equals("JKS Store"))
        certmgr1.setCertStoreType(Certmgr.cstJKSFile);
      else
        certmgr1.setCertStoreType(Certmgr.cstPFXFile);
      certmgr1.setCertStorePassword(passwordField.getText());
      certmgr1.setCertStore(textFieldStoreFile.getText());
      certmgr1.listStoreCertificates();
    } catch (InEDIException ipwe) {
      JOptionPane.showMessageDialog(this,"code="+String.valueOf(ipwe.getCode())+"; msg=\""+ipwe.getMessage()+"\"","IPWorksSSLException", JOptionPane.ERROR_MESSAGE);
    } catch(Exception ee) {
      JOptionPane.showMessageDialog(this,ee.getMessage(),"Exception",JOptionPane.ERROR_MESSAGE);
    }
  }

  void bLoad_actionPerformed(ActionEvent e) {
    tpCertInfo.setText("");
    updateEntries();
  }

  void jbCerts_mouseClicked(MouseEvent e) {
    try
    {
      tpCertInfo.setText("");
      if (cbStoreType.getSelectedItem().equals("JKS Store"))
        // JKS Store
        certmgr1.setCert( new Certificate(
            Certificate.cstJKSFile,
            textFieldStoreFile.getText(),
            passwordField.getText(),
            jbCerts.getSelectedValue().toString()));
      else
        // PFX Store
        certmgr1.setCert( new Certificate(
            Certificate.cstPFXFile,
            textFieldStoreFile.getText(),
            passwordField.getText(),
            jbCerts.getSelectedValue().toString()));

      tpCertInfo.append("Issuer: " + certmgr1.getCert().getIssuer() + "\r\n");
      tpCertInfo.append("Subject: " + certmgr1.getCert().getSubject() + "\r\n");
      tpCertInfo.append("Public Key: " + certmgr1.getCert().getPublicKey() + "\r\n");
      if (certmgr1.getCert().getPrivateKeyAvailable())
        tpCertInfo.append("You have a private key corresponding to this certificate");
    } catch (InEDIException ipwe) {
      JOptionPane.showMessageDialog(this,"code="+String.valueOf(ipwe.getCode())+"; msg=\""+ipwe.getMessage()+"\"","IPWorksSSLException", JOptionPane.ERROR_MESSAGE);
    } catch(Exception ee) {
      JOptionPane.showMessageDialog(this,ee.getMessage(),"Exception",JOptionPane.ERROR_MESSAGE);
    }
  }

  void this_windowOpened(WindowEvent e) {
    cbStoreType.addItem("JKS Store");
    cbStoreType.addItem("PFX Store");
  }
}

class certMgr_jButton1_actionAdapter implements java.awt.event.ActionListener {
  certmgr adaptee;

  certMgr_jButton1_actionAdapter(certmgr adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {

  }
}

class certMgr_certmgr1_certmgrEventAdapter implements inedi.CertmgrEventListener {
  certmgr adaptee;

  certMgr_certmgr1_certmgrEventAdapter(certmgr adaptee) {
    this.adaptee = adaptee;
  }
  public void certChain(CertmgrCertChainEvent e) {
  }
  public void certList(CertmgrCertListEvent e) {
    adaptee.certmgr1_certList(e);
  }
  public void error(CertmgrErrorEvent e) {
  }
  public void storeList(CertmgrStoreListEvent e) {
  }
  public void keyList(CertmgrKeyListEvent e) {
  }
}

class certmgr_this_windowAdapter extends java.awt.event.WindowAdapter {
  certmgr adaptee;

  certmgr_this_windowAdapter(certmgr adaptee) {
    this.adaptee = adaptee;
  }
  public void windowActivated(WindowEvent e) {
    adaptee.this_windowActivated(e);
  }
  public void windowOpened(WindowEvent e) {
    adaptee.this_windowOpened(e);
  }
}

class certmgr_jbBrowse_actionAdapter implements java.awt.event.ActionListener {
  certmgr adaptee;

  certmgr_jbBrowse_actionAdapter(certmgr adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.jbBrowse_actionPerformed(e);
  }
}

class certmgr_bLoad_actionAdapter implements java.awt.event.ActionListener {
  certmgr adaptee;

  certmgr_bLoad_actionAdapter(certmgr adaptee) {
    this.adaptee = adaptee;
  }
  public void actionPerformed(ActionEvent e) {
    adaptee.bLoad_actionPerformed(e);
  }
}

class certmgr_jbCerts_mouseAdapter extends java.awt.event.MouseAdapter {
  certmgr adaptee;

  certmgr_jbCerts_mouseAdapter(certmgr adaptee) {
    this.adaptee = adaptee;
  }
  public void mouseClicked(MouseEvent e) {
    adaptee.jbCerts_mouseClicked(e);
  }
}



