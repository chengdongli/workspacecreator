
package cli.workspacecreator.wizards;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Shell;

/**
 * A WziardDialog with default width and height and centered on display.
 * <p/>
 * 
 * @author chengdong
 */
public class SetupWizardDialog extends WizardDialog {

    public static final int DEF_WIDTH = 800;

    public static final int DEF_HEIGHT = 600;

    private int m_width;

    private int m_height;

    public SetupWizardDialog(Shell parentShell, IWizard newWizard) {
        this(parentShell, newWizard, DEF_WIDTH, DEF_HEIGHT);
    }

    public SetupWizardDialog(Shell parentShell, IWizard newWizard, int width, int height) {
        super(parentShell, newWizard);
        m_width = width;
        m_height = height;
    }

    @Override
  protected void configureShell(Shell shell)
  {
    super.configureShell(shell);
    shell.setSize(m_width,m_height);
    Rectangle rect = shell.getDisplay().getClientArea();
    shell.setLocation((rect.width-shell.getSize().x)/2, 
      (rect.height-shell.getSize().y)/2);
  }

//    @Override
//  protected Control createContents(Composite parent) {
//      Control control = super.createContents(parent);
//      for(IWizardPage page:getWizard().getPages()){
//          if(page instanceof AbstractSetupWizardPage){
//              ((AbstractSetupWizardPage)page).restoreSettings();
//          }
//      }
//      return control;
//  }
    
}
