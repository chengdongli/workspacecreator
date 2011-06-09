/**
 * Copyright (c) 2011 Chengodng Li.
 * All rights reserved.
 */
package cli.common.ui.viewer.builder;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * Utility class for viewer related operations.
 * 
 * @author chengdong
 *
 */
public class ViewerUtil
{

  public static boolean isReadonlyStyle(ColumnViewer viewer){
    if(viewer==null)
      return false;
    if(viewer instanceof TreeViewer){
      return ((((TreeViewer)viewer).getTree().getStyle() & SWT.READ_ONLY)!=0);
    }else if(viewer instanceof TableViewer){
      return ((((TableViewer)viewer).getTable().getStyle() & SWT.READ_ONLY)!=0);      
    }
    return false;
  }

  /**
   * Make columns with fit maximum width of the items it holds.
   * @param viewer tree viewer or table viewer
   */
  public static void packColumns(StructuredViewer viewer)
  {
    if(viewer instanceof TreeViewer){
      Tree tree = ((TreeViewer)viewer).getTree();
      for (int i = 0, n = tree.getColumnCount(); i < n; i++)
      {
        TreeColumn col = tree.getColumn(i);
        col.pack();
      }
    }else if(viewer instanceof TableViewer){
      Table table = ((TableViewer)viewer).getTable();
      for (int i = 0, n = table.getColumnCount(); i < n; i++)
      {
        TableColumn col = table.getColumn(i);
        col.pack();
      }
    }
  }
  
  public static void packColumnsIfFirstime(StructuredViewer viewer){
    final String KEY="ts.firstTime";
    Control control = viewer.getControl();
    if(control!=null){
      Boolean first=(Boolean) control.getData(KEY);
      if(first==null || first){
        control.setData(KEY,false);
        packColumns(viewer);
      }
    }
  }
  
}
