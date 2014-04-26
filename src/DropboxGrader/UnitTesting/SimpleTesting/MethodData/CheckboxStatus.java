/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader.UnitTesting.SimpleTesting.MethodData;

import java.util.ArrayList;

/**
 *
 * @author matt
 */
public enum CheckboxStatus {
    ALLOWED,DISALLOWED;
    public static CheckboxStatus[] getAllowed(CheckboxStatus[] arr){
        ArrayList<CheckboxStatus> allowed=new ArrayList();
        for(int i=0;i<arr.length;i++){
            if(arr[i]==ALLOWED)
                allowed.add(arr[i]);
            else
                allowed.add(null);
        }
        return allowed.toArray(arr);
    }
    public static CheckboxStatus[] getDisallowed(CheckboxStatus[] arr){
        ArrayList<CheckboxStatus> disallowed=new ArrayList();
        for(int i=0;i<arr.length;i++){
            if(arr[i]==DISALLOWED)
                disallowed.add(arr[i]);
            else
                disallowed.add(null);
        }
        return disallowed.toArray(arr);
    }
}
