package mayday.gaggle.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import mayday.core.DataSet;
import mayday.core.datasetmanager.gui.DataSetManagerView;
import mayday.gaggle.incoming.NetworkToDataset;

import org.systemsbiology.gaggle.core.datatypes.GaggleData;
import org.systemsbiology.gaggle.core.datatypes.Network;

@SuppressWarnings("serial")
public class AddInteractionsMIOAction extends AbstractAction {

	protected GaggleData data;
	protected String sender;
	
	public AddInteractionsMIOAction(GaggleData data, String sender) {
		super("Add interactions to DataSet");
		this.data=data;
		this.sender=sender;
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		DataSet ds = DataSetManagerView.getInstance().getSelectedDataSets().get(0);
		NetworkToDataset.addInteractionMIOs(ds, (Network)data);
	}


}
