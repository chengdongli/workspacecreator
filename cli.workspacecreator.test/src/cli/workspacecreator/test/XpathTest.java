
package cli.workspacecreator.test;

import cli.workspacecreator.Util;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;

public class XpathTest {

    @Test
    public void testLoadLaunchConfiguration() throws IOException {
        getProjectNames("/resources/Marketplace RCP.launch");
//        getProjectNames("/resources/rcp_projectSet.psf");
    }

    private void getProjectNames(String resourcePath) {
        InputStream stream = getClass().getResourceAsStream(resourcePath);
        Util.getProjectNames(stream);
        try {
            stream.close();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    @Test
    public void testLoadUrl(){
       Util.getProjectNames("http://hera/downloads/targetplatform/rap-projectSet.psf");
    }
}
