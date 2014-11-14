package mayday.gaggle.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import mayday.core.DataSet;
import mayday.core.ProbeList;
import mayday.core.datasetmanager.gui.DataSetManagerView;
import mayday.gaggle.incoming.GaggleToProbelist;

import org.systemsbiology.gaggle.core.datatypes.GaggleData;

@SuppressWarnings("serial")
public class CreateProbeListAction extends AbstractAction {

	protected GaggleData data;
	protected String sender;

	public CreateProbeListAction(GaggleData data, String sender) {
		super("Create ProbeList");
		this.data=data;
		this.sender=sender;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		DataSet ds = DataSetManagerView.getInstance().getSelectedDataSets().get(0);
		ProbeList pl = GaggleToProbelist.convert(ds, data, sender);
		if (pl!=null)
			ds.getProbeListManager().addObjectAtTop(pl);

	}

}
