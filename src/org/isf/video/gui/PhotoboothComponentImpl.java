package org.isf.video.gui;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.jgoodies.forms.factories.CC;

import javax.swing.*;
import java.awt.image.BufferedImage;

public final class PhotoboothComponentImpl extends PhotoboothComponent {
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
        this.webcam.setViewSize(WebcamResolution.VGA.getSize());
        this.webcamPanel = new WebcamPanel(webcam);
        this.webcamPanel.setMirrored(true);
        getStreamingPanel().add(webcamPanel, CC.xy(1, 1));
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
                getPhotoBoothPanel().revalidate();
            });
        });

        getDiscardButton().addActionListener(actionEvent -> {
            // TODO: not 100% sure why this doesnt clear the panel
            SwingUtilities.invokeLater(() -> {
                getSnapshotPanel().removeAll();
                getPhotoBoothPanel().revalidate();
            });
            getPM().getBufferedModel(PhotoboothPanelModel.PROPERTY_IMAGE).setValue(null);
        });
    }

    public void cleanup() {
        webcam.close();
    }
}
