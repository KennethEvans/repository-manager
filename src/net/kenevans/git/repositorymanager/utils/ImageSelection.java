package net.kenevans.git.repositorymanager.utils;

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public class ImageSelection implements Transferable
{
    private Image image = null;

    public ImageSelection(Image image) {
        this.image = image;
    }

    public DataFlavor[] getTransferDataFlavors() {
        return new DataFlavor[] {DataFlavor.imageFlavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
        return flavor.equals(DataFlavor.imageFlavor);
    }

    public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException {
        if(flavor.equals(DataFlavor.imageFlavor)) {
            return image;
        } else {
            throw new UnsupportedFlavorException(flavor);
        }
    }

}
