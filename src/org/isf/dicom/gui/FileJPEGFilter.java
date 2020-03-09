package org.isf.dicom.gui;

import java.io.File;

import javax.swing.filechooser.FileView;
  
/**
 * Filter for file DICOM
 * @author Pietro Castellucci
 * @version 1.0.0
 */
public class FileJPEGFilter
        extends javax.swing.filechooser.FileFilter
{
     /** 
     * Whether the given file is accepted by this filter.
     */
    public boolean accept(File f)
    {

        return  (f.getName().toLowerCase().endsWith(".jpg") ||
        		f.getName().toLowerCase().endsWith(".jpeg"));
    }

    /**
     * The description of this filter. For example: "JPG and JPEG Images"
     * @see FileView#getName
     */
    public String getDescription()
    {
        return "JPEG Images";
    }
}
