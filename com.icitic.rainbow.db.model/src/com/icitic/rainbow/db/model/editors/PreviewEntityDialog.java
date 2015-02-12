package com.icitic.rainbow.db.model.editors;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import com.icitic.core.db.model.Entity;
import com.icitic.core.db.model.Model;
import com.icitic.rainbow.db.model.TransformUtility;

public class PreviewEntityDialog extends Dialog {

	private Entity entity;

	private TabFolder tabFolder;

	private StyledText text;

	public PreviewEntityDialog(Shell parentShell, Entity entity) {
		super(parentShell);
		this.entity = entity;

	}

	@Override
	protected Point getInitialSize() {
		return new Point(400, 500);
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("预览建表语句");
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

		tabFolder = new TabFolder(composite, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		text = new StyledText(tabFolder, SWT.H_SCROLL | SWT.V_SCROLL);
		text.setEditable(false);
		final Model newModel = new Model();
		newModel.getEntities().add(entity);
		tabFolder.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				text.setText(TransformUtility.getTransformText(tabFolder.getItem(tabFolder.getSelectionIndex())
						.getText(), newModel));
			}

		});
		TabItem tabItem1 = new TabItem(tabFolder, SWT.NONE);
		tabItem1.setText("ORACLE");
		TabItem tabItem2 = new TabItem(tabFolder, SWT.NONE);
		tabItem2.setText("DB2");
		tabItem1.setControl(text);
		tabItem2.setControl(text);
		text.setText(TransformUtility.getTransformText("ORACLE", newModel));

		return composite;
	}

	@Override
	protected void okPressed() {
		Clipboard clipboard = new Clipboard(getShell().getDisplay());
		TextTransfer textTransfer = TextTransfer.getInstance();
		String displayText = text.getText();
		clipboard.setContents(new String[] { displayText }, new Transfer[] { textTransfer });
		clipboard.dispose();
		super.okPressed();
	}
}
