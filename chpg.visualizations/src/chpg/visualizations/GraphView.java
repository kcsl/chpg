package chpg.visualizations;

import java.lang.String;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import chpg.graph.Edge;
import chpg.graph.Graph;
import chpg.graph.GraphElementSet;
import chpg.graph.Node;
import chpg.graph.schema.SchemaGraph;
import io.github.spencerpark.ijava.runtime.Display;

public class GraphView {

	private static String fontAwesomeCSS = "https://cdn.jsdelivr.net/npm/font-awesome@4.7.0/css/font-awesome.css";

	private static String cytoscapeJS = "https://cdn.jsdelivr.net/npm/cytoscape@3.11.0/dist/cytoscape.min.js";

	private static String jqueryJS = "https://cdn.jsdelivr.net/npm/jquery@3.4.1/dist/jquery.min.js";

	private static String dagreJS = "https://cdn.jsdelivr.net/npm/dagre@0.8.4/dist/dagre.min.js";
	private static String cytoscapeDagreJS = "https://cdn.jsdelivr.net/npm/cytoscape-dagre@2.2.2/cytoscape-dagre.min.js";

	private static String klayJS = "https://cdn.jsdelivr.net/npm/klayjs@0.4.1/klay.min.js";
	private static String cytoscapeKlayJS = "https://cdn.jsdelivr.net/npm/cytoscape-klay@3.1.3/cytoscape-klay.min.js";

	private static String contextMenuJS = "https://cdn.jsdelivr.net/npm/cytoscape-context-menus@3.0.7/cytoscape-context-menus.min.js";
	private static String contextMenuCSS = "https://cdn.jsdelivr.net/npm/cytoscape-context-menus@3.0.7/cytoscape-context-menus.css";

	private static String wheelMenuJS = "https://cdn.jsdelivr.net/npm/cytoscape-cxtmenu@3.1.1/cytoscape-cxtmenu.min.js";

	private static String panzoomJS = "https://cdn.jsdelivr.net/npm/cytoscape-panzoom@2.5.3/cytoscape-panzoom.js";
	private static String panzoomCSS = "https://cdn.jsdelivr.net/npm/cytoscape-panzoom@2.5.3/cytoscape.js-panzoom.css";

	private static String navigatorJS = "https://cdn.jsdelivr.net/npm/cytoscape-navigator@1.3.3/cytoscape-navigator.min.js";
	private static String navigatorCSS = "https://cdn.jsdelivr.net/npm/cytoscape-navigator@1.3.3/cytoscape.js-navigator.css";

	private static boolean debug = false;

	public static void setDebug(boolean enabled) {
		debug = enabled;
	}

	public static final int DEFAULT_VERTICAL_SIZE = 600;
	public static final int DEFAULT_NAVIGATOR_NODES_SIZE = 20;
	public static final boolean DEFAULT_PANZOOM = true;

	public enum Layout {
		DAGRE, KLAY
	}

	public enum Menu {
		NONE, TEXT, WHEEL
	}

	public enum PanZoom {
		ENABLED, DISABLED
	}

	public enum Navigator {
		DEFAULT, ENABLED, DISABLED
	}

	public static Path createHTML(Graph graph) throws IOException {
		Path tempDirectory = Files.createTempDirectory("graph-viewer");
		return createHTML(graph, tempDirectory, "", DEFAULT_VERTICAL_SIZE, true, Layout.DAGRE, Menu.NONE,
				PanZoom.ENABLED, Navigator.DEFAULT);
	}

	public static Path createHTML(Graph graph, Path directoryPath) throws IOException {
		return createHTML(graph, directoryPath, "", DEFAULT_VERTICAL_SIZE, true, Layout.DAGRE, Menu.NONE,
				PanZoom.ENABLED, Navigator.DEFAULT);
	}

	public static Path createHTML(Graph graph, Path directoryPath, String name) throws IOException {
		return createHTML(graph, directoryPath, name, DEFAULT_VERTICAL_SIZE, true, Layout.DAGRE, Menu.NONE,
				PanZoom.ENABLED, Navigator.DEFAULT);
	}

	public static Path createHTML(Graph graph, Path directoryPath, String name, boolean extend) throws IOException {
		return createHTML(graph, directoryPath, name, DEFAULT_VERTICAL_SIZE, extend, Layout.DAGRE, Menu.NONE,
				PanZoom.ENABLED, Navigator.DEFAULT);
	}

	public static Path createHTML(Graph graph, Path directoryPath, String name, boolean extend, int verticalSize)
			throws IOException {
		return createHTML(graph, directoryPath, name, verticalSize, extend, Layout.DAGRE, Menu.NONE, PanZoom.ENABLED,
				Navigator.DEFAULT);
	}

	public static Path createHTML(Graph graph, Path directoryPath, String name, int verticalSize, boolean extend,
			Layout layout, Menu menu, PanZoom panzoom, Navigator navigator) throws IOException {
		// Open the directory as a file
		File graphViewerDirectory = directoryPath.toFile();

		// If in debug mode, print path to directory of HTML graph
		if (debug)
			System.out.println("DEBUG: " + graphViewerDirectory.getAbsolutePath());

		// Create File object for the copy of this resource file in graphViewerDirectory
		String htmlRes = "templates/index.html";
		File resourceFile = new File(graphViewerDirectory.getAbsolutePath() + File.separator
				+ htmlRes.replaceFirst("templates/", "").replace("/", File.separator));

		// Make the parent directory for the HTML graph if it doesn't exist
		if (!resourceFile.getParentFile().exists())
			resourceFile.getParentFile().mkdirs();

		// Open the HTML template file and read its contents
		FileWriter fw = new FileWriter(resourceFile);

		// Open the HTML template file and read its contents
		String htmlContents = readResource("templates/index.html" + ".template");

		// Format the HTML file
		htmlContents = formatHTML(htmlContents, graph, name, verticalSize, extend, layout, menu, panzoom, navigator);

		// Write the formatted HTML to HTML document being created
		fw.write(htmlContents);
		fw.close();

		// Return the path of the HTML
		return resourceFile.toPath();
	}

	public static String formatHTML(String htmlContents, Graph graph, String name, int verticalSize, boolean extend,
			Layout layout, Menu menu, PanZoom panzoom, Navigator navigator) {
		// If graph is empty, simply display text indicating that
		if (graph.isEmpty()) {
			return "<html><style></style><body><h1>Empty Graph</h1></body></html>";
		}

		// Set name to empty if it is null
		if (name == null) {
			name = "";
		}

		// Create contains graph
		Graph containsGraph = graph.toGraph(graph.edges(SchemaGraph.Contains));
		containsGraph = graph.toGraph(graph.nodes()).induce(containsGraph);

		// Set the graph name
		htmlContents = htmlContents.replace("TEMPLATE_GRAPH_NAME", name);

		// Create the nodes and edges JSON arrays, add them to a JSON array of elements
		JsonArray elements = new JsonArray();
		elements.addAll(createNodeJson(graph.nodes(), containsGraph, extend));
		elements.addAll(createEdgesJson(graph.edges(), containsGraph));
		htmlContents = htmlContents.replace("TEMPLATE_ELEMENTS", elements.toString());

		// Add layout options JSON
		JsonObject layoutOptions = createLayoutOptions(layout);
		htmlContents = htmlContents.replace("TEMPLATE_LAYOUT_OPTIONS", layoutOptions.toString());

		// Add all necessary sources for the given options
		Set<String> sourcesJS = new LinkedHashSet<String>();
		sourcesJS.add(cytoscapeJS);
		sourcesJS.add(jqueryJS);
		Set<String> sourcesCSS = new LinkedHashSet<String>();

		// Add sources for layout
		if (layout.equals(Layout.DAGRE)) {
			sourcesJS.add(dagreJS);
			sourcesJS.add(cytoscapeDagreJS);
		} else if (layout.equals(Layout.KLAY)) {
			sourcesJS.add(klayJS);
			sourcesJS.add(cytoscapeKlayJS);
		}

		// Add sources for text menu
		if (menu.equals(Menu.TEXT)) {
			sourcesCSS.add(contextMenuCSS);
			sourcesJS.add(contextMenuJS);

			// Create the context menu options
			JsonObject contextMenuOptions = createMenuJson();
			htmlContents = htmlContents.replace("TEMPLATE_OPTIONS_CONTEXT_TEXT",
					"cy.contextMenus(\n" + contextMenuOptions.toString() + "\n);");
		}

		// Add sources for wheel menu
		if (menu.equals(Menu.WHEEL)) {
			sourcesCSS.add(fontAwesomeCSS);
			sourcesJS.add(wheelMenuJS);

			// TODO set wheel menu options
			htmlContents = htmlContents.replace("TEMPLATE_OPTIONS_CONTEXT_WHEEL", "// TODO: WHEEL MENU OPTIONS");
		}

		// Add sources for panzoom menu
		if (panzoom.equals(PanZoom.ENABLED)) {
			sourcesCSS.add(fontAwesomeCSS);
			sourcesCSS.add(panzoomCSS);
			sourcesJS.add(panzoomJS);

			// TODO add panzoom options
			htmlContents = htmlContents.replace("TEMPLATE_OPTIONS_PANZOOM", "cy.panzoom({});");
		}

		// Add sources for navigator menu
		if (navigator.equals(Navigator.ENABLED)
				|| (navigator.equals(Navigator.DEFAULT) && graph.nodes().size() >= DEFAULT_NAVIGATOR_NODES_SIZE)) {
			sourcesCSS.add(fontAwesomeCSS);
			sourcesCSS.add(navigatorCSS);
			sourcesJS.add(navigatorJS);

			// TODO add navigator options
			htmlContents = htmlContents.replace("TEMPLATE_OPTIONS_NAVIGATOR", "cy.navigator({});");
		}

		// Removed unused options
		htmlContents = htmlContents.replace("TEMPLATE_OPTIONS_PANZOOM", "");
		htmlContents = htmlContents.replace("TEMPLATE_OPTIONS_NAVIGATOR", "");
		htmlContents = htmlContents.replace("TEMPLATE_OPTIONS_CONTEXT_TEXT", "");
		htmlContents = htmlContents.replace("TEMPLATE_OPTIONS_CONTEXT_WHEEL", "");

		// Concat the JS sources and add them to the HTML
		StringBuilder sourcesJSsb = new StringBuilder();
		for (String source : sourcesJS) {
			sourcesJSsb.append("<script src=\"" + source + "\"></script>\n");
		}
		htmlContents = htmlContents.replace("TEMPLATE_JS_SOURCES", sourcesJSsb);

		// Concat the CSS sources and add them to the HTML
		StringBuilder sourcesCSSsb = new StringBuilder();
		for (String source : sourcesCSS) {
			sourcesCSSsb.append("<link rel=\"stylesheet\" href=\"" + source + "\">\n");
		}
		htmlContents = htmlContents.replace("TEMPLATE_CSS_SOURCES", sourcesCSSsb);

		// Replace functions and undefined values with proper text for JavaScript
		htmlContents = toJavaScriptObjectString(htmlContents);

		return htmlContents;
	}

	public static JsonArray createNodeJson(GraphElementSet<Node> nodes, Graph containsGraph, boolean extend) {
		// Create a list of JSON representations of the graph nodes
		JsonArray nodesArray = new JsonArray();

		//start project node
		Node tempNode = nodes.iterator().next();
		String tempNodePath = nodeGetPath(tempNode);
		String projectName = nodeGetProjectName(tempNodePath);
		
		JsonObject projectNodeObject = new JsonObject();
		
		JsonObject projectDataObject = new JsonObject();
		projectDataObject.addProperty("id", "projectNode");
		projectDataObject.addProperty("name", projectName);
		projectDataObject.addProperty("shape", "round-rectangle");
		projectDataObject.addProperty("width", "");
		projectDataObject.addProperty("parent", "projectNode");
		
		JsonArray classesJson = new JsonArray();
		classesJson.add("container");
		projectNodeObject.add("classes", classesJson);
				
		projectNodeObject.add("data", projectDataObject);

		nodesArray.add(projectNodeObject);
		//end project node

		//start function (file) node
		String tempFileName = nodeGetFileName(tempNodePath);
		
		JsonObject functionNodeObject = new JsonObject();
		
		JsonObject functionDataObject = new JsonObject();
		functionDataObject.addProperty("id", tempFileName);
		functionDataObject.addProperty("name", tempFileName);
		functionDataObject.addProperty("shape", "round-rectangle");
		functionDataObject.addProperty("width", "");
		functionDataObject.addProperty("parent", "projectNode");
		classesJson = new JsonArray();
		classesJson.add("container");
		functionNodeObject.add("classes", classesJson);
				
		functionNodeObject.add("data", functionDataObject);

		nodesArray.add(functionNodeObject);
		//end function (file) node
		
		String lastFunctionName = tempFileName;
		
		for (Node node : nodes) {
			// Get the escaped name of the nodes if it has a name
			String nodeName = node.hasName() ? escapeSchemaChars(node.getName()) : "";
			
			String nodePath = nodeGetPath(node);
			String fileName = nodeGetFileName(nodePath);
			
			// Get the parent of the node if it exists
			// NOTE: this is looking for the predecessor, not the container node
			//Node parentNode = containsGraph.predecessors(node).one();
			
			//Node parentNode = fileName;
			
			// Create the JSON for the node
			JsonObject nodeObject = new JsonObject();

			// Add the basic data attribute
			JsonObject dataObject = new JsonObject();
			dataObject.addProperty("id", "n" + node.getAddress());
			dataObject.addProperty("name", nodeName);

			// Set styles dependent on node type
			if (node.tags().contains("XCSG.ControlFlowLoopCondition")
					|| node.tags().contains("XCSG.ControlFlowIfCondition")) {
				// Set diamond shape for loop conditions and if statements
				dataObject.addProperty("shape", "diamond");
				// Fix width issue for diamond nodes
				dataObject.addProperty("width", nodeName.length() * 10);
			} else {
				// Set default node style
				dataObject.addProperty("shape", "round-rectangle");
				dataObject.addProperty("width", "label");
			}

			// If extend is set to true and this node has, add parent attribute to data and
			// add classes attribute
//			if (extend && parentNode == null) {
//				dataObject.addProperty("parent", "n" + node.getAddress());
//
//				//JsonArray classesJson = new JsonArray();
//				classesJson = new JsonArray();
//				classesJson.add("container");
//				nodeObject.add("classes", classesJson);
//			}			
			dataObject.addProperty("parent", lastFunctionName);

			//JsonArray classesJson = new JsonArray();
			classesJson = new JsonArray();
			
			classesJson.add("container");
			nodeObject.add("classes", classesJson);
			nodeObject.add("data", dataObject);

			// Add the node to the array of nodes
			nodesArray.add(nodeObject);
		}

		return nodesArray;
	}

	public static JsonArray createEdgesJson(GraphElementSet<Edge> edges, Graph containsGraph) {
		// Create a list of JSON representations of the graph edges
		JsonArray edgesArray = new JsonArray();

		for (Edge edge : edges) {
			// If the edge is in containsGraph, add it to the list of edges
			if (!containsGraph.edges().contains(edge)) {
				// Get the escaped name of the edge if it has a name
				String edgeName = edge.hasName() ? escapeSchemaChars(edge.getName()) : "";

				// Create the JSON for the edge
				JsonObject edgeObject = new JsonObject();

				// Create the data attribute for the edge and add it to edgeJson
				JsonObject dataJson = new JsonObject();
				dataJson.addProperty("id", edge.getAddress());
				dataJson.addProperty("name", edgeName);
				dataJson.addProperty("source", "n" + edge.from().getAddress());
				dataJson.addProperty("target", "n" + edge.to().getAddress());
				edgeObject.add("data", dataJson);

				// Add the edge to the array of edges
				edgesArray.add(edgeObject);
			}
		}

		return edgesArray;
	}

	public static JsonObject createMenuJson() {
		// Create an array of all menu items
		JsonArray menuItemsArray = new JsonArray();
		
		// Create hide and change color buttons
		JsonObject hideButton = new JsonObject();
		hideButton.addProperty("id", "hide");
		hideButton.addProperty("content", "hide");
		hideButton.addProperty("tooltipText", "hide");
		hideButton.addProperty("selector", "*");
		hideButton.addProperty("onClickFunction",
				"function(event) {var target = event.target || event.cyTarget; target.hide();}");
		JsonObject colorBlueButton = changeColorJson("blue");
		JsonObject colorWhiteButton = changeColorJson("white");
		menuItemsArray.add(hideButton);
		menuItemsArray.add(colorBlueButton);
		menuItemsArray.add(colorWhiteButton);
		
		// Return the menu within a JSON object
		JsonObject contextMenu = new JsonObject();
		contextMenu.add("menuItems", menuItemsArray);
		return contextMenu;
	}

	public static JsonObject changeColorJson(String color) {
		// Create a button for changing the node's color to color
		JsonObject colorButton = new JsonObject();
		colorButton.addProperty("id", "change" + color);
		colorButton.addProperty("content", "Change to " + color);
		colorButton.addProperty("tooltipText", "Change to " + color);
		colorButton.addProperty("selector", "*");
		colorButton.addProperty("onClickFunction",
				"function(event) { var target = event.target || event.cyTarget; target.css(\"background-color\", \""
						+ color + "\"); }");
		return colorButton;
	}

	public static JsonObject createLayoutOptions(Layout layout) {
		// Create a JSON object of graph options for the given layout type
		JsonObject graphOptions = new JsonObject();

		// Add layout specific options
		if (layout.equals(Layout.DAGRE)) {
			graphOptions.addProperty("name", "dagre");
			graphOptions.addProperty("nodeSep", "undefined");
			graphOptions.addProperty("edgeSep", "undefined");
			graphOptions.addProperty("rankSep", "undefined");
			graphOptions.addProperty("rankDir", "TB");
			graphOptions.addProperty("rankSep", "undefined");
			graphOptions.addProperty("minLen", "function( edge ){ return 1; }");
			graphOptions.addProperty("edgeWeight", "function( edge ){ return 1; }");

		} else if (layout.equals(Layout.KLAY)) {
			graphOptions.addProperty("name", "klay");
			graphOptions.addProperty("priority", "function( edge ){ return null; }");

			JsonObject klayOptions = new JsonObject();
			klayOptions.addProperty("addUnnecessaryBendpoints", false);
			klayOptions.addProperty("aspectRatio", 1.6);
			klayOptions.addProperty("borderSpacing", 20);
			klayOptions.addProperty("compactComponents", false);
			klayOptions.addProperty("crossingMinimization", "LAYER_SWEEP");
			klayOptions.addProperty("cycleBreaking", "GREEDY");
			klayOptions.addProperty("direction", "DOWN");
			klayOptions.addProperty("edgeRouting", "ORTHOGONAL");
			klayOptions.addProperty("edgeSpacingFactor", 0.5);
			klayOptions.addProperty("feedbackEdges", false);
			klayOptions.addProperty("fixedAlignment", "NONE");
			klayOptions.addProperty("inLayerSpacingFactor", 1.0);
			klayOptions.addProperty("layoutHierarchy", true);
			klayOptions.addProperty("linearSegmentsDeflectionDampening", 0.3);
			klayOptions.addProperty("mergeEdges", false);
			klayOptions.addProperty("mergeHierarchyCrossingEdges", true);
			klayOptions.addProperty("nodeLayering", "NETWORK_SIMPLEX");
			klayOptions.addProperty("nodePlacement", "BRANDES_KOEPF");
			klayOptions.addProperty("randomizationSeed", 1);
			klayOptions.addProperty("routeSelfLoopInside", false);
			klayOptions.addProperty("separateConnectedComponents", true);
			klayOptions.addProperty("spacing", 20);
			klayOptions.addProperty("thoroughness", 7);
			graphOptions.add("klay", klayOptions);
		}

		// Add general layout options
		graphOptions.addProperty("fit", true);
		graphOptions.addProperty("padding", 30);
		graphOptions.addProperty("spacingFactor", "undefined");
		graphOptions.addProperty("nodeDimensionsIncludeLabels", false);
		graphOptions.addProperty("animate", false);
		graphOptions.addProperty("animateFilter", "function( node, i ){ return true; }");
		graphOptions.addProperty("animationDuration", 500);
		graphOptions.addProperty("animationEasing", "undefined");
		graphOptions.addProperty("boundingBox", "undefined");
		graphOptions.addProperty("transform", "function( node, pos ){ return pos; }");
		graphOptions.addProperty("ready", "function(){}");
		graphOptions.addProperty("stop", "function(){}");

		return graphOptions;
	}

	public static void show(Path htmlPath) throws IOException, InterruptedException {
		// TODO set appropriate default height
		show(htmlPath, 500);
	}

	public static void show(Path htmlPath, int verticalSize) throws IOException, InterruptedException {
		// Get the htmlContents
		String htmlContents = htmlPath.toString();

		// Create and start a thread for that listens on port and serves htmlContents
		int port = 8090;
		Object sync = new Object();
		HTMLSocketRunner socketRunner = new HTMLSocketRunner(sync, port, htmlContents);
		Thread thread = new Thread(socketRunner);
		thread.start();

		// Wait for the SocketServer to start, then display an IFrame connecting to it
		synchronized (sync) {
			sync.wait();
			Display.display("<html><iframe src='http://localhost:" + port + "/' width=\"100%\", height=\""
					+ verticalSize + "px\" frameBorder=\"0\"></iframe></html>", "text/html");
		}
	}
	//Gets the path of a node
	private static String nodeGetPath(Node node) {
		
		String sourceAttribute = "XCSG.ModelElement.sourceCorrespondence";
		
		if(node.attributes().containsKey(sourceAttribute)) {
			String nodePath = (String) node.attributes().get(sourceAttribute);
			return nodePath;
		}
		else {
			return null;
		}
	}
	//Parses a node's path to find the name of the file it is in
	private static String nodeGetFileName(String nodePath) {
		
		int end = nodePath.indexOf(",");
		nodePath = nodePath.substring(0,end);
		String filename = new File(nodePath).getName();
			
		return filename;
		
	}
	//Parses a node's path to find the name of the project it is in
	//This assumes the file name is the name of the function the node is in. Need a better solution.
	private static String fileGetProjectName(String nodePath) {

		int start = nodePath.indexOf("/");
		String temp = nodePath.substring(start+1,nodePath.length());
		int end = temp.indexOf("/");
		String projectName = temp.substring(0,end);
		
		return projectName;
		
	}
	
	//Parses a node's path to find the name of the project it is in
	//This assumes that the every node in the graph is in the same project
	private static String nodeGetProjectName(String nodePath) {

		int start = nodePath.indexOf("/");
		String temp = nodePath.substring(start+1,nodePath.length());
		int end = temp.indexOf("/");
		String projectName = temp.substring(0,end);
		
		return projectName;
		
	}
	

	private static String readResource(String path) throws IOException {
		// Read and return the contents of the resource at the given path
		InputStream inputStream = GraphView.class.getResourceAsStream("/" + path);
		if (inputStream == null) {
			throw new IOException("Unable to access resource at path: /" + path);
		}
		return convertStreamToString(inputStream);
	}

	private static String convertStreamToString(InputStream is) throws IOException {
		// Read all of the contents of the InputStream and return it as a string
		StringBuilder sb = new StringBuilder(2048);
		char[] read = new char[128];
		try (InputStreamReader ir = new InputStreamReader(is, StandardCharsets.UTF_8)) {
			for (int i; -1 != (i = ir.read(read)); sb.append(read, 0, i))
				;
		}
		return sb.toString();
	}

	public static String escapeSchemaChars(String s) {
		// Escape all schema characters
		s = s.replace("\\", "\\\\");
		s = s.replace("\"", "\\\"");
		s = s.replace("\b", "\\b");
		s = s.replace("\f", "\\f");
		s = s.replace("\n", "\\n");
		s = s.replace("\r", "\\r");
		s = s.replace("\t", "\\t");
		return s;
	}

	public static String toJavaScriptObjectString(String jsonString) {
		// Remove surrounding quotes from all JavaScript functions
		String patternString = "\"function\\s*([A-z0-9]+)?\\s*\\((?:[^)(]+|\\((?:[^)(]+|\\([^)(]*\\))*\\))*\\)\\s*\\{(?:[^}{]+|\\{(?:[^}{]+|\\{[^}{]*\\})*\\})*\\}\"";
		Pattern pattern = Pattern.compile(patternString);
		Matcher matcher = pattern.matcher(jsonString);
		StringBuffer sb = new StringBuffer();

		while (matcher.find()) {
			String toReplace = matcher.group();
			String replacement = toReplace.substring(1, toReplace.length() - 1);
			matcher.appendReplacement(sb, replacement);
		}
		matcher.appendTail(sb);

		// Remove surrounding quotes from all undefined values
		String result = sb.toString().replace("\"undefined\"", "undefined");

		return result;
	}
}
