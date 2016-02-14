import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

public class ftploginform extends JFrame {
  private static final long serialVersionUID = 3972220575326719703L;
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JLabel hostLabel = new JLabel();
  JLabel portLabel = new JLabel();
  JLabel userLabel = new JLabel();
  JLabel passwordLabel = new JLabel();
  JLabel sslmodeLabel = new JLabel();
  JTextField hostTextField = new JTextField();
  JTextField userTextField = new JTextField();
  JTextField portTextField = new JTextField();
//  JComboBox sslmodeComboBox = new JComboBox(new Object[] {"sslAuto","sslImplicit","sslExplicit","sslNone"});
  public static final String strExplicit = "AUTH TLS (port 21)";
  public static final String strImplicit = "Implicit (port 990)";
  public static final String strNone = "None (port 21)";
//  JComboBox sslmodeComboBox = new JComboBox(new Object[] {"AUTH TLS (port 21)", "Implicit (port 990)", "None (port 21)"});
  JComboBox sslmodeComboBox = new JComboBox(new Object[] {strExplicit, strImplicit, strNone});
  JButton okButton = new JButton();
  JButton cancelButton = new JButton();
  JPasswordField passwordPasswordField = new JPasswordField();

  public ftploginform() {
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.setSize(new Dimension(335, 184));
    hostLabel.setText("Host Name / Address:");
    this.setTitle("Login");
    this.setResizable(false);
    this.getContentPane().setLayout(gridBagLayout1);
    portLabel.setText("Port:");
    userLabel.setText("User:");
    passwordLabel.setText("Password:");
    sslmodeLabel.setText("SSLStartMode:");
    okButton.setMaximumSize(new Dimension(80, 23));
    okButton.setMinimumSize(new Dimension(80, 23));
    okButton.setPreferredSize(new Dimension(80, 23));
    okButton.setMargin(new Insets(1, 1, 1, 1));
    okButton.setText("OK");
    okButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        ok();
      }
    });
    cancelButton.setMaximumSize(new Dimension(80, 23));
    cancelButton.setMinimumSize(new Dimension(80, 23));
    cancelButton.setPreferredSize(new Dimension(80, 23));
    cancelButton.setMargin(new Insets(1, 1, 1, 1));
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancel();
      }
    });
    portTextField.setText("21");
    sslmodeComboBox.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        sslmodeComboBox_itemStateChanged(e);
      }
    });
    this.getContentPane().add(hostLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 20, 2, 0), 0, 0));
    this.getContentPane().add(hostTextField, new GridBagConstraints(1, 0, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 2, 20), 0, 0));

    this.getContentPane().add(sslmodeLabel, new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 20, 2, 0), 0, 0));
    this.getContentPane().add(sslmodeComboBox, new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 10, 2, 20), 0, 0));

    this.getContentPane().add(portLabel, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 20, 2, 0), 0, 0));
    this.getContentPane().add(portTextField, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 10, 2, 20), 0, 0));

    this.getContentPane().add(userLabel, new GridBagConstraints(0, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 20, 2, 0), 0, 0));
    this.getContentPane().add(userTextField, new GridBagConstraints(1, 3, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 10, 2, 20), 0, 0));

    this.getContentPane().add(passwordLabel, new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 20, 2, 0), 0, 0));
    this.getContentPane().add(passwordPasswordField, new GridBagConstraints(1, 4, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(2, 10, 2, 20), 0, 0));

    this.getContentPane().add(okButton, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 30, 2, 0), 0, 0));
    this.getContentPane().add(cancelButton, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(2, 10, 2, 0), 0, 0));
  }

  void cancel() {}

  void ok() {}

  public void sslmodeComboBox_itemStateChanged(ItemEvent e) {
    String selectedItem = e.getItem().toString();
    if ( selectedItem.equals(strImplicit) )
    {
      portTextField.setText("990");
    }
    else
    {
      portTextField.setText("21");
    }
  }
}
