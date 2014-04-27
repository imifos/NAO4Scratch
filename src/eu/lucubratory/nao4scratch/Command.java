/*****************************************************************************
* NAO4Scratch is free software: you can redistribute it and/or modify 
* it under the terms of the GNU Lesser General Public License as published by 
* the Free Software Foundation, either version 3 of the License, or 
* (at your option) any later version. 
* 
* NAO4Scratch is distributed in the hope that it will be useful, 
* but WITHOUT ANY WARRANTY; without even the implied warranty of 
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
* GNU Lesser General Public License for more details. 
* 
* A copy of the GNU Lesser General Public License can be found here:
* http://www.gnu.org/licenses/
*****************************************************************************/

package eu.lucubratory.nao4scratch;

/**
 * Holds a Command - Scratch version and NAO version.
 * <p/>
 * The Scratch raw command is a class GET request and the NAO command format 
 * is the following String "uid#command#param1#param2#...#paramN$".
 */
public class Command {
    
    // Raw SCRATCH 2 GET request command
    public String scratchRaw;
    
    // NAO Command structure
    public String naoCommand=null;
    public String naoBlockingUid;
    
    public boolean hasNaoCommand() {
        return naoCommand != null;
    }
}
