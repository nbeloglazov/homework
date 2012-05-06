import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

public class TaskB {
    
    private class Cell implements Comparable<Cell> {
        public int x, y;

        private Cell(int y, int x) {
            this.x = x;
            this.y = y;
        }
        
        public List<Cell> neighbours() {
            List<Cell> result = new ArrayList<Cell>();
            for (int i = -1; i < 2; i++) {
                for (int j = -1; j < 2; j++) {
                    if ((i != 0) ^ (j != 0)) {
                        int newX = x + i;
                        int newY = y + j;
                        if (newX >= 0 && newX < m && newY >= 0 && newY < n) {
                            result.add(new Cell(newY, newX));
                        }
                    }
                }
            }
            return result;
        }
        
        public int floor() {
            return TaskB.this.floor[y][x];
        }
        
        public int ceiling() {
            return TaskB.this.ceiling[y][x];
        }
        
        public boolean visited() {
            return TaskB.this.visited[y][x];
        }

        public void visit() {
            TaskB.this.visited[y][x] = true;
        }

        public boolean canVisitWithoutWater(Cell cell) {
            return (cell.ceiling() - cell.floor() >= 50) && (cell.ceiling() - floor() >= 50) &&
                    (ceiling() - cell.floor() >= 50);
        }

        public boolean canVisit(Cell cell, double time) {
            return canVisitWithoutWater(cell) &&  cell.ceiling() - level(time) >= 50;
        }

        public double whenCanVisit() {
            return (50 - ceiling() + h) / 10.0;
        }

        public double speed(double time) {
            return (level(time) - floor() >= 20) ? 1 : 10;
        }

        public double getTime() {
            return time[y][x];
        }

        public double setTime(double time) {
            return TaskB.this.time[y][x] = time;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Cell cell = (Cell) o;

            if (x != cell.x) return false;
            if (y != cell.y) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = x;
            result = 31 * result + y;
            return result;
        }

        public int compareTo(Cell o) {
            return Double.compare(getTime(), o.getTime());
        }
    }
    
    private int floor[][], ceiling[][];
    private double time[][];
    private boolean visited[][];
    private int n,m;
    private int h;
    private Scanner scanner;
    private PrintWriter printWriter;
    private PriorityQueue<Cell> queue;

    private double level(double time) {
        return Math.max(h - 10 * time, 0);
    }
    
    private void relax(Cell from, Cell to) {
        if (to.visited()) {
            return;
        }
        if (from.canVisitWithoutWater(to)) {
            double start = Math.max(from.getTime(), to.whenCanVisit());
            double takes = start + from.speed(start);
            if (to.getTime() == -1 || to.getTime() > takes) {
                to.setTime(takes);
                queue.add(to);
            }
        }
    }
    
    private void dijkstra() {
        while (!visited[n-1][m-1]) {
            Cell cell = queue.poll();
            queue.remove(cell);
            cell.visit();
            for (Cell neib : cell.neighbours()) {
                relax(cell, neib);
            }
        }
    }

    private void dfs(Cell cell) {
        if (cell.visited()) {
            return;
        }
        cell.visit();
        cell.setTime(0);
        for (Cell neib : cell.neighbours()) {
            if (cell.canVisit(neib, 0)) {
                dfs(neib);
            } else {
                relax(cell, neib);
            }
        }
    }

    private void readData() throws Exception {
        h = scanner.nextInt();
        n = scanner.nextInt();
        m = scanner.nextInt();
        floor = new int[n][m];
        ceiling = new int[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                ceiling[i][j] = scanner.nextInt();
            }
        }
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                floor[i][j] = scanner.nextInt();
            }
        }
        time = new double[n][m];
        for (double[] ar : time) {
            Arrays.fill(ar, -1);
        }
        visited = new boolean[n][m];
        queue = new PriorityQueue<Cell>();
    }
    
    private void outputResult(int i) throws Exception {
        printWriter.printf("Case #%d: %.7f\n", i, time[n - 1][m - 1]);
    }
    
    private void runTest(int testNumber) throws Exception {
        readData();
        dfs(new Cell(0, 0));
        dijkstra();
        outputResult(testNumber);
    }
    
    private void runAll() throws Exception {
        scanner = new Scanner(new File("input.txt"));
        printWriter = new PrintWriter(new FileWriter("output.txt"));
        int n = scanner.nextInt();
        for (int i = 0; i < n; i++) {
            runTest(i + 1);
        }
        printWriter.close();
    }


    public static void main(String[] args) throws Exception {
        new Thread() {
            @Override
            public void run() {
               try {
                   new TaskB().runAll();
               } catch (Exception e) {
                   e.printStackTrace();
               }
            }
        }.start();                              ;
    }
}
