import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.stream.Collectors;

public class Coordinator {
        Integer port;
        Integer number_of_part;
        List<String> options;
        Set<Integer> all_part= new HashSet<>();
        List<ParticipantHandler> handlers = new ArrayList<>();
    public Coordinator(String port, String parts, List<String> options)
    {
        this.port = Integer.parseInt(port);
        this.number_of_part= Integer.parseInt(parts);
        this.options= new ArrayList<>(options);
    }

    public Integer getPort()
    {
        return this.port;
    }
    public Integer getNumber_of_part()
    {
        return this.number_of_part;
    }
    public List<String> getOptions()
    {
        return this.options;
    }

    public void setAll_part(Integer port){
        this.all_part.add(port);
    }

    public static void main(String[] args) throws IOException {
        // server is listening on port 5056
        String port_c = args[0];
        String parts_nr = args[1];
        List<String> str = new ArrayList<String>(Arrays.stream(args).skip(2).collect(Collectors.toList()));
        Coordinator cord = new Coordinator(port_c, parts_nr, str);
        int i = 0;

        ServerSocket ss = new ServerSocket(cord.getPort());

        // running infinite loop for getting
        // client request
        while (i < cord.getNumber_of_part()) {
            Socket s = null;

            try {
                // socket object to receive incoming client requests
                s = ss.accept();

//                    cord.setAll_part(s.getPort());
                System.out.println("A new client is connected : " + s);

                // obtaining input and out streams
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                System.out.println("Assigning new thread for this client");

                // create a new thread object
                ParticipantHandler t = new ParticipantHandler(s, dis, dos);
                cord.handlers.add(t);


                // Invoking the start() method
                new Thread(t).start();
                i++;

            } catch (Exception e) {
                s.close();
                e.printStackTrace();
            }

        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;

                while (!(cord.all_part.size() == cord.handlers.size())) {
                    for (i = 0; i < cord.handlers.size(); i++) {
                        if (cord.handlers.get(i).flagJoin.get()) {
                            cord.setAll_part(cord.handlers.get(i).port);
                            //System.out.println(cord.handlers.get(i).port);
                        }

                    }

                }
                Iterator<Integer> it= cord.all_part.iterator();
                while(it.hasNext()) {
                    System.out.println(it.next());
                }
            }
        }).start();

    }



}
