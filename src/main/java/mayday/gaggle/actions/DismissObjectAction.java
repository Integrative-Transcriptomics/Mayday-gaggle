package mayday.gaggle.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import mayday.gaggle.GagglePlugin;

import org.systemsbiology.gaggle.core.datatypes.GaggleData;

@SuppressWarnings("serial")
public class DismissObjectAction extends AbstractAction {

	protected GaggleData data;
	
	public DismissObjectAction(GaggleData data) {
		super("Dismiss object");
		this.data=data;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		GagglePlugin.dismissObject(data);
	}

}
