package re;

import fa.State;
import fa.nfa.NFA;
import fa.nfa.NFAState;

/**
 * Parses a regular expression string and converts it into
 * an NFA.
 * @author Benjamin Hetz
 *
 */
public class RE implements REInterface {

        private String regEx;
        private NFA nfa;
        private int counter;
        
        public RE(String regEx)
        {
                this.regEx = regEx;
                counter = 0;
        }
        /**
         * Parses the regEx string into an NFA
         */
        public NFA getNFA()
        {
        	nfa = parseRegex();
                return nfa;
        }
        /**
         * Parses a regEx consisting of a term, or a term
         * followed by a '|' and another regEx
         * @return parsed regEx
         */
        private NFA parseRegex()
        {
                NFA reg = null;
                NFA term = parseTerm();
                if (more() && peek() == '|') {
                        eat('|');
                        reg = parseRegex();
                        term = union(reg, term);
                        return term;
                }
        	return term;	
        }
        /**
         * Parses a term consisting of a factor
         * or no factors
         * 
         * Since the algorithm is recursive, I had to check if
         * the NFA was already there, in which case I should concatenate
         * 
         * @return The term parsed into an NFA
         */
        private NFA parseTerm()
        {
                NFA n = null;
                while(more() && peek() != ')' && peek() != '|') {
                        if (n == null) {
                                n = parseFactor();
                        } else {
                                n = concatenate(n, parseFactor());
                        }
                }
                return n;
        }
        /**
         * Parses a factor consisting of a base
         * optionally followed by a Kleene Star
         * 
         * @return The factor parsed into an NFA
         */
        private NFA parseFactor()
        {
                NFA n = parseBase();
                while(more() && peek() == '*') {
                        eat('*');
                        n = repeat(n);
                }
                return n;
        }
        /**
         * Parses a base consisting of a char or a
         * '(' followed by a full regEx followed by a ')'
         * 
         * @return the parsed base
         */
        private NFA parseBase()
        {
                if (peek() == '(') {
                        eat('(');
                        NFA n = parseRegex();
                        eat(')');
                        return n;
                } else {
                        //Make a two state, 1 transition NFA for this char
                        NFA base = new NFA();
                        String startStateName = String.valueOf(counter);
                        base.addStartState(startStateName);
                        counter++;
                        String finalStateName = String.valueOf(counter);
                        base.addFinalState(finalStateName);
                        counter++;
                        base.addTransition(startStateName, next(), finalStateName);
                        return base;
                }
        }
        /**
         * Consumes the next char from input
         * 
         * @return the next char in the input string
         */
        private char next()
        {
                char c = regEx.charAt(0);
                regEx = regEx.substring(1);
                return c;
        }
        /**
         * Checks out the next char from input
         * this method does not consume the char
         * 
         * @return the next char in the input string
         */
        private char peek()
        {
                return regEx.charAt(0);
        }
        /**
         * Checks if the next char in input matches
         * c, and consumes it if it does.
         * 
         * Error out if we find something we don't expect
         * 
         * @param c the char to check
         */
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
        /**
         * Returns whether the input has more chars
         * 
         * @return boolean value of whether input has more chars or not
         */
        private boolean more()
        {
                return regEx.length() > 0;
        }
        /**
         * Generates the new NFA formed by concatenating n2 onto the end of n1
         * 
         * @param n2 the nfa to concatenate onto n1
         * @param n1 the nfa to be concatenated on to
         * 
         * @return the new NFA formed by concatenating n2 onto the end of n1
         */
        private NFA concatenate(NFA n1, NFA n2)
        {
                for (State s: n1.getFinalStates()) {
                        //Set final states of NFA to not final
                        //Add e transitions from old final states to start state of n
                        NFAState nS = ((NFAState)s);
                        nS.setNonFinal();
                        nS.addTransition('e', (NFAState)n2.getStartState());    
                }
                //Add alphabet and states from n2 into n1
                n1.addNFAStates(n2.getStates());
                n1.addAbc(n2.getABC());
                return n1;
        }
        
        /**
         * Constructs the Kleene star operation on an NFA
         * 
         * @param n the NFA which will be repeated
         * @return the NFA formed by adding the Kleene Star operation
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
                n.addState(newFinalName);
                counter++;
                
                //Create transitions and change final states to not final
                n.addTransition(newStartName, 'e', oldStart.getName());
                for (State s: n.getFinalStates()) {
                        ((NFAState) s).setNonFinal();
                        n.addTransition(s.getName(), 'e', newFinalName);
                        n.addTransition(s.getName(), 'e', oldStart.getName());
                }
                n.addFinalState(newFinalName);
                n.addTransition(newStartName, 'e', newFinalName);
                return n;
        }
        /**
         * Performs the union operation on two NFAs and returns the result
         * 
         * @param n1 the top half of the union
         * @param n2 the bottom half of the union
         * @return the union of n1 and n2 as an NFA
         */
        private NFA union(NFA n1, NFA n2)
        {
                NFA n = new NFA();
                String newStartName = String.valueOf(counter);
                counter++;
                n.addStartState(newStartName);
                
                //Add transitions from old starts to the new one
                ((NFAState)n.getStartState()).addTransition('e', (NFAState)n1.getStartState());
                ((NFAState)n.getStartState()).addTransition('e', (NFAState)n2.getStartState());
                
                //Add states and alphabets from n1 and n2
                n.addNFAStates(n1.getStates());
                n.addNFAStates(n2.getStates());
                n.addAbc(n1.getABC());
                n.addAbc(n2.getABC());
                
                //Create new final state and make transitions from old final states
                String newFinalName = String.valueOf(counter);
                counter++;
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
