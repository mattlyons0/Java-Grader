/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package Quadratic;

/**
 *
 * @author 141lyonsm
 */
public class Quadratic {
    private double a,b,c;
    public Quadratic(double a,double b,double c){
        this.a=a;
        this.b=b;
        this.c=c;
    }
    public double[] solve(){
        double solution1=((b*-1)+Math.sqrt(b*b-4*a*c))/(2*a);
        double solution2=((b*-1)-Math.sqrt(b*b-4*a*c))/(2*a);
        
        
        double[] solutions=new double[2];
        solutions[0]=solution1;
        solutions[1]=solution2;
        
        return solutions;
    }
    public void changeValues(double a,double b,double c){
        this.a=a;
        this.b=b;
        this.c=c;
    }
}
