package org.isf.video;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JDialog;

import org.isf.video.gui.PhotoboothDialog;
import org.isf.video.gui.PhotoboothPanelModel;
import org.isf.video.gui.PhotoboothPanelPresentationModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.sarxos.webcam.Webcam;

public class PhotoboothTester {
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoboothTester.class);

    public static void main(final String[] args) {
        final Webcam webcam = Webcam.getDefault();
        final Dimension[] resolutions = webcam.getDevice().getResolutions();
        final PhotoboothPanelPresentationModel presentationModel = new PhotoboothPanelPresentationModel();
        presentationModel.addBeanPropertyChangeListener(PhotoboothPanelModel.PROPERTY_IMAGE, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                final Object newValue = propertyChangeEvent.getNewValue();
                if (newValue instanceof BufferedImage) {
                    final BufferedImage bufferedImage = (BufferedImage) newValue;
                    LOGGER.info("New image is being set {}x{}", bufferedImage.getWidth(), bufferedImage.getHeight());
                }
            }
        });
        presentationModel.addBeanPropertyChangeListener(PhotoboothPanelModel.PROPERTY_RESOLUTION, new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
                Object newValue = propertyChangeEvent.getNewValue();
                if (newValue instanceof Dimension) {
                    final Dimension newDimension = (Dimension) newValue;
                    LOGGER.info("New dimension is {}x{}", newDimension.getWidth(), newDimension.getHeight());
                }
            }
        });

        // initialise to the highest resolution
        presentationModel.setResolution(resolutions[resolutions.length - 1]);

        final PhotoboothDialog photoboothDialog = new PhotoboothDialog(presentationModel, new JDialog());
        photoboothDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        photoboothDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                // invoked when the dialog itself is being closed
                super.windowClosing(windowEvent);
                System.exit(0);
            }

            @Override
            public void windowClosed(WindowEvent windowEvent) {
                // invoked when the dialog's "dispose" is called (programmatically)
                super.windowClosed(windowEvent);
                System.exit(0);
            }
        });
        photoboothDialog.setVisible(true);
        photoboothDialog.toFront();
        photoboothDialog.requestFocus();

    }
}
