package org.isf.video.gui;

import com.jgoodies.binding.beans.Model;

import java.awt.*;

public final class PhotoboothPanelModel extends Model {
    public static final String PROPERTY_IMAGE = "image";
    public static final String PROPERTY_WEBCAM_DIMESION = "webcamDimension";

    private Image image;

    private Dimension webcamDimension;

    public Image getImage() {
        return image;
    }

    public void setImage(final Image newValue) {
        final Image oldValue = this.image;
        this.image = newValue;
        this.firePropertyChange(PROPERTY_IMAGE, oldValue, newValue);
    }

    public Dimension getWebcamDimension() {
        return webcamDimension;
    }

    public void setWebcamDimension(final Dimension webcamDimension) {
        Dimension oldValue = this.webcamDimension;
        this.webcamDimension = webcamDimension;
        this.firePropertyChange(PROPERTY_WEBCAM_DIMESION, oldValue, webcamDimension);
    }
}
