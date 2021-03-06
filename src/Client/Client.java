package Client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import Message.*;
//用户主界面
public class Client extends JFrame{
	// 设置界面风格   
    {   
        try {   
            UIManager.setLookAndFeel(/*javax.swing.UIManager.getSystemLookAndFeelClassName()*/"com.sun.java.swing.plaf.windows.WindowsLookAndFeel");   
        } catch (Exception ex) {   
            ex.printStackTrace();   
        }   
    } 
	
	private String myName; //用户名字
	private LogIn logIn; //登陆界面对象
	private CheckClients checkClients; //查看用户类的对象
	
	private JTextField jtf=new JTextField(); //输入单词框
	private JButton jbt=new JButton("Translate"); //翻译按钮
	private JButton jbtCheckClients=new JButton("查看其他用户在线状态");
	private JButton jbtCheckWordCard=new JButton("查看是否收到了单词卡");
	//三个复选框
	private JCheckBox jcbYoudao=new JCheckBox("有道");
	private JCheckBox jcbBaidu=new JCheckBox("百度");
	private JCheckBox jcbBing=new JCheckBox("必应");
	//三个显示翻译的文本区，Like为每个翻译及其点赞选项，发送单词卡按钮封装成的类
	private Like firstLike=new Like();
	private Like secondLike=new Like();
	private Like thirdLike=new Like();
	private boolean likeLock=true;
	
	
	Socket socket=null;
	private ObjectOutputStream objtoServer=null;
	private ObjectInputStream objfromServer=null;
	
	public void setMyName(String name){
		myName=name;
	}
	
	public static void main(String args[]){
		new Client();
	}
	
	public Client(){
		connectToServer(); //连接到服务器
		logIn=new LogIn(this,socket,objfromServer,objtoServer);//先显示登录界面，将主界面设为不可见
		initGui();//画界面
		registerListener();//注册监听器
	}
	
	public void initGui(){
		JPanel p1=new JPanel();
		p1.setLayout(new FlowLayout());
		p1.add(jcbYoudao);
		p1.add(jcbBaidu);
		p1.add(jcbBing);
				
		JPanel p=new JPanel();
		p.setLayout(new BorderLayout(10,10));
		p.add(new JLabel("Input word"),BorderLayout.WEST);
		p.add(jtf,BorderLayout.CENTER);
		jtf.setHorizontalAlignment(JTextField.LEFT);
		p.add(jbt,BorderLayout.EAST);
		p.add(p1,BorderLayout.SOUTH);
		
		JPanel p6=new JPanel();
		p6.setLayout(new BorderLayout(20,30));
		p6.add(p,BorderLayout.CENTER);
		p6.add(new JPanel(),BorderLayout.NORTH);
		p6.add(new JPanel(),BorderLayout.WEST);
		p6.add(new JPanel(),BorderLayout.EAST);
		//p6.add(new JPanel(),BorderLayout.SOUTH);
		
		JPanel p2=new JPanel();
		p2.setLayout(new GridLayout(3, 1, 10, 10));
		p2.add(firstLike);
		p2.add(secondLike);
		p2.add(thirdLike);
		
		JPanel p5=new JPanel();
		p5.setLayout(new BorderLayout(20,20));
		p5.add(p2,BorderLayout.CENTER);
		p5.add(new JPanel(),BorderLayout.NORTH);
		p5.add(new JPanel(),BorderLayout.WEST);
		p5.add(new JPanel(),BorderLayout.EAST);
		p5.add(new JPanel(),BorderLayout.SOUTH);
		
		JPanel p3=new JPanel();
		//p3.setLayout(new GridLayout(1, 2,10,10));
		p3.setLayout(new FlowLayout());
		p3.add(jbtCheckClients);
		p3.add(jbtCheckWordCard);
		
		JPanel p4=new JPanel();
		p4.setLayout(new BorderLayout(20,40));
		p4.add(p3,BorderLayout.CENTER);
		//p4.add(new JPanel(),BorderLayout.NORTH);
		p4.add(new JPanel(),BorderLayout.WEST);
		p4.add(new JPanel(),BorderLayout.EAST);
		p4.add(new JPanel(),BorderLayout.SOUTH);
		
		setLayout(new BorderLayout());
		add(p6,BorderLayout.NORTH);
		add(p5,BorderLayout.CENTER);
		add(p4,BorderLayout.SOUTH);
		
		
		//将窗口置于屏幕中央
		//
		
		pack();
		setSize(1024,768);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setVisible(false);
	}
	
	public void registerListener(){
		jbt.addActionListener(new WordSearchListener());
		jbtCheckClients.addActionListener(new CheckClientsListener());
		jbtCheckWordCard.addActionListener(new CheckWordCardListener());
	}
	//查看是否收到单词卡的事件监听程序
	private class CheckWordCardListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			try{
				AskForWordCardMessage afwcm=new AskForWordCardMessage();
				objtoServer.writeObject(afwcm);
				objtoServer.flush();
				
				AnswerAskForWordCardMessage aafwcm=(AnswerAskForWordCardMessage)objfromServer.readObject();
				if(aafwcm.getIsThereWordCard()){
					ArrayList<SendWordCardMessage> wordCards=aafwcm.getWordCardsToBeSent();
					
					for(int i=0;i<wordCards.size();i++){
						int res=JOptionPane.showConfirmDialog(null,  "您收到一张来自"+wordCards.get(i).getSenderName()+"的单词卡，是否查看？", "有单词卡！", JOptionPane.YES_NO_OPTION);
						if(res==0){
							new WordCard(wordCards.get(i).getContent(),wordCards.get(i).getBackgroundColor(),wordCards.get(i).getFontColor(),wordCards.get(i).getFont(),wordCards.get(i).getSenderName());
						}
					}
				}
				else{
					JOptionPane.showMessageDialog(null, "您尚未收到任何单词卡~", "alert", JOptionPane.ERROR_MESSAGE);
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
	
	public void connectToServer(){
		try{
			socket=new Socket("localhost",8000);
			
			objfromServer=new ObjectInputStream(socket.getInputStream());
			
			objtoServer=new ObjectOutputStream(socket.getOutputStream());
			
		}
		catch(IOException ex){
			System.err.println(ex);
		}
	}
	//查单词的事件监听程序
	private class WordSearchListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			likeLock=true;
			
			String toBeTranslated=jtf.getText();
			//输入为空报错
			if(toBeTranslated.equals("")){
				JOptionPane.showMessageDialog(null, "输入不能为空！", "alert", JOptionPane.ERROR_MESSAGE);
				return;
			}
			//不是英文单词则报错
			for(int i=0;i<toBeTranslated.length();i++){
				char c=toBeTranslated.charAt(i);
				if(!((c>='a'&&c<='z')||(c>='A'&&c<='Z'))){
					JOptionPane.showMessageDialog(null, "输入非单词！", "alert", JOptionPane.ERROR_MESSAGE);
					return;
				}
			}
			
			WordSearchMessage wsm=new WordSearchMessage(toBeTranslated);
			
			wsm.setBaidu(jcbBaidu.isSelected());
			wsm.setBing(jcbBing.isSelected());
			wsm.setYoudao(jcbYoudao.isSelected());
			if(!jcbBaidu.isSelected()&&!jcbBing.isSelected()&&!jcbYoudao.isSelected()){
				wsm.setBaidu(true);
				wsm.setBing(true);
				wsm.setYoudao(true);
			}
			
			try{
				objtoServer.writeObject(wsm);
				objtoServer.flush();
				
				AnswerWordSearchMessage awsm=(AnswerWordSearchMessage)objfromServer.readObject();
				
				firstLike.refresh("", "");
				secondLike.refresh("", "");
				thirdLike.refresh("", "");
				
				if(!awsm.getWordExists()){
					JOptionPane.showMessageDialog(null, "该单词不存在！", "alert", JOptionPane.ERROR_MESSAGE);
				}
				else{
					String[] dicPriority=awsm.getDicPriority();
					String[] translation=awsm.getTranslation();

					if(!(dicPriority[0]==null||translation[0]==null||dicPriority[0].equals("")||translation[0].equals(""))){
						firstLike.refresh(dicPriority[0], translation[0]);
					}
					
					if(!(dicPriority[1]==null||translation[1]==null||dicPriority[1].equals("")||translation[1].equals(""))){
						secondLike.refresh(dicPriority[1], translation[1]);
					}
					
					if(!(dicPriority[2]==null||translation[2]==null||dicPriority[2].equals("")||translation[2].equals(""))){
						thirdLike.refresh(dicPriority[2], translation[2]);
					}
				}
			}
			catch(IOException ex){
				System.err.println(ex);
			}
			catch(ClassNotFoundException ex){
				System.err.println(ex);
			}
			
			likeLock=false;
		}
	}
	//查看在线用户的监听程序
	private class CheckClientsListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			checkClients=new CheckClients(socket,objfromServer,objtoServer);
		}
		
	}
	//每条翻译结果封装成的类
	class Like extends JPanel{
		String labelString=null;
		JLabel jtfDic=new JLabel("Dictionary");//词典网站名，每次搜索都刷新
		JCheckBox jcbLike=new JCheckBox("点赞");//点赞选择框
		JButton jbtSend=new JButton("发送单词卡");//发送单词卡按钮
		JTextArea jtaTrans=new JTextArea();//显示翻译的文本区
		
		public Like(){
			setLikeGui();
			registerLikeListener();
		}
		//画界面
		public void setLikeGui(){
			
			JPanel p=new JPanel();
			p.setLayout(new BorderLayout());
			p.add(jcbLike,BorderLayout.CENTER);
			p.add(jbtSend,BorderLayout.SOUTH);
			jtfDic.setPreferredSize(new Dimension(60, 30));
			
			JPanel p1=new JPanel();
			p1.setLayout(new BorderLayout(0,30));
			p1.add(p,BorderLayout.CENTER);
			p1.add(new JPanel(),BorderLayout.NORTH);
			p1.add(new JPanel(),BorderLayout.WEST);
			p1.add(new JPanel(),BorderLayout.SOUTH);
			
			setLayout(new BorderLayout(10,10));
			jtaTrans.setEditable(false);
			add(p1,BorderLayout.EAST);
			add(jtfDic,BorderLayout.WEST);
			add(new JScrollPane(jtaTrans),BorderLayout.CENTER);
		}
		
		public void registerLikeListener(){
			jcbLike.addActionListener(new LikeChoosingListener());
			jbtSend.addActionListener(new SendWordCardListener());
		}
		//发送单词卡的监听程序
		private class SendWordCardListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(jtaTrans.getText()==null||jtaTrans.getText().equals(""))
					JOptionPane.showMessageDialog(null, "该翻译结果为空！", "alert", JOptionPane.ERROR_MESSAGE);
				else
					new SendWordCard(socket,objfromServer,objtoServer,myName,jtaTrans.getText());
			}
		}
		//点赞监听程序
		private class LikeChoosingListener implements ActionListener{

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				if(!likeLock){
					if(labelString==null||labelString.equals("")||(!(labelString.equals("Baidu")||labelString.equals("Youdao")||labelString.equals("Bing")))){
						return;
					}
					else if(jcbLike.isSelected()){
						try{
							LikeUpdateMessage lum=new LikeUpdateMessage(labelString, 1);
							objtoServer.writeObject(lum);
							objtoServer.flush();
						}
						catch(IOException ex){
							System.err.println(ex);
						}
					}
					else{
						try{
							LikeUpdateMessage lum=new LikeUpdateMessage(labelString, -1);
							objtoServer.writeObject(lum);
							objtoServer.flush();
						}
						catch(IOException ex){
							System.err.println(ex);
						}
					}
				}
			}
		}
		//每次搜索后刷新对应的三个翻译顺序
		public void refresh(String dicName,String translation){
			labelString=dicName;
			
			if(!dicName.equals(""))
				jtfDic.setText(dicName);
			else
				jtfDic.setText("Dictionary");
			
			jtaTrans.setText(translation);
			
			jcbLike.setSelected(false);
		}
	}
}