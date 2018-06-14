package chapter28;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class TestGraph extends Application {
    @Override // Override the start method in the Application class
    public void start(Stage primaryStage) {
        System.out.println("Please enter the name of the file, it must include the coordinates.");
        Scanner reader = new Scanner(System.in);
        String fileName = reader.nextLine();
        System.out.println("Please enter a pair of vertices; format: [(int) (int)]; the first number will be used to calculate the clustering ");
        String pair = reader.nextLine();
        String[] pInt = pair.split(" ");
        int a = Integer.parseInt(pInt[0]);
        int b = Integer.parseInt(pInt[1]);

        AbstractGraph<City> graph = null;
        try {
            graph = loadGraph(fileName);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        Scene scene = new Scene(new GraphView(graph), 750, 450);
        primaryStage.setTitle("Graph"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage
        System.out.println("Connnected: " + isConnected(graph));
        System.out.println("Cycle: " + isCycle(graph));
        System.out.println("Bipartite: " + isBipartite(graph, 0));
        System.out.format("A shortest path from vertex %d to %d: ", a, b);
        shortPath(graph, a, b);
        System.out.println("Degree of Centrality: " + degreeOfCentrality(graph, a));
        System.out.println("Num Triangles: " + findNumTriangles(graph, a));
        System.out.println("Clustering index: " + clusteringNum(graph, a));
    }

    static class City implements Displayable {
        private int x, y;
        private String name;

        City(String name, int x, int y) {
            this.name = name;
            this.x = x;
            this.y = y;
        }

        @Override
        public int getX() {
            return x;
        }

        @Override
        public int getY() {
            return y;
        }

        @Override
        public String getName() {
            return name;
        }
        public String toString() {
            return String.format("City{%s (%d, %d)}", name, x, y);
        }

    }

    /**
     * The main method is only needed for the IDE with limited
     * JavaFX support. Not needed for running from the command line.
     */
    public static AbstractGraph<City> loadGraph(String fileName) throws FileNotFoundException {
        Scanner reader = new Scanner(new File(fileName));

        int numCities = Integer.parseInt(reader.nextLine());

        List<City> cities = new ArrayList<>();
        List<AbstractGraph.Edge> edges = new ArrayList<>();

        for (int i = 0; i < numCities; i++) {
            String currentLine = reader.nextLine();
            String[] nums = currentLine.split(" ");
            int currentV = Integer.parseInt(nums[0]);
            for (int j = 3; j < nums.length; j++)
            {
                edges.add(new AbstractGraph.Edge(currentV, Integer.parseInt(nums[j])));
            }
            cities.add(new City(nums[0], Integer.parseInt(nums[1]), Integer.parseInt(nums[2])));
        }
        return new UnweightedGraph<>(cities, edges);
    }
    public static boolean isConnected(AbstractGraph g)
    {
        AbstractGraph.Tree t = g.dfs(0);
        return t.getNumberOfVerticesFound() == g.getVertices().size();
    }
    public static boolean isConnected(String fileName) throws FileNotFoundException {
        AbstractGraph<City> graph = loadGraph(fileName);
        return isConnected(graph);
    }
    public static boolean isCycle(String fileName) throws FileNotFoundException {
        AbstractGraph<City> graph = loadGraph(fileName);
        return isCycle(graph);
    }
    //In order for isCycle to work, hashcode and equals should be implemented in edge.
    public static boolean isCycle(AbstractGraph g)
    {
        AbstractGraph.Tree t = g.dfs(0);
        HashSet<AbstractGraph.Edge> treeEdge = new HashSet<>();
        //Create a hash set of edges in the tree.
        for (Object x : t.getSearchOrder())
        {
            int currentV = (Integer) x;
            int parent = t.getParent(currentV);
            if (parent != -1){
                treeEdge.add(new AbstractGraph.Edge(parent, currentV));
            }
        }
        //Determine if there is extra edges not in the tree

        for (Object l : g.neighbors){
            List<AbstractGraph.Edge> verts = (List) l;
            for (AbstractGraph.Edge e : verts)
            {
                if (!(treeEdge.contains(e) || treeEdge.contains(new AbstractGraph.Edge(e.v, e.u)))){
                    return true;
                }
            }
        }
        return false;
    }

    public static void shortPath(AbstractGraph g, int a, int b){
        AbstractGraph.Tree tree = g.bfs(a);
        tree.printPath(b);
    }

    public static int findNumTriangles(AbstractGraph g, int a)
    {
        HashSet<Integer> neighbors = new HashSet<>();

        neighbors.addAll(g.getNeighbors(a));
        System.out.println(neighbors);
        int triangles = 0;
        for (int i : neighbors)
        {
            HashSet<Integer> nOfN = new HashSet<>(g.getNeighbors(i));
            System.out.println(nOfN);
            nOfN.retainAll(neighbors);
            triangles += nOfN.size();
        }
        return triangles / 2;
    }
    public static double degreeOfCentrality(AbstractGraph g, int a)
    {
        return (double) g.getNeighbors(a).size() / (g.getVertices().size() - 1);
    }

    public static double clusteringNum(AbstractGraph g, int a)
    {
        int v = g.getVertices().size();
        return (double) 4 * findNumTriangles(g, a) / (v * (v - 1));
    }
    public boolean isBipartite(AbstractGraph g, int v) {
        List<Integer> searchOrder = new ArrayList<>();
        int[] parent = new int[g.vertices.size()];
        for (int i = 0; i < parent.length; i++)
            parent[i] = -1; // Initialize parent[i] to -1

        java.util.LinkedList<Integer> queue = new java.util.LinkedList<>(); // list used as a queue
        boolean[] isVisited = new boolean[g.vertices.size()];
        boolean[] color = new boolean[g.vertices.size()];
        queue.offer(v); // Enqueue v
        isVisited[v] = true; // Mark it visited
        color[v] = true;

        while (!queue.isEmpty()) {
            int u = queue.poll(); // Dequeue to u
            searchOrder.add(u); // u searched
            for (Object o: (List) g.neighbors.get(u)) {
                AbstractGraph.Edge e = (AbstractGraph.Edge) o;
                if (!isVisited[e.v]) {
                    queue.offer(e.v); // Enqueue w
                    parent[e.v] = u; // The parent of w is u
                    isVisited[e.v] = true; // Mark it visited
                    color[e.v] = !color[u];
                } else {
                    if (color[u] == color[e.v]){
                        return false;
                    }
                }
            }
        }
        return true;
    }
    public static void main(String[] args) {
        launch(args);
    }
}
