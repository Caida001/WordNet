import edu.princeton.cs.algs4.*;

import java.util.HashMap;
import java.util.Map;

public class WordNet {
    private Map<Integer, String> id2SynsetDefinition;

    private Map<String, Bag<Integer>> synset2id;
    private SAP sap;

    public WordNet(String synsets, String hypernyms) {
        id2SynsetDefinition = new HashMap<Integer, String>();
        synset2id = new HashMap<String, Bag<Integer>>();
        createMaps(synsets);
        createSAP(hypernyms);
    }

    public Iterable<String> nouns() {
        return synset2id.keySet();
    }

    public boolean isNoun(String word) {
        return synset2id.containsKey(word);
    }

    public int distance(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB)) throw new java.lang.IllegalArgumentException("No such argument");
        return sap.length(synset2id.get(nounA), synset2id.get(nounB));
    }

    public String sap(String nounA, String nounB) {
        if (!isNoun(nounA) || !isNoun(nounB)) throw new java.lang.IllegalArgumentException("No arg in the wordnet");
        int ancestorId = sap.ancestor(synset2id.get(nounA), synset2id.get(nounB));
        String valueFields[] = id2SynsetDefinition.get(ancestorId).split(",");
        return valueFields[0];
    }

    private void createMaps(String synsets) {
        In in = new In(synsets);
        while (in.hasNextLine()) {
            String curString = in.readLine();
            String[] fields = curString.split(",");
            for (int i = 0; i < fields.length; i++) {
                fields[i] = fields[i].trim();
            }

            int id = Integer.parseInt(fields[0]);
            String synsetDefinition = fields[1] + "," + fields[2];
            id2SynsetDefinition.put(id, synsetDefinition);

            String synonyms[] = fields[1].split(" ");
            for (int i = 0; i < synonyms.length; i++) {
                synonyms[i] = synonyms[i].trim();
                Bag<Integer> bag = synset2id.get(synonyms[i]);
                if (bag == null) {
                    Bag<Integer> newBag = new Bag<Integer>();
                    newBag.add(id);
                    synset2id.put(synonyms[i], newBag);
                } else {
                    bag.add(id);
                }
            }
        }
    }

    private void createSAP(String hypernyms) {
        In in = new In(hypernyms);
        Digraph g = new Digraph(id2SynsetDefinition.size());
        while (in.hasNextLine()) {
            String curString = in.readLine();
            String[] fields = curString.split(",");
            for (int i = 0; i < fields.length; i++) {
                fields[i] = fields[i].trim();
            }
            for (int i = 1; i < fields.length; i++) {
                g.addEdge(Integer.parseInt(fields[0]), Integer.parseInt(fields[i]));
            }
        }

        if (!isRootedDAG(g)) {
            throw new java.lang.IllegalArgumentException("not rooted");
        }
        sap = new SAP(g);
    }

    private boolean isRootedDAG(Digraph g) {
        DirectedCycle diCycle = new DirectedCycle(g);
        if (diCycle.hasCycle()) return false;

        int roots = 0;
        for (int vertex = 0; vertex < g.V(); vertex++) {
            if (!g.adj(vertex).iterator().hasNext()) roots++;
        }
        if (roots != 1) return false;
        return true;
    }

    public static void main(String[] args) {
        WordNet wn = new WordNet(args[0], args[1]);
        for (String s : wn.nouns()) {
            StdOut.println(s);
        }

        while (!StdIn.isEmpty()) {
            String nounA = StdIn.readLine();
            String nounB = StdIn.readLine();
            int distance = wn.distance(nounA, nounB);
            String ancestor = wn.sap(nounA, nounB);
            StdOut.println("length = " + distance);
            StdOut.println("ancestor = " + ancestor);
        }
    }
}

