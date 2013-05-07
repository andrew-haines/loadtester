package com.ahaines.loadtest.report;

import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.RangeType;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.util.ShapeUtilities;

import com.ahaines.loadtest.ReportConfig;
import com.ahaines.loadtest.report.ErrorReport.ErrorReportFactory;


public class GraphWriter implements ReportWriter<Message> {
	
	private final File file;
	private final Plotter throughputOverTime;
	private final Plotter errorsOverTime;
	private final Plotter latencyOverTime;
	
	private final ReportWriter<Message> throughputWriter;
	private final ReportWriter<Message> errorWriter;
	private final ReportWriter<Message> latencyWriter;
	private final String reportName;

	public GraphWriter(ReportConfig config) {
		
		System.setProperty("java.awt.headless","true"); //ensures graphing can occur outside of an X display
		
		this.file = config.getFile();
		int interval = config.getSampleInterval();
		reportName = config.getReportName();
		
		try{
			PlotWriter<AggregatedMessage> throughputOverTime = new HitRatePlotWriter("throughput", file, "no. requests");
			ErrorRatePlotWriter errorsOverTime = new ErrorRatePlotWriter(file, interval);
			PlotWriter<Message> latencyOverTime = new DefaultPlotWriter<Message>("latency", file, "request time (ms)");
			
			throughputWriter = new OverallThroughputReport(interval, throughputOverTime);
			errorWriter = new ErrorReport(errorsOverTime);
			latencyWriter = new AccumalatorReportWriter(500, latencyOverTime); // average over 50ms
			//latencyWriter = latencyOverTime;
			
			this.throughputOverTime = throughputOverTime;
			this.errorsOverTime = errorsOverTime;
			this.latencyOverTime = latencyOverTime;
		} catch (FileNotFoundException e){
			throw new IllegalArgumentException("unable to create one of the plotters", e);
		}
		
	}

	@Override
	public void writeStats(Message message) {
		throughputWriter.writeStats(message);
		errorWriter.writeStats(message);
		latencyWriter.writeStats(message);
		
	}

	@Override
	public void close() throws WriterException {
		throughputWriter.close();
		errorWriter.close();
		latencyWriter.close();
		
		outputWriter(throughputOverTime, true);
		outputWriter(errorsOverTime, false);
		outputWriter(latencyOverTime, false);
	}
	
	private void outputWriter(Plotter plotter, boolean showLines) throws WriterException{
		DefaultXYDataset dataset = new DefaultXYDataset();
		plotter.addSeriesPointsToDataset(dataset);
		JFreeChart chart;
		if (showLines){
			chart = ChartFactory.createXYLineChart(reportName, "time run at (ms)", plotter.getYAxisName(), dataset, PlotOrientation.VERTICAL, true, false, false);
		} else{
			chart = ChartFactory.createScatterPlot(reportName, "time run at (ms)", plotter.getYAxisName(), dataset, PlotOrientation.VERTICAL, true, false, false);
		}
		configureChart(chart);
		BufferedImage graphImage = chart.createBufferedImage(800, 800);
		
		try {
			File file = new File(this.file.getAbsolutePath()+"_"+plotter.getSeriesName()+".jpg");
			ImageIO.write(graphImage, "png", file);
		} catch (IOException e) {
			throw new WriterException("unable to save file", e);
		}
	}
	
	private void configureChart(JFreeChart chart){
		
		Shape point = ShapeUtilities.createRegularCross(1, 1);
		XYPlot plot = chart.getXYPlot();

		NumberAxis numberAxis = (NumberAxis)plot.getDomainAxis();
		numberAxis.setAutoRangeMinimumSize(0.1);
		numberAxis.setLowerBound(0);
		numberAxis.setAutoRangeIncludesZero(true);
		numberAxis.setRangeType(RangeType.POSITIVE);
		for (int i = 0; i < plot.getSeriesCount(); i++){
			plot.getRenderer().setSeriesShape(i, point);
		}
	}
}

interface Plotter{
	
	public String getSeriesName();
	
	public void addSeriesPointsToDataset(DefaultXYDataset dataset);
	
	public String getYAxisName();
}

interface PlotWriter<MessageType extends Message> extends ReportWriter<MessageType>, Plotter{
	
}

class DefaultPlotWriter<MessageType extends Message> implements PlotWriter<MessageType> {
	
	private final String seriesName;
	private final List<Double> xData;
	private final List<Double> yData;
	protected final ReportWriter<MessageType> writer;
	private final String yAxisName;
	
	DefaultPlotWriter(String seriesName, File file, String yAxisName) throws FileNotFoundException{
		this.seriesName = seriesName;
		xData = new LinkedList<Double>();
		yData = new LinkedList<Double>();
		File dataFile = new File(file.getAbsoluteFile()+"_"+seriesName+".csv");
		if (dataFile.exists()){
			// kill existing file if there is one
			dataFile.delete();
		}
		try {
			dataFile.createNewFile();
		} catch (IOException e) {
			throw new FileNotFoundException("Unable to create file: "+dataFile.getAbsolutePath());
		}
		FileOutputStream dataStream = new FileOutputStream(dataFile);
		this.writer = new CsvWriter<MessageType>(new PrintStream(dataStream));
		this.yAxisName = yAxisName;
	}

	@Override
	public void writeStats(MessageType message) {
		addSample(message.getStartTime(), message.getRunTime());
		writer.writeStats(message);
	}
	
	protected void addSample(double x, double y){
		xData.add(x);
		yData.add(y);
	}

	@Override
	public void close() throws WriterException {
		writer.close();
	}
	
	private double[][] getSeriesPoints(){
		double[][] datapoints = new double[2][];
		datapoints[0] = new double[xData.size()];
		datapoints[1] = new double[yData.size()];
		
		if (datapoints[0].length != datapoints[1].length){
			throw new IllegalStateException("The x and y points are not the same length");
		}
		
		Iterator<Double> xIter = xData.iterator();
		Iterator<Double> yIter = yData.iterator();
		int i = 0;
		while (xIter.hasNext()){
			datapoints[0][i] = xIter.next();
			datapoints[1][i++] = yIter.next();
		}
		
		return datapoints;	
	}
	
	public void addSeriesPointsToDataset(DefaultXYDataset dataset){
		double[][] seriesData = this.getSeriesPoints();
		dataset.addSeries(this.getSeriesName(), seriesData);
	}

	@Override
	public String getSeriesName() {
		return seriesName;
	}

	@Override
	public String getYAxisName() {
		return yAxisName;
	}
}

class HitRatePlotWriter extends DefaultPlotWriter<AggregatedMessage>{

	HitRatePlotWriter(String seriesName, File file, String yAxisName) throws FileNotFoundException {
		super(seriesName, file, yAxisName);
	}

	@Override
	public void writeStats(AggregatedMessage message) {
		addSample(message.getStartTime(), message.getHitRate());
		writer.writeStats(message);
	}
}

class ErrorRatePlotWriter implements Plotter, ErrorReportFactory {

	private final Map<Integer, PlotWriter<AggregatedMessage>> errorWriters;
	private final File dataFile;
	private final int throughputInterval;
	
	ErrorRatePlotWriter(File dataFile, int throughputInterval){
		this.errorWriters = new HashMap<Integer, PlotWriter<AggregatedMessage>>();
		this.dataFile = dataFile;
		this.throughputInterval = throughputInterval;
	}
	
	private ReportWriter<AggregatedMessage> getWriter(int statusCode) {
		
		PlotWriter<AggregatedMessage> errorWriterForCode = errorWriters.get(statusCode);
		
		if (errorWriterForCode == null){
			try {
				errorWriterForCode = new HitRatePlotWriter("Error Code "+statusCode, dataFile, getYAxisName());
			} catch (FileNotFoundException e) {
				throw new IllegalStateException("Unable to locate file for writing for error code: "+statusCode, e);
			}
			errorWriters.put(statusCode, errorWriterForCode);
		}
		
		return errorWriterForCode;
	}

	@Override
	public String getSeriesName() {
		return "Error";
	}
	@Override
	public void addSeriesPointsToDataset(DefaultXYDataset dataset) {
		for (Plotter plotter: errorWriters.values()){
			plotter.addSeriesPointsToDataset(dataset);
		} 
	}
	@Override
	public String getYAxisName() {
		return "num errors";
	}
	@Override
	public ReportWriter<Message> createReportWriter(int statusCode) {
		return new OverallThroughputReport(throughputInterval, getWriter(statusCode));
	}
	
}