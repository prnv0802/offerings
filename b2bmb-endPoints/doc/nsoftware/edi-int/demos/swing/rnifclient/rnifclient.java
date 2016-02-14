import inedi.Certificate;
import inedi.Certmgr;
import inedi.InEDIException;
import inedi.Rnifsender;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;

public class rnifclient extends JFrame {
  protected static final int STORE_TYPE_USER_JKSFILE = 0;
  protected static final int STORE_TYPE_MACHINE_JKSBLOB = 1;
  protected static final int STORE_TYPE_PFXFILE = 2;
  protected static final int STORE_TYPE_PFXBLOB = 3;

  GridBagLayout gblClient = new GridBagLayout();
  JTextArea jtaURL = new JTextArea();
  JLabel jlblURL = new JLabel();
  JCheckBox jckSigned = new JCheckBox();
  JCheckBox jckEncrypted = new JCheckBox();
  JButton jbUpload = new JButton();
  Rnifsender rnifSender1 = new Rnifsender();
  JTextArea jtaPIP = new JTextArea();
  JScrollPane jspPIP = new JScrollPane();
  JScrollPane jspResponse = new JScrollPane();
  JTextArea jtaResponse = new JTextArea();
  JTextArea jtaNote = new JTextArea();
  JFileChooser jfcStoreChooser = new JFileChooser();
  certmanager certmanager = new certmanager();
  private javax.swing.filechooser.FileFilter encryptFilter = new javax.swing.filechooser.FileFilter() {
      public String getDescription() { return "Certificates (*.cer; *.pem)"; }
      public boolean accept(File f) {
        return f.isDirectory() || f.getName().endsWith(".cer") || f.getName().endsWith(".pem");
      }
  };

  public rnifclient() {
    try {
      jbInit();
    } catch (Exception exception) {
      exception.printStackTrace();
    }
  }

  public static void main(String[] args) {
    try {
      UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
    } catch (Exception e) {
    }
    (new rnifclient()).setVisible(true);
  }

  private void jbInit() throws Exception {
    this.setSize(new Dimension(630, 600));
    getContentPane().setLayout(gblClient);
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    this.setTitle("Simple RosettaNet Demo");

    jlblURL.setMaximumSize(new Dimension(80, 18));
    jlblURL.setMinimumSize(new Dimension(80, 18));
    jlblURL.setPreferredSize(new Dimension(80, 18));
    jlblURL.setText("URL:");
    jtaURL.setMaximumSize(new Dimension(80, 17));
    jtaURL.setMinimumSize(new Dimension(80, 17));
    jtaURL.setPreferredSize(new Dimension(80, 17));
    jtaURL.setText("http://localhost:8080/WebModule1/rnifserver.jsp");
    jckSigned.setMaximumSize(new Dimension(90, 23));
    jckSigned.setMinimumSize(new Dimension(90, 23));
    jckSigned.setPreferredSize(new Dimension(90, 23));
    jckSigned.setText("Signed...");
    jckSigned.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jckSigned_actionPerformed(e);
      }
    });
    jckEncrypted.setMaximumSize(new Dimension(90, 23));
    jckEncrypted.setMinimumSize(new Dimension(90, 23));
    jckEncrypted.setPreferredSize(new Dimension(90, 23));
    jckEncrypted.setText("Encrypted...");
    jckEncrypted.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jckEncrypted_actionPerformed(e);
      }
    });
    jbUpload.setMaximumSize(new Dimension(80, 23));
    jbUpload.setMinimumSize(new Dimension(80, 23));
    jbUpload.setPreferredSize(new Dimension(80, 23));
    jbUpload.setText("Upload");
    jbUpload.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        jButton1_actionPerformed(e);
      }
    });
    jtaPIP.setToolTipText("");
    jtaPIP.setLineWrap(false);
    jtaResponse.setBorder(null);
    jtaResponse.setLineWrap(true);
    jtaNote.setBackground(SystemColor.control);
    jtaNote.setForeground(SystemColor.textHighlight);
    jtaNote.setBorder(null);
    jtaNote.setDebugGraphicsOptions(0);
    jtaNote.setDisabledTextColor(new Color(172, 168, 153));
    jtaNote.setEditable(false);
    jtaNote.setText(
        "To use this demo, first select any security features you wish to " +
        "use and enter the URL for the receiving entity. Then paste the data " +
        "you wish to send (a default PIP has been supplied in the demo folder) " +
        "and click \"Upload\".");
    jtaNote.setLineWrap(true);
    jtaNote.setWrapStyleWord(true);
    jspPIP.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createEtchedBorder(
        Color.white, new Color(165, 163, 151)), "Partner Interface Process (PIP)"), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
    jspPIP.setMaximumSize(new Dimension(5, 5));
    jspPIP.setMinimumSize(new Dimension(5, 5));
    jspPIP.setPreferredSize(new Dimension(5, 5));
    jspResponse.setBorder(BorderFactory.createCompoundBorder(new TitledBorder(BorderFactory.createEtchedBorder(
        Color.white, new Color(165, 163, 151)), "Server Response"), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
    jspResponse.setMaximumSize(new Dimension(5, 5));
    jspResponse.setMinimumSize(new Dimension(5, 5));
    jspResponse.setPreferredSize(new Dimension(5, 5));
    certmanager.setModal(true);
    jspPIP.getViewport().add(jtaPIP);
    jspResponse.getViewport().add(jtaResponse);
    this.getContentPane().add(jlblURL, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 2, 0), 0, 0));
    this.getContentPane().add(jtaURL, new GridBagConstraints(1, 1, 3, 1, 1.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 2, 5), 0, 0));
    this.getContentPane().add(jbUpload, new GridBagConstraints(3, 2, 1, 1, 1.0, 0.0,
        GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(2, 0, 2, 5), 0, 0));
    this.getContentPane().add(jspPIP, new GridBagConstraints(0, 3, 4, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 5, 2, 5), 0, 0));
    this.getContentPane().add(jspResponse, new GridBagConstraints(0, 5, 4, 1, 1.0, 1.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(2, 5, 5, 5), 0, 0));
    this.getContentPane().add(jtaNote, new GridBagConstraints(0, 0, 4, 1, 1.0, 0.0,
        GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
    this.getContentPane().add(jckSigned, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 2, 0), 0, 0));
    this.getContentPane().add(jckEncrypted, new GridBagConstraints(2, 2, 1, 1, 0.0, 0.0,
        GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 5, 2, 0), 0, 0));
  }

  public void jButton1_actionPerformed(ActionEvent e) {
    jtaResponse.setText("");
    try {
      rnifSender1.setStandardName("RosettaNet");
      rnifSender1.setStandardVersion("V02.00");
      rnifSender1.setActionMessage(true);
      rnifSender1.setActionCode("Advance Shipment Notification");
      rnifSender1.setSignalMessage(false);
      rnifSender1.setGlobalUsageCode(Rnifsender.gucProduction);
      rnifSender1.setPIPCode("3B2");
      rnifSender1.setPIPInstanceId("11234567");
      rnifSender1.setPIPVersion("1.2");
      rnifSender1.setPartnerKnown(true);
      rnifSender1.setPartnerId("000002122");
      rnifSender1.setStandardName("RosettaNet");
      rnifSender1.setStandardVersion("V02.00");
      rnifSender1.setSecureTransportRequired(true);
      rnifSender1.setMessageDateTime("20001121T145200.000Z");
      rnifSender1.setMessageReceiverId("12334566");
      rnifSender1.setMessageSenderId("000000002");
      rnifSender1.setFromRole("Buyer");
      rnifSender1.setFromService("Buyer Service");
      rnifSender1.setReplyMessage(false);
      rnifSender1.setToRole("Seller");
      rnifSender1.setToService("Seller Service");
      rnifSender1.setEncryptionType(Rnifsender.etNoEncryption);
      rnifSender1.setMessageTrackingId("" + System.currentTimeMillis());
      rnifSender1.setServiceContent(new String(jtaPIP.getText()));
      rnifSender1.setResponseType(Rnifsender.rtSync);
      rnifSender1.setURL(jtaURL.getText());
      rnifSender1.post();
      jtaResponse.setText(rnifSender1.getReplyHeaders() + "\r\n" + new String(rnifSender1.getReplyData()));
    } catch (InEDIException ex) {
      JOptionPane.showMessageDialog(this, "Error (" + ex.getCode() + "): " + ex.getMessage());
    }
  }

  public void jckSigned_actionPerformed(ActionEvent e) {
    try {
      if (jckSigned.isSelected() && certmanager.showDialog(false) == certmanager.ACCEPTED) {
        Certmgr certmgr = certmanager.getCertmgr();
        rnifSender1.setCertificate(certmgr.getCert());
      } else {
        jckSigned.setSelected(false);
      }
    } catch (InEDIException ex) {
      JOptionPane.showMessageDialog(this, "Error (" + ex.getCode() + "): " + ex.getMessage());
    }
  }

  public void jckEncrypted_actionPerformed(ActionEvent e) {
      try
      {
              jfcStoreChooser.setCurrentDirectory(new File("."));
              jfcStoreChooser.setDialogTitle("Select Certificate");
              jfcStoreChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
              jfcStoreChooser.setFileFilter(encryptFilter);
              if (jckEncrypted.isSelected() && jfcStoreChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
              {
                  java.io.File f = new java.io.File(jfcStoreChooser.getSelectedFile().getAbsolutePath());
                  FileInputStream file_input = new FileInputStream(
                          jfcStoreChooser.getSelectedFile().getAbsolutePath());
                  DataInputStream data_in = new DataInputStream(file_input);
                  byte[] cert_content = new byte[(int)f.length()];
                  data_in.readFully(cert_content);
                  rnifSender1.setRecipientCert(new Certificate(cert_content));
                  rnifSender1.setEncryptionType(Rnifsender.etEncryptServiceContent);
              }
              else
              {
                  jckEncrypted.setSelected(false);
                  rnifSender1.setEncryptionType(Rnifsender.etNoEncryption);
              }

      }catch (InEDIException ex)
      {
          JOptionPane.showMessageDialog(this, "Error (" + ex.getCode() + "): " + ex.getMessage());
      }
      catch (Exception ex)
      {
          System.out.println(ex.getMessage());
      }
  }
}








