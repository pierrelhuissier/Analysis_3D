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
import segmentation_tools_bit.*;



public class DilEro_3D implements PlugInFilter{


	int val=255;
	int nbr = 1;
	String [] operation = {"Dilatation","Erosion"};
	String [] el = {"cube","oct"};
	String op;
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
		GenericDialog dia = new GenericDialog("3D fast Erosion or Dilatation:", IJ.getInstance());
        dia.addChoice("Operation",operation,operation[1]);
		dia.addChoice("structural element",el,el[1]);
		dia.addNumericField("Number of iteration", nbr, 0);
        dia.showDialog();
		if (dia.wasCanceled()) {return;}
		op = operation[dia.getNextChoiceIndex()];
		element= el[dia.getNextChoiceIndex()];
		nbr = (int) dia.getNextNumber();
		
		DiErToolsBit toto = new DiErToolsBit(stack);
		//ImageStack stack1 = stackTools.duplicate(stack);

		if(op=="Erosion")
		{
			toto.erode(nbr, element);
			
		}
		else
		{
			toto.dilate(nbr, element);
		}
		
		ImageStack stack1 = toto.getStack();
		
		if(CREATE_NEW_IMAGE)
		{
			new ImagePlus(Source + " + " + nbr + " " + op, stack1).show();
		}
		else
		{
			imp.setStack(null,stack1);
		}
    }
	
	
}
