package org.isf.video.gui;

import com.jgoodies.binding.beans.Model;

import java.awt.*;

public final class PhotoboothPanelModel extends Model {
    public static final String PROPERTY_IMAGE = "image";

    private Image image;

    public Image getImage() {
        return image;
    }

    public void setImage(final Image newValue) {
        final Image oldValue = this.image;
        this.image = newValue;
        this.firePropertyChange(PROPERTY_IMAGE, oldValue, newValue);
    }
}
