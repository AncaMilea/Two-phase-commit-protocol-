import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class Participant {
    Integer port_coord;
    Integer port_part;
    Integer timeout;
    Integer failure_type;
    Set<Integer> other_part= new HashSet<>();
    List<String> options= new ArrayList<>();
    String my_option= "VOTE ";
    String my_vote_letter;
    String final_resul=null;
    List<ParticipantHandler> handlers = new ArrayList<>();
    HashMap<Integer,String> vote_opt_port= new HashMap<>();
    AtomicBoolean flagR= new AtomicBoolean(false);
    public Participant(String cport, String pport, String timeout,String failure){
        this.port_coord= Integer.parseInt(cport);
        this.port_part= Integer.parseInt(pport);
        this.timeout= Integer.parseInt(timeout);
        this.failure_type= Integer.parseInt(failure);
    }

    public Integer getPort_coord()
    {
        return this.port_coord;
    }

    public Integer getPort_part()
    {
        return this.port_part;
    }
    public Integer getTimeout()
    {
        return this.timeout;
    }
    public Integer getFailure_type()
    {
        return this.failure_type;
    }
    public static void main(String[] args) throws IOException
    {
        Participant p= new Participant(args[0],args[1],args[2],args[3]);
        try
        {
            // getting localhost ip
            InetAddress ip = InetAddress.getByName("localhost");

            // establish the connection with server port
            Socket s = new Socket(ip, p.getPort_coord());

            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());
            dos.writeUTF("JOIN "+p.getPort_part().toString());
            while (true)
            {
                String received = dis.readUTF();
                p.decrypt(received);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            if (p.flagR.get()) {
                                try {
                                    String out= "OUTCOME "+ p.final_resul;
                                    for(Integer it:p.other_part){
                                        out= out+" "+ it;
                                    }
                                    out= out+" "+p.getPort_part();
                                    dos.writeUTF(out);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }).start();

            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void decrypt(String received) throws IOException {
        Token type = (Token) MessageToken.getToken(received);
        if(type instanceof DetailsToken){
            this.other_part.addAll(((DetailsToken) type)._ports);

            Iterator<Integer> it= this.other_part.iterator();
            while(it.hasNext()){
                System.out.println("I got "+it.next());
            }
        }
        if(type instanceof VoteOptionsToken){
            this.options.addAll(((VoteOptionsToken) type)._votes);

            Random randNum = new Random();
            int aRandomPos = randNum.nextInt((this.options).size());//Returns a nonnegative random number less than the specified maximum (firstNames.Count).
            this.my_vote_letter= this.options.get(aRandomPos);
            this.my_option = this.my_option + this.getPort_part()+" "+ this.options.get(aRandomPos);
            System.out.println("My option is "+this.my_option);

            this.startListening(this.getPort_part());
            this.startTalking(this.my_option);

        }
    }
    public void startListening(Integer port_curr) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocket ss = null;
                try {
                    ss = new ServerSocket(port_curr);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                while (true) {
                    Socket s = null;

                    try {
                        // socket object to receive incoming client requests
                        s = ss.accept();

//                    cord.setAll_part(s.getPort());
                        System.out.println("A new participant is connected : " + s);

                        // obtaining input and out streams
                        DataInputStream dis = new DataInputStream(s.getInputStream());
                        DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                     new Thread(new Runnable() {
                         @Override
                         public void run() {
                             String received= null;
                             while(vote_opt_port.size()<other_part.size()) {

                                 try {
                                     if(dis.available()>0)
                                       received = dis.readUTF();
                                 } catch (EOFException e) {
                                     System.out.println("I reached the end of the file");
                                      break;
                                 } catch (IOException e) {
                                     e.printStackTrace();
                                 }
//                        System.out.println(received);
                                 saveVote(received);
                             }
                             start_choosing(vote_opt_port);
                         }
                     }).start();


                    } catch (Exception e) {
                        try {
                            s.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        e.printStackTrace();
                    }
                }

            }

        }).start();

    }
    public void start_choosing(HashMap<Integer,String> str){
        HashMap<String,Integer> keep_score= new HashMap<String,Integer>();
        keep_score.put(this.my_vote_letter, 1);
        for(String it:str.values()){
            for(String s: options){
                if(it.equals(s)){
                    if(keep_score.containsKey(it)){
                        keep_score.put(it, keep_score.get(it)+1);
                    }else{
                        keep_score.put(it, 1);
                    }
                }
            }
        }
        int max=0;
        String result=null;
        for(String i:keep_score.keySet()){
            System.out.println(i+" "+ keep_score.get(i));
            if(max<keep_score.get(i))
            {
                max= keep_score.get(i);
                result= i;
            }else{
                if(max == keep_score.get(i))
                {
                    result= "Tie";
                }
            }
        }
       this.final_resul=result;
       this.flagR.set(true);

    }
    public void startTalking(String my_opt)
    {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    // getting localhost ip
                    InetAddress ip = InetAddress.getByName("localhost");

                    // establish the connection with server port
                    Iterator<Integer> it= other_part.iterator();
                    while(it.hasNext()) {
                        Socket s = new Socket(ip, it.next());

                        // obtaining input and out streams
                        DataInputStream dis = new DataInputStream(s.getInputStream());
                        DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                            dos.writeUTF(my_opt);

                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }
    public void saveVote(String toreturn){
        System.out.println(toreturn);
        Token received = (Token) MessageToken.getToken(toreturn);
        if(received instanceof VoteToken){
            this.vote_opt_port.put(((VoteToken) received)._port,((VoteToken) received)._vote);
        }
    }
}
