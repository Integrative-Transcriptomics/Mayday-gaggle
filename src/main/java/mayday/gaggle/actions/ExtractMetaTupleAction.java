package mayday.gaggle.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import mayday.gaggle.GagglePlugin;

import org.systemsbiology.gaggle.core.datatypes.GaggleData;
import org.systemsbiology.gaggle.core.datatypes.GaggleTuple;
import org.systemsbiology.gaggle.core.datatypes.Tuple;

@SuppressWarnings("serial")
public class ExtractMetaTupleAction extends AbstractAction {

	protected GaggleData data;
	
	public ExtractMetaTupleAction(GaggleData data) {
		super("Extract annotation");
		this.data=data;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		Tuple t = data.getMetadata();
		GaggleTuple gt = new GaggleTuple();
		gt.setData(t);
		gt.setMetadata(null);
		gt.setName("Annotation from "+data.getName());
		GagglePlugin.handleData("Local Mayday", gt);
	}

}
