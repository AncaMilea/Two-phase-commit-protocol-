import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.List;

public class Participant {
    Integer port_coord;
    Integer port_part;
    Integer timeout;
    Integer failure_type;
    Set<Integer> other_part= new HashSet<>();
    List<String> options= new ArrayList<>();
    String my_option= "VOTE ";
    List<ParticipantHandler> handlers = new ArrayList<>();
    HashMap<Integer,String> vote_opt_port= new HashMap<>();
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

            while (true)
            {

                dos.writeUTF("JOIN "+p.getPort_part().toString());

                String received = dis.readUTF();
                p.decrypt(received);
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
//                        String received= dis.readUTF();
//                        System.out.println(received);
                        ParticipantHandler t = new ParticipantHandler(s, dis, dos);
                        handlers.add(t);
                        System.out.println("size "+handlers.size());

                        // Invoking the start() method
                        new Thread(t).start();

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
        new Thread(new Runnable() {
            @Override
            public void run() {

                int i = 0;
            while(true) {
                for (i = 0; i < handlers.size(); i++) {
                    if (handlers.get(i).flagVote.get()) {
                       vote_opt_port.put(handlers.get(i).port_v,handlers.get(i).vote);
                        //System.out.println(cord.handlers.get(i).port);
                    }

                }
            }
            }

        }).start();



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


//                            String received = dis.readUTF();
//                            System.out.println(received);

                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
