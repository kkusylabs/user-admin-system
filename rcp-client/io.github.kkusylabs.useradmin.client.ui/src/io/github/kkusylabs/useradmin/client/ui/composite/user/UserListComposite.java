package io.github.kkusylabs.useradmin.client.ui.composite.user;

import java.util.List;

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
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableColumn;

import io.github.kkusylabs.useradmin.client.core.api.common.PagedResponse;
import io.github.kkusylabs.useradmin.client.core.api.user.UserListItemResponse;
import io.github.kkusylabs.useradmin.client.core.api.user.UserListResponse;
import io.github.kkusylabs.useradmin.client.ui.util.SwtUtil;
import io.github.kkusylabs.useradmin.client.ui.util.TextUtil;

public class UserListComposite extends Composite {

	private TableViewer viewer;

	private Button addButton;

	private Button deleteButton;
	
	private Button firstButton;

	private Button previousButton;

	private Label pageLabel;

	private Button nextButton;
	
	private UserListActions actions;

	private Button lastButton;
	
	private UserListResponse currentResponse;
	
	private Combo pageSizeCombo;
	
	public UserListComposite(Composite parent, int style) {
		super(parent, style);
		createControls();
		hookListeners();
		clear();
	}

	private void createControls() {
		GridLayout layout = new GridLayout(1, false);
		layout.marginWidth = 8;
		layout.marginHeight = 8;
		layout.verticalSpacing = 8;
		setLayout(layout);

		createButtonBar();
		createUserTable();
		createPagingBar();
	}
	
	private void createButtonBar() {
		Composite buttonBar = new Composite(this, SWT.NONE);

		buttonBar.setLayoutData(
				new GridData(SWT.RIGHT, SWT.CENTER, true, false));

		GridLayout buttonLayout = new GridLayout(3, false);
		buttonLayout.marginWidth = 0;
		buttonLayout.marginHeight = 0;
		buttonLayout.horizontalSpacing = 6;

		buttonBar.setLayout(buttonLayout);

		addButton =
				SwtUtil.createPushButton(buttonBar, "Add");

		deleteButton =
				SwtUtil.createPushButton(buttonBar, "Delete");
	}
	
	private void createUserTable() {
		viewer = new TableViewer(this,
				SWT.BORDER | SWT.FULL_SELECTION | SWT.SINGLE | SWT.V_SCROLL | SWT.H_SCROLL);

		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		viewer.setContentProvider(ArrayContentProvider.getInstance());

		createColumns();
	}
	
	private void createColumns() {
		createColumn("Username", 140, item -> item.user().username());

		createColumn("Full Name", 160, item -> item.user().fullName());

		createColumn("Role", 80,
				item -> item.user().role() == null ? "" : item.user().role().getDisplayName());

		createColumn("Department", 160,
				item -> item.user().department() == null
						? ""
						: item.user().department().name());

		createColumn("Active", 70,
				item -> item.user().active() ? "Yes" : "No");
	}
	
	private void createColumn(
			String title,
			int width,
			java.util.function.Function<UserListItemResponse, String> valueProvider) {

		TableViewerColumn viewerColumn = new TableViewerColumn(viewer, SWT.NONE);

		TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(width);
		column.setResizable(true);
		column.setMoveable(false);

		viewerColumn.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				UserListItemResponse item = (UserListItemResponse) element;
				return TextUtil.nullToEmpty(valueProvider.apply(item));
			}
		});
	}
	
	private void createPagingBar() {
		Composite pagingBar = new Composite(this, SWT.NONE);

		pagingBar.setLayoutData(
				new GridData(SWT.FILL, SWT.BOTTOM, true, false));

		GridLayout layout = new GridLayout(3, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 8;

		pagingBar.setLayout(layout);

		createNavigationControls(pagingBar);

		Label spacer = new Label(pagingBar, SWT.NONE);
		spacer.setLayoutData(
				new GridData(SWT.FILL, SWT.CENTER, true, false));

		createPageSizeControls(pagingBar);
	}
	
	private void createNavigationControls(Composite parent) {
		Composite nav = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout(5, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 6;

		nav.setLayout(layout);

		firstButton = new Button(nav, SWT.PUSH);
		firstButton.setText("<<");

		previousButton = new Button(nav, SWT.PUSH);
		previousButton.setText("<");

		pageLabel = new Label(nav, SWT.CENTER);
		pageLabel.setText("Page 999 of 999");
		
		GridData pageLabelData =
				new GridData(SWT.CENTER, SWT.CENTER, false, false);

		pageLabelData.widthHint =
				pageLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;

		pageLabel.setLayoutData(pageLabelData);

		pageLabel.setText("Page 0 of 0");

		nextButton = new Button(nav, SWT.PUSH);
		nextButton.setText(">");

		lastButton = new Button(nav, SWT.PUSH);
		lastButton.setText(">>");

		configurePagingButtonWidths();
	}
	
	private void configurePagingButtonWidths() {
		int buttonWidth = Math.max(
				Math.max(
						firstButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
						previousButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x),
				Math.max(
						nextButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x,
						lastButton.computeSize(SWT.DEFAULT, SWT.DEFAULT).x));

		buttonWidth += 10;

		GridData firstData = new GridData(buttonWidth, SWT.DEFAULT);
		GridData previousData = new GridData(buttonWidth, SWT.DEFAULT);
		GridData nextData = new GridData(buttonWidth, SWT.DEFAULT);
		GridData lastData = new GridData(buttonWidth, SWT.DEFAULT);

		firstButton.setLayoutData(firstData);
		previousButton.setLayoutData(previousData);
		nextButton.setLayoutData(nextData);
		lastButton.setLayoutData(lastData);
	}
	
	private void createPageSizeControls(Composite parent) {
		Composite sizeArea = new Composite(parent, SWT.NONE);

		GridLayout layout = new GridLayout(2, false);
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		layout.horizontalSpacing = 4;

		sizeArea.setLayout(layout);

		new Label(sizeArea, SWT.NONE).setText("Page size:");

		pageSizeCombo = new Combo(sizeArea, SWT.READ_ONLY);
		pageSizeCombo.setItems("10", "25", "50", "100");
		pageSizeCombo.select(1);
		pageSizeCombo.addListener(SWT.Selection, e -> {
			if (actions != null) {
				actions.pageSizeChanged(getSelectedPageSize());
			}
		});
	}
	
	private int getSelectedPageSize() {
		return Integer.parseInt(pageSizeCombo.getText());
	}
		
	public void setActions(UserListActions actions) {
		this.actions = actions;
	}
	
	private void hookListeners() {
		addButton.addListener(SWT.Selection, e -> {
			if (actions != null) {
				actions.addUserRequested();
			}
		});

		deleteButton.addListener(SWT.Selection, e -> {
			UserListItemResponse selected = getSelectedUser();

			if (selected != null && actions != null) {
				actions.deleteUserRequested(selected);
			}
		});

		viewer.addSelectionChangedListener(event -> {
			UserListItemResponse selected = getSelectedUser();

			deleteButton.setEnabled(selected != null && selected.canDelete());

			if (selected != null && actions != null) {
				actions.userSelected(selected);
			}
		});

		firstButton.addListener(SWT.Selection, e -> {
			if (actions != null) {
				actions.firstPageRequested();
			}
		});

		previousButton.addListener(SWT.Selection, e -> {
			if (actions != null) {
				actions.previousPageRequested();
			}
		});

		nextButton.addListener(SWT.Selection, e -> {
			if (actions != null) {
				actions.nextPageRequested();
			}
		});

		lastButton.addListener(SWT.Selection, e -> {
			if (actions != null) {
				actions.lastPageRequested();
			}
		});
	}
	
	public void setUsers(UserListResponse response) {
		this.currentResponse = response;

		addButton.setEnabled(response != null && response.canCreate());

		List<UserListItemResponse> users =
				response == null
						? List.of()
						: response.users().content();

		viewer.setInput(users);

		deleteButton.setEnabled(false);

		updatePaging(response);
	}
	
	public void clear() {
		this.currentResponse = null;

		viewer.setInput(List.of());

		addButton.setEnabled(false);
		deleteButton.setEnabled(false);

		updatePaging(null);
	}
	
	private void updatePaging(UserListResponse response) {
		if (response == null || response.users() == null) {
			pageLabel.setText("Page 0 of 0");

			firstButton.setEnabled(false);
			previousButton.setEnabled(false);
			nextButton.setEnabled(false);
			lastButton.setEnabled(false);

			return;
		}

		PagedResponse<UserListItemResponse> page = response.users();

		int currentPage = page.pageNumber() + 1;
		int totalPages = Math.max(page.totalPages(), 1);

		pageLabel.setText("Page " + currentPage + " of " + totalPages);
		pageLabel.getParent().layout();

		firstButton.setEnabled(!page.first());
		previousButton.setEnabled(!page.first());

		nextButton.setEnabled(!page.last());
		lastButton.setEnabled(!page.last());
	}
	
	private UserListItemResponse getSelectedUser() {
		IStructuredSelection selection =
				(IStructuredSelection) viewer.getSelection();

		return (UserListItemResponse) selection.getFirstElement();
	}
	
	public void replaceUser(UserListItemResponse updated) {
		if (currentResponse == null || currentResponse.users() == null) {
			return;
		}

		List<UserListItemResponse> users =
				currentResponse.users().content();

		for (int i = 0; i < users.size(); i++) {
			UserListItemResponse existing = users.get(i);

			if (existing.user().id().equals(updated.user().id())) {
				users.set(i, updated);
				viewer.refresh();
				viewer.setSelection(new StructuredSelection(updated), true);
				return;
			}
		}
	}
	
	public void selectUser(Long userId) {
		if (userId == null) {
			viewer.setSelection(StructuredSelection.EMPTY);
			return;
		}

		List<UserListItemResponse> users =
				currentResponse == null || currentResponse.users() == null
						? List.of()
						: currentResponse.users().content();

		for (UserListItemResponse item : users) {
			if (userId.equals(item.user().id())) {
				viewer.setSelection(new StructuredSelection(item), true);
				return;
			}
		}

		viewer.setSelection(StructuredSelection.EMPTY);
	}
}
