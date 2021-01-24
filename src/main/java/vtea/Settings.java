/*
 * Copyright (C) 2021 SciJava
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package vtea;

/**
 *
 * @author sethwinfree
 */
public class Settings {
    
public static String DATABASE_DIRECTORY = ij.Prefs.getImageJDir() + "/VTEA/h2";
public static String LAST_DIRECTORY = ij.Prefs.getString("VTEA_LastDirectory");
public static boolean DATABASE_IN_RAM = true;
 
public static void setLastDirectory(String str){
    LAST_DIRECTORY = str;
}

public static String getLastDirectory(){
    return ij.Prefs.getString("VTEA_LastDirectory");
}

public static boolean isDatabaseInRam(){
    return DATABASE_IN_RAM;
}

public static void setDatabaseInRam(boolean b){
    DATABASE_IN_RAM = b;
}
    
        
}
