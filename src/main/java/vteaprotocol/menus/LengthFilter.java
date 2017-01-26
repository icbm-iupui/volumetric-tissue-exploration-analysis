/* 
 * Copyright (C) 2016 Indiana University
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
package vteaprotocol.menus;

import java.awt.Toolkit;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

/**
 *
 * @author vinfrais
 * 
 * from: http://www.coderanch.com/t/434129/GUI/java/Reg-Validatin-JTextFields
 */

public class LengthFilter extends DocumentFilter {  
  
    private int max;  
  
    public LengthFilter(int maxLength) {  
        max = maxLength;  
    }  
  
    public void insertString(DocumentFilter.FilterBypass fb, int offset,  
            String text, AttributeSet attr) throws BadLocationException {  
        if (fb.getDocument().getLength() + text.length() <= max)  
               fb.insertString(offset, text, attr);  
          else Toolkit.getDefaultToolkit().beep();  
    }  
  
    // no need to override remove(): inherited implementation allows all removals  
  
    public void replace(DocumentFilter.FilterBypass fb, int offset, int length,  
            String text, AttributeSet attr) throws BadLocationException {  
        if (fb.getDocument().getLength() + text.length() - length <= max)  
               fb.replace(offset, length, text, attr);  
          else Toolkit.getDefaultToolkit().beep();  
    }  
}  
