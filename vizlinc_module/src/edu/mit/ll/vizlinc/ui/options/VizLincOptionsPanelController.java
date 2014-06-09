/*
 * 
 */
package edu.mit.ll.vizlinc.ui.options;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JComponent;
import org.netbeans.spi.options.OptionsPanelController;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;

@OptionsPanelController.TopLevelRegistration(
    categoryName = "#OptionsCategory_Name_VizLinc",
iconBase = "edu/mit/ll/vizlinc/ui/options/vizlinc_32_32.png",
keywords = "#OptionsCategory_Keywords_VizLinc",
keywordsCategory = "VizLinc")
@org.openide.util.NbBundle.Messages(
{
    "OptionsCategory_Name_VizLinc=VizLinc", "OptionsCategory_Keywords_VizLinc=VizLinc"
})

/**
 * Controller for VizLinc options panel.
 */
public final class VizLincOptionsPanelController extends OptionsPanelController
{

    private VizLincPanel panel;
    private final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean changed;

    public void update()
    {
        getPanel().load();
        changed = false;
    }

    public void applyChanges()
    {
        getPanel().store();
        changed = false;
    }

    public void cancel()
    {
        // need not do anything special, if no changes have been persisted yet
    }

    public boolean isValid()
    {
        return getPanel().valid();
    }

    public boolean isChanged()
    {
        return changed;
    }

    public HelpCtx getHelpCtx()
    {
        return null; // new HelpCtx("...ID") if you have a help set
    }

    public JComponent getComponent(Lookup masterLookup)
    {
        return getPanel();
    }

    public void addPropertyChangeListener(PropertyChangeListener l)
    {
        pcs.addPropertyChangeListener(l);
    }

    public void removePropertyChangeListener(PropertyChangeListener l)
    {
        pcs.removePropertyChangeListener(l);
    }

    private VizLincPanel getPanel()
    {
        if (panel == null)
        {
            panel = new VizLincPanel(this);
        }
        return panel;
    }

    void changed()
    {
        if (!changed)
        {
            changed = true;
            pcs.firePropertyChange(OptionsPanelController.PROP_CHANGED, false, true);
        }
        pcs.firePropertyChange(OptionsPanelController.PROP_VALID, null, null);
    }
}
