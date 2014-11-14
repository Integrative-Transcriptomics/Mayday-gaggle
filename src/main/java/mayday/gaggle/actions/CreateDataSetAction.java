package mayday.gaggle.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import mayday.core.DataSet;
import mayday.core.Mayday;
import mayday.core.datasetmanager.DataSetManager;
import mayday.gaggle.GaggleType;

import org.systemsbiology.gaggle.core.datatypes.GaggleData;

@SuppressWarnings("serial")
public class CreateDataSetAction extends AbstractAction {

	protected GaggleData data;
	protected String sender;
	
	public CreateDataSetAction(GaggleData data, String sender) {
		super("Create DataSet");
		this.data=data;
		this.sender=sender;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		DataSet ds = GaggleType.asDataSet(data, sender);
		if (ds==null)
			JOptionPane.showMessageDialog(Mayday.sharedInstance, "Could not convert object to a DataSet", "Conversion failed", JOptionPane.ERROR_MESSAGE);
		else {
			DataSetManager.singleInstance.addObjectAtBottom(ds);
		}
	}

}
