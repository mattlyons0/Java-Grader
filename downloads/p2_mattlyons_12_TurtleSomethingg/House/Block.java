
/**
 * Write a description of class Block here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Block
{
   private int height;
   private int width;
   private int offset;
   private int houses;
   private Builder build;
   
   public Block(int h, int w,int house,Builder builder){
       height=h;
       width=w;
       houses=house;
       build=builder;
       create();
    }
   public void move(int distance){
       offset+=distance;
       build.setOffset(offset);
    }
   public void create(){
       for(int x=houses;x>0;x--){
           int houseY=height/2-30;
           int streetY=height/2;
           build.house(x*50-50,houseY);
           build.street(0,streetY,width);
       }
    }
}
