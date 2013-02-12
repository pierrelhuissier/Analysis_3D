import ij.ImageStack;
import ij.process.ImageProcessor;

public class Coord {

	private double x;
	private double y;
	private double z;
	
	public Coord(double x, double y , double z)
	{
		this.x=x;
		this.y=y;
		this.z=z;
	}
	
	public Coord(double[] x)
	{
		this.x=x[0];
		this.y=x[1];
		this.z=x[2];
	}
	
	public double[] getCoord()
	{
		double[] x = new double[3];
		x[0]=this.x;
		x[1]=this.y;
		x[2]=this.z;
		return x;
	}
	
	public double distXY(Coord other)
	{
		double x=this.x-other.getX();
		double y=this.y-other.getY();
		return Math.sqrt(Math.pow(x,2)+Math.pow(y,2));
	}
	
	public double distZ(Coord other)
	{
		double z=this.z-other.getZ();
		return Math.abs(z);
	}
	
	public double dist(Coord other)
	{
		double x=this.x-other.getX();
		double y=this.y-other.getY();
		double z=this.z-other.getZ();
		return Math.sqrt(Math.pow(x,2)+Math.pow(y,2)+Math.pow(z,2));
	}
	
	public String toString()
	{
		return ""+this.x+" "+this.y+" "+this.z;
	}
	

	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public double getZ() {
		return z;
	}
	public void setZ(double z) {
		this.z = z;
	}


}
