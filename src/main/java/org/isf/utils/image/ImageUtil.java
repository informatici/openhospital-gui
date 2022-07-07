/*
 * Open Hospital (www.open-hospital.org)
 * Copyright © 2006-2021 Informatici Senza Frontiere (info@informaticisenzafrontiere.org)
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
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.isf.utils.image;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.imageio.ImageIO;

import org.imgscalr.Scalr;

public final class ImageUtil {

	private ImageUtil() {
	}

	public static Image scaleImage(Image image, int maxDim) {
		double scale = (double) maxDim / (double) image.getHeight(null);
		if (image.getWidth(null) > image.getHeight(null)) {
			scale = (double) maxDim / (double) image.getWidth(null);
		}
		int scaledW = (int) (scale * image.getWidth(null));
		int scaledH = (int) (scale * image.getHeight(null));

		return image.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH);
	}

	public static BufferedImage scaleImage(final BufferedImage src, final int boundWidth, final int boundHeight) {
		final int originalWidth = src.getWidth();
		final int originalHeight = src.getHeight();
		int newWidth = originalWidth;
		int newHeight = originalHeight;

		// first check if we need to scale width
		if (originalWidth > boundWidth) {
			// scale width to fit
			newWidth = boundWidth;
			// scale height to maintain aspect ratio
			newHeight = (newWidth * originalHeight) / originalWidth;
		}

		// then check if we need to scale even with the new height
		if (newHeight > boundHeight) {
			// scale height to fit instead
			newHeight = boundHeight;
			// scale width to maintain aspect ratio
			newWidth = (newHeight * originalWidth) / originalHeight;
		}

		final BufferedImage resizedImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);
		final Graphics2D g2 = resizedImg.createGraphics();
		g2.setBackground(Color.WHITE);
		g2.clearRect(0, 0, newWidth, newHeight);
		g2.drawImage(src, 0, 0, newWidth, newHeight, null);
		g2.dispose();
		return resizedImg;
	}

	public static byte[] imageToByte(final BufferedImage bufferedImage) {
		try {
			final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
			ImageIO.write(bufferedImage, "png", outStream);
			return outStream.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Unable to convert image to byte array", e);
		}
	}

	public static BufferedImage fixImageFileSize(BufferedImage bufferedImage, int maximumFileSize) throws IOException {
		return fixImageFileSize(bufferedImage, maximumFileSize, "png");
	}

	public static BufferedImage fixImageFileSize(BufferedImage bufferedImage, int maximumFileSize, String fileType)
			throws IOException {
		long arrSize = getArraySize(bufferedImage, fileType);
		while (arrSize > maximumFileSize) {
			int newTargetSize = (bufferedImage.getTileWidth() - ((bufferedImage.getTileWidth() / 100) * 10));
			bufferedImage = Scalr.resize(bufferedImage, newTargetSize);
			arrSize = getArraySize(bufferedImage, fileType);
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, fileType, baos);
		return ImageIO.read(new ByteArrayInputStream(baos.toByteArray()));
	}	

	private static long getArraySize(BufferedImage bufferedImage, String fileType) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(bufferedImage, fileType, baos);
		baos.flush();
		byte[] byteImage = baos.toByteArray();
		long size = byteImage.length;
		baos.close();
		return size;	
	}
	
	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 */
	public static BufferedImage toBufferedImage(Image img)
	{
	    if (img instanceof BufferedImage)
	    {
	        return (BufferedImage) img;
	    }

	    // Create a buffered image with transparency
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}

}
