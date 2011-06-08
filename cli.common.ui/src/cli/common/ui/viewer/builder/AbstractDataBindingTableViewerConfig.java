/**
 * Copyright (c) 2011 Chengdong Li.
 * All rights reserved.
 */

package cli.common.ui.viewer.builder;

import org.eclipse.jface.databinding.viewers.ObservableListContentProvider;
import org.eclipse.jface.viewers.ColumnViewer;

/**
 * A general config for NonDatabinding Table viewer. 
 * 
 * @author chengdong
 *
 */
public abstract class AbstractDataBindingTableViewerConfig extends AbstractTableViewerConfig
{

  public AbstractDataBindingTableViewerConfig(ColumnViewer viewer)
  {
    super(viewer);
  }

  @Override
  final protected ObservableListContentProvider createContentProvider()
  {
    return new ObservableListContentProvider();
  }
  
  final public ObservableListContentProvider getContentProvider(){
	  return (ObservableListContentProvider) super.getContentProvider();
  }

}
