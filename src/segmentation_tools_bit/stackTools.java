/***
 * Image/J Plugins
 * Copyright (C) 2008 Pierre Lhuissier
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */

package segmentation_tools_bit;

import ij.ImageStack;


/**
 *  Tools for 3D.
 *
 *@author     Lhuissier Pierre
 *@created    September 30, 2008
 *@version    $1.0 $
 */
public class stackTools {

    /**  Default constructor*/
  private stackTools() { }
    
   /**
   *  Create new ImageStack of the same type and size as the input image.
   *
   *@param  src  Input ImageStack.
   *@return      Duplicate of <code>src</code> without copying voxel values.
   */
  public static ImageStack duplicateEmpty(ImageStack src) {
    int xSize = src.getWidth();
    int ySize = src.getHeight();
    int zSize = src.getSize();

    ImageStack dest = new ImageStack(xSize, ySize);
    for (int z = 1; z <= zSize; ++z) {
      dest.addSlice(src.getSliceLabel(z),
          src.getProcessor(z).createProcessor(xSize, ySize));
    }

    dest.setColorModel(src.getColorModel());

    return dest;
  }

    /**
   *  Duplicate the input ImageStack.
   *
   *@param  src  Input ImageStack.
   *@return      Duplicate of <code>src</code>.
   */
  public static ImageStack duplicate(ImageStack src) {
    int xSize = src.getWidth();
    int ySize = src.getHeight();
    int zSize = src.getSize();

    ImageStack dest = new ImageStack(xSize, ySize);
    for (int z = 1; z <= zSize; ++z) {
      dest.addSlice(src.getSliceLabel(z),
          src.getProcessor(z).duplicate());
    }

    dest.setColorModel(src.getColorModel());

    return dest;
  }

}  

