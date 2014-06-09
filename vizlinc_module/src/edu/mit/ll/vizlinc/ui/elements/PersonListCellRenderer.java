/*
 * 
 */
package edu.mit.ll.vizlinc.ui.elements;

import edu.mit.ll.vizlinc.model.PersonValue;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 * Cell renderer for person list.
 */
public class PersonListCellRenderer extends DefaultListCellRenderer
{

    private static Font shortNamesFont;

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus)
    {
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
         String createdBy = ((PersonValue)value).getPersonEntity().getCreatedBy();
        if(createdBy.equals("weak_across_doc_person_coref"))
        {
            if(shortNamesFont == null)
            {
                initShortNamesFont(list.getFont());
            }
            setFont(list.getFont().deriveFont(Font.ITALIC));
            setForeground(Color.GRAY);
        }
        else
        {
            setFont(list.getFont());
            setForeground(Color.BLACK);
        }
        setText(value.toString());
        return this;
    }

    private void initShortNamesFont(Font font)
    {
        shortNamesFont = font.deriveFont(Font.ITALIC);
    }
}