package mayday.gaggle.actions;

import java.awt.event.ActionEvent;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;

import mayday.core.DataSet;
import mayday.core.ProbeList;
import mayday.gaggle.incoming.GaggleToProbelist;
import mayday.vis3.gui.VisualizerSelectionDialog;
import mayday.vis3.model.Visualizer;

import org.systemsbiology.gaggle.core.datatypes.GaggleData;

@SuppressWarnings("serial")
public class SelectionToVisualizerAction extends AbstractAction {

	protected GaggleData data;
	protected String sender;
	
	public SelectionToVisualizerAction(GaggleData data, String sender) {
		super("Use as visualizer selection");
		this.data=data;
		this.sender=sender;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		// now select a visualizer target
		
		VisualizerSelectionDialog vsd = new VisualizerSelectionDialog();
		vsd.setDialogDescription("Please select one or more visualizers");
		
		List<Visualizer> lv = null;
		
		if (vsd.getSelectableCount()==1) {
			lv = new LinkedList<Visualizer>();
			lv.add(Visualizer.openVisualizers.values().iterator().next().get(0)); // there is exactly one
		} else {
			vsd.setVisible(true);
			if (!vsd.isCanceled())
				lv = vsd.getSelection();
		}
					
		if (lv!=null) {
			
			DataSet lastDS = null;
			ProbeList tmp = null;
			
			for (Visualizer viz : lv) {
				DataSet vizds = viz.getViewModel().getDataSet();

				if (vizds!=lastDS || tmp==null) { // create probelist for selection
					
					tmp = GaggleToProbelist.convert(vizds, data, sender);					
					lastDS = vizds;
				} // if same dataset, keep old probelist for speedup
				
				if (tmp!=null)
					viz.getViewModel().setProbeSelection(tmp.getAllProbes());				
			}
		}

	}

}
