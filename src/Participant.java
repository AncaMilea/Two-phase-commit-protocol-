import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

public class Participant {
    Integer port_coord;
    Integer port_part;
    Integer timeout;
    Integer failure_type;
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
            Scanner scn = new Scanner(System.in);

            // getting localhost ip
            InetAddress ip = InetAddress.getByName("localhost");

            // establish the connection with server port 5056
            Socket s = new Socket(ip, p.getPort_coord());

            // obtaining input and out streams
            DataInputStream dis = new DataInputStream(s.getInputStream());
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());

            // the following loop performs the exchange of
            // information between client and client handler
            while (true)
            {
                //System.out.println(dis.readUTF());
//                String tosend = scn.nextLine();
                dos.writeUTF("JOIN "+p.getPort_part().toString());

                // If client sends exit,close this connection
                // and then break from the while loop
//                if(tosend.equals("Exit"))
//                {
//                    System.out.println("Closing this connection : " + s);
//                    s.close();
//                    System.out.println("Connection closed");
//                    break;
//                }

                // printing date or time as requested by client
                String received = dis.readUTF();
                System.out.println(received);
            }

            // closing resources
            //scn.close();
//            dis.close();
//            dos.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
