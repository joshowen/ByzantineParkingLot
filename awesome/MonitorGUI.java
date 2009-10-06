package awesome;

import javax.swing.JTabbedPane;
import java.awt.Dimension;

public class MonitorGUI extends JTabbedPane {

	private static final long serialVersionUID = 1L;
	private JTabbedPane jTabbedPane = null;

	/**
	 * This is the default constructor
	 */
	public MonitorGUI() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(669, 435);

		this.addTab(null, null, getJTabbedPane(), null);
	}

	/**
	 * This method initializes jTabbedPane	
	 * 	
	 * @return javax.swing.JTabbedPane	
	 */
	private JTabbedPane getJTabbedPane() {
		if (jTabbedPane == null) {
			jTabbedPane = new JTabbedPane();
		}
		return jTabbedPane;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
