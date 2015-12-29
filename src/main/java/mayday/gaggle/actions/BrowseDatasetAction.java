package mayday.gaggle.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import mayday.core.DataSet;
import mayday.core.datasetmanager.gui.DataSetManagerView;
import mayday.gaggle.DatasetBrowser;
import mayday.gaggle.GaggleType;

import org.systemsbiology.gaggle.core.datatypes.GaggleData;

@SuppressWarnings("serial")
public class BrowseDatasetAction extends AbstractAction {

	protected GaggleData data;
	protected String sender;
	
	public BrowseDatasetAction(GaggleData data, String sender) {
		super("Browse current DataSet");
		this.data=data;
		this.sender=sender;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		DataSet ds = DataSetManagerView.getInstance().getSelectedDataSets().get(0);
		new DatasetBrowser(ds, GaggleType.extractStrings(data)).setVisible(true);
	}

}
