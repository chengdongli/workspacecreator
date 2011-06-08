package cli.workspacecreator;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

public class Util {

    public static List<String> getProjectNames(String urlStr) {
        List<String> names = new ArrayList<String>();
        InputStream stream = null;
        try
        {
          URL url = new URL(urlStr);
          stream = url.openStream();
          stream = new BufferedInputStream(stream);
          DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
          domFactory.setNamespaceAware(true);
          DocumentBuilder builder = domFactory.newDocumentBuilder();
          Document doc = builder.parse(stream);
  
          @SuppressWarnings("unused")
          boolean a=getProjectsFromLaunch(doc,names)
          ||getProjectsFromTeamset(doc,names);
        }
        catch (Exception e)
        {
        }
        finally
        {
          try
          {
            stream.close();
          }
          catch (Exception e)
          {
          }
        }

        Collections.sort(names);
        print(names);
        return names;
    }
    
    public static List<String> getProjectNames(InputStream stream) {
        List<String> names = new ArrayList<String>();
        try{
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(stream);
    
            @SuppressWarnings("unused")
            boolean a=getProjectsFromLaunch(doc,names)
            ||getProjectsFromTeamset(doc,names);
            
        }catch(Exception ex){
        }
        Collections.sort(names);
        print(names);
        return names;
    }

    private static boolean getProjectsFromTeamset(Document doc, List<String> names) {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();

            // treat as .psf file
            XPathExpression expr = xpath
                .compile("/psf/provider[@id='org.eclipse.team.svn.core.svnnature']/project/@reference");
            Object result = expr.evaluate(doc, XPathConstants.NODESET);

            if (result != null) {
                NodeList node = (NodeList)result;
                for (int i = 0; i < node.getLength(); i++) {
                    String[] infos = node.item(i).getNodeValue().split(",");
                    String name = infos[2];
                    names.add(name);
                }
                return true;
            }

        } catch (XPathExpressionException xpe) {

        }
        return false;
    }

    private static boolean getProjectsFromLaunch(Document doc, List<String> names) {
        try {
            XPath xpath = XPathFactory.newInstance().newXPath();

            // treat as .launch file
            XPathExpression expr = xpath
                    .compile("/launchConfiguration/stringAttribute[@key='selected_workspace_plugins']/@value");
            Object result = expr.evaluate(doc, XPathConstants.NODE);
            
            if(result==null){
                expr = xpath
                .compile("/launchConfiguration/stringAttribute[@key='workspace_bundles']/@value");
                result = expr.evaluate(doc, XPathConstants.NODE);
            }

            if (result != null) {
                Node node = (Node)result;
                String[] prjs = node.getNodeValue().split(",");
                for (String prj : prjs) {
                    if(prj.contains("@")){
                        String name = prj.substring(0, prj.indexOf("@"));
                        names.add(name.trim());
                    }else{
                        names.add(prj.trim());
                    }
                }
                return true;
            }
        } catch (XPathExpressionException xpe) {

        }
        return false;
    }

    public static IStatus importProjectToWorkspace(IProgressMonitor monitor, String prjFullPath) {
        try {
            String prjFile=prjFullPath+File.separator+".project";
            if(!new File(prjFile).exists()){
                return ValidationStatus.error(prjFile+" does not exists!");
            }
            IProjectDescription description = ResourcesPlugin.getWorkspace().loadProjectDescription(
                    new Path(prjFullPath + "/.project"));
            IProject project = ResourcesPlugin.getWorkspace().getRoot()
            .getProject(description.getName());
            if (!project.exists())
                project.create(description, null);
            project.open(null);
            return Status.OK_STATUS;
        } catch (CoreException e) {
            e.printStackTrace();
            return new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getLocalizedMessage());
        }  

    }

    public static void print(List<String> list){
        StringBuilder sb=new StringBuilder();
        for(int i=0;i<list.size();i++){
            sb.append(list.get(i)+"\n");
        }
        System.out.println(sb.toString());
        System.out.println("total: "+list.size());
    }

    public static String getParentFolder(String path) {
        int index=path.lastIndexOf(File.separator);
        if(index>0)
            return path.substring(0, index);
        else
            return path;
    }
    
    public static String testUrl(String urlStr){
        try {
            URL url = new URL(urlStr);
            
//            String hostName = url.getHost();
//            int port = url.getPort();
//            InetSocketAddress sockAddr = new InetSocketAddress(hostName, port);
//            System.out.println("Pinging CDO Server");
//            Socket socket = new Socket();
//            try {
//                socket.connect(sockAddr, 5000); // 5 seconds as threshold
//                return null;
//            } catch (Exception ex) {
//                return ex.getLocalizedMessage();
//            } finally {
//                if (!socket.isClosed()) {
//                    try {
//                        socket.close();
//                    } catch (final IOException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
            URLConnection conn = url.openConnection();
            conn.setConnectTimeout(5000);
            conn.connect();
            return null;
        } catch (MalformedURLException e) {
            return e.getLocalizedMessage();
        } catch (SocketTimeoutException e){
            return e.getLocalizedMessage();
        } catch (IOException e) {
            return e.getLocalizedMessage();
        }
    }
    
    public static boolean fileReadable(String path){
        File file = new File(path);
        if (!file.isFile() || !file.exists() || !file.canRead()) {
            return false;
        }
        return true;
    }
    
    public static boolean folderReadable(String path){
        File file = new File(path);
        if (!file.isDirectory() || !file.exists() || !file.canRead()) {
            return false;
        }
        return true;
    }
}
