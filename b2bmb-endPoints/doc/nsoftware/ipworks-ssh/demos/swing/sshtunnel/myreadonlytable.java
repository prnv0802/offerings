import javax.swing.*;
import javax.swing.table.TableModel;


public class myreadonlytable extends JTable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public myreadonlytable() {
		super();
	}
	public myreadonlytable(TableModel tm) {
		super(tm);
	}
	public boolean isCellEditable( int row, int col ) {
		return false;
	}

}
