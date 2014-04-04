/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.UnitTesting.MethodData;

/**
 *
 * @author matt
 */
public class MethodModifiers {
    public boolean staticMod;
    public boolean finalMod;
    public boolean abstractMod;
    public boolean synchronizedMod;
    
    public MethodModifiers(){
        
    }
    @Override
    public String toString(){
        String mods="";
        mods+=staticMod? "static ":"";
        mods+=finalMod? "final ":"";
        mods+=abstractMod? "abstract ":"";
        mods+=synchronizedMod? "synchronized ":"";
        
        return mods;
    }
}
