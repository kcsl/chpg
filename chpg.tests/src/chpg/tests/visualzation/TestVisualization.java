package chpg.tests.visualzation;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import chpg.graph.Graph;
import chpg.io.GraphIO;
import chpg.visualizations.GraphView;
import chpg.visualizations.GraphView.Layout;
import chpg.visualizations.GraphView.Menu;
import chpg.visualizations.GraphView.Navigator;
import chpg.visualizations.GraphView.PanZoom;


public class TestVisualization {

	@Test
	public void testGraphIO() throws IOException, InterruptedException {
		// Import serialized control flow graph
		File serializedGraph = new File("/home/banjo/Workspace/SeniorDesign/Graphs/graph1.chpg");
		Graph graph = GraphIO.importGraph(serializedGraph);

		// Set up the graph options
		String name = "My Test Graph";
		int verticalSize = 600;
		boolean extend = true;
		// Layout layout = Layout.DAGRE;
		Layout layout = Layout.KLAY;
		Menu menu = Menu.NONE;
		PanZoom panzoom = PanZoom.ENABLED;
		Navigator navigator = Navigator.DEFAULT;
		
		// Create the HTML visualization of the graph
		String graphHtmlContents = GraphView.createGraphHTML(graph, name, verticalSize, extend, layout, menu, panzoom, navigator);
		
		// Write the HTML to a file
		// File newHtmlFile = new File("path/new.html");
		// FileUtils.writeStringToFile(newHtmlFile, graphHtmlContents);
	}
}
