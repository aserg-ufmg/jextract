package br.ufmg.dcc.labsoft.jextract.ui;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.corext.refactoring.code.ExtractMethodRefactoring;
import org.eclipse.jdt.internal.ui.refactoring.RefactoringMessages;
import org.eclipse.jdt.internal.ui.refactoring.actions.RefactoringStarter;
import org.eclipse.jdt.internal.ui.refactoring.code.ExtractMethodWizard;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.refactoring.RefactoringSaveHelper;
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

import br.ufmg.dcc.labsoft.jextract.generation.EmrScoringFunction;
import br.ufmg.dcc.labsoft.jextract.generation.Settings;
import br.ufmg.dcc.labsoft.jextract.ranking.EmrFileExporter;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractMethodRecomendation;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice.Fragment;
import br.ufmg.dcc.labsoft.jextract.ranking.Utils;


public class ExtractMethodRecomendationsView extends ViewPart {

	public static final String ID = "br.ufmg.dcc.labsoft.jextract.ui.ExtractMethodRecomendationsView";

	private TableViewer viewer;
	private Action actionExport;
	private Action toggleGroupBy;
	private Action actionHighlightCode;
	private Action actionApplyRefactoring;
	private Action actionExplainScore;
	private List<ExtractMethodRecomendation> recomendations;
	Listener sortListener;
	private boolean groupByMethod = false;
	private Settings settings;
	private IProject project;

	
	public ExtractMethodRecomendationsView() {
		this.recomendations = Collections.emptyList();
		
		this.sortListener = new Listener() {
			public void handleEvent(Event e) {
				TableColumn column = (TableColumn) e.widget;
				Utils.sort(recomendations, groupByMethod);
				viewer.getTable().setSortColumn(column);
				viewer.setInput(recomendations);
			}
		};
	}

	public void setRecomendations(List<ExtractMethodRecomendation> recomendations, IProject project, Settings settings) {
		this.recomendations = recomendations;
		this.settings = settings;
		this.project = project;
		this.viewer.setInput(recomendations);
	}

	public void createPartControl(Composite parent) {
		viewer = new TableViewer(parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
		
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);
		
		//TableViewerColumn colId = addColumnId();
		//colId.getColumn().addListener(SWT.Selection, sortListener);
		//addColumnOk();
		addColumnClass();
		addColumnMethodName();
		addColumnRank();
		addColumnScore().getColumn();
		addColumnOriginalSize().getColumn();
		addColumnExtractedSize();
		//addColumnExplanation();
		//addColumnDiffSize();

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
				return (element.getRank()) + "";
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
		return addColumn("Method Size", new EmrTableColumnLabelProvider() {
			@Override
			public String getColumnText(ExtractMethodRecomendation element) {
				return element.getOriginalSize() + "";
			}
		}, 100);
	}

	private TableViewerColumn addColumnExtractedSize() {
		return addColumn("Extracted Size", new EmrTableColumnLabelProvider() {
			@Override
			public String getColumnText(ExtractMethodRecomendation element) {
				return element.getExtractedSize() + "";
			}
		}, 100);
	}

	private TableViewerColumn addColumnDiffSize() {
		return addColumn("Diff Size", new EmrTableColumnLabelProvider() {
			@Override
			public String getColumnText(ExtractMethodRecomendation element) {
				return element.getDiffSize() + "";
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

	private TableViewerColumn addColumnScore() {
		TableViewerColumn col = addColumn("Score", new EmrTableColumnLabelProvider() {
			@Override
			public String getColumnText(ExtractMethodRecomendation element) {
				return String.format("%s", Double.toString(element.getScore()));
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
		manager.add(actionExplainScore);
		manager.add(new Separator());
		manager.add(actionExport);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(actionHighlightCode);
		manager.add(actionApplyRefactoring);
		manager.add(actionExplainScore);
		//manager.add(action2);
		manager.add(new Separator());
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		//manager.add(action1);
		manager.add(actionExport);
		//manager.add(toggleGroupBy);
		manager.add(new Separator());
	}

	private void makeActions() {
		actionExplainScore = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showExplanation((ExtractMethodRecomendation) obj);
			}
		};
		actionExplainScore.setText("Explain Score");
		actionExplainScore.setToolTipText("Explain Score");
		//action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_OBJS_INFO_TSK));
		
		actionExport = new Action() {
			public void run() {
				String outputPath = "E:/Danilo/Temp/out.txt";
				new EmrFileExporter(recomendations, outputPath).export();
				showMessage(String.format("Data saved at %s", outputPath));
			}
		};
		actionExport.setText("Save to file");
		actionExport.setToolTipText("Export results as a text file");
		actionExport.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_ETOOL_SAVEAS_EDIT));

//		action3 = new Action() {
//			public void run() {
//				String baseFolder = "E:/Danilo/Temp/";
//				EmrRankFileExporter.exportAll(recomendations, baseFolder);
//				showMessage(String.format("Data saved at %s", baseFolder));
//			}
//		};
//		action3.setText("Export all");
//		action3.setToolTipText("Export results for comparison");
//		action3.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
//				getImageDescriptor(ISharedImages.IMG_ETOOL_SAVEALL_EDIT));
		
		toggleGroupBy = new Action("Toggle group by method", Action.AS_CHECK_BOX) {
			public void run() {
				groupByMethod = !groupByMethod;
			}
		};
		toggleGroupBy.setToolTipText("Group recomendations by method");
		toggleGroupBy.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_ELCL_COLLAPSEALL));
		
		actionHighlightCode = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				showRefactoringDetails((ExtractMethodRecomendation) obj);
			}
		};
		actionHighlightCode.setText("Highlight recommendation");
		actionHighlightCode.setToolTipText("Highlight recommendation");

		actionApplyRefactoring = new Action() {
			public void run() {
				// TODO
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				applyRefactoring((ExtractMethodRecomendation) obj);
			}

		};
		actionApplyRefactoring.setText("Apply Refactoring");
		actionApplyRefactoring.setToolTipText("Apply Refactoring");
	}

	private void applyRefactoring(ExtractMethodRecomendation emr) {
		// TODO Auto-generated method stub
		ExtractionSlice slice = emr.getExtractionSlice();
		Fragment frag = slice.getEnclosingFragment();
		
		ExtractMethodRefactoring refactoring = new ExtractMethodRefactoring(emr.getSourceFile(), frag.start, frag.length());
		ExtractMethodWizard wizard = new ExtractMethodWizard(refactoring);
		
		new RefactoringStarter().activate(new ExtractMethodWizard(refactoring), null, RefactoringMessages.ExtractMethodAction_dialog_title, RefactoringSaveHelper.SAVE_NOTHING);
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				actionHighlightCode.run();
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
			this.clearMarkers();

			Fragment[] fragments = refactoring.getExtractionSlice().getFragments();
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
		String scoreDetails = EmrScoringFunction.getInstance(this.settings).getScoreDetails(emr);
		showMessage(scoreDetails);
		System.out.println(scoreDetails);
	}
	
	@Override
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private void clearMarkers() {
		try {
			this.project.deleteMarkers("br.ufmg.dcc.labsoft.jextract.extractionslice", true, IResource.DEPTH_INFINITE);
			this.project.deleteMarkers("br.ufmg.dcc.labsoft.jextract.extractionslicedup", true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void dispose() {
		super.dispose();
		this.clearMarkers();
	}
}
