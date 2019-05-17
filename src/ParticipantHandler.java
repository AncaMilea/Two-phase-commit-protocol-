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
    public AtomicBoolean flagOut = new AtomicBoolean(false);


    // Constructor
    public ParticipantHandler(Socket s, DataInputStream dis, DataOutputStream dos)
    {
        this.s = s;
        this.dis = dis;
        this.dos = dos;
    }

    //decrypt the messages
    @Override
    public void run()
    {
        Token received;
        String toreturn;
        while (true)
        {
            if(this.flagOut.get()==false) {
                try {

                    // receive the answer from client
                    toreturn = dis.readUTF();
                    //turn it into a Token
                    received = (Token) MessageToken.getToken(toreturn);

                    //participants join and therefore their flag will pe set to true so that the Coordinator knows it can use them
                    if (received instanceof JoinToken) {
                        this.port = ((JoinToken) received)._port;
                        flagJoin.set(true);
                        System.out.println("Participant connected " + this.port);
                    }
                    //just gets the vote for the outcome and sets their flag to true so that they be used
                    if (received instanceof OutcomeToken) {
                        this.final_v = ((OutcomeToken) received)._outcome;
                        this.flagVote.set(true);
                    }

                } catch (IOException e) {
                    //enters here only in the case of failing with two, so the Coordinator knows this participant is down
                    this.flagOut.set(true);
                }
            }
        }

    }

    //sends the Details with the other ports to each participant
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
            System.out.println(send+" TO "+this.port);
            dos.writeUTF(send);
            dos.flush();
        }
    }
    //sends the options for the vote
    public void writeToParticipantOptions(String send, List<String> opt) throws IOException {
        if (send.equals("VOTE_OPTIONS")) {
            String now;
            Iterator<String> it = opt.iterator();
            while (it.hasNext()) {
                now = it.next();
                send = send + " " + now;
            }

            dos.writeUTF(send);
            dos.flush();
        }
    }
    }

