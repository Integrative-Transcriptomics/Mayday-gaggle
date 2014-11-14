package mayday.gaggle;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import mayday.core.datasetmanager.DataSetManager;
import mayday.core.gui.components.ExcellentBoxLayout;
import mayday.gaggle.actions.AddInteractionsMIOAction;
import mayday.gaggle.actions.AddMIOAction;
import mayday.gaggle.actions.BrowseDatasetAction;
import mayday.gaggle.actions.CreateDataSetAction;
import mayday.gaggle.actions.CreateProbeListAction;
import mayday.gaggle.actions.DismissObjectAction;
import mayday.gaggle.actions.ExtractMetaTupleAction;
import mayday.gaggle.actions.SelectionToVisualizerAction;
import mayday.gaggle.actions.ShowHeatmapAction;
import mayday.gaggle.actions.ShowInteractionsAction;
import mayday.gaggle.actions.ShowMatrixAction;
import mayday.gaggle.actions.ShowNetworkAction;

import org.systemsbiology.gaggle.core.datatypes.Cluster;
import org.systemsbiology.gaggle.core.datatypes.DataMatrix;
import org.systemsbiology.gaggle.core.datatypes.GaggleData;
import org.systemsbiology.gaggle.core.datatypes.GaggleTuple;
import org.systemsbiology.gaggle.core.datatypes.Namelist;
import org.systemsbiology.gaggle.core.datatypes.Network;

@SuppressWarnings("serial")
public class GaggleDataView extends JPanel {
	


	public GaggleDataView(GaggleData data, String sender) {
		super(new ExcellentBoxLayout(true, 5));
		add(new JLabel("Recieved "+infoString(data)));
		add(new JLabel("from "+sender));
		
		// creating probeLists, browsing and using selections is always possible when a ds is open
		if (DataSetManager.singleInstance.getNumberOfObjects()>0) {
			add(new JButton(new CreateProbeListAction(data, sender)));		
			add(new JButton(new BrowseDatasetAction(data, sender)));
			add(new JButton(new SelectionToVisualizerAction(data, sender)));
		}
		
		if (data.getMetadata()!=null)
			add(new JButton(new ExtractMetaTupleAction(data)));
		
		switch(GaggleType.typeOf(data)) {
		case Network:
			add(new JButton(new ShowNetworkAction(data, sender)));  
			add(new JButton(new ShowInteractionsAction(data, sender)));
			add(new JButton(new ShowHeatmapAction(data, sender)));
			if (DataSetManager.singleInstance.getNumberOfObjects()>0) {
				add(new JButton(new AddInteractionsMIOAction(data, sender)));
				add(new JButton(new AddMIOAction(data, sender)));
			}
			add(new JButton(new CreateDataSetAction(data, sender)));
			break;
		case Matrix:
			add(new JButton(new ShowHeatmapAction(data, sender)));
			add(new JButton(new ShowMatrixAction(data, sender)));
			add(new JButton(new CreateDataSetAction(data, sender)));
		}		
		
		// dismissing is always possible
		add(new JButton(new DismissObjectAction(data)));		
		
	}
	
	public static String infoString(GaggleData data) {
		GaggleType gt = GaggleType.typeOf(data);
		switch(gt) {
		case Tuple:
			return "a "+gt+" \""+((GaggleTuple)data).getName()+"\"";
		case Network:
			return "a "+gt+" with "+((Network)data).getNodes().length+" nodes";
		case Cluster:
			return "a "+gt+" of size "+((Cluster)data).getRowNames().length+"x"+((Cluster)data).getColumnNames().length;
		case List:
			return "a "+gt+" of "+((Namelist)data).getNames().length+" names";
		case Matrix:
			return "a "+gt+" of size "+((DataMatrix)data).getRowCount()+"x"+((DataMatrix)data).getColumnCount();
		default:
			return "an unknown object named \""+data.getName()+"\"";
		}
	}

	
}
