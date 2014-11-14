package mayday.gaggle.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.AbstractAction;

import mayday.core.DataSet;
import mayday.core.MasterTable;
import mayday.core.Probe;
import mayday.core.ProbeList;
import mayday.core.structures.graph.Edge;
import mayday.graphviewer.core.GraphViewerPlot;
import mayday.graphviewer.graphprovider.ProbeGraphProvider;
import mayday.vis3.components.PlotScrollPane;
import mayday.vis3.graph.components.CanvasComponent;
import mayday.vis3.model.Visualizer;

import org.systemsbiology.gaggle.core.datatypes.GaggleData;
import org.systemsbiology.gaggle.core.datatypes.Interaction;
import org.systemsbiology.gaggle.core.datatypes.Network;

@SuppressWarnings("serial")
public class ShowNetworkAction extends AbstractAction {

	protected GaggleData data;
	protected String sender;
	
	public ShowNetworkAction(GaggleData data, String sender) {
		super("Show Network");
		this.data=data;
		this.sender=sender;
	}
	
	protected Probe getOrCreateProbe(MasterTable mt, String name) {
		Probe pb = mt.getProbe(name);
		if (pb == null) {
			pb = new Probe(mt);
			pb.setName(name);
			pb.addExperiment(0.0);
		}
		return pb;
	}
	
	@SuppressWarnings("rawtypes")
	@Override
	public void actionPerformed(ActionEvent e) {

		// Code by Claudia

		GraphViewerPlot gvp = new GraphViewerPlot(new ProbeGraphProvider());
		Component myComponent = new PlotScrollPane(gvp);
		LinkedList<ProbeList> pls = new LinkedList<ProbeList>();

		Network network = (Network)data;
		DataSet ds = new DataSet("Received Network \""+data.getName()+"\" from " + sender);
		MasterTable mt = new MasterTable(ds);
		
		mt.setNumberOfExperiments(network.getNodeAttributeNames().length);

		// Create fake probes
		ProbeList newProbeList = new ProbeList(ds, true);
		for (String s : network.getNodes()) {
			newProbeList.addProbe(getOrCreateProbe(mt, s));
		}
		pls.add(newProbeList);
		Visualizer viz = null;
		if (myComponent != null) {
			viz = Visualizer.createWithPlot(ds, pls, myComponent);
			// ProbeGraphProvider now creates CanvasCOmponents in the GraphViewerPlot's GraphModel
		}
		
		// quicker access
		HashMap<String, CanvasComponent> speedup = new HashMap<String, CanvasComponent>();
		for (CanvasComponent c : gvp.getModel().getComponents()) {
			speedup.put(c.getLabel(), c);
		}
		
		for (Interaction i : network.getInteractions()) {
			Probe tpb = getOrCreateProbe(mt, i.getTarget());
			Probe spb = getOrCreateProbe(mt, i.getSource());
			CanvasComponent c1 = speedup.get(i.getTarget());
			CanvasComponent c2 = speedup.get(i.getSource());
			Edge edge = gvp.getModel().connect(c1, c2);
			edge.setName(i.getType());
			
			for (String attributeName : network.getEdgeAttributeNames()){
				HashMap attribute = network.getEdgeAttributes(attributeName);
				for (Object oentry: attribute.entrySet()){
					Map.Entry entry = (Map.Entry)oentry;
					//noch überprüfen, dass source vor target im key
					if (entry.getKey().toString().contains(spb.getName()) && entry.getKey().toString().contains(tpb.getName())){
						edge.addProperty(attributeName, entry.getValue().toString());
					}
				}				
			}
		}

		gvp.updatePlot();
		myComponent.update(gvp.getGraphics());
		viz.updateVisualizerMenus();
	}

}
