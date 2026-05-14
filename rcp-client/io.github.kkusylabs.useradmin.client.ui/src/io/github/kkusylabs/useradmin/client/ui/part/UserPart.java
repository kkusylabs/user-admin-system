package io.github.kkusylabs.useradmin.client.ui.part;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.UISynchronize;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import io.github.kkusylabs.useradmin.client.core.api.user.UserApiClient;
import io.github.kkusylabs.useradmin.client.ui.composite.user.UserDetailsComposite;
import io.github.kkusylabs.useradmin.client.ui.composite.user.UserFilterComposite;
import io.github.kkusylabs.useradmin.client.ui.composite.user.UserListComposite;
import io.github.kkusylabs.useradmin.client.ui.runtime.ApiExecutor;
import io.github.kkusylabs.useradmin.client.ui.runtime.UiApiRunner;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Inject;

public class UserPart {
//	@Inject
//	private UISynchronize uiSync;
//
//	@Inject
//	@Optional
//	public void onLoginSuccess(
//	        @UIEventTopic(AppTopics.LOGIN_SUCCESS) UserModel user) {
//
//	    uiSync.asyncExec(this::loadUsers);
//	}
	
	private UiApiRunner apiRunner;

	private UserListComposite userList;
	private UserFilterComposite userFilter;
	private UserDetailsComposite userDetails;
	
	@Inject
	private ApiExecutor apiExecutor;

	@Inject
	private UISynchronize uiSync;

	@Inject
	private UserApiClient userApiClient;
	
	@Inject IEventBroker eventBroker;

	@PostConstruct
	public void createControls(Composite parent) {
		Shell shell = parent.getShell();
		apiRunner = new UiApiRunner(apiExecutor, uiSync, eventBroker, shell);
		
		parent.setLayout(new GridLayout(1, false));

		userFilter = new UserFilterComposite(parent, SWT.NONE);
		userFilter.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		SashForm sash = new SashForm(parent, SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		userList = new UserListComposite(sash, SWT.BORDER);
		userDetails = new UserDetailsComposite(sash, SWT.BORDER);

		sash.setWeights(new int[] { 40, 60 });

		wireEvents();
	}
	
	private void wireEvents() {
//		userFilter.addFilterChangedListener(filter -> {
//			userList.setFilter(filter);
//			userList.refresh();
//		});
//
//		userList.addSelectionChangedListener(user -> {
//			userDetails.setUser(user);
//		});
//
//		userList.addAddListener(() -> {
//			userDetails.setUser(new User());
//			userDetails.setEditable(true);
//		});
//
//		userList.addDeleteListener(user -> {
//			// delete user, then refresh list/details
//		});
//
//		userDetails.addEditListener(() -> {
//			userDetails.setEditable(true);
//		});
	}

	@Focus
	public void setFocus() {
		userList.setFocus();
	}
	
	@PreDestroy
	public void dispose() {
	}
}
