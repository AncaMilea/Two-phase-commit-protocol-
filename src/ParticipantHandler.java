import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
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

                // Ask user what he wants
//                dos.writeUTF("What do you want?[Date | Time]..\n"+
//                        "Type Exit to terminate connection.");

                // receive the answer from client
                toreturn= dis.readUTF();
                received = (Token) MessageToken.getToken(toreturn);

                if(received instanceof JoinToken){
                    //dos.writeUTF((((JoinToken) received)._port).toString());
                    this.port= ((JoinToken) received)._port;
                    flagJoin.set(true);
                   // dos.writeUTF("Join");
                }
                if(received instanceof DetailsToken){

                }
                if(received instanceof VoteOptionsToken){

                }
                if(received instanceof VoteToken){

                }
                if(received instanceof OutcomeToken){
                    this.s.close();
                }

//                System.out.println(received);
//                if(received.equals("Exit"))
//                {
//                    System.out.println("Client " + this.s + " sends exit...");
//                    System.out.println("Closing this connection.");
//                    this.s.close();
//                    System.out.println("Connection closed");
//                    break;
//                }

                // creating Date object

                // write on output stream based on the
                // answer from the client
//                switch (toreturn) {
//
//                    case "Date" :
//                        toreturn = fordate.format(date);
//                        dos.writeUTF(toreturn);
//                        break;
//
//                    case "Time" :
//                        toreturn = fortime.format(date);
//                        dos.writeUTF(toreturn);
//                        break;
//
//                    default:
//                        dos.writeUTF("Invalid input");
//                        break;
//                }
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
}
