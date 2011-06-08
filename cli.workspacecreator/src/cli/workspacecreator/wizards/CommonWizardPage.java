/**
 * Copyright (c) 2009 Tradescape Corporation.
 * All rights reserved.
 */
package cli.workspacecreator.wizards;

import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;

/**
 * Wizard page for for CommonWizard.
 * @author chengdong
 *
 */
public abstract class CommonWizardPage extends WizardPage
{
  protected CommonWizardPage(
    String pageName, String title, ImageDescriptor titleImage)
  {
    super(pageName, title, titleImage);
  }

  @Override
  public CommonWizard getWizard()
  {
    return (CommonWizard) super.getWizard();
  }

  /** Called when this page is about to to be "shown" by the Wizard.
   * 
   * @param event The "page changed event".
   */
  protected void pageAboutToShow(PageChangedEvent event)
  {
  }
 
}
