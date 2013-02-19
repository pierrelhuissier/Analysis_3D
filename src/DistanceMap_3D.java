/**
 * DilEro 3D 
 * version v1.1 - 06/09/2012
 * Pierre Lhuissier - Luc Salvo - Vincent Boulos 
 * SIMaP/GPM2 - Grenoble University - CNRS - France
 */


import ij.gui.*;
import ij.*;
import ij.plugin.filter.*;
import ij.process.*;
import segmentation_tools_bit.SegmentToolsBit;



public class DistanceMap_3D implements PlugInFilter{


	int nbr = 0;
	String [] el = {"cube","oct"};
	int val;
	String element;
	protected int dimZ;
	protected int dimX;
	protected int dimY;
	protected ImageStack stack;
	protected String Source;
	ImagePlus imp;
	protected boolean CREATE_NEW_IMAGE = true;

public int setup(String arg, ImagePlus imp) {
			stack=imp.getStack();
			Source = imp.getTitle();
		return DOES_8G+STACK_REQUIRED;
	}



public void run(ImageProcessor ip) 
	{	
		GenericDialog dia = new GenericDialog("3D fast Distance Map:", IJ.getInstance());
		dia.addChoice("Structural element",el,el[1]);
		dia.addNumericField("Distance Max", nbr, 0);
		dia.showDialog();
		if (dia.wasCanceled()) {return;}
		element= el[dia.getNextChoiceIndex()];
		nbr=(int)dia.getNextNumber();
		
		SegmentToolsBit toto = new SegmentToolsBit(stack);
		//ImageStack stack1 = stackTools.duplicate(stack);

		ImageStack stack1 = toto.distanceMap(stack, nbr, element);
		
		if(CREATE_NEW_IMAGE)
		{
			new ImagePlus("Distance Map of "+ Source, stack1).show();
		}
		else
		{
			imp.setStack(null,stack1);
		}
    }
	
	
}
