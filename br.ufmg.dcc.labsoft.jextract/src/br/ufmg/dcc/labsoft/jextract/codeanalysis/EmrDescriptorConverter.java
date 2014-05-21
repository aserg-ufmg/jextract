package br.ufmg.dcc.labsoft.jextract.codeanalysis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

import br.ufmg.dcc.labsoft.jextract.model.BlockModel;
import br.ufmg.dcc.labsoft.jextract.model.MethodModel;
import br.ufmg.dcc.labsoft.jextract.model.OffsetBasedEmrDescriptor;
import br.ufmg.dcc.labsoft.jextract.model.StatementBasedEmrDescriptor;
import br.ufmg.dcc.labsoft.jextract.model.StatementModel;
import br.ufmg.dcc.labsoft.jextract.model.impl.MethodModelBuilder;
import br.ufmg.dcc.labsoft.jextract.ranking.ExtractionSlice;

public class EmrDescriptorConverter {

	private final AstParser parser;
	private final IJavaProject project;

	public EmrDescriptorConverter(IJavaProject project, AstParser parser) {
		this.project = project;
		this.parser = parser;
	}
	
	public List<StatementBasedEmrDescriptor> convert(List<? extends OffsetBasedEmrDescriptor> descritors) {
		List<StatementBasedEmrDescriptor> result = new ArrayList<StatementBasedEmrDescriptor>();
		for (OffsetBasedEmrDescriptor emr : descritors) {
			this.convertAndAdd(emr, result);
		}
		return result;
	}

	private void convertAndAdd(OffsetBasedEmrDescriptor emr, List<StatementBasedEmrDescriptor> result) {
		ICompilationUnit icu = this.parser.getICompilationUnit(this.project, emr.getFilePath());
		CompilationUnit cu = this.parser.getCompilationUnit(icu, true);
		MethodDeclaration methodDeclaration = (MethodDeclaration) cu.findDeclaringNode(emr.getMethodBindingKey());
		MethodModel methodModel = MethodModelBuilder.create(icu, methodDeclaration);
		
		ExtractionSlice slice = emr.getExtractionSlice();
		if (slice.hasDuplication()) {
			return;
		}
		
		BlockModel mainBlock;
		for (BlockModel block : methodModel.getBlocks()) {
			for (StatementModel statement : block.getChildren()) {
				Statement node = statement.getAstNode();
				boolean selected = slice.hasIntersectionWithExtracted(node.getStartPosition(), node.getLength());
				if (selected) {
					
				}
			}
		}
	}
	
}
