package cli.workspacecreator.wizards;

import cli.workspacecreator.Util;

import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import java.util.ArrayList;
import java.util.List;

/**
 * Model class containing the data for setup
 */
public class SetupModel 
{
    public String localFolder="/tmp/svn";
    public boolean fetchSource=true;
    public String repoUrl="svn+ssh://hera/opt2/svn/test/branches/MarketPlaceManagementSuite";
    public String userName="chengdong";
    public String password="";//="chengdong";
    public List<String> projectNames=new ArrayList<String>();

	public String toString()
	{
		StringBuilder sb=new StringBuilder();
		sb.append(String.format("localFolder: %s\n", localFolder));
        sb.append(String.format("fetchSource: %b\n",fetchSource));
        sb.append(String.format("repo: %s\n",repoUrl));
        sb.append(String.format("userName: %s\n", userName));
        sb.append(String.format("password: %s\n", password));
        sb.append("projects:\n");
        for(String prj: projectNames){
            sb.append("    "+prj+"\n");
        }
		return sb.toString();	
	}
	
	public IStatus validate(){
	    if(projectNames.size()==0)
	        return ValidationStatus.error("No projects are selected!");

	    if(!Util.folderReadable(localFolder))
	        return ValidationStatus.error("Please select a valid folder!");
	    
	    if(fetchSource){
	        if(repoUrl.isEmpty())
	            return ValidationStatus.error("Please enter a valid url to SVN repository!");
            if(userName.isEmpty())
                return ValidationStatus.error("Please enter a non-empty user name!");
            if(password.isEmpty())
                return ValidationStatus.error("Please enter a non-empty password!");
	    }
	    return Status.OK_STATUS;
	}
}
