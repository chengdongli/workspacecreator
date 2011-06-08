package cli.workspacecreator.wizards;

import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * Wizard which will notify the page when page is about to show.
 * @author chengdong
 *
 */
public abstract class CommonWizard extends Wizard implements IPageChangedListener
{
  public CommonWizard()
  {
    super();
  }
  
  @Override
  public void pageChanged(PageChangedEvent event)
  {
    if(event.getSelectedPage() instanceof CommonWizardPage)
    {
      CommonWizardPage page = (CommonWizardPage)event.getSelectedPage();
      page.pageAboutToShow(event);
    }
  }

  @Override
  public void setContainer(IWizardContainer wizardContainer)
  {
    IWizardContainer oldContainer = getContainer();
    if (oldContainer instanceof IPageChangeProvider)
    {
      ((IPageChangeProvider)oldContainer).removePageChangedListener(this);
    }

    super.setContainer(wizardContainer);

    if(wizardContainer instanceof IPageChangeProvider)
    {
      ((IPageChangeProvider)wizardContainer).addPageChangedListener(this);
    }
  }

  @Override
  public void dispose()
  {
    IWizardContainer container = getContainer();
    if (container instanceof IPageChangeProvider)
    {
      ((IPageChangeProvider)container).removePageChangedListener(this);
    }
    super.dispose();
  }

  public Point getDialogSize(){
    return new Point(600,600);
  }

  final public int open(Shell shell){
    WizardDialog dialog = new WizardDialog(shell, this);
    dialog.create();  
    dialog.getShell().setSize(getDialogSize());
    return dialog.open();
  }

  public final void updateButtons()
  {
    IWizardContainer container = getContainer();
    if (container != null && container.getCurrentPage() != null) {
      container.updateButtons();
    }
  }
}
