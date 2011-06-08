package cli.workspacecreator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.ui.IStartup;

public class Starter implements IStartup {

	@Override
	public void earlyStartup() {
		System.out.println("Early start up ...");

		IProjectDescription description = null;
		try {
			
			String repoFolder=System.getProperty("repoFolder");
			System.out.println("repoFolder="+repoFolder);
			
			String PROJECT_PATH = "/workspaces/mmt-rcp/org.eclipse.emf";
			description = ResourcesPlugin.getWorkspace()
					.loadProjectDescription(
							new Path(PROJECT_PATH + "/.project")); //$NON-NLS-1$ 
			IProject project = ResourcesPlugin.getWorkspace().getRoot()
					.getProject(description.getName());
			if(!project.exists())
				project.create(description, null);
			project.open(null);
			loadConfig();
		} catch (CoreException exception_p) {
			exception_p.printStackTrace();
		}

	}

	/**
	 * 
	 */
	private void loadConfig() {
//	    String path = System.getProperty("biz.tradescape.config");
//	    if (path != null)
//	    {
//	      loader = getLoaderFromPath(path);
//	    }		
//	    FileInputStream is = null;
//	    try
//	    {
//	      File xmlFile = (File) options.get(OPTION_FILE);
//
//	      is  = new FileInputStream(xmlFile);
//	      final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
//	      final DocumentBuilder docBuilder = dbf.newDocumentBuilder();
//	      final Document doc = docBuilder.parse(is);
//	      final Element documentElement = doc.getDocumentElement();
//	      final String localName = documentElement.getNodeName();
//	      if (!"Context".equals(localName))
//	      {
//	        throw new IllegalStateException("Doc element must be Context");
//	      }
//	      
//	      final NodeList childNodes = documentElement.getElementsByTagName("Environment");
//	      for (int i = 0; i < childNodes.getLength(); i++)
//	      {
//	        final Element item = (Element) childNodes.item(i);
//	        final String name = item.getAttribute("name");
//	        if (!isValidAttribute(name))
//	        {
//	          System.err.println("Ignoring element with missing name");
//	          continue;
//	        }
//	        final String value = item.getAttribute("value");
//	        final String typename = item.getAttribute("type");
//	        if (!isValidAttribute(value) || !isValidAttribute(typename))
//	        {
//	          System.err.println("Element must have both value and typename");
//	          continue;
//	        }
//	        try
//	        {
//	          final Object parsedValue = parseValue(value, typename);
//	          if (parsedValue != null)
//	          {
//	            _lookup.put(name, parsedValue);
//	          }
//	        }
//	        catch (final Exception e)
//	        {
//	          Activator.logError(e);
//	        }
//	      }
//	    }
//	    catch (final Exception e)
//	    {
//	      Activator.logError(e);
//	      // failed so ensure _lookup is clear.  this will force defaults to be used
//	      _lookup.clear();
//	    }
//	    finally
//	    {
//	      if (is != null)
//	      {
//	        try
//	        {
//	          is.close();
//	        }
//	        catch (final IOException e)
//	        {
//	          Activator.logError(e);
//	        }
//	      }
//	    }
	}

	
}
