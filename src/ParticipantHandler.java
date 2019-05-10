import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;


public class ParticipantHandler extends Thread {

    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    public int port;
    public AtomicBoolean flagJoin = new AtomicBoolean(false);


    // Constructor
    public ParticipantHandler(Socket s, DataInputStream dis, DataOutputStream dos)
    {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }

    @Override
    public void run()
    {
        Token received;
        String toreturn;
        while (true)
        {
            try {

                // receive the answer from client
                toreturn= dis.readUTF();
                received = (Token) MessageToken.getToken(toreturn);


                if(received instanceof JoinToken){
                    //dos.writeUTF((((JoinToken) received)._port).toString());
                    this.port= ((JoinToken) received)._port;
                    flagJoin.set(true);
                    System.out.println("Participant connected "+this.port);
                   // dos.writeUTF("Join");
                }
                if(received instanceof VoteToken){

                }
                if(received instanceof OutcomeToken){
                    this.s.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

//        try
//        {
//            // closing resources
//            this.dis.close();
//            this.dos.close();
//
//        }catch(IOException e){
//            e.printStackTrace();
//}
    }
    public void writeToParticipantDetails(String send, Set<Integer> all) throws IOException {

        if (send.equals("DETAILS")) {
            int now;
            Iterator<Integer> it = all.iterator();
            while (it.hasNext()) {
                now = it.next();
                if (now != this.port) {
                    send = send + " " + Integer.toString(now);
                }
            }

            dos.writeUTF(send);
        }
    }
    public void writeToParticipantOptions(String send, List<String> opt) throws IOException {
        if (send.equals("VOTE_OPTIONS")) {
            String now;
            Iterator<String> it = opt.iterator();
            while (it.hasNext()) {
                now = it.next();
                send = send + " " + now;
            }
            System.out.println(send);
            dos.writeUTF(send);
        }
    }
    public void writeParticipantToParticipant(){

    }
    }

