package mayday.gaggle.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import mayday.core.DataSet;
import mayday.core.Mayday;
import mayday.core.ProbeList;
import mayday.core.pluma.PluginInfo;
import mayday.core.pluma.PluginManager;
import mayday.gaggle.GaggleType;
import mayday.vis3.PlotPlugin;
import mayday.vis3.model.Visualizer;

import org.systemsbiology.gaggle.core.datatypes.GaggleData;

@SuppressWarnings("serial")
public class ShowHeatmapAction extends AbstractAction {

	protected GaggleData data;
	protected String sender;
	
	public ShowHeatmapAction(GaggleData data, String sender) {
		super("Show Heatmap");
		this.data=data;
		this.sender=sender;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		DataSet ds = GaggleType.asDataSet(data, sender);
		if (ds==null)
			JOptionPane.showMessageDialog(Mayday.sharedInstance, "Could not convert object to a DataSet", "Conversion failed", JOptionPane.ERROR_MESSAGE);
		else {
			PluginInfo pli = PluginManager.getInstance().getPluginFromID("PAS.vis3.Heatmap4");
			if (pli!=null) {
				Component c = ((PlotPlugin)pli.getInstance()).getComponent();
				LinkedList<ProbeList> pls = new LinkedList<ProbeList>();
				pls.add(ds.getProbeListManager().getProbeLists().get(0));
				Visualizer.createWithPlot(ds, pls, c);
			} else {
				JOptionPane.showMessageDialog(Mayday.sharedInstance, "Could not open the heatmap plot", "Visualization failed", JOptionPane.ERROR_MESSAGE);
			}
			
		}
	}

}
