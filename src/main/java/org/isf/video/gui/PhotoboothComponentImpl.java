/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.video.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.SwingUtilities;

import org.isf.utils.image.ImageUtil;
import org.isf.utils.jobjects.Cropping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.jgoodies.binding.adapter.ComboBoxAdapter;
import com.jgoodies.binding.list.SelectionInList;
import com.jgoodies.forms.factories.CC;

public final class PhotoboothComponentImpl extends PhotoboothComponent {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoboothComponentImpl.class);
    private static final DefaultListCellRenderer RESOLUTION_DROPDOWN_OPTION_RENDERER = new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(final JList list,
                                                      final Object value,
                                                      final int index,
                                                      final boolean isSelected,
                                                      final boolean cellHasFocus) {
            final Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Dimension) {
                final Dimension valueAsDimension = (Dimension) value;
                setText(String.format("%d x %d", (int) valueAsDimension.getWidth(), (int) valueAsDimension.getHeight()));
            }
            return component;
        }
    };
    private static final DefaultListCellRenderer WEBCAM_DROPDOWN_OPTION_RENDERER = new DefaultListCellRenderer() {
        @Override
        public Component getListCellRendererComponent(final JList list,
                                                      final Object value,
                                                      final int index,
                                                      final boolean isSelected,
                                                      final boolean cellHasFocus) {
            final Component component = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof Webcam) {
                final Webcam webcam = (Webcam) value;
                setText(webcam.getName());
            }
            return component;
        }
    };

    private Webcam webcam;
    private final List<Dimension> supportedResolutions;
    private WebcamPanel webcamPanel;
    private final PhotoboothPanelPresentationModel photoboothPanelPresentationModel;
    private final JDialog owner;
    private Cropping cropping;
    private Action okAction;
    private PropertyChangeListener webcamResolutionChangeListener;
    private final List<Webcam> allWebcams;
    private final SelectionInList<Dimension> dimensionSelectionInList;
    private PropertyChangeListener webcamChangeListener;


    public PhotoboothComponentImpl(final PhotoboothPanelPresentationModel model,
                                   final JDialog owner) {
        this.photoboothPanelPresentationModel = model;
        this.owner = owner;

        this.allWebcams = Webcam.getWebcams();
        this.webcam = Webcam.getDefault();

        this.supportedResolutions = new ArrayList<>();
        Collections.addAll(supportedResolutions, webcam.getDevice().getResolutions());
        dimensionSelectionInList = new SelectionInList<>(supportedResolutions);
    }

    @Override
    public void initComponents() {
        super.initComponents();

        // set initial size of all photo panels to use the resolution defined on the model
        getStreamingPanel().setPreferredSize(photoboothPanelPresentationModel.getResolution());
        getStreamingPanel().setMinimumSize(photoboothPanelPresentationModel.getResolution());
        getSnapshotPanel().setPreferredSize(photoboothPanelPresentationModel.getResolution());
        getSnapshotPanel().setMinimumSize(photoboothPanelPresentationModel.getResolution());
        this.resolutionComboBox.setRenderer(RESOLUTION_DROPDOWN_OPTION_RENDERER);
        this.webcamComboBox.setRenderer(WEBCAM_DROPDOWN_OPTION_RENDERER);

        this.webcam.setViewSize(photoboothPanelPresentationModel.getResolution());
        this.webcamPanel = new WebcamPanel(webcam, false);
        getStreamingPanel().add(webcamPanel, CC.xy(1, 1));
    }

    @Override
    protected void initGUIState() throws Exception {
        super.initGUIState();
        this.okAction.setEnabled(false);
        this.webcamPanel.start();
    }

    @Override
    protected void bind() throws Exception {
        super.bind();
        ComboBoxAdapter<Dimension> resolutionModel = new ComboBoxAdapter(
                dimensionSelectionInList,
                photoboothPanelPresentationModel.getModel(PhotoboothPanelModel.PROPERTY_RESOLUTION)
        );
        this.resolutionComboBox.setModel(resolutionModel);

        this.webcamComboBox.setModel(new ComboBoxAdapter(
                allWebcams,
                photoboothPanelPresentationModel.getModel(PhotoboothPanelModel.PROPERTY_WEBCAM)
        ));

        webcamResolutionChangeListener = propertyChangeEvent -> {
            final Object newValue = propertyChangeEvent.getNewValue();
            if (newValue instanceof Dimension) {
                this.stopWebcam();

                LOGGER.info("Changing webcam dimension to {}", newValue);
                webcam.setViewSize((Dimension) newValue);
                this.getStreamingPanel().setPreferredSize((Dimension) newValue);
                this.getStreamingPanel().setMinimumSize((Dimension) newValue);
                this.getSnapshotPanel().setPreferredSize((Dimension) newValue);
                this.getSnapshotPanel().setMinimumSize((Dimension) newValue);

                LOGGER.info("Closing webcam before attaching to panel.");
                webcam.close();
                webcamPanel = new WebcamPanel(webcam);
                this.getStreamingPanel().add(webcamPanel, CC.xy(1, 1));
                owner.pack();
                owner.repaint();
                owner.validate();
            }
        };
        photoboothPanelPresentationModel.addBeanPropertyChangeListener(PhotoboothPanelModel.PROPERTY_RESOLUTION, webcamResolutionChangeListener);

        webcamChangeListener = propertyChangeEvent -> {
            final Object newValue = propertyChangeEvent.getNewValue();
            if (newValue instanceof Webcam) {
                this.stopWebcam();

                webcam = (Webcam) newValue;
                LOGGER.info("Webcam changed to {}", webcam.getName());

                supportedResolutions.clear();
                final Dimension[] allResolutions = webcam.getDevice().getResolutions();
                Collections.addAll(supportedResolutions, allResolutions);
                dimensionSelectionInList.fireContentsChanged(0, supportedResolutions.size() - 1);

                // set to highest resolution
                photoboothPanelPresentationModel.setResolution(allResolutions[allResolutions.length - 1]);
                LOGGER.info("Closing webcam {} before changing resolution", webcam.getName());
                webcam.close();

                LOGGER.info("Changing resolution to {}", photoboothPanelPresentationModel.getResolution());
                webcam.setViewSize(photoboothPanelPresentationModel.getResolution());

                this.getStreamingPanel().setPreferredSize(photoboothPanelPresentationModel.getResolution());
                this.getStreamingPanel().setMinimumSize(photoboothPanelPresentationModel.getResolution());
                this.getSnapshotPanel().setPreferredSize(photoboothPanelPresentationModel.getResolution());
                this.getSnapshotPanel().setMinimumSize(photoboothPanelPresentationModel.getResolution());

                LOGGER.info("Attaching webcam {} to panel", webcam.getName());
                webcamPanel = new WebcamPanel(webcam);
                this.getStreamingPanel().add(webcamPanel, CC.xy(1, 1));
                owner.pack();
                owner.repaint();
                owner.validate();

            }
        };
        photoboothPanelPresentationModel.addBeanPropertyChangeListener(PhotoboothPanelModel.PROPERTY_WEBCAM, webcamChangeListener);
    }

    private void stopWebcam() {
        webcamPanel.stop();
        webcam.close();
        getStreamingPanel().remove(webcamPanel);
    }

    @Override
    protected PhotoboothPanelPresentationModel buildModel()  {
        return photoboothPanelPresentationModel;
    }

    @Override
    protected void initEventHandling() throws Exception {
        super.initEventHandling();

        this.okAction = new AbstractAction("OK") {
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                // change image on the model, based on the cropped image
                presentationModel().setImage(cropping.clipImage());
                cleanup();
                owner.dispose();
            }
        };
        getOkButton().setAction(okAction);

        getCancelButton().addActionListener(actionEvent -> {
            this.presentationModel().triggerFlush();
            this.cleanup();
            owner.dispose();
        });

        getCaptureButton().addActionListener(actionEvent -> {
            final Dimension currentResolution = photoboothPanelPresentationModel.getResolution();
            // resize the image to match the current selected resolution. This is because under some circumstances, the
            // webcam's viewSize seems to be different from the currently selected resolution. Weird. i know..
            final BufferedImage resizedImage = ImageUtil.scaleImage(webcam.getImage(), (int) currentResolution.getWidth(), (int) currentResolution.getHeight());

            // set image on the cropping panel.
            cropping = new Cropping(resizedImage);
            okAction.setEnabled(true);
            SwingUtilities.invokeLater(() -> {
                this.getSnapshotPanel().removeAll();
                this.getSnapshotPanel().add(cropping, CC.xy(1, 1));
                this.getPhotoBoothPanel().repaint();
                this.getPhotoBoothPanel().revalidate();
            });
        });
    }

    public void cleanup() {
        stopWebcam();

        // need to remove listener here, to prevent memory leak the next time we open the photo frame again.
        photoboothPanelPresentationModel.removeBeanPropertyChangeListener(PhotoboothPanelModel.PROPERTY_RESOLUTION, webcamResolutionChangeListener);
        photoboothPanelPresentationModel.removeBeanPropertyChangeListener(PhotoboothPanelModel.PROPERTY_WEBCAM, webcamChangeListener);
    }
}
