import ij.*;
import ij.gui.GenericDialog;
import ij.io.SaveDialog;
import ij.plugin.*;
import ij.process.*;
import particle.*;


import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class ParamFilter implements PlugIn{
ImagePlus Imp;
protected ImageStack stack;	

public void run(String arg) {

	
	Imp = WindowManager.getCurrentImage();
    stack=Imp.getStack();
    
    int dimz =stack.getSize();
	int dimx = stack.getWidth();
	int dimy = stack.getHeight();

	SaveDialog sd = new SaveDialog("Input Parameter File :", "param", ".dat");
        String name = sd.getFileName();
        String prefix = name;
   	if (name == null)
        	return;
    
   	String directory = sd.getDirectory();

   	ParticlesSet raw = readParamFile(directory,name);
	
   	
    IJ.log("Write some test results.\n");
    String testName = directory+ prefix + "_test.dat";
    writeParamFile(testName,raw);

   	
   	boolean keepSelection = true;
   	//String color="";
   	
   	double[] xextr = new double[2];
   	xextr[0]=0;
   	xextr[1]=dimx;
   	double[] yextr = new double[2];
   	yextr[0]=0;
   	yextr[1]=dimy;
   	double[] zextr = new double[2];
   	zextr[0]=0;
   	zextr[1]=dimz;
   	
   	double[] volumeextr = new double[2];
   	volumeextr = raw.findExtrema("getVolume");
   	double[] sphericityextr = new double[2];
   	sphericityextr = raw.findExtrema("getSphericity");
   	double[] dxextr = new double[2];
   	dxextr = raw.findExtrema("getDx");
	double[] dyextr = new double[2];
   	dyextr = raw.findExtrema("getDy");
	double[] dzextr = new double[2];
   	dzextr = raw.findExtrema("getDz");
	double[] colorextr = new double[2];
   	colorextr = raw.findExtrema("getColor");
   	double[] Fabextr = new double[2];
   	Fabextr = raw.findExtrema("getFab");
	double[] Facextr = new double[2];
   	Facextr = raw.findExtrema("getFac");
	double[] Fbcextr = new double[2];
   	Fbcextr = raw.findExtrema("getFbc");
   	boolean[] Boundextr = new boolean[2];
   	Boundextr[0]=true;
   	Boundextr[1]=true;
   	boolean recolor = true;
   	
	
 	GenericDialog dia = new GenericDialog("ParamFilter :", IJ.getInstance());
 	dia.addCheckbox("Keep_Selection", keepSelection);
 	//dia.addStringField("Color", color);
 	dia.addNumericField("x_min", xextr[0],0);
 	dia.addNumericField("x_max", xextr[1],0);
 	dia.addNumericField("y_min", yextr[0],0);
 	dia.addNumericField("y_max", yextr[1],0);
 	dia.addNumericField("z_min", zextr[0],0);
 	dia.addNumericField("z_max", zextr[1],0);
 	dia.addNumericField("Volume_min", volumeextr[0],0);
 	dia.addNumericField("Volume_max", volumeextr[1],0);
 	dia.addNumericField("Sphericity_min", sphericityextr[0],2);
 	dia.addNumericField("Sphericity_max", sphericityextr[1],2);
 	dia.addNumericField("dx_min", dxextr[0],0);
 	dia.addNumericField("dx_max", dxextr[1],0);
 	dia.addNumericField("dy_min", dyextr[0],0);
 	dia.addNumericField("dy_max", dyextr[1],0);
 	dia.addNumericField("dz_min", dzextr[0],0);
 	dia.addNumericField("dz_max", dzextr[1],0);
 	dia.addNumericField("Fab_min", Fabextr[0],2);
 	dia.addNumericField("Fab_max", Fabextr[1],2);
 	dia.addNumericField("Fac_min", Facextr[0],2);
 	dia.addNumericField("Fac_max", Facextr[1],2);
 	dia.addNumericField("Fbc_min", Fbcextr[0],2);
 	dia.addNumericField("Fbc_max", Fbcextr[1],2);
 	dia.addCheckbox("touching_boundary", Boundextr[0]);
 	dia.addCheckbox("not_touching_boundary", Boundextr[1]);
 	dia.addCheckbox("Recolor", recolor);
 	dia.showDialog();
	
	if (dia.wasCanceled()) return;
	
	if(dia.invalidNumber()) {
		IJ.showMessage("Error", "Invalid input Number");
		return;
	}

	keepSelection=dia.getNextBoolean();
	//color=dia.getNextString();
	xextr[0]=dia.getNextNumber();
	xextr[1]=dia.getNextNumber();
	yextr[0]=dia.getNextNumber();
	yextr[1]=dia.getNextNumber();
	zextr[0]=dia.getNextNumber();
	zextr[1]=dia.getNextNumber();
	volumeextr[0]=dia.getNextNumber();
	volumeextr[1]=dia.getNextNumber();
	sphericityextr[0]=dia.getNextNumber();
	sphericityextr[1]=dia.getNextNumber();
	dxextr[0]=dia.getNextNumber();
	dxextr[1]=dia.getNextNumber();
	dyextr[0]=dia.getNextNumber();
	dyextr[1]=dia.getNextNumber();
	dzextr[0]=dia.getNextNumber();
	dzextr[1]=dia.getNextNumber();
	Fabextr[0]=dia.getNextNumber();
	Fabextr[1]=dia.getNextNumber();
	Facextr[0]=dia.getNextNumber();
	Facextr[1]=dia.getNextNumber();
	Fbcextr[0]=dia.getNextNumber();
	Fbcextr[1]=dia.getNextNumber();
	Boundextr[0]=dia.getNextBoolean();
	Boundextr[1]=dia.getNextBoolean();
	recolor=dia.getNextBoolean();
	
	IJ.log("Write filtering parameters.\n");
	String paramFilterName = directory+ prefix + "_filterParamters.dat";
	writeFilteringParamFile(paramFilterName, keepSelection, xextr, yextr, zextr, volumeextr, sphericityextr, dxextr, dyextr, dzextr, Fabextr, Facextr, Fbcextr, Boundextr, recolor);

	IJ.log("Volume contains " + raw.getParticles().size() + " objects");
	ParticlesSet filtered0 = raw.subSetCenter(xextr[0], xextr[1], yextr[0], yextr[1], zextr[0], zextr[1],keepSelection);
	IJ.log("After center position filter " + filtered0.getParticles().size() + " objects remaining");
	ParticlesSet filtered1 = filtered0.subSetVolume(volumeextr[0], volumeextr[1],keepSelection);
	IJ.log("After volume filter " + filtered1.getParticles().size() + " objects remaining");
	ParticlesSet filtered2 = filtered1.subSetDbox(dxextr[0], dxextr[1], dyextr[0], dyextr[1], dzextr[0], dzextr[1],keepSelection);
	IJ.log("After box size filter " + filtered2.getParticles().size() + " objects remaining");
	ParticlesSet filtered3 = filtered2.subSetShape(Fabextr[0], Fabextr[1], Facextr[0], Facextr[1], Fbcextr[0], Fbcextr[1],keepSelection);
	IJ.log("After shape filter " + filtered3.getParticles().size() + " objects remaining");
	ParticlesSet filtered4 = filtered3.subSetBoundary(Boundextr[0], Boundextr[1],keepSelection);
	IJ.log("After boundary contact filter " + filtered4.getParticles().size() + " objects remaining");
	ParticlesSet filtered5 = filtered4.subSetSphericity(sphericityextr[0], sphericityextr[1],keepSelection);
	IJ.log("After sphericity filter " + filtered5.getParticles().size() + " objects remaining");
	ParticlesSet filtered6;
	/*
	 if(color!="")
	 
	{
		filtered6 = filtered5.subSetColor(color,keepSelection);
		IJ.log("After color filter " + filtered6.getParticles().size() + " objects remaining");
	}
	else
	{
		filtered6 = filtered5;
	}
	*/
	//write Volume
    IJ.log("Write some results.\n");
    String paramName = directory+ prefix + "_filtered.dat";
    writeParamFile(paramName,filtered5);

    ImageStack outstack;
    if(recolor)
    	outstack = filtered5.recolor(stack,(int)colorextr[1]);
    else
    	outstack = filtered5.recolor_norenumber(stack,(int)colorextr[1]);
    
    
    new ImagePlus("Filtered stack",outstack).show();    
    IJ.showStatus("Finished!!!");
	
	
}

public void writeFilteringParamFile(String FileName, boolean keepSelection, double[] xextr, double[] yextr, double[] zextr , double[]
		volumeextr, double[] sphericityextr, double[] dxextr, double[] dyextr, double[] dzextr, double[] Fabextr, double[] Facextr,
		double[] Fbcextr, boolean[] Boundextr, boolean recolor)
{
	PrintWriter pw = null;
	try
	{
		FileOutputStream fos = new FileOutputStream(FileName);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		pw = new PrintWriter(bos);
	}
	
	catch (IOException e) 
	{
		IJ.log("pb" + e);
		return;
	}
	
	pw.println("Keep_Selection " + keepSelection);
	pw.println("x_min " + xextr[0]);
	pw.println("x_max " + xextr[1]);
	pw.println("y_min " + yextr[0]);
 	pw.println("y_max " + yextr[1]);
 	pw.println("z_min " + zextr[0]);
 	pw.println("z_max " + zextr[1]);
 	pw.println("Volume_min " + volumeextr[0]);
 	pw.println("Volume_max " + volumeextr[1]);
 	pw.println("Sphericity_min " + sphericityextr[0]);
 	pw.println("Sphericity_max " + sphericityextr[1]);
 	pw.println("dx_min " + dxextr[0]);
 	pw.println("dx_max " + dxextr[1]);
 	pw.println("dy_min " + dyextr[0]);
 	pw.println("dy_max " + dyextr[1]);
 	pw.println("dz_min " + dzextr[0]);
 	pw.println("dz_max " + dzextr[1]);
 	pw.println("Fab_min " + Fabextr[0]);
 	pw.println("Fab_max " + Fabextr[1]);
 	pw.println("Fac_min " + Facextr[0]);
 	pw.println("Fac_max " + Facextr[1]);
 	pw.println("Fbc_min " + Fbcextr[0]);
 	pw.println("Fbc_max " + Fbcextr[1]);
 	pw.println("touching_boundary " + Boundextr[0]);
 	pw.println("not_touching_boundary " + Boundextr[1]);
 	pw.println("Recolor " + recolor);
	
	pw.close();
}


public void writeParamFile(String FileName, ParticlesSet subset)
{
	PrintWriter pw = null;
	try
	{
		FileOutputStream fos = new FileOutputStream(FileName);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		pw = new PrintWriter(bos);
	}
	
	catch (IOException e) 
	{
		IJ.log("pb" + e);
		return;
	}
	
	for(int j=0; j<subset.getParticles().size(); j++)
	{
		pw.println(subset.getParticles().get(j).toString());
	}
	pw.close();
}

public static void quicksortdouble(double data[][], int left, int right, int col)
{
    if(data == null || data.length < 2)
        return;
    int i = left;
    int j = right;
    double x = data[(left + right) / 2][col];
    do
    {
        while(data[i][col] > x) 
            i++;
        for(; x > data[j][col]; j--);
        if(i <= j)
        {
            double[] temp = data[i];
            data[i] = data[j];
            data[j] = temp;
            i++;
            j--;
        }
    } while(i <= j);
    if(left < j)
        quicksortdouble(data, left, j,col);
    if(i < right)
        quicksortdouble(data, i, right,col);
}




public ParticlesSet readParamFile(String filePath, String name)
{
	ParticlesSet particles = new ParticlesSet(name);
    TextReader toto = new TextReader();
    ImageProcessor ipp = toto.open(filePath+name);
    int nLin=ipp.getHeight();
    for(int i=1; i<nLin; i++)
    {
        if(!Float.isNaN(ipp.getPixelValue(1,i)))
        {
            particles.addParticle(
            		new Particle(
            				(int)ipp.getPixelValue(0, i), 
            				 new Coord((double)ipp.getPixelValue(1, i),(double)ipp.getPixelValue(2, i),(double)ipp.getPixelValue(3, i)),
            				(double)ipp.getPixelValue(4, i), 
            				(double)ipp.getPixelValue(5, i), 
            				(double)ipp.getPixelValue(6, i), 
            				(double)ipp.getPixelValue(7, i), 
            				(double)ipp.getPixelValue(8, i), 
            				(double)ipp.getPixelValue(9, i), 
            				(double)ipp.getPixelValue(10, i), 
            				(double)ipp.getPixelValue(11, i), 
            				(double)ipp.getPixelValue(12, i), 
            				(double)ipp.getPixelValue(13, i), 
            				(double)ipp.getPixelValue(14, i), 
            				(double)ipp.getPixelValue(15, i), 
            				(double)ipp.getPixelValue(16, i), 
            				(double)ipp.getPixelValue(17, i), 
            				(double)ipp.getPixelValue(18, i), 
            				(double)ipp.getPixelValue(19, i), 
            				(double)ipp.getPixelValue(20, i), 
            				(double)ipp.getPixelValue(21, i), 
            				(double)ipp.getPixelValue(22, i), 
            				(double)ipp.getPixelValue(23, i), 
            				(double)ipp.getPixelValue(24, i), 
            				(double)ipp.getPixelValue(25, i), 
            				(double)ipp.getPixelValue(26, i), 
            				(double)ipp.getPixelValue(27, i), 
            				(double)ipp.getPixelValue(28, i), 
            				(double)ipp.getPixelValue(29, i), 
            				(double)ipp.getPixelValue(30, i), 
            				(double)ipp.getPixelValue(31, i), 
            				(double)ipp.getPixelValue(32, i), 
            				(double)ipp.getPixelValue(33, i), 
            				(double)ipp.getPixelValue(34, i), 
            				(double)ipp.getPixelValue(35, i), 
            				(boolean)(ipp.getPixelValue(36, i)==1),
            				(int)0,
            				""
            				)
            		);
        }
    }
    return particles;
}

public double[][] readFile(String filePath) //OK
{
    TextReader toto = new TextReader();
    ImageProcessor ipp = toto.open(filePath);
    int nLin=ipp.getHeight();
    int nCol=ipp.getWidth();
    double[][] data =  new double[nLin][nCol];
    for(int i=0; i<nLin; i++)
    {
        for(int j=0; j<nCol; j++)
        {
            data[i][j]=(double)ipp.getPixelValue(j, i);
        }
    }
    return data;
}

public void writeFile(String filePath, double[][] values) //OK
{
	PrintWriter pw = null;
	try
	{
		FileOutputStream fos = new FileOutputStream(filePath);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		pw = new PrintWriter(bos);
	}
	
	catch (IOException e) 
	{
		IJ.log("pb" + e);
		return;
	}
	
	for(int j=0; j<values.length; j++)
	{
		for(int i=0; i<values[0].length;i++)
		{
			pw.print(values[j][i]);
		}
		pw.println("");
	}
	pw.close();
}

public void writeFile(String filePath, int[] values) //OK
{
	PrintWriter pw = null;
	try
	{
		FileOutputStream fos = new FileOutputStream(filePath);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		pw = new PrintWriter(bos);
	}
	
	catch (IOException e) 
	{
		IJ.log("pb" + e);
		return;
	}
	
	for(int j=0; j<values.length; j++)
	{
		pw.println(values[j]);
	}
	pw.close();
}


public double[][] readFile(String filePath, int a, int b) //OK
{
    TextReader toto = new TextReader();
    ImageProcessor ipp = toto.open(filePath);
    int nLin=ipp.getHeight();
    //int nCol=ipp.getWidth();
    double[][] data =  new double[nLin][b-a+1];
    for(int i=0; i<nLin; i++)
    {
        for(int j=0; j<b-a+1; j++)
        {
            data[i][j]=(double)ipp.getPixelValue(j+a, i);
        }
    }
    return data;
}

public int[] infoFile(String filePath) //OK
{
    TextReader toto = new TextReader();
    ImageProcessor ipp = toto.open(filePath);
    int nLin=ipp.getHeight();
    int nCol=ipp.getWidth();
	int[] bibi = new int[2];
    bibi[0]=nLin;
	bibi[1]=nCol;
	return bibi;
}



 


}



