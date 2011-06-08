
package cli.workspacecreator.wizards;

import cli.workspacecreator.UIControlsFactory;
import cli.workspacecreator.UIUtil;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.ObservablesManager;
import org.eclipse.core.databinding.ValidationStatusProvider;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.ISWTObservable;
import org.eclipse.jface.databinding.viewers.IViewerObservableSet;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.wizard.WizardPageSupport;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * DataBindingWizardPage will take care of databinding and children control decorating.
 * <ul>
 *  <li>Initiating and disposing the binding context by WizardPageSupport.</li>
 *  <li>Decorating the control on setPageComplete.</li>
 * </ul>
 * 
 * @author chengdong
 *
 */
public abstract class DataBindingWizardPage extends CommonWizardPage
{
  protected DataBindingWizardPage(String pageName, String title,
  		ImageDescriptor titleImage) {
  	super(pageName, title, titleImage);
  }

  @Override
  final public void createControl(Composite parent) {
  	m_dbc = new DataBindingContext();
  	WizardPageSupport.create(this, m_dbc); // This will dispose the "data binding context" when wizard is disposed
  	setControl(initPane(parent));
  }

  @Override
  public void setPageComplete(boolean complete)
  {
    super.setPageComplete(complete);
    if (!m_isDisposed)
    {
      updateDecoration();
    }
  }

  protected void updateDecoration() {
		for (Object o : m_dbc.getValidationStatusProviders()) {
		  ValidationStatusProvider provider= (ValidationStatusProvider) o;
		  updateDecoration(provider);
		}
	}

  protected void updateDecoration(ValidationStatusProvider provider)
  {
    IStatus status = (IStatus) provider.getValidationStatus()
        .getValue();
    Control control = null;
    for(Object target: provider.getTargets()){
      if (target instanceof ISWTObservable) {
        ISWTObservable swtObservable = (ISWTObservable)target;
        control = (Control) swtObservable.getWidget();
        UIControlsFactory.updateDecoration(control, status);
      }else if(target instanceof IViewerObservableValue){
        Viewer viewer = ((IViewerObservableValue)target).getViewer();
        if(viewer!=null){ // for RAP, this could be null
          control=viewer.getControl();
          UIControlsFactory.updateDecoration(control, status);
        }
      }
      else if(target instanceof IViewerObservableSet){
        control=((IViewerObservableSet)target).getViewer().getControl();
        UIControlsFactory.updateDecoration(control, status);
      }
//      else if(target instanceof IFilteredChoiceObservableValue){ // parameter edit model
//        control=((IFilteredChoiceObservableValue)target).getChooser();
//        UIControlsFactory.updateDecoration(control, status);
//      }
//      else if(target instanceof DecoratedWritableValue){ // lookup key edit model
//        control=((DecoratedWritableValue)target).getControl();
//        UIControlsFactory.updateDecoration(control, status);
//      }
    }
  }

  protected void refreshPageStatus() {
		boolean isFirstError = true;
//		m_dbc.updateModels();
		for (Object o : m_dbc.getValidationStatusProviders()) {
			ValidationStatusProvider provider = (ValidationStatusProvider) o;
			IStatus status = (IStatus) provider.getValidationStatus()
					.getValue();

			if (!status.isOK() && isFirstError) {
				setMessage(null);
				setMessage(status.getMessage(), severityToType(status.getSeverity()));
				isFirstError = false;
			}

			updateDecoration(provider);
		}
	}

  /**
   * Convert the IStatus.serverity to WizardPage.messageType
   * @param severity
   * @return message type
   */
  public static int severityToType(int severity)
  {
    switch(severity){
      case IStatus.CANCEL:
      case IStatus.OK:
        return WizardPage.NONE;
      case IStatus.ERROR:
        return WizardPage.ERROR;
      case IStatus.INFO:
        return WizardPage.INFORMATION;
      case IStatus.WARNING:
        return WizardPage.WARNING;
        
    }
    Assert.isTrue(false, "Severity type: "+severity+" is not supported");
    return WizardPage.NONE;
  }

  protected void pageAboutToShow(PageChangedEvent event)
  {
    refreshPageStatus();
  }

	protected IStatus computeStaticPartsStatus() {
	  return ValidationStatus.ok();
	}
		
	protected void updateStaticPartsStatus(IStatus status) {
	}
 
	protected void syncWithModel(){
		for (Object o : m_dbc.getBindings()) {
			Binding binding= (Binding) o;
			binding.updateModelToTarget();
		}
		updateDecoration();
	}

  /** A standard initialization algorithm for all "pages".
    *
    * @param parent
    */
	private final Control initPane(Composite parent)
	{
	  // Create/layout all of the "controls" and child "edit panes" of this
	  // edit pane 
	  Control comp = createPartControl(parent);

	  // If necessary, populate and further initialize the controls that were
	  // just created.
	  initializeControls();

    // Add the appropriate "data binding" logic
    initDataBinding();

    // Ensure that the controls are correctly enabled and/or disabled
    enableDisableControls();

    // Add the Listener for changes in selection. Do this after the various 
    // components have been populated so premature selection notifications
    // won't be processed
    addListeners();

    // Make the control LnF as Form-UI.
    adaptControls(parent);
    
    return comp;
	}

  /** Implement this method to create all of the controls for this pane. This
    * method must only be called by the InitPane() method.
    */
	protected abstract Control createPartControl(Composite parent);

  /** This method is called just after the createPartControl method is called.
   * This method can be further initialize the controls that were just created
   * (i.e. populate a combo-box, initialize a spinner control etc.) The intent
   * of this method is to separate control creation and layout from detailed
   * initialization.
   */
  protected void initializeControls()
  {
  }

  /** Call this method to initialize any "data binding" logic. */
  protected void initDataBinding()
  {
  }

  /** This method is called from initPane(), and setInput(), and possibly
    * from your listeners. Implement this method if you have controls that need
    * be enabled or disabled based on the state associated with the "input"
    * object.
    */
  protected void enableDisableControls()
  {
  }

  /** This method is called by the standard initPane() method.
    * Implement method to add all of the "listeners" used by this Pane.
   */
  protected void addListeners()
  {
  }

  /**
   * Subclass can override this function to disable the form-ui LnF.
   * @param parent the control whose children will LnF like Form
   * @param formToolkit form tool kit singleton.
   */
  protected void adaptControls(Composite parent){
  	UIUtil.formizeChildControls(parent,null);
  }
  
  @Override
  public void dispose(){
    m_observableManager.dispose();
    m_isDisposed = true;
    super.dispose();
  }
 
  protected DataBindingContext m_dbc;// = new DataBindingContext();
  protected ObservablesManager m_observableManager=new ObservablesManager();
  private volatile boolean m_isDisposed = false;

}