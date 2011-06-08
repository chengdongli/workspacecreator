/**
 * Copyright (c) 2011 Chengdong Li.
 * All rights reserved.
 */

package cli.common.ui.viewer.builder;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;

/**
 * ColumnConfig is used for customizing the column of a column viewer using
 * databinding.
 * 
 * @author chengdong
 *
 */
abstract public class ColumnConfig
{
  abstract protected ViewerColumn createColumn(ColumnViewer viewer);
  abstract protected void configColumn(ViewerColumn column);
  abstract protected ColumnViewer getViewer();
  
  protected String getTitle(){return "";}
  protected String getColumnTooltip(){return null;}
  protected EditingSupport createEditingSupport(){return null;}
  protected CellLabelProvider createLabelProvider(){return null;}
  
  public final ViewerColumn createAndConfigColumn(ColumnViewer viewer){
    this.viewerColumn=createColumn(viewer);
    configColumn(this.viewerColumn);
    return this.viewerColumn;
  }
  
  protected int getStyle(){return SWT.BEGINNING;}
  protected int getWidth(){return 100;}
	protected void dispose() {this.viewerColumn=null;}
	
	protected ViewerColumn getViewerColumn(){return this.viewerColumn;}
	
	protected Comparator<Object> createComarator(){return null;}
  protected Comparator<Object> getComparator(){return this.comparator;}
  protected void setComparator(Comparator<Object> cp){this.comparator=cp;}
  protected boolean isDefaultSorter(){return false;}
  protected boolean allowToggleSort(){return true;}
	
	public Object getProperty(String key){
		return properties.get(key);
	}

	public void setProperty(String key, Object value){
		properties.put(key,value);
	}

	private Map<String, Object> properties=new HashMap<String, Object>();
	private ViewerColumn viewerColumn;
	private Comparator<Object> comparator; 
}
