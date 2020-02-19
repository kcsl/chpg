package chpg.tests.visualzation;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
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
	public void testGraphIO() throws IOException {

		GraphView.setDebug(true);
		
		// Import serialized control flow graph
		File serializedGraph = new File("C:/Graphs/abc.chpg");
		Graph graph = GraphIO.importGraph(serializedGraph);

		// Create path to directory where HTML graph will be written
		Path dirPath = Paths.get("C:/Graphs/TestNew1");

		// Create the HTML
		String name = "My Test Graph";
		int verticalSize = 600;
		boolean extend = true;
		// Layout layout = Layout.DAGRE;
		Layout layout = Layout.KLAY;
		Menu menu = Menu.NONE;
		PanZoom panzoom = PanZoom.ENABLED;
		Navigator navigator = Navigator.DEFAULT;
		
		GraphView.createHTML(graph, dirPath, name, 600, extend, layout, menu, panzoom, navigator);
	}
}
