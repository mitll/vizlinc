/*
 */
package edu.mit.ll.vizlinc.ui.elements;

import edu.mit.ll.vizlinc.ui.options.VizLincPanel;
import static javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE;
import javax.swing.JDialog;
import java.awt.*;
import java.awt.event.*;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * Dialog that allows the user to configure the application before it tries to load the data.
 * 
 */
public class DataConfigDialog extends JDialog 
{
    private String typedText = null;
    private JButton saveBtn;
    private String btnString1 = "Save";
    private VizLincPanel vizLincOptPanel;

    /**
     * Returns null if the typed string was invalid;
     * otherwise, returns the string as the user entered it.
     */
    public String getValidatedText() {
        return typedText;
    }

    /** Creates the reusable dialog. */
    public DataConfigDialog(Frame aFrame) {
        super(aFrame, true);
        setTitle("VizLinc Options");

        //Create an array specifying the number of dialog buttons
        //and their text.
        Object[] options = {btnString1};//, btnString2};

        //Make this dialog display it.
        this.vizLincOptPanel = new VizLincPanel(false);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(vizLincOptPanel, BorderLayout.CENTER);
        JPanel savePanel = new JPanel();
        savePanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10));
        saveBtn = new JButton("Save");
        saveBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e)
            {
                if(vizLincOptPanel.valid())
                {
                    vizLincOptPanel.store();
                    clearAndHide();
                }
            }
        });
        savePanel.add(saveBtn, BorderLayout.CENTER);
        
        panel.add(savePanel, BorderLayout.PAGE_END);
        setContentPane(panel);

        //Handle window closing correctly.
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent we) {
                /*
                 * Instead of directly closing the window,
                 * we're going to change the JOptionPane's
                 * value property.
                 */
                    //optionPane.setValue(new Integer(JOptionPane.CLOSED_OPTION));
                    System.err.println("Tried to close window");
            }
        });

        //Ensure the text field always gets the first focus.
        addComponentListener(new ComponentAdapter() {
            public void componentShown(ComponentEvent ce) {
                //TODO:  set focus
            }
        });
    }

    /** This method clears the dialog and hides it. */
    public void clearAndHide() 
    {
        setVisible(false);
    }
}