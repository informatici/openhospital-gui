package org.isf.video.gui;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.jgoodies.binding.adapter.ComboBoxAdapter;
import com.jgoodies.forms.factories.CC;
import org.isf.utils.image.ImageUtil;
import org.isf.utils.jobjects.Cropping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import static java.util.stream.Collectors.toList;

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
            if (value != null && value instanceof Dimension) {
                final Dimension valueAsDimension = (Dimension) value;
                setText(String.format("%d x %d", (int) valueAsDimension.getWidth(), (int) valueAsDimension.getHeight()));
            }
            return component;
        }
    };

    private final Webcam webcam;
    private final Dimension[] supportedResolutions;
    private WebcamPanel webcamPanel;
    private final PhotoboothPanelPresentationModel photoboothPanelPresentationModel;
    private final JDialog owner;
    private Cropping cropping;
    private Action okAction;

    public PhotoboothComponentImpl(final PhotoboothPanelPresentationModel model,
                                   final JDialog owner) {
        this.photoboothPanelPresentationModel = model;
        this.owner = owner;

        this.webcam = Webcam.getDefault();
        this.supportedResolutions = webcam.getDevice().getResolutions();
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

        this.webcamPanel = new WebcamPanel(webcam, false);
        getStreamingPanel().add(webcamPanel, CC.xy(1, 1));
    }

    @Override
    protected void initGUIState() throws Exception {
        super.initGUIState();
        this.webcam.setViewSize(photoboothPanelPresentationModel.getResolution());
        this.okAction.setEnabled(false);
        this.webcamPanel.start();
    }

    @Override
    protected void bind() throws Exception {
        super.bind();
        this.resolutionComboBox.setModel(new ComboBoxAdapter<>(
                Arrays.stream(supportedResolutions).collect(toList()),
                photoboothPanelPresentationModel.getModel(PhotoboothPanelModel.PROPERTY_RESOLUTION)
        ));
        photoboothPanelPresentationModel.addBeanPropertyChangeListener(PhotoboothPanelModel.PROPERTY_RESOLUTION, new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
                final Object newValue = propertyChangeEvent.getNewValue();
                if (newValue != null && newValue instanceof Dimension) {
                    webcamPanel.stop();
                    webcam.close();
                    getStreamingPanel().remove(webcamPanel);

                    LOGGER.info("Changing webcam dimesion to {}", (Dimension) newValue);
                    webcam.setViewSize((Dimension) newValue);
                    getStreamingPanel().setPreferredSize((Dimension) newValue);
                    getStreamingPanel().setMinimumSize((Dimension) newValue);
                    getSnapshotPanel().setPreferredSize((Dimension) newValue);
                    getSnapshotPanel().setMinimumSize((Dimension) newValue);

                    webcamPanel = new WebcamPanel(webcam);
                    getStreamingPanel().add(webcamPanel, CC.xy(1, 1));
                    owner.pack();
                    owner.repaint();
                    owner.revalidate();
                }
            }
        });
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
            presentationModel().triggerFlush();
            cleanup();
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
                getSnapshotPanel().removeAll();
                getSnapshotPanel().add(cropping, CC.xy(1, 1));
                getPhotoBoothPanel().repaint();
                getPhotoBoothPanel().revalidate();
            });
        });

        getDiscardButton().addActionListener(actionEvent -> {
            SwingUtilities.invokeLater(() -> {
                getSnapshotPanel().removeAll();
                getPhotoBoothPanel().repaint();
                getPhotoBoothPanel().revalidate();
            });
            presentationModel().getBufferedModel(PhotoboothPanelModel.PROPERTY_IMAGE).setValue(null);
        });
    }

    public void cleanup() {
        webcam.close();
    }
}
