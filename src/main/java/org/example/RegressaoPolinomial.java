package org.example;

import org.apache.commons.math3.analysis.polynomials.PolynomialFunction;
import org.apache.commons.math3.fitting.PolynomialCurveFitter;
import org.apache.commons.math3.fitting.WeightedObservedPoints;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RegressaoPolinomial {

    static List<Double> tList = new ArrayList<>();
    static List<Double> fList = new ArrayList<>();

    public static void main(String[] args) throws Exception {
        lerArquivo("src/main/resources/data.txt");
        System.out.printf("%-6s %-40s %s%n", "Grau", "Coeficientes", "R2 Ajustado");

        List<Integer> graus = new ArrayList<>();
        List<Double> r2Valores = new ArrayList<>();

        for (int grau = 1; grau <= 10; grau++) {
            PolynomialFunction funcao = ajustarPolinomio(grau);
            double r2aj = calcularR2Ajustado(funcao, grau);

            System.out.printf("%-6d %-40s %.4f%n", grau, Arrays.toString(funcao.getCoefficients()), r2aj);

            plotarGrafico(funcao, grau);
            graus.add(grau);
            r2Valores.add(r2aj);
        }

        plotarR2Ajustado(graus, r2Valores);
    }

    public static void lerArquivo(String caminho) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(caminho))) {
            String linha;
            while ((linha = br.readLine()) != null) {
                linha = linha.trim();
                if (linha.isEmpty()) continue;

                String[] partes = linha.split("\\s+");
                if (partes.length < 2) continue;

                try {
                    tList.add(Double.parseDouble(partes[0]));
                    fList.add(Double.parseDouble(partes[1]));
                } catch (NumberFormatException e) {
                    System.err.println("Linha inválida ignorada: " + linha);
                }
            }
        }
    }


    public static PolynomialFunction ajustarPolinomio(int grau) {
        WeightedObservedPoints obs = new WeightedObservedPoints();
        for (int i = 0; i < tList.size(); i++) {
            obs.add(tList.get(i), fList.get(i));
        }
        PolynomialCurveFitter fitter = PolynomialCurveFitter.create(grau);
        double[] coef = fitter.fit(obs.toList());
        return new PolynomialFunction(coef);
    }

    public static double calcularR2Ajustado(PolynomialFunction funcao, int grau) {
        double ssRes = 0.0;
        double ssTot = 0.0;
        double mediaY = fList.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);

        for (int i = 0; i < tList.size(); i++) {
            double y = fList.get(i);
            double yPred = funcao.value(tList.get(i));
            ssRes += Math.pow(y - yPred, 2);
            ssTot += Math.pow(y - mediaY, 2);
        }

        double r2 = 1 - (ssRes / ssTot);
        int n = tList.size();
        return 1 - (1 - r2) * (n - 1) / (n - grau - 1);
    }

    public static void plotarGrafico(PolynomialFunction funcao, int grau) {
        List<Double> yPred = tList.stream().map(funcao::value).collect(Collectors.toList());

        XYChart chart = new XYChartBuilder().width(600).height(400)
                .title("Ajuste - Grau " + grau).xAxisTitle("t").yAxisTitle("f(t)").build();

        chart.addSeries("Pontos", tList, fList);
        chart.addSeries("Ajuste (grau " + grau + ")", tList, yPred);

        new SwingWrapper<>(chart).displayChart();
    }

    public static void plotarR2Ajustado(List<Integer> graus, List<Double> r2Valores) {
        XYChart chart = new XYChartBuilder().width(600).height(400)
                .title("R² Ajustado por Grau").xAxisTitle("Grau do Polinômio").yAxisTitle("R² Ajustado").build();

        chart.addSeries("R² Ajustado", graus, r2Valores);

        new SwingWrapper<>(chart).displayChart();
    }

}
