package Client;

import java.awt.BorderLayout;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;

import Message.AnswerCheckClientsMessage;
import Message.CheckClientsMessage;

public class CheckClients extends JFrame{
	private Socket socket;
	private ObjectOutputStream objtoServer=null;
	private ObjectInputStream objfromServer=null;
	JList jlonlineClients=new JList<String>();
	JList jlofflineClients=new JList<String>();
	
	public CheckClients(Socket socket,ObjectInputStream objfromServer,ObjectOutputStream objtoServer){
		this.socket=socket;
		this.objfromServer=objfromServer;
		this.objtoServer=objtoServer;
		setGui();
		addThread();
	}
	
	public void setGui(){
		JPanel p1=new JPanel();
		p1.setLayout(new BorderLayout());
		p1.add(new JLabel("�����û�"),BorderLayout.NORTH);
		p1.add(jlonlineClients,BorderLayout.CENTER);
		
		JPanel p2=new JPanel();
		p2.setLayout(new BorderLayout());
		p2.add(new JLabel("�����û�"),BorderLayout.NORTH);
		p2.add(jlofflineClients,BorderLayout.CENTER);
		
		JPanel p=new JPanel();
		p.setLayout(new BoxLayout(p,BoxLayout.X_AXIS));
		p.add(p1);
		p.add(p2);
		
		setLayout(new BorderLayout(10,10));
		add(p,BorderLayout.CENTER);
		//������������Ļ����
		setLocationRelativeTo(null);
		setTitle("CheckClient");
		setSize(800,600);
		setVisible(true);
	}
	
	public void addThread(){
		Refresh task=new Refresh();
		new Thread(task).start();
	}
	
	class Refresh implements Runnable{
		@Override
		public void run() {
			// TODO Auto-generated method stub
			while(!socket.isClosed()){
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
					jlonlineClients.setModel(dlm);
				
					dlm = new DefaultListModel();
					for(int i=0;i<offlineClientsList.size();i++){
							dlm.addElement(offlineClientsList.get(i));
					}
					jlofflineClients.setModel(dlm);
					
					Thread.sleep(1000);
				}
				catch(IOException ex){
					System.err.println(ex);
				}
				catch(ClassNotFoundException ex){
					System.err.println(ex);
				}
				catch(InterruptedException ex){
					System.err.println(ex);
				}
				
			}
		}
	}
	

}