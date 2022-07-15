package org.grits.toolbox.ms.annotation.process;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;
import org.grits.toolbox.ms.annotation.gelato.AnalyteStructureAnnotation;
import org.grits.toolbox.widgets.progress.CancelableThread;
import org.grits.toolbox.widgets.progress.IProgressHandler;
import org.grits.toolbox.widgets.progress.IProgressListener;
import org.grits.toolbox.widgets.progress.IProgressThreadHandler;
import org.grits.toolbox.widgets.tools.GRITSProcessStatus;
import org.grits.toolbox.widgets.tools.GRITSWorker;

public class GelatoWorker extends GRITSWorker {
	private static final Logger logger = Logger.getLogger(GelatoWorker.class);
	protected AnalyteStructureAnnotation gsa = null;

	protected IProgressThreadHandler progressThreadHandler;
	protected IProgressHandler progressHandler;
	
	public GelatoWorker() {
		// TODO Auto-generated constructor stub
	}
	
	public GelatoWorker(AnalyteStructureAnnotation gsa, IProgressHandler progressHandler,
			IProgressThreadHandler progressThreadHandler, 
			List<IProgressListener> progressMajors, List<IProgressListener> progressStructureAnnotationMinors, 
			List<IProgressListener> progressGlycanMatcherMinors) {
		this.gsa = gsa;
		this.progressThreadHandler = progressThreadHandler;
		setProgressListeners(progressMajors);
		this.progressHandler = progressHandler;
		gsa.setProgressListeners(progressStructureAnnotationMinors);
		gsa.setMatcherProgressListener(progressGlycanMatcherMinors);
	}

	@Override
	public int doWork() {
		try{
			setMaxValue(3);
			updateListeners("Initializing...", 0);
			CancelableThread s1 = getInitializeProcess();
			int iRes = runProcess(s1);
			if (s1.isCanceled()) // check this way since the iRes may show OK although the process is canceled
				return GRITSProcessStatus.CANCEL;
			if( iRes != GRITSProcessStatus.OK ) {
				return iRes;
			}
			
			updateListeners("Populating glycan database...", 1);
			CancelableThread s2 = getPopulateGlycanObjectsProcess();
			runProcess(s2);
			if (s2.isCanceled()) // check this way since the iRes may show OK although the process is canceled
				return GRITSProcessStatus.CANCEL;
			if( iRes != GRITSProcessStatus.OK) {
				return iRes;
			}
			
			updateListeners("Applying database filters...", 1);
			CancelableThread s3 = getApplyFiltersProcess();
			runProcess(s3);
			if (s3.isCanceled()) // check this way since the iRes may show OK although the process is canceled
				return GRITSProcessStatus.CANCEL;
			if( iRes != GRITSProcessStatus.OK ) {
				return iRes;
			}
			
			updateListeners("Performing glycan annotation...", 2);
			CancelableThread s4 = getProcessScansProcess();
			iRes = runProcess(s4);
			if (s4.isCanceled()) // check this way since the iRes may show OK although the process is canceled
				return GRITSProcessStatus.CANCEL;

			updateListeners("Done!", 3);
			return iRes;
		}catch(Exception e){
			logger.error("Error in annotateGlycanStructure", e);
		}
		return GRITSProcessStatus.ERROR;
	}

	protected int runProcess( CancelableThread cp ) {
		try {
			cp.setProgressThreadHandler(progressThreadHandler); // override the thread's default handler with the progress handler
			progressHandler.setThread(cp); // make sure the progresshandler has the thread so it can be notified upon cancel/finish
			cp.start();	
			while ( ! cp.isCanceled() && ! cp.isFinished() && cp.isAlive() ) 
			{
				if (!Display.getDefault().readAndDispatch()) 
				{
			//		Display.getDefault().sleep();
				}
			}
			if( cp.isCanceled() ) {
				gsa.setCanceled(true);
				cp.interrupt();
				return GRITSProcessStatus.CANCEL;
			} else {
				return GRITSProcessStatus.OK;
			}			
		} catch( Exception ex ) {
			logger.error("Error in runProcess", ex);			
		}
		return GRITSProcessStatus.ERROR;
	}

	protected CancelableThread getInitializeProcess() {
		try {
			CancelableThread cp = new CancelableThread() {
				@Override
				public boolean threadStart(IProgressThreadHandler a_progressThreadHandler) throws Exception {
					logger.debug("Starting job: getInitializeProcess");
					try {
						gsa.initialize();
						return true;
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						return false;
					}
				}
			};
			return cp;
		} catch( Exception ex ) {
			logger.error("Error in getScanBoundsProcess", ex);			
		}
		return null;
	}

	protected CancelableThread getPopulateGlycanObjectsProcess() {
		try {
			CancelableThread cp = new CancelableThread() {
				@Override
				public boolean threadStart(IProgressThreadHandler a_progressThreadHandler) throws Exception {
					logger.debug("Starting job: getPopulateGlycanObjectsProcess");
					try {
						gsa.populateGelatoAnalyteObjects();
						return true;
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						return false;
					}
				}
			};
			return cp;
		} catch( Exception ex ) {
			logger.error("Error in getPopulateGlycanObjectsProcess", ex);			
		}
		return null;
	}
	
	protected CancelableThread getApplyFiltersProcess() {
		try {
			CancelableThread cp = new CancelableThread() {
				@Override
				public boolean threadStart(IProgressThreadHandler a_progressThreadHandler) throws Exception {
					logger.debug("Starting job: getApplyFiltersProcess");
					try {
						gsa.applyFilters();
						return true;
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						return false;
					}
				}
			};
			return cp;
		} catch( Exception ex ) {
			logger.error("Error in getApplyFiltersProcess", ex);			
		}
		return null;
	}

	protected CancelableThread getProcessScansProcess() {
		try {
			CancelableThread cp = new CancelableThread() {
				@Override
				public boolean threadStart(IProgressThreadHandler a_progressThreadHandler) throws Exception {
					logger.debug("Starting job: processScans");
					try {
						int iRes = gsa.processScans();
						if (iRes == GRITSProcessStatus.ERROR) {
							logger.info("An error has occurred during processing scans");
						}
						return true;
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						return false;
					}
				}
			};
			return cp;
		} catch( Exception ex ) {
			logger.error("Error in getProcessScansProcess", ex);			
		}
		return null;
	}	
}
