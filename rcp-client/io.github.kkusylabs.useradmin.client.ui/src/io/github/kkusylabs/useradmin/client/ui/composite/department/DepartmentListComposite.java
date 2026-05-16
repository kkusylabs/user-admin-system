package io.github.kkusylabs.useradmin.client.ui.composite.department;

import java.util.List;
import java.util.function.Function;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TableColumn;

import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentListItemResponse;
import io.github.kkusylabs.useradmin.client.core.api.department.DepartmentListResponse;
import io.github.kkusylabs.useradmin.client.ui.util.SwtUtil;
import io.github.kkusylabs.useradmin.client.ui.util.TextUtil;

public class DepartmentListComposite extends Composite {

	private TableViewer viewer;

	private Button addButton;
	private Button deleteButton;

	private DepartmentListActions actions;

	private DepartmentListResponse currentResponse;

	public DepartmentListComposite(Composite parent, int style) {
		super(parent, style);

		createControls();
		clear();
	}

	private void createControls() {
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 8;
		layout.marginHeight = 8;
		layout.verticalSpacing = 8;
		setLayout(layout);

		createButtonBar();
		createDepartmentTable();
	}

	private void createButtonBar() {
		Composite buttonBar = new Composite(this, SWT.NONE);
		buttonBar.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

		GridLayout buttonLayout = new GridLayout(3, false);
		buttonLayout.marginWidth = 0;
		buttonLayout.marginHeight = 0;
		buttonLayout.horizontalSpacing = 6;
		buttonBar.setLayout(buttonLayout);

		addButton = SwtUtil.createPushButton(buttonBar, "Add");
		deleteButton = SwtUtil.createPushButton(buttonBar, "Delete");
	}

	private void createDepartmentTable() {
		viewer = new TableViewer(
				this,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);

		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		viewer.setContentProvider(ArrayContentProvider.getInstance());

		createColumns();
	}

	private void createColumns() {
		createColumn("Name", 180, item -> item.department().name());
		createColumn("Description", 280, item -> item.department().description());
		createColumn("Active", 70, item -> item.department().active() ? "Yes" : "No");
	}

	private void createColumn(
			String title,
			int width,
			Function<DepartmentListItemResponse, String> valueProvider) {

		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);

		TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(width);
		column.setResizable(true);
		column.setMoveable(false);

		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				DepartmentListItemResponse item = (DepartmentListItemResponse) element;
				return TextUtil.nullToEmpty(valueProvider.apply(item));
			}
		});
	}

	public void setActions(DepartmentListActions actions) {
		this.actions = actions;

		addButton.addListener(SWT.Selection, e -> {
			if (this.actions != null) {
				this.actions.addDepartmentRequested();
			}
		});

		deleteButton.addListener(SWT.Selection, e -> {
			if (this.actions == null) {
				return;
			}

			DepartmentListItemResponse selected = getSelectedDepartment();

			if (selected != null) {
				this.actions.deleteDepartmentRequested(selected);
			}
		});

		viewer.addSelectionChangedListener(event -> {
			DepartmentListItemResponse selected = getSelectedDepartment();

			deleteButton.setEnabled(selected != null && selected.canDelete());

			if (selected != null && this.actions != null) {
				this.actions.departmentSelected(selected);
			}
		});
	}

	public void setDepartments(DepartmentListResponse response) {
		this.currentResponse = response;

		addButton.setEnabled(response != null && response.canCreate());

		List<DepartmentListItemResponse> departments =
				response == null || response.departments() == null
						? List.of()
						: response.departments();

		viewer.setInput(departments);

		deleteButton.setEnabled(false);
	}

	public void replaceDepartment(DepartmentListItemResponse updated) {
		if (currentResponse == null || currentResponse.departments() == null || updated == null) {
			return;
		}

		List<DepartmentListItemResponse> departments = currentResponse.departments();

		for (int i = 0; i < departments.size(); i++) {
			DepartmentListItemResponse existing = departments.get(i);

			if (existing.department().id().equals(updated.department().id())) {
				departments.set(i, updated);
				viewer.refresh();
				selectDepartment(updated.department().id());
				return;
			}
		}
	}

	public void selectDepartment(Long departmentId) {
		if (departmentId == null) {
			viewer.setSelection(StructuredSelection.EMPTY);
			return;
		}

		List<DepartmentListItemResponse> departments =
				currentResponse == null || currentResponse.departments() == null
						? List.of()
						: currentResponse.departments();

		for (DepartmentListItemResponse item : departments) {
			if (departmentId.equals(item.department().id())) {
				viewer.setSelection(new StructuredSelection(item), true);
				return;
			}
		}

		viewer.setSelection(StructuredSelection.EMPTY);
	}

	public void clear() {
		this.currentResponse = null;

		viewer.setInput(List.of());

		addButton.setEnabled(false);
		deleteButton.setEnabled(false);
	}

	private DepartmentListItemResponse getSelectedDepartment() {
		IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
		return (DepartmentListItemResponse) selection.getFirstElement();
	}
}
