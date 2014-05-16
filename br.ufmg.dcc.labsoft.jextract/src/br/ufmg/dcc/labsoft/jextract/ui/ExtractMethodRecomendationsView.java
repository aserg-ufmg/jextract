package br.ufmg.dcc.labsoft.jextract.ui;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;

import br.ufmg.dcc.labsoft.jextract.evaluation.ProjectRelevantSet;
import br.ufmg.dcc.labsoft.jextract.ranking.EmrFileExporter;
import br.ufmg.dcc.labsoft.jextract.ranking.EmrRankFileExporter;
import br.ufmg.dcc.labsoft.jextract.ranking.EmrScoringFn;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice.Fragment;
import br.ufmg.dcc.labsoft.jextract.ranking.Utils;


public class ExtractMethodRecomendationsView extends ViewPart {

	public static final String ID = "br.ufmg.dcc.labsoft.jextract.ui.ExtractMethodRecomendationsView";

	private TableViewer viewer;
	private Action action1;
	private Action action2;
	private Action action3;
	private Action toggleGroupBy;
	private Action doubleClickAction;
	private List<ExtractMethodRecomendation> recomendations;
	Listener sortListener;
	private boolean groupByMethod = false;

	public ExtractMethodRecomendationsView() {
		this.recomendations = Collections.emptyList();
		
		this.sortListener = new Listener() {
			public void handleEvent(Event e) {
				TableColumn column = (TableColumn) e.widget;
				EmrScoringFn scoringFn = EmrScoringFn.valueOf(column.getText());
				if (scoringFn != null) {
					Utils.sort(recomendations, scoringFn, groupByMethod);
				}
				viewer.getTable().setSortColumn(column);
				viewer.setInput(recomendations);
			}
		};
	}

	public void setRecomendations(List<ExtractMethodRecomendation> recomendations, IProject project) {
		this.recomendations = recomendations;
		ProjectRelevantSet set = new ProjectRelevantSet(project.getLocation().toString() + "/goldset.txt");
		for (ExtractMethodRecomendation rec : recomendations) {
			rec.setRelevant(set.contains(rec));
			rec.setSimilar(set.containsReduced(rec));
			rec.setAvailableInGoldSet(set.isMethodAvailable(rec));
		}
		this.viewer.setInput(recomendations);
	}

	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		
		addColumnRank();
		TableViewerColumn colId = addColumnId();
		colId.getColumn().addListener(SWT.Selection, sortListener);
		addColumnOk();
		addColumnClass();
		addColumnMethodName();
		addColumnOriginalSize().getColumn();
		addColumnExtractedSize();
//		addColumnDuplicatedSize();
		
		addColumnScore(EmrScoringFn.KUL_TVM).getColumn();
		addColumnScore(EmrScoringFn.SCORE).getColumn();
//		addColumnScore(EmrScoringFn.PSC_TVM).getColumn();
//		addColumnScore(EmrScoringFn.PJACD_T).getColumn();
//		addColumnScore(EmrScoringFn.PKULD_T).getColumn();
//		addColumnScore(EmrScoringFn.PPSCD_T).getColumn();
//		addColumnScore(EmrScoringFn.PJACD_T_PJACD_V).getColumn();
//		addColumnScore(EmrScoringFn.PKULD_T_PKULD_V).getColumn();
//		addColumnScore(EmrScoringFn.PPSCD_T_PPSCD_V).getColumn();
//		addColumnScore(EmrScoringFn.BAL_JACD_T_JACD_V).getColumn();
//		addColumnScore(EmrScoringFn.JACD_T).getColumn();
//		addColumnScore(EmrScoringFn.P_T).getColumn();
//		addColumnScore(EmrScoringFn.P_V).getColumn();
		addColumnExplanation();

		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private TableViewerColumn addColumn(String header, EmrTableColumnLabelProvider labelProvider, int width) {
		TableViewerColumn column = new TableViewerColumn(viewer, SWT.LEFT);
		column.getColumn().setText(header);
		column.getColumn().setResizable(true);
		column.getColumn().setWidth(width);
		column.setLabelProvider(labelProvider);
		return column;
	}
	
	private TableViewerColumn addColumnRank() {
		return addColumn("Rank", new EmrTableColumnLabelProvider() {
			@Override
			public String getColumnText(ExtractMethodRecomendation element) {
				return element.rank + "";
			}
		}, 50);
	}

	private TableViewerColumn addColumnId() {
		return addColumn("ID", new EmrTableColumnLabelProvider() {
			@Override
			public String getColumnText(ExtractMethodRecomendation element) {
				return element.id + "";
			}
		}, 50);
	}

	private TableViewerColumn addColumnClass() {
		return addColumn("Class", new EmrTableColumnLabelProvider() {
			@Override
			public String getColumnText(ExtractMethodRecomendation element) {
				return element.className;
			}
		}, 200);
	}

	private TableViewerColumn addColumnMethodName() {
		return addColumn("Method", new EmrTableColumnLabelProvider() {
			@Override
			public String getColumnText(ExtractMethodRecomendation element) {
				return element.method;
			}
		}, 300);
	}

	private TableViewerColumn addColumnOriginalSize() {
		return addColumn("Original", new EmrTableColumnLabelProvider() {
			@Override
			public String getColumnText(ExtractMethodRecomendation element) {
				return element.getOriginalSize() + "";
			}
		}, 100);
	}

	private TableViewerColumn addColumnExtractedSize() {
		return addColumn("Extracted", new EmrTableColumnLabelProvider() {
			@Override
			public String getColumnText(ExtractMethodRecomendation element) {
				return element.getExtractedSize() + "";
			}
		}, 100);
	}

	private TableViewerColumn addColumnDuplicatedSize() {
		return addColumn("Duplicated", new EmrTableColumnLabelProvider() {
			@Override
			public String getColumnText(ExtractMethodRecomendation element) {
				return element.getDuplicatedSize() + "";
			}
		}, 100);
	}

	private TableViewerColumn addColumnScore(final EmrScoringFn scoringFn) {
		TableViewerColumn col = addColumn(scoringFn.toString(), new EmrTableColumnLabelProvider() {
			@Override
			public String getColumnText(ExtractMethodRecomendation element) {
				return String.format("%s", Double.toString(scoringFn.score(element)));
			}
		}, 90);
		col.getColumn().addListener(SWT.Selection, sortListener);
		return col;
	}

	private TableViewerColumn addColumnOk() {
		return addColumn("Ok", new EmrTableColumnLabelProvider() {
			@Override
			public String getColumnText(ExtractMethodRecomendation element) {
				return element.isOk() ? "ok" : "";
			}
		}, 60);
	}

	private TableViewerColumn addColumnExplanation() {
		return addColumn("Explanation", new EmrTableColumnLabelProvider() {
			@Override
			public String getColumnText(ExtractMethodRecomendation element) {
				return element.getExplanation();
			}
		}, 2000);
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ExtractMethodRecomendationsView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		//manager.add(action2);
		manager.add(new Separator());
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(action3);
		manager.add(toggleGroupBy);
		manager.add(new Separator());
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showExplanation((ExtractMethodRecomendation) obj);
			}
		};
		action1.setText("Show details");
		action1.setToolTipText("Show details");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		action2 = new Action() {
			public void run() {
				String outputPath = "E:/Danilo/Temp/out.txt";
				new EmrFileExporter(recomendations, outputPath).export();
				showMessage(String.format("Data saved at %s", outputPath));
			}
		};
		action2.setText("Save to file");
		action2.setToolTipText("Export results as a text file");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_ETOOL_SAVEAS_EDIT));

		action3 = new Action() {
			public void run() {
				String baseFolder = "E:/Danilo/Temp/";
				EmrRankFileExporter.exportAll(recomendations, baseFolder);
				showMessage(String.format("Data saved at %s", baseFolder));
			}
		};
		action3.setText("Export all");
		action3.setToolTipText("Export results for comparison");
		action3.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_ETOOL_SAVEALL_EDIT));
		
		toggleGroupBy = new Action("Toggle group by method", Action.AS_CHECK_BOX) {
			public void run() {
				groupByMethod = !groupByMethod;
			}
		};
		toggleGroupBy.setToolTipText("Group recomendations by method");
		toggleGroupBy.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_ELCL_COLLAPSEALL));
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showRefactoringDetails((ExtractMethodRecomendation) obj);
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	private void showMessage(String message) {
		MessageDialog.openInformation(
			viewer.getControl().getShell(),
			"Extract Method Recomendations",
			message);
	}

	private void showRefactoringDetails(ExtractMethodRecomendation refactoring) {
		try {
			IFile sourceFile = (IFile) refactoring.getSourceFile().getUnderlyingResource();
			IJavaElement sourceJavaElement = JavaCore.create(sourceFile);
			ITextEditor sourceEditor = (ITextEditor) JavaUI.openInEditor(sourceJavaElement);

			// limpa as anotações
			sourceFile.deleteMarkers("br.ufmg.dcc.labsoft.jextract.extractionslice", true, IResource.DEPTH_ONE);
			sourceFile.deleteMarkers("br.ufmg.dcc.labsoft.jextract.extractionslicedup", true, IResource.DEPTH_ONE);

			Fragment[] fragments = refactoring.getSlice().getFragments();
			int firstChar = Integer.MAX_VALUE;
			int lastChar = 0;
			for (Fragment frag : fragments) {
				firstChar = Math.min(firstChar, frag.start);
				lastChar = Math.max(lastChar, frag.end);
				IMarker marker = frag.duplicate ? sourceFile.createMarker("br.ufmg.dcc.labsoft.jextract.extractionslicedup")
						                        : sourceFile.createMarker("br.ufmg.dcc.labsoft.jextract.extractionslice");
				marker.setAttribute(IMarker.CHAR_START, frag.start);
				marker.setAttribute(IMarker.CHAR_END, frag.end);
			}

			sourceEditor.setHighlightRange(firstChar, lastChar - firstChar, true);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
	
	private void showExplanation(ExtractMethodRecomendation emr) {
		showMessage(emr.getExplanationDetails());
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

}
