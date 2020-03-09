package chpg.visualizations;

import java.io.IOException;
import fi.iki.elonen.*;

public class VisualizationServer extends NanoHTTPD {
	
	public String htmlContents; 
	
	public VisualizationServer(int port) throws IOException {
        super(port);
    }
		
	@Override
	public Response serve(IHTTPSession session) {
		return newFixedLengthResponse(htmlContents);
	}
}