package particle;


public class Particle {
	
	private int color;
	private Coord coord;
	private double volume;
	private double volumemarch;
	private double surfacemarch;
	private double surfacemarchnb;
	private double sphericity;
	private double I1;
	private double I2;
	private double I3;
	private double vI1x;
	private double vI1y;
	private double vI1z;
	private double vI2x;
	private double vI2y;
	private double vI2z;
	private double vI3x;
	private double vI3y;
	private double vI3z;
	private double a;
	private double b;
	private double c;
	private double Fab;
	private double Fac;
	private double Fbc;
	private double xmin;
	private double xmax;
	private double ymin;
	private double ymax;
	private double zmin;
	private double zmax;
	private double dx;
	private double dy;
	private double dz;
	private boolean boundary;
	private int globalcolor;
	//protected final Thread[] threads= newThreadArray();
	private String param;
	
	public Particle(
			int color,
			Coord coord,
			double volume,
			double volumemarch,
			double surfacemarch,
			double surfacemarchnb,
			double sphericity,
			double I1,
			double I2,
			double I3,
			double vI1x,
			double vI1y,
			double vI1z,
			double vI2x,
			double vI2y,
			double vI2z,
			double vI3x,
			double vI3y,
			double vI3z,
			double a,
			double b,
			double c,
			double Fab,
			double Fac,
			double Fbc,
			double xmin,
			double xmax,
			double ymin,
			double ymax,
			double zmin,
			double zmax,
			double dx,
			double dy,
			double dz,
			boolean boundary,
			int globalcolor,
			String param
			) {
		super();
		this.color=color;
		this.coord = coord;
		this.volume = volume;
		this.volumemarch=volumemarch;
		this.surfacemarch=surfacemarch;
		this.surfacemarchnb=surfacemarchnb;
		this.sphericity=sphericity;
		this.I1=I1;
		this.I2=I2;
		this.I3=I3;
		this.vI1x=vI1x;
		this.vI1y=vI1y;
		this.vI1z=vI1z;
		this.vI2x=vI2x;
		this.vI2y=vI2y;
		this.vI2z=vI2z;
		this.vI3x=vI3x;
		this.vI3y=vI3y;
		this.vI3z=vI3z;
		this.a=a;
		this.b=b;
		this.c=c;
		this.Fab=Fab;
		this.Fac=Fac;
		this.Fbc=Fbc;
		this.xmin=xmin;
		this.xmax=xmax;
		this.ymin=ymin;
		this.ymax=ymax;
		this.zmin=zmin;
		this.zmax=zmax;
		this.dx=dx;
		this.dy=dy;
		this.dz=dz;
		this.boundary=boundary;
		this.globalcolor=globalcolor;
		this.param = param;
 	}
	
	public Particle()
	{
		super();
	}
	
	public double getVolumemarch() {
		return volumemarch;
	}

	public void setVolumemarch(double volumemarch) {
		this.volumemarch = volumemarch;
	}

	public double getSurfacemarch() {
		return surfacemarch;
	}

	public void setSurfacemarch(double surfacemarch) {
		this.surfacemarch = surfacemarch;
	}

	public double getSurfacemarchnb() {
		return surfacemarchnb;
	}

	public void setSurfacemarchnb(double surfacemarchnb) {
		this.surfacemarchnb = surfacemarchnb;
	}

	public double getI1() {
		return I1;
	}

	public void setI1(double i1) {
		I1 = i1;
	}

	public double getI2() {
		return I2;
	}

	public void setI2(double i2) {
		I2 = i2;
	}

	public double getI3() {
		return I3;
	}

	public void setI3(double i3) {
		I3 = i3;
	}

	public double getvI1x() {
		return vI1x;
	}

	public void setvI1x(double vI1x) {
		this.vI1x = vI1x;
	}

	public double getvI1y() {
		return vI1y;
	}

	public void setvI1y(double vI1y) {
		this.vI1y = vI1y;
	}

	public double getvI1z() {
		return vI1z;
	}

	public void setvI1z(double vI1z) {
		this.vI1z = vI1z;
	}

	public double getvI2x() {
		return vI2x;
	}

	public void setvI2x(double vI2x) {
		this.vI2x = vI2x;
	}

	public double getvI2y() {
		return vI2y;
	}

	public void setvI2y(double vI2y) {
		this.vI2y = vI2y;
	}

	public double getvI2z() {
		return vI2z;
	}

	public void setvI2z(double vI2z) {
		this.vI2z = vI2z;
	}

	public double getvI3x() {
		return vI3x;
	}

	public void setvI3x(double vI3x) {
		this.vI3x = vI3x;
	}

	public double getvI3y() {
		return vI3y;
	}

	public void setvI3y(double vI3y) {
		this.vI3y = vI3y;
	}

	public double getvI3z() {
		return vI3z;
	}

	public void setvI3z(double vI3z) {
		this.vI3z = vI3z;
	}

	public double getA() {
		return a;
	}

	public void setA(double a) {
		this.a = a;
	}

	public double getB() {
		return b;
	}

	public void setB(double b) {
		this.b = b;
	}

	public double getC() {
		return c;
	}

	public void setC(double c) {
		this.c = c;
	}

	public double getFab() {
		return Fab;
	}

	public void setFab(double fab) {
		Fab = fab;
	}

	public double getFac() {
		return Fac;
	}

	public void setFac(double fac) {
		Fac = fac;
	}

	public double getFbc() {
		return Fbc;
	}

	public void setFbc(double fbc) {
		Fbc = fbc;
	}

	public double getXmin() {
		return xmin;
	}

	public void setXmin(double xmin) {
		this.xmin = xmin;
	}

	public double getXmax() {
		return xmax;
	}

	public void setXmax(double xmax) {
		this.xmax = xmax;
	}

	public double getYmin() {
		return ymin;
	}

	public void setYmin(double ymin) {
		this.ymin = ymin;
	}

	public double getYmax() {
		return ymax;
	}

	public void setYmax(double ymax) {
		this.ymax = ymax;
	}

	public double getZmin() {
		return zmin;
	}

	public void setZmin(double zmin) {
		this.zmin = zmin;
	}

	public double getZmax() {
		return zmax;
	}

	public void setZmax(double zmax) {
		this.zmax = zmax;
	}

	public double getDx() {
		return dx;
	}

	public void setDx(double dx) {
		this.dx = dx;
	}

	public double getDy() {
		return dy;
	}

	public void setDy(double dy) {
		this.dy = dy;
	}

	public double getDz() {
		return dz;
	}

	public void setDz(double dz) {
		this.dz = dz;
	}

	public int getGlobalcolor() {
		return globalcolor;
	}

	public void setGlobalcolor(int globalcolor) {
		this.globalcolor = globalcolor;
	}

	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}


	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public Coord getCoord() {
		return coord;
	}

	public void setCoord(Coord coord) {
		this.coord = coord;
	}

	public double getVolume() {
		return volume;
	}

	public void setVolume(double volume) {
		this.volume = volume;
	}

	public boolean isBoundary() {
		return boundary;
	}

	public void setBoundary(boolean boundary) {
		this.boundary = boundary;
	}

	public double getSphericity() {
		return sphericity;
	}

	public void setSphericity(double sphericity) {
		this.sphericity = sphericity;
	}
	
	
	public int getGlobalColor() {
		return globalcolor;
	}

	public void setGlobalColor(int globalcolor) {
		this.globalcolor = globalcolor;
	}

	public String toString() {
		String s="";
		s+=this.color +" ";
		s+=this.coord.toString() +" ";
		s+=this.volume +" ";
		s+=this.volumemarch +" ";
		s+=this.surfacemarch +" ";
		s+=this.surfacemarchnb +" ";
		s+=this.sphericity +" ";
		s+=this.I1+" ";
		s+=this.I2 +" ";
		s+=this.I3 +" ";
		s+=this.vI1x +" ";
		s+=this.vI1y +" ";
		s+=this.vI1z +" ";
		s+=this.vI2x +" ";
		s+=this.vI2y +" ";
		s+=this.vI2z +" ";
		s+=this.vI3x +" ";
		s+=this.vI3y +" ";
		s+=this.vI3z +" ";
		s+=this.a +" ";
		s+=this.b +" ";
		s+=this.c +" ";
		s+=this.Fab +" ";
		s+=this.Fac +" ";
		s+=this.Fbc +" ";
		s+=this.xmin +" ";
		s+=this.xmax +" ";
		s+=this.ymin +" ";
		s+=this.ymax +" ";
		s+=this.zmin +" ";
		s+=this.zmax +" ";
		s+=this.dx +" ";
		s+=this.dy +" ";
		s+=this.dz +" ";
		s+=this.boundary +" ";
		s+=this.globalcolor +" ";
		s+=this.param ;
		return s;
	}
	


	
	public boolean equalColor(Particle part) {
		if(part!=null)
			return (this.color == part.getColor());
		return false;
	}
	
	
	 
	   
	  	
}
