import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.util.Vector;

public class ftppanel extends JPanel {
  private static final long serialVersionUID = -1187482121582386394L;
  char sep = ' ';
  JButton mkDirButton = new JButton();
  JButton chgDirButton = new JButton();
  JButton deleteButton = new JButton();
  JButton renameButton = new JButton();
  JButton refreshButton = new JButton();
  JComboBox directoryComboBox = new JComboBox();
  GridBagLayout gridBagLayout1 = new GridBagLayout();
  JScrollPane listScrollPane = new JScrollPane();
  JList listList = new JList();

  public ftppanel() {
    try {
      jbInit();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }

  private void jbInit() throws Exception {
    deleteButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        delete(listList.getSelectedValue().toString());
      }
    });
    deleteButton.setText("Delete");
    deleteButton.setMargin(new Insets(1, 1, 1, 1));
    deleteButton.setPreferredSize(new Dimension(60, 25));
    deleteButton.setMinimumSize(new Dimension(60, 25));
    deleteButton.setMaximumSize(new Dimension(60, 25));
    deleteButton.setFont(new java.awt.Font("Dialog", 0, 11));
    chgDirButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        chgDir();
      }
    });
    chgDirButton.setText("ChgDir");
    chgDirButton.setMnemonic('0');
    chgDirButton.setMargin(new Insets(1, 1, 1, 1));
    chgDirButton.setPreferredSize(new Dimension(60, 25));
    chgDirButton.setMinimumSize(new Dimension(60, 25));
    chgDirButton.setMaximumSize(new Dimension(60, 25));
    chgDirButton.setFont(new java.awt.Font("Dialog", 0, 11));
    mkDirButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mkDir();
      }
    });
    mkDirButton.setText("MkDir");
    mkDirButton.setMargin(new Insets(1, 1, 1, 1));
    mkDirButton.setPreferredSize(new Dimension(60, 25));
    mkDirButton.setMinimumSize(new Dimension(60, 25));
    mkDirButton.setMaximumSize(new Dimension(60, 25));
    mkDirButton.setFont(new java.awt.Font("Dialog", 0, 11));
    renameButton.setFont(new java.awt.Font("Dialog", 0, 11));
    renameButton.setMaximumSize(new Dimension(60, 25));
    renameButton.setMinimumSize(new Dimension(60, 25));
    renameButton.setPreferredSize(new Dimension(60, 25));
    renameButton.setMargin(new Insets(1, 1, 1, 1));
    renameButton.setText("Rename");
    renameButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (listList.getSelectedIndex() > -1)
          rename(listList.getSelectedValue().toString());
      }
    });
    refreshButton.setFont(new java.awt.Font("Dialog", 0, 11));
    refreshButton.setMaximumSize(new Dimension(60, 25));
    refreshButton.setMinimumSize(new Dimension(60, 25));
    refreshButton.setPreferredSize(new Dimension(60, 25));
    refreshButton.setMargin(new Insets(1, 1, 1, 1));
    refreshButton.setText("Refresh");
    refreshButton.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        refresh();
      }
    });
    this.setLayout(gridBagLayout1);
    listList.addMouseListener(new java.awt.event.MouseAdapter() {
      public void mouseReleased(MouseEvent e) {
        if (e.getClickCount() == 2 && listList.getSelectedIndex() > -1)
          getFile(listList.getSelectedValue().toString());
      }
    });
    directoryComboBox.setMaximumSize(new Dimension(10, 21));
    directoryComboBox.setMinimumSize(new Dimension(10, 21));
    directoryComboBox.setPreferredSize(new Dimension(10, 21));
    directoryComboBox.addActionListener(new java.awt.event.ActionListener() {
      public void actionPerformed(ActionEvent e) {
        directoryComboBox_actionPerformed(e);
      }
    });
    listScrollPane.setMaximumSize(new Dimension(10, 10));
    listScrollPane.setMinimumSize(new Dimension(10, 10));
    listScrollPane.setPreferredSize(new Dimension(10, 10));
    this.add(directoryComboBox, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 3, 0), 0, 0));
    this.add(chgDirButton, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(mkDirButton, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(renameButton, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0
            ,GridBagConstraints.SOUTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(deleteButton, new GridBagConstraints(1, 4, 1, 1, 0.0, 0.0
            ,GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(refreshButton, new GridBagConstraints(1, 5, 1, 1, 0.0, 0.0
            ,GridBagConstraints.NORTH, GridBagConstraints.HORIZONTAL, new Insets(0, 0, 0, 0), 0, 0));
    this.add(listScrollPane, new GridBagConstraints(0, 1, 1, 5, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    listScrollPane.getViewport().add(listList, null);
  }
  public void setEnabled(boolean enabled) {
    directoryComboBox.setEnabled(enabled);
    listList.setEnabled(enabled);
    mkDirButton.setEnabled(enabled);
    chgDirButton.setEnabled(enabled);
    deleteButton.setEnabled(enabled);
    renameButton.setEnabled(enabled);
    refreshButton.setEnabled(enabled);
  }

  //Ftp operations
  void mkDir() {}
  void chgDir() {}
  void delete(String fileName) {}
  void rename(String fileName) {}
  void refresh() {}
  void getFile(String fileName) {}

  int getSelectedIndex() {return listList.getSelectedIndex();}
  String getSelected() {
    if (listList.getSelectedIndex() < 0) return "";
    String fileName = listList.getSelectedValue().toString();
    if (fileName.startsWith("<DIR>")) fileName = fileName.substring(6);
    return fileName;
  }

  //set-data operations
  void setList(Vector listData) {listList.setListData(listData);}
  void setSeperator(char fileSep) {sep = fileSep;}

  //directory operations
  String getDir() {return ((String) directoryComboBox.getItemAt(0)) + sep;}
  void setDirectory(String path) {
    //if the path is in the list, remove it, and then insert it at the top
    directoryComboBox.removeItem(path);
    directoryComboBox.insertItemAt(path, 0);
    //don't forget to reset the selection index
    directoryComboBox.setSelectedIndex(0);
  }

  void directoryComboBox_actionPerformed(ActionEvent e) {
    if (directoryComboBox.getSelectedItem() != null)
      setDirectory(directoryComboBox.getSelectedItem().toString());
  }
}
