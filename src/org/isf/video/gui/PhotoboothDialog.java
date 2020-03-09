package org.isf.video.gui;

import javax.swing.*;
import java.awt.*;

public final class PhotoboothDialog extends JDialog {

    private final PhotoboothComponentImpl photoboothComponent;

    public PhotoboothDialog(final PhotoboothPanelPresentationModel model,
                            final Dialog owner) {
        super(owner, true);
        photoboothComponent = new PhotoboothComponentImpl(model, this);
        photoboothComponent.build();

        this.add(photoboothComponent.getPhotoBoothPanel());
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
    }

    @Override
    public void dispose() {
        photoboothComponent.cleanup();
        super.dispose();
    }

}
