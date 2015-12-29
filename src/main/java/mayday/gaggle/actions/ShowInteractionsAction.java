package mayday.gaggle.actions;

import java.awt.event.ActionEvent;
import java.util.LinkedList;

import javax.swing.AbstractAction;

import mayday.core.DataSet;
import mayday.core.MasterTable;
import mayday.core.Probe;
import mayday.core.ProbeList;
import mayday.gaggle.incoming.NetworkToDataset;
import mayday.vis3.model.Visualizer;
import mayday.vis3.tables.MIOTableWindow;

import org.systemsbiology.gaggle.core.datatypes.GaggleData;
import org.systemsbiology.gaggle.core.datatypes.Network;

@SuppressWarnings("serial")
public class ShowInteractionsAction extends AbstractAction {

	protected GaggleData data;
	protected String sender;
	
	public ShowInteractionsAction(GaggleData data, String sender) {
		super("Show interaction matrix");
		this.data=data;
		this.sender=sender;
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		DataSet ds = new DataSet("Interactions in the Network \""+data.getName()+"\" from " + sender);
		MasterTable mt = new MasterTable(ds);
		mt.setNumberOfExperiments(1);
		Network network = (Network)data;
		
		for (String s : network.getNodes()) {
			Probe pb = new Probe(mt);
			pb.addExperiment(0.0);
			pb.setName(s);
			try {
				mt.addProbe(pb);
			} catch (RuntimeException e) {
				String ex = "Create DataSet failed: " + e.getMessage();
				e.printStackTrace();
				throw new RuntimeException(ex);
			}
		}
		ds.setMasterTable(mt);

		NetworkToDataset.addInteractionMIOs(ds, network);
		
		ProbeList gpl = mt.createGlobalProbeList(true);
		ds.getProbeListManager().addObject(gpl);
		LinkedList<ProbeList> pls = new LinkedList<ProbeList>();
		pls.add(gpl);
		Visualizer viz = new Visualizer(ds, pls);
		MIOTableWindow etw = new MIOTableWindow(viz);
		viz.addPlot(etw);
		etw.setVisible(true);
	}


}
