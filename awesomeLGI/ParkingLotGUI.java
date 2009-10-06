package awesomeLGI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * @author bobby
 * It's far from Complete.
 */
//@SuppressWarnings("serial")
public class ParkingLotGUI extends JFrame implements ActionListener, ChangeListener{

	private Vector<GateRecord> gates = new Vector<GateRecord>();
	private Vector<Car> cars = new Vector<Car>();
	private int size;
	private int minLength;
	
	private int method;
	
	private Calendar sevenAM;
	
	private JPanel mainp;
	private JPanel gatep;
	private JPanel ginfop;
	private JPanel lotp;
	private JPanel linfop;
	private JPanel carp;
	private JPanel cinfop;
	private JPanel optp;
	
	public static final int NAIVE = 0;
	public static final int METHOD_1 = 1;
	public static final int METHOD_2 = 2;
	
	public ParkingLotGUI(Vector<GateRecord> gates, Vector<Car> cars,int size, int method, int minLength)
	{	
		this.gates = gates;
		this.cars = cars;
		this.size = size;
		this.method = method;
		this.minLength = minLength;
		sevenAM = Calendar.getInstance();
		
		this.setTitle("Parking Lot");
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setBounds(300, 200, 450, 300);
		this.setVisible(true);
		this.setResizable(false);
		JTabbedPane jtp = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT);
		this.add(jtp);
		
		mainp = new JPanel(new FlowLayout(FlowLayout.CENTER, 75, 10));
		gatep = new JPanel(new BorderLayout());
		lotp = new JPanel(new BorderLayout());
		carp = new JPanel(new BorderLayout());
		optp = new JPanel(new BorderLayout());
		mainp.setName("Overview");
		gatep.setName("Gates");
		lotp.setName("Parking Lot");
		carp.setName("Cars");
		optp.setName("Options");

		JPanel refp = new JPanel(new BorderLayout());
		refp.add(new JLabel("Refreshing..."), BorderLayout.CENTER);
		refp.setName("Refresh");
		
		makeMainPanel();
		makeGatePanel();
		makeLotPanel();
		makeCarPanel();
		makeOptionsPanel(this.method);
		
		jtp.addTab("Overview", mainp);
		jtp.addTab("Gates", gatep);
//		jtp.addTab("Parking Lot", lotp);
		//jtp.addTab("Cars", carp);
		jtp.addTab("Options", optp);
		jtp.addTab("Refresh", refp);

		jtp.addChangeListener(this);
		jtp.setSelectedIndex(0);
	}

	private void makeMainPanel(){
		mainp.removeAll();

		JLabel timel = new JLabel("Current Time");
		JLabel avgl = new JLabel("Average Wait Time");
		JLabel gatel = new JLabel("Number of Gates");
		JLabel tcarl = new JLabel("Total Number of Cars");
		JLabel pcarl = new JLabel("Number of Parked Cars");
		JLabel wcarl = new JLabel("Number of Waiting Cars");
		JLabel tlotl = new JLabel("Number of Total Lots");
		JLabel alotl = new JLabel("Number of Lots Available");
		JLabel tokl = new JLabel("Number of Tokens Available");
		JLabel trafl = new JLabel("Traffic");
		JLabel tokpl = new JLabel("Token Redistribution System");

		JTextField timetf = new JTextField(timeNow(Calendar.getInstance())); 
		JTextField avgtf = new JTextField(averageWaitTime(gates));    
		JTextField gatetf = new JTextField(gates.size() + " Gates");
		//JTextField tcartf = new JTextField(cars.size() + " Cars");
		//JTextField pcartf = new JTextField(numberParked(cars) + " Cars");
		JTextField wcartf = new JTextField(numberWaiting(gates) + " Cars");
		JTextField tlottf = new JTextField(size + " Parking Lots");
		//JTextField alottf = new JTextField((size - numberParked(cars)) + " Parking Lots");
		JTextField toktf = new JTextField(numberTokens(gates) + " Tokens");
		JTextField traftf = new JTextField("Default"); // TODO how to change this
		JTextField tokptf = new JTextField("Naive"); // TODO how to change this

		timetf.setEditable(false);
		avgtf.setEditable(false);
		gatetf.setEditable(false);
		//tcartf.setEditable(false);
		//pcartf.setEditable(false);
		wcartf.setEditable(false);
		tlottf.setEditable(false);
		//alottf.setEditable(false);
		toktf.setEditable(false);
		traftf.setEditable(false);
		tokptf.setEditable(false);

		mainp.add(timel);
		mainp.add(timetf);
		mainp.add(avgl);
		mainp.add(avgtf);
		mainp.add(gatel);
		mainp.add(gatetf);
		//mainp.add(tcarl);
		//mainp.add(tcartf);
		//mainp.add(pcarl);
		//mainp.add(pcartf);
		mainp.add(wcarl);
		mainp.add(wcartf);
		mainp.add(tlotl);
		mainp.add(tlottf);
		//mainp.add(alotl);
		//mainp.add(alottf);
		mainp.add(tokl);
		mainp.add(toktf);
		mainp.add(trafl);
		mainp.add(traftf);
		mainp.add(tokpl);
		mainp.add(tokptf);
	}

	private void makeGatePanel(){
		gatep.removeAll();

		JPanel ggp;
		ginfop = new JPanel(new GridLayout(2,3));
		int gatenum = gates.size();
		JButton[] gate = new JButton[gatenum];
		if (gatenum % 5 == 0){
			ggp = new JPanel(new GridLayout(gatenum / 5, 5));	
		}else{
			ggp = new JPanel(new GridLayout(gatenum / 5 + 1, 5));
		}
		ggp.setBackground(Color.BLACK);
		for(int i = 0; i < gatenum; i++){
			gate[i] = new JButton("G" + i);
			ggp.add(gate[i]);
			gate[i].addActionListener(this);
		}

		JScrollPane jsp = new JScrollPane(ggp);
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		gatep.add(jsp, BorderLayout.WEST);
		gatep.add(ginfop, BorderLayout.EAST);
	}

	// Lot Panel NOT USED
	private void makeLotPanel(){
		lotp.removeAll();

		JPanel llp;
		linfop = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
		int lotnum = size;
		JButton[] lot = new JButton[lotnum];
		if (lotnum % 5 == 0){
			llp = new JPanel(new GridLayout(lotnum / 5, 5));	
		}else{
			llp = new JPanel(new GridLayout(lotnum / 5 + 1, 5));
		}
		llp.setBackground(Color.BLACK);
		for(int i = 0; i < lotnum; i++){
			lot[i] = new JButton("L" + i);
			llp.add(lot[i]);
			lot[i].addActionListener(this);
		}
		JScrollPane jsp = new JScrollPane(llp);
		jsp.setMaximumSize(new Dimension(250,400));
		jsp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		lotp.add(jsp , BorderLayout.WEST);
		lotp.add(linfop, BorderLayout.CENTER);	
	}
//No
	private void makeCarPanel(){
		carp.removeAll();
		String[] scar = new String[cars.size()];
		for(int i = 0; i < scar.length; i++){
			scar[i] = ("Car" + cars.get(i).carID);
		}
		JPanel ctp = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));
		cinfop = new JPanel(new FlowLayout(FlowLayout.CENTER, 25, 10));

		JLabel cinfo = new JLabel("Select A Car");
		JLabel lcil = new JLabel("Last Car Index");

		JComboBox jcb = new JComboBox(scar);
		jcb.setEditable(false);
		
		if(scar.length != 0){
			jcb.addActionListener(this);
			jcb.setEnabled(true);
		}else {
			jcb.setEnabled(false);
		}
		
		JTextField lcitf = new JTextField("" + scar.length);
		lcitf.setEditable(false);

		ctp.add(lcil);
		ctp.add(lcitf);
		ctp.add(cinfo);
		ctp.add(jcb);
		
		ctp.setBorder(BorderFactory.createLineBorder(Color.black));
		
		carp.add(ctp, BorderLayout.NORTH);
		carp.add(cinfop, BorderLayout.CENTER );
	}

	private void makeOptionsPanel(int method){
		optp.removeAll();

		JPanel trafp = new JPanel(new GridLayout(0,1));
		JPanel tokep = new JPanel(new GridLayout(0,1));

		JLabel trafl = new JLabel("Traffic Flow");
		JLabel tokel = new JLabel("Token Redistribution Method");

		ButtonGroup trafb = new ButtonGroup();
		ButtonGroup tokeb = new ButtonGroup();

		JRadioButton high = new JRadioButton("High", false);
		JRadioButton med = new JRadioButton("Medium", false);
		JRadioButton low = new JRadioButton("Low", false);
		JRadioButton def = new JRadioButton("Default", true);

		JRadioButton naive = new JRadioButton("Naive", true);
		JRadioButton meth1 = new JRadioButton("Method 1", false);
		JRadioButton meth2 = new JRadioButton("Method 2", false);

		high.addActionListener(this);
		med.addActionListener(this);
		low.addActionListener(this);
		def.addActionListener(this);
		naive.addActionListener(this);
		meth1.addActionListener(this);
		meth2.addActionListener(this);

		trafb.add(high);
		trafb.add(med);
		trafb.add(low);
		trafb.add(def);

		tokeb.add(naive);
		tokeb.add(meth1);
		tokeb.add(meth2);

		trafp.add(trafl);
		trafp.add(high);
		trafp.add(med);
		trafp.add(low);
		trafp.add(def);

		tokep.add(tokel);
		tokep.add(naive);
		tokep.add(meth1);
		tokep.add(meth2);

		if(method >= 0){
			if (method == NAIVE){
				naive.setSelected(true);
			}else if(method == METHOD_1){
				meth1.setSelected(true);
			}else if(method == METHOD_2){
				meth2.setSelected(true);
			}
		}
		
		optp.add(trafp, BorderLayout.WEST);
		optp.add(tokep, BorderLayout.EAST);

	}

	public void actionPerformed(ActionEvent e) {

		if(Character.isDigit(e.getActionCommand().charAt(1))){
			if(e.getActionCommand().charAt(0) == 'G'){
				ginfop.removeAll();
				JLabel gatel = new JLabel("Gate ID");
				JLabel ncarl = new JLabel("Number of Cars");
				//JLabel fcarl = new JLabel("First Car ID");
				JLabel tokl = new JLabel("Tokens");

				GateRecord gr = gates.get(Integer.parseInt(e.getActionCommand().substring(1,e.getActionCommand().length())));
				JTextField gatetf = new JTextField(gr.address);
				JTextField ncartf = new JTextField(gr.waitingCars.size() + " Cars");
				//JTextField fcartf = new JTextField("Car"+gr.waitingCars.get(0).carID);
				JTextField toktf = new JTextField(gr.tokens +" Tokens");

				gatetf.setEditable(false);
				ncartf.setEditable(false);
//				fcartf.setEditable(false);
				toktf.setEditable(false);

				ginfop.add(gatel);
				ginfop.add(gatetf);
				ginfop.add(ncarl);
				ginfop.add(ncartf);
				//ginfop.add(fcarl);
				//ginfop.add(fcartf);
				ginfop.add(tokl);
				ginfop.add(toktf);
				this.repaint();
			}
			else if(e.getActionCommand().charAt(0) == 'L'){
				// Lot Panel NOT Used
				linfop.removeAll();

				JLabel carl = new JLabel("Car ID");
				JLabel waitl = new JLabel("Car's Wait Time");

				JTextField cartf = new JTextField("");//gates.get(Integer.parseInt(e.getActionCommand().substring(1,e.getActionCommand().length())));
				JTextField waittf = new JTextField("00:00");

				cartf.setEditable(false);
				waittf.setEditable(false);

				linfop.add(carl);
				linfop.add(cartf);
				linfop.add(waitl);
				linfop.add(waittf);				
				this.repaint();
			}
		}
		else{
			if(e.getActionCommand().charAt(0) == 'c'){
				cinfop.removeAll();

				JComboBox jcb = (JComboBox)e.getSource();
				Car c = cars.get(jcb.getSelectedIndex());

				JLabel cidl = new JLabel("Car ID");
				JLabel progl = new JLabel("Progress");
				JLabel cgel = new JLabel("Gate Car Entered");
				JLabel cgll = new JLabel("Gate Car Left");
				JLabel waitl = new JLabel("Car's Wait Time");

				JTextField cidtf = new JTextField("Car" + c.carID);
				JTextField progtf = new JTextField(carProgress(c));
				JTextField cgetf = new JTextField(c.gateIn);
				String temp = c.gateOut; 
				JTextField cgltf = new JTextField(((temp == null ? "N/A" : temp))); 
				JTextField waittf = new JTextField(waitTime(c));

				cidtf.setEditable(false);
				progtf.setEditable(false);
				cgetf.setEditable(false);
				cgltf.setEditable(false);
				waittf.setEditable(false);

				cinfop.add(cidl);
				cinfop.add(cidtf);
				cinfop.add(progl);
				cinfop.add(progtf);
				cinfop.add(waitl);
				cinfop.add(waittf);
				cinfop.add(cgel);
				cinfop.add(cgetf);
				cinfop.add(cgll);
				cinfop.add(cgltf);
				this.repaint();

			} else if(e.getActionCommand().equals("Low")){
				//TODO set traffic to low
			} else if(e.getActionCommand().equals("Medium")){
				//TODO set traffic to medium
			} else if(e.getActionCommand().equals("High")){
				//TODO set traffic to high
			} else if(e.getActionCommand().equals("Default")){
				//TODO set traffic to default
			} else if(e.getActionCommand().equals("Naive")){
				//Careful for infinite loop
				//TODO set TokenPassing to Naive
			} else if(e.getActionCommand().equals("Method 1")){
				//TODO set tokenPassing to Method 1?
			} else if(e.getActionCommand().equals("Method 2")){
				//TODO set TokenPassing to method 2?
			}
		}
	}

	public void stateChanged(ChangeEvent e) {
		//System.out.println(e.getSource());
		JTabbedPane jtp = (JTabbedPane)e.getSource();
		String name = ((JPanel)jtp.getSelectedComponent()).getName();
		if(name.equals("Overview")){
			this.setSize(450, 300);
			makeMainPanel();
			this.repaint();
		} else if (name.equals("Gates")){
			this.setSize(600, 200);
			makeGatePanel();
			this.repaint();
		} else if (name.equals("Parking Lot")){
			this.setSize(550, 150);
			makeLotPanel();
			this.repaint();
		} else if (name.equals("Cars")){
			this.setSize(550, 200);
			makeCarPanel();
			this.repaint();
		} else if (name.equals("Options")){
			this.setSize(500, 200);
			makeOptionsPanel(method);
			this.repaint();
		}  else if (name.equals("Refresh")){
			jtp.setSelectedIndex(0);
		}
	}
	 
	String timeNow(Calendar now){
		String output = "";
		long time = now.getTimeInMillis() - sevenAM.getTimeInMillis();
		long totMins = time / minLength;
		int hrs = (int)totMins / 60;
		int mins = (int)totMins % 60;
		boolean AM = false;
		if ((hrs + 7) % 24 < 12 && (hrs + 7) % 24 >= 0)
			AM = true;
		if((hrs + 7) % 12 == 0 ){
			output = (AM ? "12:" + mins + "AM" : "12:" + mins + "PM");
		}else{
			output = (AM ? "" + ((hrs + 7) % 12)+ ":" + mins + "AM" : "" + ((hrs + 7) % 12)+ ":" + mins + "PM");
		}
		if (output.charAt(1) == ':')
			output = "0" + output;
		
		for(int i = 0; i < output.length(); i++){
			if(output.charAt(i)== 'A' || output.charAt(i)== 'P'){
				if (output.charAt(i - 2) == ':'){
					output = output.substring(0, i-1) + "0" + output.substring(i-1);
					break;
				}
			}
		}
		return output;
		
	}
	// No
	int numberParked(Vector<Car> cars){
		int number = 0;
		for (int i = 0; i < cars.size(); i++){
			Car c = cars.get(i);
			//TODO how to know where a car is from the monitor?
			//number++;
		}
		return number;
	}
	
	int numberWaiting(Vector<GateRecord> gates){
		int number = 0;
		for (int i = 0; i < gates.size(); i++){
			GateRecord gr = gates.get(i);
			number += gr.waitingCars.size();
		}
		return number;
	}
	
	int numberTokens(Vector<GateRecord> gates){
		int number = 0;
		for (int i = 0; i < gates.size(); i++){
			number = gates.get(i).tokens;
		}
		return number;
	}
	
	String averageWaitTime(Vector<GateRecord> gates){
		String output = "";
		long totMins = 0;
		int cars = 0;
		for(int i = 0; i < gates.size(); i++){
			for(int j = 0; j < gates.get(i).waitingCars.size(); j++){
				totMins += (gates.get(i).waitingCars.get(j).fromGate.getTimeInMillis() - gates.get(i).waitingCars.get(j).toGate.getTimeInMillis()) / minLength / 60;
				cars++;
			}
			
		}
		
		if(cars != 0)
			totMins = totMins / cars;
		output = ("" + (totMins / 60) + ":" + (totMins % 60) + "");
		
		if (output.charAt(1) == ':')
			output = "0" + output;
		if (output.charAt(output.length() - 2) == ':'){
			output = output.substring(0, output.length() - 1) + "0" + output.substring(output.length()-1);
		}
		return output;
	}
	//No
	String waitTime(Car c){
	//TODO need wait time for 1 car
		return "00:00";
	}
	//No
	String carProgress(Car c){
		// TODO help
		return "Waiting";
	}

	public void updateGateRecord(Vector<GateRecord> gates){
		this.gates = gates;
	}
	//No
	public void updateCars(Vector<Car> cars){
		this.cars = cars;	
	}
	
	public void updateML(int minLength){
		this.minLength = minLength;
	}
	

}