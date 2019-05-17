import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.*;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;

public class Participant {
    Integer port_Coordinator;
    Integer port_Participant;
    Integer timeout;
    Integer failure_type;
    Set<Integer> participants_ports_C = ConcurrentHashMap.newKeySet();
    List<String> options= new CopyOnWriteArrayList<>();
    String my_option= null;
    String my_vote_letter;
    String final_result=null;
    List<Socket> sockets_accepting= new CopyOnWriteArrayList<>();
    List<Socket> sockets_talking= new CopyOnWriteArrayList<>();
    List<ServerSocket> serversockets_listening= new CopyOnWriteArrayList<>();
    Map<Integer,String> votes_received_with_ports= new ConcurrentHashMap<>();
    Map<String,Integer> all_votes_for_outcome= new ConcurrentHashMap<String,Integer>();
    AtomicBoolean flagOutcome= new AtomicBoolean(false);
    AtomicBoolean flagWhile= new AtomicBoolean(false);
    CyclicBarrier barrier;

    //Constructor with sockets_accepting the start information about the Participant
    public Participant(String cport, String pport, String timeout,String failure){
        this.port_Coordinator= Integer.parseInt(cport);
        this.port_Participant= Integer.parseInt(pport);
        this.timeout= Integer.parseInt(timeout);
        this.failure_type= Integer.parseInt(failure);
    }

    //help functions to retrieve the information
    public Integer getPort_coord()
    {
        return this.port_Coordinator;
    }
    public Integer getPort_part()
    {
        return this.port_Participant;
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
            //sending the port for the connection
            dos.writeUTF("JOIN "+p.getPort_part().toString());
            
            while (true)
            {
                String received = dis.readUTF();
                //decide the type of message and the next move
                p.decrypt(received);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {

                            while (p.flagWhile.get() == false) {
                                //doesn't enter until there is an Ouctome to be sent
                                if (p.flagOutcome.get() == true) {

                                    try {
                                        String out = "OUTCOME " + p.final_result;
                                        //create the string for the outcome
                                        for (Integer it : p.participants_ports_C) {
                                            out = out + " " + it;
                                        }

                                        out = out + " " + p.getPort_part();
                                        //decide if there is a failure or not
                                        if (p.getFailure_type()==2) {
                                            System.out.println("I am failing with 2");
                                            System.exit(2);
                                        }else{
                                            //send the outcome
                                            if(p.getFailure_type()==0) {
                                                dos.writeUTF(out);
                                                dos.flush();
                                            }
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    p.flagWhile.set(true);
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

        //adds all the other participants to the a list
        if(type instanceof DetailsToken){
            participants_ports_C.addAll(((DetailsToken) type)._ports);

            Iterator<Integer> it= participants_ports_C.iterator();
            while(it.hasNext()){
                System.out.println("I will get and send information to "+it.next());
            }
        }

        //saves all the options from the Coordinator, chooses a random vote and if there are more participants it starts the communication, otherwise sends its vote
        if(type instanceof VoteOptionsToken){
            this.options.addAll(((VoteOptionsToken) type)._votes);
            for(String v: this.options) {
                System.out.println("Possible votes "+v);
            }

            Random randNum = new Random();
            int aRandomPos = randNum.nextInt((this.options).size());//Returns a nonnegative random number less than the specified maximum.
            this.my_vote_letter= this.options.get(aRandomPos);
            this.my_option = "VOTE " + this.getPort_part()+" "+ this.my_vote_letter;
            System.out.println("My option is "+this.my_option);
            if(this.participants_ports_C.size()==0)
            {

                this.final_result= this.my_vote_letter;
                this.flagOutcome.set(true);
                System.out.println(this.final_result);
            }else {
                this.startListening(this.getPort_part());
                this.startTalking(this.my_option);
            }

        }
        //in case of a restart it deletes all the information received from the Coordinator
        if(type instanceof RestartToken){
            System.out.println("Restarting ");
            for(Socket s:sockets_accepting){
                s.close();
            }
            for(ServerSocket ss:serversockets_listening){
                ss.close();
            }
            this.options.clear();
            this.my_option=null;
            this.final_result=null;
            this.my_vote_letter=null;
            this.serversockets_listening.clear();
            this.sockets_accepting.clear();
            this.votes_received_with_ports.clear();
            all_votes_for_outcome.clear();
            sockets_talking.clear();
            this.flagOutcome.set(false);
            this.flagWhile.set(false);
            this.participants_ports_C.clear();
        }

    }
    public void startListening(Integer port_curr) throws IOException {
        new Thread(new Runnable() {
            @Override
            public void run() {
                ServerSocket ss;
                barrier = new CyclicBarrier(participants_ports_C.size()+1);
                try {
                    ss = new ServerSocket(port_curr);
                    serversockets_listening.add(ss);

                //does this until the number of sockets that have a connection is equal with the number of other participants
                while (sockets_accepting.size() < participants_ports_C.size()) {
                    // socket object to receive incoming client requests
                    Socket s;
                    try {
                        s = ss.accept();
                        sockets_accepting.add(s);

                        System.out.println("A new participant is connected : " + s);

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                String received = null;
                                    try {
                                        // obtaining input
                                        DataInputStream dis = new DataInputStream(s.getInputStream());

                                        while (true)
                                        {   //doesn't process information until there is something to process
                                            if (dis.available() > 0) {
                                                received = dis.readUTF();
                                                //saves all the votes and ports in votes_received_with_ports
                                                saveVote(received);
                                                barrier.await();
                                            }
                                        }

                                    } catch (EOFException e) {

                                    } catch (IOException e) {
//                                        e.printStackTrace();
                                } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    } catch (BrokenBarrierException e) {
                                        e.printStackTrace();
                                    }
                            }
                        }).start();
                    }catch(SocketException e) {
                        break;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }
                barrier.await();
                //proceeds to deciding the outcome only if it gathered all the votes available
                start_choosing(votes_received_with_ports);


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (BrokenBarrierException e) {
                    e.printStackTrace();
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
                    List<Integer> temp= new ArrayList<>();
                    Integer porting;
                    // getting localhost ip
                    InetAddress ip = InetAddress.getByName("localhost");

                    //starts connection until they are equal to the number of participants available
                    while(sockets_talking.size()!=participants_ports_C.size()) {
                        Iterator<Integer> it = participants_ports_C.iterator();
                        while (it.hasNext()) {
                            Socket s = null;
                            porting= it.next();
                            //doesn't try to access same port twice
                            if(temp.contains(porting)){
                                break;
                            }else {

                                try {
                                    s = new Socket(ip, porting);
                                } catch (SocketException e) {
                                    break;
                                }
                                temp.add(porting);
                                sockets_talking.add(s);

                                DataOutputStream dos = new DataOutputStream(s.getOutputStream());
                                //sends the information to the other participants
                                dos.writeUTF(my_opt);
                                dos.flush();
                            }

                        }
                    }
                    System.out.println("There are participants connected "+temp.size());
                    temp.clear();

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();

    }
    //saves the votes from all the other participants
    public void saveVote(String toreturn){
        Token received = (Token) MessageToken.getToken(toreturn);
        if(received instanceof VoteToken){
            this.votes_received_with_ports.put(((VoteToken) received)._port,((VoteToken) received)._vote);
        }
    }
    public void start_choosing(Map<Integer, String> votes_received){
        int max=0;
        String result=null;
        //gathers all the possible votes, including the vote of this participant, stating also their frequency
        all_votes_for_outcome.put(this.my_vote_letter, 1);
        for(String it:votes_received.values()){
            for(String s: this.options){
                if(it.equals(s)){
                    if(all_votes_for_outcome.containsKey(it)){
                        all_votes_for_outcome.put(it, all_votes_for_outcome.get(it)+1);
                    }else{
                        all_votes_for_outcome.put(it, 1);
                    }
                }
            }
        }
        System.out.println("Votes and frequency ");
        //decides which vote has more frequency, in case of a draw, it sets the message to TIE
        for(String i:this.all_votes_for_outcome.keySet()){
            System.out.println(i+" "+this.all_votes_for_outcome.get(i));
            //if the frequency is bigger than the current maximum set it as the new max and save the vote
            if(max<this.all_votes_for_outcome.get(i))
            {
                max= this.all_votes_for_outcome.get(i);
                result= i;
            }else{
                //in case of a draw set the message to TIE
                if(max == this.all_votes_for_outcome.get(i))
                {
                    result=  "TIE";
                }
            }
        }
        //sets the final result to either a Vote or a Tie
        this.final_result=result;
        System.out.println("My final outcome is "+this.final_result);
        this.flagOutcome.set(true);
    }
}
