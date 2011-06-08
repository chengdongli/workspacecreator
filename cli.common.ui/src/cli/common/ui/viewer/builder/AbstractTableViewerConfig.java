/**
 * Copyright (c) 2011 Chengdong Li.
 * All rights reserved.
 */

package cli.common.ui.viewer.builder;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

/**
 * Table viewer has an ObservableListContentProvider.
 * 
 * @author chengdong
 *
 */
public abstract class AbstractTableViewerConfig extends AbstractColumnViewerConfig
{

  public static void configCellEditingBehavior(TableViewer viewer){
    class EditorActivationStrategy extends ColumnViewerEditorActivationStrategy
    {
      private EditorActivationStrategy(final ColumnViewer viewer)
      {
        super(viewer);
        setEnableEditorActivationWithKeyboard(true);
      }
      protected boolean isEditorActivationEvent(
        final ColumnViewerEditorActivationEvent event)
      {
        boolean result;
        if (event.character == '\r')
        {
          result = true;
        }
        else
        {
          result = super.isEditorActivationEvent(event);
        }
        return result;
      }
    }
    
    // Focus support
    ColumnViewerEditorActivationStrategy as = new EditorActivationStrategy(
        viewer);

    FocusCellOwnerDrawHighlighter hl = new FocusCellOwnerDrawHighlighter(
      viewer);
    TableViewerFocusCellManager fm = new TableViewerFocusCellManager(
      viewer, hl); // could add a customized CellNavigationStrategy.

    int feature = ColumnViewerEditor.TABBING_HORIZONTAL
        | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
        | ColumnViewerEditor.KEEP_EDITOR_ON_DOUBLE_CLICK;
    
//    int feature = ColumnViewerEditor.TABBING_HORIZONTAL
//    | ColumnViewerEditor.TABBING_CYCLE_IN_ROW
//    | ColumnViewerEditor.KEEP_EDITOR_ON_DOUBLE_CLICK;

//        | ColumnViewerEditor.TABBING_CYCLE_IN_ROW;
    TableViewerEditor.create(viewer, fm, as, feature);
  }
  
  public AbstractTableViewerConfig(ColumnViewer viewer)
  {
    super(viewer);
  }

  @Override
  protected IStructuredContentProvider createContentProvider()
  {
    return new ArrayContentProvider();
  }
  
  @Override
  protected void setOptionalConfig(){
    super.setOptionalConfig();
    
    ColumnViewerToolTipSupport.enableFor(getColumnViewer());
    getColumnViewer().getTable().setHeaderVisible( true );
    getColumnViewer().getTable().setLinesVisible( false );
    
  }

  @Override
  protected TableViewer getColumnViewer(){
    return (TableViewer) super.getColumnViewer();
  }

  @Override
  final protected ColumnViewer disposeViewerColumns(){
  	if(getColumnViewer()==null || getColumnViewer().getTable().isDisposed())
  		return null;
  	
    TableColumn[] columns = getColumnViewer().getTable().getColumns();

    getColumnViewer().getTable().removeAll(); // a fix for cocoa. when dispose columns, cocoa always compute the scroll width.
    for(int i=columns.length-1;i>=0;i--){ // reverse dispose to avoid flicker
      TableColumn col = columns[i];
      col.dispose();
    }
    
    getColumnViewer().refresh();
    return getColumnViewer();
  }
  
  @Override
  public void packViewerColumns()
  {
    Table table = getColumnViewer().getTable();
    for (int i = 0, n = table.getColumnCount(); i < n; i++)
    {
      TableColumn col = table.getColumn(i);
      col.pack();
    }
  }
  
}
