package br.ufmg.dcc.labsoft.jextract.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.internal.UISynchronizer;

public abstract class AbstractJob extends Job {

	// get UISynchronize injected as field
	public AbstractJob(String jobName) {
		super(jobName);
		setUser(true);
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		final int iterations = this.getWorkIterations();
		monitor.beginTask(this.getTaskName(), iterations);
		try {
			for (int i = 0; i < iterations; i++) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				//monitor.subTask("Processing tick #" + i);
				// ... do some work ...
				try {
					this.doWorkIteration(i, monitor);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	protected abstract void doWorkIteration(int i, IProgressMonitor monitor) throws Exception;

	protected String getTaskName() {
	    return "Processing";
    }

	protected int getWorkIterations() {
	    //return IProgressMonitor.UNKNOWN;
		return 1;
    }

}
