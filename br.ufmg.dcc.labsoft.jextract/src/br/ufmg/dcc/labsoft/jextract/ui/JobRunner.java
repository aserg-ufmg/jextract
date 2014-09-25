package br.ufmg.dcc.labsoft.jextract.ui;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;

public class JobRunner<T> extends Job {

	public static <T> void run(String jobName, ItemProcessingJob<T> job) {
		new JobRunner<T>(jobName, job).schedule();
	}

	private ItemProcessingJob<T> job;

	// get UISynchronize injected as field
	private JobRunner(String jobName, ItemProcessingJob<T> job) {
		super(jobName);
		setUser(true);
		this.job = job;
	}

	@Override
	public IStatus run(IProgressMonitor monitor) {
		List<T> items = this.job.getItems();
		final int iterations = items.size();
		monitor.beginTask("Processing", iterations);
		try {
			for (int i = 0; i < iterations; i++) {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				//monitor.subTask("Processing tick #" + i);
				// ... do some work ...
				try {
					//this.doWorkIteration(i, monitor);
					this.job.processItem(items.get(i));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
				monitor.worked(1);
			}
			
			Display.getDefault().asyncExec(new Runnable() {
				@Override
                public void run() {
					try {
						JobRunner.this.job.updateUI();
					} catch (Exception e) {
						throw new RuntimeException(e);
					}
				}
			});
			
		} finally {
			monitor.done();
		}
		return Status.OK_STATUS;
	}

}
