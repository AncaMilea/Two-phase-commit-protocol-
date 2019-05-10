import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
/**
 * A scanner and parser for requests.
 */

class MessageToken{
    MessageToken() { ; }

    /**
     * Parses requests.
     */
    static Token getToken(String req) {
        StringTokenizer sTokenizer = new StringTokenizer(req);
        if (!(sTokenizer.hasMoreTokens()))
            return null;
        String firstToken = sTokenizer.nextToken();
        if (firstToken.equals("JOIN")) {
            if (sTokenizer.hasMoreTokens())
                return new JoinToken(req, sTokenizer.nextToken());
            else
                return null;
        }
        if (firstToken.equals("DETAILS")) {
            List<Integer> ports = new ArrayList<>();
            while (sTokenizer.hasMoreTokens())
                ports.add(Integer.parseInt(sTokenizer.nextToken()));
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
            while (sTokenizer.hasMoreTokens())
                ports.add(Integer.parseInt(sTokenizer.nextToken()));
            return new OutcomeToken(req, outcome, ports);
        }
        if (firstToken.equals("VOTE")) {
            Integer port = null;
            String vote = null;
            if (sTokenizer.hasMoreTokens()) {
                port = Integer.parseInt(sTokenizer.nextToken());
            }
            if(sTokenizer.hasMoreTokens()){
                vote = sTokenizer.nextToken();
            }

            return new VoteToken(req, port, vote);
        }else
            return  null;// Ignore request..
    }
}

/**
 * The Token Prototype.
 */
abstract class Token {
    String _req;
}

/**
 * Syntax: JOIN &lt;name&gt;
 */
class JoinToken extends Token {
    Integer _port;
    JoinToken(String req, String port) {
        this._req = req;
        this._port = Integer.parseInt(port);
    }
}

/**
 * Syntax: YELL &lt;msg&gt;
 */
class DetailsToken extends Token {
    List<Integer> _ports;

    DetailsToken(String req, List<Integer> porting) {
        this._req = req;
        this._ports = new ArrayList<>(porting);
    }
}

/**
 * Syntax: TELL &lt;rcpt&gt; &lt;msg&gt;
 */
class VoteOptionsToken extends Token {
    List<String> _votes;

    VoteOptionsToken(String req, List<String> voting) {
        this._req = req;
        this._votes = new ArrayList<>(voting);

    }
}

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
 * Syntax: EXIT
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
