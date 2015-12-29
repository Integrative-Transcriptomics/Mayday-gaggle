package mayday.gaggle.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.table.TableModel;

import mayday.core.DataSet;
import mayday.core.datasetmanager.gui.DataSetManagerView;
import mayday.core.meta.io.probemio.CSVImportPlugin;
import mayday.gaggle.incoming.NetworkToDataset;

import org.systemsbiology.gaggle.core.datatypes.GaggleData;
import org.systemsbiology.gaggle.core.datatypes.Network;

@SuppressWarnings("serial")
public class AddMIOAction extends AbstractAction {

	protected GaggleData data;
	protected String sender;
	
	public AddMIOAction(GaggleData data, String sender) {
		super("Add meta information to DataSet");
		this.data=data;
		this.sender=sender;
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		DataSet ds = DataSetManagerView.getInstance().getSelectedDataSets().get(0);
		NetworkToDataset.addInteractionMIOs(ds, (Network)data);
		TableModel model = NetworkToDataset.convert((Network)data);
		new CSVImportPlugin().runWithModel(ds, model);
	}


}
