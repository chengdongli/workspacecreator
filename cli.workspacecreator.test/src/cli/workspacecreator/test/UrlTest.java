
package cli.workspacecreator.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class UrlTest {
    @Test
    public void testURL1() throws Exception {
        String strUrl = "http://stackoverflow.com/about";

        try {
            URL url = new URL(strUrl);
            HttpURLConnection urlConn = (HttpURLConnection)url.openConnection();
            urlConn.connect();

            assertEquals(HttpURLConnection.HTTP_OK, urlConn.getResponseCode());
        } catch (IOException e) {
            System.err.println("Error creating HTTP connection");
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    public void testURL2() {
        try {
            URL url = new URL("http://hera/downloads/targetplatform/rcp-projectSet.psf");
            URLConnection conn = url.openConnection();
            conn.connect();
        } catch (MalformedURLException e) {
            assertTrue(e.getLocalizedMessage(),false);
        } catch (IOException e) {
            assertTrue(e.getLocalizedMessage(),false);
        }

    }

    @Test
    public void testURL3() {
        try {
            URL url = new URL("file:/tmp");
            URLConnection conn = url.openConnection();
            conn.connect();
        } catch (MalformedURLException e) {
            assertTrue(e.getLocalizedMessage(),false);
        } catch (IOException e) {
            assertTrue(e.getLocalizedMessage(),false);
        }

    }

}
