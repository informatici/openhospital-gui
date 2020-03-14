package org.isf.video.gui;

import com.jgoodies.binding.PresentationModel;

import java.awt.*;

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
}
