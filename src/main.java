import java.io.IOException;

import org.htmlparser.util.ParserException;

public class main {
	public static void constructURLtoID(Page root) throws IOException{
		UrlToIdMap IDmap = null;
        try {
        	IDmap = UrlToIdMap.getInstance();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        if(IDmap != null){
        	IDmap.addEntry(root.url, root.index);
        }
        if(root.children.size() == 0){ return;}
        	
        for(Page p : root.children){
        	constructURLtoID(p);
        }
	}
	
	public static void test(){
		
	}

    public static void main (String[] args)
    {
        boolean links;
        String url;
        StringExtractor se;
        Indexer indexer = new Indexer();

        links = false;
        url = null;
        for (int i = 0; i < args.length; i++)
            if (args[i].equalsIgnoreCase ("-links"))
                links = true;
            else
                url = args[i];
        if (null != url)
        {
            se = new StringExtractor (url);
            Page root = null;
           
                try {
					root = se.extractStrings(30);
//					se.print();
					UrlToIdMap IDmap = null;
			        try {
			        	IDmap = UrlToIdMap.getInstance();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if(IDmap != null){
												
							indexer.indexingPageRoot(root);
							constructURLtoID(root);
							ForwardFile ff = ForwardFile.getInstance();
							ff.printFile();
						
					}					
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                try {
					ForwardFile ff = ForwardFile.getInstance();
					InvertedFile ivf = InvertedFile.getInstance();
//					ivf.print();
					UrlToIdMap imap = UrlToIdMap.getInstance();
					ff.finalize();
					ivf.finalize();
					imap.finalize();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
        else
            System.out.println ("Usage: java -classpath htmlparser.jar org.htmlparser.parserapplications.StringExtractor [-links] url");
        
              
    }
}
