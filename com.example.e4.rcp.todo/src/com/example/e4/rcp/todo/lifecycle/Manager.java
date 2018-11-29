package com.example.e4.rcp.todo.lifecycle;

import javax.inject.Inject;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.extensions.Preference;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.e4.ui.internal.workbench.swt.PartRenderingEngine;
import org.eclipse.e4.ui.workbench.lifecycle.PostContextCreate;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Monitor;
import org.eclipse.swt.widgets.Shell;
import org.osgi.service.prefs.BackingStoreException;

import com.beust.jcommander.JCommander;
import com.example.e4.rcp.todo.ProgrammArgs;
import com.example.e4.rcp.todo.dialogs.PasswordDialog;
import com.example.e4.rcp.todo.preferences.PreferenceConstants;

public class Manager {

	// We add the nodePath in case you move the lifecycle handler to
	// another plug-in later
	@Inject
	@Preference(nodePath = PreferenceConstants.NODEPATH, value = PreferenceConstants.USER_PREF_KEY)
	private String user;

	@PostContextCreate
	public void postContextCreate(@Preference IEclipsePreferences prefs, IApplicationContext appContext,
			Display display, IEclipseContext context) {

		String[] args = (String[]) appContext.getArguments().get("application.args");
		ProgrammArgs programArgs = new ProgrammArgs();
		JCommander.newBuilder().acceptUnknownOptions(true).addObject(programArgs).build().parse(args);

		String username = programArgs.getUsername();
		String password = programArgs.getPassword();
		boolean autologin = programArgs.isAutologin();

		if (autologin) {
			// ... do automatic login with username and password ...

			return;
		}

		final Shell shell = new Shell(SWT.SHELL_TRIM);
		PasswordDialog dialog = new PasswordDialog(shell);
		if (user != null) {
			dialog.setUser(user);
		} else if (username != null) {
			dialog.setUser(username);
		}
		if (password != null) {
			dialog.setPassword(password);
		}

		// close the static splash screen
		appContext.applicationRunning();

		// position the shell
		setLocation(display, shell);

		String cssURI = "platform:/plugin/com.example.e4.rcp.todo/css/rainbow.css";
		context.set(E4Workbench.CSS_URI_ARG, cssURI);
		PartRenderingEngine.initializeStyling(shell.getDisplay(), context);
		// open the dialog
		if (dialog.open() != Window.OK) {
			// close the application
			System.exit(-1);
		} else {
			// get the user from the dialog
			String userValue = dialog.getUser();
			// store the user values in the preferences
			prefs.put(PreferenceConstants.USER_PREF_KEY, userValue);
			try {
				prefs.flush();
			} catch (BackingStoreException e) {
				e.printStackTrace();
			}
		}

	}

	private void setLocation(Display display, Shell shell) {
		Monitor monitor = display.getPrimaryMonitor();
		Rectangle monitorRect = monitor.getBounds();
		Rectangle shellRect = shell.getBounds();
		int x = monitorRect.x + (monitorRect.width - shellRect.width) / 2;
		int y = monitorRect.y + (monitorRect.height - shellRect.height) / 2;
		shell.setLocation(x, y);
	}
}
