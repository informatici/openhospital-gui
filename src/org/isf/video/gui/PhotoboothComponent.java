package org.isf.video.gui;

import java.awt.*;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import org.isf.gui.BaseComponent;

import javax.swing.*;
/*
 * Created by JFormDesigner on Mon Mar 09 21:10:41 AEDT 2020
 */



/**
 * @author User #1
 */
public class PhotoboothComponent extends BaseComponent<PhotoboothPanelPresentationModel> {

	public JPanel getPhotoBoothPanel() {
		return photoBoothPanel;
	}

	public JPanel getPhotoContainer() {
		return photoContainer;
	}

	public JPanel getStreamingPanel() {
		return streamingPanel;
	}

	public JPanel getSnapshotPanel() {
		return snapshotPanel;
	}

	public JButton getCaptureButton() {
		return captureButton;
	}

	public JButton getDiscardButton() {
		return discardButton;
	}

	public JButton getOkButton() {
		return okButton;
	}

	public JButton getCancelButton() {
		return cancelButton;
	}

	public JPanel getButtonContainer() {
		return buttonContainer;
	}

	public void initComponents() {
		// JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
		photoBoothPanel = new JPanel();
		photoContainer = new JPanel();
		streamingPanel = new JPanel();
		snapshotPanel = new JPanel();
		buttonContainer = new JPanel();
		captureButton = new JButton();
		discardButton = new JButton();
		okButton = new JButton();
		cancelButton = new JButton();

		//======== photoBoothPanel ========
		{
			photoBoothPanel.setLayout(new FormLayout(
				"$ugap, $lcgap, default:grow, $lcgap, $ugap",
				"default, $lgap, fill:default:grow, $lgap, default"));

			//======== photoContainer ========
			{
				photoContainer.setLayout(new FormLayout(
					"default:grow, $lcgap, default:grow",
					"fill:default:grow"));
				((FormLayout)photoContainer.getLayout()).setColumnGroups(new int[][] {{1, 3}});

				//======== streamingPanel ========
				{
					streamingPanel.setPreferredSize(new Dimension(640, 480));
					streamingPanel.setMinimumSize(new Dimension(640, 480));
					streamingPanel.setLayout(new FormLayout(
						"default:grow",
						"default:grow"));
				}
				photoContainer.add(streamingPanel, CC.xy(1, 1));

				//======== snapshotPanel ========
				{
					snapshotPanel.setMinimumSize(new Dimension(640, 480));
					snapshotPanel.setPreferredSize(new Dimension(640, 480));
					snapshotPanel.setLayout(new FormLayout(
						"default:grow",
						"default:grow"));
				}
				photoContainer.add(snapshotPanel, CC.xy(3, 1));
			}
			photoBoothPanel.add(photoContainer, CC.xy(3, 3, CC.FILL, CC.FILL));

			//======== buttonContainer ========
			{
				buttonContainer.setLayout(new FormLayout(
					"default, $lcgap, default",
					"default, $lgap, default"));

				//---- captureButton ----
				captureButton.setText("Capture");
				buttonContainer.add(captureButton, CC.xy(1, 1));

				//---- discardButton ----
				discardButton.setText("Discard");
				buttonContainer.add(discardButton, CC.xy(3, 1));

				//---- okButton ----
				okButton.setText("OK");
				buttonContainer.add(okButton, CC.xy(1, 3));

				//---- cancelButton ----
				cancelButton.setText("Cancel");
				buttonContainer.add(cancelButton, CC.xy(3, 3));
			}
			photoBoothPanel.add(buttonContainer, CC.xy(3, 5, CC.CENTER, CC.DEFAULT));
		}
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel photoBoothPanel;
	private JPanel photoContainer;
	private JPanel streamingPanel;
	private JPanel snapshotPanel;
	private JPanel buttonContainer;
	private JButton captureButton;
	private JButton discardButton;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables



}
