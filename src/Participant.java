import java.awt.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
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

                String received = dis.readUTF();
                p.decrypt(received);
//                String toreturn= dis.readUTF();
//                Token received1 = (Token) MessageToken.getToken(toreturn);
//                if(received1 instanceof DetailsToken){
//
//                }
//                if(received1 instanceof VoteOptionsToken){
//
//                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
    }
    public void decrypt(String received)
    {
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

            for(String s: this.options){
                System.out.println("My votes can be "+s);
            }
            Random randNum = new Random();
            int aRandomPos = randNum.nextInt((this.options).size());//Returns a nonnegative random number less than the specified maximum (firstNames.Count).

            String currName = this.options.get(aRandomPos);
            System.out.println("My option is "+currName);

        }
    }
}
