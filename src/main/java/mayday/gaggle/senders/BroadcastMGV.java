package mayday.gaggle.senders;

import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JOptionPane;

import mayday.core.DataSet;
import mayday.core.Mayday;
import mayday.core.pluma.PluginInfo;
import mayday.core.pluma.PluginManagerException;
import mayday.core.settings.SettingDialog;
import mayday.core.settings.generic.HierarchicalSetting;
import mayday.core.settings.typed.BooleanSetting;
import mayday.core.settings.typed.RestrictedStringSetting;
import mayday.core.structures.graph.Edge;
import mayday.core.structures.graph.Node;
import mayday.gaggle.GagglePlugin;
import mayday.graphviewer.core.GraphViewerPlot;
import mayday.graphviewer.plugins.AbstractGraphViewerPlugin;
import mayday.graphviewer.plugins.GraphViewerPlugin;
import mayday.vis3.graph.components.CanvasComponent;
import mayday.vis3.graph.components.MultiProbeComponent;
import mayday.vis3.graph.model.GraphModel;

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.core.datatypes.GaggleData;
import org.systemsbiology.gaggle.core.datatypes.Interaction;
import org.systemsbiology.gaggle.core.datatypes.Network;

public class BroadcastMGV extends AbstractGraphViewerPlugin {

	@Override
	public void run(GraphViewerPlot canvas, GraphModel model, List<CanvasComponent> components) {

		GagglePlugin.showGUI();
		if (!GagglePlugin.ensureConnected()) {
			//			JOptionPane.showMessageDialog(Mayday.sharedInstance, "Could not connect to the Gaggle Boss", 
			//					"Connection failed", JOptionPane.ERROR_MESSAGE);
			return;
		}

		String[] geese = GagglePlugin.getGoose().getGeese();
		String[] geese_and_boss = new String[geese.length+1];
		geese_and_boss[0] = "Boss";
		System.arraycopy(geese, 0, geese_and_boss, 1, geese.length);

		RestrictedStringSetting gtgt = new RestrictedStringSetting("Target","Select which Goose should receive the object.\n" +
				"Select \"Boss\" to send to all connected Geese.",0,geese_and_boss);
		BooleanSetting useExData = new BooleanSetting("Include expression values", null, true);

		HierarchicalSetting sett = new HierarchicalSetting("Send ProbeList to Gaggle")
		.addSetting(gtgt).addSetting(useExData);

		SettingDialog sd = new SettingDialog(Mayday.sharedInstance, "Send ProbeList to Gaggle", sett);
		sd.showAsInputDialog();
		if (!sd.closedWithOK())
			return;

		Boss boss = GagglePlugin.getBoss();

		boolean addExdata = useExData.getBooleanValue();

		Network gaggleNetwork = new Network();
		gaggleNetwork.setName(model.getGraph().getName()); 

		addMetaTuple(gaggleNetwork,canvas.getModelHub().getViewModel().getDataSet());

		for (CanvasComponent c : components){
			gaggleNetwork.add(c.getLabel());
			if (addExdata && (c instanceof MultiProbeComponent)) { // here are values				
				MultiProbeComponent mpc=(MultiProbeComponent)c;
				int sz = mpc.getProbes().size();
				if (sz>0) {
					double[] val1 = mpc.getProbes().get(0).getValues();
					if (sz>1) { // compute mean
						double[] val = new double[val1.length];
						System.arraycopy(val1, 0, val, 0, val1.length);
						for (int i=1; i!=sz; ++i) {
							double[] valn = mpc.getProbes().get(i).getValues();
							for (int j=0; j!=val.length; ++j) {
								val[j]+=valn[j];
							}
						}
						for (int j=0; j!=val.length; ++j) {
							val[j]/=(double)sz;
						}
						val1=val;						
					}
					int count = 0; 
					for (double value : val1){
						gaggleNetwork.addNodeAttribute(c.getLabel(), String.valueOf(count++), String.valueOf(value));
					}
				}
			}

		}

		for (Edge e : model.getEdges()){
			Node source = e.getSource();
			Node target = e.getTarget();
			String type = e.getName();
			Map<String, String> properties = e.getProperties();
			for (Entry<String, String> prop : properties.entrySet()){
				gaggleNetwork.addEdgeAttribute(e.getName(), prop.getKey(), prop.getValue());  //how is e.getName() mapped to Interaction inta?
			}
			Interaction inta = new Interaction(source.getName(), target.getName(), type);
			gaggleNetwork.add(inta);
		}

		try {
			boss.broadcastNetwork(GagglePlugin.getGoose().getAssignedName(), gtgt.getStringValue(), gaggleNetwork);
		} catch (RemoteException re) {
			JOptionPane.showMessageDialog(Mayday.sharedInstance, "Could not transfer data:\n"+re.getMessage(), "Communication failed", JOptionPane.ERROR_MESSAGE);
		}
	}


	@Override
	public void init() {

	}

	protected static void addMetaTuple(GaggleData data, DataSet ds) {
		MaydayMetaTuple.addMetaTuple(data, ds);
	}

	@Override
	public PluginInfo register() throws PluginManagerException {
		PluginInfo pli = new PluginInfo(
				getClass(),
				"PAS.gaggle.send.mgv",
				new String[]{},
				GraphViewerPlugin.MC_GRAPH_GRAPH,
				new HashMap<String, Object>(),
				"Claudia Broelemann",
				"broelema@informatik.uni-tuebingen.de",
				"Sends an MGV graph to the Gaggle Boss",
				"Send Network to Gaggle"		
				);
		pli.setIcon("mayday/gaggle/gaggle.gif");
		return pli;	
	}


}
