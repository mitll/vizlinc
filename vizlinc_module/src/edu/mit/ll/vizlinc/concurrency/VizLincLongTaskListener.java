/*
 * 
 */
package edu.mit.ll.vizlinc.concurrency;

import javax.swing.SwingUtilities;
import org.gephi.utils.longtask.api.LongTaskListener;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;

/**
 * Listener of VizLincLongTask(s)
 */
public class VizLincLongTaskListener implements LongTaskListener
{
    public void whenDone() {
        // Do nothing; subclass may override this.
    }
    
    @Override
    public void taskFinished(LongTask lt)
    {
        final ProgressTicket pt = ((VizLincLongTask)lt).getProgressTicket(); 
        SwingUtilities.invokeLater(new Runnable() 
        {
            @Override
            public void run()
            {
                Progress.finish(pt);
                whenDone();
            }
        });
    }
}
