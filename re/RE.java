package re;

import fa.State;
import fa.nfa.NFA;
import fa.nfa.NFAState;

public class RE implements REInterface {

        private String regEx;
        private NFA nfa;
        private int counter;
        
        public RE(String regEx)
        {
                this.regEx = regEx;
                nfa = new NFA();
                counter = 0;
        }
        public NFA getNFA()
        {
        	nfa = parseRegex();
                return nfa;
        }
        private NFA parseRegex()
        {
                NFA reg = null;
                NFA term = parseTerm();
                if (more() && peek() == '|') {
                        eat('|');
                        NFA regEx = parseRegex();
                        reg = union(regEx, term);
                }
        	return reg;	
        }
        private NFA parseTerm()
        {
                while(peek() != ')' && peek() != '|' && more()) {
                        NFA n = parseFactor();
                }
                return null;
        }
        private NFA parseFactor()
        {
                NFA n = parseBase();
                while(more() && peek() == '*') {
                        eat('*');
                        
                }
                return null;
        }
        private NFA parseBase()
        {
                if (peek() == '(') {
                        eat('(');
                        parseRegex();
                        eat(')');
                        return null;
                } else {
                        nfa.addState(String.valueOf(counter));
                }
                return null;
        }
        private char peek()
        {
                return regEx.charAt(0);
        }
        private void eat(char c)
        {
                if (peek() == c) {
                        this.regEx = this.regEx.substring(1);
                } else {
                        System.err.println("Expected: " + c + "; got: " + peek());
                        System.exit(-1);
                }
                return;
        }
        private boolean more()
        {
                return regEx.length() > 0;
        }
        /**
         * Generates the new NFA formed by concatenating n onto the end of our current
         * NFA
         * 
         * @param n the nfa to concatenate onto the one stored in our object
         * @return the new NFA formed by concatenating n onto the end of the stored NFA
         */
        private void concatenate(NFA n)
        {
                for (State s: nfa.getFinalStates()) {
                        //Set final states of nfa to not final
                        //Add e transitions from old final states to start state of n
                        NFAState nS = ((NFAState)s);
                        nS.setNonFinal();
                        nS.addTransition('e', (NFAState)n.getStartState());    
                }
        }
        
        /**
         * Constructs the Kleene star operation on an NFA
         * 
         * @param n the NFA which will be repeated
         */
        private NFA repeat(NFA n)
        {
                //Record name of old start
                State oldStart = n.getStartState();
                
                //Create new start and final states and add to n
                String newStartName = String.valueOf(counter);
                n.addStartState(newStartName);
                counter++;
                String newFinalName = String.valueOf(counter);
                NFAState newFinal = new NFAState(newFinalName);
                n.addState(newFinalName);
                counter++;
                
                //Create transitions and change final states to not final
                n.addTransition(newStartName, 'e', oldStart.getName());
                for (State s: n.getFinalStates()) {
                        ((NFAState) s).setNonFinal();
                        ((NFAState) s).addTransition('e', newFinal);
                }
                n.addFinalState(newFinalName);
                return n;
        }
        private NFA union(NFA n1, NFA n2)
        {
                NFA n = new NFA();
                String newStartName = String.valueOf(counter);
                counter++;
                n.addStartState(newStartName);
                
                ((NFAState)n.getStartState()).addTransition('e', (NFAState)n1.getStartState());
                ((NFAState)n.getStartState()).addTransition('e', (NFAState)n2.getStartState());
                
                n1.addNFAStates(n1.getStates());
                n1.addNFAStates(n2.getStates());
                
                String newFinalName = String.valueOf(counter);
                n.addFinalState(newFinalName);
                for (State s: n.getFinalStates()) {
                        if (s.getName().equals(newFinalName)) {
                                continue;
                        }
                        ((NFAState)s).setNonFinal();
                        n.addTransition(s.toString(), 'e', newFinalName);
                }
                return n;
                
        }

}
