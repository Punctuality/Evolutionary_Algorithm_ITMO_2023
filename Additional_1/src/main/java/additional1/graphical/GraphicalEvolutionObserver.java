package additional1.graphical;

import additional1.codec.TspDescription;
import additional1.codec.TspEdgeType;
import additional1.codec.nodes.TspNode2D;
import additional1.evoalgo.TspSolution;
import org.uncommons.watchmaker.framework.EvolutionObserver;
import org.uncommons.watchmaker.framework.PopulationData;

import javax.swing.*;
import java.awt.*;
import java.util.Arrays;

public class GraphicalEvolutionObserver extends JFrame implements EvolutionObserver<TspSolution> {
    final int width;
    final int height;
    final int generations;
    final TspDescription problemDescription;
    final double maxY;
    final double maxX;
    final double minX;
    final double minY;
    final Timer repainter;
    int timerTicks = 0;
    boolean toRepaint = false;
    boolean updateInfo = false;
    int infoForceUpdate = 10;

    TspSolution bestCandidate;

    double bestResult = Double.MAX_VALUE;
    double meanResult = Double.MAX_VALUE;
    int currentGen = -1;
    final JLabel bestResultText = new JLabel();
    final JLabel meanResultText = new JLabel();
    final JLabel currentGenText = new JLabel();

    public GraphicalEvolutionObserver(int width, int height, int generations, TspDescription problemDescription) {
        super("TSP Evolution Observer");
        this.width = width;
        this.height = height;
        this.generations = generations;
        this.problemDescription = problemDescription;

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(this.width, this.height);


        JPanel info = new JPanel();
        FlowLayout layout = new FlowLayout();
        info.setLayout(layout);

        info.add(this.bestResultText);
        info.add(this.meanResultText);
        info.add(this.currentGenText);

        // FlowLayout vertical

        BorderLayout coreLayout = new BorderLayout();

        getContentPane().setLayout(coreLayout);
        getContentPane().add(info, BorderLayout.NORTH);

        if (problemDescription.nodeCoordSection[0].getClass() != TspNode2D.class) {
            throw new IllegalArgumentException("GraphicalEvolutionObserver: unsupported node type");
        } else {
            this.maxX = Arrays.stream(this.problemDescription.nodeCoordSection)
                    .map(node -> (TspNode2D) node)
                    .mapToDouble(TspNode2D::x)
                    .max()
                    .orElseThrow();
            this.maxY = Arrays.stream(this.problemDescription.nodeCoordSection)
                    .map(node -> (TspNode2D) node)
                    .mapToDouble(TspNode2D::y)
                    .max()
                    .orElseThrow();
            this.minX = Arrays.stream(this.problemDescription.nodeCoordSection)
                    .map(node -> (TspNode2D) node)
                    .mapToDouble(TspNode2D::x)
                    .min()
                    .orElseThrow();
            this.minY = Arrays.stream(this.problemDescription.nodeCoordSection)
                    .map(node -> (TspNode2D) node)
                    .mapToDouble(TspNode2D::y)
                    .min()
                    .orElseThrow();
        }

        JPanel edgesDisplay = new JPanel() {
            int adjustX(double x) {
                return (int) ((x - minX) / (maxX - minX) * (this.getWidth() - 50)) + 25;
            }
            int adjustY(double y) {
                return (int) ((y - minY) / (maxY - minY) * (this.getHeight() - 50)) + 25;
            }

            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D graphics = (Graphics2D) g;

                graphics.setBackground(Color.WHITE);
                Arrays.stream(problemDescription.nodeCoordSection)
                        .map(node -> (TspNode2D) node)
                        .forEach(node -> {
                            graphics.setColor(Color.BLACK);
                            graphics.fillOval(adjustX(node.x()) - 2,  adjustY(node.y()) - 2, 4, 4);
                            graphics.drawString(String.valueOf(node.getId()), adjustX(node.x()) + 5, adjustY(node.y()) + 5);
                        });

                if (bestCandidate != null) {
                    graphics.setColor(Color.RED);
                    for (int i = 1; i <= problemDescription.dimension; i++) {
                        int from = bestCandidate.orderedSolution[i - 1]; // This way we achieve full route cycle
                        int to = bestCandidate.orderedSolution[i == problemDescription.dimension ? 0 : i];

                        TspNode2D fromN = (TspNode2D) problemDescription.nodeCoordSection[from - 1];
                        TspNode2D toN = (TspNode2D) problemDescription.nodeCoordSection[to - 1];
                        graphics.drawLine(adjustX(fromN.x()), adjustY(fromN.y()), adjustX(toN.x()), adjustY(toN.y()));
                    }
                }
            }
        };

        this.repainter = new Timer(20, e -> {
            if (timerTicks++ % infoForceUpdate == 0) {
                this.updateInfo = true;
            }
            if (this.toRepaint) {
                edgesDisplay.repaint();
                this.toRepaint = false;
            }
            if (this.updateInfo) {
                this.bestResultText.setText(String.format("Best result: %.2f", bestResult));
                this.meanResultText.setText(String.format("Mean result: %.2f", meanResult));
                this.currentGenText.setText(String.format("Current generation: %d/%d", currentGen, this.generations));
                this.updateInfo = false;
            }
        });

        getContentPane().add(edgesDisplay, BorderLayout.CENTER);
    }

    public void start() {
        this.setVisible(true);
        this.repainter.start();
    }

    public void stop() {
        this.repainter.stop();
    }

    @Override
    public void populationUpdate(PopulationData<? extends TspSolution> populationData) {
        if (populationData.getMeanFitness() != this.meanResult) {
            this.meanResult = populationData.getMeanFitness();
            this.updateInfo = true;
        }
        this.currentGen = populationData.getGenerationNumber();
        if (populationData.getBestCandidateFitness() < this.bestResult) {
            this.bestResult = populationData.getBestCandidateFitness();
            this.bestCandidate = populationData.getBestCandidate();
            this.toRepaint = true;
            this.updateInfo = true;
        }
    }
}
