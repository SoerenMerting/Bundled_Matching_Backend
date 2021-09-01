package maxcu;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;

import maxcu.ModelMatrix.constrType;
import gurobi.GRBException;
import py4j.GatewayServer;

public class FacadeDMatrix {
	double[][] dMatrix;
	double[] dConstraint;
	constrType[] dConstraintType;
	double[] costCoeff;
	ModelMatrix mm;
	MAXCU maxcu;

	public FacadeDMatrix() {

	}
	public void initFacade(int firstdim, int seconddim){
		this.dMatrix = new double[firstdim][seconddim];
		this.dConstraint = new double[firstdim];
		this.dConstraintType = new constrType[firstdim];
		this.costCoeff = new double[seconddim];
	}
	
	public void deleteData(){
		this.dMatrix = null;
		this.dConstraint = null;
		this.dConstraintType = null;
		this.costCoeff = null;
		this.mm = null;
		this.maxcu = null;
		System.gc();
		System.out.println("Data Deleted");
	}
	
	public void killProcess(){
		System.exit(0);
	}
	
	public void fillDMatrix(int row, ArrayList<Integer> rowentry){

		for(int i = 0;i<rowentry.size();i++){
			this.dMatrix[row][rowentry.get(i).intValue()] = 1.0;
		}
	}
	
	public void filldConstraint(ArrayList<Integer> dConstraint){
		if(dConstraint.size()!=this.dConstraint.length){
			throw new IllegalArgumentException();
		}
		for(int i = 0;i<dConstraint.size();i++){
			this.dConstraint[i] = (double) (dConstraint.get(i).intValue());
		}
	}
	
	public void fillCostCoeff(ArrayList<Integer> costCoeff){
		if(costCoeff.size()!=this.costCoeff.length){
			throw new IllegalArgumentException();
		}
		for(int i = 0;i<costCoeff.size();i++){
			this.costCoeff[i] = (double) (costCoeff.get(i).intValue());
		}
	}
	
	public void filldConstraintType(ArrayList<String> dConstraintType){
		if(dConstraintType.size()!=this.dConstraintType.length){
			throw new IllegalArgumentException();
		}
		for(int i = 0;i<dConstraintType.size();i++){
			String type = dConstraintType.get(i);
			
			if("demand".equals(type)){
			this.dConstraintType[i] = constrType.demand;
			}

			if("supply".equals(type)){
			this.dConstraintType[i] = constrType.supply;
			}
		}
	}
	
	public void createModelMatrix(int bundleSize){
		this.mm = new ModelMatrix(this.dMatrix,this.dConstraint,this.costCoeff,this.dConstraintType,bundleSize);
	}
	
    public ArrayList<Double> getLambdas() {
        ArrayList<Double> lambdas = new ArrayList<Double>();
        for (double d : this.maxcu.getLambdas())
            lambdas.add(d);
        return lambdas;
    }
    
    public ArrayList<ArrayList<Double>> getVectors() {
        double[][] vectors = maxcu.getS();
        ArrayList<ArrayList<Double>> result = new ArrayList<ArrayList<Double>>();
        for (double[] elem : vectors) {
            ArrayList<Double> temp = new ArrayList<Double>();
            for(int i = 0;i<elem.length;i++){
                temp.add(elem[i]);
            }
            result.add(temp);
        }
        return result;
    }
	
    public void writeDMatrix(){
    	Charset utf = StandardCharsets.UTF_8;
    	Writer writer = null;
    	try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("facadedmatrix.txt"),"utf-8"));
			double[][] dmatrix = this.mm.getDmatrix();
			writer.write("[ \n");
			for(int i = 0;i<dmatrix.length;i++){
				String line = "[";
				int j=0;
				for(;j<dmatrix[0].length-1;j++){
					line+=""+dmatrix[i][j]+",";
				}
				
				line+=""+dmatrix[i][j]+"]\n";
				writer.write(line);

			}
			writer.write(" \n ]");
			
    	} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally {
			   try {writer.close();} catch (Exception ex) {/*ignore*/}
		}
    }
    
	public void createMaxcu(ArrayList<Double> bps, double epsilon, double delta)throws GRBException
	{
		System.out.println("Rows:"+this.dMatrix.length);
		System.out.println("Cols:"+this.dMatrix[0].length);
		System.out.println("Epsilon:"+epsilon);
		if(bps.size()!=this.dMatrix[0].length){
			throw new IllegalArgumentException();
		}
		double[] solution = new double[bps.size()];
		for(int i = 0;i<bps.size();i++){
			solution[i] = (double) bps.get(i);
		}
			System.out.println("Try to create MAXCU");
			this.maxcu = new MAXCU(this.mm, epsilon, delta, solution, 0.0);
			System.out.println("Maxcu Created"+this.maxcu);

		
	}
	
	public void calculate(){
		try {
			System.out.println("Maxcu calculate");
			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
			this.maxcu.calculate();
			System.out.println("Maxcu calculated");
			for(double[] vec:this.maxcu.getS()){
				System.out.println(Arrays.toString(vec)+", ");
			}
		} catch (GRBException e) {
			// TODO Auto-generated catch block
			System.out.println("Errorcode:"+e.getErrorCode()+"ErrorMessage:"+e.getMessage());
			e.printStackTrace();
		}
	}
	
	public String getProperty(){
		return System.getProperty("java.library.path");
	}
	public static void main(String args[]){
		FacadeDMatrix fc = new FacadeDMatrix();
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		System.out.println(timeStamp);
        GatewayServer gs = new GatewayServer(fc,25335);
        System.out.println(System.getProperty("java.library.path"));
        gs.start();
        System.out.println("GESTARTET");
        
	}
}
