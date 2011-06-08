package cli.common.ui.viewer.builder;

import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ViewerColumn;

public interface IColumnViewerConfig {

	public abstract void packViewerColumns();

	public abstract ViewerColumn[] createAndConfigColumns();

	public abstract void dispose();

	public abstract IContentProvider getContentProvider();
}