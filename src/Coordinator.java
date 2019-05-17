import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;



public class Coordinator {
        Integer port;
        Integer number_of_part;
        List<String> options;
        Set<Integer> all_part= new HashSet<>();
        List<ParticipantHandler> handlers = new ArrayList<>();
        Map<Integer,String> outcomes = new ConcurrentHashMap();
        Set<String> outcomes_not_replicas = new HashSet<>();
    public AtomicBoolean flagStartOptions = new AtomicBoolean(false);
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
                break;
                //e.printStackTrace();
            }

        }


        while(cord.number_of_part>cord.all_part.size()) {
             for (ParticipantHandler j : cord.handlers) {
                 if (j.flagJoin.get()) {
                    cord.setAll_part(j.port);
//                 System.out.println("Ports "+j.port);
                 }
             }
        }

        cord.sendDetails(cord);
        cord.sendVoteOpt(cord,cord.getOptions());

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    while (!(cord.outcomes.size() == cord.handlers.size())) {

                        for (int i = 0; i < cord.handlers.size(); i++) {
                            if(cord.handlers.get(i).flagOut.get()==false){
                               if (cord.handlers.get(i).flagVote.get()) {
                                   if(!cord.outcomes.containsValue(cord.handlers.get(i).port)) {
                                       cord.outcomes.put(cord.handlers.get(i).port,cord.handlers.get(i).final_v);
                                       //System.out.println("votes " + cord.handlers.get(i).final_v + " " + cord.handlers.get(i).port);
                                       //cord.handlers.remove(i);
                                   }
                                }
                            }else{
                                System.out.println("I am here");
                                cord.all_part.remove(cord.handlers.get(i).port);
                                for(Integer c: cord.all_part)
                                {
                                    System.out.println(c);
                                }
                                cord.handlers.remove(i);
                                System.out.println("Size "+cord.handlers.size()+ " --"+cord.outcomes.size());
                            }
                        }

                    }
                   // System.out.println("I am out "+cord.outcomes.size());
                    if (cord.outcomes.size() == cord.handlers.size()) {
                        for (String st : cord.outcomes.values()) {
                            cord.outcomes_not_replicas.add(st);
                        }
                        if (cord.outcomes_not_replicas.size() == 1) {
                            String result;
                            Iterator<String> it = cord.outcomes_not_replicas.iterator();
                            result = it.next();
                            System.out.println("Final " + result);

                            if (result.contains("Tie")) {
                                if (cord.options.size() > 1) {
                                    cord.options.remove(cord.options.size() - 1);
                                }
                                    //sets to false the flags so that it waits until the new value comes in
                                for (int i = 0; i < cord.handlers.size(); i++) {
                                    if (cord.handlers.get(i).flagVote.get()) {
                                        cord.handlers.get(i).flagVote.set(false);
                                    }

                                }
                                    //sents to each participant the Restart instruction so that it cleans all
                                for (int i = 0; i < cord.handlers.size(); i++) {
                                   try {
                                       cord.handlers.get(i).dos.writeUTF("RESTART");
                                       cord.handlers.get(i).dos.flush();
                                       } catch (IOException e) {
                                            e.printStackTrace();
                                       }
                                }
                                //resend the available participants
                                if(cord.handlers.size()>1) {
                                    cord.sendDetails(cord);
                                }

                                //sends the new options to the participants
                                cord.sendVoteOpt(cord,cord.getOptions());
                                    //erases all the elements
                                cord.outcomes_not_replicas.clear();
                                cord.outcomes.clear();
                            } else {
                                System.out.println("The final outcome is "+result);
                                break;
                            }

                        }
                    }
                }
                System.out.println("I am out of thread");

            }
        }).start();
    }
    public void sendVoteOpt(Coordinator cord,List<String> opt){

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
    public void sendDetails(Coordinator cord){

        String send= "DETAILS";
        for (ParticipantHandler p:cord.handlers) {
            if (p.flagJoin.get()) {
                try {

                        p.writeToParticipantDetails(send, cord.all_part);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        }
    }



}
