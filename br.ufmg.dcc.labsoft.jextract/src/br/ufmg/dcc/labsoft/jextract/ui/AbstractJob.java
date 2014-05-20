package br.ufmg.dcc.labsoft.jextract.ui;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

public abstract class AbstractJob extends Job {

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
				this.doWorkIteration(i, monitor);
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

	protected abstract void doWorkIteration(int i, IProgressMonitor monitor);

	protected String getTaskName() {
	    return "Processing";
    }

	protected int getWorkIterations() {
	    //return IProgressMonitor.UNKNOWN;
		return 1;
    }

}
