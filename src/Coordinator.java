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
        List<String> outcomes = new ArrayList<>();
        Set<String> outcomes_not_replicas = new HashSet<>();
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
                    for (i = 0; i < cord.handlers.size(); i++) {
                        if (cord.handlers.get(i).flagJoin.get()) {
                            cord.setAll_part(cord.handlers.get(i).port);
                            //System.out.println(cord.handlers.get(i).port);
                        }

                    }


                String send= "DETAILS";
                for (i = 0; i < cord.handlers.size(); i++) {
                    if (cord.handlers.get(i).flagJoin.get()) {
                        try {
                            cord.handlers.get(i).writeToParticipantDetails(send, cord.all_part);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                }

            }
        }).start();

        cord.sendVoteOpt(cord,cord.getOptions());

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(!(cord.outcomes.size() == cord.handlers.size())) {
                    for (int i = 0; i < cord.handlers.size(); i++) {
                        if (cord.handlers.get(i).flagVote.get()) {
                            cord.outcomes.add(cord.handlers.get(i).final_v);
                        }

                    }
                    if(cord.outcomes.size()==cord.handlers.size())
                    {
                        for(String st: cord.outcomes){
                            cord.outcomes_not_replicas.add(st);
                        }
                        if(cord.outcomes_not_replicas.size()==1){
                            String result;
                            Iterator<String> it = cord.outcomes_not_replicas.iterator();
                            while(it.hasNext()){
                                result=it.next();
                                System.out.println("Final " +result);
                                if(result.equals("Tie")) {
                                    for (int i = 0; i < cord.handlers.size(); i++) {
                                        try {
                                            cord.handlers.get(i).dos.writeUTF("RESTART");
                                            cord.handlers.get(i).dos.flush();
                                        } catch (IOException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                    cord.outcomes_not_replicas.clear();
                                    cord.outcomes.clear();
                                    if(cord.options.size()>1) {
                                        cord.options.remove(cord.options.size() - 1);
                                    }
                                    cord.sendVoteOpt(cord,cord.getOptions());
                                }
                            }


                    }else{
                           if(cord.outcomes_not_replicas.size()==0)
                           {
                               System.out.println("Si'a bagat ceva in rezultat");
                           }else{
                               System.out.println("Nici nu ar trebui sa fie asa ceva uman posibil");
                           }
                        }
                    }
             }

            }
        }).start();
    }
    public void sendVoteOpt(Coordinator cord,List<String> opt){
        new Thread(new Runnable() {
            @Override
            public void run() {
                String send= "VOTE_OPTIONS";
                for (int i = 0; i < cord.handlers.size(); i++) {
                    if (cord.handlers.get(i).flagJoin.get()) {
                        try {
                            cord.handlers.get(i).writeToParticipantOptions(send, opt);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }

                }

            }
        }).start();
    }



}
