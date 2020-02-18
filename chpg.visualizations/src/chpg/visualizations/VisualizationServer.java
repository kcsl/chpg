package chpg.visualizations;

import java.io.IOException;
import fi.iki.elonen.NanoHTTPD;

public class VisualizationServer extends NanoHTTPD {
	
	private String htmlContents; 
	
	public VisualizationServer(String htmlContents, int port) throws IOException {
        super(port);
        this.htmlContents = htmlContents;
    }

	@Override
	public Response serve(IHTTPSession session) {
		return newFixedLengthResponse(htmlContents);
	}
}