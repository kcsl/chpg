package chpg.visualizations;

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

	public static final int DEFAULT_VERTICAL_SIZE = 1000;
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
		return createHTML(graph, tempDirectory, "", DEFAULT_VERTICAL_SIZE, true, Layout.DAGRE, Menu.TEXT,
				PanZoom.ENABLED, Navigator.DEFAULT);
	}

	public static Path createHTML(Graph graph, Path directoryPath) throws IOException {
		return createHTML(graph, directoryPath, "", DEFAULT_VERTICAL_SIZE, true, Layout.DAGRE, Menu.TEXT,
				PanZoom.ENABLED, Navigator.DEFAULT);
	}

	public static Path createHTML(Graph graph, Path directoryPath, String name) throws IOException {
		return createHTML(graph, directoryPath, name, DEFAULT_VERTICAL_SIZE, true, Layout.DAGRE, Menu.TEXT,
				PanZoom.ENABLED, Navigator.DEFAULT);
	}

	public static Path createHTML(Graph graph, Path directoryPath, String name, boolean extend) throws IOException {
		return createHTML(graph, directoryPath, name, DEFAULT_VERTICAL_SIZE, extend, Layout.DAGRE, Menu.TEXT,
				PanZoom.ENABLED, Navigator.DEFAULT);
	}

	public static Path createHTML(Graph graph, Path directoryPath, String name, boolean extend, int verticalSize)
			throws IOException {
		return createHTML(graph, directoryPath, name, verticalSize, extend, Layout.DAGRE, Menu.TEXT, PanZoom.ENABLED,
				Navigator.DEFAULT);
	}

	public static Path createHTML(Graph graph, Path directoryPath, String name, int verticalSize, boolean extend,
			Layout layout, Menu menu, PanZoom panzoom, Navigator navigator) throws IOException {
		// Open the directory as a file
		File graphViewerDirectory = directoryPath.toFile();

		// If in debug mode, print path to directory of HTML graph
		if (debug)
			System.out.println("DEBUG: " + graphViewerDirectory.getAbsolutePath());

		// TODO clean this up
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

		// TODO Unsure what this does
		Graph containsGraph = graph.toGraph(graph.edges(SchemaGraph.Contains));
		containsGraph = graph.toGraph(graph.nodes()).induce(containsGraph);

		// Set the graph name
		htmlContents = htmlContents.replace("TEMPLATE_GRAPH_NAME", name);

		// Create the nodes and edges JSON arrays, add them to a JSON array of elements
		JsonArray elementsJsonArray = new JsonArray();
		elementsJsonArray.addAll(createNodeJson(graph.nodes(), containsGraph, extend));
		elementsJsonArray.addAll(createEdgesJson(graph.edges(), containsGraph));
		htmlContents = htmlContents.replace("TEMPLATE_ELEMENTS", elementsJsonArray.toString());

		// Add layout options JSON
		JsonObject layoutOptionsJsonObject = createLayoutOptions(layout);
		htmlContents = htmlContents.replace("TEMPLATE_LAYOUT_OPTIONS", layoutOptionsJsonObject.toString());

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
			// TODO set context menu options
			// Experimenting
			//Will create Options for menu, but just starting with basic elements JsonArray MenuOptions = new JsonArray();
			JsonArray itemArray = createMenuJson();
			JsonObject contextMenuJson = new JsonObject();
			contextMenuJson.add("menuItems", itemArray);
			System.out.println("cy.contextMenus({\n"+contextMenuJson.toString()+"});");
			htmlContents = htmlContents.replace("TEMPLATE_OPTIONS_CONTEXT_TEXT", "cy.contextMenus(\n"+contextMenuJson.toString()+");");
			htmlContents = htmlContents.replace("TEMPLATE_OPTIONS_CONTEXT_TEXT", "cy.contextMenus({\n" + 
					"                                    menuItems: [\n" + 
					"                                        {\n" + 
					"                                            id: 'remove',\n" + 
					"                                            content: 'remove',\n" + 
					"                                            tooltipText: 'remove',\n" + 
					"                                            image: {src : \"remove.svg\", width : 12, height : 12, x : 6, y : 4},\n" + 
					"                                            selector: 'node, edge',\n" + 
					"                                            onClickFunction: function (event) {\n" + 
					"                                              var target = event.target || event.cyTarget;\n" + 
					"                                              target.remove();\n" + 
					"                                            },\n" + 
					"                                            hasTrailingDivider: true\n" + 
					"                                          },\n" + 
					"                                          {\n" + 
					"                                            id: 'hide',\n" + 
					"                                            content: 'hide',\n" + 
					"                                            tooltipText: 'hide',\n" + 
					"                                            selector: '*',\n" + 
					"                                            onClickFunction: function (event) {\n" + 
					"                                              var target = event.target || event.cyTarget;\n" + 
					"                                              target.hide();\n" + 
					"                                            },\n" + 
					"                                            disabled: false\n" + 
					"                                          },\n" + 
					"                                          {\n" + 
					"                                            id: 'add-node',\n" + 
					"                                            content: 'add node',\n" + 
					"                                            tooltipText: 'add node',\n" + 
					"                                            image: {src : \"add.svg\", width : 12, height : 12, x : 6, y : 4},\n" + 
					"                                            coreAsWell: true,\n" + 
					"                                            onClickFunction: function (event) {\n" + 
					"                                              var data = {\n" + 
					"                                                  group: 'nodes'\n" + 
					"                                              };\n" + 
					"                                              \n" + 
					"                                              var pos = event.position || event.cyPosition;\n" + 
					"                                              \n" + 
					"                                              cy.add({\n" + 
					"                                                  data: data,\n" + 
					"                                                  position: {\n" + 
					"                                                      x: pos.x,\n" + 
					"                                                      y: pos.y\n" + 
					"                                                  }\n" + 
					"                                              });\n" + 
					"                                            }\n" + 
					"                                          },\n" + 
					"                                          {\n" + 
					"                                            id: 'remove-selected',\n" + 
					"                                            content: 'remove selected',\n" + 
					"                                            tooltipText: 'remove selected',\n" + 
					"                                            image: {src : \"remove.svg\", width : 12, height : 12, x : 6, y : 6},\n" + 
					"                                            coreAsWell: true,\n" + 
					"                                            onClickFunction: function (event) {\n" + 
					"                                              cy.$(':selected').remove();\n" + 
					"                                            }\n" + 
					"                                          },\n" + 
					"                                          {\n" + 
					"                                            id: 'select-all-nodes',\n" +
					"									         content: 'select all nodes',\n" +
					"									         selector: 'node',\n" +
					"									         show: true,\n" +
					"									         onClickFunction: function (event) {\n" +
					"									           selectAllOfTheSameType(event.target || event.cyTarget);\n" +
					"											   \n" +
					"									           contextMenu.hideMenuItem('select-all-nodes');\n" +
					"									           contextMenu.showMenuItem('unselect-all-nodes');\n" +
					"									         }\n" +
					"                                          },\n" +
					"										   {\n" +
					"										     id: 'unselect-all-nodes',\n" +
					"										     content: 'unselect all nodes',\n" +
					"										     selector: 'node',\n" +
					"										     show: false,\n" +
					"										     onClickFunction: function (event) {\n" +
					"										       unselectAllOfTheSameType(event.target || event.cyTarget);\n" +
					"											  \n" +
					"										       contextMenu.showMenuItem('select-all-nodes');\n" +
					"										       contextMenu.hideMenuItem('unselect-all-nodes');\n" +
					"										     }\n" +
					"										   },\n" +	
					"                                          {\n" + 
					"                                            id: 'select-all-edges',\n" + 
					"                                            content: 'select all edges',\n" + 
					"                                            tooltipText: 'select all edges',\n" + 
					"                                            selector: 'edge',\n" + 
					"                                            onClickFunction: function (event) {\n" + 
					"                                              selectAllOfTheSameType(event.target || event.cyTarget);\n" + 
					"                                            }\n" + 
					"                                          }\n" + 
					"                                        ]\n" + 
					"                                      });");
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

		// Concat the sources and add them to the HTML
		StringBuilder sourcesJSsb = new StringBuilder();
		for (String source : sourcesJS) {
			sourcesJSsb.append("<script src=\"" + source + "\"></script>\n");
		}
		htmlContents = htmlContents.replace("TEMPLATE_JS_SOURCES", sourcesJSsb);

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
		JsonArray nodeJsonArray = new JsonArray();

		for (Node node : nodes) {
			// Get the escaped name of the nodes if it has a name
			String nodeName = node.hasName() ? escapeSchemaChars(node.getName()) : "";

			// Get the parent of the node if it exists
			Node parentNode = containsGraph.predecessors(node).one();

			// Create the JSON for the node
			JsonObject nodeJson = new JsonObject();

			// Add the basic data attribute
			JsonObject dataJson = new JsonObject();
			dataJson.addProperty("id", "n" + node.getAddress());
			dataJson.addProperty("name", nodeName);

			// Set shape of node TODO fix padding around diamond shape
			dataJson.addProperty("shape", "round-rectangle");

			if (node.tags().contains("XCSG.ControlFlowLoopCondition")
					|| node.tags().contains("XCSG.ControlFlowIfCondition")) {
				// Set diamond shape for loop conditions and if statements
				dataJson.addProperty("shape", "diamond");
				// Fix width issue for diamond nodes
				dataJson.addProperty("width", nodeName.length() * 10);	
			} else {
				// Set default node style
				dataJson.addProperty("shape", "round-rectangle");
				dataJson.addProperty("width", "label");
			}

			// If extend is set to true and this node has, add parent attribute to data and
			// add classes attribute
			if (extend && parentNode == null) {
				dataJson.addProperty("parent", "n" + node.getAddress());

				JsonArray classesJson = new JsonArray();
				classesJson.add("container");
				nodeJson.add("classes", classesJson);
			}
			nodeJson.add("data", dataJson);

			// Add the node to the array of nodes
			nodeJsonArray.add(nodeJson);
		}

		return nodeJsonArray;
	}

	public static JsonArray createEdgesJson(GraphElementSet<Edge> edges, Graph containsGraph) {
		// Create a list of JSON representations of the graph edges
		JsonArray edgesJsonArray = new JsonArray();

		for (Edge edge : edges) {
			// TODO not sure what this does
			if (!containsGraph.edges().contains(edge)) {

				// Get the escaped name of the edge if it has a name
				String edgeName = edge.hasName() ? escapeSchemaChars(edge.getName()) : "";

				// Create the JSON for the edge
				JsonObject edgeJson = new JsonObject();

				// Create the data attribute for the edge and add it to edgeJson
				JsonObject dataJson = new JsonObject();
				dataJson.addProperty("id", edge.getAddress());
				dataJson.addProperty("name", edgeName);
				dataJson.addProperty("source", "n" + edge.from().getAddress());
				dataJson.addProperty("target", "n" + edge.to().getAddress());
				edgeJson.add("data", dataJson);

				// Add the edge to the array of edges
				edgesJsonArray.add(edgeJson);
			}
		}

		return edgesJsonArray;
	}
	
	public static JsonArray createMenuJson() {
		JsonArray menuArrayJson = new JsonArray();
		JsonObject itemHide = new JsonObject();
		itemHide.addProperty("id","hide");
		itemHide.addProperty("content", "hide");
		itemHide.addProperty("tooltipText","hide");
		itemHide.addProperty("selector", "*");
		itemHide.addProperty("onClickFunction", "function ( event ) { var target = event.target || event.cyTarget; target.hide(); }");
		JsonObject colorBlue = new JsonObject();
		colorBlue.addProperty("id","changeBlue");
		colorBlue.addProperty("content", "Change to Blue");
		colorBlue.addProperty("tooltipText","Change to Blue");
		colorBlue.addProperty("selector", "*");
		colorBlue.addProperty("onClickFunction", "function ( event ) { var target = event.target || event.cyTarget; target.css(\"background-color\", \"blue\") ; }");
		JsonObject colorWhite = new JsonObject();
		colorWhite.addProperty("id","changeWhite");
		colorWhite.addProperty("content", "Change to White");
		colorWhite.addProperty("tooltipText","Change to White");
		colorWhite.addProperty("selector", "*");
		colorWhite.addProperty("onClickFunction", "function ( event ) { var target = event.target || event.cyTarget; target.css(\"background-color\", \"white\") ; }");
		menuArrayJson.add(itemHide);
		menuArrayJson.add(colorBlue);
		menuArrayJson.add(colorWhite);
//		System.out.println(menuArrayJson);
		return menuArrayJson;
	}

	public static JsonObject createLayoutOptions(Layout layout) {
		// Create a JSON object of graph options for the given layout type
		JsonObject graphOptionsJsonObject = new JsonObject();

		// Add layout specific options
		if (layout.equals(Layout.DAGRE)) {
			graphOptionsJsonObject.addProperty("name", "dagre");
			graphOptionsJsonObject.addProperty("nodeSep", "undefined");
			graphOptionsJsonObject.addProperty("edgeSep", "undefined");
			graphOptionsJsonObject.addProperty("rankSep", "undefined");
			graphOptionsJsonObject.addProperty("rankDir", "TB");
			graphOptionsJsonObject.addProperty("rankSep", "undefined");
			graphOptionsJsonObject.addProperty("minLen", "function( edge ){ return 1; }");
			graphOptionsJsonObject.addProperty("edgeWeight", "function( edge ){ return 1; }");

		} else if (layout.equals(Layout.KLAY)) {
			graphOptionsJsonObject.addProperty("name", "klay");
			graphOptionsJsonObject.addProperty("priority", "function( edge ){ return null; }");

			JsonObject klayJsonObject = new JsonObject();
			klayJsonObject.addProperty("addUnnecessaryBendpoints", false);
			klayJsonObject.addProperty("aspectRatio", 1.6);
			klayJsonObject.addProperty("borderSpacing", 20);
			klayJsonObject.addProperty("compactComponents", false);
			klayJsonObject.addProperty("crossingMinimization", "LAYER_SWEEP");
			klayJsonObject.addProperty("cycleBreaking", "GREEDY");
			klayJsonObject.addProperty("direction", "DOWN");
			klayJsonObject.addProperty("edgeRouting", "ORTHOGONAL");
			klayJsonObject.addProperty("edgeSpacingFactor", 0.5);
			klayJsonObject.addProperty("feedbackEdges", false);
			klayJsonObject.addProperty("fixedAlignment", "NONE");
			klayJsonObject.addProperty("inLayerSpacingFactor", 1.0);
			klayJsonObject.addProperty("layoutHierarchy", true);
			klayJsonObject.addProperty("linearSegmentsDeflectionDampening", 0.3);
			klayJsonObject.addProperty("mergeEdges", false);
			klayJsonObject.addProperty("mergeHierarchyCrossingEdges", true);
			klayJsonObject.addProperty("nodeLayering", "NETWORK_SIMPLEX");
			klayJsonObject.addProperty("nodePlacement", "BRANDES_KOEPF");
			klayJsonObject.addProperty("randomizationSeed", 1);
			klayJsonObject.addProperty("routeSelfLoopInside", false);
			klayJsonObject.addProperty("separateConnectedComponents", true);
			klayJsonObject.addProperty("spacing", 20);
			klayJsonObject.addProperty("thoroughness", 7);
			graphOptionsJsonObject.add("klay", klayJsonObject);
		}

		// Add general layout options
		graphOptionsJsonObject.addProperty("fit", true);
		graphOptionsJsonObject.addProperty("padding", 30);
		// find better way to handle undefined
		graphOptionsJsonObject.addProperty("spacingFactor", "undefined");
		graphOptionsJsonObject.addProperty("nodeDimensionsIncludeLabels", false);
		graphOptionsJsonObject.addProperty("animate", false);
		graphOptionsJsonObject.addProperty("animateFilter", "function( node, i ){ return true; }");
		graphOptionsJsonObject.addProperty("animationDuration", 500);
		graphOptionsJsonObject.addProperty("animationEasing", "undefined");
		graphOptionsJsonObject.addProperty("boundingBox", "undefined");
		graphOptionsJsonObject.addProperty("transform", "function( node, pos ){ return pos; }");
		graphOptionsJsonObject.addProperty("ready", "function(){}");
		graphOptionsJsonObject.addProperty("stop", "function(){}");
		return graphOptionsJsonObject;
	}
	

	public static void show(Path htmlPath) throws IOException, InterruptedException {
		// TODO set appropriate default height
		show(htmlPath, 500);
	}

	public static void show(Path htmlPath, int verticalSize) throws IOException, InterruptedException {
		// Get the htmlContents
		String htmlContents = Files.readString(htmlPath);

		// Create and start a thread for that listens on port and serves htmlContents 
		int port = 8090;
		Object sync = new Object();
		HTMLSocket runnable = new HTMLSocket(sync, port, htmlContents);
		Thread thread = new Thread(runnable);
		thread.start();

		// Wait for the SocketServer to start and then display an IFrame connecting to it
		synchronized (sync) {
			sync.wait();
			Display.display("<html><iframe src='http://localhost:" + port + "/' width=\"100%\", height=\"" + verticalSize
					+ "px\" frameBorder=\"0\"></iframe></html>", "text/html");
		}
	}

	private static String readResource(String path) throws IOException {
		InputStream inputStream = GraphView.class.getResourceAsStream("/" + path);
		if (inputStream == null) {
			throw new IOException("Unable to access resource at path: /" + path);
		}
		return convertStreamToString(inputStream);
	}

	private static String convertStreamToString(InputStream is) throws IOException {
		StringBuilder sb = new StringBuilder(2048);
		char[] read = new char[128];
		try (InputStreamReader ir = new InputStreamReader(is, StandardCharsets.UTF_8)) {
			for (int i; -1 != (i = ir.read(read)); sb.append(read, 0, i))
				;
		}
		return sb.toString();
	}

	public static String escapeSchemaChars(String s) {
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
		StringBuilder sb = new StringBuilder();

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
