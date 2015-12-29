package mayday.gaggle.actions;

import java.awt.event.ActionEvent;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import mayday.core.DataSet;
import mayday.core.Mayday;
import mayday.core.ProbeList;
import mayday.gaggle.GaggleType;
import mayday.vis3.tables.ExpressionTable;

import org.systemsbiology.gaggle.core.datatypes.GaggleData;

@SuppressWarnings("serial")
public class ShowMatrixAction extends AbstractAction {

	protected GaggleData data;
	protected String sender;
	
	public ShowMatrixAction(GaggleData data, String sender) {
		super("Show data matrix");
		this.data=data;
		this.sender=sender;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		DataSet ds = GaggleType.asDataSet(data, sender);
		if (ds==null)
			JOptionPane.showMessageDialog(Mayday.sharedInstance, "Could not convert object to a DataSet", "Conversion failed", JOptionPane.ERROR_MESSAGE);
		else {
			LinkedList<ProbeList> pls = new LinkedList<ProbeList>();
			pls.add(ds.getProbeListManager().getProbeLists().get(0));
			new ExpressionTable().run(pls, ds.getMasterTable());
		}
	}

}
