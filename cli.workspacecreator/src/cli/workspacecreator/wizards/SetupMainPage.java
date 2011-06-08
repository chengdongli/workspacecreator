/*
 * Licensed Material - Property of IBM 
 * (C) Copyright IBM Corp. 2002 - All Rights Reserved. 
 */
 
package cli.workspacecreator.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;

/**
 * Class representing the first page of the wizard
 */

public class SetupMainPage extends WizardPage implements Listener
{

	IWorkbench workbench;
	IStructuredSelection selection;
		
	private Text txtFolder;
    private Text txtRepo;
    private Text txtUser;
    private Text txtPassword;

    private Button btnBrowseFolder;
    private Button btnRepo;
    private Label labelRepo;
    private Label labelUser;
    private Label labelPassword;
    
    private Button btnBrowseFile;
    private Button btnUrlConfig;
    private Button btnFileConfig;
    private Text txtConfigFile;
    private Text txtConfigUrl;

	/**
	 * Constructor for MainPage.
	 */
	public SetupMainPage(IWorkbench workbench, IStructuredSelection selection) {
		super("Page1");
		setTitle("Import Projects into Workspace");
		setDescription("Select from where and how to import projects");
		this.workbench = workbench;
		this.selection = selection;
//		destinationStatus = new Status(IStatus.OK, "not_used", 0, "", null);
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {

	    // create the composite to hold the widgets
		Composite composite =  new Composite(parent, SWT.NULL);
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(composite);
		
        createProjectSetGroup(composite);
        createLine(composite, 3);
        createSourceFolderGroup(composite);
        
	    // set the composite as the control for this page
		setControl(composite);		
		addListeners();
	}
	
	private void createProjectSetGroup(Composite parent) {
	    Group composite=new Group(parent, SWT.NONE);
	    composite.setText("Project Set");
	    GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(3,1).grab(true, false).applyTo(composite);
	    GridLayoutFactory.fillDefaults().numColumns(3).applyTo(composite);
	    
        btnUrlConfig=new Button(composite, SWT.RADIO);
        btnUrlConfig.setText("Load from Url:");
        txtConfigUrl=new Text(composite, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2,1).grab(true, false).applyTo(txtConfigUrl);
        
        btnFileConfig=new Button(composite, SWT.RADIO);
        btnFileConfig.setText("Load from file:");
        txtConfigFile=new Text(composite, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(1,1).grab(true, false).applyTo(txtConfigFile);
        
        btnBrowseFile=new Button(composite, SWT.BORDER);
        btnBrowseFile.setText("Browse...");

    }

    private void createSourceFolderGroup(Composite parent) {
        Group composite=new Group(parent, SWT.NONE);
        composite.setText("Import from");
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(3,1).grab(true, false).applyTo(composite);
        GridLayoutFactory.fillDefaults().numColumns(3).applyTo(composite);

        new Label (composite, SWT.NONE).setText("Folder:");
        txtFolder=new Text(composite, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(1,1).grab(true, false).applyTo(txtFolder);
        btnBrowseFolder=new Button(composite, SWT.BORDER);
        btnBrowseFolder.setText("Browse...");
        
        btnRepo=new Button(composite, SWT.CHECK);
        btnRepo.setText("Check out source from repository");
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(3,1).grab(false, false).applyTo(btnRepo);

        labelRepo=new Label (composite, SWT.NONE);
        labelRepo.setText("Repo:");
        txtRepo=new Text(composite, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2,1).grab(true, false).applyTo(txtRepo);

        labelUser=new Label (composite, SWT.NONE);
        labelUser.setText("User:");
        txtUser=new Text(composite, SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2,1).grab(true, false).applyTo(txtUser);

        labelPassword = new Label (composite, SWT.NONE);
        labelPassword.setText("Password:");
        txtPassword=new Text(composite, SWT.PASSWORD|SWT.BORDER);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(2,1).grab(true, false).applyTo(txtPassword);
    }

    private void addListeners()
	{
        btnRepo.addListener(SWT.Selection, this);
	}

	public void handleEvent(Event event) {
	    // If the event is triggered by the destination or departure fields
	    // set the corresponding status variable to the right value
	    if (event.widget == btnRepo){
	        enableRepoGroup(btnRepo.getSelection());
	    }

	    // Show the most serious error
	    applyToStatusLine(findMostSevere());
		getWizard().getContainer().updateButtons();
	}

	private void enableRepoGroup(boolean enabled) {
        labelRepo.setEnabled(enabled);
        labelUser.setEnabled(enabled);
        labelPassword.setEnabled(enabled);
        txtRepo.setEnabled(enabled);
        txtUser.setEnabled(enabled);
        txtPassword.setEnabled(enabled);
    }

    /**
	 * @see IWizardPage#canFlipToNextPage()
	 */
	public boolean canFlipToNextPage()
	{
//		if (getErrorMessage() != null) return false;
//		if (isTextNonEmpty(fromText)
//			&& isTextNonEmpty(toText) &&
//			(planeButton.getSelection() || carButton.getSelection()) 
//			&& isReturnDateSet())
//			return true;
		return false;
	}
	
	/**
	 * Applies the status to the status line of a dialog page.
	 */
	private void applyToStatusLine(IStatus status) {
		String message= status.getMessage();
		if (message.length() == 0) message= null;
		switch (status.getSeverity()) {
			case IStatus.OK:
				setErrorMessage(null);
				setMessage(message);
				break;
			case IStatus.WARNING:
				setErrorMessage(null);
				setMessage(message, WizardPage.WARNING);
				break;				
			case IStatus.INFO:
				setErrorMessage(null);
				setMessage(message, WizardPage.INFORMATION);
				break;			
			default:
				setErrorMessage(message);
				setMessage(null);
				break;		
		}
	}	
	
	private IStatus findMostSevere()
	{
	    return Status.OK_STATUS;
	}

	private void createLine(Composite parent, int ncol) 
	{
		Label line = new Label(parent, SWT.SEPARATOR|SWT.HORIZONTAL|SWT.BOLD);
        GridDataFactory.fillDefaults().align(SWT.FILL, SWT.CENTER).span(ncol,1).grab(true, false).applyTo(line);
	}	

}

