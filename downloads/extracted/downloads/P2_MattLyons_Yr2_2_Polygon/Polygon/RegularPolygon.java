/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Polygon;

/**
 *
 * @author 141lyonsm
 */
public class RegularPolygon {
    private int numSides;
    private double sideLength; 
    private double cirR; //radius circumscribed circle
    private double inscR; // radius inscribed circle
    private int offset;
    
    public RegularPolygon(){
        numSides=3;
        sideLength=5;
        calc();
    }
    public RegularPolygon(int sides,double length){
        numSides=sides;
        sideLength=length;
        calc();
    }
    private void calc(){
        calcCir();
        calcInsc();
        offset=(int)(sideLength*numSides)*5;
    }
    private void calcCir(){
        cirR=(0.5d*sideLength)*(1/Math.sin(Math.PI/numSides));
    }
    private void calcInsc(){
        inscR=(0.5d*sideLength)*(1/Math.tan(Math.PI/numSides));
    }
    public double vertexAngle(){
        return ((numSides-2)/(double)numSides)*180;
    }
    public double perimeter(){
        return numSides*sideLength;
    }
    public double area(){
        return 0.5d*numSides*(cirR*cirR)*Math.sin((2*Math.PI)/numSides);
    }
    public int getNumSide(){
        return numSides;
    }
    public double getSideLength(){
        return sideLength;
    }
    public double getCirR(){
        return cirR;
    }
    public double getInscR(){
        return inscR;
    }
    public int[] getXVerts(){
        int[] verts=new int[numSides];
        for(int i=0;i<verts.length;i++){
            verts[i]=Math.round((float)(cirR*Math.cos(2*Math.PI*i/numSides)*10))+offset;
        }
        return verts;
    }
    public int[] getYVerts(){
        int[] verts=new int[numSides];
        for(int i=0;i<verts.length;i++){
            verts[i]=Math.round((float)(cirR*Math.sin(2*Math.PI*i/numSides)*10))+offset;
        }
        return verts;
    }
    public void setNumSides(int numSides){
        this.numSides=numSides;
        calc();
    }
    public void setSideLength(double length){
        this.sideLength=length;
        calc();
    }
    
    public String toString(){
        return "Sides: "+getNumSide()+" Length: "+getSideLength()+" Angle: "+vertexAngle()+" InscRadius: "+inscR+" CircRadius: "+cirR+" Perimeter: "+perimeter()+" Area: "+area();
    }
}
