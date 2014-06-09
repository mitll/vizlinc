/*
 * 
 */
package edu.mit.ll.vizlinc.concurrency;

import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.gephi.utils.longtask.spi.LongTask;
import org.gephi.utils.progress.ProgressTicket;

/**
 * Provide utility features for long and asynchronous task execution
 */
public abstract class VizLincLongTask implements LongTask {

    private ProgressTicket progressTicket;
    protected String taskName;
    protected String progressLabel;

    /**
     * Create a task to run with the given name and progress label to display.
     * @param taskName
     * @param progressLabel 
     */
    public VizLincLongTask(String taskName, String progressLabel) {
        this.taskName = taskName;
        this.progressLabel = progressLabel;
    }
    
    /**
     Create a task to run. Use the same string for both task name and progress label.
     * @param progressLabel 
     */
    public VizLincLongTask(String progressLabel) {
        this(progressLabel, progressLabel);
    }
    
    /**
     * Hide default constructor.
     */
    private VizLincLongTask() { }
    
    
    /**
     * By default, a VizLincLongTask cannot be canceled. Override cancel() to change this behavior.
     * @return false 
     */
    @Override
    public boolean cancel() {
        return false;
    }

    @Override
    public void setProgressTicket(ProgressTicket pt) {
        progressTicket = pt;
    }

    public ProgressTicket getProgressTicket() {
        return this.progressTicket;
    }

    
    /**
     * Do the work of the task. Must be implemented by subclasses.
     */
    public abstract void execute();
    
    /**
     * Run a task and do not wait for it to complete. Run a task that will take a long time, with progress updates. 
     */
    public void run() {
        run(new VizLincLongTaskListener());
    }
    
     /**
     * Run a task that will take a long time, with progress updates.
     * @param taskListener if not null, call when the task is done. If null, a default VizLincLongTaskListener will be called that only does Progress.finish().
     */
    public void run(VizLincLongTaskListener taskListener) {
        LongTaskExecutor executor = new LongTaskExecutor(/*doInBackground*/ true, taskName);
        executor.setLongTaskListener(taskListener == null ? new VizLincLongTaskListener() : taskListener);

        executor.execute(this, new Runnable() {
            @Override
            public void run() {
                execute();
            }
        }, progressLabel, null);
    }

}
