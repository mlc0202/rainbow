package com.icitic.rainbow.db.model.editors;

import static com.icitic.rainbow.db.model.editors.UiUtility.createColumn;

import java.io.FileOutputStream;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import com.icitic.core.db.model.Model;
import com.icitic.rainbow.db.model.TransformUtility;

public class ExportEntityDialog extends Dialog {

	private Model model;
	private Button btnDB2;
	private Button btnOracle;
	private TableViewer viewer;
	private String transform = "ORACLE";

	public Button getBtnDB2() {
		return btnDB2;
	}

	public Button getBtnOracle() {
		return btnOracle;
	}

	public ExportEntityDialog(Shell parentShell, Model model) {
		super(parentShell);
		this.model = model;

	}

	@Override
	protected Point getInitialSize() {
		return new Point(300, 400);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("导出建表语句");
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		applyDialogFont(composite);

		Composite container = new Composite(composite, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		container.setLayout(new GridLayout(1, false));

		Composite bar = new Composite(container, SWT.BORDER);
		bar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		bar.setLayout(new GridLayout(2, false));
		btnDB2 = new Button(bar, SWT.RADIO);
		btnDB2.setText("DB2");
		btnOracle = new Button(bar, SWT.RADIO);
		btnOracle.setSelection(true);
		btnOracle.setText("ORACLE");

		viewer = new TableViewer(container, SWT.FULL_SELECTION | SWT.BORDER | SWT.MULTI);
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new EntityLabelProvider());
		Table table = viewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);

		createColumn(table, "表名", 130);
		createColumn(table, "中文名", 150);
		viewer.setInput(model.getEntities());
		return composite;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void okPressed() {
		if (viewer.getSelection().isEmpty()) {
			MessageDialog.openInformation(getShell(), "提示", "请选择需要导出的表【按住Shift键可多选】！");
			return;
		}
		FileDialog dialog = new FileDialog(getShell(),SWT.SAVE);
		dialog.setFilterExtensions(new String[] { "*.sql" });
		String filename = dialog.open();
		if (filename == null)
			return;
		Model newModel = new Model();
		StructuredSelection selection = (StructuredSelection) viewer.getSelection();
		newModel.setEntities(selection.toList());
		newModel.setName(model.getName());
		if (btnDB2.getSelection())
			transform = "DB2";
		FileOutputStream os;			
		try {
			os = new FileOutputStream(filename);
			TransformUtility.transform(transform, newModel, os);
			os.close();
			super.okPressed();
		} catch (Exception e) {
			MessageDialog.openInformation(getShell(), "错误", e.getMessage());
		}
	}
}
