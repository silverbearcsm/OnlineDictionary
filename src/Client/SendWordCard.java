package Client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.*;

import Message.*;
//发送单词卡的界面
public class SendWordCard extends JFrame{
	// 设置界面风格   
    {   
        try {   
            UIManager.setLookAndFeel(/*javax.swing.UIManager.getSystemLookAndFeelClassName()*/"com.sun.java.swing.plaf.windows.WindowsLookAndFeel");   
        } catch (Exception ex) {   
            ex.printStackTrace();   
        }   
    }
	
	private Socket socket;
	private ObjectOutputStream objtoServer=null;
	private ObjectInputStream objfromServer=null;
	
	private String senderName=null; //发送者名字
	private String content=null; //发送内容
	private Color backgroundColor=Color.WHITE; //单词卡背景色
	private Color fontColor=Color.BLACK; //单词卡字体颜色
	private Font font=new Font("Dialog", Font.PLAIN, 20); //单词卡字体
	
	JList jlClients=new JList<String>(); //可选择的用户列表
	JList jlExpressions=new JList<String>(); //可选择的表情列表
	JButton jbtChooseBackgroundColor=new JButton("选择单词卡背景色"); //选背景色按钮
	JButton jbtChooseFont=new JButton("选择字体和颜色"); //选字体和颜色按钮
	JButton jbtReview=new JButton("预览"); //预览按钮
	JButton jbtSend=new JButton("发送"); //发送按钮
	
	
	public SendWordCard(Socket socket,ObjectInputStream objfromServer,ObjectOutputStream objtoServer,String senderName,String content){
		this.socket=socket;
		this.objfromServer=objfromServer;
		this.objtoServer=objtoServer;
		
		this.senderName=senderName;
		this.content=content;
		
		setGui();
		registerListener();
		showClients();
	}
	//画界面
	public void setGui(){
		jlClients.setBorder(BorderFactory.createTitledBorder("您要发送给（按住Ctrl可群发）："));
		jlExpressions.setBorder(BorderFactory.createTitledBorder("您还可以点击为单词卡添加以下表情："));
		drawExpressions();
		
		jlClients.setBounds(40, 40, 240, 480);
		
		jlExpressions.setBounds(320, 40, 240, 480);
		
		jbtChooseBackgroundColor.setBounds(600, 95, 160, 40);
		
		jbtChooseFont.setBounds(600, 205, 160, 40);
		
		jbtReview.setBounds(600, 315, 160, 40);
		
		jbtSend.setBounds(600, 425, 160, 40);


		add(jbtChooseBackgroundColor);
		add(jbtChooseFont);
		add(jbtReview);
		add(jbtSend);
		
		add(jlClients);
		add(jlExpressions);
			
		setLayout(null);
		setTitle("Send Wordcard");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		pack();
		setSize(800,600);
		setLocationRelativeTo(null);
		setVisible(true);
	}
	//显示可选表情
	public void drawExpressions(){
		DefaultListModel dlm = new DefaultListModel();
		dlm.addElement("( ⊙ o ⊙ )");
		dlm.addElement("/(ㄒoㄒ)/~~");
		dlm.addElement("O(∩_∩)O~~");
		dlm.addElement("￣へ￣");
		dlm.addElement("(⊙﹏⊙)");
		dlm.addElement("(ノω<。)ノ))☆.。");
		dlm.addElement("o(>﹏<)o");
		dlm.addElement("(╯﹏╰)");
		jlExpressions.setModel(dlm);
	}
	//注册监听器
	public void registerListener(){
		jbtChooseBackgroundColor.addActionListener(new ChooseBackgroundListener());
		jbtChooseFont.addActionListener(new ChooseFontListener());
		jbtReview.addActionListener(new ReviewListener());
		jbtSend.addActionListener(new SendListener());
	}
	//背景色选择监听程序
	private class ChooseBackgroundListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			backgroundColor=JColorChooser.showDialog(SendWordCard.this, "请选择单词卡背景颜色", backgroundColor);
		}
		
	}
	//字体选择监听程序
	private class ChooseFontListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			JFontChooser jfc=new JFontChooser();
			jfc.showDialog(SendWordCard.this, 600, 300);
			font=jfc.getSelectedfont();
			fontColor=jfc.getSelectedcolor();
			if(font!=null&&fontColor!=null){   
	        /*打印用户选择的字体和颜色*/   
				System.out.println(font);   
				System.out.println(fontColor);   
	        }
		}	
	}
	//预览事件监听程序
	private class ReviewListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			String tempContent=content;
			String exp=(String)jlExpressions.getSelectedValue();
			if(exp!=null&&!exp.equals("")){
				tempContent=exp+"\n"+tempContent;
			}
			new WordCard(tempContent, backgroundColor, fontColor, font, senderName);
		}
	}
	//发送事件监听程序
	private class SendListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			Object[] tempReceivers=jlClients.getSelectedValues();
			if(tempReceivers.length==0||(String)tempReceivers[0]==null||((String)tempReceivers[0]).equals("")){
				JOptionPane.showMessageDialog(null, "您尚未选择有效的发送用户", "alert", JOptionPane.ERROR_MESSAGE);
			}
			else{
				String[] receivers=new String[tempReceivers.length];
				for(int i=0;i<tempReceivers.length;i++){
					receivers[i]=(String)tempReceivers[i];
				}
				
				try{
					String tempContent=content;
					String exp=(String)jlExpressions.getSelectedValue();
					if(exp!=null&&!exp.equals("")){
						tempContent=exp+"\n"+tempContent;
					}
					
					for(int i=0;i<receivers.length;i++){
						SendWordCardMessage swcm=new SendWordCardMessage(senderName, receivers[i], tempContent, backgroundColor, fontColor, font);
						objtoServer.writeObject(swcm);
						objtoServer.flush();
					}
					
					for(int i=0;i<receivers.length;i++){
						AnswerSendWordCardMessage aswcm=(AnswerSendWordCardMessage)objfromServer.readObject();
						if(!aswcm.getIsClientOnline()){
							JOptionPane.showMessageDialog(null, "o(>﹏<)o不好！"+aswcm.getReceiver()+"居然不在学习，跑去浪了~~", "alert", JOptionPane.ERROR_MESSAGE);
						}else{
							JOptionPane.showMessageDialog(null, "发送给"+aswcm.getReceiver()+"成功！O(∩_∩)O~~");
						}
					}
				}
				catch(IOException ex){
					ex.printStackTrace();
				}
				catch(ClassNotFoundException ex){
					ex.printStackTrace();
				}
			}
		}
		
	}
	//展示可以发送单词卡的用户列表
	public void showClients(){
		try{
			CheckClientsMessage ccm=new CheckClientsMessage();
			objtoServer.writeObject(ccm);
			objtoServer.flush();
			
			AnswerCheckClientsMessage accm=(AnswerCheckClientsMessage)objfromServer.readObject();
			ArrayList<String> onlineClientsList=accm.getOnlineClientsList();
			ArrayList<String> offlineClientsList=accm.getOfflineClientsList();
			
			DefaultListModel dlm = new DefaultListModel();
			for(int i=0;i<onlineClientsList.size();i++){
					dlm.addElement(onlineClientsList.get(i));
			}
			
			for(int i=0;i<offlineClientsList.size();i++){
					dlm.addElement(offlineClientsList.get(i));
			}
			jlClients.setModel(dlm);
			
		}
		catch(IOException ex){
			System.err.println(ex);
		}
		catch(ClassNotFoundException ex){
			System.err.println(ex);
		}
	}
}
