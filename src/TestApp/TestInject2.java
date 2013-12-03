package TestApp;

import java.util.Scanner;
/**
 * @author David Dennison
 *assignment 9: TestInject2.java
 */
public class TestInject2
{
    public static void main (String[]args)
    {
	//DROPBOXGRADERCODESTART
        java.io.FileInputStream iDropbox=null;
        java.io.PrintStream printDropbox=null;
        try{	
            int x=0;
            java.io.File f=new java.io.File("runtimeFiles\\input"+x+".log");
            while(f.exists()){
                x++;
                f=new java.io.File("runtimeFiles\\input"+x+".log");
            }
            f=new java.io.File("runtimeFiles\\input0.log");
            iDropbox=new java.io.FileInputStream(f){
            //int runNum=0; //requires everything to be written twice, for stupid reasons.
                @Override
                public int read(byte[] b, int off, int len) throws java.io.IOException {
                    int read=super.read(b, off, len);
                    while(read==-1){ //every 2nd call is for caching and doesnt matter
                        read=super.read(b, off, len);
                    }
                    return read;
                }
            };
        System.setIn(iDropbox);
        printDropbox=new java.io.PrintStream(new java.io.FileOutputStream("runtimeFiles\\output"+x+".log"));
        //System.setOut(printDropbox);
        //System.setErr(printDropbox);
        } catch(java.io.IOException e){
            System.out.println("Injection code has failed. "+e);
        }
        //DROPBOXGRADERCODEEND
        Scanner scan = new Scanner(System.in);
        System.out.println("what would you like to calculate, sphere, cone or cylinder?");
        String answer = scan.nextLine();
        if(answer.equals("sphere"))
        {
            System.out.println("please type the radius of the sphere.");
            double rSphere = scan.nextDouble();
            System.out.println("the volume is "+Geometry.sphereVolume(rSphere));
            System.out.println("the surface area is "+ Geometry.sphereSurface(rSphere));
        }
        else if (answer.equals("cone"))
        {
            System.out.println("please type the radius of the cone.");
            double rCone = scan.nextDouble();
            System.out.println("please type the height of the cone.");
            double hCone = scan.nextDouble();
            System.out.println("the volume of the cone is "+Geometry.coneVolume(rCone, hCone));
            System.out.println("the surface area of the cone is "+Geometry.coneSurface(rCone, hCone));
        }
        else if (answer.equals("cylinder"))
        {
            System.out.println("please type the radius of the cylinder.");
            double rCylinder = scan.nextDouble();
            System.out.println("please type the height of the cylinder.");
            double hCylinder = scan.nextDouble();
            System.out.println("the volume of the cylinder is "+Geometry.cylinderVolume(rCylinder, hCylinder));
            System.out.println("the surface area of the cylinder is "+Geometry.cylinderSurface(rCylinder, hCylinder));
        
        }
    }
}
/*
 * what would you like to calculate, sphere, cone or cylinder?
cylinder
please type the radius of the cylinder.
5
please type the height of the cylinder.
3
the volume of the cylinder is 235.61944901923448
the surface area of the cylinder is 175.92918860102841


what would you like to calculate, sphere, cone or cylinder?
sphere
please type the radius of the sphere.
10
the volume is 4188.79020478639
the surface area is 125.66370614359172

what would you like to calculate, sphere, cone or cylinder?
cone
please type the radius of the cone.
12
please type the height of the cone.
34
the volume of the cone is 5127.079210658542
the surface area of the cone is 1811.6501500136076

 */
