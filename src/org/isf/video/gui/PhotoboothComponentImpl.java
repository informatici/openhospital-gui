package org.isf.video.gui;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.jgoodies.binding.adapter.ComboBoxAdapter;
import com.jgoodies.forms.factories.CC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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

    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private final PhotoboothPanelPresentationModel photoboothPanelPresentationModel;
    private final JDialog owner;

    public PhotoboothComponentImpl(final PhotoboothPanelPresentationModel model,
                                   final JDialog owner) {
        this.photoboothPanelPresentationModel = model;
        this.owner = owner;
    }

    @Override
    public void initComponents() {
        super.initComponents();
        this.webcam = Webcam.getDefault();
        final List<Dimension> allSupportedDimesions = Arrays.stream(this.webcam.getDevice().getResolutions()).collect(Collectors.toList());
        final ComboBoxAdapter<Dimension> comboBoxAdapter = new ComboBoxAdapter<>(allSupportedDimesions, photoboothPanelPresentationModel.getModel(PhotoboothPanelModel.PROPERTY_WEBCAM_DIMESION));
        this.resolutionComboBox.setModel(comboBoxAdapter);
        this.resolutionComboBox.setRenderer(RESOLUTION_DROPDOWN_OPTION_RENDERER);
        this.webcam.setViewSize(allSupportedDimesions.get(allSupportedDimesions.size()-1));
        this.webcamPanel = new WebcamPanel(webcam);
        getStreamingPanel().add(webcamPanel, CC.xy(1, 1));
    }

    @Override
    protected void bind() throws Exception {
        super.bind();
        photoboothPanelPresentationModel.addBeanPropertyChangeListener(PhotoboothPanelModel.PROPERTY_WEBCAM_DIMESION, new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent propertyChangeEvent) {
                Object newValue = propertyChangeEvent.getNewValue();
                if (newValue != null && newValue instanceof Dimension) {
                    webcamPanel.stop();
                    webcam.close();
                    getStreamingPanel().remove(webcamPanel);

                    LOGGER.info("Changing webcam dimesion to {}", (Dimension) newValue);
                    webcam.setViewSize((Dimension) newValue);
                    webcamPanel = new WebcamPanel(webcam);
                    getStreamingPanel().add(webcamPanel, CC.xy(1, 1));
                    getPhotoBoothPanel().repaint();
                    getPhotoBoothPanel().revalidate();
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

        getOkButton().addActionListener(actionEvent -> {
            getPM().triggerCommit();
            cleanup();
            owner.dispose();
        });

        getCancelButton().addActionListener(actionEvent -> {
            getPM().triggerFlush();
            cleanup();
            owner.dispose();
        });

        getCaptureButton().addActionListener(actionEvent -> {
            final BufferedImage newImage = webcam.getImage();
            getPM().getBufferedModel(PhotoboothPanelModel.PROPERTY_IMAGE).setValue(newImage);

            final JLabel photoFrame = new JLabel(new ImageIcon(newImage));
            SwingUtilities.invokeLater(() -> {
                getSnapshotPanel().removeAll();
                getSnapshotPanel().add(photoFrame, CC.xy(1, 1));
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
            getPM().getBufferedModel(PhotoboothPanelModel.PROPERTY_IMAGE).setValue(null);
        });
    }

    public void cleanup() {
        webcam.close();
    }
}
