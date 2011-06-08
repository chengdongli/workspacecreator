/**
 * Copyright (c) 2011 Chengdong Li.
 * All rights reserved.
 */
package cli.common.ui.viewer.builder;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

import java.util.Comparator;

/**
 * Table viewer column config for a general table viewer.
 * 
 * @author chengdong
 *
 */
public class GeneralTableColumnConfig extends TableColumnConfig {

	public GeneralTableColumnConfig(TableViewer viewer) {
		super(viewer);
	}
	
	@Override
	protected String getTitle()
	{
		return ICommonConstants.ms_emptyString;
	}
	
	@Override
	protected Comparator<Object> createComarator(){
		return new Comparator<Object>() {
			@Override
			public int compare(Object o1, Object o2) {
				return getText(o1).compareTo(getText(o2));
			}
		};
	}
	
	@Override
	protected ColumnLabelProvider createLabelProvider()
	{
		
		ColumnLabelProvider provider = new ColumnLabelProvider(){
			@Override
			public String getToolTipText(Object element) {
				return getTooltipText(element);
			}
      @Override
      public void update(ViewerCell cell)
      {
      	Object element = cell.getElement();
      	cell.setText(getText(element));
      	Image image = getImage(element);
      	cell.setImage(image);
      	cell.setBackground(getBackground(element));
      	cell.setForeground(getForeground(element));
      	cell.setFont(getFont(element));
      }
      
    	public Font getFont(Object element) {
    		return GeneralTableColumnConfig.this.getFont(element);
    	}

    	public Color getBackground(Object element) {
    		return GeneralTableColumnConfig.this.getBackground(element);
    	}

    	public Color getForeground(Object element) {
    		return GeneralTableColumnConfig.this.getForeground(element);
    	}

    	public Image getImage(Object element) {
    		return GeneralTableColumnConfig.this.getImage(element);
    	}

    	public String getText(Object element) {
    		return GeneralTableColumnConfig.this.getText(element);
    	}
		};
		return provider;
	}
	
//////////////////////////// Override these iff NOT overriding createLabelProvider() ///////// 
	protected Font getFont(Object element) {
		return null;
//		if(m_fontRegistry==null){
//			m_fontRegistry = new FontRegistry(m_viewer.getControl().getShell().getDisplay());
//			m_fontRegistry.put("BOLD", new FontData[]{new FontData("Arial", 9, SWT.BOLD)} );
//		}
//		
//		return m_fontRegistry.get("BOLD");
	}

	protected Color getForeground(Object element) {
		return null;
	}

	protected Color getBackground(Object element) {
		return null;
	}

	protected String getTooltipText(Object element) {
		return null;
	}

	/** override this if you want customize the editing support */
	protected boolean canEdit(Object element) {
		return true;
	}
	
	protected Image getImage(Object element) {
		return null;
	}

	protected String getText(Object element) {
		return element == null ? ICommonConstants.ms_emptyString : element.toString();//$NON-NLS-1$
	}
//////////////////////////////////////////////////////////////////////////
	
	@Override
	protected EditingSupport createEditingSupport()
	{
		return new EditingSupport(getViewer()) {
			
			@Override
			protected void setValue(Object element, Object value) {
				setValueFromCellEditor(element, value);
			}
			
			@Override
			protected Object getValue(Object element) {
				return getValueForCellEditor(element);
			}
			
			@Override
			protected CellEditor getCellEditor(Object element) {
				return getCellEditorFor(element);
			}
			
			@Override
			protected boolean canEdit(Object element) {
				return GeneralTableColumnConfig.this.canEdit(element);
			}
		};
		
	}

	/**
	 * 
	 * @param modelElement
	 * @return cell editor for the modelElement
	 */
	protected CellEditor getCellEditorFor(Object modelElement) {
		return null;
	}

	/**
	 * @param modelElement
	 * @return a value for cell editing for modelElement. You need
	 *   to take care of converting the modelElement suitable for 
	 *   your celleditor editing. 
	 */
	protected Object getValueForCellEditor(Object modelElement) {
		return null;
	}

	/**
	 * Set the cell editing newValue to the modelElement.
	 * You need to take care of conversion.
	 * @param modelElement
	 * @param newValue
	 */
	protected void setValueFromCellEditor(Object modelElement, Object newValue) {
	}

}


