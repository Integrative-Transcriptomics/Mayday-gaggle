package mayday.gaggle;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import mayday.core.gui.components.ExcellentBoxLayout;

import org.systemsbiology.gaggle.core.Boss;
import org.systemsbiology.gaggle.geese.common.GaggleConnectionListener;

@SuppressWarnings("serial")
public class GaggleGUIElement extends JPanel {
	
	JPanel objectPanel = new JPanel(new BorderLayout());
	JLabel remainingLabel = new JLabel("");
	
	public GaggleGUIElement(MaydayGoose goose) {
		super(new ExcellentBoxLayout(true, 5));
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(new ConnectionState(goose));
		buttonPanel.add(new ConnectionButton(goose));
		add(buttonPanel);
		buttonPanel.setMaximumSize(buttonPanel.getPreferredSize());
		
		objectPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		add(objectPanel);
		
		add(remainingLabel);
		
		notifyObject();
	}
	
	public void removeNotify() {
		GagglePlugin.isGuiVisible = false;
	}

	protected static class ConnectionState extends JLabel implements GaggleConnectionListener {

		public ConnectionState(MaydayGoose goose) {
			setConnected(goose.isConnected(),null);
			goose.addConnectionListener(this);
		}
		@Override
		public void setConnected(boolean isConnected, Boss arg1) {
			setText((isConnected?"Connected.":"Not connected."));
			
		}
		
	}
	
	protected static class ConnectionButton extends JButton implements GaggleConnectionListener, ActionListener {
		
		protected MaydayGoose goose;
		
		protected ConnectionButton(MaydayGoose goose) {
			setConnected(goose.isConnected(), null);
			goose.addConnectionListener(this);
			addActionListener(this);
			this.goose = goose;
		}

		@Override
		public void setConnected(boolean isConnected, Boss arg1) {
			setText((isConnected?"Disconnect":"Connect"));
		}

		@Override
		public void actionPerformed(ActionEvent e)  {
			if (goose.isConnected())
				goose.disconnect();
			else
				try {
					goose.connect();
				} catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(this, "Could not connect to the Gaggle Boss", "No connection", JOptionPane.ERROR_MESSAGE);
				}
			
		}
	}

	// there are new/removed objects recieved
	public void notifyObject() {
		objectPanel.removeAll();

		int waiting = GagglePlugin.getNumberOfWaitingObjects();
		if (waiting>0) {
			objectPanel.add(new GaggleDataView(GagglePlugin.getWaitingObject(0), GagglePlugin.getSender(0)));			
		} else {
			objectPanel.add(new JLabel("Received objects will appear here."));
		}
		objectPanel.setMaximumSize(objectPanel.getPreferredSize());
		
		waiting--;
		if (waiting>1) 
			remainingLabel.setText("There are "+(waiting)+" further objects");
		else if (waiting==1)
			remainingLabel.setText("There is 1 further object");
		else
			remainingLabel.setText("");
		
		invalidate();
		validate();
		if (getParent()!=null) {
			getParent().invalidate();
			getParent().validate();
		}
		revalidate();
	}
	
}
