/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2020 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isf.video.gui;

import java.awt.Dimension;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.isf.gui.BaseComponent;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author User #1
 * Created by JFormDesigner on Mon Mar 09 21:10:41 AEDT 2020
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
		resolutionPanel = new JPanel();
		webcamLabel = new JLabel();
		webcamComboBox = new JComboBox();
		resolutionLabel = new JLabel();
		resolutionComboBox = new JComboBox();
		captureButton = new JButton();
		buttonContainer = new JPanel();
		okButton = new JButton();
		cancelButton = new JButton();

		//======== photoBoothPanel ========
		{
			photoBoothPanel.setLayout(new FormLayout(
				"$ugap, $lcgap, default:grow, $lcgap, $ugap",
				"$ugap, $lgap, fill:default:grow, 2*($lgap, default), $lgap, $ugap"));

			//======== photoContainer ========
			{
				photoContainer.setLayout(new FormLayout(
					"default:grow, $lcgap, default:grow",
					"fill:default:grow"));
				((FormLayout)photoContainer.getLayout()).setColumnGroups(new int[][] {{1, 3}});

				//======== streamingPanel ========
				{
					streamingPanel.setPreferredSize(new Dimension(300, 200));
					streamingPanel.setMinimumSize(new Dimension(300, 200));
					streamingPanel.setLayout(new FormLayout(
						"default:grow",
						"default:grow"));
				}
				photoContainer.add(streamingPanel, CC.xy(1, 1));

				//======== snapshotPanel ========
				{
					snapshotPanel.setMinimumSize(new Dimension(300, 200));
					snapshotPanel.setPreferredSize(new Dimension(300, 200));
					snapshotPanel.setLayout(new FormLayout(
						"default:grow",
						"default:grow"));
				}
				photoContainer.add(snapshotPanel, CC.xy(3, 1));
			}
			photoBoothPanel.add(photoContainer, CC.xy(3, 3, CC.FILL, CC.FILL));

			//======== resolutionPanel ========
			{
				resolutionPanel.setLayout(new FormLayout(
					"default, 3*($lcgap), default, $lcgap, $rgap, $lcgap, default, 3*($lcgap), default, $lcgap, $button",
					"default"));

				//---- webcamLabel ----
				webcamLabel.setText("Webcam");
				resolutionPanel.add(webcamLabel, CC.xy(1, 1));
				resolutionPanel.add(webcamComboBox, CC.xy(5, 1));

				//---- resolutionLabel ----
				resolutionLabel.setText("Resolution");
				resolutionPanel.add(resolutionLabel, CC.xy(9, 1));
				resolutionPanel.add(resolutionComboBox, CC.xy(13, 1));

				//---- captureButton ----
				captureButton.setText("Capture");
				resolutionPanel.add(captureButton, CC.xy(15, 1));
			}
			photoBoothPanel.add(resolutionPanel, CC.xy(3, 5, CC.CENTER, CC.DEFAULT));

			//======== buttonContainer ========
			{
				buttonContainer.setLayout(new FormLayout(
					"$button, $lcgap, $button",
					"default"));

				//---- okButton ----
				okButton.setText("OK");
				buttonContainer.add(okButton, CC.xy(1, 1));

				//---- cancelButton ----
				cancelButton.setText("Cancel");
				buttonContainer.add(cancelButton, CC.xy(3, 1));
			}
			photoBoothPanel.add(buttonContainer, CC.xy(3, 7, CC.RIGHT, CC.DEFAULT));
		}
		// JFormDesigner - End of component initialization  //GEN-END:initComponents
	}

	// JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
	private JPanel photoBoothPanel;
	private JPanel photoContainer;
	private JPanel streamingPanel;
	private JPanel snapshotPanel;
	private JPanel resolutionPanel;
	private JLabel webcamLabel;
	protected JComboBox webcamComboBox;
	private JLabel resolutionLabel;
	protected JComboBox resolutionComboBox;
	private JButton captureButton;
	private JPanel buttonContainer;
	private JButton okButton;
	private JButton cancelButton;
	// JFormDesigner - End of variables declaration  //GEN-END:variables

}
