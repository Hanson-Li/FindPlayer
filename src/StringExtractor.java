import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Vector;

import org.htmlparser.beans.LinkBean;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.htmlparser.visitors.HtmlPage;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.Node;
/**
 * Extract plaintext strings from a web page.
 * Illustrative program to gather the textual contents of a web page.
 * Uses a {@link org.htmlparser.beans.StringBean StringBean} to accumulate
 * the user visible text (what a browser would display) into a single string.
 */
public class StringExtractor
{
    private String url;
    private Queue<Page> queue;
    private ArrayList<Page> list;
    /**
     * Construct a StringExtractor to read from the given resource.
     * @param resource Either a URL or a file name.
     */
    public StringExtractor (String url)
    {
        this.url = url;
        queue = new LinkedList<Page>();
        this.list = new ArrayList<Page>();
    }

    /**
     * Extract the text from a page.
     * @return The textual contents of the page.
     * @param links if <code>true</code> include hyperlinks in output.
     * @exception ParserException If a parse error occurs.
     * @throws IOException 
     */
    public Page extractStrings (int limit)
        throws
            ParserException, IOException
    {
    	int count = 0;
        queue.add(new Page(url));
        while (limit > 0 && !queue.isEmpty()) {
        	Page CurrentPage = queue.remove();
        	StringBean sb;
            sb = new StringBean ();
            sb.setLinks (false);
            sb.setURL (CurrentPage.url);
           	CurrentPage.setPageID(count);
            
            try {
				CurrentPage.page = new String(sb.getStrings().getBytes("ISO-8859-1"),"UTF-8");
				Parser parser = new Parser();
			    try {   
			    	// HtmlPage extends visitor,Apply the given visitor to the current   
			    	// page.
			    	try {
			    		parser.setURL(CurrentPage.url);
			    	} catch (Exception e) {
			    		continue;
			    	}
			    	// get date
			    	URL url = new URL(CurrentPage.url);
			        HttpURLConnection httpCon = (HttpURLConnection) url.openConnection();
			    	parser.setEncoding("UTF-8");
			    	long date = httpCon.getLastModified();
			    	CurrentPage.date = new Date(date);
			    	// get page size
					InputStream is = httpCon.getInputStream();
					InputStreamReader isr = new InputStreamReader(is);
					int numCharsRead;
					char[] charArray = new char[1024];
					StringBuffer sbuf = new StringBuffer();
					while ((numCharsRead = isr.read(charArray)) > 0) {
						sbuf.append(charArray, 0, numCharsRead);
					}
					String result = sbuf.toString();
					CurrentPage.size = result.length();
			    	// get title
			    	NodeFilter filter = new TagNameFilter("TITLE");
			    	NodeList nodelist = parser.extractAllNodesThatMatch(filter);
			    	if (nodelist.size() > 0) {
			    		CurrentPage.title = nodelist.elementAt(0).toPlainTextString();
			    	}
			    } catch (ParserException e) {
			        e.printStackTrace();
			    } 
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            LinkBean lb = new LinkBean();
            lb.setURL(CurrentPage.url);
            URL[] URL_array = lb.getLinks();
            for(int i=0; i<URL_array.length; i++){
            	Page ChildPage = new Page(URL_array[i].toString()); 
            	ChildPage.parent = CurrentPage;
            	queue.add(ChildPage);
            }
            boolean exist = false;
            for (int i = 0; i < list.size(); i++) {
            	if (list.get(i).url.equals(CurrentPage.url)) {
            		exist = true;
            	}
            }
            if (!exist) {
            	limit--;
            	count++;
            	list.add(CurrentPage);
            	if (CurrentPage.parent != null) {
            		CurrentPage.parent.children.add(CurrentPage);
            	}
            }
        }
        if (list.isEmpty()) {
        	return null;
        } else {
        	return list.get(0);
        }
    }
    
    public void print() {
    	for (int i = 0; i < list.size(); i++) {
    			System.out.println(list.get(i).children.size());
//    			System.out.println(list.get(i).url);
//    			System.out.print(list.get(i).date);
//    			System.out.println(list.get(i).size);
    	}
    }
}
