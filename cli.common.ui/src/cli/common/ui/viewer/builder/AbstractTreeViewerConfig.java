/**
 * Copyright (c) 2011 Chengdong Li.
 * All rights reserved.
 */

package cli.common.ui.viewer.builder;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Tree viewer has an ObservableListTreeContentProvider by default.
 * 
 * @author chengdong
 *
 */
public abstract class AbstractTreeViewerConfig extends AbstractColumnViewerConfig
{

  public AbstractTreeViewerConfig(ColumnViewer viewer)
  {
    super(viewer);
  }

  @Override
  protected void setOptionalConfig(){
    super.setOptionalConfig();
    
    getColumnViewer().getTree().setHeaderVisible( true );
    getColumnViewer().getTree().setLinesVisible( false );
    
  }

  @Override
  protected TreeViewer getColumnViewer(){
    return (TreeViewer) super.getColumnViewer();
  }
  
  @Override
  final protected ColumnViewer disposeViewerColumns(){
  	if(getColumnViewer()==null || getColumnViewer().getTree().isDisposed())
  		return null;
  	
    TreeColumn[] columns = getColumnViewer().getTree().getColumns();
    
    getColumnViewer().getTree().removeAll();
    for(int i=columns.length-1;i>=0;i--){ // reverse dispose to avoid flicker
      TreeColumn col = columns[i];
      col.dispose();
    }
    getColumnViewer().refresh();
    
    return getColumnViewer();
  }
  
  @Override
  public void packViewerColumns()
  {
    Tree tree = getColumnViewer().getTree();
//    ColumnConfig[] configs = getColumnConfigs();
    for (int i = 0, n = tree.getColumnCount(); i < n; i++)
    {
      TreeColumn col = tree.getColumn(i);
      col.pack();
    }
  }

  public void resetViewerColumnWidth()
  {
    Tree tree = getColumnViewer().getTree();
    ColumnConfig[] configs = getColumnConfigs();
    for (int i = 0, n = tree.getColumnCount(); i < n; i++)
    {
      TreeColumn col = tree.getColumn(i);
      col.setWidth(configs[i].getWidth());
    }
  }
  

}
