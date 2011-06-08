package cli.workspacecreator.wizards;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.ProgressMonitorPart;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * A WziardDialog with default width and height and centered on display.
 * <p/>
 * We fully hide the ProgressionMonitorPart if you pass showProgressionPart 
 * false to the constructor. 
 * 
 * @author chengdong
 *
 */
public class CommonWizardDialog
  extends
    WizardDialog
{

  public static final int DEF_WIDTH=800;
  public static final int DEF_HEIGHT=600;
 
  public CommonWizardDialog(
    Shell parentShell, IWizard newWizard)
  {
    this(parentShell, newWizard, DEF_WIDTH, DEF_HEIGHT);
  }
  
  public CommonWizardDialog(
    Shell parentShell, IWizard newWizard, int width, int height)
  {
    this(parentShell, newWizard, width, height, false);
  }

  public CommonWizardDialog(
    Shell parentShell, IWizard newWizard, int width, int height, boolean showProgressionPart)
  {
    super(parentShell, newWizard);
    m_width = width;
    m_height = height;
    m_showProgressionPart=showProgressionPart;
  }

  @Override
  protected Control createDialogArea(Composite parent) {
    Composite composite=(Composite) super.createDialogArea(parent);
    
    // This is a hack to just totally remove the  the progress monitor part.
    for(Control control: composite.getChildren()){
      if(control instanceof ProgressMonitorPart){
        Object gd = control.getLayoutData();
        Assert.isTrue(gd instanceof GridData);
        if(!m_showProgressionPart){
          ((GridData)gd).heightHint=0;
        }
        break;
      }
    }
    composite.layout();
    return composite;
  };
  
  @Override
  protected void configureShell(Shell shell)
  {
    super.configureShell(shell);
    shell.setSize(m_width, m_height);
    Rectangle rect = shell.getDisplay().getClientArea();
    shell.setLocation((rect.width-shell.getSize().x)/2, 
      (rect.height-shell.getSize().y)/2);
  }

  private int m_width;
  private int m_height;
  private boolean m_showProgressionPart;

}
