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
