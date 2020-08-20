package org.isf.video.gui;

import java.awt.Dimension;
import java.awt.Image;

import com.github.sarxos.webcam.Webcam;
import com.jgoodies.binding.PresentationModel;

public final class PhotoboothPanelPresentationModel extends PresentationModel<PhotoboothPanelModel> {

    public PhotoboothPanelPresentationModel() {
        super(new PhotoboothPanelModel());
    }

    public void setImage(final Image image) {
        getBean().setImage(image);
    }

    public Dimension getResolution() {
        return getBean().getResolution();
    }

    public void setResolution(final Dimension resolution) {
        getBean().setResolution(resolution);
    }

    public void setWebcam(final Webcam webcam) {
        getBean().setWebcam(webcam);
    }
}
