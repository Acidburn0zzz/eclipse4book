package com.example.e4.rcp.todo.parts;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.PojoProperties;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.MDirtyable;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.example.e4.rcp.todo.model.ITodoService;
import com.example.e4.rcp.todo.model.Todo;

public class EditorPart {

	@Inject
	MDirtyable dirty;

	private Text summary;
	private Text description;
	private Button btnDone;
	private DateTime dateTime;
	private DataBindingContext ctx = new DataBindingContext();

	// Define listener for the databinding
	IChangeListener listener = new IChangeListener() {
		@Override
		public void handleChange(ChangeEvent event) {
			if (dirty!=null){
				dirty.setDirty(true);
			}
		}
	};
	private Todo todo;

	@PostConstruct
	public void createControls(Composite parent, MPart part, ITodoService todoService) {
		// extract the id of the todo item
		String string = part.getPersistedState().get(Todo.FIELD_ID);
		Long idOfTodo = Long.valueOf(string);
		
		// retrieve the todo based on the MInput
		todo = todoService.getTodo(idOfTodo);
		
		GridLayout gl_parent = new GridLayout(2, false);
		gl_parent.marginRight = 10;
		gl_parent.marginLeft = 10;
		gl_parent.horizontalSpacing = 10;
		gl_parent.marginWidth = 0;
		parent.setLayout(gl_parent);

		Label lblSummary = new Label(parent, SWT.NONE);
		lblSummary.setText("Summary");

		summary = new Text(parent, SWT.BORDER);
		summary.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		Label lblDescription = new Label(parent, SWT.NONE);
		lblDescription.setText("Description");

		description = new Text(parent, SWT.BORDER | SWT.MULTI);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gd.heightHint = 122;
		description.setLayoutData(gd);

		Label lblNewLabel = new Label(parent, SWT.NONE);
		lblNewLabel.setText("Due Date");

		dateTime = new DateTime(parent, SWT.BORDER);
		dateTime.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false,
				1, 1));
		new Label(parent, SWT.NONE);

		btnDone = new Button(parent, SWT.CHECK);
		btnDone.setText("Done");
		
		updateUserInterface(todo);
	}

	@Persist
	public void save(MDirtyable dirty, ITodoService model) {
		model.saveTodo(todo);
		dirty.setDirty(false);
	}

	private void updateUserInterface(Todo todo) {
		
		// Check if the user interface is available
		// assume you have a field called "summary"
		// for a widget
		if (summary != null && !summary.isDisposed()) {
			
			
			// Deregister change listener to the old binding
			IObservableList providers = ctx.getValidationStatusProviders();
			for (Object o : providers) {
				Binding b = (Binding) o;
				b.getTarget().removeChangeListener(listener);
			}
			
			// Remove bindings
			ctx.dispose();
			
			IObservableValue target = WidgetProperties.text(SWT.Modify)
					.observe(summary);
			IObservableValue model = PojoProperties.value(Todo.FIELD_SUMMARY).observe(
					todo);
			ctx.bindValue(target, model);

			target = WidgetProperties.text(SWT.Modify).observe(description);
			model = PojoProperties.value(Todo.FIELD_DESCRIPTION).observe(todo);
			ctx.bindValue(target, model);
			
			target = WidgetProperties.selection().observe(btnDone);
			model = PojoProperties.value(Todo.FIELD_DONE).observe(todo);
			ctx.bindValue(target, model);

			IObservableValue observeSelectionDateTimeObserveWidget = 
					WidgetProperties.selection().observe(dateTime);
			IObservableValue dueDateTodoObserveValue = 
					PojoProperties.value(Todo.FIELD_DUEDATE).observe(todo);
			ctx.bindValue(observeSelectionDateTimeObserveWidget,
					dueDateTodoObserveValue, null, null);
			
			// Register for the changes
			providers = ctx.getValidationStatusProviders();
			for (Object o : providers) {
				Binding b = (Binding) o;
				b.getTarget().addChangeListener(listener);
			}
		}
	}


	@Focus
	public void onFocus() {
		// The following assumes that you have a Text field
		// called summary
		summary.setFocus();
	}
}
