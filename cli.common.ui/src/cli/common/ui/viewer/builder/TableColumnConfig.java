/**
 * Copyright (c) 2011 Chengdong Li.
 * All rights reserved.
 */

package cli.common.ui.viewer.builder;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Table;

/**
 * Column configuration for Table viewer.
 * 
 * @author chengdong
 *
 */
public abstract class TableColumnConfig extends ColumnConfig
{
	private TableViewer m_viewer;
	
	public TableColumnConfig(TableViewer viewer){
		this.m_viewer=viewer;
	}
	
	@Override
	final protected TableViewer getViewer() {
		return m_viewer;
	}
	
	@Override
	final protected TableViewerColumn getViewerColumn(){
	  return (TableViewerColumn) super.getViewerColumn();
	}
	
  @Override
  protected TableViewerColumn createColumn(ColumnViewer viewer)
  {
    Assert.isTrue(viewer instanceof TableViewer,"Error: viewer is not a TableViewer");
    return new TableViewerColumn((TableViewer) viewer, getStyle());
  }

  @Override
  final protected void configColumn(ViewerColumn col)
  {
    Assert.isTrue(col instanceof TableViewerColumn,"Error: viewer column is not a TableViewerColumn");

    final TableViewerColumn column=(TableViewerColumn) col;
    column.getColumn().setWidth(getWidth());
    column.getColumn().setMoveable(true);
    column.getColumn().setResizable(true);
    column.getColumn().setText(getTitle());
    String colTooltip = getColumnTooltip();
    if(colTooltip!=null)
      column.getColumn().setToolTipText(colTooltip);
    
    CellLabelProvider labelProvider = createLabelProvider();
    if(labelProvider!=null)
      column.setLabelProvider(labelProvider);
    else
      column.setLabelProvider(new ColumnLabelProvider());

    EditingSupport editingSupport=createEditingSupport();
    if(!ViewerUtil.isReadonlyStyle(column.getViewer()) && editingSupport!=null)
      column.setEditingSupport(editingSupport);
        
    // additional functionality for dynamic sorts
    setComparator(createComarator());
    if(getComparator()!=null){
      if(allowToggleSort()){
        column.getColumn().addSelectionListener( new SelectionAdapter(){
          @Override
          public void widgetSelected( SelectionEvent e ) {
            sort();
          }
        });
      }
      if(isDefaultSorter()){
        getViewer().setComparator( new ViewerComparator(){
          @Override
          public int compare( Viewer viewer, Object e1, Object e2 ) {
            return TableColumnConfig.this.getComparator().compare( e1, e2 );
          }
        });
      }
    }
  }

  private void sort()
  {
    // select sort direction - change if same column
    int sortDirection = SWT.UP;
    Table table = getViewer().getTable();
    if ( getViewerColumn().getColumn() == table.getSortColumn() ) {
      if ( SWT.UP == table.getSortDirection() ) {
        sortDirection = SWT.DOWN;
      }
    }
    final int sortMultipler = (sortDirection == SWT.UP ) ? 1 : -1;
    try {
      table.setRedraw( false );
      table.setSortColumn( getViewerColumn().getColumn() );
      table.setSortDirection( sortDirection );
      getViewer().setComparator( new ViewerComparator(){
        @Override
        public int compare( Viewer viewer, Object e1, Object e2 ) {
          return sortMultipler *TableColumnConfig.this.getComparator().compare( e1, e2 );
        }
      });
    }
    finally {
      table.setRedraw( true ); // we have to call this to refresh table
    }
  }

}
