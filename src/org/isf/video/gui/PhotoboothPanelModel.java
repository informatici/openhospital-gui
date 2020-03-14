package org.isf.video.gui;

import com.jgoodies.binding.beans.Model;

import java.awt.*;

public final class PhotoboothPanelModel extends Model {
    public static final String PROPERTY_IMAGE = "image";
    public static final String PROPERTY_RESOLUTION = "resolution";
    private Image image;

    private Dimension resolution;

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
}
