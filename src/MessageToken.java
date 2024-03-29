import java.util.*;

/**
 * A scanner and parser for requests.
 */

class MessageToken{
    MessageToken() { ; }

    /**
     * Parses requests and saves the information in the given token
     */
    static Token getToken(String req) {
        StringTokenizer sTokenizer = new StringTokenizer(req);
        if (!(sTokenizer.hasMoreTokens()))
            return null;
        String firstToken = sTokenizer.nextToken();
        if (firstToken.equals("JOIN")) {
            if (sTokenizer.hasMoreTokens())
                return new JoinToken(req, sTokenizer.nextToken().trim());
            else
                return null;
        }
        if (firstToken.equals("DETAILS")) {
            Set<Integer> ports = new HashSet<>();
            while (sTokenizer.hasMoreTokens())
                ports.add(Integer.parseInt(sTokenizer.nextToken().trim()));
            return new DetailsToken(req, ports);
        }
        if (firstToken.equals("VOTE_OPTIONS")) {
            List<String> votes = new ArrayList<>();
            while (sTokenizer.hasMoreTokens())
                votes.add(sTokenizer.nextToken());
            return new VoteOptionsToken(req, votes);
        }
        if (firstToken.equals("OUTCOME")) {
            String outcome = sTokenizer.nextToken();
            List<Integer> ports = new ArrayList<>();
            Integer now;
            while (sTokenizer.hasMoreTokens()) {
                ports.add(Integer.parseInt(sTokenizer.nextToken().trim()));
            }
            return new OutcomeToken(req, outcome, ports);
        }
        if (firstToken.equals("VOTE")) {
            Integer port = null;
            String vote = null;
            if (sTokenizer.hasMoreTokens()) {
                port = Integer.parseInt(sTokenizer.nextToken().trim());
            }
            if(sTokenizer.hasMoreTokens()){
                vote = sTokenizer.nextToken();
            }

            return new VoteToken(req, port, vote);
        }
        if(firstToken.equals("RESTART")){
            return new RestartToken(req);
        }else
            return  null;// Ignore request..
    }
}


abstract class Token {
    String _req;
}

/**
 * Syntax: JOIN port
 */
class JoinToken extends Token {
    Integer _port;
    JoinToken(String req, String port) {
        this._req = req;
        this._port = Integer.parseInt(port);
    }
}

/**
 * Syntax: Details <[ports]>
 */
class DetailsToken extends Token {
    Set<Integer> _ports;

    DetailsToken(String req, Set<Integer> porting) {
        this._req = req;
        this._ports = new HashSet<Integer>(porting);
    }
}

/**
 * Syntax: VoteOptions <[votes]>
 */
class VoteOptionsToken extends Token {
    List<String> _votes;

    VoteOptionsToken(String req, List<String> voting) {
        this._req = req;
        this._votes = new ArrayList<>(voting);

    }
}
/**
 * Syntax: Outcome <vote> <[ports]>
 */
class OutcomeToken extends Token {
    List<Integer> _ports;
    String _outcome;

    OutcomeToken(String req, String outcome, List<Integer> porting) {
        this._req = req;
        this._outcome=outcome;
        this._ports = new ArrayList<>(porting);
    }
}
/**
 * Syntax: Vote <vote> <port>
 */
class VoteToken extends Token {
    String _vote;
    Integer _port;
    VoteToken(String req, Integer port, String vote) {
        this._req = req;
        this._vote=vote;
        this._port=port;
    }
}
/**
 * Syntax: Restart
 */
class RestartToken extends Token {
    RestartToken(String req) { this._req = req; }
}

