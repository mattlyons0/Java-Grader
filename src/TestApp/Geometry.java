
/**
 * Write a description of class Geometry here.
 * 
 * @author David Dennison
 * assignment 9: Geometry.java
 * does basic calculations of cylnders, spheres and cones 
 */
public class Geometry
{
    /**
    *finds the volume of a sphere
    *@param takes in the radius of the sphere
    *@return returns the volume of the sphere
    */
    public static double sphereVolume(double r)
    {
        double volume= (r*r*r)*Math.PI*(4.0/3.0);
        return volume;
    }
        /**
    *finds the surface area of a sphere
    *@param takes in the radius of the sphere
    *@return returns the surface area of the sphere
    */
        public static double sphereSurface(double r)
    {
        double surface = 4.0*Math.PI* r;
        return surface;
    }
        /**
    *finds the volume of a cylinder
    *@param takes in the radius of the cylinder
    *@param takes in the hieght of the cylinder
    *@return returns the volume of the cylinder
    */
        public static double cylinderVolume(double r, double h)
    {
        double volume = (r*r)*Math.PI*h;
        return volume;
    }
    /**
    *finds the surface area of a cylinder
    *@param takes in the radius of the cylinder
    *@param takes in the hieght of the cylinder
    *@return returns the surface area of the cylinder
    */
        public static double cylinderSurface(double r, double h)
    {
        double surface =  (2*Math.PI*h)+(2*Math.PI*(r*r));
        return surface;
    }
        /**
    *finds the volume of a cone
    *@param takes in the radius of the cone
    *@param takes in the hieght of the cone
    *@return returns the volume of the cone
    */
        public static double coneVolume(double r, double h)
    {
        double volume = (1./3.)*(Math.PI*(r*r))*h;
        return volume;
    }
        /**
    *finds the surface area of a cone
    *@param takes in the radius of the cone
    *@param takes in the hieght of the cone
    *@return returns the surface area of the cone
    */
        public static double coneSurface(double r, double h)
    {
        double surface = ((Math.sqrt((h*h)+(r*r)))*Math.PI*r)+Math.PI*(r*r);
        return surface;
    }
}