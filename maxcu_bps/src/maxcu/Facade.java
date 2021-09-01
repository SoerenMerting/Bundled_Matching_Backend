package maxcu;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Arrays;

import gurobi.GRBException;
import py4j.GatewayServer;
import report.Report;

public class Facade {
    private PreferenceTable prefT;
    private MAXCU maxcu;
    private TimeSlot[] tsarray;
    private Bundle[] bundlesarray;
    private String[] clientsarray;
    private ModelMatrix mm;
    private int maxBundleSize  = 0;




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


    public ArrayList<Double> getLambdas() {
        ArrayList<Double> lambdas = new ArrayList<Double>();
        for (double d : this.maxcu.getLambdas())
            lambdas.add(d);
        return lambdas;
    }

    public void addClientList(ArrayList<String> clients){
        this.clientsarray = clients.toArray(new String[clients.size()]);
    }


    public void addTimeslots(ArrayList<Integer> tsid, ArrayList<Integer> capacity){
        this.tsarray = new TimeSlot[tsid.size()];

        for(int i = 0;i<tsid.size();i++){
            this.tsarray[tsid.get(i)] = new TimeSlot(tsid.get(i).toString(),capacity.get(i));
        }

    }

    public void initBundlearray(int size){
        this.bundlesarray = new Bundle[size];
    }

    public void addBundle(int index, ArrayList<Integer> tsIndex){
        TimeSlot[] ts = new TimeSlot[tsIndex.size()];
        for(int i = 0;i<tsIndex.size();i++){
            ts[i] = this.tsarray[tsIndex.get(i)];
        }
        Bundle b = new Bundle(ts);
        this.bundlesarray[index] = b;
    }
    //
    //	public void addBundleList(ArrayList<ArrayList<Integer>> bundlelist ){
    //		ArrayList<Bundle> bundles = new ArrayList<Bundle>();
    //		for(int i = 0;i<bundlelist.size();i++){
    //			ArrayList<TimeSlot> slotsofbundle = new ArrayList<TimeSlot>();
    //			for(int j = 0;j<bundlelist.get(i).size();j++){
    //				int index = bundlelist.get(i).get(j);
    //				slotsofbundle.add(this.tsarray[index]);
    //			}
    //			Bundle b = new Bundle(slotsofbundle.toArray(new TimeSlot[slotsofbundle.size()]));
    //			bundles.add(b);
    //		}
    //		this.bundlesarray = bundles.toArray(new Bundle[bundles.size()]);
    //	}
    //
    public void addPreferences(int clientid, int bundleid, int value){
        this.prefT.addPreference(this.clientsarray[clientid], this.bundlesarray[bundleid], value);
    }

    public void initPrefTable(){
        this.prefT = new PrefTableManual(maxBundleSize, this.clientsarray, this.bundlesarray);
    }
    
    public void initMM(){
    	this.mm = new ModelMatrix(this.prefT);
    }
    
    public double[][] getDMatrix(){
    	return this.mm.getDmatrix();
    }


    public void createMaxcu(double epsilon, double delta, ArrayList<Double> fractDV, double fractOpt)throws GRBException{
        double[] fractDVarray = new double[fractDV.size()];
        String solution = "" ;
        System.out.println("The solution of bps:" + fractDV.toString());
        for(int i = 0; i<fractDV.size();i++){
            fractDVarray[i] = fractDV.get(i);
            solution = fractDVarray[i]+", ";

        }
        System.out.println(solution);



            this.maxcu = new MAXCU(this.mm, epsilon, delta, fractDVarray, fractOpt);

            System.out.println("Starting calculation");
            this.maxcu.calculate();
            System.out.println("Calculation finished");

    }

    public void init(int maxBundleSize){
        this.maxBundleSize = maxBundleSize;
    }



	public void init4(int maxBundleSize) throws GRBException {
		System.out.println("Starte init4");
		HashMap<String,TimeSlot> slots = new HashMap<String, TimeSlot>();
		ArrayList<Bundle> bundles = new ArrayList<Bundle>();
		TimeSlot tx = new TimeSlot("x", 1);
		TimeSlot ty = new TimeSlot("y", 2);
		TimeSlot tz = new TimeSlot("z", 3);
		Bundle xy = new Bundle(new TimeSlot[] { tx,ty });
		Bundle xz = new Bundle(new TimeSlot[] { tx, tz });
		Bundle x =new Bundle(new TimeSlot[] { tx });
		Bundle yz = new Bundle(new TimeSlot[] { ty, tz });
		Bundle z =new Bundle(new TimeSlot[] {tz });
		Bundle y = new Bundle(new TimeSlot[] { ty });
		String a = "1";
		String b = "2";
		String c = "3";
		prefT = new PrefTableManual(maxBundleSize,new String[]{a,b,c}, new Bundle[]{xy,xz,yz,x,y,z});
		//stud 1
		//stud 1
		prefT.addPreference(a, xy, 6);
//		prefT.addPreference(a, xz, 5);
//		prefT.addPreference(a, x, 4);
		prefT.addPreference(a, yz, 5);
		prefT.addPreference(a, z, 4);
//		prefT.addPreference(a, y, 1);
		//stud 2
		prefT.addPreference(b, xy, 6);
		prefT.addPreference(b, yz, 5);
		prefT.addPreference(b, z, 4);

		//stud 3
		prefT.addPreference(c, xy, 6);
		prefT.addPreference(c, z, 5);

		double[][] vectors = null;
		double[] lambdas = null;
		ModelMatrix tempM = new ModelMatrix(prefT);
		System.out.println(tempM.constraintTable());
		System.out.println(prefT.toStringTable());
		double[] solution =  {0.333333,0.5,0.16666666,
							  0.333333,0.5,0.16666666,
							  0.333333,0.66666666};

			maxcu = new MAXCU(tempM, 0.3, 0.3,solution,1.0);
			maxcu.calculate();



			String output = "vectors : ";
			vectors = maxcu.getS();
			System.out.println(vectors);
			for(double[] arr: vectors){
				output+="[";
				for(double elem : arr){
					output+=elem+", ";
				}
				output+="]\n";
			}
			output += "lambdas: \n";

			lambdas = maxcu.getLambdas();
			for (double eleme: lambdas){
				output+=eleme + " ";
			}
			System.out.println(output);


	}
	
    public void writeDMatrix(){
    	Charset utf = StandardCharsets.UTF_8;
    	Writer writer = null;
    	try {
			writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("facade.txt"),"utf-8"));
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


    public static void main(String args[]) throws GRBException {
    	Facade fc = new Facade();
    	fc.init4(2);
        GatewayServer gs = new GatewayServer(fc);
        System.out.println(System.getProperty("java.library.path"));
        gs.start();
        System.out.println("GESTARTET");

    }
}
