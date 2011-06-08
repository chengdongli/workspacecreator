/**
 * Copyright (c) 2011 Chengdong Li.
 * All rights reserved.
 */

package cli.common.ui.viewer.builder;

import org.eclipse.core.databinding.observable.masterdetail.IObservableFactory;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.TreeStructureAdvisor;
import org.eclipse.jface.viewers.ColumnViewer;

/**
 * A general config for NonDatabinding Tree viewer. 
 * 
 * @author chengdong
 *
 */
public abstract class AbstractDataBindingTreeViewerConfig extends AbstractTreeViewerConfig
{

  public AbstractDataBindingTreeViewerConfig(ColumnViewer viewer)
  {
    super(viewer);
  }

  @Override
  final protected ObservableListTreeContentProvider createContentProvider()
  {
    return new ObservableListTreeContentProvider(createObservableFactory(),
      createTreeStructureAdvisor());
  }
  
  final public ObservableListTreeContentProvider getContentProvider(){
	  return (ObservableListTreeContentProvider) super.getContentProvider();
  }

  abstract protected IObservableFactory createObservableFactory();
  abstract protected TreeStructureAdvisor createTreeStructureAdvisor();

}
