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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Tree;

/**
 * Column configuration for Tree viewer.
 * 
 * @author chengdong
 *
 */
public abstract class TreeColumnConfig extends ColumnConfig
{
	private TreeViewer m_viewer;
	
	public TreeColumnConfig(TreeViewer viewer){
		this.m_viewer=viewer;
	}
	
	@Override
	final protected TreeViewer getViewer() {
		return m_viewer;
	}
	
  @Override
  final protected TreeViewerColumn getViewerColumn()
  {
    return (TreeViewerColumn) super.getViewerColumn();
  }

  @Override
  protected TreeViewerColumn createColumn(ColumnViewer viewer)
  {
    Assert.isTrue(viewer instanceof TreeViewer,"Error: viewer is not a TreeViewer");
    return new TreeViewerColumn((TreeViewer) viewer, getStyle());
  }

  @Override
  protected void configColumn(ViewerColumn col)
  {
    Assert.isTrue(col instanceof TreeViewerColumn,"Error: viewer column is not a TreeViewerColumn");

    final TreeViewerColumn column=(TreeViewerColumn) col;
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
            return TreeColumnConfig.this.getComparator().compare( e1, e2 );
          }
        });
      }
    }
  }

  private void sort(){
    // select sort direction - change if same column
    int sortDirection = SWT.UP;
    Tree tree = getViewer().getTree();
    if ( getViewerColumn().getColumn() == tree.getSortColumn() ) {
      if ( SWT.UP == tree.getSortDirection() ) {
        sortDirection = SWT.DOWN;
      }
    }
    final int sortMultipler = (sortDirection == SWT.UP ) ? 1 : -1;
    try {
      tree.setRedraw( false );
      tree.setSortColumn( getViewerColumn().getColumn() );
      tree.setSortDirection( sortDirection );
      getViewer().setComparator( new ViewerComparator(){
        @Override
        public int compare( Viewer viewer, Object e1, Object e2 ) {
          return sortMultipler * TreeColumnConfig.this.getComparator().compare( e1, e2 );
        }
      });
    }
    finally {
      tree.setRedraw( true ); // we have to call this to refresh tree
    }
  }
  
}
