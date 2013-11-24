/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package DropboxGrader;

import java.util.Objects;

/**
 *
 * @author Matt
 */
public class CellLocation {
    private final String column;
    private final int row;
    public CellLocation(String columnTitle,int rowNum){
        column=columnTitle;
        row=rowNum;
    }
    public String getColumn(){
        return column;
    }
    public int getRow(){
        return row;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this.column);
        hash = 71 * hash + this.row;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final CellLocation other = (CellLocation) obj;
        if (!Objects.equals(this.column, other.column)) {
            return false;
        }
        if (this.row != other.row) {
            return false;
        }
        return true;
    }
    @Override
    public String toString(){
        return column+", "+row;
    }
}
