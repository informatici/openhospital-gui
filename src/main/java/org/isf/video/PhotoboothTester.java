/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2023 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
 *
 * Open Hospital is a free and open source software for healthcare data management.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * https://www.gnu.org/licenses/gpl-3.0-standalone.html
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */
package org.isf.video;

import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;
import javax.swing.WindowConstants;

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
        presentationModel.addBeanPropertyChangeListener(PhotoboothPanelModel.PROPERTY_IMAGE, propertyChangeEvent -> {
            final Object newValue = propertyChangeEvent.getNewValue();
            if (newValue instanceof BufferedImage) {
                final BufferedImage bufferedImage = (BufferedImage) newValue;
                LOGGER.info("New image is being set {}x{}", bufferedImage.getWidth(), bufferedImage.getHeight());
            }
        });
        presentationModel.addBeanPropertyChangeListener(PhotoboothPanelModel.PROPERTY_RESOLUTION, propertyChangeEvent -> {
            Object newValue = propertyChangeEvent.getNewValue();
            if (newValue instanceof Dimension) {
                final Dimension newDimension = (Dimension) newValue;
                LOGGER.info("New dimension is {}x{}", newDimension.getWidth(), newDimension.getHeight());
            }
        });

        // Initialize to the highest resolution
        presentationModel.setResolution(resolutions[resolutions.length - 1]);

        final PhotoboothDialog photoboothDialog = new PhotoboothDialog(presentationModel, new JDialog());
        photoboothDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
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
