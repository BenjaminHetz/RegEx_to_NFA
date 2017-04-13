package re;

import fa.nfa.NFA;

public class RE implements REInterface {

        private NFA parsed;
        
        public RE(String regEx)
        {
                parsed = getNFA();
        }
        public NFA getNFA()
        {
                return null;
        }

}
