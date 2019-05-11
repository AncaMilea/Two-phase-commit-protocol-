import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


public class ParticipantHandler extends Thread {

    final DataInputStream dis;
    final DataOutputStream dos;
    final Socket s;
    public int port;
    public String final_v;
    public AtomicBoolean flagJoin = new AtomicBoolean(false);
    public AtomicBoolean flagVote = new AtomicBoolean(false);


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
            //    System.out.println(toreturn);
                received = (Token) MessageToken.getToken(toreturn);


                if(received instanceof JoinToken){
                    this.port= ((JoinToken) received)._port;
                    flagJoin.set(true);
                    System.out.println("Participant connected "+this.port);
                }
                if(received instanceof OutcomeToken){
                    this.final_v = ((OutcomeToken) received)._outcome;
                    flagVote.set(true);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
            dos.writeUTF(send);
        }
    }
    }

