package org.isf.video.gui;

import java.awt.Dimension;
import java.awt.Image;

import com.github.sarxos.webcam.Webcam;
import com.jgoodies.binding.beans.Model;

public final class PhotoboothPanelModel extends Model {
    public static final String PROPERTY_IMAGE = "image";
    public static final String PROPERTY_RESOLUTION = "resolution";
    public static final String PROPERTY_WEBCAM = "webcam";
    private Image image;
    private Dimension resolution;
    private Webcam webcam;

    public Image getImage() {
        return image;
    }

    public void setImage(final Image newValue) {
        final Image oldValue = this.image;
        this.image = newValue;
        this.firePropertyChange(PROPERTY_IMAGE, oldValue, newValue);
    }

    public Dimension getResolution() {
        return resolution;
    }

    public void setResolution(final Dimension resolution) {
        Dimension oldValue = this.resolution;
        this.resolution = resolution;
        this.firePropertyChange(PROPERTY_RESOLUTION, oldValue, resolution);
    }

    public Webcam getWebcam() {
        return webcam;
    }

    public void setWebcam(final Webcam webcam) {
        Webcam oldValue = this.webcam;
        this.webcam = webcam;
        this.firePropertyChange(PROPERTY_WEBCAM, oldValue, webcam);
    }
}
