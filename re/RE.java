package re;

import fa.nfa.NFA;

public class RE implements REInterface {

        private String regEx;
        
        public RE(String regEx)
        {
                this.regEx = regEx;
        }
        public NFA getNFA()
        {
                return null;
        }

}
