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
package org.isf.utils.image;

import static org.assertj.core.api.Assertions.assertThat;

import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import org.isf.generaldata.GeneralData;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Disabled until native libraries are available in CI build")
class ImageUtilTest {

	@Test
	void testScaling() throws Exception {
		File file = new File(getClass().getResource("patient.jpg").getFile());
		BufferedImage image = ImageIO.read(file);
		int originalWidth = image.getWidth();
		int originalHeight = image.getHeight();
		int newWidth = GeneralData.IMAGE_THUMBNAIL_MAX_WIDTH;
		int newHeight = GeneralData.IMAGE_THUMBNAIL_MAX_WIDTH;
		BufferedImage newImage = ImageUtil.scaleImage(image, newWidth, newHeight);

		// Uncomment these next 4 lines to save the scaled image back into the same directory as the source image
		//		String path = file.getParent();
		//		path = path.replaceAll("target/test-classes/", "src/test/resources/");
		//		System.out.println("path=" + path);
		//		ImageIO.write(newImage, "jpg", new File(path + "/NEWpatient.jpg"));

		assertThat(newImage.getWidth()).isEqualTo(newWidth);
		assertThat(newImage.getHeight()).isEqualTo(newHeight);
	}

}
