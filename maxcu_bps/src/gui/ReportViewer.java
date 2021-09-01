package gui;

import javax.swing.*;

import gurobi.GRBException;
import maxcu.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import report.Report;

public class ReportViewer extends JFrame{
	private static final long serialVersionUID = 1891623189280012L;
	private final Report r;
	private final int numChapters;
	private int currentChapter;
	
	private JButton btnNext;
	private JButton btnPrev;
	private JScrollPane scroll;
	
	private JTextArea jta;
	
	public ReportViewer(Report r)
	{
		this.r = r;
		initWindow2();
		jta.setText(r.getChapter(0));
		jta.setCaretPosition(0);
		numChapters = r.getNumChapters();
		currentChapter = 0;
	}
	
	private void initWindow2()
	{
		//see http://stackoverflow.com/a/13907527
		
		Font font = new Font("monospaced", Font.PLAIN, 12);
		
		this.setTitle("Report");
		this.setSize(new Dimension(1000, 700));
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	      
		jta = new JTextArea(8, 40);
	    jta.setEditable(false);
	    jta.setFocusable(true);
	    jta.setFont(font);
	    scroll = new JScrollPane(jta,
	    		  JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
					JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	    
	    jta.setBorder( //http://stackoverflow.com/a/2287750
	    		   javax.swing.BorderFactory.createCompoundBorder(
	    		      javax.swing.BorderFactory.createTitledBorder(
	    		         null, "",
	    		         javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
	    		         javax.swing.border.TitledBorder.DEFAULT_POSITION,
	    		         new java.awt.Font("Verdana", 1, 11)
	    		      ),
	    		      javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8)
	    		   )
	    		);
	    
	    
	    JPanel textPanel = new JPanel(new BorderLayout());
	    //textPanel.add(new JLabel("Chat:", SwingConstants.LEFT), BorderLayout.PAGE_START);
	    textPanel.add(scroll);
	      
	      
	    btnNext = new JButton("Next chapter");
	    
	    btnNext.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				onNext();
			}
		});
	    
	    
		btnPrev = new JButton("Previous chapter");
		btnPrev.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				onPrev();
			}
		});
		
		JPanel controls = new JPanel();
		controls.setLayout(new BoxLayout(controls, BoxLayout.LINE_AXIS));
		controls.add(btnPrev);
		controls.add(btnNext);
		  
		JPanel mainPanel = new JPanel();
	    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
	    mainPanel.add(textPanel);
	    mainPanel.add(Box.createVerticalStrut(10));
	    mainPanel.add(controls);
	      
	    this.add(mainPanel);
	}

	private void onPrev()
	{
		if(currentChapter <= 0)
			return;
		
		jta.setText(r.getChapter(--currentChapter));
		jta.setCaretPosition(0);
	}
	
	private void onNext()
	{
		if(currentChapter >= numChapters -1)
			return;
		
		jta.setText(r.getChapter(++currentChapter));
		jta.setCaretPosition(0);
	}
	
	public static void main(String[] args) throws GRBException
	{
		PreferenceTable o = new PrefTableAuto(2);
		TimeSlot tsA = new TimeSlot("A", 1);
		TimeSlot tsB = new TimeSlot("B", 1);
		Bundle first = new Bundle(new TimeSlot[]{tsA});
		Bundle second = new Bundle(new TimeSlot[]{tsA, tsB});
		
		o.addPreference("1", first, 1);
		o.addPreference("1", second, 2);
		o.addPreference("2", second, 3);

		Report r = null;

		MAXCU m = new MAXCU(o, 0.1, 0.25, new Report(true));
		m.calculate();

		r = m.getReport();
		
		ReportViewer rv = new ReportViewer(r);
		
		rv.setVisible(true);
	}
}
