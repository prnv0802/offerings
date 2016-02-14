import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ftpprogressbar extends JFrame {
  private static final long serialVersionUID = -2164037910292790620L;
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JProgressBar progressProgressBar = new JProgressBar();
  JLabel progressLabel = new JLabel();
  JButton cancelButton = new JButton();

  public ftpprogressbar() {
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    this.getContentPane().setLayout(gridBagLayout1);
    this.setSize(new Dimension(290, 115));
    this.setTitle("Transfer Progress");
    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    Dimension frameSize = this.getSize();
    if (frameSize.height > screenSize.height)
      frameSize.height = screenSize.height;
    if (frameSize.width > screenSize.width)
      frameSize.width = screenSize.width;
    this.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    progressLabel.setText("Transfer Progress:");
    cancelButton.setMaximumSize(new Dimension(75, 23));
    cancelButton.setMinimumSize(new Dimension(75, 23));
    cancelButton.setPreferredSize(new Dimension(75, 23));
    cancelButton.setMargin(new Insets(1, 1, 1, 1));
    cancelButton.setText("Cancel");
    cancelButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {cancelButton();}
    });
    this.getContentPane().add(progressProgressBar, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(2, 20, 0, 10), 0, 0));
    this.getContentPane().add(progressLabel, new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0
            ,GridBagConstraints.SOUTHWEST, GridBagConstraints.NONE, new Insets(0, 20, 2, 10), 0, 0));
    this.getContentPane().add(cancelButton, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 15, 15), 0, 0));
  }

  void cancelButton() {}

  void setMax(int max) {
    progressProgressBar.setMaximum(max);
    progressProgressBar.setValue(0);
  }

  void setProgress(int value) {
    progressProgressBar.setValue(value);
  }
}
